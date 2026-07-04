package com.warehouse.service;

import com.warehouse.dto.RuleFormDto;
import com.warehouse.interpreter.Expression;
import com.warehouse.interpreter.ExpressionEvaluation;
import com.warehouse.interpreter.RuleEngineResult;
import com.warehouse.interpreter.RuleEvaluationResult;
import com.warehouse.interpreter.RuleParser;
import com.warehouse.model.Robot;
import com.warehouse.model.Rule;
import com.warehouse.model.Strategy;
import com.warehouse.repository.RuleRepository;
import com.warehouse.repository.StrategyRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RuleService {

    private final RuleRepository ruleRepository;
    private final StrategyRepository strategyRepository;
    private final RuleParser ruleParser;

    public RuleService(RuleRepository ruleRepository, StrategyRepository strategyRepository, RuleParser ruleParser) {
        this.ruleRepository = ruleRepository;
        this.strategyRepository = strategyRepository;
        this.ruleParser = ruleParser;
    }

    @Transactional(readOnly = true)
    public List<Rule> findAllRules() {
        return ruleRepository.findAllByOrderByPriorityAscRuleNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Rule> loadActiveRules() {
        return ruleRepository.findByActiveStatusTrueOrderByPriorityAscRuleNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Rule> findTopActiveRules() {
        return ruleRepository.findTop10ByActiveStatusTrueOrderByPriorityAscRuleNameAsc();
    }

    @Transactional(readOnly = true)
    public List<String> findStrategyNames() {
        return strategyRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(Strategy::getClassName)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countActiveRules() {
        return ruleRepository.countByActiveStatusTrue();
    }

    @Transactional(readOnly = true)
    public RuleFormDto createEmptyForm() {
        RuleFormDto form = new RuleFormDto();
        form.setRuleName("");
        form.setConditionExpression("battery < 20");
        form.setStrategyName("EnergySavingStrategy");
        return form;
    }

    @Transactional(readOnly = true)
    public RuleFormDto findFormById(Long id) {
        Rule rule = findRuleById(id);
        RuleFormDto form = new RuleFormDto();
        form.setId(rule.getId());
        form.setRuleName(rule.getRuleName());
        form.setConditionExpression(rule.getConditionExpression());
        form.setStrategyName(rule.getStrategyName());
        form.setActiveStatus(rule.getActiveStatus());
        form.setPriority(rule.getPriority());
        return form;
    }

    @Transactional
    public Rule saveRule(RuleFormDto form) {
        validateForm(form);

        Rule rule = form.getId() == null ? new Rule() : findRuleById(form.getId());
        rule.setRuleName(form.getRuleName().trim());
        rule.setConditionExpression(form.getConditionExpression().trim());
        rule.setStrategyName(form.getStrategyName().trim());
        rule.setActiveStatus(Boolean.TRUE.equals(form.getActiveStatus()));
        rule.setPriority(form.getPriority());
        rule.setLegacyTargetStrategy(findStrategyByClassName(form.getStrategyName().trim()).orElse(null));
        return ruleRepository.save(rule);
    }

    @Transactional
    public void toggleRule(Long id) {
        Rule rule = findRuleById(id);
        rule.setActiveStatus(!Boolean.TRUE.equals(rule.getActiveStatus()));
        ruleRepository.save(rule);
    }

    @Transactional
    public void deleteRule(Long id) {
        ruleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public RuleEngineResult evaluateRules(Robot robot) {
        List<RuleEvaluationResult> ruleResults = loadActiveRules().stream()
                .map(rule -> evaluateRule(rule, robot))
                .toList();
        return new RuleEngineResult(ruleResults);
    }

    private RuleEvaluationResult evaluateRule(Rule rule, Robot robot) {
        Expression expression = ruleParser.parse(rule.getConditionExpression());
        ExpressionEvaluation expressionEvaluation = expression.evaluate(robot);

        return new RuleEvaluationResult(
                rule.getPriority(),
                rule.getRuleName(),
                expression.getExpressionText(),
                expressionEvaluation.isMatched(),
                rule.getStrategyName(),
                expressionEvaluation
        );
    }

    private Rule findRuleById(Long id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + id));
    }

    private void validateForm(RuleFormDto form) {
        if (isBlank(form.getRuleName())) {
            throw new IllegalArgumentException("Rule name is required.");
        }
        if (isBlank(form.getConditionExpression())) {
            throw new IllegalArgumentException("Condition expression is required.");
        }
        if (isBlank(form.getStrategyName())) {
            throw new IllegalArgumentException("Strategy is required.");
        }
        if (form.getPriority() == null || form.getPriority() < 1) {
            throw new IllegalArgumentException("Priority must be 1 or greater.");
        }
        if (findStrategyByClassName(form.getStrategyName().trim()).isEmpty()) {
            throw new IllegalArgumentException("Selected strategy is not active.");
        }
        ruleParser.parse(form.getConditionExpression());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Optional<Strategy> findStrategyByClassName(String strategyName) {
        return strategyRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .filter(strategy -> strategy.getClassName().equals(strategyName))
                .findFirst();
    }
}
