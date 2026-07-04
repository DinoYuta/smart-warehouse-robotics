package com.warehouse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.warehouse.interpreter.RuleEngineResult;
import com.warehouse.interpreter.RuleParser;
import com.warehouse.model.CargoType;
import com.warehouse.model.Robot;
import com.warehouse.model.Rule;
import com.warehouse.repository.RuleRepository;
import com.warehouse.repository.StrategyRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class RulePrioritySelectionTest {

    @Test
    void selectsFirstMatchedRuleInPriorityOrder() {
        RuleRepository ruleRepository = mock(RuleRepository.class);
        StrategyRepository strategyRepository = mock(StrategyRepository.class);
        RuleService ruleService = new RuleService(ruleRepository, strategyRepository, new RuleParser());
        Robot robot = createRobot(5, true, 90, 20.0, 1);

        when(ruleRepository.findByActiveStatusTrueOrderByPriorityAscRuleNameAsc())
                .thenReturn(List.of(
                        createRule("Critical Battery With Obstacle Rule",
                                "battery < 10 AND obstacleDetected == TRUE", "ChargingStrategy", 1),
                        createRule("Low Battery Rule", "battery < 20", "EnergySavingStrategy", 2),
                        createRule("Obstacle Detection Rule", "obstacleDetected == TRUE",
                                "ObstacleAvoidanceStrategy", 3),
                        createRule("Heavy Load Rule", "robotLoad > 80", "HeavyLoadStrategy", 4),
                        createRule("Long Distance Rule", "distance > 15", "SafeRouteStrategy", 5),
                        createRule("Urgent Priority Rule", "priority == 1", "FastRouteStrategy", 6)
                ));

        RuleEngineResult result = ruleService.evaluateRules(robot);

        assertThat(result.hasMatch()).isTrue();
        assertThat(result.getMatchedRule().getPriority()).isEqualTo(1);
        assertThat(result.getMatchedRule().getRuleName()).isEqualTo("Critical Battery With Obstacle Rule");
        assertThat(result.getSelectedStrategyName()).isEqualTo("ChargingStrategy");
        assertThat(result.getRuleResults())
                .extracting(ruleResult -> ruleResult.isMatched())
                .containsExactly(true, true, true, true, true, true);
    }

    @Test
    void lowBatterySelectsEnergySavingWhenRobotIsNotCritical() {
        RuleRepository ruleRepository = mock(RuleRepository.class);
        StrategyRepository strategyRepository = mock(StrategyRepository.class);
        RuleService ruleService = new RuleService(ruleRepository, strategyRepository, new RuleParser());
        Robot robot = createRobot(15, false, 30, 8.0, 3);

        when(ruleRepository.findByActiveStatusTrueOrderByPriorityAscRuleNameAsc())
                .thenReturn(List.of(
                        createRule("Critical Battery With Obstacle Rule",
                                "battery < 10 AND obstacleDetected == TRUE", "ChargingStrategy", 1),
                        createRule("Low Battery Rule", "battery < 20", "EnergySavingStrategy", 2),
                        createRule("Long Distance Rule", "distance > 15", "SafeRouteStrategy", 3)
                ));

        RuleEngineResult result = ruleService.evaluateRules(robot);

        assertThat(result.hasMatch()).isTrue();
        assertThat(result.getMatchedRule().getRuleName()).isEqualTo("Low Battery Rule");
        assertThat(result.getSelectedStrategyName()).isEqualTo("EnergySavingStrategy");
    }

    @Test
    void priorityOneSelectsFastRouteWhenNoHigherRuleMatches() {
        RuleRepository ruleRepository = mock(RuleRepository.class);
        StrategyRepository strategyRepository = mock(StrategyRepository.class);
        RuleService ruleService = new RuleService(ruleRepository, strategyRepository, new RuleParser());
        Robot robot = createRobot(80, false, CargoType.estimatedLoadPercentFor("Small Cargo"), 8.0, 1);

        when(ruleRepository.findByActiveStatusTrueOrderByPriorityAscRuleNameAsc())
                .thenReturn(List.of(
                        createRule("Heavy Load Rule", "robotLoad >= 80", "HeavyLoadStrategy", 1),
                        createRule("Low Battery Rule", "battery < 20", "EnergySavingStrategy", 2),
                        createRule("Urgent Priority Rule", "priority == 1", "FastRouteStrategy", 3),
                        createRule("Long Distance Rule", "distance > 15", "SafeRouteStrategy", 4)
                ));

        RuleEngineResult result = ruleService.evaluateRules(robot);

        assertThat(result.hasMatch()).isTrue();
        assertThat(result.getMatchedRule().getRuleName()).isEqualTo("Urgent Priority Rule");
        assertThat(result.getSelectedStrategyName()).isEqualTo("FastRouteStrategy");
    }

    @Test
    void heavyLoadRuleMatchesLargeCargoAtEightyPercentThreshold() {
        RuleRepository ruleRepository = mock(RuleRepository.class);
        StrategyRepository strategyRepository = mock(StrategyRepository.class);
        RuleService ruleService = new RuleService(ruleRepository, strategyRepository, new RuleParser());
        Robot robot = createRobot(76, false, CargoType.estimatedLoadPercentFor("Large Cargo"), 14.0, 3);

        when(ruleRepository.findByActiveStatusTrueOrderByPriorityAscRuleNameAsc())
                .thenReturn(List.of(
                        createRule("Heavy Load Rule", "robotLoad >= 80", "HeavyLoadStrategy", 1),
                        createRule("Long Distance Rule", "distance > 15", "SafeRouteStrategy", 2)
                ));

        RuleEngineResult result = ruleService.evaluateRules(robot);

        assertThat(result.hasMatch()).isTrue();
        assertThat(result.getMatchedRule().getRuleName()).isEqualTo("Heavy Load Rule");
        assertThat(result.getSelectedStrategyName()).isEqualTo("HeavyLoadStrategy");
    }

    @Test
    void heavyLoadRuleCanMatchMediumCargoWhenAdminLowersThresholdToSixtyPercent() {
        RuleRepository ruleRepository = mock(RuleRepository.class);
        StrategyRepository strategyRepository = mock(StrategyRepository.class);
        RuleService ruleService = new RuleService(ruleRepository, strategyRepository, new RuleParser());
        Robot robot = createRobot(76, false, CargoType.estimatedLoadPercentFor("Medium Cargo"), 14.0, 3);

        when(ruleRepository.findByActiveStatusTrueOrderByPriorityAscRuleNameAsc())
                .thenReturn(List.of(
                        createRule("Heavy Load Rule", "robotLoad >= 60", "HeavyLoadStrategy", 1),
                        createRule("Long Distance Rule", "distance > 15", "SafeRouteStrategy", 2)
                ));

        RuleEngineResult result = ruleService.evaluateRules(robot);

        assertThat(result.hasMatch()).isTrue();
        assertThat(result.getMatchedRule().getRuleName()).isEqualTo("Heavy Load Rule");
        assertThat(result.getSelectedStrategyName()).isEqualTo("HeavyLoadStrategy");
    }

    private Rule createRule(String ruleName, String conditionExpression, String strategyName, Integer priority) {
        return new Rule(ruleName, conditionExpression, strategyName, true, priority);
    }

    private Robot createRobot(Integer battery, Boolean obstacleDetected, Integer robotLoad,
                              Double distance, Integer priority) {
        Robot robot = new Robot();
        robot.setBattery(battery);
        robot.setObstacleDetected(obstacleDetected);
        robot.setRobotLoad(robotLoad);
        robot.setDistance(distance);
        robot.setPriority(priority);
        return robot;
    }
}
