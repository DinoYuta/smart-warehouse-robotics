package com.warehouse.service;

import com.warehouse.model.Mission;
import com.warehouse.model.Robot;
import com.warehouse.model.RobotMovementMode;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class RobotBatteryDrainService {

    private static final int MIN_BATTERY_PERCENT = 0;
    private static final int MAX_BATTERY_PERCENT = 100;
    private static final int LOW_BATTERY_THRESHOLD = 20;
    private static final int CRITICAL_BATTERY_THRESHOLD = 5;
    private static final String NORMAL_WARNING_LEVEL = "NORMAL";
    private static final String LOW_WARNING_LEVEL = "LOW";
    private static final String CRITICAL_WARNING_LEVEL = "CRITICAL";
    private static final String NORMAL_BATTERY_MESSAGE = "Battery level normal.";
    private static final String ENERGY_SAVING_MESSAGE = "Energy saving mode active.";
    private static final String CHARGING_REQUIRED_MESSAGE = "Charging required after current mission.";
    private static final String CHARGING_MESSAGE = "Charging at Charging Station.";
    private static final int CHARGING_RECOVERY_SECONDS = 10;
    private static final int CHARGING_RECOVERY_PERCENT = 5;

    public BatteryDrainResult calculateEffectiveBattery(Integer storedBatteryPercent, int traveledWaypointCount) {
        return calculateEffectiveBattery(storedBatteryPercent, traveledWaypointCount, RobotMovementMode.NORMAL);
    }

    public BatteryDrainResult calculateEffectiveBattery(Integer storedBatteryPercent,
                                                        int traveledWaypointCount,
                                                        RobotMovementMode movementMode) {
        RobotMovementMode resolvedMovementMode = resolveMovementMode(movementMode);
        int batteryDrainPercent = calculateBatteryDrainPercent(traveledWaypointCount, resolvedMovementMode);
        int batteryPercent = Math.max(
                MIN_BATTERY_PERCENT,
                clampBatteryPercent(storedBatteryPercent) - batteryDrainPercent
        );
        return buildBatteryResult(batteryPercent, batteryDrainPercent, resolvedMovementMode);
    }

    public BatteryDrainResult buildBatteryResult(Integer batteryPercent,
                                                 int batteryDrainPercent,
                                                 RobotMovementMode movementMode) {
        RobotMovementMode resolvedMovementMode = resolveMovementMode(movementMode);
        int safeBatteryPercent = clampBatteryPercent(batteryPercent);
        int safeBatteryDrainPercent = Math.max(0, batteryDrainPercent);
        boolean criticalBattery = safeBatteryPercent <= CRITICAL_BATTERY_THRESHOLD;
        boolean lowBattery = safeBatteryPercent < LOW_BATTERY_THRESHOLD;
        boolean energySavingMode = lowBattery || resolvedMovementMode == RobotMovementMode.ENERGY_SAVING;
        boolean chargingRequired = criticalBattery;
        String batteryWarningLevel = determineBatteryWarningLevel(safeBatteryPercent);

        return new BatteryDrainResult(
                safeBatteryPercent,
                safeBatteryPercent,
                safeBatteryDrainPercent,
                formatBatteryDisplayText(safeBatteryPercent, safeBatteryDrainPercent),
                batteryWarningLevel,
                lowBattery,
                criticalBattery,
                energySavingMode,
                chargingRequired,
                determineBatteryMessage(batteryWarningLevel),
                false,
                0,
                null,
                resolvedMovementMode.name(),
                resolvedMovementMode.getDisplayName(),
                resolvedMovementMode.getWaypointsPerBatteryPercent()
        );
    }

    public RobotMovementMode resolveMovementMode(Mission mission, Robot robot) {
        if (mission != null) {
            String strategyName = normalize(mission.getSelectedStrategyName());
            if (strategyName.equals("fastroutestrategy")) {
                return RobotMovementMode.FAST;
            }
            if (strategyName.equals("energysavingstrategy")) {
                return RobotMovementMode.ENERGY_SAVING;
            }
            if (strategyName.equals("saferoutestrategy")) {
                return RobotMovementMode.SAFE;
            }
            if (strategyName.equals("heavyloadstrategy")) {
                return RobotMovementMode.HEAVY_LOAD;
            }
            if (strategyName.equals("obstacleavoidancestrategy")) {
                return RobotMovementMode.OBSTACLE_AVOIDANCE;
            }
            if (strategyName.equals("chargingstrategy")) {
                return RobotMovementMode.NORMAL;
            }
        }

        Integer battery = mission != null && mission.getBatteryAtExecutionStart() != null
                ? mission.getBatteryAtExecutionStart()
                : (robot != null ? robot.getBattery() : null);
        if (battery != null && clampBatteryPercent(battery) < LOW_BATTERY_THRESHOLD) {
            return RobotMovementMode.ENERGY_SAVING;
        }

        return RobotMovementMode.NORMAL;
    }

    public int clampBatteryPercent(Integer batteryPercent) {
        if (batteryPercent == null) {
            return MIN_BATTERY_PERCENT;
        }

        return Math.max(MIN_BATTERY_PERCENT, Math.min(MAX_BATTERY_PERCENT, batteryPercent));
    }

    private BatteryDrainResult buildChargingBatteryResult(int batteryPercent,
                                                          int displayedRecoveredPercent,
                                                          String batteryDisplayText) {
        boolean criticalBattery = batteryPercent <= CRITICAL_BATTERY_THRESHOLD;
        boolean lowBattery = batteryPercent < LOW_BATTERY_THRESHOLD;
        boolean energySavingMode = lowBattery;
        boolean chargingRequired = batteryPercent < MAX_BATTERY_PERCENT;
        String batteryWarningLevel = determineBatteryWarningLevel(batteryPercent);

        return new BatteryDrainResult(
                batteryPercent,
                batteryPercent,
                0,
                batteryDisplayText,
                batteryWarningLevel,
                lowBattery,
                criticalBattery,
                energySavingMode,
                chargingRequired,
                CHARGING_MESSAGE,
                chargingRequired,
                displayedRecoveredPercent,
                batteryDisplayText,
                null,
                null,
                null
        );
    }

    public BatteryDrainResult calculateChargingBattery(Integer batteryBeforeCharging, long chargingElapsedSeconds) {
        int baseBattery = clampBatteryPercent(batteryBeforeCharging);
        int chargingRecoveredPercent = calculateChargingRecoveredPercent(chargingElapsedSeconds);
        int batteryPercent = clampBatteryPercent(baseBattery + chargingRecoveredPercent);
        boolean criticalBattery = batteryPercent <= CRITICAL_BATTERY_THRESHOLD;
        boolean chargingRequired = batteryPercent < MAX_BATTERY_PERCENT;
        int displayedRecoveredPercent = Math.max(0, batteryPercent - baseBattery);
        String batteryDisplayText = displayedRecoveredPercent > 0
                ? batteryPercent + "% battery (charging +" + displayedRecoveredPercent + "%)"
                : batteryPercent + "% battery (charging)";

        return buildChargingBatteryResult(batteryPercent, displayedRecoveredPercent, batteryDisplayText);
    }

    public int calculateBatteryDrainPercent(int traveledWaypointCount) {
        return calculateBatteryDrainPercent(traveledWaypointCount, RobotMovementMode.NORMAL);
    }

    public int calculateBatteryDrainPercent(int traveledWaypointCount, RobotMovementMode movementMode) {
        int waypointsPerBatteryPercent = Math.max(1, resolveMovementMode(movementMode).getWaypointsPerBatteryPercent());
        return Math.max(0, traveledWaypointCount) / waypointsPerBatteryPercent;
    }

    public int calculateChargingRecoveredPercent(long chargingElapsedSeconds) {
        long safeElapsedSeconds = Math.max(0, chargingElapsedSeconds);
        long chargingIntervals = safeElapsedSeconds / CHARGING_RECOVERY_SECONDS;
        long recoveredPercent = chargingIntervals * CHARGING_RECOVERY_PERCENT;
        return (int) Math.min(MAX_BATTERY_PERCENT, recoveredPercent);
    }

    private String formatBatteryDisplayText(int batteryPercent, int batteryDrainPercent) {
        if (batteryDrainPercent > 0) {
            return batteryPercent + "% battery (" + batteryDrainPercent + "% route drain)";
        }

        return batteryPercent + "% battery";
    }

    private String determineBatteryWarningLevel(int batteryPercent) {
        if (batteryPercent <= CRITICAL_BATTERY_THRESHOLD) {
            return CRITICAL_WARNING_LEVEL;
        }
        if (batteryPercent < LOW_BATTERY_THRESHOLD) {
            return LOW_WARNING_LEVEL;
        }
        return NORMAL_WARNING_LEVEL;
    }

    private String determineBatteryMessage(String batteryWarningLevel) {
        return switch (batteryWarningLevel) {
            case CRITICAL_WARNING_LEVEL -> CHARGING_REQUIRED_MESSAGE;
            case LOW_WARNING_LEVEL -> ENERGY_SAVING_MESSAGE;
            default -> NORMAL_BATTERY_MESSAGE;
        };
    }

    private RobotMovementMode resolveMovementMode(RobotMovementMode movementMode) {
        return movementMode != null ? movementMode : RobotMovementMode.NORMAL;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.US);
    }

    public record BatteryDrainResult(int batteryLevel,
                                     int batteryPercent,
                                     int batteryDrainPercent,
                                     String batteryDisplayText,
                                     String batteryWarningLevel,
                                     boolean lowBattery,
                                     boolean criticalBattery,
                                     boolean energySavingMode,
                                     boolean chargingRequired,
                                     String batteryMessage,
                                     boolean charging,
                                     int chargingRecoveredPercent,
                                     String chargingDisplayText,
                                     String movementMode,
                                     String movementModeDisplay,
                                     Integer waypointsPerBatteryPercent) {
    }
}
