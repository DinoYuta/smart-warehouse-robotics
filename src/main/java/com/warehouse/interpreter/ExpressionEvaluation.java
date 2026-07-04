package com.warehouse.interpreter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExpressionEvaluation {

    private final String expressionText;
    private final boolean matched;
    private final List<ExpressionEvaluation> children;

    public ExpressionEvaluation(String expressionText, boolean matched) {
        this(expressionText, matched, Collections.emptyList());
    }

    public ExpressionEvaluation(String expressionText, boolean matched, List<ExpressionEvaluation> children) {
        this.expressionText = expressionText;
        this.matched = matched;
        this.children = List.copyOf(children);
    }

    public String getExpressionText() {
        return expressionText;
    }

    public boolean isMatched() {
        return matched;
    }

    public List<ExpressionEvaluation> getChildren() {
        return children;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public List<ExpressionEvaluation> getLeafEvaluations() {
        List<ExpressionEvaluation> leaves = new ArrayList<>();
        collectLeaves(this, leaves);
        return leaves;
    }

    private void collectLeaves(ExpressionEvaluation evaluation, List<ExpressionEvaluation> leaves) {
        if (evaluation.isLeaf()) {
            leaves.add(evaluation);
            return;
        }

        for (ExpressionEvaluation child : evaluation.getChildren()) {
            collectLeaves(child, leaves);
        }
    }
}
