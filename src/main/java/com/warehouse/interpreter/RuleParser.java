package com.warehouse.interpreter;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class RuleParser {

    private static final Pattern NUMBER_CONDITION = Pattern.compile(
            "^(battery|robotLoad|distance|priority)\\s*(<=|>=|==|<|>)\\s*(\\d+(?:\\.\\d+)?)$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern OBSTACLE_CONDITION = Pattern.compile(
            "^obstacleDetected\\s*==\\s*(TRUE|FALSE)$",
            Pattern.CASE_INSENSITIVE
    );

    public Expression parse(String conditionExpression) {
        // Interpreter step: parse rule conditions into expressions evaluated against robot context.
        String normalizedExpression = normalize(conditionExpression);

        List<String> orParts = split(normalizedExpression, "OR");
        if (orParts.size() > 1) {
            return new OrExpression(orParts.stream().map(this::parse).toList());
        }

        List<String> andParts = split(normalizedExpression, "AND");
        if (andParts.size() > 1) {
            return new AndExpression(andParts.stream().map(this::parse).toList());
        }

        return parseSingleCondition(normalizedExpression);
    }

    private String normalize(String expression) {
        String normalizedExpression = expression.trim();
        if (normalizedExpression.toUpperCase().startsWith("IF ")) {
            normalizedExpression = normalizedExpression.substring(3).trim();
        }

        int thenIndex = normalizedExpression.toUpperCase().indexOf(" THEN ");
        if (thenIndex >= 0) {
            normalizedExpression = normalizedExpression.substring(0, thenIndex).trim();
        }

        return normalizedExpression;
    }

    private List<String> split(String expression, String logicalOperator) {
        return Arrays.stream(expression.split("(?i)\\s+" + logicalOperator + "\\s+"))
                .map(String::trim)
                .filter(part -> !part.isBlank())
                .toList();
    }

    private Expression parseSingleCondition(String expression) {
        Matcher numberMatcher = NUMBER_CONDITION.matcher(expression);
        if (numberMatcher.matches()) {
            String conditionName = numberMatcher.group(1);
            ComparisonOperator operator = ComparisonOperator.fromSymbol(numberMatcher.group(2));
            String thresholdText = numberMatcher.group(3);

            if ("distance".equalsIgnoreCase(conditionName)) {
                return new DistanceExpression(operator, Double.parseDouble(thresholdText));
            }

            int threshold = parseWholeNumberThreshold(conditionName, thresholdText);

            if ("battery".equalsIgnoreCase(conditionName)) {
                return new BatteryExpression(operator, threshold);
            }
            if ("robotLoad".equalsIgnoreCase(conditionName)) {
                return new RobotLoadExpression(operator, threshold);
            }
            return new PriorityExpression(operator, threshold);
        }

        Matcher obstacleMatcher = OBSTACLE_CONDITION.matcher(expression);
        if (obstacleMatcher.matches()) {
            boolean expectedValue = Boolean.parseBoolean(obstacleMatcher.group(1).toLowerCase());
            return new ObstacleExpression(expectedValue);
        }

        throw new IllegalArgumentException("Unsupported condition: " + expression);
    }

    private int parseWholeNumberThreshold(String conditionName, String thresholdText) {
        if (thresholdText.contains(".")) {
            throw new IllegalArgumentException(conditionName + " requires a whole-number threshold.");
        }
        return Integer.parseInt(thresholdText);
    }
}
