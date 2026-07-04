package com.warehouse.interpreter;

import com.warehouse.model.Robot;

public interface Expression {

    ExpressionEvaluation evaluate(Robot robot);

    default boolean interpret(Robot robot) {
        return evaluate(robot).isMatched();
    }

    String getExpressionText();
}
