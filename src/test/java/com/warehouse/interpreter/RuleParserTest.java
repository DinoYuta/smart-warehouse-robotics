package com.warehouse.interpreter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.warehouse.model.Robot;
import org.junit.jupiter.api.Test;

class RuleParserTest {

    private final RuleParser ruleParser = new RuleParser();

    @Test
    void parsesAndEvaluatesSupportedSingleConditions() {
        Robot robot = createRobot(15, true, 90, 20.0, 1);

        assertMatches("battery < 20", robot, true);
        assertMatches("obstacleDetected == TRUE", robot, true);
        assertMatches("robotLoad > 80", robot, true);
        assertMatches("distance > 15", robot, true);
        assertMatches("priority == 1", robot, true);
    }

    @Test
    void andExpressionOnlyMatchesWhenAllChildrenMatch() {
        Robot matchingRobot = createRobot(15, true, 90, 20.0, 1);
        Robot robotWithoutObstacle = createRobot(15, false, 90, 20.0, 1);

        Expression expression = ruleParser.parse("battery < 20 AND obstacleDetected == TRUE");

        ExpressionEvaluation matchingEvaluation = expression.evaluate(matchingRobot);
        ExpressionEvaluation nonMatchingEvaluation = expression.evaluate(robotWithoutObstacle);

        assertThat(matchingEvaluation.isMatched()).isTrue();
        assertThat(matchingEvaluation.getChildren())
                .extracting(ExpressionEvaluation::isMatched)
                .containsExactly(true, true);
        assertThat(nonMatchingEvaluation.isMatched()).isFalse();
        assertThat(nonMatchingEvaluation.getChildren())
                .extracting(ExpressionEvaluation::isMatched)
                .containsExactly(true, false);
        assertThat(expression.getExpressionText()).isEqualTo("battery < 20 AND obstacleDetected == TRUE");
    }

    @Test
    void orExpressionMatchesWhenAtLeastOneChildMatches() {
        Robot batteryMatchRobot = createRobot(15, false, 40, 8.2, 3);
        Robot priorityMatchRobot = createRobot(60, false, 40, 8.2, 1);
        Robot noMatchRobot = createRobot(60, false, 40, 8.2, 3);

        Expression expression = ruleParser.parse("battery < 20 OR priority == 1");

        assertThat(expression.evaluate(batteryMatchRobot).isMatched()).isTrue();
        assertThat(expression.evaluate(priorityMatchRobot).isMatched()).isTrue();
        assertThat(expression.evaluate(noMatchRobot).isMatched()).isFalse();
        assertThat(expression.getExpressionText()).isEqualTo("battery < 20 OR priority == 1");
    }

    @Test
    void rejectsDecimalThresholdForWholeNumberConditions() {
        assertThatThrownBy(() -> ruleParser.parse("priority > 2.5"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("whole-number threshold");
    }

    private void assertMatches(String conditionExpression, Robot robot, boolean expectedMatch) {
        Expression expression = ruleParser.parse(conditionExpression);
        ExpressionEvaluation evaluation = expression.evaluate(robot);

        assertThat(evaluation.isMatched()).isEqualTo(expectedMatch);
        assertThat(expression.getExpressionText()).isEqualTo(conditionExpression);
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
