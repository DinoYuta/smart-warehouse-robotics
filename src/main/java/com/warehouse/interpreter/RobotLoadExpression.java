package com.warehouse.interpreter;

import com.warehouse.model.Robot;

public class RobotLoadExpression implements Expression {

    private final ComparisonOperator operator;
    private final int threshold;

    public RobotLoadExpression(ComparisonOperator operator, int threshold) {
        this.operator = operator;
        this.threshold = threshold;
    }

    @Override
    public ExpressionEvaluation evaluate(Robot robot) {
        return new ExpressionEvaluation(getExpressionText(), operator.matches(robot.getRobotLoad(), threshold));
    }

    @Override
    public String getExpressionText() {
        return "robotLoad " + operator.getSymbol() + " " + threshold;
    }
}
