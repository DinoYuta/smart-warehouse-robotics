package com.warehouse.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import com.warehouse.model.Robot;
import java.util.List;
import org.junit.jupiter.api.Test;

class StrategyContextTest {

    private final StrategyContext strategyContext = new StrategyContext(List.of(
            new ChargingStrategy(),
            new FastRouteStrategy(),
            new SafeRouteStrategy(),
            new ObstacleAvoidanceStrategy(),
            new HeavyLoadStrategy(),
            new EnergySavingStrategy()
    ));

    @Test
    void dispatchesChargingStrategy() {
        assertDispatches("ChargingStrategy", "ChargingStrategy",
                "SIM-001 is routed to the nearest charging station.");
    }

    @Test
    void dispatchesFastRouteStrategy() {
        assertDispatches("FastRouteStrategy", "FastRouteStrategy",
                "SIM-001 selects the fastest available route.");
    }

    @Test
    void dispatchesSafeRouteStrategy() {
        assertDispatches("SafeRouteStrategy", "SafeRouteStrategy",
                "SIM-001 uses a safe route with lower collision risk.");
    }

    @Test
    void dispatchesObstacleAvoidanceStrategy() {
        assertDispatches("ObstacleAvoidanceStrategy", "ObstacleAvoidanceStrategy",
                "SIM-001 slows down and recalculates around the obstacle.");
    }

    @Test
    void dispatchesHeavyLoadStrategy() {
        assertDispatches("HeavyLoadStrategy", "HeavyLoadStrategy",
                "SIM-001 reduces speed and requests a heavy-load route.");
    }

    @Test
    void dispatchesEnergySavingStrategy() {
        assertDispatches("EnergySavingStrategy", "EnergySavingStrategy",
                "SIM-001 reduces speed to preserve battery.");
    }

    @Test
    void unknownStrategyReturnsNoStrategyResult() {
        StrategyResult result = strategyContext.executeStrategy("UnknownStrategy", createRobot());

        assertThat(result.getStrategyName()).isEqualTo("NoStrategy");
        assertThat(result.getActionMessage()).isEqualTo("No matching strategy was selected.");
    }

    private void assertDispatches(String requestedStrategyName, String expectedStrategyName,
                                  String expectedActionMessage) {
        StrategyResult result = strategyContext.executeStrategy(requestedStrategyName, createRobot());

        assertThat(result.getStrategyName()).isEqualTo(expectedStrategyName);
        assertThat(result.getActionMessage()).isEqualTo(expectedActionMessage);
    }

    private Robot createRobot() {
        Robot robot = new Robot();
        robot.setCode("SIM-001");
        robot.setName("Simulation Robot");
        robot.setBattery(15);
        robot.setObstacleDetected(true);
        robot.setRobotLoad(90);
        robot.setDistance(20.0);
        robot.setPriority(1);
        return robot;
    }
}
