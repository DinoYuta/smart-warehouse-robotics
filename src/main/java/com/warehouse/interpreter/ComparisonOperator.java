package com.warehouse.interpreter;

import java.util.Arrays;
import java.util.function.BiPredicate;

public enum ComparisonOperator {

    LESS_THAN("<", (left, right) -> left < right),
    GREATER_THAN(">", (left, right) -> left > right),
    LESS_THAN_OR_EQUAL("<=", (left, right) -> left <= right),
    GREATER_THAN_OR_EQUAL(">=", (left, right) -> left >= right),
    EQUAL_TO("==", (left, right) -> Double.compare(left, right) == 0);

    private final String symbol;
    private final BiPredicate<Double, Double> predicate;

    ComparisonOperator(String symbol, BiPredicate<Double, Double> predicate) {
        this.symbol = symbol;
        this.predicate = predicate;
    }

    public boolean matches(Integer actualValue, Integer expectedValue) {
        return actualValue != null
                && expectedValue != null
                && matches(actualValue.doubleValue(), expectedValue.doubleValue());
    }

    public boolean matches(Double actualValue, Double expectedValue) {
        return actualValue != null && expectedValue != null && predicate.test(actualValue, expectedValue);
    }

    public String getSymbol() {
        return symbol;
    }

    public static ComparisonOperator fromSymbol(String symbol) {
        return Arrays.stream(values())
                .filter(operator -> operator.symbol.equals(symbol))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported operator: " + symbol));
    }
}
