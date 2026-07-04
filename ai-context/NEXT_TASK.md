# NEXT TASK

## CURRENT PRIORITY

Phase 5.2 - Rule Execution History and System Flow Visualization

---

# MAIN GOALS

Persist and display rule execution history so the project can explain not only which rule matched, but also how the Interpreter and Strategy patterns produced the final robot action.

---

# TARGET FEATURES

## Rule Execution History

Add a small database-backed execution history module:

* RuleExecutionHistory entity
* RuleExecutionHistoryRepository
* Service method to record simulator executions
* Dashboard recent execution list backed by persisted data
* Clear fields for robot state, matched rule, selected strategy, action message, and execution time

---

## System Flow Page

Create a beginner-friendly route:

* `/system-flow`

The page should visualize:

* Robot State Input
* Interpreter Evaluation
* Rule Match Selection
* Strategy Dispatch
* Robot Action Result

Keep this simple. Do not add complex animations or an enterprise workflow builder.

---

## Test Targets

Add focused tests for:

* Rule priority selection
* No-match behavior
* StrategyContext dispatch
* Execution history record creation

---

# IMPORTANT IMPLEMENTATION RULES

* Preserve Interpreter Pattern clarity
* Preserve Strategy Pattern modularity
* Do not put behavior logic in controllers
* Do not create giant if-else chains
* Do not build a complex DSL parser yet
* Keep UI simple, visual, and educational
* Keep each new class small and easy to explain

---

# CURRENTLY COMPLETED BEFORE THIS TASK

* Database-backed rule CRUD
* Active rule loading
* Rule priority support
* Strategy dropdown from active strategies
* Simulator input for battery, obstacleDetected, robotLoad, distance, and priority
* DistanceExpression and PriorityExpression
* Seed rules for FastRouteStrategy and SafeRouteStrategy
* Parser tests for distance and priority

---

# SUCCESS CRITERIA

This milestone is successful when:

* Every simulation can be recorded as a history entry
* Dashboard can display real recent executions from the database
* System Flow page explains the runtime decision path
* Interpreter and Strategy patterns remain easy to identify in the codebase
* `mvn test` passes
