package com.warehouse.repository;

import com.warehouse.model.RuleExecutionHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuleExecutionHistoryRepository extends JpaRepository<RuleExecutionHistory, Long> {

    List<RuleExecutionHistory> findTop10ByOrderByExecutedAtDescIdDesc();
}
