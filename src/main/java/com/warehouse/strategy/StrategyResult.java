package com.warehouse.strategy;

public class StrategyResult {

    private final String strategyName;
    private final String actionMessage;

    public StrategyResult(String strategyName, String actionMessage) {
        this.strategyName = strategyName;
        this.actionMessage = actionMessage;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public String getActionMessage() {
        return actionMessage;
    }
}
