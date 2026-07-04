package com.warehouse.dto;

import com.warehouse.model.Mission;
import com.warehouse.model.Robot;
import com.warehouse.service.RobotBatteryDrainService.BatteryDrainResult;
import java.util.List;

public class RobotTaskGroupDto {

    private final Robot robot;
    private final List<Mission> activeMissions;
    private final long pendingConfirmationCount;
    private final long highPriorityMissionCount;
    private final BatteryDrainResult batteryStatus;
    private final LiveMapRobotStateDto liveState;
    private static final String NORMAL_STRATEGY = "NormalStrategy";

    public RobotTaskGroupDto(Robot robot,
                             List<Mission> activeMissions,
                             long highPriorityMissionCount) {
        this(robot, activeMissions, 0, highPriorityMissionCount, null, null);
    }

    public RobotTaskGroupDto(Robot robot,
                             List<Mission> activeMissions,
                             long highPriorityMissionCount,
                             BatteryDrainResult batteryStatus) {
        this(robot, activeMissions, 0, highPriorityMissionCount, batteryStatus, null);
    }

    public RobotTaskGroupDto(Robot robot,
                             List<Mission> activeMissions,
                             long pendingConfirmationCount,
                             long highPriorityMissionCount,
                             BatteryDrainResult batteryStatus,
                             LiveMapRobotStateDto liveState) {
        this.robot = robot;
        this.activeMissions = activeMissions;
        this.pendingConfirmationCount = pendingConfirmationCount;
        this.highPriorityMissionCount = highPriorityMissionCount;
        this.batteryStatus = batteryStatus;
        this.liveState = liveState;
    }

    public Robot getRobot() {
        return robot;
    }

    public List<Mission> getActiveMissions() {
        return activeMissions;
    }

    public long getActiveMissionCount() {
        return activeMissions.size();
    }

    public long getPendingConfirmationCount() {
        return pendingConfirmationCount;
    }

    public long getHighPriorityMissionCount() {
        return highPriorityMissionCount;
    }

    public BatteryDrainResult getBatteryStatus() {
        return batteryStatus;
    }

    public LiveMapRobotStateDto getLiveState() {
        return liveState;
    }

    public boolean hasActiveMissions() {
        return !activeMissions.isEmpty();
    }

    public boolean isCharging() {
        return liveState != null ? liveState.charging() : batteryStatus != null && batteryStatus.charging();
    }

    public boolean isLowBattery() {
        return liveState != null ? liveState.lowBattery() : batteryStatus != null && batteryStatus.lowBattery();
    }

    public boolean isCriticalBattery() {
        return liveState != null
                ? liveState.criticalBattery()
                : batteryStatus != null && batteryStatus.criticalBattery();
    }

    public int getBatteryPercent() {
        if (liveState != null && liveState.batteryPercent() != null) {
            return liveState.batteryPercent();
        }
        if (batteryStatus != null) {
            return batteryStatus.batteryPercent();
        }
        return robot != null && robot.getBattery() != null ? robot.getBattery() : 0;
    }

    public String getBatteryDisplayText() {
        if (liveState != null && !isBlank(liveState.batteryDisplayText())) {
            return liveState.batteryDisplayText();
        }
        if (batteryStatus != null && !isBlank(batteryStatus.batteryDisplayText())) {
            return batteryStatus.batteryDisplayText();
        }
        return robot != null && robot.getBattery() != null ? robot.getBattery() + "%" : "N/A";
    }

    public String getBatteryMessage() {
        if (liveState != null && !isBlank(liveState.batteryMessage())) {
            return liveState.batteryMessage();
        }
        return batteryStatus != null ? batteryStatus.batteryMessage() : "";
    }

    public String getCurrentStrategyDisplay() {
        if (isCharging()) {
            return "ChargingStrategy";
        }
        if (!hasActiveMissions()) {
            return "Idle";
        }
        String strategyName = liveState != null ? liveState.currentActiveStrategyName() : null;
        return isRuntimeStrategy(strategyName) ? strategyName : "No active strategy";
    }

    public String getPrimaryStrategyDisplay() {
        if (isCharging()) {
            return "ChargingStrategy";
        }
        if (!hasActiveMissions()) {
            return "Idle";
        }
        String strategyName = liveState != null ? liveState.primaryStrategyName() : null;
        return isRuntimeStrategy(strategyName) ? strategyName : "No active strategy";
    }

    public String getMovementModeDisplayText() {
        if (liveState == null || isCharging() || isBlank(liveState.movementModeDisplay())) {
            return "";
        }
        return liveState.movementModeDisplay();
    }

    public boolean isPrimaryStrategyDifferent() {
        return liveState != null
                && hasActiveMissions()
                && isRuntimeStrategy(liveState.primaryStrategyName())
                && isRuntimeStrategy(liveState.currentActiveStrategyName())
                && !liveState.primaryStrategyName().equals(liveState.currentActiveStrategyName());
    }

    private boolean isRuntimeStrategy(String strategyName) {
        return !isBlank(strategyName) && !NORMAL_STRATEGY.equals(strategyName);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
