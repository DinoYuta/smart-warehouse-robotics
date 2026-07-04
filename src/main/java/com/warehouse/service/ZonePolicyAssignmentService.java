package com.warehouse.service;

import com.warehouse.dto.ZonePolicyAssignmentDto;
import com.warehouse.model.CargoType;
import com.warehouse.model.Rule;
import com.warehouse.model.ZonePolicyAssignment;
import com.warehouse.repository.RuleRepository;
import com.warehouse.repository.ZonePolicyAssignmentRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ZonePolicyAssignmentService {

    private static final Map<String, String> CARGO_TYPE_BY_ZONE = CargoType.cargoTypeByZone();

    private final ZonePolicyAssignmentRepository assignmentRepository;
    private final RuleRepository ruleRepository;

    public ZonePolicyAssignmentService(ZonePolicyAssignmentRepository assignmentRepository,
                                       RuleRepository ruleRepository) {
        this.assignmentRepository = assignmentRepository;
        this.ruleRepository = ruleRepository;
    }

    public ZonePolicyAssignment assignPolicy(ZonePolicyAssignmentDto assignmentForm) {
        normalize(assignmentForm);
        Rule assignedRule = validateAndFindActiveRule(assignmentForm);

        ZonePolicyAssignment assignment = assignmentRepository.findByZone(assignmentForm.getZone())
                .orElseGet(ZonePolicyAssignment::new);
        assignment.setZone(assignmentForm.getZone());
        assignment.setCargoType(assignmentForm.getCargoType());
        assignment.setRuleId(assignedRule.getId());
        return assignmentRepository.save(assignment);
    }

    @Transactional(readOnly = true)
    public List<Rule> getActiveRules() {
        return ruleRepository.findByActiveStatusTrueOrderByPriorityAscRuleNameAsc();
    }

    @Transactional(readOnly = true)
    public Map<String, String> getCargoTypeByZone() {
        return CARGO_TYPE_BY_ZONE;
    }

    @Transactional(readOnly = true)
    public Map<String, ZonePolicyAssignment> getAssignmentsByZone() {
        return assignmentRepository.findAllByOrderByZoneAsc()
                .stream()
                .collect(Collectors.toMap(
                        ZonePolicyAssignment::getZone,
                        Function.identity(),
                        (existingAssignment, ignoredAssignment) -> existingAssignment,
                        LinkedHashMap::new
                ));
    }

    @Transactional(readOnly = true)
    public Map<String, Rule> getAssignedRulesByZone() {
        Map<String, ZonePolicyAssignment> assignmentsByZone = getAssignmentsByZone();
        List<Long> assignedRuleIds = assignmentsByZone.values()
                .stream()
                .map(ZonePolicyAssignment::getRuleId)
                .distinct()
                .toList();

        Map<Long, Rule> rulesById = ruleRepository.findAllById(assignedRuleIds)
                .stream()
                .collect(Collectors.toMap(Rule::getId, Function.identity()));

        Map<String, Rule> assignedRulesByZone = new LinkedHashMap<>();
        assignmentsByZone.forEach((zone, assignment) -> {
            Rule rule = rulesById.get(assignment.getRuleId());
            if (rule != null) {
                assignedRulesByZone.put(zone, rule);
            }
        });
        return assignedRulesByZone;
    }

    private Rule validateAndFindActiveRule(ZonePolicyAssignmentDto assignmentForm) {
        if (!CARGO_TYPE_BY_ZONE.containsKey(assignmentForm.getZone())) {
            throw new IllegalArgumentException("Zone must be Zone A, Zone B, or Zone C.");
        }

        String expectedCargoType = CARGO_TYPE_BY_ZONE.get(assignmentForm.getZone());
        if (!expectedCargoType.equals(assignmentForm.getCargoType())) {
            throw new IllegalArgumentException(assignmentForm.getZone() + " must use " + expectedCargoType + ".");
        }

        if (assignmentForm.getRuleId() == null) {
            throw new IllegalArgumentException("Assigned rule is required.");
        }

        Rule assignedRule = ruleRepository.findById(assignmentForm.getRuleId())
                .orElseThrow(() -> new IllegalArgumentException("Selected rule does not exist."));
        if (!assignedRule.isActive()) {
            throw new IllegalArgumentException("Selected rule must be active.");
        }

        return assignedRule;
    }

    private void normalize(ZonePolicyAssignmentDto assignmentForm) {
        assignmentForm.setZone(trimToNull(assignmentForm.getZone()));
        assignmentForm.setCargoType(trimToNull(assignmentForm.getCargoType()));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}
