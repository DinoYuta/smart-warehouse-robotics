package com.warehouse.dto;

import com.warehouse.interpreter.ExpressionEvaluation;
import com.warehouse.interpreter.RuleEvaluationResult;
import java.util.List;

public class SimulationResultDto {

    private final Integer battery;
    private final Boolean obstacleDetected;
    private final Integer robotLoad;
    private final Double distance;
    private final Integer priority;
    private final boolean matched;
    private final String matchedRuleName;
    private final String matchedConditionExpression;
    private final String selectedStrategy;
    private final String actionMessage;
    private final List<RuleEvaluationResult> ruleResults;
    private final List<ExpressionEvaluation> conditionResults;

    public SimulationResultDto(Integer battery, Boolean obstacleDetected, Integer robotLoad,
                               Double distance, Integer priority,
                               boolean matched, String matchedRuleName, String matchedConditionExpression,
                               String selectedStrategy, String actionMessage,
                               List<RuleEvaluationResult> ruleResults,
                               List<ExpressionEvaluation> conditionResults) {
        this.battery = battery;
        this.obstacleDetected = obstacleDetected;
        this.robotLoad = robotLoad;
        this.distance = distance;
        this.priority = priority;
        this.matched = matched;
        this.matchedRuleName = matchedRuleName;
        this.matchedConditionExpression = matchedConditionExpression;
        this.selectedStrategy = selectedStrategy;
        this.actionMessage = actionMessage;
        this.ruleResults = List.copyOf(ruleResults);
        this.conditionResults = List.copyOf(conditionResults);
    }

    public Integer getBattery() {
        return battery;
    }

    public Boolean getObstacleDetected() {
        return obstacleDetected;
    }

    public Integer getRobotLoad() {
        return robotLoad;
    }

    public Double getDistance() {
        return distance;
    }

    public Integer getPriority() {
        return priority;
    }

    public boolean isMatched() {
        return matched;
    }

    public String getMatchedRuleName() {
        return matchedRuleName;
    }

    public String getMatchedConditionExpression() {
        return matchedConditionExpression;
    }

    public String getSelectedStrategy() {
        return selectedStrategy;
    }

    public String getActionMessage() {
        return actionMessage;
    }

    public List<RuleEvaluationResult> getRuleResults() {
        return ruleResults;
    }

    public List<ExpressionEvaluation> getConditionResults() {
        return conditionResults;
    }
}
