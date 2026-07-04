# ARCHITECTURE_RULES.md

# ARCHITECTURE RULES

## CORE DESIGN PRINCIPLES

This project exists primarily to demonstrate:

1. Interpreter Pattern
2. Strategy Pattern
3. Runtime behavior switching
4. Dynamic rule evaluation

All future code must preserve these concepts clearly.

---

# ARCHITECTURE STYLE

## Required Style

✔ Modular
✔ Beginner-friendly
✔ Educational
✔ Maintainable
✔ Extensible

---

# FORBIDDEN ARCHITECTURE

❌ Giant if-else chains
❌ Monolithic services
❌ Hardcoded strategy switching
❌ Hardcoded rule logic
❌ Overengineered parsers
❌ Premature AI complexity

---

# INTERPRETER PATTERN RULES

## Expressions must remain modular

Each condition type should be isolated into its own Expression class.

Examples:

* BatteryExpression
* ObstacleExpression
* RobotLoadExpression

Logical composition must remain separated:

* AndExpression
* OrExpression

---

# STRATEGY PATTERN RULES

Strategies must remain independent modules.

Each robot behavior should exist as a separate strategy class.

Examples:

* ChargingStrategy
* ObstacleAvoidanceStrategy
* HeavyLoadStrategy

Avoid behavior logic directly inside controllers.

---

# DATABASE RULES

Rules should eventually become fully database-driven.

Future direction:

✔ Rule CRUD
✔ Dynamic activation
✔ Rule priority
✔ Runtime strategy switching

---

# UI RULES

UI must remain:

✔ Simple
✔ Visual
✔ Educational
✔ Dashboard-oriented

Avoid:

❌ Complex admin enterprise UI
❌ Heavy animations
❌ Overcrowded pages

---

# CODE ORGANIZATION RULES

Controllers:

* Handle requests only

Services:

* Handle business logic

Repositories:

* Handle database access

Interpreter package:

* Rule evaluation only

Strategy package:

* Behavior execution only

Simulator package:

* Simulation orchestration only

---

# FUTURE SCALABILITY

Future versions may include:

* Rule Builder UI
* Dynamic expression parser
* Robot fleet simulation
* Analytics dashboard
* Event logs
* Rule execution history

Current implementation should not block these future expansions.
