package com.warehouse.strategy;

import com.warehouse.model.Robot;

public interface Strategy {

    String getStrategyName();

    StrategyResult execute(Robot robot);
}
