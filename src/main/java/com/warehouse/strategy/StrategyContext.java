package com.warehouse.strategy;

import com.warehouse.model.Robot;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class StrategyContext {

    private final Map<String, Strategy> strategies;

    public StrategyContext(List<Strategy> strategies) {
        this.strategies = strategies.stream()
                .collect(Collectors.toMap(Strategy::getStrategyName, Function.identity()));
    }

    public StrategyResult executeStrategy(String strategyName, Robot robot) {
        // The selected strategy controls robot behavior after a rule matches.
        Strategy strategy = strategies.get(strategyName);
        if (strategy == null) {
            return new StrategyResult("NoStrategy", "No matching strategy was selected.");
        }
        return strategy.execute(robot);
    }
}
