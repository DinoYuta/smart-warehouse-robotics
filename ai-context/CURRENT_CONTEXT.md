# CURRENT CONTEXT

## Project Name

Smart Warehouse Robotics Decision Engine using Interpreter and Strategy Patterns

---

# CURRENT DEVELOPMENT STAGE

Phase 5.3 Completed - Moving to Final Polish, Documentation, UML Diagrams, and Presentation Preparation

---

# CURRENT WORKING STATUS

## Environment

* Java configured
* Maven configured
* Spring Boot configured
* Embedded Tomcat working
* SQL Server listening on localhost:1433
* Application verified locally
* Latest test run passed with 16 tests

---

## Core Architecture

Clean package structure is active:

com.warehouse
* controller
* service
* repository
* model
* strategy
* interpreter
* simulator
* dto
* config

---

## Interpreter Pattern

Expression interface is implemented.

Existing Expressions:

* BatteryExpression
* ObstacleExpression
* RobotLoadExpression
* DistanceExpression
* PriorityExpression
* AndExpression
* OrExpression

RuleParser supports:

* battery
* obstacleDetected
* robotLoad
* distance
* priority
* <
* >
* ==
* <=
* >=
* AND
* OR

Dynamic expression composition is supported.

Focused tests verify:

* Single-condition parsing
* AND expression behavior
* OR expression behavior
* Rule evaluation output
* Interpreter leaf condition trace output

---

## Strategy Pattern

Existing Strategies:

* ChargingStrategy
* ObstacleAvoidanceStrategy
* HeavyLoadStrategy
* EnergySavingStrategy
* FastRouteStrategy
* SafeRouteStrategy

StrategyContext dispatches by strategy name and keeps behavior logic outside controllers.

Focused tests verify:

* Correct dispatch for all supported strategies
* NoStrategy fallback when strategy name is unknown

---

## Rule Engine

Current rule engine supports:

* Database-backed active rule loading
* Runtime rule create, edit, delete, enable, and disable
* Priority-based rule selection
* Multiple rule evaluation
* Matched / skipped rule trace
* Removal of giant hardcoded if-else chains

Lower numeric priority means higher priority.

---

## Simulator

Current simulator inputs:

* Battery
* Obstacle detected
* Robot load
* Distance
* Task priority

Current simulator outputs:

* Input state metrics
* Final decision summary
* Matched rule
* Selected strategy
* Final robot action
* Evaluated leaf conditions
* Per-rule evaluation trace table
* MATCHED / SKIPPED rule status
* Highlighted selected rule
* Persistent execution history after each run

---

## Rule Execution History

Persistent rule execution history is implemented.

The system now saves simulation execution results after:

1. Robot input is submitted
2. Interpreter evaluates active rules
3. Rule priority selects the final matched rule
4. StrategyContext dispatches the selected strategy
5. Final robot action is returned

Recent simulation execution history is displayed on:

* Dashboard
* System Flow page

---

## System Flow Visualization

The `/system-flow` page is implemented.

It visualizes:

* Robot Input
* Interpreter evaluation
* Rule matching
* Strategy dispatch
* Robot action
* Execution history

The page also displays:

* Active rules
* Strategy information
* Recent execution history

---

# CURRENT RUNTIME STATUS

Latest verification on May 20, 2026:

* `mvn test` passed
* 16 tests passed
* RuleParser tests passed
* Rule priority selection test passed
* StrategyContext dispatch tests passed
* Unknown strategy fallback test passed
* SimulationService trace output test passed

Previous route verification confirmed:

* `/dashboard` returned HTTP 200
* `/rules` returned HTTP 200
* `/simulation` returned HTTP 200
* `/system-flow` returned HTTP 200
* `POST /simulation` returned HTTP 200

---

# CURRENT LOCALHOST ROUTES

Primary routes:

* http://localhost:8080
* http://localhost:8080/dashboard
* http://localhost:8080/rules
* http://localhost:8080/simulation
* http://localhost:8080/system-flow

Alternative verified runtime if port 8080 is busy:

* http://localhost:8081
* http://localhost:8081/dashboard
* http://localhost:8081/rules
* http://localhost:8081/simulation
* http://localhost:8081/system-flow

---

# IMPORTANT NOTES

* Keep architecture educational
* Avoid enterprise overengineering
* Keep Interpreter Pattern clearly visible
* Keep Strategy Pattern clearly visible
* Avoid giant if-else logic
* Keep simulator understandable for beginners
* Add new conditions as separate Expression classes
* Keep Strategy behavior in strategy classes, not controllers
* Do not build a complicated DSL parser too soon
* Do not add AI, WebSocket, microservices, or realtime map features unless explicitly requested
* Focus next work on presentation readiness

---

# DESIGN SOURCE

UI references are located in:

* /docs
* DESIGN.md

Generated previously from Stitch.

Future UI work should follow those designs consistently without unnecessary redesign.

---

# NEXT FOCUS

Prepare the project for final presentation.

Recommended next work:

1. Create UML diagrams:
   * Class Diagram
   * Sequence Diagram
   * Activity Diagram
   * Use Case Diagram

2. Prepare final demo script.

3. Prepare final screenshots:
   * Dashboard
   * Rule Management
   * Simulation trace
   * System Flow
   * Execution History

4. Clean up documentation:
   * README
   * PROJECT_STATUS.md
   * PROJECT_ROADMAP.md

5. Perform small UI polish only where needed.