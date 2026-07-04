package com.warehouse.simulator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.warehouse.dto.SimulationRequestDto;
import com.warehouse.dto.SimulationResultDto;
import com.warehouse.interpreter.ExpressionEvaluation;
import com.warehouse.interpreter.RuleEngineResult;
import com.warehouse.interpreter.RuleEvaluationResult;
import com.warehouse.model.Robot;
import com.warehouse.model.RuleExecutionHistory;
import com.warehouse.repository.RuleExecutionHistoryRepository;
import com.warehouse.interpreter.RuleEvaluator;
import com.warehouse.strategy.StrategyContext;
import com.warehouse.strategy.StrategyResult;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SimulationServiceTest {

    @Test
    void persistsHistoryAfterSimulation() {
        RuleEvaluator ruleEvaluator = mock(RuleEvaluator.class);
        StrategyContext strategyContext = mock(StrategyContext.class);
        RuleExecutionHistoryRepository historyRepository = mock(RuleExecutionHistoryRepository.class);
        SimulationService simulationService = new SimulationService(ruleEvaluator, strategyContext, historyRepository);

        SimulationRequestDto request = new SimulationRequestDto(15, true, 45, 8.2, 1);
        RuleEvaluationResult matchedRule = new RuleEvaluationResult(
                1,
                "Critical Battery With Obstacle Rule",
                "battery < 20 AND obstacleDetected == TRUE",
                true,
                "ChargingStrategy",
                new ExpressionEvaluation("battery < 20 AND obstacleDetected == TRUE", true)
        );
        when(ruleEvaluator.evaluate(org.mockito.ArgumentMatchers.any(Robot.class)))
                .thenReturn(new RuleEngineResult(List.of(matchedRule)));
        when(strategyContext.executeStrategy(org.mockito.ArgumentMatchers.eq("ChargingStrategy"),
                org.mockito.ArgumentMatchers.any(Robot.class)))
                .thenReturn(new StrategyResult("ChargingStrategy", "SIM-001 is routed to the nearest charging station."));

        SimulationResultDto result = simulationService.simulate(request);

        ArgumentCaptor<RuleExecutionHistory> historyCaptor = ArgumentCaptor.forClass(RuleExecutionHistory.class);
        verify(historyRepository).save(historyCaptor.capture());
        RuleExecutionHistory savedHistory = historyCaptor.getValue();

        assertThat(result.isMatched()).isTrue();
        assertThat(savedHistory.getRobotCode()).isEqualTo("SIM-001");
        assertThat(savedHistory.getRobotName()).isEqualTo("Simulation Robot");
        assertThat(savedHistory.getBattery()).isEqualTo(15);
        assertThat(savedHistory.getObstacleDetected()).isTrue();
        assertThat(savedHistory.getRobotLoad()).isEqualTo(45);
        assertThat(savedHistory.getDistance()).isEqualTo(8.2);
        assertThat(savedHistory.getPriority()).isEqualTo(1);
        assertThat(savedHistory.isMatched()).isTrue();
        assertThat(savedHistory.getMatchedRuleName()).isEqualTo("Critical Battery With Obstacle Rule");
        assertThat(savedHistory.getMatchedConditionExpression()).isEqualTo("battery < 20 AND obstacleDetected == TRUE");
        assertThat(savedHistory.getSelectedStrategy()).isEqualTo("ChargingStrategy");
        assertThat(savedHistory.getActionMessage()).isEqualTo("SIM-001 is routed to the nearest charging station.");
        assertThat(savedHistory.getExecutedAt()).isNotNull();
    }

    @Test
    void returnsRuleAndConditionTraceDetailsWithPrioritySelectedRule() {
        RuleEvaluator ruleEvaluator = mock(RuleEvaluator.class);
        StrategyContext strategyContext = mock(StrategyContext.class);
        RuleExecutionHistoryRepository historyRepository = mock(RuleExecutionHistoryRepository.class);
        SimulationService simulationService = new SimulationService(ruleEvaluator, strategyContext, historyRepository);

        SimulationRequestDto request = new SimulationRequestDto(15, true, 90, 20.0, 1);
        RuleEvaluationResult selectedRule = new RuleEvaluationResult(
                1,
                "Critical Battery With Obstacle Rule",
                "battery < 20 AND obstacleDetected == TRUE",
                true,
                "ChargingStrategy",
                new ExpressionEvaluation(
                        "battery < 20 AND obstacleDetected == TRUE",
                        true,
                        List.of(
                                new ExpressionEvaluation("battery < 20", true),
                                new ExpressionEvaluation("obstacleDetected == TRUE", true)
                        )
                )
        );
        RuleEvaluationResult secondMatchedRule = new RuleEvaluationResult(
                2,
                "Obstacle Detection Rule",
                "obstacleDetected == TRUE",
                true,
                "ObstacleAvoidanceStrategy",
                new ExpressionEvaluation("obstacleDetected == TRUE", true)
        );
        RuleEvaluationResult lowerPriorityMatchedRule = new RuleEvaluationResult(
                3,
                "Heavy Load Rule",
                "robotLoad > 80",
                true,
                "HeavyLoadStrategy",
                new ExpressionEvaluation("robotLoad > 80", true)
        );
        when(ruleEvaluator.evaluate(org.mockito.ArgumentMatchers.any(Robot.class)))
                .thenReturn(new RuleEngineResult(List.of(selectedRule, secondMatchedRule, lowerPriorityMatchedRule)));
        when(strategyContext.executeStrategy(org.mockito.ArgumentMatchers.eq("ChargingStrategy"),
                org.mockito.ArgumentMatchers.any(Robot.class)))
                .thenReturn(new StrategyResult("ChargingStrategy", "SIM-001 is routed to the nearest charging station."));

        SimulationResultDto result = simulationService.simulate(request);

        assertThat(result.getBattery()).isEqualTo(15);
        assertThat(result.getObstacleDetected()).isTrue();
        assertThat(result.getRobotLoad()).isEqualTo(90);
        assertThat(result.getDistance()).isEqualTo(20.0);
        assertThat(result.getPriority()).isEqualTo(1);
        assertThat(result.isMatched()).isTrue();
        assertThat(result.getRuleResults())
                .extracting(RuleEvaluationResult::getRuleName)
                .containsExactly(
                        "Critical Battery With Obstacle Rule",
                        "Obstacle Detection Rule",
                        "Heavy Load Rule"
                );
        assertThat(result.getConditionResults())
                .extracting(ExpressionEvaluation::getExpressionText)
                .containsExactly(
                        "battery < 20",
                        "obstacleDetected == TRUE",
                        "robotLoad > 80"
                );
        assertThat(result.getMatchedRuleName()).isEqualTo("Critical Battery With Obstacle Rule");
        assertThat(result.getMatchedConditionExpression()).isEqualTo("battery < 20 AND obstacleDetected == TRUE");
        assertThat(result.getSelectedStrategy()).isEqualTo("ChargingStrategy");
        assertThat(result.getActionMessage()).isEqualTo("SIM-001 is routed to the nearest charging station.");
        verify(strategyContext).executeStrategy(org.mockito.ArgumentMatchers.eq("ChargingStrategy"),
                org.mockito.ArgumentMatchers.any(Robot.class));
    }
}
