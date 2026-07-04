package com.warehouse.service;

import com.warehouse.dto.LiveMapRobotStateDto;
import com.warehouse.dto.LiveMapRouteStepDto;
import com.warehouse.dto.LiveMapStateDto;
import com.warehouse.dto.MissionExecutionProgressDto;
import com.warehouse.dto.MissionRouteStep;
import com.warehouse.model.Mission;
import com.warehouse.model.MissionExecutionStep;
import com.warehouse.model.MissionStatus;
import com.warehouse.model.Robot;
import com.warehouse.model.RobotMovementMode;
import com.warehouse.service.RobotBatteryDrainService.BatteryDrainResult;
import com.warehouse.service.RobotExecutionBehaviorService.MovementPlan;
import com.warehouse.service.RobotExecutionBehaviorService.StrategyBehavior;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LiveMapStateService {

    private static final String BASE_STATION_POSITION_KEY = "base-station";
    private static final String NO_ACTIVE_MISSION_MESSAGE = "No active pickup mission assigned.";
    private static final String WAITING_FOR_BRIDGE_MESSAGE = "Waiting for bridge path to clear.";
    private static final List<BridgeSegment> CRITICAL_BRIDGE_SEGMENTS = List.of(
            new BridgeSegment("bridge-c-b-left", List.of("bridge-c-b-left-1", "bridge-c-b-left-2")),
            new BridgeSegment("bridge-b-a-left", List.of("bridge-b-a-left-1", "bridge-b-a-left-2")),
            new BridgeSegment("bridge-a-b-right", List.of("bridge-a-b-right-1", "bridge-a-b-right-2")),
            new BridgeSegment("bridge-b-c-right", List.of("bridge-b-c-right-1", "bridge-b-c-right-2"))
    );
    private final RobotService robotService;
    private final MissionService missionService;
    private final WarehouseRouteService warehouseRouteService;
    private final MissionExecutionProgressService missionExecutionProgressService;
    private final RobotBatteryDrainService robotBatteryDrainService;
    private final RobotChargingService robotChargingService;
    private final RobotMissionBatteryService robotMissionBatteryService;
    private final RobotExecutionBehaviorService robotExecutionBehaviorService;

    public LiveMapStateService(RobotService robotService,
                               MissionService missionService,
                               WarehouseRouteService warehouseRouteService,
                               MissionExecutionProgressService missionExecutionProgressService,
                               RobotBatteryDrainService robotBatteryDrainService,
                               RobotChargingService robotChargingService,
                               RobotMissionBatteryService robotMissionBatteryService,
                               RobotExecutionBehaviorService robotExecutionBehaviorService) {
        this.robotService = robotService;
        this.missionService = missionService;
        this.warehouseRouteService = warehouseRouteService;
        this.missionExecutionProgressService = missionExecutionProgressService;
        this.robotBatteryDrainService = robotBatteryDrainService;
        this.robotChargingService = robotChargingService;
        this.robotMissionBatteryService = robotMissionBatteryService;
        this.robotExecutionBehaviorService = robotExecutionBehaviorService;
    }

    public LiveMapStateDto getLiveMapState() {
        // Live Map state is backend-driven; the frontend only renders the returned DTO.
        List<Robot> robots = robotService.getRobots();
        List<Mission> activeMissions = missionService.getActiveMissions(missionService.getMissionsNewestFirst());
        List<RobotMissionProgress> progressRows = robots.stream()
                .map(robot -> buildRobotMissionProgress(robot, activeMissions))
                .toList();
        Map<String, RobotMissionProgress> bridgeOccupants = findBridgeOccupants(progressRows);

        List<LiveMapRobotStateDto> robotStates = progressRows.stream()
                .map(progressRow -> buildRobotState(progressRow, bridgeOccupants))
                .toList();

        return new LiveMapStateDto(robotStates);
    }

    private RobotMissionProgress buildRobotMissionProgress(Robot robot, List<Mission> activeMissions) {
        if (robotChargingService.currentBatteryStatus(robot).charging()) {
            return new RobotMissionProgress(robot, null, List.of(), null, null);
        }

        Mission currentMission = findCurrentMission(activeMissions, robot);
        if (currentMission == null) {
            return new RobotMissionProgress(robot, null, List.of(), null, null);
        }

        if (currentMission.getStatus() == MissionStatus.IN_PROGRESS) {
            robotMissionBatteryService.captureExecutionStartBattery(currentMission);
        }
        MovementPlan movementPlan = robotExecutionBehaviorService.movementPlanFor(currentMission, robot);
        List<MissionRouteStep> missionRoute = warehouseRouteService.buildExecutionRoute(currentMission);
        MissionExecutionProgressDto progress = missionExecutionProgressService.calculateProgress(
                currentMission,
                missionRoute,
                movementPlan.outboundMovementMode(),
                movementPlan.returnMovementMode()
        );
        if (currentMission.getStatus() == MissionStatus.IN_PROGRESS
                && progress.executionStep() == MissionExecutionStep.RETURNED_TO_BASE) {
            currentMission = missionService.markReturnedToBase(currentMission.getId()).mission();
            progress = new MissionExecutionProgressDto(
                    BASE_STATION_POSITION_KEY,
                    MissionExecutionStep.RETURNED_TO_BASE,
                    progress.phase(),
                    MissionExecutionProgressService.RETURNED_TO_BASE_MESSAGE,
                    progress.elapsedSeconds(),
                    null,
                    0.0,
                    progress.traveledWaypointCount()
            );
        }
        return new RobotMissionProgress(robot, currentMission, missionRoute, progress, movementPlan);
    }

    private LiveMapRobotStateDto buildRobotState(RobotMissionProgress progressRow,
                                                 Map<String, RobotMissionProgress> bridgeOccupants) {
        if (progressRow.mission() == null) {
            return buildFallbackState(progressRow.robot());
        }

        return buildMissionState(progressRow, bridgeOccupants);
    }

    private LiveMapRobotStateDto buildFallbackState(Robot robot) {
        BatteryDrainResult battery = robotChargingService.currentBatteryStatus(robot);
        if (battery.charging()) {
            return buildChargingState(robot, battery);
        }
        RobotMovementMode movementMode = robotBatteryDrainService.resolveMovementMode(null, robot);
        battery = robotBatteryDrainService.buildBatteryResult(robot.getBattery(), 0, movementMode);
        StrategyBehavior behavior = robotExecutionBehaviorService.behaviorFor(null, robot, null);

        return new LiveMapRobotStateDto(
                robot.getId(),
                robot.getName(),
                robot.getCode(),
                resolveRobotColor(robot),
                battery.batteryLevel(),
                battery.batteryPercent(),
                battery.batteryDrainPercent(),
                battery.batteryDisplayText(),
                battery.batteryWarningLevel(),
                battery.lowBattery(),
                battery.criticalBattery(),
                battery.energySavingMode(),
                battery.chargingRequired(),
                battery.batteryMessage(),
                battery.charging(),
                battery.chargingRecoveredPercent(),
                battery.chargingDisplayText(),
                battery.movementMode(),
                battery.movementModeDisplay(),
                battery.waypointsPerBatteryPercent(),
                battery.movementMode(),
                behavior.primaryStrategyName(),
                behavior.currentActiveStrategyName(),
                behavior.strategyMessage(),
                robot.getStatus(),
                null,
                null,
                null,
                null,
                BASE_STATION_POSITION_KEY,
                null,
                0.0,
                null,
                null,
                List.of(),
                NO_ACTIVE_MISSION_MESSAGE,
                false,
                null
        );
    }

    private LiveMapRobotStateDto buildChargingState(Robot robot, BatteryDrainResult battery) {
        StrategyBehavior behavior = robotExecutionBehaviorService.chargingBehavior();
        return new LiveMapRobotStateDto(
                robot.getId(),
                robot.getName(),
                robot.getCode(),
                resolveRobotColor(robot),
                battery.batteryLevel(),
                battery.batteryPercent(),
                battery.batteryDrainPercent(),
                battery.batteryDisplayText(),
                battery.batteryWarningLevel(),
                battery.lowBattery(),
                battery.criticalBattery(),
                battery.energySavingMode(),
                battery.chargingRequired(),
                battery.batteryMessage(),
                battery.charging(),
                battery.chargingRecoveredPercent(),
                battery.chargingDisplayText(),
                behavior.movementMode().name(),
                behavior.movementMode().getDisplayName(),
                null,
                behavior.batteryDrainMode().name(),
                behavior.primaryStrategyName(),
                behavior.currentActiveStrategyName(),
                behavior.strategyMessage(),
                robot.getStatus(),
                null,
                null,
                robot.getStatus(),
                null,
                RobotChargingService.CHARGING_STATION_POSITION_KEY,
                null,
                0.0,
                null,
                null,
                List.of(),
                RobotChargingService.CHARGING_MESSAGE,
                false,
                null
        );
    }

    private LiveMapRobotStateDto buildMissionState(RobotMissionProgress progressRow,
                                                   Map<String, RobotMissionProgress> bridgeOccupants) {
        Robot robot = progressRow.robot();
        Mission mission = progressRow.mission();
        BatteryDrainResult currentBatteryStatus = robotChargingService.currentBatteryStatus(robot);
        if (currentBatteryStatus.charging()) {
            return buildChargingState(robot, currentBatteryStatus);
        }
        MissionExecutionProgressDto progress = applyBridgeWaitingIfNeeded(progressRow, bridgeOccupants);
        StrategyBehavior behavior = robotExecutionBehaviorService.behaviorFor(mission, robot, progress);
        BatteryDrainResult battery = mission.getStatus() == MissionStatus.IN_PROGRESS
                ? robotMissionBatteryService.calculateAndPersistBatteryForTraveledWaypoints(
                        mission,
                        robot,
                        progress.traveledWaypointCount(),
                        behavior.batteryDrainMode()
                )
                : robotBatteryDrainService.buildBatteryResult(robot.getBattery(), 0, behavior.batteryDrainMode());
        List<LiveMapRouteStepDto> route = progressRow.route().stream()
                .map(LiveMapRouteStepDto::from)
                .toList();
        boolean chargingRequired = battery.chargingRequired() || behavior.chargingRequiredByStrategy();
        String displayMessage = behavior.currentActiveStrategyName() != null
                && behavior.currentActiveStrategyName().equals(
                        RobotExecutionBehaviorService.OBSTACLE_AVOIDANCE_STRATEGY
                )
                ? behavior.strategyMessage()
                : progress.message();

        return new LiveMapRobotStateDto(
                robot.getId(),
                robot.getName(),
                robot.getCode(),
                resolveRobotColor(robot),
                battery.batteryLevel(),
                battery.batteryPercent(),
                battery.batteryDrainPercent(),
                battery.batteryDisplayText(),
                battery.batteryWarningLevel(),
                battery.lowBattery(),
                battery.criticalBattery(),
                battery.energySavingMode(),
                chargingRequired,
                battery.batteryMessage(),
                battery.charging(),
                battery.chargingRecoveredPercent(),
                battery.chargingDisplayText(),
                behavior.movementMode().name(),
                behavior.movementMode().getDisplayName(),
                behavior.movementMode().getWaypointsPerBatteryPercent(),
                behavior.batteryDrainMode().name(),
                behavior.primaryStrategyName(),
                behavior.currentActiveStrategyName(),
                behavior.strategyMessage(),
                robot.getStatus(),
                mission.getId(),
                mission.getRequestCode(),
                mission.getStatus().name(),
                progress.executionStep().name(),
                progress.positionKey(),
                progress.nextPositionKey(),
                progress.segmentProgress(),
                mission.getZone(),
                normalizeLocationCode(mission.getLocationCode()),
                route,
                displayMessage,
                progress.waiting(),
                progress.blockedSegment()
        );
    }

    private MissionExecutionProgressDto applyBridgeWaitingIfNeeded(RobotMissionProgress progressRow,
                                                                   Map<String, RobotMissionProgress> bridgeOccupants) {
        Mission mission = progressRow.mission();
        MissionExecutionProgressDto progress = progressRow.progress();
        Optional<BridgeSegment> bridgeSegment = bridgeSegmentForProgress(progress);

        if (mission == null || mission.getStatus() != MissionStatus.IN_PROGRESS || bridgeSegment.isEmpty()) {
            return progress;
        }

        RobotMissionProgress occupant = bridgeOccupants.get(bridgeSegment.get().key());
        if (occupant == null || isSameMission(progressRow, occupant)) {
            return progress;
        }

        return new MissionExecutionProgressDto(
                previousSafeWaypointFor(progressRow.route(), bridgeSegment.get()),
                progress.executionStep(),
                progress.phase(),
                WAITING_FOR_BRIDGE_MESSAGE,
                progress.elapsedSeconds(),
                null,
                0.0,
                progress.traveledWaypointCount(),
                true,
                bridgeSegment.get().key()
        );
    }

    private Map<String, RobotMissionProgress> findBridgeOccupants(List<RobotMissionProgress> progressRows) {
        Map<String, RobotMissionProgress> bridgeOccupants = new HashMap<>();

        for (RobotMissionProgress progressRow : progressRows) {
            Mission mission = progressRow.mission();
            MissionExecutionProgressDto progress = progressRow.progress();
            if (mission == null || progress == null || mission.getStatus() != MissionStatus.IN_PROGRESS) {
                continue;
            }

            bridgeSegmentForProgress(progress).ifPresent(bridgeSegment -> {
                RobotMissionProgress currentOccupant = bridgeOccupants.get(bridgeSegment.key());
                if (currentOccupant == null || compareBridgePriority(progressRow, currentOccupant) < 0) {
                    bridgeOccupants.put(bridgeSegment.key(), progressRow);
                }
            });
        }

        return bridgeOccupants;
    }

    private Optional<BridgeSegment> bridgeSegmentForPosition(String positionKey) {
        if (positionKey == null || positionKey.isBlank()) {
            return Optional.empty();
        }

        return CRITICAL_BRIDGE_SEGMENTS.stream()
                .filter(bridgeSegment -> bridgeSegment.positionKeys().contains(positionKey))
                .findFirst();
    }

    private Optional<BridgeSegment> bridgeSegmentForProgress(MissionExecutionProgressDto progress) {
        if (progress == null) {
            return Optional.empty();
        }

        return bridgeSegmentForPosition(progress.positionKey())
                .or(() -> bridgeSegmentForPosition(progress.nextPositionKey()));
    }

    private String previousSafeWaypointFor(List<MissionRouteStep> route, BridgeSegment bridgeSegment) {
        for (int index = 0; index < route.size(); index++) {
            if (bridgeSegment.positionKeys().contains(route.get(index).positionKey())) {
                return route.get(Math.max(0, index - 1)).positionKey();
            }
        }
        return BASE_STATION_POSITION_KEY;
    }

    private int compareBridgePriority(RobotMissionProgress first, RobotMissionProgress second) {
        return Comparator
                .comparing((RobotMissionProgress row) -> row.mission().getExecutionStartedAt(),
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(row -> row.mission().getCreatedAt(), Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(row -> row.mission().getId(), Comparator.nullsLast(Comparator.naturalOrder()))
                .compare(first, second);
    }

    private boolean isSameMission(RobotMissionProgress first, RobotMissionProgress second) {
        Long firstMissionId = first.mission().getId();
        Long secondMissionId = second.mission().getId();
        return firstMissionId != null && firstMissionId.equals(secondMissionId);
    }

    private Mission findCurrentMission(List<Mission> activeMissions, Robot robot) {
        List<Mission> assignedMissions = activeMissions.stream()
                .filter(mission -> isMissionAssignedToRobot(mission, robot))
                .toList();

        return findOldestMissionWithStatus(assignedMissions, MissionStatus.IN_PROGRESS)
                .or(() -> findOldestMissionWithStatus(assignedMissions, MissionStatus.ASSIGNED))
                .or(() -> findOldestMissionWithStatus(assignedMissions, MissionStatus.PENDING))
                .or(() -> findOldestMissionWithStatus(assignedMissions, MissionStatus.WAITING_CONFIRMATION))
                .orElse(null);
    }

    private Optional<Mission> findOldestMissionWithStatus(List<Mission> missions, MissionStatus status) {
        return missions.stream()
                .filter(mission -> mission.getStatus() == status)
                .min(Comparator
                        .comparing(Mission::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Mission::getId, Comparator.nullsLast(Comparator.naturalOrder())));
    }

    private boolean isMissionAssignedToRobot(Mission mission, Robot robot) {
        if (mission.getAssignedRobotId() != null && mission.getAssignedRobotId().equals(robot.getId())) {
            return true;
        }

        String assignedRobotName = mission.getAssignedRobotName();
        if (isBlank(assignedRobotName)) {
            return false;
        }

        String normalizedAssignedRobotName = normalize(assignedRobotName);
        if (!isBlank(robot.getName()) && normalizedAssignedRobotName.contains(normalize(robot.getName()))) {
            return true;
        }

        return !isBlank(robot.getCode()) && normalizedAssignedRobotName.contains(normalize(robot.getCode()));
    }

    private String resolveRobotColor(Robot robot) {
        String robotText = normalize(robot.getName() + " " + robot.getCode());
        if (robotText.contains("alpha") || robotText.contains("picker")) {
            return "green";
        }
        if (robotText.contains("beta") || robotText.contains("mover")) {
            return "red";
        }
        if (robotText.contains("gamma") || robotText.contains("carrier")) {
            return "blue";
        }
        return "default";
    }

    private String normalizeLocationCode(String locationCode) {
        return isBlank(locationCode) ? null : locationCode.trim().toUpperCase(Locale.US);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.US);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record RobotMissionProgress(Robot robot,
                                        Mission mission,
                                        List<MissionRouteStep> route,
                                        MissionExecutionProgressDto progress,
                                        MovementPlan movementPlan) {
    }

    private record BridgeSegment(String key, List<String> positionKeys) {
    }
}
