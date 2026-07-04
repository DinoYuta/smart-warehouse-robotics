package com.warehouse.interpreter;

import java.util.List;
import java.util.Optional;

public class RuleEngineResult {

    private final List<RuleEvaluationResult> ruleResults;
    private final RuleEvaluationResult matchedRule;

    public RuleEngineResult(List<RuleEvaluationResult> ruleResults) {
        this.ruleResults = List.copyOf(ruleResults);
        this.matchedRule = ruleResults.stream()
                .filter(RuleEvaluationResult::isMatched)
                .findFirst()
                .orElse(null);
    }

    public List<RuleEvaluationResult> getRuleResults() {
        return ruleResults;
    }

    public RuleEvaluationResult getMatchedRule() {
        return matchedRule;
    }

    public boolean hasMatch() {
        return matchedRule != null;
    }

    public String getSelectedStrategyName() {
        return Optional.ofNullable(matchedRule)
                .map(RuleEvaluationResult::getTargetStrategyName)
                .orElse(null);
    }
}
