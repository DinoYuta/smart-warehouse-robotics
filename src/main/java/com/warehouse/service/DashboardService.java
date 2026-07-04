package com.warehouse.service;

import com.warehouse.dto.DashboardSummaryDto;
import com.warehouse.dto.RuleExecutionDto;
import com.warehouse.interpreter.RuleEngineResult;
import com.warehouse.interpreter.RuleEvaluator;
import com.warehouse.model.MissionStatus;
import com.warehouse.model.Robot;
import com.warehouse.model.Rule;
import com.warehouse.model.RuleExecutionHistory;
import com.warehouse.model.Strategy;
import com.warehouse.repository.MissionRepository;
import com.warehouse.repository.RobotRepository;
import com.warehouse.repository.RuleExecutionHistoryRepository;
import com.warehouse.repository.StrategyRepository;
import com.warehouse.strategy.StrategyContext;
import com.warehouse.strategy.StrategyResult;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final RobotRepository robotRepository;
    private final MissionRepository missionRepository;
    private final StrategyRepository strategyRepository;
    private final RuleService ruleService;
    private final RuleEvaluator ruleEvaluator;
    private final StrategyContext strategyContext;
    private final RuleExecutionHistoryRepository ruleExecutionHistoryRepository;

    public DashboardService(RobotRepository robotRepository,
                            MissionRepository missionRepository,
                            StrategyRepository strategyRepository,
                            RuleService ruleService,
                            RuleEvaluator ruleEvaluator,
                            StrategyContext strategyContext,
                            RuleExecutionHistoryRepository ruleExecutionHistoryRepository) {
        this.robotRepository = robotRepository;
        this.missionRepository = missionRepository;
        this.strategyRepository = strategyRepository;
        this.ruleService = ruleService;
        this.ruleEvaluator = ruleEvaluator;
        this.strategyContext = strategyContext;
        this.ruleExecutionHistoryRepository = ruleExecutionHistoryRepository;
    }

    public DashboardSummaryDto getSummary() {
        return new DashboardSummaryDto(
                robotRepository.count(),
                missionRepository.countByStatusInAndDeletedAtIsNull(List.of(
                        MissionStatus.PENDING,
                        MissionStatus.ASSIGNED,
                        MissionStatus.IN_PROGRESS
                )),
                ruleService.countActiveRules(),
                strategyRepository.countByActiveTrue(),
                "Dynamic Rule Engine"
        );
    }

    public List<Robot> getRobots() {
        return robotRepository.findTop10ByOrderByIdAsc();
    }

    public List<Rule> getActiveRules() {
        return ruleService.findTopActiveRules();
    }

    public List<Strategy> getActiveStrategies() {
        return strategyRepository.findByActiveTrueOrderByNameAsc();
    }

    public List<RuleExecutionDto> getRuleExecutions() {
        return getRobots().stream()
                .map(this::evaluateRobot)
                .toList();
    }

    public List<RuleExecutionHistory> getRecentExecutionHistory() {
        return ruleExecutionHistoryRepository.findTop10ByOrderByExecutedAtDescIdDesc();
    }

    private RuleExecutionDto evaluateRobot(Robot robot) {
        RuleEngineResult engineResult = ruleEvaluator.evaluate(robot);
        if (!engineResult.hasMatch()) {
            return new RuleExecutionDto(
                    robot.getCode(),
                    robot.getName(),
                    "No Rule Matched",
                    "No active expression matched",
                    "NoStrategy",
                    "Robot keeps its current strategy.",
                    false
            );
        }

        StrategyResult strategyResult = strategyContext.executeStrategy(engineResult.getSelectedStrategyName(), robot);
        return new RuleExecutionDto(
                robot.getCode(),
                robot.getName(),
                engineResult.getMatchedRule().getRuleName(),
                engineResult.getMatchedRule().getConditionExpression(),
                strategyResult.getStrategyName(),
                strategyResult.getActionMessage(),
                true
        );
    }
}
