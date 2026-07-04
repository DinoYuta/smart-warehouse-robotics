package com.warehouse.strategy;

import com.warehouse.model.Robot;
import org.springframework.stereotype.Component;

@Component
public class HeavyLoadStrategy implements Strategy {

    @Override
    public String getStrategyName() {
        return "HeavyLoadStrategy";
    }

    @Override
    public StrategyResult execute(Robot robot) {
        return new StrategyResult(
                getStrategyName(),
                robot.getCode() + " reduces speed and requests a heavy-load route."
        );
    }
}
