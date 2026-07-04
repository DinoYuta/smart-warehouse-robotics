package com.warehouse.service;

import com.warehouse.interpreter.RuleEngineResult;
import com.warehouse.interpreter.RuleEvaluationResult;
import com.warehouse.interpreter.RuleEvaluator;
import com.warehouse.model.CargoType;
import com.warehouse.model.Mission;
import com.warehouse.model.MissionStatus;
import com.warehouse.model.Robot;
import com.warehouse.model.Rule;
import com.warehouse.model.ZonePolicyAssignment;
import com.warehouse.repository.MissionRepository;
import com.warehouse.repository.RuleRepository;
import com.warehouse.repository.ZonePolicyAssignmentRepository;
import com.warehouse.service.RobotAssignmentService.RobotAssignment;
import com.warehouse.strategy.StrategyContext;
import com.warehouse.strategy.StrategyResult;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MissionProcessingService {

    private static final String NO_RULE_MATCHED = "No Rule Matched";

    private final MissionRepository missionRepository;
    private final RobotAssignmentService robotAssignmentService;
    private final ZonePolicyAssignmentRepository assignmentRepository;
    private final RuleRepository ruleRepository;
    private final RuleEvaluator ruleEvaluator;
    private final StrategyContext strategyContext;

    public MissionProcessingService(MissionRepository missionRepository,
                                    RobotAssignmentService robotAssignmentService,
                                    ZonePolicyAssignmentRepository assignmentRepository,
                                    RuleRepository ruleRepository,
                                    RuleEvaluator ruleEvaluator,
                                    StrategyContext strategyContext) {
        this.missionRepository = missionRepository;
        this.robotAssignmentService = robotAssignmentService;
        this.assignmentRepository = assignmentRepository;
        this.ruleRepository = ruleRepository;
        this.ruleEvaluator = ruleEvaluator;
        this.strategyContext = strategyContext;
    }

    public Mission processPendingMission(Long missionId) {
        Mission mission = missionRepository.findByIdAndDeletedAtIsNull(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));
        if (mission.getStatus() != MissionStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING missions can be processed.");
        }

        RobotAssignment robotAssignment = robotAssignmentService.selectRobotForMission(mission)
                .orElse(null);
        if (robotAssignment == null) {
            return keepPendingWithoutRobot(mission);
        }

        Robot selectedRobot = robotAssignment.robot();
        Robot evaluationRobot = buildEvaluationRobot(mission, selectedRobot);

        RuleEngineResult engineResult = ruleEvaluator.evaluate(evaluationRobot);
        PolicyEvaluation policyEvaluation = findZonePolicyEvaluation(mission, engineResult);
        RuleEvaluationResult selectedRule = policyEvaluation.matchedPolicyRule() != null
                ? policyEvaluation.matchedPolicyRule()
                : engineResult.getMatchedRule();

        StrategyResult strategyResult = selectedRule != null
                ? strategyContext.executeStrategy(selectedRule.getTargetStrategyName(), evaluationRobot)
                : new StrategyResult(
                        "NoStrategy",
                        "No active rule matched this mission input, so the robot behavior is unchanged."
                );

        mission.setAssignedRobotId(selectedRobot.getId());
        mission.setAssignedRobotName(formatRobotName(selectedRobot));
        mission.setAssignmentReason(robotAssignment.assignmentReason());
        mission.setMatchedRuleName(selectedRule != null ? selectedRule.getRuleName() : NO_RULE_MATCHED);
        mission.setSelectedStrategyName(strategyResult.getStrategyName());
        mission.setActionMessage(strategyResult.getActionMessage());
        mission.setDecisionSummary(buildDecisionSummary(
                mission,
                evaluationRobot,
                robotAssignment,
                policyEvaluation,
                selectedRule,
                strategyResult
        ));
        mission.setProcessedAt(LocalDateTime.now());
        mission.setStatus(MissionStatus.ASSIGNED);
        return missionRepository.save(mission);
    }

    private Mission keepPendingWithoutRobot(Mission mission) {
        String message = "No available robot found for this mission.";
        mission.setAssignedRobotId(null);
        mission.setAssignedRobotName(null);
        mission.setAssignmentReason(message);
        mission.setMatchedRuleName(null);
        mission.setSelectedStrategyName(null);
        mission.setActionMessage(null);
        mission.setDecisionSummary(message
                + " Mission remains PENDING and was not sent to RuleEvaluator or StrategyContext.");
        mission.setProcessedAt(null);
        mission.setStatus(MissionStatus.PENDING);
        return missionRepository.save(mission);
    }

    private Robot buildEvaluationRobot(Mission mission, Robot selectedRobot) {
        Robot evaluationRobot = new Robot();
        evaluationRobot.setId(selectedRobot.getId());
        evaluationRobot.setCode(selectedRobot.getCode());
        evaluationRobot.setName(selectedRobot.getName());
        evaluationRobot.setBattery(selectedRobot.getBattery() != null ? selectedRobot.getBattery() : 100);
        evaluationRobot.setObstacleDetected(Boolean.TRUE.equals(selectedRobot.getObstacleDetected()));
        evaluationRobot.setRobotLoad(resolveMissionRobotLoad(mission));
        evaluationRobot.setDistance(resolveMissionDistance(mission));
        evaluationRobot.setPriority(mission.getPriority());
        evaluationRobot.setStatus(selectedRobot.getStatus());
        evaluationRobot.setCurrentStrategy(selectedRobot.getCurrentStrategy());
        return evaluationRobot;
    }

    private Integer resolveMissionRobotLoad(Mission mission) {
        return CargoType.estimatedLoadPercentFor(mission.getCargoType());
    }

    private Double resolveMissionDistance(Mission mission) {
        double zoneBaseDistance = switch (mission.getZone()) {
            case "Zone A" -> 4.0;
            case "Zone B" -> 9.0;
            case "Zone C" -> 14.0;
            default -> 8.0;
        };
        return zoneBaseDistance + resolveLocationIndex(mission.getLocationCode());
    }

    private int resolveLocationIndex(String locationCode) {
        if (locationCode == null || locationCode.length() < 2) {
            return 1;
        }
        try {
            return Integer.parseInt(locationCode.substring(1));
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    private PolicyEvaluation findZonePolicyEvaluation(Mission mission, RuleEngineResult engineResult) {
        return assignmentRepository.findByZone(mission.getZone())
                .map(assignment -> evaluateAssignedPolicy(mission, assignment, engineResult))
                .orElseGet(() -> new PolicyEvaluation(
                        "No zone policy assignment was found for " + mission.getZone()
                                + "; evaluated active Admin-created rules.",
                        null
                ));
    }

    private PolicyEvaluation evaluateAssignedPolicy(Mission mission,
                                                    ZonePolicyAssignment assignment,
                                                    RuleEngineResult engineResult) {
        Rule assignedRule = ruleRepository.findById(assignment.getRuleId())
                .filter(Rule::isActive)
                .orElse(null);
        if (assignedRule == null) {
            return new PolicyEvaluation(
                    mission.getZone()
                            + " policy assignment references a deleted or inactive rule; "
                            + "evaluated active Admin-created rules.",
                    null
            );
        }

        RuleEvaluationResult assignedRuleResult = engineResult.getRuleResults()
                .stream()
                .filter(ruleResult -> matchesRule(ruleResult, assignedRule))
                .findFirst()
                .orElse(null);
        if (assignedRuleResult == null) {
            return new PolicyEvaluation(
                    mission.getZone() + " policy '" + assignedRule.getRuleName()
                            + "' was assigned but was not available in the active rule evaluation; "
                            + "evaluated active Admin-created rules.",
                    null
            );
        }
        if (!assignedRuleResult.isMatched()) {
            return new PolicyEvaluation(
                    mission.getZone() + " policy '" + assignedRule.getRuleName()
                            + "' was evaluated but did not match mission input; "
                            + "fallback used the first matched active rule.",
                    null
            );
        }
        return new PolicyEvaluation(
                mission.getZone() + " policy '" + assignedRule.getRuleName()
                        + "' matched mission input and selected its configured strategy.",
                assignedRuleResult
        );
    }

    private boolean matchesRule(RuleEvaluationResult ruleResult, Rule rule) {
        return Objects.equals(ruleResult.getRuleName(), rule.getRuleName())
                && Objects.equals(ruleResult.getConditionExpression(), rule.getConditionExpression())
                && Objects.equals(ruleResult.getTargetStrategyName(), rule.getStrategyName());
    }

    private String buildDecisionSummary(Mission mission,
                                        Robot evaluationRobot,
                                        RobotAssignment robotAssignment,
                                        PolicyEvaluation policyEvaluation,
                                        RuleEvaluationResult selectedRule,
                                        StrategyResult strategyResult) {
        String ruleSummary = selectedRule != null
                ? "Matched rule '" + selectedRule.getRuleName() + "'."
                : "No active rule matched.";
        return String.format(
                Locale.US,
                "Mission %s processed for %s / %s (%s). Assigned robot %s. %s "
                        + "Active workload for selected robot: %d active mission(s), "
                        + "%d active high-priority mission(s). %s %s Condition input: battery=%d, "
                        + "obstacleDetected=%s, robotLoad=%d, distance=%.1f, priority=%d. "
                        + "StrategyContext dispatched %s.",
                mission.getRequestCode(),
                mission.getZone(),
                mission.getLocationCode(),
                mission.getCargoType(),
                formatRobotName(evaluationRobot),
                robotAssignment.assignmentReason(),
                robotAssignment.activeMissionCount(),
                robotAssignment.activeHighPriorityMissionCount(),
                policyEvaluation.message(),
                ruleSummary,
                evaluationRobot.getBattery(),
                evaluationRobot.getObstacleDetected(),
                evaluationRobot.getRobotLoad(),
                evaluationRobot.getDistance(),
                evaluationRobot.getPriority(),
                strategyResult.getStrategyName()
        );
    }

    private String formatRobotName(Robot robot) {
        if (robot.getCode() == null || robot.getCode().isBlank()) {
            return robot.getName();
        }
        return robot.getName() + " (" + robot.getCode() + ")";
    }

    private record PolicyEvaluation(String message, RuleEvaluationResult matchedPolicyRule) {
    }
}
