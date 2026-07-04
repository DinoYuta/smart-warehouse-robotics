package com.warehouse.strategy;

import com.warehouse.model.Robot;
import org.springframework.stereotype.Component;

@Component
public class ObstacleAvoidanceStrategy implements Strategy {

    @Override
    public String getStrategyName() {
        return "ObstacleAvoidanceStrategy";
    }

    @Override
    public StrategyResult execute(Robot robot) {
        return new StrategyResult(
                getStrategyName(),
                robot.getCode() + " slows down and recalculates around the obstacle."
        );
    }
}
