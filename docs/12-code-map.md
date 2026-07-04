# Central Code Map

All paths below are relative to the project root. Java packages start under `src/main/java/com/warehouse`; templates and static assets start under `src/main/resources`.

## 1. Pages and templates

| Feature | Route | Template file | Controller | Notes |
| --- | --- | --- | --- | --- |
| Login | `/login` | `templates/login.html` | `AuthController` | Form POST is processed by Spring Security. |
| Access denied | `/access-denied` | `templates/access-denied.html` | `AuthController` | Returns the controlled denied view. |
| Dashboard | `/`, `/dashboard` | `templates/dashboard.html` | `DashboardController` | Admin only. |
| Rule Management | `/rules` | `templates/rules.html` | `RuleManagementController` | Edit route: `/rules/{id}/edit`. |
| Robot Management | `/robots` | `templates/robots.html` | `RobotManagementController` | Read-only fleet view. |
| Simulation | `/simulation` | `templates/simulation.html` | `SimulationController` | GET form and POST result. |
| System Flow | `/system-flow` | `templates/system-flow.html` | `SystemFlowController` | Current architecture and history. |
| Settings | `/settings` | `templates/settings.html` | `SettingsController` | Client-side language/theme/density settings. |
| Pickup Request | `/staff/pickup-request` | `templates/staff-pickup-request.html` | `StaffPickupRequestController` | GET/POST request form. |
| Staff Missions | `/staff/missions` | `templates/staff-missions.html` | `StaffPickupRequestController` | Filters and lifecycle actions. |
| Mission Detail | `/staff/missions/{id}` | `templates/staff-mission-detail.html` | `StaffPickupRequestController` | Read-only detail. |
| Live Map | `/staff/live-map` | `templates/staff-live-map.html` | `StaffLiveMapController` | Standalone fullscreen application view. |
| Live Map JSON | `/staff/live-map/state` | Not a template | `StaffLiveMapController` | Returns `LiveMapStateDto`. |
| Policy Assignment | `/manager/policy-assignment` | `templates/manager-policy-assignment.html` | `ManagerPolicyAssignmentController` | GET/POST zone policy form. |
| Robot Task Board | `/manager/robot-tasks` | `templates/manager-robot-tasks.html` | `ManagerRobotTaskBoardController` | Read-only operational board. |

## 2. Controllers

| Controller | Responsibility | Main routes |
| --- | --- | --- |
| `AuthController` | Login page redirect/display and access-denied view. | `/login`, `/access-denied` |
| `DashboardController` | Builds Admin dashboard model. | `/`, `/dashboard` |
| `RuleManagementController` | Rule form, edit, save, toggle, delete. | `/rules`, `/rules/{id}/edit`, `/rules/{id}/toggle`, `/rules/{id}/delete` |
| `RobotManagementController` | Read-only robot/fleet model. | `/robots` |
| `SimulationController` | Simulator form and execution. | `/simulation` |
| `SystemFlowController` | Pattern/system flow model. | `/system-flow` |
| `SettingsController` | Settings page. | `/settings` |
| `StaffPickupRequestController` | Pickup creation, mission list/detail, processing, start, complete, stop, soft delete. | `/staff/pickup-request`, `/staff/missions/**` |
| `StaffLiveMapController` | Initial Live Map model and JSON state. | `/staff/live-map`, `/staff/live-map/state` |
| `ManagerPolicyAssignmentController` | Zone policy display/save. | `/manager/policy-assignment` |
| `ManagerRobotTaskBoardController` | Manager workload board. | `/manager/robot-tasks` |
| `SecurityModelAdvice` | Adds username/role flags for shared templates. | Global model attributes; no route |

## 3. Services

| Service | Responsibility | Important methods |
| --- | --- | --- |
| `DashboardService` | Dashboard counts, robots, rules, strategies, and history. | `getSummary`, `getRobots`, `getActiveRules`, `getActiveStrategies`, `getRecentExecutionHistory` |
| `RuleService` | Rule CRUD validation and Interpreter orchestration. | `saveRule`, `toggleRule`, `loadActiveRules`, `evaluateRules` |
| `SimulationService` | Manual decision simulation and execution history persistence. | `simulate`, `simulateBatteryDecision` |
| `MissionService` | Request validation and mission lifecycle. | `createMission`, `startExecution`, `markReturnedToBase`, `completeMission`, `stopMission`, `deleteStoppedMission` |
| `MissionProcessingService` | Robot assignment, policy selection, rule evaluation, strategy dispatch, decision persistence. | `processPendingMission` |
| `RobotAssignmentService` | Workload-aware available robot selection. | `selectRobotForMission`, `selectRobotForMissionExcluding` |
| `ZonePolicyAssignmentService` | Validate and persist zone-to-rule assignments. | `assignPolicy`, `getActiveRules`, `getAssignmentsByZone`, `getAssignedRulesByZone` |
| `ManagerRobotTaskBoardService` | Aggregate robot workload, confirmation counts, pending, cancelled, and live state. | `getRobotTaskBoard` |
| `RobotService` | Read persisted robot list. | `getRobots` |
| `RobotFleetStatusService` | Combine robot entities with Live Map/battery runtime state. | `getRobotFleetStatus` |
| `WarehouseRouteService` | Build validated outbound/return waypoint routes. | `buildExecutionRoute`, `buildOutboundRoute`, `buildReturnRoute` |
| `MissionExecutionProgressService` | Derive route step/position from time and movement mode. | `calculateProgress` |
| `RobotExecutionBehaviorService` | Convert stored strategy and route phase into active movement behavior. | `movementPlanFor`, `behaviorFor`, `chargingBehavior` |
| `RobotBatteryDrainService` | Battery drain, warnings, movement mode, and charging recovery calculations. | `calculateEffectiveBattery`, `calculateBatteryDrainPercent`, `calculateChargingBattery` |
| `RobotMissionBatteryService` | Capture mission start battery and persist traveled-waypoint drain. | `captureExecutionStartBattery`, `calculateAndPersistBatteryForTraveledWaypoints` |
| `RobotChargingService` | Start/read/complete charging and reassign queued work. | `prepareChargingDecision`, `updateRobotAvailabilityAfterMissionReturn`, `currentBatteryStatus` |
| `LiveMapStateService` | Assemble backend-driven robot/mission DTO state and persist return progress. | `getLiveMapState` |

## 4. Entities and model types

| Entity/type | Responsibility | Important fields |
| --- | --- | --- |
| `Robot` | Persisted robot identity and operating state. | `code`, `name`, `battery`, `obstacleDetected`, `status`, charging fields, `currentStrategy` |
| `Rule` | Persisted configurable condition and target behavior. | `ruleName`, `conditionExpression`, `strategyName`, `activeStatus`, `priority` |
| `com.warehouse.model.Strategy` | Persisted strategy catalog entry. | `code`, `name`, `className`, `active`, `description` |
| `RuleExecutionHistory` | Persisted simulator audit result. | input values, matched rule, selected strategy, action, `executedAt` |
| `Mission` | Pickup request, assignment, decision, execution, lifecycle, cancellation, and soft-delete record. | request/cargo fields, status/step, robot assignment, rule/strategy output, timestamps, cancellation fields |
| `ZonePolicyAssignment` | One persisted active-rule reference per zone. | `zone`, `cargoType`, `ruleId` |
| `CargoType` | Central Small/Medium/Large zone and load mapping. | display name, warehouse zone, estimated load percent |
| `MissionStatus` | Business mission states. | `PENDING`, `ASSIGNED`, `IN_PROGRESS`, `WAITING_CONFIRMATION`, `COMPLETED`, `CANCELLED` |
| `MissionExecutionStep` | Route execution phases. | `NOT_STARTED`, `MOVING_TO_TARGET`, `PICKING_UP`, `RETURNING_TO_BASE`, `RETURNED_TO_BASE` |
| `RobotMovementMode` | Timing/drain mode metadata. | mode display name, waypoints per battery percent |
| `CancellationReason` | Allowed cancellation codes and labels. | seven current reason values |

Note: `com.warehouse.model.Strategy` is the database entity; `com.warehouse.strategy.Strategy` is the Strategy Pattern interface.

## 5. Repositories

| Repository | Main responsibility |
| --- | --- |
| `RobotRepository` | Ordered robot reads with `currentStrategy` entity graph. |
| `RuleRepository` | Active/all rule ordering, named seed lookup, active counts. |
| `StrategyRepository` | Active strategy catalog and code lookup. |
| `MissionRepository` | Visible mission reads, status lists, assigned workload counts, queued reassignment lists. |
| `ZonePolicyAssignmentRepository` | One assignment per zone and ordered assignment reads. |
| `RuleExecutionHistoryRepository` | Ten newest simulation history records. |

## 6. Pattern classes

| Pattern | Classes | Purpose |
| --- | --- | --- |
| Interpreter | `Expression`, leaf expressions, `AndExpression`, `OrExpression`, `RuleParser`, `ExpressionEvaluation`, `RuleEvaluationResult`, `RuleEngineResult`, `RuleEvaluator`, `RuleService` | Parse supported rule text, evaluate robot context, preserve trace, choose first match. |
| Strategy | `Strategy`, six concrete strategy components, `StrategyContext`, `StrategyResult` | Dispatch selected strategy by name and return action output. |
| Runtime strategy interpretation | `RobotExecutionBehaviorService`, `RobotMovementMode` | Apply primary strategy to mission movement and temporary phase behavior. |

No `DefaultStrategy` or `RuleEngine` class was found in the current codebase. Fallback output is `NoStrategy`; engine orchestration lives in `RuleService`/`RuleEvaluator`.

## 7. JavaScript and CSS

| File | Purpose | Used by |
| --- | --- | --- |
| `static/js/staff-live-map.js` | Poll state, bind backend DTO fields, render warehouse, markers, selected/show-all modes, and preview. | `staff-live-map.html` |
| `static/js/app-settings.js` | Language translation, theme, and density stored in browser storage. | Application pages |
| `static/js/app-notifications.js` | Recent notifications, toasts, and Live Map event deduplication. | Shared pages and Live Map |
| `static/css/app.css` | Shared application layout, navigation, forms, cards, tables, badges, responsive Staff layout. | Most Thymeleaf templates |
| `static/css/staff-live-map.css` | Fullscreen warehouse map, zone, route, marker, station, and mobile styling. | `staff-live-map.html` |

## 8. Configuration

| File/class | Purpose |
| --- | --- |
| `SecurityConfig` | Role access, demo users, login redirects, logout. |
| `DataSeeder` | Idempotent strategy/rule seed refresh and initial three robots. |
| `DatabaseCompatibilityConfig` | SQL Server compatibility handling for mission enum/check constraints. |
| `application.properties` | Default SQL Server runtime and port. |
| `application-demo.properties` | In-memory H2 classroom/demo profile. |
| `src/test/resources/application.properties` | In-memory H2 test configuration. |

![System Flow](images/system-flow.png)

Add the screenshot with this exact filename under `docs/images/`.
