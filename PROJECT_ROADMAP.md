# PROJECT_ROADMAP.md

# PROJECT ROADMAP

---

# PHASE 1 — FOUNDATION SETUP

## Goal

Build the basic warehouse system structure.

### Tasks

* Setup Spring Boot project
* Configure database
* Create base UI layout
* Setup project architecture
* Create Robot entity
* Create Rule entity
* Create Strategy structure

### Deliverables

* Running application
* Basic dashboard
* Database connection

---

# PHASE 2 — BASIC RULE ENGINE

## Goal

Build the first working Interpreter Pattern.

### Tasks

* Create Expression interface
* Create ConditionExpression
* Create AndExpression
* Create OrExpression
* Create Rule Evaluator
* Implement simple parser

### Example Rule

IF battery < 20 THEN ChargingStrategy

### Deliverables

* Dynamic rule evaluation
* Rule execution result

---

# PHASE 3 — STRATEGY IMPLEMENTATION

## Goal

Implement dynamic robot behaviors.

### Tasks

* Create Strategy interface
* Create multiple strategies
* Implement Strategy Context
* Dynamic strategy switching

### Strategies

* ChargingStrategy
* FastRouteStrategy
* SafeRouteStrategy

### Deliverables

* Runtime behavior switching

---

# PHASE 4 — RULE MANAGEMENT UI

## Goal

Allow admin to create rules visually.

### Tasks

* Rule creation form
* Rule listing table
* Rule activation toggle
* Rule editing
* Rule deletion

### Deliverables

* Functional rule management system

---

# PHASE 5 — ROBOT SIMULATION

## Goal

Simulate robot decisions.

### Tasks

* Robot condition simulator
* Execution result page
* Rule matching logs
* Strategy execution logs

### Deliverables

* Interactive robot simulation

---

# PHASE 6 — VISUALIZATION

## Goal

Improve understanding of Interpreter Pattern.

### Tasks

* Expression tree visualization
* Rule execution flow
* Strategy switching visualization
* Warehouse activity dashboard

### Deliverables

* Educational architecture visualization

---

# PHASE 7 — ADVANCED FEATURES

## Goal

Add enterprise-like behavior.

### Tasks

* Rule priority
* Nested conditions
* Multi-condition rules
* Rule conflict handling
* Execution history

### Deliverables

* Advanced rule engine behavior

---

# PHASE 8 — FINAL POLISH

## Goal

Prepare for presentation/demo.

### Tasks

* UI improvements
* Bug fixing
* Performance optimization
* Final testing
* Documentation
* UML diagrams

### Deliverables

* Final presentation-ready system

---

# PHASE 9 - ROLE-BASED WAREHOUSE WORKFLOW

## Goal

Expand the project from an admin-focused rule simulation system into a role-based warehouse workflow while preserving the existing Interpreter Pattern and Strategy Pattern architecture.

The role workflow is a business layer around the current rule engine. The technical core remains:

```text
Robot Input -> Interpreter -> Rule Match -> Strategy Dispatch -> Robot Action -> History
```

The planned business flow is:

```text
Staff Pickup Request -> Robot Selection -> Rule/Policy Assignment from Manager -> Interpreter Evaluation -> Strategy Dispatch -> Robot Action -> Live Map / Mission Status -> History
```

## Planned Task Order

1. [x] Documentation update for Admin / Manager / Staff workflow
2. [x] Role-based navigation structure
3. [x] Staff Pickup Request page
4. [x] PickupRequest / Mission model and service
5. [x] Manager rule/policy assignment page
6. [ ] Connect mission flow to existing RuleEvaluator and StrategyContext
7. [ ] Mission status / mission history page
8. [ ] Live Warehouse Map UI

## Role Scope

### Admin

Admin has full system access and manages system configuration.

* Creates, edits, deletes, enables, and disables rules.
* Configures rules evaluated by the Interpreter Pattern.
* Configures or selects strategies used by the Strategy Pattern.
* Can manage robots.
* Can view dashboard, rules, robots, simulation, system flow, and execution history.

### Manager

Manager controls warehouse operation using rules and policies already created by Admin.

* Assigns existing rule sets or policies to robots or warehouse zones.
* Monitors robot status.
* Decides which robots are available for operation.
* Manages robot availability, zone assignment, and rule or policy assignment.

### Staff

Staff handles customer pickup requests from an operational perspective.

* Creates pickup requests when customers need cargo.
* Selects or enters cargo type, cargo location, zone, and priority.
* Tracks mission status until completed.

## Deliverables

* Role-based navigation and page structure
* Staff pickup request workflow
* Mission data model connected to the existing rule engine
* Manager assignment workflow for existing rules and policies
* Mission status and mission history review
* Simple live warehouse map UI after mission/request data exists

---

# FUTURE POSSIBLE FEATURES

These ideas are not part of the immediate implementation roadmap. They should only be considered after the role-based mission workflow is working.

* Additional warehouse zones beyond Zone A, Zone B, and Zone C
* More detailed mission reporting
* More rule and strategy examples for demonstration
* Presentation-focused UI refinements
