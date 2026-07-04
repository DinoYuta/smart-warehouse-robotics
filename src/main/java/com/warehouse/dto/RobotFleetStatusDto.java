package com.warehouse.dto;

import com.warehouse.model.Robot;
import com.warehouse.service.RobotBatteryDrainService.BatteryDrainResult;

public record RobotFleetStatusDto(Robot robot,
                                  BatteryDrainResult batteryStatus,
                                  String statusLabel,
                                  String statusBadgeClass,
                                  LiveMapRobotStateDto liveState) {

    private static final String NORMAL_STRATEGY = "NormalStrategy";

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

    public String getBatteryWarningLevel() {
        if (liveState != null && !isBlank(liveState.batteryWarningLevel())) {
            return liveState.batteryWarningLevel();
        }
        return batteryStatus != null ? batteryStatus.batteryWarningLevel() : "NORMAL";
    }

    public String getBatteryMessage() {
        if (liveState != null && !isBlank(liveState.batteryMessage())) {
            return liveState.batteryMessage();
        }
        return batteryStatus != null ? batteryStatus.batteryMessage() : "";
    }

    public boolean hasActiveMission() {
        return liveState != null && liveState.missionId() != null;
    }

    public String getPrimaryStrategyDisplay() {
        if (isCharging()) {
            return "ChargingStrategy";
        }
        if (!hasActiveMission()) {
            return "Idle";
        }
        String strategyName = liveState != null ? liveState.primaryStrategyName() : null;
        return isRuntimeStrategy(strategyName) ? strategyName : "No active strategy";
    }

    public String getCurrentStrategyDisplay() {
        if (isCharging()) {
            return "ChargingStrategy";
        }
        if (!hasActiveMission()) {
            return "Idle";
        }
        String strategyName = liveState != null ? liveState.currentActiveStrategyName() : null;
        return isRuntimeStrategy(strategyName) ? strategyName : "No active strategy";
    }

    public String getStrategyMessageDisplay() {
        if (liveState == null
                || (!hasActiveMission() && !isCharging())
                || !isRuntimeStrategy(liveState.currentActiveStrategyName())) {
            return "";
        }
        return liveState.strategyMessage() != null ? liveState.strategyMessage() : "";
    }

    public String getMovementModeDisplayText() {
        if (liveState == null || isCharging() || isBlank(liveState.movementModeDisplay())) {
            return "";
        }
        return liveState.movementModeDisplay();
    }

    public boolean isPrimaryStrategyDifferent() {
        return hasActiveMission()
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
