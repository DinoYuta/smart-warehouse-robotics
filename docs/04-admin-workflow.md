# Admin Workflow

The `Admin` account has full access because it is granted `ADMIN`, `MANAGE`, and `STAFF` roles.

## Dashboard

Route: `/dashboard` (also `/`)

`DashboardController` uses `DashboardService` to display robot state, active rules, active strategy entities, rule execution summaries, and recent `RuleExecutionHistory` records in `dashboard.html`.

## Rule Management

Route: `/rules`

Admin can:

* create a rule;
* edit its name, condition, target strategy, active state, and numeric priority;
* enable or disable it through `/rules/{id}/toggle`;
* delete it through `/rules/{id}/delete`.

`RuleManagementController` handles the forms. `RuleService` validates the condition by parsing it with `RuleParser`, verifies that the selected persisted strategy is active, and saves `Rule` through `RuleRepository`.

Lower numeric priority values are evaluated first. Rule names are the secondary ordering key.

## Robot Management

Route: `/robots`

The page is read-only. `RobotManagementController` combines `RobotService.getRobots()` with `RobotFleetStatusService.getRobotFleetStatus()` and renders `robots.html`. It shows persisted robot identity/status plus backend-derived battery, charging, mission, movement, and current strategy information.

There is no robot create/edit/delete UI in the current codebase.

## Execution Simulator

Route: `/simulation`

`SimulationController` sends manual input to `SimulationService.simulate(...)`. The service creates an in-memory `Robot` context, calls `RuleEvaluator`, dispatches through `StrategyContext`, returns rule and leaf-condition traces, and saves `RuleExecutionHistory`.

The simulator tests decision logic; it does not create or move a warehouse mission.

## System Flow

Route: `/system-flow`

`SystemFlowController` renders `system-flow.html` with architecture steps, supported robot conditions, Interpreter components, active rules, active strategy entities, and recent execution history.

## Code map

| Feature | Controller | Service | Template | Main model/repository |
| --- | --- | --- | --- | --- |
| Dashboard | `DashboardController` | `DashboardService` | `dashboard.html` | `Robot`, `Rule`, `Strategy`, `RuleExecutionHistory` repositories |
| Rule Management | `RuleManagementController` | `RuleService` | `rules.html` | `Rule`, `RuleRepository`, `StrategyRepository` |
| Robot Management | `RobotManagementController` | `RobotService`, `RobotFleetStatusService` | `robots.html` | `Robot`, `RobotRepository` |
| Simulation | `SimulationController` | `SimulationService` | `simulation.html` | `RuleExecutionHistory`, `RuleExecutionHistoryRepository` |
| System Flow | `SystemFlowController` | `RuleService`, `DashboardService` | `system-flow.html` | Active rules, strategies, and history |

![Admin Rule Management](images/admin-rule-management.png)

Add the screenshot with this exact filename under `docs/images/`.

![Admin Robot Management](images/admin-robot-management.png)

Add the screenshot with this exact filename under `docs/images/`.

![Execution Simulator](images/execution-simulator.png)

Add the screenshot with this exact filename under `docs/images/`.
