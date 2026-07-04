package com.warehouse.interpreter;

import com.warehouse.model.Robot;

public class ObstacleExpression implements Expression {

    private final boolean expectedValue;

    public ObstacleExpression(boolean expectedValue) {
        this.expectedValue = expectedValue;
    }

    @Override
    public ExpressionEvaluation evaluate(Robot robot) {
        boolean actualValue = Boolean.TRUE.equals(robot.getObstacleDetected());
        return new ExpressionEvaluation(getExpressionText(), actualValue == expectedValue);
    }

    @Override
    public String getExpressionText() {
        return "obstacleDetected == " + String.valueOf(expectedValue).toUpperCase();
    }
}
