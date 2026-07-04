package com.warehouse.strategy;

import com.warehouse.model.Robot;
import org.springframework.stereotype.Component;

@Component
public class SafeRouteStrategy implements Strategy {

    @Override
    public String getStrategyName() {
        return "SafeRouteStrategy";
    }

    @Override
    public StrategyResult execute(Robot robot) {
        return new StrategyResult(getStrategyName(), robot.getCode() + " uses a safe route with lower collision risk.");
    }
}
