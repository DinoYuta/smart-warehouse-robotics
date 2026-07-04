package com.warehouse.service;

import com.warehouse.dto.LiveMapRobotStateDto;
import com.warehouse.dto.RobotFleetStatusDto;
import com.warehouse.model.Robot;
import com.warehouse.repository.RobotRepository;
import com.warehouse.service.RobotBatteryDrainService.BatteryDrainResult;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RobotFleetStatusService {

    private final RobotRepository robotRepository;
    private final RobotChargingService robotChargingService;
    private final LiveMapStateService liveMapStateService;

    public RobotFleetStatusService(RobotRepository robotRepository,
                                   RobotChargingService robotChargingService,
                                   LiveMapStateService liveMapStateService) {
        this.robotRepository = robotRepository;
        this.robotChargingService = robotChargingService;
        this.liveMapStateService = liveMapStateService;
    }

    public List<RobotFleetStatusDto> getRobotFleetStatus() {
        Map<Long, LiveMapRobotStateDto> liveStateByRobotId = liveMapStateService.getLiveMapState()
                .robots()
                .stream()
                .filter(state -> state.robotId() != null)
                .collect(Collectors.toMap(
                        LiveMapRobotStateDto::robotId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        return robotRepository.findAllByOrderByIdAsc()
                .stream()
                .map(robot -> buildRobotFleetStatus(robot, liveStateByRobotId.get(robot.getId())))
                .toList();
    }

    private RobotFleetStatusDto buildRobotFleetStatus(Robot robot, LiveMapRobotStateDto liveState) {
        BatteryDrainResult batteryStatus = robotChargingService.currentBatteryStatus(robot);
        boolean charging = liveState != null ? liveState.charging() : batteryStatus.charging();
        boolean criticalBattery = liveState != null ? liveState.criticalBattery() : batteryStatus.criticalBattery();
        boolean lowBattery = liveState != null ? liveState.lowBattery() : batteryStatus.lowBattery();
        boolean obstacleDetected = Boolean.TRUE.equals(robot.getObstacleDetected());
        String statusLabel = statusLabelFor(robot, liveState, charging, criticalBattery, lowBattery, obstacleDetected);
        String statusBadgeClass = statusBadgeClassFor(robot, charging, lowBattery, obstacleDetected);
        return new RobotFleetStatusDto(robot, batteryStatus, statusLabel, statusBadgeClass, liveState);
    }

    private String statusLabelFor(Robot robot,
                                  LiveMapRobotStateDto liveState,
                                  boolean charging,
                                  boolean criticalBattery,
                                  boolean lowBattery,
                                  boolean obstacleDetected) {
        if (charging) {
            return RobotChargingService.CHARGING_STATUS;
        }
        if (criticalBattery) {
            return "Charging Required";
        }
        if (lowBattery) {
            return "Energy Saving";
        }
        if (obstacleDetected) {
            return "Obstacle Detected";
        }
        if (liveState != null && liveState.robotStatus() != null && !liveState.robotStatus().isBlank()) {
            return liveState.robotStatus();
        }
        return robot.getStatus() != null ? robot.getStatus() : "Unknown";
    }

    private String statusBadgeClassFor(Robot robot,
                                       boolean charging,
                                       boolean lowBattery,
                                       boolean obstacleDetected) {
        if (charging) {
            return "status-charging";
        }
        if (lowBattery || obstacleDetected) {
            return "status-alert";
        }
        String status = robot.getStatus();
        if ("IDLE".equals(status)) {
            return "status-idle";
        }
        if ("MOVING".equals(status)) {
            return "status-moving";
        }
        if ("LOADED".equals(status)) {
            return "status-loaded";
        }
        return "status-default";
    }
}
