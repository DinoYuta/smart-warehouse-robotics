package com.warehouse.interpreter;

public class RuleEvaluationResult {

    private final Integer priority;
    private final String ruleName;
    private final String conditionExpression;
    private final boolean matched;
    private final String targetStrategyName;
    private final ExpressionEvaluation expressionEvaluation;

    public RuleEvaluationResult(String ruleName,
                                String conditionExpression,
                                boolean matched,
                                String targetStrategyName) {
        this(
                null,
                ruleName,
                conditionExpression,
                matched,
                targetStrategyName,
                new ExpressionEvaluation(conditionExpression, matched)
        );
    }

    public RuleEvaluationResult(Integer priority, String ruleName, String conditionExpression, boolean matched,
                                String targetStrategyName, ExpressionEvaluation expressionEvaluation) {
        this.priority = priority;
        this.ruleName = ruleName;
        this.conditionExpression = conditionExpression;
        this.matched = matched;
        this.targetStrategyName = targetStrategyName;
        this.expressionEvaluation = expressionEvaluation;
    }

    public Integer getPriority() {
        return priority;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getConditionExpression() {
        return conditionExpression;
    }

    public boolean isMatched() {
        return matched;
    }

    public String getTargetStrategyName() {
        return targetStrategyName;
    }

    public String getStrategyName() {
        return targetStrategyName;
    }

    public ExpressionEvaluation getExpressionEvaluation() {
        return expressionEvaluation;
    }
}
