package com.warehouse.interpreter;

import com.warehouse.model.Robot;

public class BatteryExpression implements Expression {

    private final ComparisonOperator operator;
    private final int threshold;

    public BatteryExpression(int threshold) {
        this(ComparisonOperator.LESS_THAN, threshold);
    }

    public BatteryExpression(ComparisonOperator operator, int threshold) {
        this.operator = operator;
        this.threshold = threshold;
    }

    @Override
    public ExpressionEvaluation evaluate(Robot robot) {
        return new ExpressionEvaluation(getExpressionText(), operator.matches(robot.getBattery(), threshold));
    }

    @Override
    public String getExpressionText() {
        return "battery " + operator.getSymbol() + " " + threshold;
    }
}
