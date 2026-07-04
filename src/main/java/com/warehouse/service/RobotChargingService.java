package com.warehouse.service;

import com.warehouse.dto.MissionExecutionProgressDto;
import com.warehouse.dto.MissionRouteStep;
import com.warehouse.model.Mission;
import com.warehouse.model.MissionStatus;
import com.warehouse.model.Robot;
import com.warehouse.model.RobotMovementMode;
import com.warehouse.repository.MissionRepository;
import com.warehouse.repository.RobotRepository;
import com.warehouse.service.RobotAssignmentService.RobotAssignment;
import com.warehouse.service.RobotBatteryDrainService.BatteryDrainResult;
import com.warehouse.service.RobotExecutionBehaviorService.MovementPlan;
import com.warehouse.service.RobotExecutionBehaviorService.StrategyBehavior;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RobotChargingService {

    public static final String CHARGING_STATUS = "CHARGING";
    public static final String AVAILABLE_STATUS = "IDLE";
    public static final String CHARGING_STATION_POSITION_KEY = "charging-station";
    public static final String CHARGING_MESSAGE = "Charging at Charging Station.";

    private static final List<MissionStatus> QUEUED_REASSIGNMENT_STATUSES = List.of(
            MissionStatus.PENDING,
            MissionStatus.ASSIGNED
    );

    private final RobotRepository robotRepository;
    private final MissionRepository missionRepository;
    private final RobotAssignmentService robotAssignmentService;
    private final WarehouseRouteService warehouseRouteService;
    private final MissionExecutionProgressService missionExecutionProgressService;
    private final RobotBatteryDrainService robotBatteryDrainService;
    private final RobotMissionBatteryService robotMissionBatteryService;
    private final RobotExecutionBehaviorService robotExecutionBehaviorService;

    public RobotChargingService(RobotRepository robotRepository,
                                MissionRepository missionRepository,
                                RobotAssignmentService robotAssignmentService,
                                WarehouseRouteService warehouseRouteService,
                                MissionExecutionProgressService missionExecutionProgressService,
                                RobotBatteryDrainService robotBatteryDrainService,
                                RobotMissionBatteryService robotMissionBatteryService,
                                RobotExecutionBehaviorService robotExecutionBehaviorService) {
        this.robotRepository = robotRepository;
        this.missionRepository = missionRepository;
        this.robotAssignmentService = robotAssignmentService;
        this.warehouseRouteService = warehouseRouteService;
        this.missionExecutionProgressService = missionExecutionProgressService;
        this.robotBatteryDrainService = robotBatteryDrainService;
        this.robotMissionBatteryService = robotMissionBatteryService;
        this.robotExecutionBehaviorService = robotExecutionBehaviorService;
    }

    public ChargingDecision prepareChargingDecision(Mission mission) {
        Optional<Robot> assignedRobot = robotMissionBatteryService.findAssignedRobot(mission);
        if (assignedRobot.isEmpty()) {
            return ChargingDecision.notRequired();
        }

        Robot robot = assignedRobot.get();
        MissionProgressSnapshot progressSnapshot = progressSnapshotFor(mission, robot);
        RobotMovementMode batteryDrainMode = progressSnapshot.strategyBehavior() != null
                ? progressSnapshot.strategyBehavior().batteryDrainMode()
                : robotBatteryDrainService.resolveMovementMode(mission, robot);
        BatteryDrainResult battery = robotMissionBatteryService.calculateAndPersistBatteryForTraveledWaypoints(
                mission,
                robot,
                progressSnapshot.traveledWaypointCount(),
                batteryDrainMode
        );
        boolean chargingRequired = battery.chargingRequired()
                || robotExecutionBehaviorService.isChargingStrategySelected(mission);
        return new ChargingDecision(robot.getId(), battery.batteryPercent(), chargingRequired);
    }

    public ChargingWorkflowResult startChargingAfterMissionClosure(Mission closedMission,
                                                                   ChargingDecision chargingDecision) {
        if (chargingDecision == null || !chargingDecision.chargingRequired() || chargingDecision.robotId() == null) {
            return ChargingWorkflowResult.none();
        }

        Robot robot = robotRepository.findById(chargingDecision.robotId()).orElse(null);
        if (robot == null) {
            return ChargingWorkflowResult.none();
        }
        if (isChargingWorkflowActive(robot)) {
            return ChargingWorkflowResult.none();
        }

        startCharging(robot, chargingDecision.batteryPercent());
        // Queued missions are reassigned only when the robot enters charging.
        ReassignmentSummary reassignmentSummary = reassignRemainingQueuedMissions(robot, closedMission.getId());
        return new ChargingWorkflowResult(
                true,
                reassignmentSummary.reassignedCount(),
                reassignmentSummary.unassignedCount()
        );
    }

    public ChargingWorkflowResult updateRobotAvailabilityAfterMissionReturn(Mission returnedMission,
                                                                            ChargingDecision chargingDecision) {
        if (chargingDecision == null || chargingDecision.robotId() == null) {
            return ChargingWorkflowResult.none();
        }
        if (chargingDecision.chargingRequired()) {
            return startChargingAfterMissionClosure(returnedMission, chargingDecision);
        }

        robotRepository.findById(chargingDecision.robotId()).ifPresent(robot -> {
            if (isChargingWorkflowActive(robot)) {
                return;
            }
            boolean changed = false;
            if (!Integer.valueOf(chargingDecision.batteryPercent()).equals(robot.getBattery())) {
                robot.setBattery(chargingDecision.batteryPercent());
                changed = true;
            }
            if (Boolean.TRUE.equals(robot.getChargingRequired())) {
                robot.setChargingRequired(false);
                changed = true;
            }
            if (Boolean.TRUE.equals(robot.getCharging())) {
                robot.setCharging(false);
                changed = true;
            }
            if (!AVAILABLE_STATUS.equals(robot.getStatus())) {
                robot.setStatus(AVAILABLE_STATUS);
                changed = true;
            }
            if (changed) {
                robotRepository.save(robot);
            }
        });
        return ChargingWorkflowResult.none();
    }

    public BatteryDrainResult currentBatteryStatus(Robot robot) {
        if (!isChargingWorkflowActive(robot)) {
            return robotBatteryDrainService.calculateEffectiveBattery(robot.getBattery(), 0);
        }

        long elapsedSeconds = chargingElapsedSeconds(robot);
        BatteryDrainResult chargingBattery = robotBatteryDrainService.calculateChargingBattery(
                resolveBatteryBeforeCharging(robot),
                elapsedSeconds
        );

        if (chargingBattery.batteryPercent() >= 100) {
            completeCharging(robot);
            return robotBatteryDrainService.calculateEffectiveBattery(robot.getBattery(), 0);
        }

        return chargingBattery;
    }

    public boolean isChargingWorkflowActive(Robot robot) {
        return robot != null
                && Boolean.TRUE.equals(robot.getCharging())
                && robot.getChargingStartedAt() != null
                && robot.getChargingCompletedAt() == null;
    }

    private ReassignmentSummary reassignRemainingQueuedMissions(Robot chargingRobot, Long closedMissionId) {
        if (chargingRobot.getId() == null) {
            return new ReassignmentSummary(0, 0);
        }

        List<Mission> queuedMissions = missionRepository
                .findByAssignedRobotIdAndStatusInAndDeletedAtIsNullOrderByPriorityAscCreatedAtAscIdAsc(
                        chargingRobot.getId(),
                        QUEUED_REASSIGNMENT_STATUSES
                )
                .stream()
                .filter(mission -> closedMissionId == null || !closedMissionId.equals(mission.getId()))
                .toList();

        int reassignedCount = 0;
        int unassignedCount = 0;
        for (Mission queuedMission : queuedMissions) {
            Optional<RobotAssignment> reassignment = robotAssignmentService.selectRobotForMissionExcluding(
                    queuedMission,
                    Set.of(chargingRobot.getId())
            );

            if (reassignment.isPresent()) {
                assignQueuedMissionToRobot(queuedMission, chargingRobot, reassignment.get());
                reassignedCount++;
            } else {
                keepQueuedMissionPendingWithoutRobot(queuedMission, chargingRobot);
                unassignedCount++;
            }
            missionRepository.save(queuedMission);
        }

        return new ReassignmentSummary(reassignedCount, unassignedCount);
    }

    private void assignQueuedMissionToRobot(Mission mission,
                                            Robot chargingRobot,
                                            RobotAssignment reassignment) {
        Robot selectedRobot = reassignment.robot();
        mission.setAssignedRobotId(selectedRobot.getId());
        mission.setAssignedRobotName(formatRobotName(selectedRobot));
        mission.setStatus(MissionStatus.ASSIGNED);
        mission.setAssignmentReason("Reassigned from " + formatRobotName(chargingRobot)
                + " because battery was critical and charging is required after the current mission. "
                + reassignment.assignmentReason());
    }

    private void keepQueuedMissionPendingWithoutRobot(Mission mission, Robot chargingRobot) {
        mission.setAssignedRobotId(null);
        mission.setAssignedRobotName(null);
        mission.setStatus(MissionStatus.PENDING);
        mission.setAssignmentReason("Waiting for reassignment because " + formatRobotName(chargingRobot)
                + " requires charging and no other available robot was found.");
    }

    private void startCharging(Robot robot, int batteryAtChargingStart) {
        if (isChargingWorkflowActive(robot)) {
            return;
        }

        int safeBatteryAtStart = robotBatteryDrainService
                .calculateEffectiveBattery(batteryAtChargingStart, 0)
                .batteryPercent();
        robot.setBattery(safeBatteryAtStart);
        robot.setBatteryBeforeCharging(safeBatteryAtStart);
        robot.setChargingRequired(true);
        robot.setCharging(true);
        robot.setChargingStartedAt(LocalDateTime.now());
        robot.setChargingCompletedAt(null);
        robot.setStatus(CHARGING_STATUS);
        robotRepository.save(robot);
    }

    private void completeCharging(Robot robot) {
        robot.setBattery(100);
        robot.setCharging(false);
        robot.setChargingRequired(false);
        robot.setChargingCompletedAt(LocalDateTime.now());
        robot.setStatus(AVAILABLE_STATUS);
        robotRepository.save(robot);
    }

    private MissionProgressSnapshot progressSnapshotFor(Mission mission, Robot robot) {
        if (mission == null
                || mission.getStatus() != MissionStatus.IN_PROGRESS
                || mission.getExecutionStartedAt() == null) {
            StrategyBehavior behavior = mission != null
                    ? robotExecutionBehaviorService.behaviorFor(mission, robot, null)
                    : null;
            return new MissionProgressSnapshot(0, behavior);
        }

        try {
            List<MissionRouteStep> route = warehouseRouteService.buildExecutionRoute(mission);
            MovementPlan movementPlan = robotExecutionBehaviorService.movementPlanFor(mission, robot);
            MissionExecutionProgressDto progress = missionExecutionProgressService.calculateProgress(
                    mission,
                    route,
                    movementPlan.outboundMovementMode(),
                    movementPlan.returnMovementMode()
            );
            StrategyBehavior behavior = robotExecutionBehaviorService.behaviorFor(mission, robot, progress);
            return new MissionProgressSnapshot(progress.traveledWaypointCount(), behavior);
        } catch (IllegalArgumentException ex) {
            StrategyBehavior behavior = robotExecutionBehaviorService.behaviorFor(mission, robot, null);
            return new MissionProgressSnapshot(0, behavior);
        }
    }

    private long chargingElapsedSeconds(Robot robot) {
        return Math.max(0, Duration.between(robot.getChargingStartedAt(), LocalDateTime.now()).getSeconds());
    }

    private int resolveBatteryBeforeCharging(Robot robot) {
        if (robot.getBatteryBeforeCharging() != null) {
            return robot.getBatteryBeforeCharging();
        }
        return robot.getBattery() != null ? robot.getBattery() : 0;
    }

    private String formatRobotName(Robot robot) {
        if (robot.getCode() == null || robot.getCode().isBlank()) {
            return robot.getName();
        }
        return robot.getName() + " (" + robot.getCode() + ")";
    }

    private record ReassignmentSummary(int reassignedCount, int unassignedCount) {
    }

    private record MissionProgressSnapshot(int traveledWaypointCount,
                                           StrategyBehavior strategyBehavior) {
    }

    public record ChargingDecision(Long robotId, int batteryPercent, boolean chargingRequired) {

        private static ChargingDecision notRequired() {
            return new ChargingDecision(null, 0, false);
        }
    }

    public record ChargingWorkflowResult(boolean chargingStarted,
                                         int reassignedMissionCount,
                                         int unassignedMissionCount) {

        public static ChargingWorkflowResult none() {
            return new ChargingWorkflowResult(false, 0, 0);
        }
    }
}
