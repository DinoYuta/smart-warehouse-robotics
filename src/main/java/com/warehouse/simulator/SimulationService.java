package com.warehouse.simulator;

import com.warehouse.dto.SimulationRequestDto;
import com.warehouse.dto.SimulationResultDto;
import com.warehouse.interpreter.ExpressionEvaluation;
import com.warehouse.interpreter.RuleEngineResult;
import com.warehouse.interpreter.RuleEvaluator;
import com.warehouse.model.Robot;
import com.warehouse.model.RuleExecutionHistory;
import com.warehouse.repository.RuleExecutionHistoryRepository;
import com.warehouse.strategy.StrategyContext;
import com.warehouse.strategy.StrategyResult;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SimulationService {

    private final RuleEvaluator ruleEvaluator;
    private final StrategyContext strategyContext;
    private final RuleExecutionHistoryRepository ruleExecutionHistoryRepository;

    public SimulationService(RuleEvaluator ruleEvaluator,
                             StrategyContext strategyContext,
                             RuleExecutionHistoryRepository ruleExecutionHistoryRepository) {
        this.ruleEvaluator = ruleEvaluator;
        this.strategyContext = strategyContext;
        this.ruleExecutionHistoryRepository = ruleExecutionHistoryRepository;
    }

    public SimulationResultDto simulateBatteryDecision(Integer battery) {
        return simulate(new SimulationRequestDto(battery));
    }

    @Transactional
    public SimulationResultDto simulate(SimulationRequestDto simulationRequest) {
        Robot robot = new Robot();
        robot.setCode("SIM-001");
        robot.setName("Simulation Robot");
        robot.setBattery(simulationRequest.getBattery());
        robot.setObstacleDetected(Boolean.TRUE.equals(simulationRequest.getObstacleDetected()));
        robot.setRobotLoad(simulationRequest.getRobotLoad());
        robot.setDistance(simulationRequest.getDistance());
        robot.setPriority(simulationRequest.getPriority());

        RuleEngineResult engineResult = ruleEvaluator.evaluate(robot);
        StrategyResult strategyResult = engineResult.hasMatch()
                ? strategyContext.executeStrategy(engineResult.getSelectedStrategyName(), robot)
                : new StrategyResult(
                        "NoStrategy",
                        "No active rule matched this robot state, so the current behavior is unchanged."
                );

        SimulationResultDto result = new SimulationResultDto(
                simulationRequest.getBattery(),
                Boolean.TRUE.equals(simulationRequest.getObstacleDetected()),
                simulationRequest.getRobotLoad(),
                simulationRequest.getDistance(),
                simulationRequest.getPriority(),
                engineResult.hasMatch(),
                engineResult.hasMatch() ? engineResult.getMatchedRule().getRuleName() : "No Rule Matched",
                engineResult.hasMatch()
                        ? engineResult.getMatchedRule().getConditionExpression()
                        : "No active expression matched",
                strategyResult.getStrategyName(),
                strategyResult.getActionMessage(),
                engineResult.getRuleResults(),
                collectUniqueConditionResults(engineResult)
        );
        saveExecutionHistory(robot, result);
        return result;
    }

    private void saveExecutionHistory(Robot robot, SimulationResultDto result) {
        RuleExecutionHistory history = new RuleExecutionHistory();
        history.setRobotCode(robot.getCode());
        history.setRobotName(robot.getName());
        history.setBattery(result.getBattery());
        history.setObstacleDetected(result.getObstacleDetected());
        history.setRobotLoad(result.getRobotLoad());
        history.setDistance(result.getDistance());
        history.setPriority(result.getPriority());
        history.setMatched(result.isMatched());
        history.setMatchedRuleName(result.getMatchedRuleName());
        history.setMatchedConditionExpression(result.getMatchedConditionExpression());
        history.setSelectedStrategy(result.getSelectedStrategy());
        history.setActionMessage(result.getActionMessage());
        history.setExecutedAt(LocalDateTime.now());
        ruleExecutionHistoryRepository.save(history);
    }

    private List<ExpressionEvaluation> collectUniqueConditionResults(RuleEngineResult engineResult) {
        Map<String, ExpressionEvaluation> conditionResults = new LinkedHashMap<>();
        engineResult.getRuleResults().forEach(ruleResult ->
                ruleResult.getExpressionEvaluation().getLeafEvaluations().forEach(conditionResult ->
                        conditionResults.putIfAbsent(conditionResult.getExpressionText(), conditionResult)
                )
        );
        return conditionResults.values().stream().toList();
    }
}
