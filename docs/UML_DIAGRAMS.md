# UML Diagrams

These diagrams summarize the final architecture of the Smart Warehouse Robotics Decision Engine. They are written in Mermaid so they can render in Markdown tools that support Mermaid diagrams.

These diagrams focus on the original decision-engine core. For the complete
current role workflow and source map, use the [Documentation Index](README.md),
[Mission Lifecycle](05-mission-lifecycle.md), and [Central Code Map](12-code-map.md).

## Use Case Diagram

```mermaid
flowchart LR
    Admin["Warehouse Manager / Admin"]

    subgraph System["Smart Warehouse Decision Engine"]
        UC1(["View dashboard"])
        UC2(["Manage rules"])
        UC3(["Run robot simulation"])
        UC4(["View rule evaluation trace"])
        UC5(["View execution history"])
        UC6(["View system flow"])
    end

    Admin --> UC1
    Admin --> UC2
    Admin --> UC3
    Admin --> UC4
    Admin --> UC5
    Admin --> UC6
```

## Class Diagram

The project contains two different Java types named `Strategy`:

* `com.warehouse.strategy.Strategy` is the Strategy Pattern interface.
* `com.warehouse.model.Strategy` is shown as `StrategyEntity` in the diagram because it is the persisted database entity.

```mermaid
classDiagram
    direction LR

    class DashboardController
    class RuleManagementController
    class SimulationController
    class SystemFlowController

    class DashboardService
    class RuleService
    class SimulationService
    class RuleEvaluator
    class StrategyContext

    class Expression {
        <<interface>>
        evaluate(robot)
        getExpressionText()
    }
    class BatteryExpression
    class ObstacleExpression
    class RobotLoadExpression
    class DistanceExpression
    class PriorityExpression
    class AndExpression
    class OrExpression
    class RuleParser
    class ExpressionEvaluation
    class RuleEvaluationResult
    class RuleEngineResult

    class Strategy {
        <<interface>>
        getStrategyName()
        execute(robot)
    }
    class ChargingStrategy
    class ObstacleAvoidanceStrategy
    class HeavyLoadStrategy
    class EnergySavingStrategy
    class FastRouteStrategy
    class SafeRouteStrategy
    class StrategyResult

    class Robot {
        <<entity>>
        code
        battery
        obstacleDetected
        robotLoad
        distance
        priority
        status
    }
    class Rule {
        <<entity>>
        ruleName
        conditionExpression
        strategyName
        activeStatus
        priority
    }
    class StrategyEntity {
        <<entity>>
        code
        name
        className
        active
    }
    class RuleExecutionHistory {
        <<entity>>
        robotCode
        matchedRuleName
        selectedStrategy
        actionMessage
        executedAt
    }

    class RobotRepository
    class RuleRepository
    class StrategyRepository
    class RuleExecutionHistoryRepository

    DashboardController --> DashboardService
    RuleManagementController --> RuleService
    SimulationController --> SimulationService
    SystemFlowController --> RuleService
    SystemFlowController --> DashboardService

    DashboardService --> RobotRepository
    DashboardService --> StrategyRepository
    DashboardService --> RuleService
    DashboardService --> RuleEvaluator
    DashboardService --> StrategyContext
    DashboardService --> RuleExecutionHistoryRepository

    RuleEvaluator --> RuleService
    RuleService --> RuleRepository
    RuleService --> StrategyRepository
    RuleService --> RuleParser
    SimulationService --> RuleEvaluator
    SimulationService --> StrategyContext
    SimulationService --> RuleExecutionHistoryRepository

    RuleParser --> Expression
    Expression <|.. BatteryExpression
    Expression <|.. ObstacleExpression
    Expression <|.. RobotLoadExpression
    Expression <|.. DistanceExpression
    Expression <|.. PriorityExpression
    Expression <|.. AndExpression
    Expression <|.. OrExpression
    AndExpression o-- Expression
    OrExpression o-- Expression
    RuleEngineResult o-- RuleEvaluationResult
    RuleEvaluationResult --> ExpressionEvaluation

    StrategyContext --> Strategy
    Strategy <|.. ChargingStrategy
    Strategy <|.. ObstacleAvoidanceStrategy
    Strategy <|.. HeavyLoadStrategy
    Strategy <|.. EnergySavingStrategy
    Strategy <|.. FastRouteStrategy
    Strategy <|.. SafeRouteStrategy
    Strategy --> StrategyResult

    Robot --> StrategyEntity
    Rule --> StrategyEntity
    RobotRepository --> Robot
    RuleRepository --> Rule
    StrategyRepository --> StrategyEntity
    RuleExecutionHistoryRepository --> RuleExecutionHistory
```

## Sequence Diagram: Simulation Flow

```mermaid
sequenceDiagram
    actor User as Warehouse Manager / Admin
    participant UI as Simulation UI
    participant Controller as SimulationController
    participant SimService as SimulationService
    participant Evaluator as RuleEvaluator
    participant RuleSvc as RuleService
    participant RuleRepo as RuleRepository
    participant Parser as RuleParser
    participant Expr as Expression
    participant EngineResult as RuleEngineResult
    participant Context as StrategyContext
    participant SelectedStrategy as Selected Strategy
    participant HistoryRepo as RuleExecutionHistoryRepository

    User->>UI: Submit simulation form
    UI->>Controller: POST /simulation
    Controller->>SimService: simulate(SimulationRequestDto)
    SimService->>SimService: Create simulation Robot
    SimService->>Evaluator: evaluate(robot)
    Evaluator->>RuleSvc: evaluateRules(robot)
    RuleSvc->>RuleRepo: Load active rules by priority
    RuleRepo-->>RuleSvc: Active rules

    loop Each active rule
        RuleSvc->>Parser: parse(conditionExpression)
        Parser-->>RuleSvc: Expression
        RuleSvc->>Expr: evaluate(robot)
        Expr-->>RuleSvc: ExpressionEvaluation
        RuleSvc->>RuleSvc: Create RuleEvaluationResult
    end

    RuleSvc-->>Evaluator: RuleEngineResult
    Evaluator-->>SimService: RuleEngineResult
    SimService->>EngineResult: Read first matched rule by priority

    alt Matched rule exists
        SimService->>Context: executeStrategy(selectedStrategyName, robot)
        Context->>SelectedStrategy: execute(robot)
        SelectedStrategy-->>Context: StrategyResult
        Context-->>SimService: StrategyResult
    else No matched rule
        SimService->>SimService: Create NoStrategy result
    end

    SimService->>HistoryRepo: save(RuleExecutionHistory)
    SimService-->>Controller: SimulationResultDto
    Controller-->>UI: Show simulation result and trace
```

## Activity Diagram: Rule Evaluation

```mermaid
flowchart TD
    Start([Receive robot state])
    LoadRules[Load active rules ordered by priority]
    HasRule{More active rules?}
    Parse[Parse condition expression]
    Evaluate[Evaluate expression against robot]
    Matched{Expression matched?}
    MarkMatched[Mark rule as MATCHED]
    MarkSkipped[Mark rule as SKIPPED]
    Store[Store RuleEvaluationResult]
    AnyMatched{Any matched rules?}
    Select[Select first matched rule by priority]
    Execute[Execute selected strategy]
    NoStrategy[Return NoStrategy]
    SaveHistory[Save RuleExecutionHistory]
    ShowResult([Show simulation result])

    Start --> LoadRules
    LoadRules --> HasRule
    HasRule -- Yes --> Parse
    Parse --> Evaluate
    Evaluate --> Matched
    Matched -- Yes --> MarkMatched
    Matched -- No --> MarkSkipped
    MarkMatched --> Store
    MarkSkipped --> Store
    Store --> HasRule
    HasRule -- No --> AnyMatched
    AnyMatched -- Yes --> Select
    Select --> Execute
    AnyMatched -- No --> NoStrategy
    Execute --> SaveHistory
    NoStrategy --> SaveHistory
    SaveHistory --> ShowResult
```
