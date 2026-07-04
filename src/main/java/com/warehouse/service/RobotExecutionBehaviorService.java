package com.warehouse.service;

import com.warehouse.dto.MissionExecutionProgressDto;
import com.warehouse.model.Mission;
import com.warehouse.model.MissionExecutionStep;
import com.warehouse.model.Robot;
import com.warehouse.model.RobotMovementMode;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class RobotExecutionBehaviorService {

    public static final String FAST_ROUTE_STRATEGY = "FastRouteStrategy";
    public static final String ENERGY_SAVING_STRATEGY = "EnergySavingStrategy";
    public static final String OBSTACLE_AVOIDANCE_STRATEGY = "ObstacleAvoidanceStrategy";
    public static final String HEAVY_LOAD_STRATEGY = "HeavyLoadStrategy";
    public static final String CHARGING_STRATEGY = "ChargingStrategy";
    public static final String SAFE_ROUTE_STRATEGY = "SafeRouteStrategy";

    private static final String NORMAL_STRATEGY = "NormalStrategy";
    private static final int LOW_BATTERY_THRESHOLD = 20;

    private final RobotBatteryDrainService robotBatteryDrainService;

    public RobotExecutionBehaviorService(RobotBatteryDrainService robotBatteryDrainService) {
        this.robotBatteryDrainService = robotBatteryDrainService;
    }

    public MovementPlan movementPlanFor(Mission mission, Robot robot) {
        String primaryStrategyName = resolvePrimaryStrategyName(mission, robot);
        RobotMovementMode primaryMovementMode = movementModeForStrategy(primaryStrategyName, mission, robot);
        RobotMovementMode returnMovementMode = primaryMovementMode;

        if (isLargeCargo(mission) && !isStrategy(primaryStrategyName, HEAVY_LOAD_STRATEGY)) {
            returnMovementMode = RobotMovementMode.HEAVY_LOAD;
        }

        return new MovementPlan(primaryMovementMode, returnMovementMode);
    }

    public StrategyBehavior behaviorFor(Mission mission,
                                        Robot robot,
                                        MissionExecutionProgressDto progress) {
        String primaryStrategyName = resolvePrimaryStrategyName(mission, robot);
        RobotMovementMode movementMode = movementModeForStrategy(primaryStrategyName, mission, robot);
        String activeStrategyName = primaryStrategyName;
        String strategyMessage = messageForStrategy(primaryStrategyName);

        if (isLargeCargoReturnPhase(mission, progress) && !isStrategy(primaryStrategyName, HEAVY_LOAD_STRATEGY)) {
            activeStrategyName = HEAVY_LOAD_STRATEGY;
            movementMode = RobotMovementMode.HEAVY_LOAD;
            strategyMessage = "Large cargo picked up. Returning with heavy load behavior.";
        }

        if (progress != null && progress.waiting()) {
            activeStrategyName = OBSTACLE_AVOIDANCE_STRATEGY;
            movementMode = RobotMovementMode.OBSTACLE_AVOIDANCE;
            strategyMessage = "Waiting for path to clear. Will resume "
                    + resumeStrategyName(primaryStrategyName, mission, progress)
                    + ".";
        }

        if (isStrategy(primaryStrategyName, CHARGING_STRATEGY)
                && (progress == null || !progress.waiting())) {
            activeStrategyName = CHARGING_STRATEGY;
            strategyMessage = "Charging required after current mission.";
        }

        return new StrategyBehavior(
                primaryStrategyName,
                activeStrategyName,
                strategyMessage,
                movementMode,
                batteryDrainModeFor(movementMode),
                isStrategy(primaryStrategyName, CHARGING_STRATEGY)
        );
    }

    public StrategyBehavior chargingBehavior() {
        return new StrategyBehavior(
                CHARGING_STRATEGY,
                CHARGING_STRATEGY,
                RobotChargingService.CHARGING_MESSAGE,
                RobotMovementMode.CHARGING,
                RobotMovementMode.CHARGING,
                true
        );
    }

    public boolean isChargingStrategySelected(Mission mission) {
        return isStrategy(resolvePrimaryStrategyName(mission, null), CHARGING_STRATEGY);
    }

    public boolean isChargingStrategyName(String strategyName) {
        return isStrategy(strategyName, CHARGING_STRATEGY);
    }

    private String resolvePrimaryStrategyName(Mission mission, Robot robot) {
        if (mission != null) {
            return !isBlank(mission.getSelectedStrategyName())
                    ? mission.getSelectedStrategyName().trim()
                    : NORMAL_STRATEGY;
        }
        return NORMAL_STRATEGY;
    }

    private RobotMovementMode movementModeForStrategy(String strategyName, Mission mission, Robot robot) {
        if (isStrategy(strategyName, FAST_ROUTE_STRATEGY)) {
            return RobotMovementMode.FAST;
        }
        if (isStrategy(strategyName, ENERGY_SAVING_STRATEGY)) {
            return RobotMovementMode.ENERGY_SAVING;
        }
        if (isStrategy(strategyName, HEAVY_LOAD_STRATEGY)) {
            return RobotMovementMode.HEAVY_LOAD;
        }
        if (isStrategy(strategyName, OBSTACLE_AVOIDANCE_STRATEGY)) {
            return RobotMovementMode.OBSTACLE_AVOIDANCE;
        }
        if (isStrategy(strategyName, SAFE_ROUTE_STRATEGY)) {
            return RobotMovementMode.SAFE;
        }
        if (isStrategy(strategyName, CHARGING_STRATEGY)) {
            return lowBatteryAtMissionStart(mission, robot)
                    ? RobotMovementMode.ENERGY_SAVING
                    : RobotMovementMode.NORMAL;
        }
        return robotBatteryDrainService.resolveMovementMode(mission, robot);
    }

    private RobotMovementMode batteryDrainModeFor(RobotMovementMode movementMode) {
        if (movementMode == RobotMovementMode.HEAVY_LOAD
                || movementMode == RobotMovementMode.OBSTACLE_AVOIDANCE
                || movementMode == RobotMovementMode.SAFE
                || movementMode == RobotMovementMode.CHARGING) {
            return movementMode;
        }
        return movementMode != null ? movementMode : RobotMovementMode.NORMAL;
    }

    private String resumeStrategyName(String primaryStrategyName,
                                      Mission mission,
                                      MissionExecutionProgressDto progress) {
        if (isLargeCargoReturnPhase(mission, progress) && !isStrategy(primaryStrategyName, HEAVY_LOAD_STRATEGY)) {
            return HEAVY_LOAD_STRATEGY;
        }
        return primaryStrategyName;
    }

    private String messageForStrategy(String strategyName) {
        if (isStrategy(strategyName, FAST_ROUTE_STRATEGY)) {
            return "Fast route mode active.";
        }
        if (isStrategy(strategyName, ENERGY_SAVING_STRATEGY)) {
            return "Energy Saving mode active.";
        }
        if (isStrategy(strategyName, OBSTACLE_AVOIDANCE_STRATEGY)) {
            return "Obstacle avoidance strategy active. Waiting safely if the path is occupied.";
        }
        if (isStrategy(strategyName, HEAVY_LOAD_STRATEGY)) {
            return "Heavy load behavior active.";
        }
        if (isStrategy(strategyName, CHARGING_STRATEGY)) {
            return "Charging required after current mission.";
        }
        if (isStrategy(strategyName, SAFE_ROUTE_STRATEGY)) {
            return "Safe Route mode active.";
        }
        return "Normal movement mode active.";
    }

    private boolean isLargeCargoReturnPhase(Mission mission, MissionExecutionProgressDto progress) {
        return isLargeCargo(mission)
                && progress != null
                && (progress.executionStep() == MissionExecutionStep.RETURNING_TO_BASE
                || progress.executionStep() == MissionExecutionStep.RETURNED_TO_BASE);
    }

    private boolean isLargeCargo(Mission mission) {
        return mission != null && "large cargo".equals(normalize(mission.getCargoType()));
    }

    private boolean lowBatteryAtMissionStart(Mission mission, Robot robot) {
        Integer battery = mission != null && mission.getBatteryAtExecutionStart() != null
                ? mission.getBatteryAtExecutionStart()
                : (robot != null ? robot.getBattery() : null);
        return battery != null && robotBatteryDrainService.clampBatteryPercent(battery) < LOW_BATTERY_THRESHOLD;
    }

    private boolean isStrategy(String strategyName, String expectedStrategyName) {
        return normalize(strategyName).equals(normalize(expectedStrategyName));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.US);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record MovementPlan(RobotMovementMode outboundMovementMode,
                               RobotMovementMode returnMovementMode) {
    }

    public record StrategyBehavior(String primaryStrategyName,
                                   String currentActiveStrategyName,
                                   String strategyMessage,
                                   RobotMovementMode movementMode,
                                   RobotMovementMode batteryDrainMode,
                                   boolean chargingRequiredByStrategy) {
    }
}
