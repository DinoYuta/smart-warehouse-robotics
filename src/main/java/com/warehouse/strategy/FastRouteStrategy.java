package com.warehouse.strategy;

import com.warehouse.model.Robot;
import org.springframework.stereotype.Component;

@Component
public class FastRouteStrategy implements Strategy {

    @Override
    public String getStrategyName() {
        return "FastRouteStrategy";
    }

    @Override
    public StrategyResult execute(Robot robot) {
        return new StrategyResult(getStrategyName(), robot.getCode() + " selects the fastest available route.");
    }
}
