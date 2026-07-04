package com.warehouse.service;

import com.warehouse.model.Mission;
import com.warehouse.model.Robot;
import com.warehouse.model.RobotMovementMode;
import com.warehouse.repository.MissionRepository;
import com.warehouse.repository.RobotRepository;
import com.warehouse.service.RobotBatteryDrainService.BatteryDrainResult;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RobotMissionBatteryService {

    private final RobotRepository robotRepository;
    private final MissionRepository missionRepository;
    private final RobotBatteryDrainService robotBatteryDrainService;

    public RobotMissionBatteryService(RobotRepository robotRepository,
                                      MissionRepository missionRepository,
                                      RobotBatteryDrainService robotBatteryDrainService) {
        this.robotRepository = robotRepository;
        this.missionRepository = missionRepository;
        this.robotBatteryDrainService = robotBatteryDrainService;
    }

    public Optional<Robot> findAssignedRobot(Mission mission) {
        if (mission == null) {
            return Optional.empty();
        }
        if (mission.getAssignedRobotId() != null) {
            Optional<Robot> robotById = robotRepository.findById(mission.getAssignedRobotId());
            if (robotById.isPresent()) {
                return robotById;
            }
        }

        String assignedRobotName = mission.getAssignedRobotName();
        if (assignedRobotName == null || assignedRobotName.isBlank()) {
            return Optional.empty();
        }

        String normalizedAssignedRobotName = normalize(assignedRobotName);
        return robotRepository.findAllByOrderByIdAsc()
                .stream()
                .filter(robot -> matchesAssignedRobotName(robot, normalizedAssignedRobotName))
                .findFirst();
    }

    public void captureExecutionStartBattery(Mission mission) {
        if (mission == null || mission.getBatteryAtExecutionStart() != null) {
            return;
        }

        findAssignedRobot(mission).ifPresent(robot -> {
            mission.setBatteryAtExecutionStart(robotBatteryDrainService.clampBatteryPercent(robot.getBattery()));
            missionRepository.save(mission);
        });
    }

    public BatteryDrainResult calculateAndPersistBatteryForTraveledWaypoints(Mission mission,
                                                                             Robot robot,
                                                                             int traveledWaypointCount,
                                                                             RobotMovementMode movementMode) {
        Integer startingBattery = resolveExecutionStartBattery(mission, robot);
        BatteryDrainResult effectiveBattery = robotBatteryDrainService.calculateEffectiveBattery(
                startingBattery,
                traveledWaypointCount,
                movementMode
        );

        int storedBattery = robotBatteryDrainService.clampBatteryPercent(robot != null ? robot.getBattery() : null);
        int displayedBatteryPercent = Math.min(storedBattery, effectiveBattery.batteryPercent());
        int displayedDrainPercent = Math.max(
                effectiveBattery.batteryDrainPercent(),
                robotBatteryDrainService.clampBatteryPercent(startingBattery) - displayedBatteryPercent
        );

        if (robot != null && effectiveBattery.batteryPercent() < storedBattery) {
            robot.setBattery(effectiveBattery.batteryPercent());
            robotRepository.save(robot);
            displayedBatteryPercent = effectiveBattery.batteryPercent();
            displayedDrainPercent = effectiveBattery.batteryDrainPercent();
        }

        return robotBatteryDrainService.buildBatteryResult(
                displayedBatteryPercent,
                displayedDrainPercent,
                movementMode
        );
    }

    private Integer resolveExecutionStartBattery(Mission mission, Robot robot) {
        if (mission != null && mission.getBatteryAtExecutionStart() != null) {
            return mission.getBatteryAtExecutionStart();
        }

        Integer startBattery = robot != null ? robot.getBattery() : null;
        int safeStartBattery = robotBatteryDrainService.clampBatteryPercent(startBattery);
        if (mission != null) {
            mission.setBatteryAtExecutionStart(safeStartBattery);
            missionRepository.save(mission);
        }
        return safeStartBattery;
    }

    private boolean matchesAssignedRobotName(Robot robot, String normalizedAssignedRobotName) {
        return (!isBlank(robot.getName()) && normalizedAssignedRobotName.contains(normalize(robot.getName())))
                || (!isBlank(robot.getCode()) && normalizedAssignedRobotName.contains(normalize(robot.getCode())));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.US);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
