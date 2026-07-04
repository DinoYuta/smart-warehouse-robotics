package com.warehouse.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.warehouse.model.RuleExecutionHistory;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class RuleExecutionHistoryRepositoryTest {

    @Autowired
    private RuleExecutionHistoryRepository repository;

    @Test
    void retrievesRecentHistoryNewestFirst() {
        repository.save(createHistory("SIM-OLD", LocalDateTime.of(2026, 5, 19, 9, 0)));
        repository.save(createHistory("SIM-NEW", LocalDateTime.of(2026, 5, 19, 10, 0)));

        List<RuleExecutionHistory> recentHistory = repository.findTop10ByOrderByExecutedAtDescIdDesc();

        assertThat(recentHistory)
                .extracting(RuleExecutionHistory::getRobotCode)
                .containsExactly("SIM-NEW", "SIM-OLD");
    }

    private RuleExecutionHistory createHistory(String robotCode, LocalDateTime executedAt) {
        return new RuleExecutionHistory(
                robotCode,
                "Simulation Robot",
                15,
                true,
                45,
                8.2,
                1,
                true,
                "Critical Battery With Obstacle Rule",
                "battery < 20 AND obstacleDetected == TRUE",
                "ChargingStrategy",
                robotCode + " is routed to the nearest charging station.",
                executedAt
        );
    }
}
