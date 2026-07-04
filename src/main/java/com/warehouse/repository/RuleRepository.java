package com.warehouse.repository;

import com.warehouse.model.Rule;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuleRepository extends JpaRepository<Rule, Long> {

    long countByActiveStatusTrue();

    Optional<Rule> findByRuleName(String ruleName);

    List<Rule> findTop10ByActiveStatusTrueOrderByPriorityAscRuleNameAsc();

    List<Rule> findByActiveStatusTrueOrderByPriorityAscRuleNameAsc();

    List<Rule> findAllByOrderByPriorityAscRuleNameAsc();
}
