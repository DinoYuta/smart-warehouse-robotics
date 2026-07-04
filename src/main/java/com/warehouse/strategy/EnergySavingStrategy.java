package com.warehouse.strategy;

import com.warehouse.model.Robot;
import org.springframework.stereotype.Component;

@Component
public class EnergySavingStrategy implements Strategy {

    @Override
    public String getStrategyName() {
        return "EnergySavingStrategy";
    }

    @Override
    public StrategyResult execute(Robot robot) {
        return new StrategyResult(getStrategyName(), robot.getCode() + " reduces speed to preserve battery.");
    }
}
