# CODE_STYLE.md

# CODE STYLE GUIDE

## GENERAL STYLE

✔ Clean OOP
✔ Readable naming
✔ Small classes
✔ Small methods
✔ Educational clarity first

---

# NAMING RULES

## Classes

Use clear descriptive names.

Examples:

* BatteryExpression
* StrategyContext
* RuleEvaluator

Avoid vague names like:

* Helper
* Utils
* Manager

---

# METHOD RULES

Methods should:

✔ Do one thing only
✔ Be easy to understand
✔ Avoid deep nesting

Preferred:

* evaluate()
* execute()
* matchRule()

Avoid giant methods.

---

# CONTROLLER STYLE

Controllers should remain lightweight.

Responsibilities:

✔ Receive requests
✔ Call services
✔ Return views

Avoid business logic inside controllers.

---

# SERVICE STYLE

Services contain:

✔ Rule evaluation flow
✔ Strategy selection flow
✔ Simulation orchestration

Avoid putting database queries directly in services when repositories exist.

---

# ENTITY STYLE

Entities should remain simple.

Avoid:

❌ Business logic inside entities
❌ Complex helper methods

---

# UI STYLE

Frontend stack:

* Bootstrap 5
* Thymeleaf or JSP

UI should remain:

✔ Minimal
✔ Clean
✔ Educational

Avoid enterprise-level complexity.

---

# COMMENTS

Use comments only when they improve educational understanding.

Avoid obvious comments.

Good:

// Evaluate combined logical expressions

Bad:

// Increment i

---

# FUTURE CONTRIBUTION RULE

Whenever adding new functionality:

1. Preserve Interpreter Pattern
2. Preserve Strategy Pattern
3. Avoid architectural shortcuts
4. Keep code easy for presentations/demo
