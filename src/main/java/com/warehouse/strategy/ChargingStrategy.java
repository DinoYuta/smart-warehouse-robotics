package com.warehouse.strategy;

import com.warehouse.model.Robot;
import org.springframework.stereotype.Component;

@Component
public class ChargingStrategy implements Strategy {

    @Override
    public String getStrategyName() {
        return "ChargingStrategy";
    }

    @Override
    public StrategyResult execute(Robot robot) {
        return new StrategyResult(getStrategyName(), robot.getCode() + " is routed to the nearest charging station.");
    }
}
