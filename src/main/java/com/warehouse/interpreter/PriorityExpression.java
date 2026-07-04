package com.warehouse.interpreter;

import com.warehouse.model.Robot;

public class PriorityExpression implements Expression {

    private final ComparisonOperator operator;
    private final int threshold;

    public PriorityExpression(ComparisonOperator operator, int threshold) {
        this.operator = operator;
        this.threshold = threshold;
    }

    @Override
    public ExpressionEvaluation evaluate(Robot robot) {
        return new ExpressionEvaluation(getExpressionText(), operator.matches(robot.getPriority(), threshold));
    }

    @Override
    public String getExpressionText() {
        return "priority " + operator.getSymbol() + " " + threshold;
    }
}
