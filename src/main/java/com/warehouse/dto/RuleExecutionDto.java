package com.warehouse.dto;

public class RuleExecutionDto {

    private final String robotCode;
    private final String robotName;
    private final String ruleName;
    private final String conditionExpression;
    private final String selectedStrategy;
    private final String actionMessage;
    private final boolean matched;

    public RuleExecutionDto(String robotCode, String robotName, String ruleName, String conditionExpression,
                            String selectedStrategy, String actionMessage, boolean matched) {
        this.robotCode = robotCode;
        this.robotName = robotName;
        this.ruleName = ruleName;
        this.conditionExpression = conditionExpression;
        this.selectedStrategy = selectedStrategy;
        this.actionMessage = actionMessage;
        this.matched = matched;
    }

    public String getRobotCode() {
        return robotCode;
    }

    public String getRobotName() {
        return robotName;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getConditionExpression() {
        return conditionExpression;
    }

    public String getSelectedStrategy() {
        return selectedStrategy;
    }

    public String getActionMessage() {
        return actionMessage;
    }

    public boolean isMatched() {
        return matched;
    }
}
