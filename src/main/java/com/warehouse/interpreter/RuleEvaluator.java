package com.warehouse.interpreter;

import com.warehouse.model.Robot;
import com.warehouse.service.RuleService;
import org.springframework.stereotype.Component;

@Component
public class RuleEvaluator {

    private final RuleService ruleService;

    public RuleEvaluator(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    public RuleEngineResult evaluate(Robot robot) {
        return ruleService.evaluateRules(robot);
    }
}
