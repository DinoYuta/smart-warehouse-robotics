package com.warehouse.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.warehouse.model.RobotMovementMode;
import com.warehouse.service.RobotBatteryDrainService.BatteryDrainResult;
import org.junit.jupiter.api.Test;

class RobotBatteryDrainServiceTest {

    private final RobotBatteryDrainService robotBatteryDrainService = new RobotBatteryDrainService();

    @Test
    void normalModeUsesOnePercentForEachFiveCompletedWaypoints() {
        assertEffectiveBattery(100, 0, RobotMovementMode.NORMAL, 100, 0, 5);
        assertEffectiveBattery(100, 4, RobotMovementMode.NORMAL, 100, 0, 5);
        assertEffectiveBattery(100, 5, RobotMovementMode.NORMAL, 99, 1, 5);
        assertEffectiveBattery(100, 20, RobotMovementMode.NORMAL, 96, 4, 5);
    }

    @Test
    void fastAndEnergySavingModesUseStrategySpecificBatteryDrain() {
        assertEffectiveBattery(100, 18, RobotMovementMode.FAST, 94, 6, 3);
        assertEffectiveBattery(100, 18, RobotMovementMode.NORMAL, 97, 3, 5);
        assertEffectiveBattery(100, 21, RobotMovementMode.ENERGY_SAVING, 97, 3, 7);

        BatteryDrainResult fastBattery = robotBatteryDrainService.calculateEffectiveBattery(100, 21, RobotMovementMode.FAST);
        BatteryDrainResult normalBattery = robotBatteryDrainService.calculateEffectiveBattery(100, 21, RobotMovementMode.NORMAL);
        BatteryDrainResult energySavingBattery = robotBatteryDrainService.calculateEffectiveBattery(100, 21, RobotMovementMode.ENERGY_SAVING);

        assertThat(fastBattery.batteryPercent()).isLessThan(normalBattery.batteryPercent());
        assertThat(energySavingBattery.batteryPercent()).isGreaterThan(normalBattery.batteryPercent());
    }

    @Test
    void safeHeavyLoadAndObstacleAvoidanceUseNormalBatteryDrainForNow() {
        assertEffectiveBattery(100, 20, RobotMovementMode.SAFE, 96, 4, 5);
        assertEffectiveBattery(100, 20, RobotMovementMode.HEAVY_LOAD, 96, 4, 5);
        assertEffectiveBattery(100, 20, RobotMovementMode.OBSTACLE_AVOIDANCE, 96, 4, 5);
    }

    @Test
    void effectiveBatteryNeverFallsBelowZeroOrExceedsOneHundred() {
        assertEffectiveBattery(5, 1000, RobotMovementMode.FAST, 0, 333, 3);
        assertEffectiveBattery(120, 0, RobotMovementMode.NORMAL, 100, 0, 5);
        assertEffectiveBattery(-5, 0, RobotMovementMode.ENERGY_SAVING, 0, 0, 7);
    }

    @Test
    void batteryWarningThresholdsUseEffectiveBatteryPercent() {
        assertBatteryWarning(25, "NORMAL", false, false, false, false, "Battery level normal.");
        assertBatteryWarning(19, "LOW", true, false, true, false, "Energy saving mode active.");
        assertBatteryWarning(6, "LOW", true, false, true, false, "Energy saving mode active.");
        assertBatteryWarning(5, "CRITICAL", true, true, true, true, "Charging required after current mission.");
        assertBatteryWarning(0, "CRITICAL", true, true, true, true, "Charging required after current mission.");
    }

    @Test
    void chargingBatteryRecoversFivePercentEveryTenSecondsAndStopsAtOneHundred() {
        assertChargingBattery(5, 0, 5, 0, "5% battery (charging)");
        assertChargingBattery(5, 9, 5, 0, "5% battery (charging)");
        assertChargingBattery(5, 10, 10, 5, "10% battery (charging +5%)");
        assertChargingBattery(5, 20, 15, 10, "15% battery (charging +10%)");
        assertChargingBattery(95, 20, 100, 5, "100% battery (charging +5%)");
    }

    private void assertEffectiveBattery(Integer storedBatteryPercent,
                                        int traveledWaypointCount,
                                        RobotMovementMode movementMode,
                                        int expectedBatteryPercent,
                                        int expectedDrainPercent,
                                        int expectedWaypointsPerBatteryPercent) {
        BatteryDrainResult battery = robotBatteryDrainService.calculateEffectiveBattery(
                storedBatteryPercent,
                traveledWaypointCount,
                movementMode
        );

        assertThat(battery.batteryLevel()).isEqualTo(expectedBatteryPercent);
        assertThat(battery.batteryPercent()).isEqualTo(expectedBatteryPercent);
        assertThat(battery.batteryDrainPercent()).isEqualTo(expectedDrainPercent);
        assertThat(battery.movementMode()).isEqualTo(movementMode.name());
        assertThat(battery.waypointsPerBatteryPercent()).isEqualTo(expectedWaypointsPerBatteryPercent);
    }

    private void assertBatteryWarning(Integer storedBatteryPercent,
                                      String expectedWarningLevel,
                                      boolean expectedLowBattery,
                                      boolean expectedCriticalBattery,
                                      boolean expectedEnergySavingMode,
                                      boolean expectedChargingRequired,
                                      String expectedBatteryMessage) {
        BatteryDrainResult battery = robotBatteryDrainService.calculateEffectiveBattery(storedBatteryPercent, 0);

        assertThat(battery.batteryWarningLevel()).isEqualTo(expectedWarningLevel);
        assertThat(battery.lowBattery()).isEqualTo(expectedLowBattery);
        assertThat(battery.criticalBattery()).isEqualTo(expectedCriticalBattery);
        assertThat(battery.energySavingMode()).isEqualTo(expectedEnergySavingMode);
        assertThat(battery.chargingRequired()).isEqualTo(expectedChargingRequired);
        assertThat(battery.batteryMessage()).isEqualTo(expectedBatteryMessage);
    }

    private void assertChargingBattery(Integer batteryBeforeCharging,
                                       long chargingElapsedSeconds,
                                       int expectedBatteryPercent,
                                       int expectedRecoveredPercent,
                                       String expectedDisplayText) {
        BatteryDrainResult battery = robotBatteryDrainService.calculateChargingBattery(
                batteryBeforeCharging,
                chargingElapsedSeconds
        );

        assertThat(battery.batteryLevel()).isEqualTo(expectedBatteryPercent);
        assertThat(battery.batteryPercent()).isEqualTo(expectedBatteryPercent);
        assertThat(battery.chargingRecoveredPercent()).isEqualTo(expectedRecoveredPercent);
        assertThat(battery.batteryDisplayText()).isEqualTo(expectedDisplayText);
        assertThat(battery.chargingDisplayText()).isEqualTo(expectedDisplayText);
        assertThat(battery.batteryDrainPercent()).isZero();
        assertThat(battery.batteryMessage()).isEqualTo("Charging at Charging Station.");
    }
}
