package com.warehouse.interpreter;

import com.warehouse.model.Robot;

public class DistanceExpression implements Expression {

    private final ComparisonOperator operator;
    private final double threshold;

    public DistanceExpression(ComparisonOperator operator, double threshold) {
        this.operator = operator;
        this.threshold = threshold;
    }

    @Override
    public ExpressionEvaluation evaluate(Robot robot) {
        return new ExpressionEvaluation(getExpressionText(), operator.matches(robot.getDistance(), threshold));
    }

    @Override
    public String getExpressionText() {
        return "distance " + operator.getSymbol() + " " + formatThreshold();
    }

    private String formatThreshold() {
        if (threshold == Math.rint(threshold)) {
            return String.valueOf((int) threshold);
        }
        return String.valueOf(threshold);
    }
}
