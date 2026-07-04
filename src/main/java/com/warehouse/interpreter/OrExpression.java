package com.warehouse.interpreter;

import com.warehouse.model.Robot;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OrExpression implements Expression {

    private final List<Expression> expressions;

    public OrExpression(Expression... expressions) {
        this(Arrays.asList(expressions));
    }

    public OrExpression(List<Expression> expressions) {
        this.expressions = List.copyOf(expressions);
    }

    @Override
    public ExpressionEvaluation evaluate(Robot robot) {
        List<ExpressionEvaluation> childEvaluations = expressions.stream()
                .map(expression -> expression.evaluate(robot))
                .toList();
        boolean matched = childEvaluations.stream().anyMatch(ExpressionEvaluation::isMatched);
        return new ExpressionEvaluation(getExpressionText(), matched, childEvaluations);
    }

    @Override
    public String getExpressionText() {
        return expressions.stream()
                .map(Expression::getExpressionText)
                .collect(Collectors.joining(" OR "));
    }
}
