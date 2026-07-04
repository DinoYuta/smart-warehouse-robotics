# PROJECT STATUS

## Project Name

Smart Warehouse Robotics Decision Engine using Interpreter and Strategy Patterns

---

# CURRENT DEVELOPMENT STAGE

Project Audit - Truthful Feature Status Added

June 23, 2026 audit-only pass:

* Scope: source-code and documentation audit only.
* No application feature, route, service, database model, template, or static
  frontend behavior was changed in this pass.
* Only `PROJECT_STATUS.md` was updated to record truthful implementation
  status.
* Verification after the audit:
  * `mvn test` was launched twice, but the shell tool timed out before Maven
    returned a final exit summary.
  * Surefire reports were produced for all 20 test classes and show 150 tests
    run, 0 failures, 0 errors, and 0 skipped.
  * No Maven/Surefire Java test process remained after the timeout; only the
    already-running Spring Boot app process was still active.
  * `mvn -DskipTests compile` initially failed in the sandbox because Maven
    network access to Maven Central was blocked. After approval for the same
    command, it completed with `BUILD SUCCESS`.

## Project Audit - Truthful Feature Status

### 1. Fully Implemented Features

* Database-backed Spring Security login is implemented through `app_users`,
  `CustomUserDetailsService`, and BCrypt password hashes.
* The actual role hierarchy is implemented as `ADMIN > MANAGE > STAFF`.
  `ADMIN` receives Admin, Manager, and Staff authorities; `MANAGE` receives
  Manager and Staff authorities; `STAFF` receives Staff authority only.
* Route protection is implemented in `SecurityConfig`: `/admin/**` is Admin
  only, `/manager/**` is Manager/Admin, and `/staff/**` is Staff/Manager/Admin.
* Admin User Management is implemented at `/admin/users`, including BCrypt
  password update at `/admin/users/{id}/change-password` without displaying
  raw passwords or password hashes.
* Seed users are implemented by `DataSeeder`: `Admin/admin`, `Manage/manage`,
  and `Nova001/nova001`.
* Rule Management is implemented for Admin-created rules, including parser
  validation, active/inactive state, priority, target strategy, edit, delete,
  and execution-history integration.
* The Interpreter Pattern is implemented and connected to working flows.
  `RuleParser`, `RuleEvaluator`, `BatteryExpression`, `ObstacleExpression`,
  `RobotLoadExpression`, `DistanceExpression`, `PriorityExpression`,
  `AndExpression`, and `OrExpression` are used by simulation, dashboard
  evaluation, rule validation, and mission processing.
* Supported comparison operators in code are `<`, `>`, `<=`, `>=`, and `==`.
  Logical `AND` and `OR` are supported by `RuleParser`.
* The Strategy Pattern is implemented through Spring strategy beans and
  `StrategyContext`. Existing strategy classes are `ChargingStrategy`,
  `EnergySavingStrategy`, `FastRouteStrategy`, `HeavyLoadStrategy`,
  `ObstacleAvoidanceStrategy`, and `SafeRouteStrategy`.
* Mission processing is connected to the Interpreter and Strategy patterns.
  `MissionProcessingService` selects a robot, evaluates active rules through
  `RuleEvaluator`, applies zone policy override when configured, and dispatches
  the selected strategy through `StrategyContext`.
* Staff manual pickup request flow is implemented at `/staff/pickup-request`.
  It creates database-backed `PENDING` missions with cargo type, mapped zone,
  location, priority, customer/request data, and notes.
* Manager Customer Pickup Code flow is implemented at
  `/manager/customer-pickup-codes`, including customer validation, unique
  uppercase code generation, demo email preview, Staff lookup/process, High
  priority mission creation, and used-code protection.
* Staff Missions flow is implemented at `/staff/missions`, including mission
  processing, assignment details, start execution, complete after return, stop,
  and delete-after-cancel behavior.
* Mission lifecycle states implemented in code are `PENDING`, `ASSIGNED`,
  `IN_PROGRESS`, `WAITING_CONFIRMATION`, `COMPLETED`, and `CANCELLED`.
* Stop behavior is implemented by converting stoppable missions to
  `CANCELLED` with cancellation reason, note, user, and timestamp. Soft delete
  is allowed only after cancellation.
* Cargo-zone-location mapping is implemented: Small Cargo -> Zone A / A1-A9,
  Medium Cargo -> Zone B / B1-B9, and Large Cargo -> Zone C / C1-C9.
* Seed robots are implemented: Picker Alpha, Mover Beta, and Carrier Gamma.
* Robot colors are implemented in Live Map state/CSS: Picker Alpha green,
  Mover Beta red, and Carrier Gamma blue.
* Robot Management page exists and displays robot status, battery/charging
  state, availability-related status labels, and current strategy display.
* Manager Rule / Policy Assignment is implemented for Zone A, Zone B, and
  Zone C using existing Admin-created active rules.
* Manager Robot Task Board is implemented and groups mission workload by
  assigned robot, including active workload, high-priority active count,
  waiting confirmation count, unassigned pending missions, and cancelled
  missions.
* Live Map backend state is implemented at `/staff/live-map/state`. It exposes
  robot mission state, route progress, battery, current behavior, mission flow
  step, and bridge waiting state.
* Live Map frontend is implemented with A/B/C zones, A1-A9/B1-B9/C1-C9 slot
  layout, Base Station, Charging Station, route network, robot filtering by
  robot click, Show All Robots, and polling refresh through JavaScript.
* Live Map route generation is implemented by `WarehouseRouteService` with
  outbound and return lanes, Base Station, zone transitions, bridges between
  C/B/A, and waypoint sequences for mission locations.
* Mission Flow tree is implemented in the Live Map UI with assignment/start,
  move, pickup, return, and returned/confirmation states.
* Battery drain by waypoint count is implemented: Fast mode drains 1% per 3
  waypoints, Normal/Safe/Heavy/Obstacle modes drain 1% per 5 waypoints, and
  Energy Saving mode drains 1% per 7 waypoints.
* Charging workflow is implemented with a critical threshold at `<= 5%`,
  charging recovery of about 5% per 10 seconds, persistent charging state, and
  queued mission reassignment when a robot starts charging.
* Source review did not find the earlier battery reset bug in the backend
  drain/persistence path. `RobotMissionBatteryService` persists only lower
  effective battery during route polling and charging completion sets battery
  to 100 intentionally.

### 2. Partially Implemented Features

* Robot Management is display-focused. It shows robot state, battery, charging,
  and current strategy, but no robot CRUD or manual availability/status control
  UI was found.
* Manager operational control is partial. Managers can assign zone policies and
  review workload, but they do not directly decide robot availability from the
  Manager UI.
* Mission queue per robot is partial. Workload is counted and displayed by
  robot, and assignment prefers robots with fewer active/high-priority
  missions, but there is no explicit queue runner that automatically processes
  each robot's queued missions one by one.
* Robot assignment reduces stacking of High priority missions by sorting on
  high-priority active mission count first, but it does not absolutely forbid
  multiple High priority missions on the same robot.
* Live Map route logic is mission-zone/location based. Routes vary by cargo
  zone and target location, but there is no separate robot-specific routing
  algorithm beyond each robot's current assigned mission and route progress.
* "No diagonal movement" is visually implied by the waypoint network and
  horizontal/vertical lane coordinates, but no standalone validator or rule was
  found that formally rejects diagonal route segments.
* Obstacle behavior is partial. Obstacle expressions and
  `ObstacleAvoidanceStrategy` exist, and bridge contention can temporarily show
  obstacle-avoidance behavior with waiting. No true alternative-lane reroute
  implementation was found.
* Temporary strategy override exists for bridge waiting behavior in
  `RobotExecutionBehaviorService`, which can display ObstacleAvoidance and a
  resume message. This is not a general strategy stack/history mechanism.
* Heavy load behavior is partial. Large Cargo maps to high robot load and can
  match `HeavyLoadStrategy`; return movement for Large Cargo uses heavy-load
  movement mode. No distinct heavy-load route geometry or advanced safety model
  was found.
* High priority behavior is partial. High priority can select
  `FastRouteStrategy` through the active rule `priority == 1`, and Fast mode
  drains battery faster. Higher-priority battery/obstacle/heavy-load rules or
  zone policy assignment can override that selection.
* ChargingStrategy priority is rule-driven, not hardcoded globally. Seeded
  critical battery rules have higher rule priority, but the code does not force
  ChargingStrategy to win independently of configured rules.
* Customer pickup code missions link `missionId` back to the pickup code.
  Mission stores customer name directly, while customer email/phone are stored
  in mission notes rather than separate structured mission fields.
* `WAITING_CONFIRMATION` missions are counted separately on the Manager Task
  Board, but `RobotAssignmentService` excludes them from active workload
  counting. This may be intentional after the robot returns to Base, but it is
  a behavior to keep explicit in the demo.

### 3. Planned But Not Implemented Features

* No separate `MANAGER` database role or Spring authority exists. The code uses
  `MANAGE` as the actual role value and "Manager" as the user-facing label.
* Strategy classes named `DistanceStrategy`, `PriorityStrategy`, and
  `DefaultStrategy` were not found.
* A standalone `ConditionExpression` class from the roadmap was not found; the
  implementation uses condition-specific expression classes instead.
* Parenthesized or deeply nested rule expressions were not found. `AND`/`OR`
  composition is implemented, but parentheses are not parsed.
* A separate `STOPPED` mission status was not found. Stop is represented as
  `CANCELLED`.
* A separate Mission Monitor page distinct from Staff Missions and Manager
  Robot Task Board was not found.
* Real Gmail, SMTP, app passwords, external email API integration, and real
  email sending are not implemented. The current customer email flow is preview
  only.
* `user_activity_log` audit persistence was not found, so password-change audit
  logging is not implemented.
* Alternative-lane obstacle routing and dynamic reroute around blocked aisles
  were not found.
* Real robot/hardware execution, WebSocket push, and autonomous robot control
  were not found. The Live Map is a backend-polling visualization/simulation
  layer.
* Registration, forgot password, email reset, OTP, JWT, OAuth, and Google Login
  are intentionally not implemented.

### 4. Features That Need Verification

* SQL Server runtime behavior needs verification against the local
  `warehouse_db` instance because automated tests use H2.
* Browser rendering of Live Map layout, route animation, station labels, and
  responsive behavior needs visual verification in a running app.
* Battery persistence across a real browser refresh should be demo-tested even
  though the reviewed backend source no longer shows a battery reset path.
* Vietnamese text rendering should be checked in the browser. Most translation
  keys exist in `app-settings.js`, but rendered encoding should be verified on
  the running page.
* Admin password change should be demo-tested against the local SQL Server data
  if existing database rows predate the current seed data.
* Staff/Manager/Admin route access is covered by tests for key pages, but a
  full manual route sweep in the running app is still useful before the demo.

### 5. Inconsistencies Found

* Role naming is inconsistent in project language: source code and database
  seed use `MANAGE`, while documentation and UI labels often say "Manager".
  This is acceptable as a label, but the actual role is not `MANAGER`.
* `PROJECT_ROADMAP.md` Phase 9 is outdated. It still marks mission processing
  connection, mission status/history, and Live Warehouse Map UI as incomplete,
  even though source code implements those flows.
* Roadmap/design language says Manager can decide robot availability, but the
  current Manager pages do not provide robot availability controls.
* Documentation language sometimes implies complete runtime strategy switching.
  In source, strategy selection is rule/policy driven during mission
  processing, and temporary obstacle overlay exists for bridge waiting only.
* AGENTS.md lists operator text as `> =`; the actual parser supports `>=`.
* Stop/delete language is mixed. The UI uses stopped/cancelled wording, while
  the persisted status is `CANCELLED`; no `STOPPED` status exists.
* Mission entity has `pickupReachedAt`, but the reviewed progress code did not
  show that timestamp being persisted during normal Live Map progress.
* Live Map has both backend-following execution state and a "visual preview"
  button that does not change backend state. Demo scripts should distinguish
  those two behaviors.

### 6. Important Files Reviewed

* `AGENTS.md`, `DESIGN.md`, `PROJECT_ROADMAP.md`, `PROJECT_STATUS.md`, and
  `README.md`: project architecture, intended workflow, roadmap, and existing
  status claims.
* `pom.xml`, `src/main/resources/application.properties`, and
  `src/main/resources/application-demo.properties`: dependencies, Java/Spring
  versions, SQL Server runtime config, and H2 demo config.
* `SecurityConfig.java`, `CustomUserDetailsService.java`,
  `SecurityModelAdvice.java`, `AuthController.java`, and `DataSeeder.java`:
  login, BCrypt, seed users, route authorization, role hierarchy, and sidebar
  model flags.
* Controllers reviewed: Admin user management, dashboard, manager pickup
  codes, manager policy assignment, manager robot task board, robot management,
  rule management, settings, simulation, Staff Live Map, Staff pickup/missions,
  and system flow.
* Services reviewed: admin users, customer pickup codes, dashboard, Live Map
  state, manager task board, mission execution progress, mission processing,
  mission lifecycle, robot assignment, battery drain, charging, execution
  behavior, fleet status, robot service, rule service, warehouse routes, and
  zone policy assignment.
* Models/entities reviewed: users, robots, rules, strategies, execution
  history, missions, mission status/steps, cancellation reasons, cargo types,
  movement modes, customer pickup codes/status, and zone policy assignment.
* Repositories reviewed: app users, customer pickup codes, missions, robots,
  rules, rule execution history, strategies, and zone policy assignments.
* Interpreter classes reviewed: parser, evaluator, expression interface,
  condition expressions, logical expressions, comparison operators, and result
  objects.
* Strategy classes reviewed: charging, energy saving, fast route, heavy load,
  obstacle avoidance, safe route, context, interfaces, and result object.
* Frontend files reviewed: sidebar/topbar-related navigation, login,
  access-denied, Admin user pages, robot management, Staff pickup request,
  Staff missions/detail, Manager task board, Manager pickup codes, Live Map
  template, Live Map JavaScript, Live Map CSS, and app translation settings.
* Test files reviewed by inventory: controller, security, interpreter,
  service, simulator, strategy, repository, and model test classes.

### 7. Recommended Next Tasks

1. Update `PROJECT_ROADMAP.md` to mark completed Phase 9 items accurately and
   clarify that the actual database role is `MANAGE`.
2. Run a browser demo pass against SQL Server for Admin, Manager, Staff, Live
   Map, battery refresh, and pickup-code mission execution.
3. Decide whether to keep stop as `CANCELLED` or add a true `STOPPED` status in
   a future task.
4. Decide whether Manager robot availability controls are required for the
   graduation demo; if yes, implement them as a scoped feature later.
5. Decide whether obstacle avoidance should remain bridge-waiting only or be
   expanded into real alternative-lane routing in a future task.
6. Add or update documentation to explain the difference between Live Map
   backend execution mode and visual preview mode.

---

Admin User Management and Change Password Added

June 23, 2026 admin password management pass:

Completed in this security-focused pass:

* Required project context files were reviewed before code changes:
  * `AGENTS.md`
  * `DESIGN.md`
  * `PROJECT_ROADMAP.md`
  * `PROJECT_STATUS.md`
  * `README.md`
* Added Admin-only User Management page at `/admin/users`:
  * Shows username, role, enabled status, created time, updated time, and
    actions.
  * Does not show raw passwords.
  * Does not show stored password hashes.
* Added Admin-only Change Password page at
  `/admin/users/{id}/change-password`:
  * New Password field.
  * Confirm Password field.
  * Save and Cancel actions.
* Added password validation:
  * Password is required.
  * Minimum length is 8 characters.
  * Maximum length is 100 characters.
  * Confirm Password must match.
  * English and Vietnamese validation translations were added.
* Implemented secure BCrypt password update:
  * Reuses existing `app_users` table.
  * Reuses existing `AppUser` entity and repository.
  * Reuses the existing Spring `PasswordEncoder` bean.
  * Saves `BCryptPasswordEncoder.encode(newPassword)` into `password_hash`.
  * Does not store plain text passwords.
  * Does not compare password values with `equals()`.
* No existing `user_activity_log` table, entity, repository, or activity-log
  service was present, so no audit-log schema or separate audit flow was added.
* Added Admin-only access control:
  * `ADMIN` can open `/admin/users`.
  * `MANAGE` is denied.
  * `STAFF` is denied.
* Added sidebar navigation:
  * English: `User Management`
  * Vietnamese UI translation added in `app-settings.js`.
* Updated `README.md` with a `User Management` section.

Files changed:

* `README.md`
* `PROJECT_STATUS.md`
* `src/main/java/com/warehouse/config/SecurityConfig.java`
* `src/main/java/com/warehouse/controller/AdminUserManagementController.java`
* `src/main/java/com/warehouse/dto/ChangePasswordFormDto.java`
* `src/main/java/com/warehouse/model/AppUser.java`
* `src/main/java/com/warehouse/repository/AppUserRepository.java`
* `src/main/java/com/warehouse/service/AdminUserService.java`
* `src/main/resources/static/js/app-settings.js`
* `src/main/resources/templates/admin-change-password.html`
* `src/main/resources/templates/admin-users.html`
* `src/main/resources/templates/fragments/sidebar.html`
* `src/test/java/com/warehouse/controller/AdminUserManagementControllerTest.java`
* `src/test/java/com/warehouse/controller/RoleNavigationControllerTest.java`

Verification result:

* JavaScript syntax check passed:
  * `node --check src/main/resources/static/js/app-settings.js`
* Admin password templates do not reference hash field names or raw-password
  wording:
  * `rg -n "password_hash|passwordHash|raw password" src/main/resources/templates/admin-users.html src/main/resources/templates/admin-change-password.html src/main/resources/templates/fragments/sidebar.html`
  * 0 matches.
* Focused admin user management, authentication, and role-navigation regression
  passed:
  * `mvn test "-Dtest=AdminUserManagementControllerTest,DatabaseBackedAuthenticationTest,RoleNavigationControllerTest"`
  * 22 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.
* Full automated regression passed:
  * `mvn test`
  * 150 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.
* Verified by automated tests:
  * `ADMIN` can open `/admin/users`.
  * `MANAGE` and `STAFF` cannot open `/admin/users`.
  * Admin can change `Nova001`, `Manage`, and `Admin` passwords.
  * The database password hash changes after password update.
  * The stored value remains a BCrypt hash.
  * New password login works.
  * Old password login fails.
  * Password hash text is not displayed in the User Management UI.
  * Existing authentication still works.
  * Existing role protection still works.
  * Existing Staff workflow route still works.
  * Existing Live Map state route still works.
  * Vietnamese translation keys exist.

Scope confirmation:

* No registration, forgot password, email reset, OTP, JWT, OAuth, Google Login,
  or external authentication provider was added.
* Existing SQL Server-backed authentication was not replaced.
* Existing BCrypt authentication was not broken.
* Session login behavior was not changed.
* Role hierarchy was not changed.
* Interpreter Pattern logic was not changed.
* Strategy Pattern logic was not changed.
* Mission lifecycle logic was not changed.
* Live Map route logic was not changed.
* Battery drain, charging, and reassignment logic was not changed.
* Rule evaluation logic was not changed.

---

Customer Pickup Code Flow Added

June 23, 2026 customer pickup code workflow pass:

Completed in this workflow-focused pass:

* Required project context files were reviewed before code changes:
  * `AGENTS.md`
  * `DESIGN.md`
  * `PROJECT_ROADMAP.md`
  * `PROJECT_STATUS.md`
  * `README.md`
* Added Manager-created customer pickup code flow:
  * New `customer_pickup_codes` persistence model.
  * Unique uppercase human-friendly codes such as `CUS-2026-A91K`.
  * Status values `UNUSED` and `USED`.
  * Linked `missionId`, `createdBy`, `createdAt`, `usedBy`, and `usedAt`
    tracking.
* Added Manager/Admin page at `/manager/customer-pickup-codes`:
  * List recent pickup codes.
  * Create customer pickup codes.
  * Show customer name, email, phone, cargo type, pickup location, status,
    created time, used time, linked mission, and email preview.
* Added demo email preview only:
  * English preview subject/body.
  * Vietnamese preview subject/body.
  * No Gmail SMTP, app password, external email API, secret, or real email send
    was added.
* Added customer validation:
  * Customer name required, trimmed, and length-limited.
  * Customer email required, length-limited, and format-validated.
  * Customer phone required and normalized for safe Vietnamese-style 10-11 digit
    validation.
  * Cargo type must be one of existing cargo types.
  * Pickup location must match the existing cargo-zone mapping:
    Small Cargo -> A1-A9, Medium Cargo -> B1-B9, Large Cargo -> C1-C9.
* Added Staff customer pickup code lookup/process flow on
  `/staff/pickup-request`:
  * Staff enters a pickup code.
  * Lookup is case-insensitive after trimming.
  * UNUSED codes show customer name, email, phone, cargo type, pickup location,
    and `Priority: High`.
  * Processing creates a High priority mission from the code data.
  * The created mission is processed through the existing
    `MissionProcessingService`, which reuses the existing `RuleEvaluator` and
    `StrategyContext`.
  * Staff continues with the existing Staff Missions, Start Execution, and Live
    Map workflow.
* Added used-code protection:
  * Processing marks the pickup code `USED`.
  * `usedBy` and `usedAt` are stored.
  * USED codes cannot be processed again.
  * USED codes cannot create duplicate missions.
  * USED code message:
    `This pickup code has already been used.`
  * Vietnamese translation:
    `Mã nhận hàng này đã được sử dụng.`
  * Invalid code message:
    `Pickup code not found.`
  * Vietnamese translation:
    `Không tìm thấy mã nhận hàng.`
* Added Manager navigation link:
  * `Customer Pickup Codes`
  * Vietnamese translation: `Mã nhận hàng khách hàng`
  * Admin sees the link through existing Admin full access.
  * Staff does not see or access the Manager page.
* Added English/Vietnamese UI labels and validation translations for the pickup
  code flow.
* Updated `README.md` with a `Customer Pickup Code Flow` section.

Files changed:

* `README.md`
* `PROJECT_STATUS.md`
* `src/main/java/com/warehouse/controller/ManagerCustomerPickupCodeController.java`
* `src/main/java/com/warehouse/controller/StaffPickupRequestController.java`
* `src/main/java/com/warehouse/dto/CustomerPickupCodeFormDto.java`
* `src/main/java/com/warehouse/dto/CustomerPickupCodeLookupDto.java`
* `src/main/java/com/warehouse/dto/CustomerPickupCodeProcessResult.java`
* `src/main/java/com/warehouse/model/CustomerPickupCode.java`
* `src/main/java/com/warehouse/model/CustomerPickupCodeStatus.java`
* `src/main/java/com/warehouse/repository/CustomerPickupCodeRepository.java`
* `src/main/java/com/warehouse/service/CustomerPickupCodeService.java`
* `src/main/resources/static/js/app-settings.js`
* `src/main/resources/templates/fragments/sidebar.html`
* `src/main/resources/templates/manager-customer-pickup-codes.html`
* `src/main/resources/templates/staff-pickup-request.html`
* `src/test/java/com/warehouse/controller/CustomerPickupCodeFlowTest.java`
* `src/test/java/com/warehouse/controller/RoleNavigationControllerTest.java`

Verification result:

* JavaScript syntax check passed:
  * `node --check src/main/resources/static/js/app-settings.js`
* Focused pickup-code and role-access regression passed:
  * `mvn test "-Dtest=CustomerPickupCodeFlowTest,RoleNavigationControllerTest"`
  * 22 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.
* Full automated regression passed:
  * `mvn test`
  * 142 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.

Scope confirmation:

* No real Gmail, SMTP, external email API, app password, or secret was added.
* No separate robot execution flow was added.
* Manual Staff Pickup Request remains working.
* Existing mission Process / Start Execution flow remains working.
* Pickup-code missions join the existing mission pipeline.
* `RuleEvaluator` executable logic was not changed.
* `StrategyContext` executable logic was not changed.
* Interpreter Pattern classes were not changed.
* Strategy Pattern classes were not changed.
* Live Map route logic was not changed.
* Battery drain, charging, and reassignment logic was not changed.
* Role hierarchy and Spring Security route rules were not changed.

---

SQL Server-Backed Authentication Completed

June 23, 2026 authentication persistence pass:

Completed in this security-focused pass:

* Required project context files were reviewed before code changes:
  * `AGENTS.md`
  * `DESIGN.md`
  * `PROJECT_ROADMAP.md`
  * `PROJECT_STATUS.md`
  * `README.md`
* Replaced hard-coded in-memory demo users with database-backed Spring Security
  authentication.
* Added SQL Server-compatible `app_users` persistence:
  * `AppUser` entity mapped to `app_users`
  * `AppUserRepository`
  * `findByUsernameIgnoreCase(String username)`
* Added `CustomUserDetailsService` to load users from SQL Server/H2 through
  JPA instead of hard-coded username/password checks.
* Preserved session-based login through `/login`, logout through `/logout`,
  and the existing role redirects:
  * `ADMIN` -> `/dashboard`
  * `MANAGE` -> `/manager/robot-tasks`
  * `STAFF` -> `/staff/pickup-request`
* Preserved the existing role hierarchy by mapping database roles to Spring
  Security authorities:
  * `ADMIN` receives `ROLE_ADMIN`, `ROLE_MANAGE`, and `ROLE_STAFF`
  * `MANAGE` receives `ROLE_MANAGE` and `ROLE_STAFF`
  * `STAFF` receives `ROLE_STAFF`
* Added idempotent seeded demo accounts in `DataSeeder`:
  * `Admin / admin / ADMIN`
  * `Manage / manage / MANAGE`
  * `Nova001 / nova001 / STAFF`
* Seeded passwords are stored with `BCryptPasswordEncoder` hashes in
  `password_hash`; existing users are not overwritten on startup.
* Removed/replaced active in-memory demo users from `SecurityConfig`.
* Kept the public login screen limited to the Staff demo credential
  `Nova001 / nova001`; Admin and Manager credentials are not shown publicly.
* Kept the generic login failure message: `Invalid username or password`.
* Updated `README.md` with Authentication & Authorization documentation.

Files changed:

* `README.md`
* `PROJECT_STATUS.md`
* `src/main/java/com/warehouse/config/DataSeeder.java`
* `src/main/java/com/warehouse/config/SecurityConfig.java`
* `src/main/java/com/warehouse/model/AppUser.java`
* `src/main/java/com/warehouse/repository/AppUserRepository.java`
* `src/main/java/com/warehouse/service/CustomUserDetailsService.java`
* `src/main/resources/templates/login.html`
* `src/test/java/com/warehouse/security/DatabaseBackedAuthenticationTest.java`

Verification result:

* Focused authentication and role-access regression passed:
  * `mvn test "-Dtest=DatabaseBackedAuthenticationTest,RoleNavigationControllerTest"`
  * 14 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.
* Full automated regression passed:
  * `mvn test`
  * 130 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.
* SQL Server runtime smoke passed on port `8096` using the default SQL Server
  configuration:
  * `/login` returned HTTP 200.
  * Login page showed `Nova001 / nova001`.
  * Login page did not show `Admin / admin`.
  * Login page did not show `Manage / manage`.
  * Direct `sqlcmd` query confirmed `app_users` contains `Admin`, `Manage`,
    and `Nova001` with roles `ADMIN`, `MANAGE`, and `STAFF`.
  * Direct `sqlcmd` query confirmed stored password hash prefixes are BCrypt
    prefixes (`$2a$`) and not plain text passwords.
  * `Admin / admin` redirected to `/dashboard`.
  * `Manage / manage` redirected to `/manager/robot-tasks`.
  * `Nova001 / nova001` redirected to `/staff/pickup-request`.
  * Staff access to `/rules` returned HTTP 403.
  * Manage access to `/robots` returned HTTP 403.
  * Admin access to `/rules` returned HTTP 200.
  * Staff access to `/staff/live-map/state` returned HTTP 200 with JSON.
  * Temporary verification app process was stopped.

Scope confirmation:

* Authentication now comes from SQL Server/H2-backed `app_users`, not active
  hard-coded in-memory users.
* No plain text password is stored in the database.
* No raw password equality comparison was added.
* No JWT, OAuth, Google Login, LDAP, Keycloak, registration, password reset, or
  production-grade security expansion was added.
* Interpreter Pattern logic was not changed.
* Strategy Pattern logic was not changed.
* `RuleParser`, `RuleEvaluator`, `RuleEngine`, `StrategyContext`, and strategy
  classes were not changed.
* Mission assignment logic and mission lifecycle logic were not changed.
* Live Map route logic and Live Map polling were not changed.
* Battery drain, charging, and reassignment logic were not changed.
* Staff mobile UI behavior was not changed.

---

Complete Project Documentation Package Created

June 19, 2026 documentation pass:

Completed in this documentation-only pass:

* Reviewed `AGENTS.md`, `DESIGN.md`, `PROJECT_ROADMAP.md`, `PROJECT_STATUS.md`,
  and `README.md` before making documentation changes.
* Inspected the current controllers, services, entities, repositories,
  configuration, templates, JavaScript, CSS, Interpreter classes, Strategy
  classes, and tests so the documentation uses actual names and behavior.
* Added a 13-part reviewer and maintainer guide covering project scope, role
  navigation, Staff/Manager/Admin workflows, mission lifecycle, both design
  patterns, Live Map polling, battery/charging/reassignment, cancellation
  audit, full demo testing, and a central code map.
* Added Markdown diagrams, route/code mapping tables, lifecycle tables, test
  expectations, limitations, and source-verified behavior notes.
* Added manual screenshot placeholders throughout the guide.
* Added `docs/images/README.md` with the required exact filenames:
  * `login-staff-mobile.png`
  * `staff-pickup-request.png`
  * `staff-missions.png`
  * `staff-live-map-show-all.png`
  * `staff-live-map-selected-robot.png`
  * `manager-policy-assignment.png`
  * `manager-robot-task-board.png`
  * `admin-rule-management.png`
  * `admin-robot-management.png`
  * `execution-simulator.png`
  * `system-flow.png`
* Updated the root `README.md` with concise links to the canonical guides.
* Retained old documentation filenames as compatibility links and marked the
  numbered guide as authoritative, preventing stale demo/checklist guidance
  from conflicting with current behavior.

Documentation files added:

* `docs/README.md`
* `docs/00-project-overview.md`
* `docs/01-user-roles-and-navigation.md`
* `docs/02-staff-workflow.md`
* `docs/03-manager-workflow.md`
* `docs/04-admin-workflow.md`
* `docs/05-mission-lifecycle.md`
* `docs/06-rule-engine-interpreter-pattern.md`
* `docs/07-strategy-pattern.md`
* `docs/08-live-map-simulation.md`
* `docs/09-battery-charging-and-reassignment.md`
* `docs/10-cancellation-and-audit.md`
* `docs/11-demo-test-guide.md`
* `docs/12-code-map.md`
* `docs/images/README.md`

Documentation files updated:

* `README.md`
* `docs/FINAL_DEMO_SCRIPT.md`
* `docs/SCREENSHOT_CHECKLIST.md`
* `docs/UML_DIAGRAMS.md`
* `PROJECT_STATUS.md`

Verification:

* Markdown title/code-fence checks passed.
* All local Markdown document links resolve.
* All 11 requested screenshot filenames are present in the image guide.
* `mvn test` passed: 126 tests, 0 failures, 0 errors, 0 skipped; Maven
  reported `BUILD SUCCESS`.
* No Java source, Thymeleaf template, CSS, JavaScript, application
  configuration, route mapping, business logic, UI behavior, role permission,
  mission lifecycle, database schema, or application feature was changed.

---

Safe Code Cleanup and Formatting Completed

June 19, 2026 cleanup and readability refinement:

Completed in this cleanup pass:

* Required project context files were reviewed before code changes:
  * `AGENTS.md`
  * `DESIGN.md`
  * `PROJECT_ROADMAP.md`
  * `PROJECT_STATUS.md`
  * `README.md`
* Java controllers, services, DTOs, entities, repositories, configuration, Interpreter classes, and Strategy classes were inspected for cleanup-only opportunities.
* Thymeleaf templates, CSS, and JavaScript were inspected for indentation, stale comments, long lines, duplicated comments, and behavior-sensitive bindings.
* Java formatting was made consistent by wrapping long method calls, constructor calls, conditions, signatures, and user-facing message construction.
* Import ordering was corrected where it was inconsistent.
* Live Map JavaScript expressions and calls were wrapped for readability while preserving endpoint URLs, polling, route arrays, state binding, and notification behavior.
* One unused private helper was removed from `RobotChargingService`.
* Comment cleanup completed:
  * Replaced the outdated Live Map CSS restoration comment with a short section label.
  * Replaced the verbose demo-oriented CSRF comment with a concise explanation of the existing form constraint.
  * No duplicated AI-style, debug, stale TODO, or syntax-explaining comment blocks remained after inspection.
* Short business comments were added for:
  * Interpreter rule parsing into expressions evaluated against robot context.
  * Strategy dispatch after a rule match.
  * `robotLoad` cargo mapping: Small=30, Medium=60, Large=90.
  * Returned-to-Base robot availability versus later Staff confirmation.
  * Required cancellation reason storage for Manager review.
  * Backend-driven Live Map DTO rendering.
  * Queued mission reassignment only when a robot enters charging.
* Thymeleaf route links, form actions, field names, data attributes, and i18n attributes were left unchanged.
* CSS visual rules and responsive behavior were left unchanged, including Staff mobile layout and hidden yellow cargo approach guides.

Files changed:

* `PROJECT_STATUS.md`
* `src/main/java/com/warehouse/config/SecurityConfig.java`
* `src/main/java/com/warehouse/controller/AuthController.java`
* `src/main/java/com/warehouse/controller/StaffLiveMapController.java`
* `src/main/java/com/warehouse/controller/StaffPickupRequestController.java`
* `src/main/java/com/warehouse/dto/MissionExecutionProgressDto.java`
* `src/main/java/com/warehouse/dto/RobotFleetStatusDto.java`
* `src/main/java/com/warehouse/dto/RobotTaskGroupDto.java`
* `src/main/java/com/warehouse/interpreter/RuleEvaluationResult.java`
* `src/main/java/com/warehouse/interpreter/RuleParser.java`
* `src/main/java/com/warehouse/model/CargoType.java`
* `src/main/java/com/warehouse/service/DashboardService.java`
* `src/main/java/com/warehouse/service/LiveMapStateService.java`
* `src/main/java/com/warehouse/service/MissionExecutionProgressService.java`
* `src/main/java/com/warehouse/service/MissionProcessingService.java`
* `src/main/java/com/warehouse/service/MissionService.java`
* `src/main/java/com/warehouse/service/RobotAssignmentService.java`
* `src/main/java/com/warehouse/service/RobotBatteryDrainService.java`
* `src/main/java/com/warehouse/service/RobotChargingService.java`
* `src/main/java/com/warehouse/service/RobotExecutionBehaviorService.java`
* `src/main/java/com/warehouse/service/RobotFleetStatusService.java`
* `src/main/java/com/warehouse/simulator/SimulationService.java`
* `src/main/java/com/warehouse/strategy/HeavyLoadStrategy.java`
* `src/main/java/com/warehouse/strategy/ObstacleAvoidanceStrategy.java`
* `src/main/java/com/warehouse/strategy/StrategyContext.java`
* `src/main/resources/static/css/staff-live-map.css`
* `src/main/resources/static/js/staff-live-map.js`

Verification result:

* Final `mvn test` passed after cleanup:
  * 126 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.
* Focused Live Map regression passed:
  * `mvn test "-Dtest=StaffLiveMapControllerTest"`
  * 22 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.
* Final package build passed:
  * `mvn clean package`
  * 126 tests passed during package.
  * Repackaged Spring Boot jar created successfully.
* JavaScript syntax checks passed:
  * `node --check src/main/resources/static/js/app-settings.js`
  * `node --check src/main/resources/static/js/staff-live-map.js`
  * `node --check src/main/resources/static/js/app-notifications.js`
* Local app smoke test passed on port `8095` using the test-classpath H2 runtime:
  * `/login` returned HTTP 200.
  * Staff routes `/staff/pickup-request`, `/staff/missions`, and `/staff/live-map` returned HTTP 200.
  * `/staff/live-map/state` returned HTTP 200 with `application/json` on three consecutive polls.
  * Manager routes `/manager/policy-assignment` and `/manager/robot-tasks` returned HTTP 200.
  * Admin routes `/dashboard`, `/robots`, `/rules`, `/simulation`, and `/system-flow` returned HTTP 200.
  * The temporary app process was stopped after verification.

Scope confirmation:

* Cleanup and formatting only; no feature was added and no architecture was rewritten.
* No business logic, public route mapping, database schema, repository method, DTO field, entity field, enum value, role hierarchy, or role permission was changed.
* Mission lifecycle behavior was not changed. Robot availability after return-to-Base remains separate from Staff confirmation.
* Live Map route logic, waypoint geometry, backend polling, and backend-driven state binding were not changed.
* Battery drain, charging, and reassignment behavior were not changed.
* Interpreter and Strategy behavior was not changed.
* `RuleParser` and `StrategyContext` received comments only; their executable behavior was unchanged.
* `RuleEvaluator`, `RuleEngine`, and all strategy execution behavior were unchanged.
* No yellow cargo approach lines were reintroduced.

---

Full End-to-End QA Regression Completed

June 18, 2026 full QA regression and demo-readiness stabilization:

Completed in this QA pass:

* Required project context files were reviewed before testing or code changes:
  * `AGENTS.md`
  * `DESIGN.md`
  * `PROJECT_ROADMAP.md`
  * `PROJECT_STATUS.md`
  * `README.md`
* Full end-to-end regression pass completed for the graduation project workflow:
  * Admin rule management, robot management, simulation, system flow, and cross-role access.
  * Manager policy/task monitoring and cancellation visibility.
  * Staff pickup request, mission processing, mission execution, Live Map polling, Complete validation, and Stop/Cancel validation.
  * Interpreter Pattern rule evaluation and Strategy Pattern dispatch demonstrations.
  * English/Vietnamese translation coverage for the recently added mission lifecycle and cancellation text.
* Blocking bug fixed:
  * The seeded demo `Low Battery Rule` still mapped `battery < 20` to `ChargingStrategy`.
  * This conflicted with the project requirement that `battery < 20` demonstrates `EnergySavingStrategy`, while charging is reserved for critical/configured charging rules.
* Demo seed data corrected:
  * `Critical Battery With Obstacle Rule` now seeds as `battery < 10 AND obstacleDetected == TRUE -> ChargingStrategy`.
  * `Low Battery Rule` now seeds as `battery < 20 -> EnergySavingStrategy`.
  * Existing named seed rules are refreshed on startup when their stored condition/strategy/priority differs from the intended demo default, so an older local demo database is corrected without changing the rule engine.
* Rule form default corrected:
  * New Admin rule form now defaults `battery < 20` to `EnergySavingStrategy` instead of `ChargingStrategy`.
* Regression tests added:
  * Seeded battery rules are asserted at Spring context level.
  * Low battery selects `EnergySavingStrategy` when the robot is not critical.
  * Critical battery with obstacle still selects `ChargingStrategy`.
  * `priority == 1` selects `FastRouteStrategy` when no higher-priority rule matches.

Scenarios tested:

* Role access:
  * Staff can access Staff pages and is blocked from Manager/Admin pages.
  * Manager can access Manager + Staff pages and is blocked from Admin pages.
  * Admin can access Admin + Manager + Staff pages.
  * Public login page shows Staff demo credential only and does not show Admin/Manager demo credentials.
* Routes checked locally:
  * `/dashboard`
  * `/robots`
  * `/rules`
  * `/simulation`
  * `/system-flow`
  * `/staff/pickup-request`
  * `/staff/missions`
  * `/staff/live-map`
  * `/staff/live-map/state`
  * `/manager/policy-assignment`
  * `/manager/robot-tasks`
* Rule/strategy checks:
  * Small Cargo maps to `robotLoad=30`.
  * Medium Cargo maps to `robotLoad=60`.
  * Large Cargo maps to `robotLoad=90`.
  * Large Cargo matches HeavyLoad behavior at the default heavy-load threshold.
  * Medium Cargo can match HeavyLoad when the threshold is lowered to 60.
  * `priority == 1` selects `FastRouteStrategy` when no higher-priority rule matches.
  * `battery < 20` selects `EnergySavingStrategy`.
  * Critical/configured charging rule still selects `ChargingStrategy`.
  * StrategyContext dispatch tests still cover all strategy classes and unknown strategy fallback.
* Staff E2E checks:
  * Staff login with `Nova001 / nova001`.
  * Created pickup requests through the real form.
  * Processed missions through RuleEvaluator and StrategyContext.
  * Started mission execution.
  * Verified `/staff/live-map/state` returns HTTP 200 repeatedly.
  * Verified Live Map robot position/progress changes during polling.
  * Verified returned missions reach `WAITING_CONFIRMATION`.
  * Verified robot can show/start a new active mission while an older mission waits for Staff confirmation.
  * Verified Complete succeeds after return-to-Base and does not block robot availability.
  * Verified early Complete, already-completed Complete, cancelled Complete, and missing-mission Complete all redirect safely without Whitelabel 500.
* Stop/Cancel checks:
  * Stop without reason is rejected with `Please select a cancellation reason.`
  * Stop with `WRONG_LOCATION` and a note succeeds.
  * Staff Missions shows cancellation reason, note, cancelled by, and cancelled at details.
  * Manager Robot Task Board shows cancellation reason, note, and cancelled by for Manager visibility.
* Live Map checks:
  * Show All Robots control is present.
  * Selected/current task state comes from `/staff/live-map/state`.
  * Strategy, battery, status, charging, route, and mission state come from backend JSON.
  * Polling remained stable after mission Complete/cancel QA actions.
  * Old visible cargo approach guide lines remain hidden by CSS; route geometry was not changed.
* Language/static checks:
  * Required Vietnamese translations are present for cargo load, waiting confirmation, complete guard, cancellation labels, and cancellation reason options.
  * JavaScript syntax passed for settings, Live Map, and notifications.
* Mobile/tunnel readiness checks:
  * Application code/templates/static assets/tests contain no hard-coded `localhost` or `127.0.0.1` URLs.
  * Staff mobile pages rely on responsive Bootstrap/app CSS already in the project.
  * A real public tunnel and physical phone were not opened in this QA environment; local relative URL and responsive/static checks passed.

Files changed in this QA pass:

* `PROJECT_STATUS.md`
* `src/main/java/com/warehouse/config/DataSeeder.java`
* `src/main/java/com/warehouse/service/RuleService.java`
* `src/test/java/com/warehouse/controller/ManagerPolicyAssignmentControllerTest.java`
* `src/test/java/com/warehouse/service/RulePrioritySelectionTest.java`

Verification result:

* Focused affected tests passed:
  * `mvn test "-Dtest=RulePrioritySelectionTest,ManagerPolicyAssignmentControllerTest"`
  * 9 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.
* Full automated regression passed:
  * `mvn test`
  * 126 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.
* Package build passed:
  * `mvn clean package`
  * 126 tests passed during package.
  * Repackaged Spring Boot jar created successfully.
* JavaScript syntax checks passed:
  * `node --check src/main/resources/static/js/app-settings.js`
  * `node --check src/main/resources/static/js/staff-live-map.js`
  * `node --check src/main/resources/static/js/app-notifications.js`
* Local app smoke test passed on port `8095` using test-classpath H2 runtime:
  * `/login` returned HTTP 200.
  * Role access checks passed for Staff, Manager, and Admin.
  * Staff mission flow, Live Map polling, Complete validation, Stop/Cancel reason validation, and Manager cancellation visibility were verified.
  * Temporary port `8095` app process was stopped after verification.

Remaining known limitations:

* Physical phone and real public tunnel were not exercised in this environment.
* Mobile checks were limited to responsive/static inspection and local route/workflow verification.
* Some seeded demo missions may already be in `WAITING_CONFIRMATION` or assigned states; this is acceptable for classroom demo data but should be reset if a completely empty demo workflow is desired.

Scope confirmation:

* Core Interpreter Pattern logic was not changed.
* Core Strategy Pattern logic was not changed.
* `RuleParser`, `RuleEvaluator`, `RuleEngine`, `StrategyContext`, and strategy classes were not changed.
* Live Map route geometry and waypoint layout were not changed.
* Battery drain formula was not changed.
* Charging/reassignment behavior was not changed in this QA pass.
* Role hierarchy, demo accounts, login flow, and security access rules were not changed.
* No UI redesign was performed.
* No WebSocket was added.
* No scheduler was added.
* No new major module was added.
* Original project requirements remain aligned: Interpreter Pattern + Strategy Pattern + warehouse robot decision simulation.

---

Live Map Polling Stabilization Completed

June 18, 2026 Live Map robot stuck and polling stabilization fix:

Completed in this session:

* Required project context files were reviewed before code changes:
  * `AGENTS.md`
  * `DESIGN.md`
  * `PROJECT_ROADMAP.md`
  * `PROJECT_STATUS.md`
  * `README.md`
* Live Map robot stuck bug fixed in the returned-to-base / charging / reassignment path.
* `/staff/live-map/state` polling path inspected end to end:
  * `StaffLiveMapController.showLiveMapState`
  * `LiveMapStateService.getLiveMapState`
  * `LiveMapStateService.buildRobotMissionProgress`
  * `MissionService.markReturnedToBase`
  * `MissionService.markReturnedToBaseInternal`
  * `RobotChargingService.updateRobotAvailabilityAfterMissionReturn`
  * `RobotChargingService.startChargingAfterMissionClosure`
  * `RobotChargingService.reassignRemainingQueuedMissions`
  * `MissionRepository.findByAssignedRobotIdAndStatusInAndDeletedAtIsNullOrderByPriorityAscCreatedAtAscIdAsc`
* Likely runtime root cause addressed:
  * `WAITING_CONFIRMATION` was added after the SQL Server `missions` table may already have existed.
  * Hibernate/H2 tests recreate the enum with `WAITING_CONFIRMATION`, but SQL Server `ddl-auto=update` may leave old enum/check constraints in place.
  * A SQL Server-only compatibility runner now removes old check constraints from `missions.status` and `missions.execution_step`, preventing auto-flush failure when the Live Map persists `WAITING_CONFIRMATION` / `RETURNED_TO_BASE`.
* `markReturnedToBase` idempotency improved:
  * `WAITING_CONFIRMATION` returns as a no-op.
  * `COMPLETED` returns as a no-op.
  * `CANCELLED` returns as a no-op.
  * Returned-to-base processing still runs only for an eligible `IN_PROGRESS` mission that has actually reached Base.
* Charging/reassignment repeated side effects fixed:
  * A robot already in an active charging workflow does not re-run queued reassignment.
  * Charging is started once before reassignment is attempted.
  * Non-charging robot availability updates skip redundant robot saves when the robot is already available with the same battery/charging state.
* Robot availability after return preserved:
  * If charging is not required, robot becomes `IDLE` / available.
  * If charging is required, robot becomes `CHARGING`.
  * Pending queued missions are reassigned only when the robot actually enters charging.
* Complete action safety verified after this change:
  * Early Complete remains blocked safely.
  * Returned / `WAITING_CONFIRMATION` missions can still be completed.
  * Complete no longer shows Whitelabel 500 for normal blocked states.
* Added regression coverage for repeated Live Map polling:
  * Critical-battery return starts charging once.
  * Queued mission reassignment happens once.
  * A second `/staff/live-map/state` poll stays HTTP 200 and does not change `chargingStartedAt`.
  * A second poll does not reassign the same queued mission again.

Files changed:

* `PROJECT_STATUS.md`
* `src/main/java/com/warehouse/config/DatabaseCompatibilityConfig.java`
* `src/main/java/com/warehouse/service/MissionService.java`
* `src/main/java/com/warehouse/service/RobotChargingService.java`
* `src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java`

Verification result:

* Focused Live Map verification passed:
  * `mvn test "-Dtest=StaffLiveMapControllerTest"`
  * 22 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.
* Staff mission lifecycle verification passed:
  * `mvn test "-Dtest=StaffPickupRequestControllerTest"`
  * 32 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.
* `mvn test` passed:
  * 123 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.
* `mvn clean package` passed:
  * 123 tests passed during package.
  * Repackaged Spring Boot jar created successfully.

Scope confirmation:

* Core Interpreter Pattern logic was not changed.
* Core Strategy Pattern logic was not changed.
* `RuleParser`, `RuleEvaluator`, `RuleEngine`, `StrategyContext`, and strategy classes were not changed.
* Live Map route geometry and waypoint layout were not changed.
* Battery drain formula was not changed.
* Role hierarchy, demo accounts, and security flow were not changed.
* Language settings and notification UI were not changed.
* No WebSocket was added.
* No scheduler was added.
* No new major module was added.

---

Complete Mission Whitelabel Error Fix Completed

June 18, 2026 Complete mission Whitelabel 500 fix:

Completed in this session:

* Required project context files were reviewed before code changes:
  * `AGENTS.md`
  * `DESIGN.md`
  * `PROJECT_ROADMAP.md`
  * `PROJECT_STATUS.md`
  * `README.md`
* Complete mission 500 error fixed for Staff mission completion.
* Complete action validation is now safe for direct route/form submissions:
  * Missing mission redirects to `/staff/missions` with `Mission not found.`
  * Early completion redirects to `/staff/missions` with `Mission can only be completed after the robot returns to Base Station.`
  * Already completed mission redirects to `/staff/missions` with `Mission is already completed.`
  * Cancelled mission redirects to `/staff/missions` with `Cancelled missions cannot be completed.`
* `/staff/missions/{id}/complete` now has a controlled GET fallback as well as the existing POST action, so opening the Complete URL directly does not show a Whitelabel page.
* Successful completion now redirects to `/staff/missions` with `Mission completed.`
* Completion is still allowed only after return-to-Base evidence:
  * `executionStep == RETURNED_TO_BASE`
  * or `returnedAt` exists
  * or status is `WAITING_CONFIRMATION`
* `WAITING_CONFIRMATION` support checked and aligned:
  * Backend completion readiness accepts `WAITING_CONFIRMATION`.
  * Staff Missions already renders the `WAITING_CONFIRMATION` badge.
  * Staff Missions already shows flash success/error messages cleanly.
  * Staff Missions still shows a disabled Complete action with `Robot is still working.` while the robot has not returned.
* Vietnamese translations added for the new safe completion messages:
  * `Mission not found.` = `Không tìm thấy nhiệm vụ.`
  * `Mission is already completed.` = `Nhiệm vụ đã được hoàn tất.`
  * `Cancelled missions cannot be completed.` = `Nhiệm vụ đã hủy không thể hoàn tất.`
* Completion controller tests were updated to cover:
  * successful returned mission completion
  * successful `WAITING_CONFIRMATION` completion
  * direct GET completion fallback for a returned mission
  * early completion redirect without Whitelabel
  * missing mission redirect without Whitelabel
  * completed and cancelled mission redirect handling

Files changed:

* `PROJECT_STATUS.md`
* `src/main/java/com/warehouse/controller/StaffPickupRequestController.java`
* `src/main/java/com/warehouse/service/MissionService.java`
* `src/main/java/com/warehouse/model/Mission.java`
* `src/main/resources/static/js/app-settings.js`
* `src/test/java/com/warehouse/controller/StaffPickupRequestControllerTest.java`

Verification result:

* Focused controller verification passed:
  * `mvn test "-Dtest=StaffPickupRequestControllerTest"`
  * 32 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.
* JavaScript syntax check passed:
  * `node --check src/main/resources/static/js/app-settings.js`
* `mvn test` passed:
  * 122 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.

Scope confirmation:

* Core Interpreter Pattern logic was not changed.
* Core Strategy Pattern logic was not changed.
* `RuleParser`, `RuleEvaluator`, `RuleEngine`, `StrategyContext`, and strategy classes were not changed.
* Live Map route logic and route geometry were not changed.
* Battery, charging, and reassignment logic were not changed.
* Robot availability remains separated from Staff clicking Complete.
* Role hierarchy, demo accounts, and security flow were not changed.
* No WebSocket was added.
* No scheduler was added.
* No new major module was added.

---

Mission Rule Meaning and Confirmation Flow Refinement Completed

June 18, 2026 mission rule meaning and operational flow refinement:

Completed in this session:

* Required project context files were reviewed before code changes:
  * `AGENTS.md`
  * `DESIGN.md`
  * `PROJECT_ROADMAP.md`
  * `PROJECT_STATUS.md`
  * `README.md`
* `robotLoad` meaning clarified and documented as estimated cargo load percentage, not physical zone size.
* Cargo load mapping verified in the centralized `CargoType` enum:
  * Small Cargo = 30% load -> Zone A
  * Medium Cargo = 60% load -> Zone B
  * Large Cargo = 90% load -> Zone C
* Staff Pickup Request now uses server-provided cargo/zone/load/location maps from `MissionService` for the warehouse zone cards and inline JavaScript instead of duplicating cargo mapping in the template script.
* Staff Pickup Request saved mission summary now shows `Estimated cargo load`, so the cargo-to-`robotLoad` value is visible after Staff creates a request.
* HeavyLoad rule behavior documented in `README.md`:
  * `robotLoad >= 80` matches Large Cargo.
  * `robotLoad >= 60` matches Medium Cargo and Large Cargo.
  * Changing the Admin rule threshold intentionally changes which cargo types match `HeavyLoadStrategy`.
* Existing HeavyLoad behavior verified:
  * Large Cargo uses `robotLoad=90`.
  * Medium Cargo uses `robotLoad=60`.
  * `HeavyLoadStrategy` can match Medium Cargo when the threshold is lowered to 60.
* Robot availability remains separated from Staff mission confirmation:
  * Active robot workload counts include `PENDING`, `ASSIGNED`, and `IN_PROGRESS`.
  * `WAITING_CONFIRMATION` is counted separately and does not block robot assignment.
  * A robot can receive another mission after returning to Base before Staff clicks Completed.
* Existing returned-to-base lifecycle verified:
  * Live Map polling persists `WAITING_CONFIRMATION` when backend progress reaches Base Station.
  * `executionStep` becomes `RETURNED_TO_BASE`.
  * `returnedAt` is recorded.
  * Robot becomes `IDLE` / available if charging is not required.
  * Robot becomes `CHARGING` if existing charging rules require charging.
* Staff Live Map initial page render now includes `WAITING_CONFIRMATION` missions when a robot has no current `IN_PROGRESS`, `ASSIGNED`, or `PENDING` task.
* Staff Live Map initial mission flow now highlights the returned/waiting-confirmation step and shows:
  * `Returned to Base. Waiting for confirmation.`
* Completed action validation preserved:
  * Backend still rejects early completion before returned-to-base.
  * Completed closes a `WAITING_CONFIRMATION` record later without being required for robot availability.
* Cancellation reason requirement verified:
  * Stop/Cancel requires a valid cancellation reason.
  * Missing cancellation reason is rejected with `Please select a cancellation reason.`
  * Stored cancellation metadata includes reason code, note, cancelled by, and cancelled at.
* Manager cancellation visibility verified:
  * Manager Robot Task Board shows cancelled/stopped missions with reason, note, cancelled by, and cancelled at in details.
* Vietnamese translations verified as present for the new labels/messages, including:
  * Estimated cargo load = `Mức tải ước tính`
  * Waiting Confirmation = `Chờ xác nhận`
  * Returned-to-base waiting message
  * Completion guard message
  * Cancellation reason/note/by/at labels
  * All suggested cancellation reason labels

Files changed:

* `README.md`
* `PROJECT_STATUS.md`
* `src/main/java/com/warehouse/controller/StaffLiveMapController.java`
* `src/main/resources/templates/staff-pickup-request.html`
* `src/main/resources/templates/staff-live-map.html`
* `src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java`

Verification result:

* Focused controller verification passed:
  * `mvn test "-Dtest=StaffPickupRequestControllerTest,StaffLiveMapControllerTest"`
  * 50 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.
* JavaScript syntax checks passed:
  * `node --check src/main/resources/static/js/staff-live-map.js`
  * `node --check src/main/resources/static/js/app-settings.js`
  * `node --check src/main/resources/static/js/app-notifications.js`
* `mvn test` passed:
  * 119 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.
* `mvn clean package` passed:
  * 119 tests passed during package.
  * Repackaged Spring Boot jar created successfully.

Scope confirmation:

* Core Interpreter Pattern architecture was not changed.
* Core Strategy Pattern architecture was not changed.
* `RuleParser`, `RuleEvaluator`, `RuleEngine`, `StrategyContext`, and strategy classes were not changed.
* Rule condition syntax was not changed.
* Live Map route geometry and waypoint data were not changed.
* Battery drain formula was not changed.
* Charging and reassignment logic were not changed; existing availability behavior after return-to-base was preserved.
* Role hierarchy, demo accounts, and security flow were not changed.
* No WebSocket was added.
* No scheduler was added.
* No new major module was added.

---

Shared Local Tunnel Demo Preparation Completed

June 16, 2026 shared local tunnel demo preparation:

Completed in this session:

* Shared local tunnel demo readiness completed for classroom testing from the user's laptop.
* Public tunnel demo flow documented in `README.md`.
* Added a `Shared Local Mobile Demo Using Public Tunnel` section that explains:
  * Teacher phone connects through a public tunnel URL.
  * The tunnel forwards to the user's laptop localhost Spring Boot app.
  * Teacher phone and laptop browser see the same missions because both hit the same running app instance.
  * The laptop, Spring Boot app, and tunnel terminal must stay running.
  * Closing the tunnel stops public access as expected.
* Added Cloudflare Tunnel instructions:
  * `cloudflared tunnel --url http://localhost:8080`
  * `cloudflared tunnel --url http://localhost:8095` when the app runs on port 8095.
* Added ngrok instructions:
  * `ngrok http 8080`
  * `ngrok http 8095` when the app runs on port 8095.
* Added tunnel troubleshooting for:
  * Teacher cannot open the link.
  * Phone does not update.
  * Live Map state fails.
  * Login redirects incorrectly.
* Existing Cloud Run documentation is now clearly marked as optional and not required for the shared local tunnel demo.
* Relative URL check completed:
  * No hard-coded `localhost`, `127.0.0.1`, `http://localhost`, or `https://localhost` URLs were found in application templates, static assets, Java source, or tests.
  * Live Map polling still uses the relative endpoint `/staff/live-map/state`.
  * Login, logout, Staff forms, and static app assets remain server-relative or Thymeleaf-generated.
* Public login page check completed:
  * Public login shows only Staff demo credentials: `Nova001 / nova001`.
  * Public login does not show `Admin / admin`.
  * Public login does not show `Manage / manage`.
* Staff mobile flow verified locally through the running app:
  * Staff login worked.
  * `/staff/pickup-request` returned HTTP 200.
  * `/staff/missions` returned HTTP 200.
  * `/staff/live-map` returned HTTP 200.
  * `/staff/live-map/state` returned HTTP 200.
  * Staff created pickup request `REQ-TUNNEL-SMOKE`.
  * Staff Missions displayed the new request from the same running app instance.
  * Staff processed the mission.
  * Staff started execution.
  * Live Map state contained the new mission.
  * Direct early Complete submission was rejected until return to Base Station.
* Staff access control verified locally:
  * Staff received HTTP 403 for `/dashboard`, `/rules`, `/robots`, `/simulation`, `/system-flow`, `/manager/policy-assignment`, and `/manager/robot-tasks`.
* Mobile layout readiness checked against the current responsive CSS:
  * Sidebar hides on phone width.
  * Page padding reduces on phone width.
  * Form controls, buttons, location cells, mission actions, robot controls, zone controls, and Live Map controls have touch-friendly mobile sizing.
  * Staff Missions action buttons wrap on phone width.
  * Notification, settings, and user dropdown panels are width-constrained on phone width.
  * Live Map prioritizes the map before side panels on phone width and allows horizontal scrolling for the wide warehouse board.
* Shared-device behavior documented:
  * Local SQL Server data is unchanged when running the default app configuration.
  * In-memory demo/test data is acceptable for a tunnel demo when both devices hit the same app instance, but resets when the app restarts.

Files changed:

* `README.md`
* `PROJECT_STATUS.md`

Verification result:

* `node --check src/main/resources/static/js/staff-live-map.js` passed.
* `node --check src/main/resources/static/js/app-settings.js` passed.
* `node --check src/main/resources/static/js/app-notifications.js` passed.
* `mvn test` passed:
  * 110 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.
* Local app startup smoke test passed on port `8095` using test classpath H2 runtime:
  * `/login` returned HTTP 200.
  * Login page showed `Nova001 / nova001`.
  * Login page did not show Admin credentials.
  * Login page did not show Manager credentials.
  * Staff login `Nova001 / nova001` reached Staff Pickup Request.
  * Staff could access `/staff/pickup-request`, `/staff/missions`, `/staff/live-map`, and `/staff/live-map/state`.
  * Staff was blocked from Admin and Manager pages with HTTP 403.
  * Staff created, viewed, processed, and started pickup request `REQ-TUNNEL-SMOKE`.
  * `/staff/live-map/state` returned the same mission state from the running app.
  * Early Complete was blocked by the backend with the return-to-Base validation message.
  * Temporary smoke-test app process was stopped after verification.

Scope confirmation:

* No full cloud deployment was added.
* No Google Cloud Run configuration was added in this task.
* No Firebase Hosting, Cloud SQL, paid service, service account key, or secret was added.
* No WebSocket or scheduler was added.
* No core mission logic was changed.
* No mission assignment logic was changed.
* No Live Map route logic was changed.
* No battery, charging, or reassignment logic was changed.
* No role hierarchy or role permissions were changed.
* No Interpreter Pattern logic was changed.
* No Strategy Pattern logic was changed.
* `RuleParser`, `RuleEvaluator`, `RuleEngine`, `StrategyContext`, and strategy classes were not changed.

---

Online Staff Mobile Demo Preparation Completed

June 11, 2026 online Staff mobile demo deployment preparation:

Completed in this session:

* Online Staff mobile demo preparation completed for public Cloud Run testing.
* Public login page now shows only the Staff demo credentials:
  * `Nova001 / nova001`
* Admin and Manager demo accounts remain in `SecurityConfig` for local/admin testing, but they are hidden from the public login hint.
* Staff mobile workflow prepared and smoke verified locally with the demo profile:
  * Staff login.
  * Staff Pickup Request.
  * Staff Missions.
  * Process mission.
  * Start execution.
  * Staff Live Map backend polling state.
  * Backend rejection of direct early `complete` submission before return to Base Station.
* Staff mobile UI refinement completed:
  * Larger touch targets for forms, selects, buttons, mission actions, and location cells.
  * Mission action buttons wrap cleanly on phone width.
  * Returned/waiting and still-working messages wrap as compact mobile-friendly status text.
  * Staff Missions now exposes a visible Live Map button for phone users because the desktop sidebar is hidden on mobile.
  * Live Map mobile layout prioritizes the map before side panels and keeps horizontal scroll available for the wide warehouse board.
* Cloud/demo profile status:
  * Added `application-demo.properties`.
  * Demo profile uses H2 and does not require localhost SQL Server.
  * Demo profile explicitly overrides the inherited SQL Server Hibernate dialect with `H2Dialect`.
* H2/demo database status:
  * In-memory H2 database configured as `jdbc:h2:mem:warehouse_demo`.
  * Demo data may reset when the app or Cloud Run instance restarts.
* Demo data seed status:
  * Existing idempotent `DataSeeder` seeds the three required robots: Picker Alpha, Mover Beta, and Carrier Gamma.
  * Existing idempotent seed data provides active strategies and active rules for mission processing.
  * Staff location data remains generated by `MissionService` for Zone A / A1-A9, Zone B / B1-B9, and Zone C / C1-C9.
* Dockerfile status:
  * Added `Dockerfile`.
  * Added `.dockerignore`.
  * Docker CLI was not available/responding in this shell, so local Docker build/run could not be executed here.
* Cloud Run readiness status:
  * `server.port=${PORT:8080}` configured.
  * Packaged jar runs locally with `--spring.profiles.active=demo` on `PORT=8096`.
  * Static app CSS/JS loaded successfully from the demo app.
  * Staff route access and blocked Admin/Manager route access verified locally.
* README deployment instructions updated with:
  * Public Staff demo purpose.
  * Public Staff credentials.
  * Local demo run command.
  * Docker build/run command.
  * Cloud Run deploy command.
  * Demo limitations.

Files changed:

* `pom.xml`
* `Dockerfile`
* `.dockerignore`
* `README.md`
* `PROJECT_STATUS.md`
* `src/main/resources/application.properties`
* `src/main/resources/application-demo.properties`
* `src/main/resources/templates/login.html`
* `src/main/resources/templates/staff-missions.html`
* `src/main/resources/static/css/app.css`
* `src/main/resources/static/css/staff-live-map.css`
* `src/main/java/com/warehouse/controller/AuthController.java`
* `src/test/java/com/warehouse/controller/RoleNavigationControllerTest.java`

Verification result:

* `node --check src/main/resources/static/js/staff-live-map.js` passed.
* `node --check src/main/resources/static/js/app-settings.js` passed.
* `node --check src/main/resources/static/js/app-notifications.js` passed.
* `mvn test` passed:
  * 110 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.
* `mvn clean package` passed:
  * 110 tests passed during package.
  * Repackaged Spring Boot jar created successfully.
* Local demo profile smoke test passed:
  * `/login` returned HTTP 200.
  * Login page showed `Nova001 / nova001`.
  * Login page did not show Admin credentials.
  * Login page did not show Manager credentials.
  * Staff login `Nova001 / nova001` reached Staff Pickup Request.
  * Staff could access `/staff/pickup-request`, `/staff/missions`, `/staff/live-map`, `/staff/live-map/state`, `/settings`, `/css/app.css`, and `/js/app-settings.js`.
  * Staff received HTTP 403 for `/dashboard`, `/rules`, `/robots`, `/simulation`, `/system-flow`, `/manager/policy-assignment`, and `/manager/robot-tasks`.
  * Staff created pickup request `REQ-DEMO-SMOKE`.
  * Staff processed the mission.
  * Staff started execution.
  * `/staff/live-map/state` returned backend mission state with the mission `IN_PROGRESS`.
  * Direct early POST to complete the mission was rejected with the return-to-Base validation message.

Scope confirmation:

* No core business logic was changed.
* No Interpreter Pattern logic was changed.
* No Strategy Pattern logic was changed.
* No mission execution logic was changed.
* No Live Map route logic was changed.
* No battery, charging, or reassignment logic was changed.
* No role hierarchy or role permissions were changed.
* `RuleParser`, `RuleEvaluator`, `RuleEngine`, `StrategyContext`, and strategy classes were not changed.

---

Live Map Yellow Cargo Approach Lines Removed

June 11, 2026 Live Map visual cleanup update:

Completed in this session:

* Yellow cargo approach lines removed from the Live Map visual presentation.
* Zone A/B/C visual cleanup completed.
* Cargo approach SVG path/node rendering is now hidden so the yellow horizontal lane overlays no longer appear inside cargo rows.
* Route-world approach segments no longer render as yellow lines.
* Approach route dots no longer use yellow styling.
* White route network lines remain visible.
* Route dots / waypoints remain visible.
* Robot markers, robot labels, package/cargo boxes, Base Station, and Charging Station visuals remain intact.
* Show All Robots mode and selected robot mode are unchanged.
* Route logic preserved, including existing `*-approach` waypoint data used by movement paths.

Files changed:

* `src/main/resources/static/css/staff-live-map.css`
* `PROJECT_STATUS.md`

Verification result:

* `node --check src/main/resources/static/js/staff-live-map.js` passed.
* `mvn test` passed:
  * 110 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.

Scope confirmation:

* No backend behavior changed.
* No mission assignment or mission execution logic changed.
* No Live Map route/waypoint logic changed.
* No battery, charging, or reassignment logic changed.
* No role logic changed.
* `RuleParser`, `RuleEvaluator`, `RuleEngine`, `StrategyContext`, and strategy classes were not changed.

---

Full Project-Wide Vietnamese Translation Completed

June 11, 2026 full UI Vietnamese translation update:

Completed in this session:

* Full project-wide Vietnamese translation completed for the frontend UI language switch.
* Pages translated:
  * Login / authentication.
  * Dashboard.
  * Rule Management.
  * Robot Management.
  * Staff Pickup Request.
  * Staff Missions and Staff Mission Detail.
  * Staff Live Map.
  * Manager Policy Assignment.
  * Manager Robot Task Board.
  * Execution Simulator.
  * System Flow.
  * Settings, shared header/topbar controls, sidebar navigation, user menu, and notification dropdown.
* Live Map translated for static labels and dynamic JavaScript-rendered text:
  * Show All Robots / selected robot modes.
  * Zone cards, cargo subtitles, station labels, mission flow labels, route preview messages, status/battery/strategy lines, and polling-driven messages.
* Notification/toast messages translated through the shared frontend translation helper.
* Dynamic status, priority, strategy, mission-flow, battery, charging, route-preview, and lifecycle message translation added.
* Settings/header/sidebar translation completed using the existing `localStorage` language preference path.
* Backend state values remain unchanged; translation is display-only.
* Technical rule expressions and route keys remain unchanged.

Files changed:

* `src/main/resources/static/js/app-settings.js`
* `src/main/resources/static/js/app-notifications.js`
* `src/main/resources/static/js/staff-live-map.js`
* `src/main/resources/templates/staff-pickup-request.html`
* `PROJECT_STATUS.md`

Verification result:

* `node --check src/main/resources/static/js/app-settings.js` passed.
* `node --check src/main/resources/static/js/app-notifications.js` passed.
* `node --check src/main/resources/static/js/staff-live-map.js` passed.
* `mvn test` passed:
  * 110 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.

Scope confirmation:

* No business logic changed.
* No backend services changed.
* No mission execution logic changed.
* No Live Map route/waypoint logic changed.
* No battery, charging, reassignment, or route service logic changed.
* No role hierarchy or role permissions changed.
* `RuleParser`, `RuleEvaluator`, `RuleEngine`, `StrategyContext`, and strategy classes were not changed.

---

Live Map UI Restored and Polished

June 11, 2026 Live Map UI restoration update:

Completed in this session:

* Live Map UI restored/polished toward the older cleaner map layout style.
* Backend-driven behavior preserved.
* `/staff/live-map` route preserved.
* `/staff/live-map/state` polling preserved.
* Backend-driven robot positions preserved.
* Real battery, status, charging, mission, `primaryStrategyName`, and `currentActiveStrategyName` state remain sourced from backend polling.
* Mission flow, route/waypoint logic, cargo approach lanes, Show All Robots mode, and selected robot mode were preserved.
* Base Station / Charging Station display was restored to the cleaner Zone C-only presentation while keeping route waypoint data intact.
* Show All Robots overview now uses larger, clearer Zone A / Zone B / Zone C cards.
* Cargo boxes, route dots, robot markers, and labels were enlarged and simplified for readability.
* Side panel spacing and mission-flow rows were tightened to keep the detail panel compact.
* No backend services, mission execution logic, `RuleEvaluator`, `StrategyContext`, battery logic, charging logic, or route logic were changed.
* No static fake frontend data was added.

Files changed:

* `src/main/resources/static/css/staff-live-map.css`
* `src/main/resources/static/js/staff-live-map.js`
* `PROJECT_STATUS.md`

Verification result:

* `node --check src/main/resources/static/js/staff-live-map.js` passed.
* `node --check src/main/resources/static/js/app-settings.js` passed.
* `node --check src/main/resources/static/js/app-notifications.js` passed.
* `mvn test` passed:
  * 110 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.
* Runtime smoke test with test classpath app on port `8095` passed:
  * Staff login with `Nova001` / `nova001` succeeded.
  * `/staff/live-map` returned HTTP 200.
  * `/staff/live-map/state` returned HTTP 200 JSON.
  * State response contained robots, `primaryStrategyName`, `currentActiveStrategyName`, and battery data.

Scope confirmation:

* No backend logic changed.
* No mission execution logic changed.
* No rule evaluation or strategy dispatch logic changed.
* No battery, charging, reassignment, or warehouse route service logic changed.
* Live Map behavior remains backend-driven through `/staff/live-map/state`.

---

Settings Dropdown, User Menu, Completion Guard, and Vietnamese Translation Refinement Completed

June 10, 2026 settings/user dropdown and Staff mission action refinement:

This task continued the existing Settings and In-App Notifications work. The required project documents and current implementation were inspected before changes were made, and the existing settings/notification files were reused instead of duplicating routes, controllers, JavaScript, CSS, or templates.

Already present before this refinement:

* `/settings` route and page already existed and remained as a fallback.
* `SettingsController` already allowed Admin, Manage, and Staff to access `/settings`.
* `app-settings.js` already stored Language, Theme, and UI Density in `localStorage`.
* Theme and density classes already existed:
  * `theme-light`
  * `theme-dark`
  * `density-compact`
  * `density-comfortable`
* Shared topbar already had the notification bell/dropdown.
* Shared topbar already had a Settings icon, but it linked to the full Settings page.
* Sidebar still had a large Settings/User area that made the layout feel cluttered.
* User/avatar display in the header was not yet a compact dropdown.
* Staff mission completion was too permissive because active missions could expose completion before the robot had returned to Base Station.
* Notification duplicate protection for Live Map polling was already present and was preserved.

Completed in this session:

* Moved the primary Settings flow into a compact gear dropdown in the shared topbar.
* The gear dropdown now contains:
  * Language: English / Tiếng Việt
  * Theme: Light mode / Dark mode
  * UI Density: Compact / Comfortable
* Settings still apply through the existing localStorage-backed `app-settings.js` mechanism.
* Kept `/settings` as a fallback route, but removed it as the main navigation flow.
* Removed the large Settings/User block from the sidebar to keep role navigation compact.
* Added/refined the topbar user avatar dropdown with:
  * Current display name
  * Current role
  * Logout action
* Kept the notification bell/dropdown compact in the topbar.
* Improved Vietnamese labels and messages for common navigation, settings, role, mission, status, action, notification, cargo, zone, and strategy display text.
* Notification rendering now uses the lightweight settings translation helper so stored English notification messages display naturally in Vietnamese mode.
* Staff Missions now shows a disabled Complete action and the message `Robot is still working.` while a mission is assigned, moving, picking up, or returning.
* Staff Missions enables Complete only when the mission is active and returned to Base Station.
* Returned missions show `Returned to Base. Waiting for confirmation.`
* Backend completion validation was added in `MissionService`; direct POST completion is blocked until the robot has returned to Base Station.
* The backend error for early completion is:
  * English: `Mission can only be completed after the robot returns to Base Station.`
  * Vietnamese dictionary: `Chỉ có thể hoàn tất nhiệm vụ sau khi robot đã về trạm.`
* The backend readiness check reuses the existing route/progress services so dynamically returned Live Map missions can be completed without changing Live Map route logic.

Settings dropdown status:

* Gear dropdown is present in the shared topbar for logged-in Admin, Manage, and Staff users.
* Language selector is compact and uses `Tiếng Việt` for Vietnamese.
* Theme selector still applies `theme-light` and `theme-dark`.
* UI Density selector still applies `density-compact` and `density-comfortable`.
* Settings apply immediately when changed.
* `/settings` remains available as a fallback compatibility page.

User avatar dropdown status:

* Header avatar is now a dropdown.
* Dropdown displays current username and role.
* Logout remains available and functional inside the dropdown.
* No profile management, registration, password change, OAuth, JWT, or database user features were added.

Staff Completed action status:

* Complete is unavailable while a mission is only assigned.
* Complete is unavailable while execution is `MOVING_TO_TARGET`.
* Complete is unavailable while execution is `PICKING_UP`.
* Complete is unavailable while execution is `RETURNING_TO_BASE`.
* Complete is available after persisted `returnedAt`, persisted `RETURNED_TO_BASE`, or dynamically computed returned-to-base progress.
* Direct early completion POSTs are rejected by the backend with a clear user-facing error.

Vietnamese translation status:

* Key shared UI labels now use natural Vietnamese terms such as:
  * `Cài đặt`
  * `Thông báo`
  * `Đăng xuất`
  * `Vai trò`
  * `Nhiệm vụ`
  * `Yêu cầu lấy hàng`
  * `Bản đồ trực tiếp`
* Mission action/status messages include natural Vietnamese translations:
  * `Robot đang thực hiện nhiệm vụ.`
  * `Robot đã về trạm. Chờ xác nhận hoàn tất.`
  * `Chỉ có thể hoàn tất nhiệm vụ sau khi robot đã về trạm.`
  * `Nhiệm vụ đã hoàn tất.`
  * `Nhiệm vụ đã dừng.`
* Strategy class names remain unchanged in code, while the UI dictionary provides friendly labels such as:
  * `FastRouteStrategy` -> `Di chuyển nhanh`
  * `EnergySavingStrategy` -> `Tiết kiệm năng lượng`
  * `ObstacleAvoidanceStrategy` -> `Tránh vật cản`
  * `ChargingStrategy` -> `Sạc pin`

Files changed in this refinement:

* `src/main/java/com/warehouse/model/Mission.java`
* `src/main/java/com/warehouse/service/MissionService.java`
* `src/main/java/com/warehouse/controller/StaffPickupRequestController.java`
* `src/main/resources/templates/fragments/topbar-controls.html`
* `src/main/resources/templates/fragments/sidebar.html`
* `src/main/resources/templates/settings.html`
* `src/main/resources/templates/staff-missions.html`
* `src/main/resources/static/css/app.css`
* `src/main/resources/static/js/app-settings.js`
* `src/main/resources/static/js/app-notifications.js`
* `src/test/java/com/warehouse/controller/RoleNavigationControllerTest.java`
* `src/test/java/com/warehouse/controller/StaffPickupRequestControllerTest.java`
* `PROJECT_STATUS.md`

Verification result:

* `node --check src/main/resources/static/js/app-settings.js` passed.
* `node --check src/main/resources/static/js/app-notifications.js` passed.
* `node --check src/main/resources/static/js/staff-live-map.js` passed.
* Focused test run passed:
  * `mvn test "-Dtest=RoleNavigationControllerTest,StaffPickupRequestControllerTest,StaffLiveMapControllerTest"`
  * 55 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.
* Final full `mvn test` passed:
  * 110 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.

Scope confirmation:

* No WebSocket was added.
* No scheduler was added.
* No email or push notification feature was added.
* No browser push notification permission request was added.
* Notification storage remains simple/localStorage-based.
* Role hierarchy was not changed.
* Security access rules were not changed except preserving existing `/settings` access.
* Mission assignment logic was not changed except enforcing Completed validation.
* Route calculation logic was not changed.
* Battery drain logic was not changed.
* Charging/reassignment logic was not changed.
* `/staff/live-map/state` contract was not changed.
* Live Map polling behavior was not changed.
* `RuleParser` was not changed.
* `RuleEvaluator` was not changed.
* `RuleEngine` classes were not changed.
* `StrategyContext` was not changed.
* Strategy classes were not changed.

---

Settings and In-App Notifications Continuation Completed

June 10, 2026 Settings + Notifications continuation update:

This was a continuation of an interrupted task. The codebase was inspected first to avoid restarting or duplicating existing work.

Already present before this continuation:

* `/settings` route and `SettingsController` already existed.
* Security already allowed `/settings` for `ADMIN`, `MANAGE`, and `STAFF`.
* Shared sidebar already included a Settings link while preserving role-based navigation and Logout.
* Shared topbar controls already included a notification bell/dropdown and Settings icon.
* `app-settings.js` already stored preferences in `localStorage` and applied:
  * `theme-light`
  * `theme-dark`
  * `density-compact`
  * `density-comfortable`
* `settings.html` already provided Language, Theme, and UI Density controls.
* `app-notifications.js` already provided localStorage-backed recent notifications, notification dropdown rendering, toast notifications, Live Map event collection, and active-event duplicate protection during polling.
* `/staff/live-map` already loaded `app-notifications.js` before `staff-live-map.js`.
* `staff-live-map.js` already called `window.WarehouseNotifications.trackLiveMapState(state)` after polling `/staff/live-map/state`.
* Completed and Stop mission actions already emitted primary mission notification flash attributes.

Completed in this session:

* Reused the existing settings and notification implementation instead of duplicating controllers, templates, CSS, JavaScript, or routes.
* Made Staff mission workflow notifications explicit for charging and reassignment events instead of relying on success-message text parsing.
* Added explicit flash-backed notification events for:
  * Charging started: `Robot is charging at station.`
  * Task reassigned: `Remaining tasks were reassigned.`
* Applied those explicit notification events to both Completed and Stop mission lifecycle actions when the existing charging workflow reports that charging/reassignment happened.
* Updated `staff-missions.html` to render the explicit notification events through existing hidden `data-notification-event` hooks.
* Added focused test coverage for:
  * Charging-started notification flash attributes.
  * Task-reassigned notification flash attributes.
  * Stop lifecycle charging/reassignment notification coverage.
  * Live Map notification script stable event keys and duplicate-polling guard.

Settings page status:

* `/settings` is available to Admin, Manage, and Staff.
* Language preference is saved in `localStorage`.
* Language preference is applied to key shared layout/settings labels through the existing lightweight `data-i18n` mechanism.
* Light/Dark theme switching is implemented with `theme-light` and `theme-dark` classes.
* UI Density switching is implemented with `density-compact` and `density-comfortable` classes.
* Settings remain browser/device local and demo-friendly.

Notification status:

* Notification bell/dropdown is present in the shared topbar after login.
* Toast notification support is present through `app-notifications.js`.
* Recent notifications are stored in `localStorage`.
* Live Map notification integration is present through `/staff/live-map/state` polling.
* Duplicate notification protection is implemented for active Live Map polling events with stable event keys.
* Supported Live Map notification events include:
  * Returned to Base
  * Low battery
  * Critical battery / charging required
  * Charging started
  * Fully charged
  * Waiting for path
* Supported Staff action notification events include:
  * Mission completed
  * Mission stopped
  * Task reassigned
  * Charging started
* Role-specific filtering remains intentionally simple for the demo: common operational notifications are visible to logged-in users through the shared topbar.

Files changed in this continuation:

* `src/main/java/com/warehouse/controller/StaffPickupRequestController.java`
* `src/main/resources/templates/staff-missions.html`
* `src/test/java/com/warehouse/controller/StaffPickupRequestControllerTest.java`
* `src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java`
* `PROJECT_STATUS.md`

Verification result:

* `node --check src/main/resources/static/js/app-settings.js` passed.
* `node --check src/main/resources/static/js/app-notifications.js` passed.
* `node --check src/main/resources/static/js/staff-live-map.js` passed.
* Initial full `mvn test` passed before final edits:
  * 106 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.
* Focused post-edit test run passed:
  * `mvn test "-Dtest=StaffPickupRequestControllerTest,StaffLiveMapControllerTest,RoleNavigationControllerTest"`
  * 53 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.
* Final full `mvn test` passed after all edits:
  * 108 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.

Scope confirmation:

* No WebSocket was added.
* No scheduler was added.
* No email notification was added.
* No browser push notification was added.
* No push notification permission request was added.
* No complex notification database was added.
* Mission lifecycle behavior was not changed.
* Mission assignment logic was not changed.
* Live Map route logic was not changed.
* `/staff/live-map/state` contract was not changed.
* Live Map polling behavior was not changed.
* Battery drain logic was not changed.
* Charging/reassignment logic was not changed.
* `RuleParser` was not changed.
* `RuleEvaluator` was not changed.
* `RuleEngine` classes were not changed.
* `StrategyContext` was not changed.
* Strategy classes were not changed.

---

Simple Role-Based Authentication Completed

June 9, 2026 simple role-based login and navigation update:

This task added demo-friendly authentication and role-based navigation for Admin, Manage, and Staff users while keeping the warehouse decision engine logic unchanged.

Simple role-based login added:

* Spring Security was added through Maven using the existing Spring Boot 3.5.0 stack.
* A custom login page was added at `/login`.
* Successful login redirects by role:
  * `Admin` -> `/dashboard`
  * `Manage` -> `/manager/robot-tasks`
  * `Nova001` -> `/staff/pickup-request`
* Logout uses `/logout` and redirects to `/login?logout`.
* Unauthorized access returns a simple `/access-denied` page with HTTP 403 instead of a 500 error.
* CSRF is disabled intentionally for this graduation demo so existing simple POST forms continue to work without rewriting all forms; this is documented as demo-only behavior, not production security.

Demo users added:

* `Admin` / `admin` with role hierarchy `ADMIN`, `MANAGE`, `STAFF`
* `Manage` / `manage` with role hierarchy `MANAGE`, `STAFF`
* `Nova001` / `nova001` with role `STAFF`
* Passwords use `BCryptPasswordEncoder` in the in-memory demo user store.

Role hierarchy documented:

* `ADMIN > MANAGE > STAFF`
* Admin can open Admin, Manager, and Staff pages.
* Manage can open Manager and Staff pages.
* Staff can open Staff pages only.

Route access rules documented:

* Staff routes:
  * `/staff/**` requires `STAFF`, `MANAGE`, or `ADMIN`.
  * `/staff/live-map/state` remains available to authenticated Staff/Manage/Admin users for Live Map polling.
* Manager routes:
  * `/manager/**` requires `MANAGE` or `ADMIN`.
* Admin routes:
  * `/`
  * `/dashboard`
  * `/rules/**`
  * `/robots/**`
  * `/simulation/**`
  * `/system-flow/**`
  * These require `ADMIN`.
* Static assets under `/css/**`, `/js/**`, `/images/**`, `/webjars/**`, and `/favicon.ico` remain public so authenticated pages render correctly.

Role-based navigation added:

* Shared sidebar navigation now renders sections based on the logged-in role.
* Staff navigation shows only:
  * Create Pickup Request
  * My Missions
  * Live Warehouse Map
  * Logout
* Manage navigation shows:
  * Manager Robot Task Board
  * Rule / Policy Assignment
  * Staff Pickup Request
  * Staff Missions
  * Live Map
  * Logout
* Admin navigation shows:
  * Dashboard
  * Rule Management
  * Robot Management
  * Simulation
  * System Flow
  * Manager Policy Assignment
  * Manager Robot Task Board
  * Staff Pickup Request
  * Staff Missions
  * Live Map
  * Logout
* Duplicate/empty planned links such as Mission Monitor and Robot Operations remain hidden.

Staff mobile-friendly UI improvements:

* `/staff/pickup-request` location cells are larger and easier to tap on phone width.
* Staff form controls and top bar spacing were tightened for mobile.
* `/staff/missions` action buttons now wrap into larger touch-friendly controls on phone width.
* Mission lifecycle and filter controls stack cleanly on small screens.
* `/staff/live-map` keeps existing polling and route movement intact while improving mobile control tap targets and spacing.

Files changed:

* `pom.xml`
* `src/main/java/com/warehouse/config/SecurityConfig.java`
* `src/main/java/com/warehouse/controller/AuthController.java`
* `src/main/java/com/warehouse/controller/SecurityModelAdvice.java`
* `src/main/resources/templates/login.html`
* `src/main/resources/templates/access-denied.html`
* `src/main/resources/templates/fragments/sidebar.html`
* `src/main/resources/static/css/app.css`
* `src/main/resources/static/css/staff-live-map.css`
* `src/test/java/com/warehouse/controller/RoleNavigationControllerTest.java`
* `src/test/java/com/warehouse/controller/SystemFlowControllerTest.java`
* `src/test/java/com/warehouse/controller/RobotManagementControllerTest.java`
* `src/test/java/com/warehouse/controller/ManagerPolicyAssignmentControllerTest.java`
* `src/test/java/com/warehouse/controller/ManagerRobotTaskBoardControllerTest.java`
* `src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java`
* `src/test/java/com/warehouse/controller/StaffPickupRequestControllerTest.java`
* `PROJECT_STATUS.md`

Verification result:

* `mvn test` passed.
* 105 tests passed.
* 0 failures.
* 0 errors.
* 0 skipped.
* Temporary verification app started on port `8096` with test classpath / H2 datasource.
* HTTP verification on the running app confirmed:
  * `/login` returned HTTP 200 and showed the role demo credentials used at that stage.
  * Staff login `Nova001` / `nova001` redirected to `/staff/pickup-request`.
  * Staff could open `/staff/pickup-request`.
  * Staff received HTTP 403 for `/rules`.
  * Manage login `Manage` / `manage` redirected to `/manager/robot-tasks`.
  * Manage could open `/manager/robot-tasks`.
  * Manage received HTTP 403 for `/robots`.
  * Admin login `Admin` / `admin` redirected to `/dashboard`.
  * Admin could open `/dashboard` and `/robots`.
  * Staff could open `/staff/live-map`.
  * Staff could call `/staff/live-map/state`, which returned HTTP 200 JSON.
* Security/navigation tests verify:
  * Login page loads.
  * Admin/Admin does not apply; correct credentials are `Admin` / `admin`.
  * `Admin` redirects to `/dashboard`.
  * `Manage` redirects to `/manager/robot-tasks`.
  * `Nova001` redirects to `/staff/pickup-request`.
  * Admin can access Admin, Manager, and Staff routes.
  * Manage can access Manager and Staff routes but receives HTTP 403 for Admin-only routes.
  * Staff can access Staff routes and `/staff/live-map/state` but receives HTTP 403 for Manager/Admin routes.
  * Logout redirects to `/login?logout`.
  * Sidebar links change by role.

Scope confirmation:

* `RuleParser` was not changed.
* `RuleEvaluator` was not changed.
* `RuleEngine` classes were not changed.
* `StrategyContext` was not changed.
* Strategy classes were not changed.
* Mission logic was not changed.
* Mission assignment logic was not changed.
* Mission lifecycle logic was not changed.
* Live Map route logic was not changed.
* Live Map polling was not changed.
* Battery logic was not changed.
* Charging logic was not changed.
* Reassignment logic was not changed.
* No WebSocket was added.
* No scheduler was added.
* No barcode scan was added.
* No customer priority queue was added.
* No OAuth, JWT, database user management, registration, or password reset was added.

---

Previous completed stage:

Live Map Route Polish Completed

June 9, 2026 Live Map route polish and strategy synchronization update:

This task polished the Staff Live Map Show All Robots view, made cargo slot approach paths visible and stable, and removed static seeded strategy display from linked robot pages when backend runtime state is available.

Show All Robots layout polished:

* `/staff/live-map` Show All Robots mode now uses a larger central map area with narrower side panels and tighter spacing.
* Zone A, Zone B, and Zone C cards are larger and easier to scan together on a laptop screen.
* Cargo/package boxes, package dots, route lines, route dots, robot markers, and robot labels were enlarged and given stronger contrast.
* Existing selected-robot mode still uses the same route, package, and robot components.

Cargo approach lanes added/refined:

* Every package slot A1-A9, B1-B9, and C1-C9 now has a visible approach lane in the zone map.
* Each approach lane exposes a stable approach waypoint such as `A3-approach`, `B5-approach`, and `C9-approach` through `data-route-point`.
* Exact package slots keep their `data-location` / `data-location-code` values such as `A3`, `B5`, and `C9`.
* Frontend route coordinates now resolve approach waypoints as lane-aligned points before the exact slot, instead of weighted points near the package that could still look diagonal.
* Full route preview also draws approach lane segments, so the preview and single-zone/overview maps use the same approach network.

Approach waypoint behavior documented in code/tests:

* Backend route building continues to use explicit route rules only:
  * outbound route ends with lane waypoint -> target approach waypoint -> exact target slot
  * return route starts with exact target slot -> target approach waypoint -> return lane waypoint
* `WarehouseRouteServiceTest` now verifies all 27 cargo slots use the exact approach-before-pickup and approach-before-return sequence.
* Existing row-aware routes remain intact for C9, A3, B5, C5, and the A1-A9 / B1-B9 / C1-C9 slot mapping.

Strategy display synchronized from backend state:

* Live Map strategy badges continue to read `primaryStrategyName`, `currentActiveStrategyName`, `movementMode`, and `strategyMessage` from `/staff/live-map/state`.
* Live Map now displays backend strategy class names such as `FastRouteStrategy`, `ObstacleAvoidanceStrategy`, `HeavyLoadStrategy`, and `ChargingStrategy` instead of remapping them to hard-coded frontend labels.
* Idle/no-mission robots now fall back to neutral `NormalStrategy` backend state and the UI hides it where appropriate instead of showing seeded robot strategy assignments as active runtime strategy.

Robot Management and Manager Task Board synchronization:

* `/robots` now uses the Live Map backend state snapshot for battery display, charging display, current mission strategy, movement mode, and compact strategy badges.
* Idle robots on `/robots` show `Idle` instead of fake/static seeded strategy classes.
* Charging robots on `/robots` show `CHARGING`, charging battery progress, and `ChargingStrategy` from backend state.
* `/manager/robot-tasks` now attaches the same backend live robot state per robot group and shows compact current strategy / movement badges without duplicating Live Map details.
* `/staff/missions` selected strategy badges were left unchanged and remain compact mission-selected strategy display.

Files changed:

* `src/main/java/com/warehouse/dto/RobotFleetStatusDto.java`
* `src/main/java/com/warehouse/dto/RobotTaskGroupDto.java`
* `src/main/java/com/warehouse/service/ManagerRobotTaskBoardService.java`
* `src/main/java/com/warehouse/service/RobotExecutionBehaviorService.java`
* `src/main/java/com/warehouse/service/RobotFleetStatusService.java`
* `src/main/resources/templates/manager-robot-tasks.html`
* `src/main/resources/templates/robots.html`
* `src/main/resources/static/css/staff-live-map.css`
* `src/main/resources/static/js/staff-live-map.js`
* `src/test/java/com/warehouse/controller/ManagerRobotTaskBoardControllerTest.java`
* `src/test/java/com/warehouse/controller/RobotManagementControllerTest.java`
* `src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java`
* `src/test/java/com/warehouse/service/WarehouseRouteServiceTest.java`
* `PROJECT_STATUS.md`

Verification result:

* `node --check src/main/resources/static/js/staff-live-map.js` passed.
* Focused test run passed:
  * `mvn test "-Dtest=WarehouseRouteServiceTest,StaffLiveMapControllerTest,RobotManagementControllerTest,ManagerRobotTaskBoardControllerTest"`
  * 32 tests passed.
  * 0 failures.
  * 0 errors.
  * 0 skipped.
* Full `mvn test` passed.
* 99 tests passed.
* 0 failures.
* 0 errors.
* 0 skipped.
* Temporary verification app started on port `8095` with test classpath.
* HTTP route verification returned HTTP 200 for:
  * `/dashboard`
  * `/robots`
  * `/rules`
  * `/simulation`
  * `/system-flow`
  * `/staff/pickup-request`
  * `/staff/missions`
  * `/staff/live-map`
  * `/staff/live-map/state`
  * `/manager/policy-assignment`
  * `/manager/robot-tasks`

Scope confirmation:

* `RuleParser` was not changed.
* `RuleEvaluator` was not changed.
* `RuleEngine` classes were not changed.
* `StrategyContext` was not changed.
* Strategy classes were not changed.
* Mission lifecycle logic was not changed.
* Battery drain logic was not changed.
* Charging/reassignment logic was not changed.
* Backend polling architecture remains unchanged.
* No WebSocket was added.
* No scheduler was added.
* No barcode scan was added.
* No customer priority queue was added.
* No complex pathfinding or random alternate route selection was added.

---

Previous completed stage:

Final UI Cleanup Completed

June 6, 2026 final UI cleanup update:

This task completed the final application-wide UI cleanup for demo readiness. The work focused on presentation, navigation clarity, compact page layout, and shorter user-facing messages while keeping backend behavior unchanged.

Final UI cleanup completed:

* Pages polished for a cleaner Admin, Manager, and Staff dashboard experience.
* Navigation cleanup completed.
* The duplicate/empty planned `Mission Monitor` item was hidden from navigation.
* Long notes were reduced.
* Technical details remain available where useful, but are collapsed or summarized by default.
* Existing Bootstrap/project styling was reused.

Dashboard UI cleanup:

* `/dashboard` now focuses on high-level summary cards for robots, active missions, active rules, and recent history.
* Fleet and recent execution data are shown in compact sections.
* Active rule and strategy catalog details are kept in a collapsed technical section.
* A read-only active mission count was added for dashboard display only.

Staff Pickup Request UI improved:

* `/staff/pickup-request` now has shorter copy and a cleaner form area.
* Cargo mapping is easier to scan:
  * Small Cargo -> Zone A
  * Medium Cargo -> Zone B
  * Large Cargo -> Zone C
* Location grid ranges are shown clearly:
  * A1-A9
  * B1-B9
  * C1-C9
* Old planned-phase wording was removed.

Manager Rule / Policy Assignment UI improved:

* `/manager/policy-assignment` keeps the Manager role clear:
  * Admin creates active rules.
  * Manager assigns active policies to zones.
* Zone policy cards remain compact and show current policy, strategy, and save action.
* Long negative/explanatory text and planned labels were removed.

Live Map side panel simplified:

* `/staff/live-map` now uses the final mission flow label:
  * Returned / Waiting Confirmation
* Returned-to-base display text was shortened to:
  * Returned to Base. Waiting for confirmation.
* Backend polling remains the source of truth.
* Live Map route movement logic was not changed.

Other pages kept in presentation-ready state:

* Staff Missions UI remains compact with lifecycle actions preserved.
* Robot Management battery/charging UI remains compact and consistent with Live Map and Manager Robot Task Board.
* Manager Robot Task Board UI remains focused on workload, active tasks, high priority tasks, battery, and charging/unavailable state.
* Rule Management, Simulation, and System Flow remain cleaned up and presentation-friendly.
* Simulation still exposes Interpreter Pattern and Strategy Pattern results, with evaluation trace available.
* System Flow still explains Robot Input -> Interpreter -> Rule Match -> Strategy Dispatch -> Robot Action -> History.

Files changed in this final cleanup:

* `src/main/java/com/warehouse/dto/DashboardSummaryDto.java`
* `src/main/java/com/warehouse/repository/MissionRepository.java`
* `src/main/java/com/warehouse/service/DashboardService.java`
* `src/main/resources/templates/fragments/sidebar.html`
* `src/main/resources/templates/dashboard.html`
* `src/main/resources/templates/staff-pickup-request.html`
* `src/main/resources/templates/manager-policy-assignment.html`
* `src/main/resources/templates/staff-live-map.html`
* `src/main/resources/static/js/staff-live-map.js`
* `src/test/java/com/warehouse/controller/RoleNavigationControllerTest.java`
* `src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java`
* `PROJECT_STATUS.md`

Verification result:

* `node --check src/main/resources/static/js/staff-live-map.js` passed.
* `mvn test` passed.
* 96 tests passed.
* 0 failures.
* 0 errors.
* 0 skipped.
* Temporary verification app started on port `8095` with test classpath.
* HTTP route verification returned HTTP 200 for:
  * `/dashboard`
  * `/robots`
  * `/rules`
  * `/simulation`
  * `/system-flow`
  * `/staff/pickup-request`
  * `/staff/missions`
  * `/staff/live-map`
  * `/staff/live-map/state`
  * `/manager/policy-assignment`
  * `/manager/robot-tasks`
* Content checks confirmed:
  * Dashboard shows Active Missions.
  * Navigation no longer shows Mission Monitor.
  * Pickup Request shows A1-A9, B1-B9, and C1-C9.
  * Manager Policy Assignment shows Policy Flow and no planned labels.
  * Live Map shows Returned / Waiting Confirmation and no old Returned / Completed Pending label.
* Sample `POST /simulation` returned HTTP 200 and confirmed:
  * Rule Match output is still shown.
  * Selected Strategy is still shown.
  * Robot Action is still shown.
  * Evaluation Trace is still available.
* Temporary verification app on port `8095` was stopped after route checks.

Scope confirmation:

* UI/UX cleanup only.
* No major features were added.
* No business logic was changed.
* The dashboard active mission count is read-only display data only.
* `RuleParser` was not changed.
* `RuleEvaluator` was not changed.
* `RuleEngine` classes were not changed.
* `StrategyContext` was not changed.
* Strategy classes were not changed.
* Mission assignment logic was not changed.
* Mission lifecycle logic was not changed.
* Mission execution behavior was not changed.
* Live Map route logic was not changed.
* Live Map backend polling remains unchanged.
* Battery drain logic was not changed.
* Battery/charging/reassignment logic was not changed.
* No WebSocket was added.
* No scheduler was added.
* No barcode scan was added.
* No customer priority queue was added.

---

Previous completed stage:

Presentation-Ready UI Cleanup Completed

June 6, 2026 UI/UX cleanup update:

This task cleaned up the user-facing warehouse UI for presentation/demo use while preserving existing backend behavior. The changes reduce developer-style explanations, summarize technical data, and move detailed decision output into collapsed sections where normal users are not overwhelmed.

UI cleanup completed:

* Pages polished for a cleaner warehouse dashboard style.
* Long notes and technical explanation blocks were reduced.
* Technical details were collapsed or summarized instead of shown by default.
* Badges, compact cards, concise labels, and progress bars were used consistently.
* No major features were added.
* No business logic was changed.

Staff Missions UI improved:

* `/staff/missions` now uses compact mission cards instead of a dense multi-column table.
* Default mission view focuses on:
  * Request Code
  * Cargo Type
  * Location
  * Priority
  * Status
  * Assigned Robot
  * Strategy
  * Execution state
  * Lifecycle actions
* Lifecycle buttons remain available on the same endpoints:
  * Process
  * Start Execution
  * Completed
  * Stop
  * Delete after Stop
* Assignment reason, matched rule, selected strategy class, action message, decision summary, notes, timestamps, and current position are now inside collapsed `Technical details`.
* Long mission processing explanation was replaced with a compact lifecycle strip.

Live Map UI improved:

* `/staff/live-map` header copy was shortened.
* Map remains the visual focus.
* Right-side panel is simplified around selected robot state.
* Side panel now summarizes:
  * Selected Robot
  * Current Mission
  * Status
  * Battery progress
  * Primary Strategy
  * Current Active Strategy
  * Mission Flow
  * One concise status message
* Mission Flow is now a clean 5-step flow:
  * Assigned
  * Moving
  * Pickup
  * Returning
  * Returned / Completed Pending
* Long visible Live Map messages were shortened:
  * Returned to Base. Waiting for Staff confirmation.
  * Charging at station.
  * Waiting for path to clear.
  * Heavy load mode active.
* Strategy names are displayed as compact user-facing labels such as Fast, Energy Saving, Heavy Load, Obstacle Avoidance, Charging, and Safe Route.
* Visual Route Preview remains clearly labeled as preview-only while backend polling remains the main behavior.
* Live Map backend polling and route logic were not changed.

Robot Management UI improved:

* `/robots` now keeps robot cards focused on:
  * Robot name/code
  * Status
  * Battery progress
  * Charging state
  * Current strategy
* Charging/critical battery messages are shortened in the UI.
* Raw robot fields such as obstacle, load, distance, and priority are moved into collapsed `Technical details`.

Manager Robot Task Board UI improved:

* `/manager/robot-tasks` now shows workload cards with compact battery progress and concise charging/critical messages.
* Long workload definition text is collapsed.
* Mission rows are tighter and easier to scan.
* Rule, action, and decision summary details are collapsed per mission.
* Reassignment/decision details remain available for demo review without crowding the default board.

Rule Management UI cleanup:

* `/rules` title and subtitle were simplified.
* Supported syntax guidance is collapsed.
* Rule table now separates condition, strategy, status, priority, and actions into cleaner columns.
* Strategy names and status values remain visible through compact badges.
* Create/edit/delete/enable/disable actions remain unchanged.

Simulation UI cleanup:

* `/simulation` keeps the input form clear.
* Result view now emphasizes:
  * Matched Rule
  * Selected Strategy
  * Robot Action
  * Evaluation Trace
* Evaluation Trace and condition details are collapsed by default but still available for explaining the Interpreter Pattern.
* Simulation behavior was not changed.

System Flow UI cleanup:

* `/system-flow` copy was shortened.
* Architecture flow is presented as concise cards/steps:
  * Robot Input
  * Interpreter
  * Rule Match
  * Strategy Dispatch
  * Robot Action
  * History
* Active rule, strategy, and history tables remain available.

Files changed in this task:

* `src/main/resources/templates/staff-missions.html`
* `src/main/resources/templates/staff-live-map.html`
* `src/main/resources/templates/manager-robot-tasks.html`
* `src/main/resources/templates/robots.html`
* `src/main/resources/templates/rules.html`
* `src/main/resources/templates/simulation.html`
* `src/main/resources/templates/system-flow.html`
* `src/main/resources/static/css/app.css`
* `src/main/resources/static/css/staff-live-map.css`
* `src/main/resources/static/js/staff-live-map.js`
* `src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java`
* `src/test/java/com/warehouse/controller/ManagerRobotTaskBoardControllerTest.java`
* `src/test/java/com/warehouse/controller/RobotManagementControllerTest.java`
* `PROJECT_STATUS.md`

Verification result:

* `node --check src/main/resources/static/js/staff-live-map.js` passed.
* `mvn test` passed.
* 96 tests passed.
* 0 failures.
* 0 errors.
* 0 skipped.
* Temporary verification app started on port `8097` with test classpath.
* HTTP route verification returned HTTP 200 for:
  * `/dashboard`
  * `/robots`
  * `/rules`
  * `/simulation`
  * `/system-flow`
  * `/staff/pickup-request`
  * `/staff/missions`
  * `/staff/live-map`
  * `/staff/live-map/state`
  * `/manager/policy-assignment`
  * `/manager/robot-tasks`
* Sample `POST /simulation` returned HTTP 200 and confirmed:
  * Evaluation Trace is still available.
  * Selected Strategy is still shown.
  * Final Robot Action is still shown.
* Temporary verification app on port `8097` was stopped after route checks.

Scope confirmation:

* UI/UX cleanup only.
* No business logic was changed.
* `RuleParser` was not changed.
* `RuleEvaluator` was not changed.
* `RuleEngine` classes were not changed.
* `StrategyContext` was not changed.
* Strategy classes were not changed.
* Mission assignment logic was not changed.
* Mission execution behavior was not changed.
* Live Map route logic was not changed.
* Live Map backend polling remains unchanged.
* Battery drain logic was not changed.
* Battery/charging/reassignment logic was not changed.
* No WebSocket was added.
* No scheduler was added.
* No barcode scan was added.
* No customer priority queue was added.

---

Previous completed stage:

Live Map Rule-Selected Strategy Behavior And Robot Management Charging Synchronization Added

June 6, 2026 strategy behavior and Robot Management synchronization update:

This task connected backend Live Map execution behavior to the mission's stored rule-selected strategy while preserving the existing Interpreter Pattern and Strategy Pattern core. It also synchronized Robot Management with the same charging-aware battery/status state already used by Live Map and the Manager Robot Task Board.

Strategy behavior now follows rule-selected primary strategy:

* `Mission.selectedStrategyName` remains the primary strategy selected from the winning matched rule during mission processing.
* `RobotExecutionBehaviorService` was added as a lightweight read-time behavior resolver.
* Primary strategy is not overwritten during runtime or phase overrides.
* Active missions with no stored selected strategy fall back to Normal behavior rather than inventing a rule-selected primary strategy.
* `FastRouteStrategy` uses Fast movement and Fast battery drain:
  * 3 waypoints = 1% battery
* `EnergySavingStrategy` uses Energy Saving movement and lower battery drain:
  * 7 waypoints = 1% battery
* `SafeRouteStrategy` is visible as Safe Route behavior and uses Normal timing/drain:
  * 5 waypoints = 1% battery
* `ObstacleAvoidanceStrategy` can be primary if selected by the rule engine and uses existing safe waiting behavior without changing route lane logic.

Temporary ObstacleAvoidanceStrategy override behavior:

* Existing bridge/path occupancy detection remains in `LiveMapStateService`.
* If a robot would enter an occupied bridge/path segment, its current active strategy becomes `ObstacleAvoidanceStrategy`.
* The primary strategy remains unchanged.
* The robot waits at the previous safe waypoint.
* The Live Map message becomes:
  * `Waiting for path to clear. Will resume FastRouteStrategy.`
* After the path is clear, active strategy resolves back to the primary/phase strategy on the next state read.
* No random alternate route or complex pathfinding was added.
* Existing lane rules remain unchanged:
  * outbound uses the middle-left lane
  * return uses the middle-right lane

HeavyLoadStrategy phase behavior after pickup:

* Large Cargo missions now use `HeavyLoadStrategy` as a phase strategy after pickup during the return phase.
* Example behavior:
  * outbound can stay `FastRouteStrategy`
  * after pickup / return phase switches current active strategy to `HeavyLoadStrategy`
* Heavy Load return movement uses slower/stable timing:
  * `HEAVY_LOAD_WAYPOINT_SECONDS = 20`
  * `HEAVY_LOAD_BRIDGE_SECONDS = 18`
* Heavy Load battery drain uses Normal drain for now:
  * 5 waypoints = 1% battery
* Primary `HeavyLoadStrategy` still behaves as Heavy Load from the start.

ChargingStrategy rule-based behavior and no-interruption rule:

* `ChargingStrategy` can now trigger the post-mission charging workflow because it was selected by the winning rule, not only because battery reached the hard `<= 5%` critical threshold.
* If a rule selects `ChargingStrategy` at a different threshold, the stored `selectedStrategyName = ChargingStrategy` is enough to require charging after mission closure.
* The current `IN_PROGRESS` mission is still not interrupted.
* `RETURNED_TO_BASE` still does not auto-complete the mission.
* Staff Completed is still required.
* After Staff completes the mission:
  * remaining queued tasks are reassigned
  * the robot is sent to Charging Station
  * charging simulation starts
* Charging still recovers about `+5%` every `10` seconds.

Live Map state fields added:

* `/staff/live-map/state` robot JSON now includes:
  * `primaryStrategyName`
  * `currentActiveStrategyName`
  * `strategyMessage`
  * `batteryDrainMode`
  * `robotStatus`
* Existing fields remain available:
  * `movementMode`
  * `batteryPercent`
  * `charging`
  * `chargingRequired`
  * mission status / execution status fields
* Charging robots now report:
  * `movementMode = CHARGING`
  * `primaryStrategyName = ChargingStrategy`
  * `currentActiveStrategyName = ChargingStrategy`
  * `robotStatus = CHARGING`

Live Map UI update:

* Staff Live Map now shows compact strategy details beside the robot battery/current target display.
* The UI displays:
  * Primary strategy
  * Current active strategy
  * Strategy message
* The existing polling model remains unchanged.

Robot Management charging synchronization fixed:

* `/robots` now uses `RobotFleetStatusService` and `RobotFleetStatusDto`.
* Robot Management reads charging-aware battery progress from `RobotChargingService.currentBatteryStatus`.
* Charging robots show:
  * status `CHARGING`
  * charging battery progress such as `15% battery (charging +10%)`
  * `Charging at Charging Station.`
* If charging reaches 100% during read, the existing charging completion behavior can mark the robot `IDLE` and battery `100`.
* Robot Management now stays consistent with `/staff/live-map/state` and `/manager/robot-tasks`.

Files changed in this task:

* `src/main/java/com/warehouse/model/RobotMovementMode.java`
* `src/main/java/com/warehouse/service/RobotExecutionBehaviorService.java`
* `src/main/java/com/warehouse/service/RobotBatteryDrainService.java`
* `src/main/java/com/warehouse/service/MissionExecutionProgressService.java`
* `src/main/java/com/warehouse/service/RobotChargingService.java`
* `src/main/java/com/warehouse/service/LiveMapStateService.java`
* `src/main/java/com/warehouse/service/RobotFleetStatusService.java`
* `src/main/java/com/warehouse/dto/LiveMapRobotStateDto.java`
* `src/main/java/com/warehouse/dto/RobotFleetStatusDto.java`
* `src/main/java/com/warehouse/controller/RobotManagementController.java`
* `src/main/resources/templates/robots.html`
* `src/main/resources/static/js/staff-live-map.js`
* `src/main/resources/static/css/staff-live-map.css`
* `src/main/resources/static/css/app.css`
* `src/test/java/com/warehouse/service/RobotBatteryDrainServiceTest.java`
* `src/test/java/com/warehouse/service/MissionExecutionProgressServiceTest.java`
* `src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java`
* `src/test/java/com/warehouse/controller/StaffPickupRequestControllerTest.java`
* `src/test/java/com/warehouse/controller/RobotManagementControllerTest.java`
* `PROJECT_STATUS.md`

Verification result:

* `node --check src/main/resources/static/js/staff-live-map.js` passed.
* `mvn test "-Dtest=RobotBatteryDrainServiceTest,MissionExecutionProgressServiceTest"` passed.
* `mvn test "-Dtest=StaffLiveMapControllerTest,StaffPickupRequestControllerTest,RobotManagementControllerTest,ManagerRobotTaskBoardControllerTest"` passed.
* `mvn test` passed.
* 96 tests passed.
* 0 failures.
* 0 errors.
* 0 skipped.
* A fresh verification app was started on port `8096` because an older app was already running on `8095`.
* Temporary HTTP route verification on `http://localhost:8096` returned HTTP 200 for:
  * `/dashboard`
  * `/robots`
  * `/rules`
  * `/simulation`
  * `/system-flow`
  * `/staff/pickup-request`
  * `/staff/missions`
  * `/staff/live-map`
  * `/staff/live-map/state`
  * `/manager/policy-assignment`
  * `/manager/robot-tasks`
* `/staff/live-map/state` was verified to expose `primaryStrategyName`, `currentActiveStrategyName`, and `robotStatus`.

Test coverage added or updated:

* Mission with `FastRouteStrategy` exposes primary and current active strategy as `FastRouteStrategy`.
* Bridge/path conflict temporarily exposes `ObstacleAvoidanceStrategy`.
* Bridge/path conflict waits safely and does not change route lane logic.
* After bridge/path waiting clears, active strategy is read-time resolved again from primary/phase behavior.
* Large Cargo mission switches to `HeavyLoadStrategy` after pickup / during return.
* Heavy Load return movement is slower than Fast return movement.
* `ChargingStrategy` selected by rule can start charging after Staff Completed even above the hard critical battery threshold.
* `ChargingStrategy` does not interrupt the current `IN_PROGRESS` mission.
* Safe, Heavy Load, and Obstacle Avoidance drain use Normal battery drain for now.
* `/staff/live-map/state` includes the new strategy fields.
* `/staff/live-map` script renders primary/current strategy and strategy message.
* `/robots` shows charging status and charging battery progress.
* Existing charging simulation still works.
* Existing reassignment after current mission completion still works.
* Existing lifecycle actions still work:
  * Completed
  * Stop
  * Delete after Stop
* Existing simulation behavior still works.

Scope confirmation:

* No WebSocket was added.
* No scheduler was added.
* No barcode scan was added.
* No customer priority queue was added.
* No complex pathfinding was added.
* No automatic mission completion was added.
* No current `IN_PROGRESS` mission interruption was added.
* `RuleParser` was not changed.
* `RuleEvaluator` was not changed.
* `RuleEngine` classes were not rewritten.
* `StrategyContext` was not changed.
* Strategy classes were not rewritten.

---

Previous completed stage:

Persistent Robot Battery And Strategy-Based Movement/Battery Consumption Fixed

June 6, 2026 persistent battery and movement mode update:

This task fixed the robot battery reset bug and connected route progression / battery drain to the mission movement mode. It keeps the existing Interpreter Pattern and Strategy Pattern core stable and does not add WebSocket behavior, scheduler behavior, barcode scanning, customer priority queues, complex pathfinding, or automatic mission completion.

Persistent robot battery fix:

* Robot battery now behaves as real persisted robot state during mission execution.
* `Mission.batteryAtExecutionStart` was added to anchor drain calculation to the battery value when execution starts.
* Staff Start Execution captures the assigned robot's battery at mission start.
* Live Map polling calculates effective battery from:
  * mission execution start battery
  * traveled waypoint count
  * movement mode drain rule
* The robot row is persisted only when a new whole-percent drain is reached.
* Battery is no longer recalculated by repeatedly subtracting from the already-drained robot value.
* Battery no longer resets after returning to Base Station.
* Refreshing `/staff/live-map/state` does not restore the original battery.
* Staff Completed / Stop also calculate and persist final drained battery before deciding whether charging is required.
* Battery remains clamped between 0% and 100%.

Cross-page battery consistency:

* `/staff/live-map/state` now returns the persisted/effective robot battery.
* `/staff/live-map` displays the updated backend battery from polling.
* `/robots` reads the same updated `Robot.battery` value, so the Robot Management battery bar/value stays consistent after mission drain.
* `/manager/robot-tasks` reads the same persisted robot battery and stays consistent with Robot Management.
* Test coverage verifies Mover Beta can drain from 15% to 13% and then show 13% through Live Map state, Robot Management, and Manager Robot Task Board.

Strategy/mode-based battery drain rules added:

* `NORMAL` mode:
  * applies when no special movement mode is selected
  * 5 traveled waypoints = 1% battery drain
* `FAST` mode:
  * applies when `selectedStrategyName = FastRouteStrategy`
  * 3 traveled waypoints = 1% battery drain
  * drains more battery than Normal for the same traveled waypoint count
* `ENERGY_SAVING` mode:
  * applies when `selectedStrategyName = EnergySavingStrategy`
  * also applies when a mission starts with low battery and no FastRouteStrategy override
  * 7 traveled waypoints = 1% battery drain
  * drains less battery than Normal for the same traveled waypoint count
* Low-battery warning behavior still exposes Energy Saving state when battery is below 20%.
* Critical battery still exposes charging required when battery is equal to or below 5%.

Strategy/mode-based movement timing added:

* Movement progression now supports mode-aware timing:
  * `FAST_WAYPOINT_SECONDS = 9`
  * `NORMAL_WAYPOINT_SECONDS = 15`
  * `ENERGY_SAVING_WAYPOINT_SECONDS = 18`
  * `FAST_BRIDGE_SECONDS = 8`
  * `NORMAL_BRIDGE_SECONDS = 12`
  * `ENERGY_SAVING_BRIDGE_SECONDS = 16`
* Existing default progress calculation remains Normal mode for compatibility.
* Fast mode reaches more waypoints than Normal for the same elapsed time.
* Energy Saving mode reaches fewer waypoints than Normal for the same elapsed time.
* To avoid route jumps, a mission that starts with normal battery keeps its route timing stable even if the battery warning later drops below 20%; the low-battery display still updates.

Live Map state endpoint update:

* `/staff/live-map/state` robot JSON now includes:
  * `movementMode`
  * `movementModeDisplay`
  * `waypointsPerBatteryPercent`
* Existing battery fields remain:
  * `batteryLevel`
  * `batteryPercent`
  * `batteryDrainPercent`
  * `batteryDisplayText`
  * `batteryWarningLevel`
  * `lowBattery`
  * `criticalBattery`
  * `energySavingMode`
  * `chargingRequired`
  * `batteryMessage`
* Charging state remains separate and does not run route-drain calculation.

Live Map UI update:

* Staff Live Map battery display now shows a compact movement mode line when available.
* Example display:
  * `Fast Mode - 3 waypoints per 1%`
  * `Normal Mode - 5 waypoints per 1%`
  * `Energy Saving Mode - 7 waypoints per 1%`
* No large battery table was added.

Charging and reassignment behavior preserved:

* Battery equal to or below 5% still does not interrupt an `IN_PROGRESS` mission.
* The robot still completes the current mission first.
* `RETURNED_TO_BASE` still does not auto-complete the mission.
* Staff Completed is still required.
* Charging simulation still recovers about `+5%` every 10 seconds.
* Reassignment after charging-required mission completion still works.

Files changed in this task:

* `src/main/java/com/warehouse/model/Mission.java`
* `src/main/java/com/warehouse/model/RobotMovementMode.java`
* `src/main/java/com/warehouse/service/RobotBatteryDrainService.java`
* `src/main/java/com/warehouse/service/MissionExecutionProgressService.java`
* `src/main/java/com/warehouse/service/RobotMissionBatteryService.java`
* `src/main/java/com/warehouse/service/MissionService.java`
* `src/main/java/com/warehouse/service/RobotChargingService.java`
* `src/main/java/com/warehouse/service/LiveMapStateService.java`
* `src/main/java/com/warehouse/dto/LiveMapRobotStateDto.java`
* `src/main/resources/static/js/staff-live-map.js`
* `src/main/resources/static/css/staff-live-map.css`
* `src/test/java/com/warehouse/service/RobotBatteryDrainServiceTest.java`
* `src/test/java/com/warehouse/service/MissionExecutionProgressServiceTest.java`
* `src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java`
* `src/test/java/com/warehouse/controller/StaffPickupRequestControllerTest.java`
* `PROJECT_STATUS.md`

Verification result:

* `node --check src/main/resources/static/js/staff-live-map.js` passed.
* `mvn test "-Dtest=RobotBatteryDrainServiceTest,MissionExecutionProgressServiceTest"` passed.
* `mvn test "-Dtest=StaffLiveMapControllerTest,StaffPickupRequestControllerTest,ManagerRobotTaskBoardControllerTest,RobotManagementControllerTest"` passed.
* `mvn test` passed.
* 89 tests passed.
* 0 failures.
* 0 errors.
* 0 skipped.
* App started on port `8095` with test classpath / H2 datasource for HTTP verification.
* Temporary HTTP route verification on `http://localhost:8095` returned HTTP 200 for:
  * `/dashboard`
  * `/robots`
  * `/rules`
  * `/simulation`
  * `/system-flow`
  * `/staff/pickup-request`
  * `/staff/missions`
  * `/staff/live-map`
  * `/staff/live-map/state`
  * `/manager/policy-assignment`
  * `/manager/robot-tasks`
* `/staff/live-map/state` was verified to expose `movementMode`, `waypointsPerBatteryPercent`, and `batteryPercent`.

Test coverage added or updated:

* Normal mode uses 5 waypoints per 1% battery.
* FastRouteStrategy mode uses 3 waypoints per 1% battery.
* EnergySavingStrategy / Energy Saving mode uses 7 waypoints per 1% battery.
* Fast mode drains more than Normal for the same waypoint count.
* Energy Saving mode drains less than Normal for the same waypoint count.
* Battery never goes below 0%.
* Battery never exceeds 100%.
* Fast mode route progression is faster than Normal.
* Energy Saving mode route progression is slower than Normal.
* Start Execution captures `batteryAtExecutionStart`.
* Robot battery does not reset after returning to Base Station.
* Robot battery does not reset after refreshing `/staff/live-map/state`.
* `/staff/live-map/state` returns updated battery and movement mode metadata.
* `/robots` displays the updated persisted battery after mission drain.
* `/manager/robot-tasks` displays the updated persisted battery after mission drain.
* Critical battery equal to or below 5% still does not interrupt the current `IN_PROGRESS` mission.
* Existing charging simulation still works.
* Existing reassignment after current mission completion still works.
* Existing lifecycle actions still work:
  * Completed
  * Stop
  * Delete after Stop
* Existing simulation behavior still works.

Scope confirmation:

* No WebSocket was added.
* No scheduler was added.
* No barcode scan was added.
* No customer priority queue was added.
* No automatic mission completion was added.
* No current `IN_PROGRESS` mission interruption was added.
* `RuleParser` was not changed.
* `RuleEvaluator` was not changed.
* `RuleEngine` classes were not rewritten.
* `StrategyContext` was not changed.
* Strategy classes were not rewritten.

---

Previous completed stage:

Charging Station Simulation And Reassign Remaining Tasks Added

June 6, 2026 charging station simulation update:

This task added the simple post-current-mission charging workflow for critical-battery robots. It keeps the existing Interpreter Pattern and Strategy Pattern core stable and does not add WebSocket behavior, scheduler behavior, barcode scanning, customer priority queues, complex pathfinding, fake missions, mission duplication, or automatic mission completion.

Critical-battery current mission behavior confirmed:

* Battery equal to or below 5% still does not interrupt the current `IN_PROGRESS` mission.
* Live Map route progression continues through `MOVING_TO_TARGET`, `PICKING_UP`, `RETURNING_TO_BASE`, and `RETURNED_TO_BASE`.
* The mission remains `IN_PROGRESS` after the robot returns to Base Station.
* Staff must still manually click Completed.
* The charging workflow is started only after the mission is closed through the existing Staff Completed action, or through Stop for the accepted cancelled-mission edge case.

Charging Station simulation added:

* `Robot` now supports charging workflow state:
  * `chargingRequired`
  * `charging`
  * `chargingStartedAt`
  * `chargingCompletedAt`
  * `batteryBeforeCharging`
* Charging robots use `status = CHARGING`.
* Fully charged robots return to `status = IDLE`.
* Charging uses `charging-station` as the simple Live Map position key.
* Charging message is `Charging at Charging Station.`

Charging rate documented:

* Charging recovers about `+5%` battery every `10` seconds.
* Formula:
  * `recoveredPercent = floor(chargingElapsedSeconds / 10) * 5`
  * `currentBattery = min(100, batteryBeforeCharging + recoveredPercent)`
* Battery never exceeds 100%.
* Database writes are kept to important milestones:
  * charging start
  * charging completion
  * final full battery value
* Intermediate charging battery is calculated on read through `RobotChargingService.currentBatteryStatus`.

Reassignment of remaining queued tasks added:

* After Staff completes a critical-battery robot's current mission, remaining queued missions assigned to that robot are redistributed.
* Reassignment includes only active queued missions assigned to the charging robot:
  * `PENDING`
  * `ASSIGNED`
* Reassignment excludes:
  * the mission just completed or stopped
  * `IN_PROGRESS` missions
  * `COMPLETED` missions
  * `CANCELLED` missions
  * soft-deleted missions
* Reassignment priority/order:
  * priority `1` first
  * priority `2` second
  * priority `3` third
  * oldest mission first within the same priority
  * stable `id` ordering after created time
* Existing workload-aware robot selection is reused through `RobotAssignmentService.selectRobotForMissionExcluding`.
* The charging/critical robot is excluded from reassignment candidates.
* If another available robot exists, the mission remains `ASSIGNED` and receives the new robot id/name.
* If no other robot is available, the mission becomes unassigned `PENDING` with a clear assignment reason.
* RuleEvaluator and StrategyContext are not rerun during reassignment.
* No new missions are created and no missions are duplicated.

Robot availability while charging:

* `RobotAssignmentService` excludes robots with unavailable status text such as charging, offline, maintenance, error, unavailable, disabled, or out-of-service.
* `RobotAssignmentService` also excludes robots whose robot-level `charging` flag is true, even if the status string is stale.
* Charging robots cannot receive new task assignments until charging completes.

Live Map state charging display:

* `/staff/live-map/state` includes charging state fields:
  * `charging`
  * `chargingRequired`
  * `chargingRecoveredPercent`
  * `chargingDisplayText`
  * `batteryPercent`
  * `currentPositionKey`
  * `message`
* Charging robots report:
  * `currentPositionKey = charging-station`
  * `message = Charging at Charging Station.`
  * no active pickup mission id
  * an empty active pickup route
* Battery progress is visible from the same polling endpoint.

Manager Robot Task Board impact:

* `/manager/robot-tasks` shows charging robots as `CHARGING` / unavailable.
* Reassigned queued tasks move away from the charging robot and appear under their new robots.
* Active workload counts still include only `PENDING`, `ASSIGNED`, and `IN_PROGRESS` non-deleted missions.
* Completed, cancelled, and deleted missions do not count as active workload.

Staff Missions UI impact:

* Completing a critical-battery robot's current mission shows:
  * `Remaining tasks were reassigned and robot sent to Charging Station.`
* The message also reports reassigned queued mission count and unassigned queued mission count.
* Stopping a critical-battery active mission can also send the robot to Charging Station, which is documented as the simple accepted stop/cancel edge case.
* Delete-after-stop remains soft delete only and is not part of the charging workflow.

Files changed in this task:

* `src/main/java/com/warehouse/model/Robot.java`
* `src/main/java/com/warehouse/repository/MissionRepository.java`
* `src/main/java/com/warehouse/service/RobotChargingService.java`
* `src/main/java/com/warehouse/service/RobotBatteryDrainService.java`
* `src/main/java/com/warehouse/service/RobotAssignmentService.java`
* `src/main/java/com/warehouse/service/MissionService.java`
* `src/main/java/com/warehouse/service/LiveMapStateService.java`
* `src/main/java/com/warehouse/service/ManagerRobotTaskBoardService.java`
* `src/main/java/com/warehouse/dto/LiveMapRobotStateDto.java`
* `src/main/java/com/warehouse/dto/MissionLifecycleResult.java`
* `src/main/java/com/warehouse/dto/RobotTaskGroupDto.java`
* `src/main/java/com/warehouse/controller/StaffPickupRequestController.java`
* `src/main/resources/templates/staff-missions.html`
* `src/main/resources/templates/manager-robot-tasks.html`
* `src/main/resources/static/js/staff-live-map.js`
* `src/main/resources/static/css/staff-live-map.css`
* `src/main/resources/static/css/app.css`
* `src/test/java/com/warehouse/service/RobotBatteryDrainServiceTest.java`
* `src/test/java/com/warehouse/service/RobotAssignmentServiceTest.java`
* `src/test/java/com/warehouse/controller/StaffPickupRequestControllerTest.java`
* `src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java`
* `src/test/java/com/warehouse/controller/ManagerRobotTaskBoardControllerTest.java`
* `PROJECT_STATUS.md`

Verification result:

* `node --check src/main/resources/static/js/staff-live-map.js` passed.
* `mvn test -Dtest=RobotAssignmentServiceTest` passed.
* `mvn test` passed.
* 87 tests passed.
* 0 failures.
* 0 errors.
* 0 skipped.
* Temporary app started on port `8095`.
* Temporary HTTP route verification on `http://localhost:8095` returned HTTP 200 for:
  * `/dashboard`
  * `/robots`
  * `/rules`
  * `/simulation`
  * `/system-flow`
  * `/staff/pickup-request`
  * `/staff/missions`
  * `/staff/live-map`
  * `/staff/live-map/state`
  * `/manager/policy-assignment`
  * `/manager/robot-tasks`
* `/staff/live-map/state` was verified to expose `charging`, `chargingRequired`, and `currentPositionKey`.
* Temporary port `8095` verification app processes were stopped after route checks.

Test coverage added or updated:

* Critical battery does not interrupt an `IN_PROGRESS` mission.
* Critical battery does not cancel an `IN_PROGRESS` mission.
* Critical battery does not auto-complete an `IN_PROGRESS` mission after `RETURNED_TO_BASE`.
* Staff Completed action is still required.
* Completing a returned critical-battery mission starts the charging workflow.
* Remaining queued `PENDING` / `ASSIGNED` missions are reassigned away from the charging robot.
* Reassignment respects priority and oldest-first ordering through the repository query.
* Reassignment excludes the charging robot.
* Reassignment excludes `COMPLETED`, `CANCELLED`, and soft-deleted missions.
* Charging robots are unavailable for new assignment by both `status = CHARGING` and `charging = true`.
* Charging battery recovers about `+5%` every `10` seconds.
* Charging battery never exceeds 100%.
* Fully charged robots become `IDLE` again and clear charging flags.
* `/staff/live-map/state` shows charging robots at `charging-station`.
* `/staff/live-map/state` shows charging battery progress.
* `/manager/robot-tasks` shows charging robot state and reassigned task movement.
* Existing lifecycle actions still work:
  * Completed
  * Stop
  * Delete after Stop
* Existing simulation behavior still works.

Scope confirmation:

* No WebSocket was added.
* No scheduler was added.
* No customer priority queue was added.
* No barcode scan was added.
* No complex pathfinding was added.
* No automatic mission completion was added.
* No current `IN_PROGRESS` mission interruption was added.
* No fake missions were created.
* No missions are duplicated by the charging workflow.
* `RuleParser` was not changed.
* `RuleEvaluator` was not changed.
* `RuleEngine` classes were not rewritten.
* `StrategyContext` was not changed.
* Strategy classes were not rewritten.

---

Previous completed stage:

Low Battery Behavior Without Interrupting Current Mission Added

June 3, 2026 low battery behavior update:

This task added only low-battery warning behavior and display state. It did not add charging station simulation, forced return to Charging Station, task redistribution, mission interruption, mission auto-complete behavior, WebSocket behavior, scheduler behavior, barcode scanning, customer priority queues, complex queue logic, or changes to the Interpreter Pattern / Strategy Pattern core.

Battery warning thresholds added:

* Effective battery is calculated from the Task 1 live battery drain value before warning thresholds are evaluated.
* `battery >= 20` -> `NORMAL`
* `battery < 20` and `battery > 5` -> `LOW`
* `battery <= 5` -> `CRITICAL`
* `LOW` means Energy Saving behavior is shown as active.
* `CRITICAL` means Charging Required after the current mission.
* `CRITICAL` also keeps Energy Saving mode visible because it is still a low-battery condition.

Low battery behavior added:

* Robots with effective battery below 20% and above 5% are marked as low battery.
* Low battery robots expose Energy Saving display state.
* The message shown for this state is `Energy saving mode active.`
* No mission is stopped, cancelled, reassigned, or completed because of low battery.
* `RuleEvaluator` is not rerun continuously for this display behavior.
* `StrategyContext` is not dispatched during polling for this display behavior.

Critical battery behavior added:

* Robots with effective battery equal to or below 5% are marked as critical battery.
* Critical robots expose `chargingRequired = true`.
* The message shown for this state is `Charging required after current mission.`
* A robot already running an `IN_PROGRESS` mission continues through the existing execution flow.
* Battery critical state does not interrupt `MOVING_TO_TARGET`, `PICKING_UP`, `RETURNING_TO_BASE`, or `RETURNED_TO_BASE`.
* `RETURNED_TO_BASE` still does not auto-complete the mission.
* Staff must still manually click Completed when the mission is truly finished.

Live Map state endpoint update:

* `/staff/live-map/state` robot JSON now includes:
  * `batteryWarningLevel`
  * `lowBattery`
  * `criticalBattery`
  * `energySavingMode`
  * `chargingRequired`
  * `batteryMessage`
* Existing battery fields remain available:
  * `batteryLevel`
  * `batteryPercent`
  * `batteryDrainPercent`
  * `batteryDisplayText`
* Active mission robots use effective battery after route drain.
* Robots without active missions use stored robot battery with no route drain.

UI updates:

* Staff Live Map selected robot Mission Flow now shows compact battery warning badges and message text.
* Staff Live Map can show battery state even when the selected robot has no active mission.
* Manager Robot Task Board now shows low battery / critical battery indicators from stored robot battery.
* Manager Robot Task Board does not count low or critical robots as unavailable in this task.
* Manager Robot Task Board does not use active route-drain integration; it stays simple and uses stored robot battery.

Files changed in this task:

* `src/main/java/com/warehouse/service/RobotBatteryDrainService.java`
* `src/main/java/com/warehouse/dto/LiveMapRobotStateDto.java`
* `src/main/java/com/warehouse/service/LiveMapStateService.java`
* `src/main/java/com/warehouse/dto/RobotTaskGroupDto.java`
* `src/main/java/com/warehouse/service/ManagerRobotTaskBoardService.java`
* `src/main/resources/templates/manager-robot-tasks.html`
* `src/main/resources/static/css/app.css`
* `src/main/resources/static/js/staff-live-map.js`
* `src/main/resources/static/css/staff-live-map.css`
* `src/test/java/com/warehouse/service/RobotBatteryDrainServiceTest.java`
* `src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java`
* `src/test/java/com/warehouse/controller/ManagerRobotTaskBoardControllerTest.java`
* `PROJECT_STATUS.md`

Verification result:

* `node --check src/main/resources/static/js/staff-live-map.js` passed.
* `mvn test` passed.
* 82 tests passed.
* 0 failures.
* 0 errors.
* 0 skipped.
* Temporary app started on port `8095`.
* Temporary HTTP route verification on `http://localhost:8095` returned HTTP 200 for:
  * `/dashboard`
  * `/robots`
  * `/rules`
  * `/simulation`
  * `/system-flow`
  * `/staff/pickup-request`
  * `/staff/missions`
  * `/staff/live-map`
  * `/staff/live-map/state`
  * `/manager/policy-assignment`
  * `/manager/robot-tasks`
* `/staff/live-map/state` JSON was verified to expose `batteryWarningLevel`, `lowBattery`, `criticalBattery`, `energySavingMode`, `chargingRequired`, and `batteryMessage`.
* A live state sample showed `Mover Beta` at 15% with `batteryWarningLevel = LOW`, `energySavingMode = true`, and `chargingRequired = false`.
* The temporary port `8095` verification app process was stopped after route checks.

Test coverage added or updated:

* 25% battery resolves to `NORMAL`.
* 19% battery resolves to `LOW`.
* 6% battery resolves to `LOW`.
* 5% battery resolves to `CRITICAL` with `chargingRequired = true`.
* 0% battery resolves to `CRITICAL` with `chargingRequired = true`.
* LOW battery does not stop mission progression.
* CRITICAL battery does not interrupt an `IN_PROGRESS` mission.
* CRITICAL battery does not cancel a mission.
* CRITICAL battery does not auto-complete a mission.
* `/staff/live-map/state` includes low-battery fields.
* `/staff/live-map/state` shows the Energy Saving message for LOW battery.
* `/staff/live-map/state` shows the Charging Required after current mission message for CRITICAL battery.
* Mission state can still report `RETURNED_TO_BASE` while battery is critical.
* Existing manual Completed, Stop, and Delete-after-Stop lifecycle behavior remains covered by the existing controller tests.
* Manager Robot Task Board still loads and displays low / critical battery indicators.

Scope confirmation:

* No charging simulation was added.
* No forced return to Charging Station was added.
* No task redistribution was added.
* No current mission interruption was added.
* No mission cancellation due to low battery was added.
* No automatic mission completion was added.
* No WebSocket was added.
* No scheduler was added.
* No barcode scan was added.
* No customer priority queue was added.
* No complex queue logic was added.
* `RuleParser` was not changed.
* `RuleEvaluator` was not changed.
* `RuleEngine` classes were not rewritten.
* `StrategyContext` was not changed.
* Strategy classes were not changed.
* Existing simulation behavior was not changed.

What remains planned next:

* Add Charging Station Simulation And Reassign Remaining Tasks.

---

Previous completed stage:

Battery Drain During Mission Execution Added

June 3, 2026 battery drain during mission execution update:

This task added only battery drain data for active mission execution. It did not add low-battery behavior, forced charging, charging simulation, task redistribution, mission auto-complete behavior, WebSocket behavior, scheduler behavior, barcode scanning, customer priority queues, complex queue logic, or changes to the Interpreter Pattern / Strategy Pattern core.

Battery drain behavior added:

* `MissionExecutionProgressDto` now carries `traveledWaypointCount`, calculated from completed backend route waypoints.
* The starting `base-station` waypoint is not counted as traveled distance.
* Continuous segment progress is still used for movement, but battery drain is based only on completed waypoint count for this task.
* Battery drain formula:
  * `batteryDrainPercent = floor(traveledWaypointCount / 10)`
  * 0-9 traveled waypoints -> 0% drain
  * 10-19 traveled waypoints -> 1% drain
  * 20-29 traveled waypoints -> 2% drain
* Effective battery formula:
  * `effectiveBattery = max(0, clamp(robot.battery, 0, 100) - batteryDrainPercent)`
* Battery is never displayed below 0%.
* Battery is never displayed above 100%.

Battery calculation and persistence decision:

* `RobotBatteryDrainService` was added as a small focused service for battery drain math.
* Stored robot battery remains the base value from the `Robot` entity.
* `/staff/live-map/state` calculates effective live battery from the active mission route progress.
* Battery is not persisted during `/staff/live-map/state` polling.
* No per-second database writes were added.
* Manager Robot Task Board still shows stored robot battery because this task keeps drain as effective live-map state only.

Live Map state endpoint update:

* `/staff/live-map/state` robot JSON now includes:
  * `batteryLevel`
  * `batteryPercent`
  * `batteryDrainPercent`
  * `batteryDisplayText`
* Robots with active `IN_PROGRESS` execution and `executionStartedAt` show effective battery after route drain.
* Robots without active missions show stored battery with `batteryDrainPercent = 0`.

Live Map UI update:

* The selected robot Mission Flow panel now shows a concise Battery line.
* The battery line uses the backend `batteryDisplayText`, including route drain only when drain is greater than 0.
* No large battery table or extra mission UI was added.

Files changed in this task:

* `src/main/java/com/warehouse/dto/MissionExecutionProgressDto.java`
* `src/main/java/com/warehouse/dto/LiveMapRobotStateDto.java`
* `src/main/java/com/warehouse/service/MissionExecutionProgressService.java`
* `src/main/java/com/warehouse/service/LiveMapStateService.java`
* `src/main/java/com/warehouse/service/RobotBatteryDrainService.java`
* `src/main/resources/static/js/staff-live-map.js`
* `src/main/resources/static/css/staff-live-map.css`
* `src/test/java/com/warehouse/service/MissionExecutionProgressServiceTest.java`
* `src/test/java/com/warehouse/service/RobotBatteryDrainServiceTest.java`
* `src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java`
* `PROJECT_STATUS.md`

Verification result:

* `node --check src/main/resources/static/js/staff-live-map.js` passed.
* `mvn test` passed.
* 80 tests passed.
* 0 failures.
* 0 errors.
* 0 skipped.
* Temporary app started on port `8095`.
* Temporary HTTP route verification on `http://localhost:8095` returned HTTP 200 for:
  * `/dashboard`
  * `/robots`
  * `/rules`
  * `/simulation`
  * `/system-flow`
  * `/staff/pickup-request`
  * `/staff/missions`
  * `/staff/live-map`
  * `/staff/live-map/state`
  * `/manager/policy-assignment`
  * `/manager/robot-tasks`
* `/staff/live-map/state` JSON was verified to expose battery fields.
* A drained battery display sample was observed in state JSON: `65% battery (3% route drain)`.
* The temporary port `8095` verification app process was stopped after route checks.

Test coverage added or updated:

* 100% battery and 0 traveled waypoints remains 100%.
* 100% battery and 9 traveled waypoints remains 100%.
* 100% battery and 10 traveled waypoints becomes 99%.
* 100% battery and 20 traveled waypoints becomes 98%.
* Effective battery never goes below 0%.
* Effective battery never exceeds 100%.
* `/staff/live-map/state` includes battery data for robots.
* Robot without active mission shows stored battery and 0 drain.
* `IN_PROGRESS` mission battery reflects traveled waypoint drain.
* Live-map polling does not persist battery drain to the robot row.
* Live-map polling does not auto-complete the mission.

Scope confirmation:

* No low-battery behavior was added.
* No `battery < 20` Energy Saving behavior was added.
* No `battery <= 5` Charging Required behavior was added.
* No forced return to Charging Station was added.
* No charging simulation was added.
* No task redistribution was added.
* No automatic mission completion was added.
* No WebSocket was added.
* No scheduler was added.
* No barcode scan was added.
* No customer priority queue was added.
* No complex queue logic was added.
* `RuleParser` was not changed.
* `RuleEvaluator` was not changed.
* `RuleEngine` classes were not rewritten.
* `StrategyContext` was not changed.
* Strategy classes were not changed.
* Existing simulation behavior was not changed.

What remains planned next:

* Add Low Battery Behavior Without Interrupting Current Mission.
* Add Charging Station Simulation And Reassign Remaining Tasks.

---

Previous completed stage:

Live Map Waypoint-to-Slot Approach and Continuous Segment Interpolation Fixed

June 2, 2026 Live Map waypoint approach and segment interpolation update:

This task refined only Staff Live Map route smoothness and waypoint correctness. It did not add mission automation, WebSocket behavior, scheduler behavior, barcode scanning, customer priority queues, complex pathfinding, or changes to the Interpreter Pattern / Strategy Pattern core.

Waypoint-to-slot approach behavior fixed:

* Backend execution routes now remain row-aware inside the target zone before entering the exact slot.
* Every exact target still resolves to its actual slot:
  * `A1` through `A9`
  * `B1` through `B9`
  * `C1` through `C9`
* Every target route still includes an approach waypoint before the exact slot:
  * `A1-approach` through `A9-approach`
  * `B1-approach` through `B9-approach`
  * `C1-approach` through `C9-approach`
* Outbound row approach strategy:
  * Row 1 slots (`1-3`) use target-zone middle-left lane `main-1` -> `main-2` -> slot approach -> exact slot.
  * Row 2 slots (`4-6`) use target-zone middle-left lane `main-1` -> slot approach -> exact slot.
  * Row 3 slots (`7-9`) use target-zone entry -> slot approach -> exact slot.
* Return row approach strategy:
  * Exact slot -> same slot approach -> target-zone middle-right return lane.
  * Row 1 returns through `right-main-2` -> `right-main-1`.
  * Row 2 returns through `right-main-1`.
  * Row 3 returns directly from approach to the return exit.
* `C9` no longer routes from the Zone C middle area directly to `C9`; it now uses `zone-c-left-entry` -> `C9-approach` -> exact `C9`.
* `A3` now routes through `zone-a-left-main-2` -> `A3-approach` -> exact `A3`, then returns through `A3-approach` -> `zone-a-right-main-2`.
* The frontend Visual Route Preview now uses the same row-aware route shape as the backend.
* Approach waypoint key normalization was fixed so keys like `A3-approach` and `C9-approach` resolve to real route waypoints instead of falling back to the exact package slot.

Continuous segment interpolation added:

* `/staff/live-map/state` robot JSON now includes:
  * `currentPositionKey`
  * `nextPositionKey`
  * `segmentProgress`
* `segmentProgress` is calculated from elapsed backend time as a `0.0` to `1.0` value between the current waypoint and the next waypoint.
* The frontend now resolves `currentPositionKey` and `nextPositionKey` in full warehouse route coordinates, interpolates by `segmentProgress`, then converts the result back to the rendered zone coordinate.
* This prevents the robot from waiting visually at a waypoint for the full segment duration and then jumping to the next waypoint.
* Frontend requestAnimationFrame smoothing remains active and now smooths between polled segment samples:
  * `ROBOT_VISUAL_SEGMENT_SAMPLE_DURATION_MS = 900`
  * Poll interval remains `LIVE_MAP_STATE_POLL_INTERVAL_MS = 1000`.
* Selected robot mode and Show All Robots continue to use the same backend-driven robot state path.

Slow smooth movement timing updated:

* Backend time-based progression constants are now:
  * `NORMAL_WAYPOINT_SECONDS = 15`
  * `BRIDGE_WAYPOINT_SECONDS = 12`
  * `PICKUP_PAUSE_SECONDS = 5`
* Normal waypoint-to-waypoint movement is now slower and easier to follow.
* Bridge/connector movement is now slower and continuously interpolated.
* Pickup pause remains visible and read-only.
* `RETURNED_TO_BASE` still does not auto-complete the mission; Staff must still manually click Completed.

Lane and bridge behavior confirmation:

* Outbound lane remains the middle-left travel column, column 2 from the left.
* Return lane remains the middle-right travel column, column 3 from the left / 2nd from the right.
* No outbound travel was moved onto the return lane.
* No return travel was moved onto the outbound lane.
* No new lane/bridge waiting feature was added.
* Existing simple read-only bridge waiting was only made segment-aware so it can inspect the current waypoint or next waypoint for the active moving segment.
* No scheduler, database route reservation, path reservation, or background bridge controller was added.

Files changed in this task:

* `src/main/java/com/warehouse/dto/MissionExecutionProgressDto.java`
* `src/main/java/com/warehouse/dto/LiveMapRobotStateDto.java`
* `src/main/java/com/warehouse/service/WarehouseRouteService.java`
* `src/main/java/com/warehouse/service/MissionExecutionProgressService.java`
* `src/main/java/com/warehouse/service/LiveMapStateService.java`
* `src/main/resources/static/js/staff-live-map.js`
* `src/test/java/com/warehouse/service/WarehouseRouteServiceTest.java`
* `src/test/java/com/warehouse/service/MissionExecutionProgressServiceTest.java`
* `src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java`
* `PROJECT_STATUS.md`

Verification result:

* `node --check src/main/resources/static/js/staff-live-map.js` passed.
* `mvn test` passed.
* 76 tests passed.
* 0 failures.
* 0 errors.
* 0 skipped.
* Local app started on port `8095`.
* Temporary HTTP route verification on `http://localhost:8095` returned HTTP 200 for:
  * `/dashboard`
  * `/robots`
  * `/rules`
  * `/simulation`
  * `/system-flow`
  * `/staff/pickup-request`
  * `/staff/missions`
  * `/staff/live-map`
  * `/staff/live-map/state`
  * `/manager/policy-assignment`
  * `/manager/robot-tasks`
* `/staff/live-map/state` JSON was verified to expose `currentPositionKey`, `nextPositionKey`, and `segmentProgress`.

Scope confirmation:

* No WebSocket was added.
* No scheduler was added.
* No auto-complete behavior was added.
* No automatic mission `COMPLETED` transition was added.
* No barcode scan was added.
* No customer priority queue was added.
* No complex pathfinding was added.
* No random alternate route selection was added.
* No database schema change was added.
* `RuleParser` was not changed.
* `RuleEvaluator` was not changed.
* `RuleEngine` classes were not rewritten.
* `StrategyContext` was not changed.
* Strategy classes were not changed.

What remains planned next:

* Browser demo verification with real A3, B5, C5, and C9 missions.
* Screenshot/video capture for final presentation if needed.

---

Previous completed stage:

Live Map Lane Alignment and Slow Smooth Movement Polished

June 2, 2026 Live Map visual route polish update:

This task refined only the Staff Live Map route visual quality and backend-driven movement smoothness. It did not add new mission automation or change the Interpreter Pattern / Strategy Pattern core.

Lane alignment polished:

* Outbound movement still uses the existing backend `left` route key vocabulary, but the Live Map now renders those outbound route points on the middle-left travel column:
  * 2nd column from the left.
  * `OUTBOUND_COLUMN_LEFT = 36.67`.
* Return movement still uses the existing backend `right` route key vocabulary, and the Live Map renders those return route points on the middle-right travel column:
  * 3rd column from the left.
  * 2nd column from the right.
  * `RETURN_COLUMN_LEFT = 63.33`.
* Zone C, Zone B, and Zone A outbound waypoints now visually align to the 2nd column from the left instead of the far-left route column.
* Zone A, Zone B, and Zone C return waypoints remain aligned to the 3rd column from the left instead of the far-right route column.
* Bridge route dots for outbound C-B and B-A movement now connect through the same middle-left travel column.
* Bridge route dots for return A-B and B-C movement continue to connect through the middle-right travel column.

Target approach waypoint behavior added:

* Backend routes now insert target approach waypoint keys before exact pickup slots:
  * `A1-approach` through `A9-approach`
  * `B1-approach` through `B9-approach`
  * `C1-approach` through `C9-approach`
* Backend routes also include the same approach waypoint after pickup before returning to the return lane.
* Exact target slots remain exact:
  * `A3` still resolves to exact `A3`.
  * `B5` still resolves to exact `B5`.
  * `C5` still resolves to exact `C5`.
* The frontend computes approach coordinates near the exact target slot from the outbound travel column, so the robot moves from lane -> nearby approach point -> exact package slot instead of snapping sideways from a distant slot.
* The optional Visual Route Preview now uses the same approach waypoint behavior.

Slow smooth movement timing:

* Backend time-based progression now uses easy-to-adjust constants:
  * `NORMAL_WAYPOINT_SECONDS = 12`
  * `BRIDGE_WAYPOINT_SECONDS = 10`
  * `PICKUP_PAUSE_SECONDS = 4`
* Bridge segment duration is selected when either side of the segment is a `bridge-*` waypoint.
* Backend progression remains read-only for `/staff/live-map/state`.
* The frontend keeps the existing polling model and does not add WebSocket behavior.
* Frontend marker smoothing now uses a requestAnimationFrame loop over stored visual position state:
  * normal visual movement duration: `12000ms`
  * bridge visual movement duration: `10000ms`
  * easing: linear for readable robot motion
* The smoothing loop avoids competing top/left transitions while the robot is moving, reducing flicker and repeated animation restarts from polling.

Files changed in this task:

* `src/main/java/com/warehouse/service/WarehouseRouteService.java`
* `src/main/java/com/warehouse/service/MissionExecutionProgressService.java`
* `src/main/resources/static/js/staff-live-map.js`
* `src/main/resources/static/css/staff-live-map.css`
* `src/test/java/com/warehouse/service/WarehouseRouteServiceTest.java`
* `src/test/java/com/warehouse/service/MissionExecutionProgressServiceTest.java`
* `src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java`
* `PROJECT_STATUS.md`

Verification result:

* `node --check src/main/resources/static/js/staff-live-map.js` passed.
* `mvn test` passed.
* 75 tests passed.
* 0 failures.
* 0 errors.
* 0 skipped.
* Temporary app route verification on port `8095` returned HTTP 200 for:
  * `/dashboard`
  * `/robots`
  * `/rules`
  * `/simulation`
  * `/system-flow`
  * `/staff/pickup-request`
  * `/staff/missions`
  * `/staff/live-map`
  * `/staff/live-map/state`
  * `/manager/policy-assignment`
  * `/manager/robot-tasks`
* Route payload coverage verifies A/B/C target routes include approach waypoints and exact target slots.

Scope confirmation:

* Existing lane/bridge waiting behavior was not expanded beyond the previous read-only bridge occupancy response behavior.
* No WebSocket was added.
* No scheduler was added.
* No auto-complete behavior was added.
* No automatic mission `COMPLETED` transition was added.
* No barcode scan was added.
* No customer priority queue was added.
* No complex pathfinding was added.
* No random alternate route selection was added.
* No database schema change was added.
* `RuleParser` was not changed.
* `RuleEvaluator` was not changed.
* `RuleEngine` classes were not rewritten.
* `StrategyContext` was not changed.
* Strategy classes were not changed.

What remains planned next:

* Browser-based final demo verification for A3, B5, and C5 visual movement.
* Screenshot/video capture for final presentation if needed.

---

Previous completed stage:

Lane-Based Live Map Route Logic and Simple Bridge Waiting Added

June 2, 2026 lane-based Live Map route update:

This task refined the backend-driven Live Map route model so outbound movement uses explicit left-lane waypoint keys and return movement uses explicit right-lane waypoint keys. It also added a simple read-only bridge/path waiting rule for `/staff/live-map/state` so a following robot waits at the previous safe waypoint when another in-progress robot is occupying the same critical bridge segment.

Lane route rules added:

* Outbound/upward movement always uses the left lane.
* Return/downward movement always uses the right lane.
* The backend route service no longer uses generic bridge keys for new calculated execution routes.
* Exact target slots remain unchanged:
  * `A1` means `A1`.
  * `B5` means `B5`.
  * `C5` means `C5`.

Zone route examples:

* Zone C target route:
  * `base-station` -> `zone-c-left-entry` -> `zone-c-left-main-1` -> exact `C` slot
  * exact `C` slot -> `zone-c-right-main-1` -> `zone-c-right-exit` -> `base-station`
* Zone B target route:
  * `base-station` -> Zone C left lane -> `bridge-c-b-left-*` -> Zone B left lane -> exact `B` slot
  * exact `B` slot -> Zone B right lane -> `bridge-b-c-right-*` -> Zone C right lane -> `base-station`
* Zone A target route:
  * `base-station` -> Zone C left lane -> `bridge-c-b-left-*` -> Zone B left lane -> `bridge-b-a-left-*` -> Zone A left lane -> exact `A` slot
  * exact `A` slot -> Zone A right lane -> `bridge-a-b-right-*` -> Zone B right lane -> `bridge-b-c-right-*` -> Zone C right lane -> `base-station`

Simple bridge/path waiting behavior:

* Critical bridge segments are tracked by waypoint key:
  * `bridge-c-b-left`
  * `bridge-b-a-left`
  * `bridge-a-b-right`
  * `bridge-b-c-right`
* `/staff/live-map/state` calculates each active robot progress from the existing time-based route service.
* If another in-progress robot is already on the same critical bridge segment, the following robot reports:
  * `waiting = true`
  * `blockedSegment` set to the occupied bridge segment
  * `currentPositionKey` set to the previous safe waypoint
  * `message = Waiting for bridge path to clear.`
* Waiting does not change mission status, choose another path, reassign a robot, or complete a mission.
* Waiting is calculated for the state response and does not add a scheduler or per-second database writes.

Live Map frontend updates:

* The fullscreen Live Map still polls `/staff/live-map/state`.
* Marker placement still uses backend `currentPositionKey` as the source of truth.
* New left/right lane waypoint keys are mapped to visible route waypoints.
* Smooth marker movement from the previous task remains active.
* Mission Flow can show `Waiting for bridge path to clear.` while keeping the correct execution phase highlighted.
* Selected robot mode and Show All Robots behavior remain preserved.

Files changed in this task:

* `src/main/java/com/warehouse/service/WarehouseRouteService.java`
* `src/main/java/com/warehouse/service/LiveMapStateService.java`
* `src/main/java/com/warehouse/dto/MissionExecutionProgressDto.java`
* `src/main/java/com/warehouse/dto/LiveMapRobotStateDto.java`
* `src/main/resources/static/js/staff-live-map.js`
* `src/test/java/com/warehouse/service/WarehouseRouteServiceTest.java`
* `src/test/java/com/warehouse/service/MissionExecutionProgressServiceTest.java`
* `src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java`
* `PROJECT_STATUS.md`

Verification result:

* `node --check src/main/resources/static/js/staff-live-map.js` passed.
* `mvn test` passed.
* 73 tests passed.
* 0 failures.
* 0 errors.
* 0 skipped.

Existing route coverage remains in the test suite for:

* `/dashboard`
* `/robots`
* `/rules`
* `/simulation`
* `/system-flow`
* `/staff/pickup-request`
* `/staff/missions`
* `/staff/live-map`
* `/staff/live-map/state`
* `/manager/policy-assignment`
* `/manager/robot-tasks`

Scope confirmation:

* No WebSocket was added.
* No scheduler was added.
* No auto-complete behavior was added.
* No automatic mission `COMPLETED` transition was added.
* No barcode scan was added.
* No customer priority queue was added.
* No complex pathfinding was added.
* No random alternate route selection was added.
* Outbound travel is not routed onto the return lane.
* Return travel is not routed onto the outbound lane.
* No database schema change was added.
* `RuleParser` was not changed.
* `RuleEvaluator` was not changed.
* `RuleEngine` classes were not rewritten.
* `StrategyContext` was not changed.
* Strategy classes were not changed.

What remains planned next:

* Live Map final polish.
* Full demo verification.
* Screenshot/video capture.

---

Previous completed stage:

Live Map Frontend Polling Added

June 2, 2026 Live Map frontend polling update:

This task connected the fullscreen Staff Live Map frontend to the backend Live Map state endpoint. The page now follows backend mission execution state through simple polling and updates robot marker positions and selected-robot Mission Flow from `/staff/live-map/state`. This task does not complete missions automatically and does not add WebSocket or scheduler behavior.

Polling behavior added:

* `staff-live-map.js` now polls `GET /staff/live-map/state`.
* Polling interval:
  * `LIVE_MAP_STATE_POLL_INTERVAL_MS = 1000`
* Polling starts when `/staff/live-map` loads.
* The polling timer is cleared on `beforeunload`.
* If a polling request fails:
  * The page logs a non-blocking console warning.
  * Last known robot marker positions remain on screen.
  * The next interval retries automatically.

Robot marker position behavior:

* Each backend robot state is mapped to the existing Live Map robot keys:
  * Picker Alpha / Alpha / green -> Picker Alpha marker
  * Mover Beta / Beta / red -> Mover Beta marker
  * Carrier Gamma / Gamma / blue -> Carrier Gamma marker
* `currentPositionKey` is resolved to exact map anchors:
  * `base-station` maps to Base Station.
  * Route waypoint keys such as `zone-c-entry`, `bridge-c-b-1`, `zone-b-entry`, and return aliases map to existing route-dot positions.
  * Exact slot keys such as `A1`, `B5`, and `C5` map to the matching package slot coordinates.
* Exact slot mapping is preserved:
  * `A1` maps to `A1`.
  * `B5` maps to `B5`, not another B slot.
  * `C5` maps to `C5`, not another C slot.
* Backend return route aliases were added to the frontend waypoint map:
  * `bridge-b-c-1`
  * `bridge-b-c-2`
  * `bridge-a-b-1`
  * `bridge-a-b-2`
* The frontend continues to use the existing `data-route-point` and `data-location` warehouse mapping style.

Mission Flow behavior:

* When a robot is selected, the Mission Flow card is rebuilt from the latest backend state.
* `executionStep` now drives the highlighted flow step:
  * `NOT_STARTED` -> Assigned Mission
  * `MOVING_TO_TARGET` -> Move to Target Zone
  * `PICKING_UP` -> Pick up Cargo
  * `RETURNING_TO_BASE` -> Return to Base Station
  * `RETURNED_TO_BASE` -> Return to Base Station with manual-completion message
* For `RETURNED_TO_BASE`, the selected robot message is:
  * `Robot returned to Base Station. Waiting for Staff to confirm Completed.`
* For robots without active work:
  * The selected card shows `No active pickup mission assigned.`
  * No fake mission is shown.
  * No route animation is started.

Selected robot and Show All behavior:

* Selected robot mode remains preserved.
* When a robot is selected:
  * Only the selected robot marker is rendered.
  * Polling updates that selected robot's backend position.
  * Other robot markers remain hidden.
* Show All Robots still:
  * Clears selected robot state.
  * Shows all robot markers again.
  * Keeps polling active.
  * Resets the Mission Flow instruction to `Select a robot to view its current pickup flow.`

Old frontend-only route animation behavior:

* The old button is now presented as `Visual Route Preview`.
* The visual preview remains available only for a selected robot with an assigned target and no active backend execution.
* When backend execution is active, the button is disabled and shows `Following Backend State`.
* Starting visual preview while backend execution is active is blocked with:
  * `Live Map is following backend execution state.`
* The visual preview still does not change backend mission status.

Files changed in this task:

* `src/main/resources/static/js/staff-live-map.js`
* `src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java`
* `PROJECT_STATUS.md`

Verification result:

* `node --check src/main/resources/static/js/staff-live-map.js` passed.
* `mvn test` passed.
* 72 tests passed.
* 0 failures.
* 0 errors.
* 0 skipped.

Existing route coverage remains in the test suite for:

* `/dashboard`
* `/robots`
* `/rules`
* `/simulation`
* `/system-flow`
* `/staff/pickup-request`
* `/staff/missions`
* `/staff/live-map`
* `/staff/live-map/state`
* `/manager/policy-assignment`
* `/manager/robot-tasks`

What remains planned next:

* Returned-to-base manual Completed flow refinement if needed.
* Final Live Map polish.
* Demo verification.

Scope confirmation:

* No WebSocket was added.
* No scheduler was added.
* No auto-complete behavior was added.
* No automatic mission `COMPLETED` transition was added.
* No barcode scan was added.
* No customer priority queue was added.
* No task interruption logic was added.
* No complex queue logic was added.
* No database schema change was added.
* `RuleParser` was not changed.
* `RuleEvaluator` was not changed.
* `RuleEngine` classes were not rewritten.
* `StrategyContext` was not changed.
* Strategy classes were not changed.

---

Previous completed stage:

Backend Time-Based Route Progression Added

June 2, 2026 backend time-based route progression update:

This task added backend execution progress calculation for lightweight Live Map simulation. The backend can now calculate a robot's current route position and execution step from `executionStartedAt`, current time, and the deterministic route generated by `WarehouseRouteService`. This task still does not move the frontend by polling and does not complete missions automatically.

New progression DTO added:

* `MissionExecutionProgressDto`
* Fields:
  * `positionKey`
  * `executionStep`
  * `phase`
  * `message`
  * `elapsedSeconds`

New progression service added:

* `MissionExecutionProgressService`
* Purpose:
  * Calculate current execution progress for an `IN_PROGRESS` mission.
  * Use existing route waypoints from `WarehouseRouteService`.
  * Derive the current waypoint and `MissionExecutionStep` from elapsed time.
  * Avoid database writes on each state request.

Timing model used:

* `SECONDS_PER_WAYPOINT = 1`
* `PICKUP_PAUSE_SECONDS = 2`
* Elapsed time is calculated from:
  * `executionStartedAt`
  * current backend time
* The route is not persisted.
* The route is recalculated on demand from the mission target.

Execution step calculation:

* Before the target waypoint is reached:
  * `executionStep = MOVING_TO_TARGET`
  * message: `Moving to target location.`
* During the pickup pause at the target waypoint:
  * `executionStep = PICKING_UP`
  * message: `Picking up cargo.`
* After pickup pause while moving back to Base Station:
  * `executionStep = RETURNING_TO_BASE`
  * message: `Returning to Base Station.`
* After the final Base Station route time:
  * `executionStep = RETURNED_TO_BASE`
  * message: `Returned to Base Station. Waiting for Staff to confirm completion.`

Current position calculation:

* The calculated `currentPositionKey` is selected from the backend route waypoint list.
* At elapsed time 0, the position is `base-station`.
* Zone C targets progress through Base Station -> Zone C -> exact C target -> Zone C -> Base Station.
* Zone B targets progress through Base Station -> Zone C -> C-B bridge -> Zone B -> exact B target -> Zone B -> Zone C -> Base Station.
* Zone A targets progress through Base Station -> Zone C -> Zone B -> B-A bridge -> Zone A -> exact A target -> Zone A -> Zone B -> Zone C -> Base Station.

Persistence behavior:

* Progress is calculated virtually for `/staff/live-map/state`.
* `pickupReachedAt` is not persisted by this task.
* `returnedAt` is not persisted by this task.
* `currentPositionKey` in the mission row is not updated on each state request.
* Stored `executionStep` in the mission row is not updated on each state request.
* This avoids scheduler-style database writes.

Effect on `/staff/live-map/state`:

* The endpoint now returns calculated progress for `IN_PROGRESS` missions that have `executionStartedAt`.
* The JSON `currentPositionKey` reflects the calculated route position.
* The JSON `executionStep` reflects calculated time-based state.
* The JSON `message` reflects the calculated phase.
* The JSON `status` remains the stored mission status.
* A returned-to-base mission still returns `status = IN_PROGRESS` until Staff manually clicks Completed.
* `ASSIGNED` or assigned `PENDING` missions still return a safe not-started snapshot.
* Missions without `executionStartedAt` keep the stored execution snapshot for compatibility with existing rows/tests.

Start Execution behavior remains unchanged:

* Start Execution still sets:
  * `status = IN_PROGRESS`
  * `executionStep = MOVING_TO_TARGET`
  * `executionStartedAt = current timestamp`
  * `currentPositionKey = base-station`
* Start Execution still does not auto-run the route.
* Start Execution still does not auto-complete missions.

Lifecycle interaction:

* Completed remains a manual Staff action.
* Returned-to-base does not automatically mark a mission `COMPLETED`.
* Stop/CANCELLED behavior remains unchanged.
* Cancelled missions are not active work for `/staff/live-map/state`.
* Delete-after-stop soft-deleted missions remain ignored.

Files changed in this task:

* `src/main/java/com/warehouse/dto/MissionExecutionProgressDto.java`
* `src/main/java/com/warehouse/service/MissionExecutionProgressService.java`
* `src/main/java/com/warehouse/service/LiveMapStateService.java`
* `src/test/java/com/warehouse/service/MissionExecutionProgressServiceTest.java`
* `src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java`
* `PROJECT_STATUS.md`

Verification result:

* `mvn test` passed.
* 72 tests passed.
* 0 failures.
* 0 errors.
* 0 skipped.
* `node --check src/main/resources/static/js/staff-live-map.js` passed.

Existing route coverage remains in the test suite for:

* `/dashboard`
* `/robots`
* `/rules`
* `/simulation`
* `/system-flow`
* `/staff/pickup-request`
* `/staff/missions`
* `/staff/live-map`
* `/staff/live-map/state`
* `/manager/policy-assignment`
* `/manager/robot-tasks`

What remains planned next:

* Frontend polling to update Live Map from `/staff/live-map/state`
* Returned-to-base manual Completed flow refinement if needed

Scope confirmation:

* No frontend polling was added.
* No WebSocket was added.
* No scheduler was added.
* No automatic database write loop was added.
* No auto-complete behavior was added.
* No automatic mission `COMPLETED` transition was added.
* No Live Map frontend rewrite was added.
* No Live Map route animation rewrite was added.
* No barcode scan was added.
* No customer priority queue was added.
* No task interruption logic was added.
* No complex queue logic was added.
* No database schema change was added.
* `RuleParser` was not changed.
* `RuleEvaluator` was not changed.
* `RuleEngine` classes were not rewritten.
* `StrategyContext` was not changed.
* Strategy classes were not changed.

---

Previous completed stage:

Live Map State Endpoint Added

June 2, 2026 Live Map state endpoint update:

This task added the first backend JSON state endpoint for lightweight backend-driven Live Map execution simulation. The endpoint reports robot and active mission state only. It does not move robots, poll the frontend, schedule progression, or mutate mission lifecycle state.

New route added:

* `GET /staff/live-map/state`

New DTOs added:

* `LiveMapStateDto`
  * Contains the list of robot state rows returned by the endpoint.
* `LiveMapRobotStateDto`
  * Contains robot identity, display color, current mission state, target location, route, and message.
* `LiveMapRouteStepDto`
  * Converts backend `MissionRouteStep` data into JSON-friendly route waypoints.

New service added:

* `LiveMapStateService`
* Purpose:
  * Load real robots from the database.
  * Select one current mission per robot.
  * Build backend route data through `WarehouseRouteService`.
  * Return a read-only state snapshot for future Live Map polling work.

Controller update:

* `StaffLiveMapController` now exposes `GET /staff/live-map/state`.
* Existing `GET /staff/live-map` Thymeleaf rendering remains unchanged.

Current mission selection rule used by the endpoint:

* For each real database robot, the endpoint returns at most one current mission.
* `IN_PROGRESS` mission is selected first.
* If no `IN_PROGRESS` mission exists, the oldest `ASSIGNED` mission is selected.
* If no `ASSIGNED` mission exists, an assigned `PENDING` mission is selected only when it is assigned to that robot by robot id, name, or code.
* `COMPLETED` missions are ignored.
* `CANCELLED` missions are ignored.
* Soft-deleted missions are ignored.
* If a robot has an `IN_PROGRESS` mission and a newer `ASSIGNED` mission, the `IN_PROGRESS` mission is returned.

Robot state JSON behavior:

* Real robots are returned from the database.
* No fake backend robots are created.
* Robot identity includes:
  * `robotId`
  * `robotName`
  * `robotCode`
* Stable display color mapping is returned:
  * Picker Alpha / Alpha -> `green`
  * Mover Beta / Beta -> `red`
  * Carrier Gamma / Gamma -> `blue`
* Current mission state includes:
  * `missionId`
  * `requestCode`
  * `status`
  * `executionStep`
  * `currentPositionKey`
  * `targetZone`
  * `targetLocationCode`
  * `route`
  * `message`

No active mission fallback behavior:

* Robots without current active mission are still included in the `robots` array.
* `missionId`, `requestCode`, `status`, `executionStep`, `targetZone`, and `targetLocationCode` are returned as null.
* `currentPositionKey` defaults to `base-station`.
* `route` is empty.
* `message` is `No active pickup mission assigned.`

Route data behavior:

* Route waypoints are returned from `WarehouseRouteService`.
* Route data includes:
  * `positionKey`
  * `label`
  * `phase`
* Zone C routes include Base Station, Zone C waypoints, exact target slot, and return to Base Station.
* Zone B routes include Base Station, Zone C, C-B bridge, Zone B, exact target slot, and return through Zone B -> Zone C -> Base Station.
* Zone A routes include Base Station, Zone C, Zone B, B-A bridge, Zone A, exact target slot, and return through Zone A -> Zone B -> Zone C -> Base Station.
* Exact target location code is preserved, such as `A1`, `B5`, or `C5`.

Read-only behavior confirmation:

* `GET /staff/live-map/state` does not change mission status.
* It does not update `currentPositionKey`.
* It does not advance route position.
* It does not set `pickupReachedAt`.
* It does not set `returnedAt`.
* It does not mark missions completed.
* It does not change robot assignment.
* It does not create mission history rows.

Files changed in this task:

* `src/main/java/com/warehouse/dto/LiveMapStateDto.java`
* `src/main/java/com/warehouse/dto/LiveMapRobotStateDto.java`
* `src/main/java/com/warehouse/dto/LiveMapRouteStepDto.java`
* `src/main/java/com/warehouse/service/LiveMapStateService.java`
* `src/main/java/com/warehouse/controller/StaffLiveMapController.java`
* `src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java`
* `PROJECT_STATUS.md`

Verification result:

* `mvn test` passed.
* 64 tests passed.
* 0 failures.
* 0 errors.
* 0 skipped.
* `node --check src/main/resources/static/js/staff-live-map.js` passed.

Existing route coverage remains in the test suite for:

* `/dashboard`
* `/robots`
* `/rules`
* `/simulation`
* `/system-flow`
* `/staff/pickup-request`
* `/staff/missions`
* `/staff/live-map`
* `/staff/live-map/state`
* `/manager/policy-assignment`
* `/manager/robot-tasks`

What remains planned next:

* Backend time-based route progression
* Frontend polling to update Live Map from `/staff/live-map/state`
* Returned-to-base manual Completed flow

Scope confirmation:

* No frontend polling was added.
* No WebSocket was added.
* No scheduler was added.
* No automatic route progression was added.
* No auto-complete behavior was added.
* No automatic `RETURNED_TO_BASE` behavior was added.
* No Live Map frontend rewrite was added.
* No Live Map route animation rewrite was added.
* No barcode scan was added.
* No customer priority queue was added.
* No task interruption logic was added.
* No complex queue logic was added.
* No database schema change was added.
* `RuleParser` was not changed.
* `RuleEvaluator` was not changed.
* `RuleEngine` classes were not rewritten.
* `StrategyContext` was not changed.
* Strategy classes were not changed.

---

Previous completed stage:

Backend Mission Route Calculation Added

June 2, 2026 backend mission route calculation update:

This task added deterministic backend route calculation for mission execution simulation. The route dots are now represented in backend service logic as real waypoint keys for future Live Map state and route progression work. This task only calculates routes on demand; it does not move robots yet.

Route service added:

* `WarehouseRouteService`
* Purpose:
  * Build deterministic warehouse execution routes from a mission target.
  * Validate `locationCode` values before execution can start.
  * Keep route logic explicit and simple instead of using pathfinding.

Route step DTO added:

* `MissionRouteStep`
* Fields:
  * `positionKey`
  * `label`
  * `phase`
* Supported phases:
  * `MOVE_TO_TARGET`
  * `PICKUP`
  * `RETURN_TO_BASE`

Route methods added:

* `buildExecutionRoute(Mission mission)`
* `buildExecutionRoute(String zone, String locationCode)`
* `buildOutboundRoute(String locationCode)`
* `buildReturnRoute(String locationCode)`

Backend route behavior:

* Base Station is the shared start and end point.
* Zone C is always nearest to Base Station.
* Zone B is reached through Zone C.
* Zone A is reached through Zone C, then Zone B.
* Return route is built as the warehouse reverse path back to Base Station.
* Exact target slot is preserved, so `A1`, `B5`, and `C5` remain exact target waypoints.

Zone C route behavior:

* Outbound route uses:
  * `base-station`
  * `zone-c-entry`
  * `zone-c-main-1`
  * exact target slot such as `C5`
* Return route uses:
  * exact target slot such as `C5`
  * `zone-c-main-1`
  * `zone-c-entry`
  * `base-station`

Zone B route behavior:

* Outbound route uses:
  * `base-station`
  * Zone C entry and main waypoints
  * `bridge-c-b-1`
  * `bridge-c-b-2`
  * Zone B entry and main waypoint
  * exact target slot such as `B5`
* Return route uses:
  * exact target slot such as `B5`
  * Zone B main and entry waypoints
  * `bridge-b-c-1`
  * `bridge-b-c-2`
  * Zone C main waypoints reversed
  * `zone-c-entry`
  * `base-station`

Zone A route behavior:

* Outbound route uses:
  * `base-station`
  * Zone C entry and main waypoints
  * `bridge-c-b-1`
  * `bridge-c-b-2`
  * Zone B entry and main waypoints
  * `bridge-b-a-1`
  * `bridge-b-a-2`
  * Zone A entry and main waypoint
  * exact target slot such as `A1`
* Return route uses:
  * exact target slot such as `A1`
  * Zone A main and entry waypoints
  * `bridge-a-b-1`
  * `bridge-a-b-2`
  * Zone B main and entry waypoints
  * `bridge-b-c-1`
  * `bridge-b-c-2`
  * Zone C main waypoints reversed
  * `zone-c-entry`
  * `base-station`

Explicit warehouse route order:

```text
Base Station -> Zone C -> Zone B -> Zone A
```

Start Execution integration:

* `MissionService.startExecution(id)` now calls `WarehouseRouteService.buildExecutionRoute(mission)` before changing mission state.
* This validates the mission target route before setting:
  * `status = IN_PROGRESS`
  * `executionStep = MOVING_TO_TARGET`
  * `executionStartedAt = current timestamp`
  * `currentPositionKey = base-station`
* Start Execution still does not advance route position.
* Start Execution still does not auto-complete missions.
* The full calculated route is not persisted in the database.

Route validation:

* Empty `locationCode` is rejected.
* Invalid target prefixes are rejected.
* Invalid target slots outside `A1-A9`, `B1-B9`, or `C1-C9` are rejected.
* Zone and location mismatch is rejected, for example `Zone B` with `A1`.

Files changed in this task:

* `src/main/java/com/warehouse/dto/MissionRouteStep.java`
* `src/main/java/com/warehouse/service/WarehouseRouteService.java`
* `src/main/java/com/warehouse/service/MissionService.java`
* `src/test/java/com/warehouse/service/WarehouseRouteServiceTest.java`
* `src/test/java/com/warehouse/controller/StaffPickupRequestControllerTest.java`
* `PROJECT_STATUS.md`

Verification result:

* `mvn test` passed.
* 58 tests passed.
* 0 failures.
* 0 errors.
* 0 skipped.
* `node --check src/main/resources/static/js/staff-live-map.js` passed.

Existing route coverage remains in the test suite for:

* `/dashboard`
* `/robots`
* `/rules`
* `/simulation`
* `/system-flow`
* `/staff/pickup-request`
* `/staff/missions`
* `/staff/live-map`
* `/manager/policy-assignment`
* `/manager/robot-tasks`

What remains planned next:

* Live Map state endpoint
* Backend time-based route progression
* Frontend polling
* Returned-to-base manual Completed flow

Scope confirmation:

* No `/staff/live-map/state` endpoint was added.
* No frontend polling was added.
* No WebSocket was added.
* No scheduler was added.
* No automatic route progression was added.
* No auto-complete behavior was added.
* No automatic `RETURNED_TO_BASE` behavior was added.
* No Live Map frontend rewrite was added.
* No Live Map route animation rewrite was added.
* No barcode scan was added.
* No customer priority queue was added.
* No task interruption logic was added.
* No complex queue logic was added.
* `RuleParser` was not changed.
* `RuleEvaluator` was not changed.
* `RuleEngine` classes were not rewritten.
* `StrategyContext` was not changed.
* Strategy classes were not changed.

---

Previous completed stage:

Start Execution Action Added For Backend Mission Execution State

June 2, 2026 Start Execution action update:

This task added the first backend action for lightweight Live Map execution simulation. Staff can now explicitly start execution for an already assigned mission. The action only changes mission state to indicate execution has begun from the Base Station; it does not move the robot along a route yet.

New route added:

* `POST /staff/missions/{id}/start-execution`

MissionService behavior added:

* `MissionService.startExecution(id)` loads a visible, non-deleted mission.
* Start Execution accepts only `ASSIGNED` missions with an assigned robot and `executionStep = NOT_STARTED` or null-equivalent.
* On success it sets:
  * `status = IN_PROGRESS`
  * `executionStep = MOVING_TO_TARGET`
  * `executionStartedAt = current timestamp`
  * `currentPositionKey = base-station`
  * `pickupReachedAt = null`
  * `returnedAt = null`
* Start Execution does not change the assigned robot.
* Start Execution does not change stored assignment reason, matched rule, selected strategy, action message, decision summary, or processed timestamp.
* Start Execution does not create a new mission.
* Start Execution does not mark the mission completed.

Button visibility rules:

* `PENDING` missions show `Process Mission / Assign Robot` and `Stop`, but not `Start Execution`.
* `ASSIGNED` missions with `executionStep = NOT_STARTED` and an assigned robot show `Start Execution`, `Completed`, and `Stop`.
* `IN_PROGRESS` missions do not show `Start Execution`; they still show `Completed` and `Stop`.
* `COMPLETED` missions do not show `Start Execution`, `Stop`, or `Delete`.
* `CANCELLED` missions do not show `Start Execution`; they show `Delete` when the existing delete-after-stop rule applies.
* Soft-deleted missions remain hidden from the main mission lists.

Backend validation rules:

* Missing or soft-deleted missions are rejected with the existing "Mission not found" behavior.
* `PENDING` missions are rejected.
* Already `IN_PROGRESS` missions are rejected.
* `COMPLETED` missions are rejected.
* `CANCELLED` missions are rejected.
* Missions without an assigned robot are rejected.
* Missions already at `MOVING_TO_TARGET`, `PICKING_UP`, `RETURNING_TO_BASE`, or `RETURNED_TO_BASE` are rejected.
* Rejections redirect safely back to `/staff/missions` with flash error messages.

UI impact:

* `/staff/missions` now shows a `Start Execution` action for eligible assigned missions.
* `/staff/missions` now displays concise execution state in the mission timeline.
* `/staff/missions/{id}` continues to display execution fields:
  * Execution Step
  * Current Position
  * Execution Started
  * Pickup Reached
  * Returned To Base
* `/manager/robot-tasks` now displays concise execution state for active robot tasks.

Manager Robot Task Board impact:

* `IN_PROGRESS` missions still count as active workload.
* Completed, cancelled, and deleted missions remain excluded from active workload counts.

Completed, Stop, and Delete-after-stop behavior:

* Completed action remains allowed for `ASSIGNED` and `IN_PROGRESS` missions according to the existing lifecycle rule.
* Completed action was not changed to require `RETURNED_TO_BASE`.
* Stop action still cancels `PENDING`, `ASSIGNED`, and `IN_PROGRESS` missions.
* Stop leaves existing execution fields as historical state; no future execution progression was added.
* Delete-after-stop still soft deletes only `CANCELLED` missions.

Files changed in this task:

* `src/main/java/com/warehouse/model/Mission.java`
* `src/main/java/com/warehouse/service/MissionService.java`
* `src/main/java/com/warehouse/controller/StaffPickupRequestController.java`
* `src/main/resources/templates/staff-missions.html`
* `src/main/resources/templates/manager-robot-tasks.html`
* `src/test/java/com/warehouse/controller/StaffPickupRequestControllerTest.java`
* `src/test/java/com/warehouse/controller/ManagerRobotTaskBoardControllerTest.java`
* `PROJECT_STATUS.md`

Verification result:

* `mvn test` passed.
* 53 tests passed.
* 0 failures.
* 0 errors.
* 0 skipped.
* `node --check src/main/resources/static/js/staff-live-map.js` passed.

Existing route coverage remains in the test suite for:

* `/dashboard`
* `/robots`
* `/rules`
* `/simulation`
* `/system-flow`
* `/staff/pickup-request`
* `/staff/missions`
* `/staff/live-map`
* `/manager/policy-assignment`
* `/manager/robot-tasks`

What remains planned next:

* Backend route calculation
* Live Map state endpoint
* Frontend polling
* Returned-to-base manual Completed flow

Scope confirmation:

* No route progression was added.
* No `/staff/live-map/state` endpoint was added.
* No realtime backend polling was added.
* No frontend polling was added.
* No WebSocket was added.
* No scheduler was added.
* No auto-complete behavior was added.
* No automatic `RETURNED_TO_BASE` behavior was added.
* No Live Map route animation rewrite was added.
* No barcode scan was added.
* No customer priority queue was added.
* No task interruption logic was added.
* No complex queue logic was added.
* `RuleParser` was not changed.
* `RuleEvaluator` was not changed.
* `RuleEngine` classes were not rewritten.
* `StrategyContext` was not changed.
* Strategy classes were not changed.

---

Previous completed stage:

Mission Execution State Foundation Added

June 2, 2026 mission execution state foundation update:

This task added lightweight backend data fields so missions can later support backend-driven Live Map execution simulation. The implementation only prepares mission execution state data. It does not start missions, move robots, poll the frontend, or change the current frontend-only Live Map route animation.

New enum added:

* `MissionExecutionStep`
* Supported values:
  * `NOT_STARTED`
  * `MOVING_TO_TARGET`
  * `PICKING_UP`
  * `RETURNING_TO_BASE`
  * `RETURNED_TO_BASE`

New `Mission` fields added:

* `executionStep`
* `currentPositionKey`
* `executionStartedAt`
* `pickupReachedAt`
* `returnedAt`

Default execution behavior:

* New Staff pickup missions are still created with `status = PENDING`.
* New Staff pickup missions now default to `executionStep = NOT_STARTED`.
* New Staff pickup missions keep `currentPositionKey = null`.
* New Staff pickup missions keep `executionStartedAt = null`.
* New Staff pickup missions keep `pickupReachedAt = null`.
* New Staff pickup missions keep `returnedAt = null`.

Mission processing / assignment behavior:

* Processing still only accepts visible `PENDING` missions.
* Processing still uses workload-aware robot assignment, `RuleEvaluator`, and `StrategyContext`.
* Successfully processed missions still become `ASSIGNED`.
* Processing does not automatically start execution.
* Processed missions keep `executionStep = NOT_STARTED`.
* Processing does not set `currentPositionKey`, `executionStartedAt`, `pickupReachedAt`, or `returnedAt`.

Completed, Stop, and Delete-after-stop behavior:

* Completed action still works for `ASSIGNED` and `IN_PROGRESS` missions only.
* Completed action still sets `status = COMPLETED` and `completedAt`.
* Completed action does not force route execution and does not set `RETURNED_TO_BASE` unless a later execution flow has already done that.
* Stop action still works for `PENDING`, `ASSIGNED`, and `IN_PROGRESS` missions only.
* Stop action still sets `status = CANCELLED` and `cancelledAt`.
* Stop action leaves existing non-null execution state unchanged and does not continue execution.
* If an older mission row has no execution step, entity defaults normalize it to `NOT_STARTED` when the mission is saved.
* Delete-after-stop still works only for `CANCELLED` missions.
* Delete-after-stop still soft deletes with `deletedAt`.
* Delete-after-stop does not add execution behavior.

Small UI display added:

* Staff Mission Detail now displays read-only execution fields:
  * Execution Step
  * Current Position
  * Execution Started
  * Pickup Reached
  * Returned To Base
* No execution buttons were added.
* No Start Execution action was added.

Validation and filtering confirmation:

* Processing completed, cancelled, or deleted missions remains rejected by existing service/controller validation.
* Manager Robot Task Board active workload remains based on active mission statuses and still ignores `COMPLETED`, `CANCELLED`, and soft-deleted missions, even if execution fields contain movement-like values.
* Live Map current work selection remains based on the existing active mission status rules and still ignores `COMPLETED`, `CANCELLED`, and soft-deleted missions, even if execution fields contain movement-like values.

Files changed in this task:

* `src/main/java/com/warehouse/model/MissionExecutionStep.java`
* `src/main/java/com/warehouse/model/Mission.java`
* `src/main/resources/templates/staff-mission-detail.html`
* `src/test/java/com/warehouse/controller/StaffPickupRequestControllerTest.java`
* `src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java`
* `src/test/java/com/warehouse/controller/ManagerRobotTaskBoardControllerTest.java`
* `src/test/java/com/warehouse/service/RobotAssignmentServiceTest.java`
* `PROJECT_STATUS.md`

Verification result:

* `mvn test` passed.
* 51 tests passed.
* 0 failures.
* 0 errors.
* 0 skipped.
* `node --check src/main/resources/static/js/staff-live-map.js` passed.

Existing route coverage remains in the test suite for:

* `/dashboard`
* `/robots`
* `/rules`
* `/simulation`
* `/system-flow`
* `/staff/pickup-request`
* `/staff/missions`
* `/staff/live-map`
* `/manager/policy-assignment`
* `/manager/robot-tasks`

What remains planned next:

* Start Execution action
* Backend route calculation
* Live Map state endpoint
* Frontend polling
* Returned-to-base manual Completed flow

Scope confirmation:

* No realtime backend was added.
* No WebSocket was added.
* No frontend polling was added.
* No scheduler was added.
* No barcode scan was added.
* No customer priority queue was added.
* No auto-complete behavior was added.
* No route time calculation was added.
* No Start Execution button was added.
* No Live Map route animation rewrite was added.
* No task interruption logic was added.
* No complex queue logic was added.
* `RuleParser` was not changed.
* `RuleEvaluator` was not changed.
* `RuleEngine` classes were not rewritten.
* `StrategyContext` was not changed.
* Strategy classes were not changed.

---

Previous completed stage:

Final Integration Verification And Demo Script Update Completed

June 1, 2026 final integration re-verification:

The final integration verification was rerun against the current workspace. The existing final demo documentation was reviewed and already matched the current Admin, Manager, Staff, Live Map, Simulation, and System Flow presentation flow, so no demo script rewrite was needed in this pass.

Automated test result:

* `mvn test` passed.
* 51 tests passed.
* 0 failures.
* 0 errors.
* 0 skipped.

Route verification result on a temporary H2-backed app at port `8095`:

* `/dashboard` returned HTTP 200.
* `/robots` returned HTTP 200.
* `/rules` returned HTTP 200.
* `/simulation` returned HTTP 200.
* `/system-flow` returned HTTP 200.
* `/staff/pickup-request` returned HTTP 200.
* `/staff/missions` returned HTTP 200.
* `/staff/live-map` returned HTTP 200.
* `/manager/policy-assignment` returned HTTP 200.
* `/manager/robot-tasks` returned HTTP 200.

Integrated demo flow smoke verification:

* Manager policy assignment saved a Zone A policy using an existing active Admin rule.
* Staff Pickup Request created a temporary H2 verification mission with `Small Cargo`, `Zone A`, and location `A1`.
* Staff Missions processed the mission and showed `ASSIGNED` output with selected strategy data.
* Mission processing still uses the existing `RuleEvaluator` and `StrategyContext`.
* Completed lifecycle action moved the assigned mission into the completed group.
* Stop lifecycle action moved an active mission into the cancelled/stopped group.
* Delete-after-stop soft delete removed the stopped mission from the visible cancelled group.
* Manager Robot Task Board rendered successfully.
* Live Map rendered successfully with `Mission Flow` and `Start Route Animation` controls.
* The temporary verification app was stopped after route and smoke checks.

Documentation status:

* `docs/FINAL_DEMO_SCRIPT.md` was reviewed and already matched the current final demo flow.
* `README.md` was reviewed and already matched the current completed routes and modules.
* `docs/SCREENSHOT_CHECKLIST.md` was reviewed and already listed the current pages.
* Only `PROJECT_STATUS.md` was updated in this pass to record the fresh verification.

Files changed in this task:

* `PROJECT_STATUS.md`

Scope confirmation:

* No application code was changed.
* No barcode scan was added.
* No customer priority queue was added.
* No realtime backend was added.
* No WebSocket was added.
* No authentication or Spring Security was added.
* No new database schema was added.
* No fake demo data was added.
* No existing routes were removed.
* No Live Map redesign was performed.
* `RuleParser` was not changed.
* `RuleEvaluator` was not changed.
* `RuleEngine` classes were not rewritten.
* `StrategyContext` was not changed.
* Strategy classes were not changed.

Next recommended work remains:

* UI polish.
* Screenshot capture using `docs/SCREENSHOT_CHECKLIST.md`.
* Final report and presentation preparation.
* Optional later Live Map polish.
* Optional later barcode scan.
* Optional later customer priority queue.

---

June 1, 2026 final integration verification and demo script update:

The full integrated project was verified for presentation readiness. This task was intentionally limited to verification and documentation updates. No new major feature was added.

Automated test result:

* `mvn test` passed.
* 51 tests passed.
* 0 failures.
* 0 errors.
* 0 skipped.

Route verification result:

* `/dashboard` returned HTTP 200.
* `/robots` returned HTTP 200.
* `/rules` returned HTTP 200.
* `/simulation` returned HTTP 200.
* `/system-flow` returned HTTP 200.
* `/staff/pickup-request` returned HTTP 200.
* `/staff/missions` returned HTTP 200.
* `/staff/live-map` returned HTTP 200.
* `/manager/policy-assignment` returned HTTP 200.
* `/manager/robot-tasks` returned HTTP 200.

Integrated demo flow verification:

* Manager policy assignment page loaded and saved a Zone A policy using an existing active Admin rule.
* Staff Pickup Request created a temporary H2 verification pickup mission with cargo-to-zone and location-grid behavior.
* Staff Missions processed the mission through the existing `RuleEvaluator` and `StrategyContext`.
* Processed mission output showed assigned robot, matched rule, selected strategy, and decision/action summary.
* Completed lifecycle action moved an assigned mission into the completed group.
* Stop lifecycle action moved an active mission into the cancelled/stopped group.
* Delete-after-stop soft delete removed the stopped mission from the visible mission groups.
* Manager Robot Task Board loaded successfully.
* Manager Policy Assignment loaded successfully.
* Live Map loaded successfully and still renders Mission Flow and Start Route Animation controls.
* Simulation and System Flow routes remain available for explaining Interpreter Pattern and Strategy Pattern.

Current completed modules:

* Dashboard
* Admin Rule Management
* Interpreter Pattern rule evaluation
* Strategy Pattern dispatch
* Robot Management
* Staff Pickup Request
* Mission model and service
* Staff Missions status/history page
* Mission lifecycle actions: Completed, Stop, Delete after Stop only
* Manager Rule / Policy Assignment
* Workload-aware robot assignment
* Manager Robot Task Board
* Fullscreen Live Warehouse Map visualization
* System Flow page
* Simulation page

Documentation updates completed:

* `docs/FINAL_DEMO_SCRIPT.md` updated to the current Admin, Manager, Staff, Live Map, Simulation, and System Flow demo.
* `README.md` updated with current role-based routes and completed modules.
* `docs/SCREENSHOT_CHECKLIST.md` updated with current presentation pages.
* `PROJECT_STATUS.md` updated with final verification results.

Files changed in this task:

* `docs/FINAL_DEMO_SCRIPT.md`
* `README.md`
* `docs/SCREENSHOT_CHECKLIST.md`
* `PROJECT_STATUS.md`

Remaining known Live Map note:

* Live Map is functional enough for the current phase and should be presented as a visualization layer.
* Optional later polish can improve visual route clarity, route styling, and screenshot framing.
* The current Live Map animation remains frontend-only and does not update backend mission status automatically.

Next recommended work:

* Final UI polish for presentation consistency.
* Capture screenshots using `docs/SCREENSHOT_CHECKLIST.md`.
* Prepare final report and presentation slides.
* Optional later Live Map polish.
* Optional later barcode scan.
* Optional later customer priority queue.

Scope confirmation:

* No barcode scan was added.
* No customer priority queue was added.
* No realtime backend was added.
* No WebSocket was added.
* No authentication or Spring Security was added.
* No new database schema was added.
* No fake demo data was added.
* No existing routes were removed.
* No Live Map redesign was performed.
* `RuleParser` was not changed.
* `RuleEvaluator` was not changed.
* `RuleEngine` classes were not rewritten.
* `StrategyContext` was not changed.
* Strategy classes were not changed.

---

Previous completed stage:

Mission Lifecycle Actions And Status Grouping Completed

May 30, 2026 Staff mission lifecycle action update:

The Staff Missions page now supports explicit mission lifecycle controls for saved pickup missions while preserving the existing Interpreter Pattern and Strategy Pattern processing flow.

Mission lifecycle actions added:

* `Completed` action for `ASSIGNED` and `IN_PROGRESS` missions.
* `Stop` action for `PENDING`, `ASSIGNED`, and `IN_PROGRESS` missions.
* `Delete` action only after a mission has been stopped/cancelled.

Completed action behavior:

* Sets mission status to `COMPLETED`.
* Sets `completedAt` to the current timestamp.
* Keeps the mission visible in the Completed group/history.
* Does not delete the mission.
* Does not run `RuleEvaluator` again.
* Does not run `StrategyContext` again.
* Does not create a new robot assignment.

Stop action behavior:

* Uses the existing `CANCELLED` status.
* Sets `cancelledAt` to the current timestamp.
* Keeps the mission visible in the Cancelled / Stopped group.
* Does not delete the mission immediately.
* Does not run `RuleEvaluator` again.
* Does not run `StrategyContext` again.
* Does not create a new robot assignment.

Delete-after-stop rule:

* Delete is backend-enforced and allowed only for `CANCELLED` missions.
* Delete is rejected for `PENDING`, `ASSIGNED`, `IN_PROGRESS`, and `COMPLETED` missions.
* Delete uses soft delete by setting `deletedAt`.
* Soft-deleted missions are hidden from the main Staff Missions groups, Manager Robot Task Board workload lists, and Live Map current mission selection.

Mission timestamp fields added:

* `completedAt`
* `cancelledAt`
* `deletedAt`

The existing `createdAt` and `processedAt` behavior remains unchanged.

Mission grouping/filtering added to `/staff/missions`:

* `Active / Waiting`: `PENDING`, `ASSIGNED`, and `IN_PROGRESS`
* `Completed`: `COMPLETED`
* `Cancelled / Stopped`: `CANCELLED`
* `All non-deleted missions`

Button visibility on `/staff/missions` now follows mission status:

* `PENDING`: shows `Process Mission / Assign Robot` and `Stop`.
* `ASSIGNED` / `IN_PROGRESS`: shows `Completed` and `Stop`.
* `COMPLETED`: shows completed history and does not show process, stop, or delete.
* `CANCELLED`: shows `Delete` and does not show process, completed, or stop.
* Soft-deleted missions are not shown in the main mission list filters.

Backend validation was added so the UI is not the only enforcement layer:

* Completing a non-active mission is rejected.
* Stopping a completed or already cancelled mission is rejected.
* Deleting anything except a cancelled mission is rejected.
* Processing completed, cancelled, or soft-deleted missions is rejected.

Manager Robot Task Board impact:

* Active workload counts now ignore `COMPLETED`, `CANCELLED`, and soft-deleted missions.
* High-priority active workload counts now ignore `COMPLETED`, `CANCELLED`, and soft-deleted missions.
* Unassigned pending missions exclude soft-deleted rows.

Live Map impact:

* Live Map current mission selection continues to ignore `COMPLETED` and `CANCELLED` missions.
* Live Map now also ignores soft-deleted missions because Staff Live Map reads the non-deleted mission list.
* Route animation logic was not changed for this lifecycle task.

Files changed for this lifecycle update:

* `src/main/java/com/warehouse/model/Mission.java`
* `src/main/java/com/warehouse/repository/MissionRepository.java`
* `src/main/java/com/warehouse/service/MissionService.java`
* `src/main/java/com/warehouse/service/MissionProcessingService.java`
* `src/main/java/com/warehouse/service/RobotAssignmentService.java`
* `src/main/java/com/warehouse/service/ManagerRobotTaskBoardService.java`
* `src/main/java/com/warehouse/controller/StaffPickupRequestController.java`
* `src/main/resources/templates/staff-missions.html`
* `src/main/resources/templates/staff-mission-detail.html`
* `src/main/resources/templates/manager-robot-tasks.html`
* `src/test/java/com/warehouse/controller/StaffPickupRequestControllerTest.java`
* `src/test/java/com/warehouse/controller/ManagerRobotTaskBoardControllerTest.java`
* `src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java`
* `src/test/java/com/warehouse/service/RobotAssignmentServiceTest.java`
* `PROJECT_STATUS.md`

Verification on May 30, 2026:

* `mvn test` passed.
* 51 tests passed.
* Tests cover Staff Missions HTTP 200, lifecycle button visibility, completed action timestamping, stop action timestamping, delete-after-stop soft delete, delete rejection for non-cancelled statuses, hidden soft-deleted missions, Completed and Cancelled / Stopped filters, Manager Robot Task Board active count exclusion, Live Map exclusion for completed/cancelled/deleted missions, and existing route coverage.

This update did not add barcode scan, customer priority queue, WebSocket, realtime backend, polling, scheduler, authentication, Spring Security, login/logout, user accounts, complex queue engine, complex scheduler, `RuleParser` rewrite, `RuleEvaluator` rewrite, `RuleEngine` rewrite, `StrategyContext` rewrite, or strategy class rewrites. It did not change the existing Live Map route animation engine or existing simulation behavior.

May 30, 2026 Live Map route animation rebuild:

The Live Map route animation was rebuilt so route dots are treated as the actual warehouse path logic. `Start Route Animation` no longer animates inside only the selected target zone and no longer switches between local zone canvases during the route. It now renders a dedicated full-route animation board with Zone A, Zone B, and Zone C stacked in real warehouse order, then moves one selected robot marker through explicit route-dot waypoints.

The route order is now documented and implemented as:

* Base Station
* Zone C route dots / waypoints
* Zone C to Zone B bridge route dots when needed
* Zone B route dots / waypoints when needed
* Zone B to Zone A bridge route dots when needed
* Zone A route dots / waypoints when needed
* Exact target pickup slot
* Reverse route back to Base Station

Zone-specific route behavior:

* Zone C target such as `C1`-`C9`: `base-station` -> `zone-c-entry` -> `zone-c-main-1` -> exact C slot -> pickup pause -> reverse route -> `base-station`.
* Zone B target such as `B1`-`B9`: `base-station` -> Zone C route dots -> `bridge-c-b-1` -> `bridge-c-b-2` -> Zone B route dots -> exact B slot -> pickup pause -> reverse route through Zone B, bridge dots, Zone C -> `base-station`.
* Zone A target such as `A1`-`A9`: `base-station` -> Zone C route dots -> C/B bridge dots -> Zone B route dots -> `bridge-b-a-1` -> `bridge-b-a-2` -> Zone A route dots -> exact A slot -> pickup pause -> reverse route through Zone A, Zone B, Zone C -> `base-station`.

The return-to-base behavior is built by reversing the outbound route. Exact target slots remain keyed by mission `locationCode`, so `A1`, `A5`, `B5`, and `C5` stop at those exact slots and not at neighboring package slots.

Visible route and slot elements now use stable frontend data attributes:

* Route dots, bridge dots, and the base station route point use `data-route-point`.
* Pickup slots use `data-location` and `data-location-code`.

The selected robot remains the only visible robot during animation. The Mission Flow still highlights only `Move to Target Zone`, `Pick up Cargo`, and `Return to Base Station`. `Show All Robots` still stops/resets any running animation, restores all three robots to their current displayed mission/default positions, and resets the instruction text.

Files changed for this route-dot animation rebuild:

* `src/main/resources/static/js/staff-live-map.js`
* `src/main/resources/static/css/staff-live-map.css`
* `src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java`
* `PROJECT_STATUS.md`

Verification on May 30, 2026:

* `node --check src/main/resources/static/js/staff-live-map.js` passed.
* `mvn test` passed.
* 44 tests passed.
* Temporary app smoke check on `http://localhost:8094/staff/live-map` returned HTTP 200.
* Live Map static coverage now checks the full route animation board, stable route waypoint data attributes, explicit Base Station -> Zone C -> Zone B -> Zone A waypoint order, C/B and B/A bridge dots, reverse-return route construction, exact `A1`, `B5`, and `C5` target coordinate usage, phase highlighting, Show All reset behavior, and absence of backend calls from the animation script.

This remains a visual frontend demo only. The rebuild did not add WebSocket, realtime backend, polling, scheduler, barcode scan, customer priority queue, task interruption logic, complex backend scheduling, fake backend missions, fake mission completion, automatic mission completion, backend mission status updates, database schema changes, `RuleParser` rewrite, `RuleEvaluator` rewrite, `RuleEngine` rewrite, `StrategyContext` rewrite, or strategy class rewrites.

The project is expanding from an admin-focused rule simulation system into a role-based warehouse workflow with three confirmed roles: Admin, Manager, and Staff.

Staff can create pickup requests at `/staff/pickup-request`; valid requests are saved as PENDING missions and reviewed at `/staff/missions`.

Staff can now open the fullscreen standalone Live Warehouse Map at:

* `GET /staff/live-map`

The Live Warehouse Map was adapted from the existing standalone files under `docs/live-map-demo` into the Spring Boot / Thymeleaf application, then refined into a map-only page. The `/staff/live-map` route now renders the Live Map UI without the normal application sidebar, top bar, Admin / Manager / Staff navigation, saved mission status table, or robot status table.

The Staff sidebar still links `Live Warehouse Map` to `/staff/live-map`, but that link now opens in a new browser tab with `target="_blank"` and `rel="noopener noreferrer"`. Staff `Create Pickup Request` and `My Missions`, Manager navigation, and Admin navigation remain working normally.

The fullscreen page keeps the visual map concept from the standalone demo:

* Zone A
* Zone B
* Zone C
* Base Station
* Charging Station
* Package/location grid
* Bridge and route visuals
* Mission flow panel
* Cargo guide
* Robot legend

The Live Map now focuses on three robot markers:

* Picker Alpha = green robot
* Mover Beta = red robot
* Carrier Gamma = blue robot

The Live Map selected-robot focus was refined. The user can select a robot from the map marker or from the Robot Legend / Control panel. When Picker Alpha, Mover Beta, or Carrier Gamma is selected, only that selected robot remains visible on the map; the other two robots are visually removed from the focused view instead of being dimmed.

A `Show All Robots` button was added to the Robot Legend / Control panel. It clears the selected robot state with simple JavaScript, shows all three robots again, and resets the Mission Flow panel to: `Select a robot to view its current pickup flow.`

The Live Map current mission selection was fixed. For each display robot, `StaffLiveMapController` now chooses one current mission using this rule:

* Use the oldest `IN_PROGRESS` mission assigned to the robot first.
* If there is no `IN_PROGRESS` mission, use the oldest `ASSIGNED` mission assigned to the robot.
* If there is no `IN_PROGRESS` or `ASSIGNED` mission, use the oldest assigned `PENDING` mission because the current workload logic already treats assigned `PENDING` missions as active.
* Never use `COMPLETED` or `CANCELLED` missions as current Live Map work.
* Never choose the newest mission just because it was created later when the robot already has unfinished current work.

The Live Map robot location mapping was fixed. Each robot flow now passes the selected current mission zone and exact `locationCode` to the page. The JavaScript uses an explicit A1-A9, B1-B9, and C1-C9 coordinate map so locations such as `B5` and `C5` render at the center slot for their zone, not at neighboring slots.

`Show All Robots` now renders a simple all-zone current-position view. Picker Alpha, Mover Beta, and Carrier Gamma each render exactly once, in the zone and slot for their selected current mission. Robots without a current active mission use their existing safe default zone/position. Selected robot mode still hides the other two robots and keeps the selected robot positioned from that robot's selected current mission.

A frontend-only `Start Route Animation` button remains on the Live Map. The button is disabled until a robot with an active mission target is selected. When clicked, the selected robot now runs a complete visual pickup route:

* Start at the Base Station point.
* Move through simple route dots / path waypoints toward the selected mission zone.
* Stop at the exact selected current mission `locationCode`.
* Pause briefly at the pickup slot to represent `Pick up Cargo`.
* Return through route waypoints to the Base Station point.

The animation uses the corrected A1-A9, B1-B9, and C1-C9 coordinate map. Locations such as `B5` and `C5` are still mapped to their exact center slot coordinates, not neighboring slots.

The route animation uses only already-rendered Live Map mission data. It does not call the backend, does not update robot state, does not update mission status, does not mark missions as `IN_PROGRESS` or `COMPLETED`, and does not create mission history. When the visual route finishes, the page shows: `Visual pickup route completed. Mission status was not changed automatically.`

During the visual animation, the fixed Mission Flow tree highlight changes on the frontend only:

* Before animation, the tree keeps the selected mission's real saved status highlight.
* While the robot moves from Base Station to the target slot, `Move to Target Zone` is highlighted.
* When the robot reaches the target slot and pauses, `Pick up Cargo` is highlighted.
* While the robot returns to Base Station, `Return to Base Station` is highlighted.

Files changed for this complete visual pickup route enhancement:

* `src/main/resources/static/js/staff-live-map.js`
* `src/main/resources/static/css/staff-live-map.css`
* `src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java`
* `PROJECT_STATUS.md`

For the selected robot, the Mission Flow panel now shows:

* Robot name
* Current target when an active mission exists, such as `Move to Zone A - A1`
* A fixed pickup tree/timeline:
  * `Assigned Mission`
  * `Move to Target Zone`
  * `Pick up Cargo`
  * `Return to Base Station`
* A highlighted current step based on the selected robot's active mission status

The current-step highlight maps existing mission status to the fixed pickup tree: `PENDING` and `ASSIGNED` highlight `Assigned Mission`; `IN_PROGRESS` highlights `Move to Target Zone`. `COMPLETED` and `CANCELLED` are not treated as active pickup work.

If no active assigned mission exists for the selected robot, the page shows the clean fallback message: `No active pickup mission assigned.` The Mission Flow panel no longer shows request details, cargo details, selected strategy, long rule/decision summaries, saved mission lists, robot status tables, or robot workload lists.

No fake backend missions are created. The existing demo route buttons remain visual prototype controls only; they do not claim that a saved mission moved or completed.

Routes and navigation for this refinement:

* Existing `GET /staff/live-map` refined while remaining a fullscreen standalone map-only page
* Existing Staff sidebar `Live Warehouse Map` link still opens `/staff/live-map` in a new tab
* No backend route, realtime endpoint, polling endpoint, or mission-status mutation endpoint was added for this animation

Files changed for this refinement:

* `src/main/java/com/warehouse/controller/StaffLiveMapController.java`
* `src/main/resources/templates/staff-live-map.html`
* `src/main/resources/static/css/staff-live-map.css`
* `src/main/resources/static/js/staff-live-map.js`
* `src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java`
* `PROJECT_STATUS.md`

The Staff Missions page at `/staff/missions` now acts as the simple mission status and mission history page for the demo. It lists missions newest first, keeps active missions visible, and shows completed or cancelled missions in a clear history section when those statuses exist.

The Staff Missions page now shows:

* Request code and customer/request identifier
* Cargo type
* Zone and location code
* Priority label: `1 = High`, `2 = Medium`, and `3 = Low`
* Current mission status
* Assigned robot when available
* Matched rule when processed
* Selected strategy when processed
* Decision/action summary when processed
* Created time
* Processed time when available

The Staff Missions page uses clear badges for `PENDING`, `ASSIGNED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, High, Medium, and Low priority states. PENDING missions show the `Process Mission / Assign Robot` action. Processed missions show stored decision output clearly and are not displayed as completed unless their saved status is actually `COMPLETED`.

The read-only mission detail page is implemented at:

* `GET /staff/missions/{id}`

The detail page shows request information, cargo information, assignment information, rule evaluation result, strategy result, decision/action summary, current status, created time, and processed time.

Manager can open `/manager/robot-tasks` to view the Robot Task Board. The board still groups existing database-backed missions by assigned robot so the workload-aware assignment logic is visible after Staff missions are processed.

The Manager Robot Task Board now shows each mission under a robot with:

* Request code and customer/request identifier
* Cargo type
* Zone and location code
* Priority label
* Mission status
* Matched rule when available
* Selected strategy when available
* Short action/decision summary when available

Routes changed or added for this polish:

* Existing `GET /staff/missions` polished as the mission status/history page
* Existing `POST /staff/missions/{id}/process` preserved for PENDING mission processing
* `GET /staff/missions/{id}` added/finished as a read-only mission detail page
* Existing `GET /manager/robot-tasks` display polished while preserving robot grouping

Files changed for this polish:

* `src/main/java/com/warehouse/controller/StaffPickupRequestController.java`
* `src/main/java/com/warehouse/model/Mission.java`
* `src/main/java/com/warehouse/service/MissionService.java`
* `src/main/resources/templates/staff-missions.html`
* `src/main/resources/templates/staff-mission-detail.html`
* `src/main/resources/templates/manager-robot-tasks.html`
* `src/main/resources/static/css/app.css`
* `src/test/java/com/warehouse/controller/StaffPickupRequestControllerTest.java`
* `src/test/java/com/warehouse/controller/ManagerRobotTaskBoardControllerTest.java`
* `PROJECT_STATUS.md`

The Staff Missions page now supports processing a PENDING mission through:

* `POST /staff/missions/{id}/process`

Processing reads saved mission data, selects a workload-aware robot, builds the same type of robot condition input used by the simulation flow, evaluates active rules through the existing `RuleEvaluator`, and dispatches the selected behavior through the existing `StrategyContext`.

Mission processing uses:

* Robot battery and obstacle state from the selected robot
* Mission cargo type to derive `robotLoad`
* Mission zone and location to derive `distance`
* Mission priority as the rule input `priority`
* Manager `ZonePolicyAssignment` when one exists for the mission zone

Robot selection is now workload-aware but still intentionally simple. `RobotAssignmentService` reads candidate robots from the existing Robot table, excludes clearly unavailable statuses such as offline, maintenance, error, charging, unavailable, disabled, or out-of-service, and ranks candidates by:

* Active high-priority mission count ascending
* Active mission count ascending
* Battery descending
* Robot id and name as stable tie breakers

Active workload is counted from database-backed Mission rows assigned to the robot. Active statuses are `PENDING`, `ASSIGNED`, and `IN_PROGRESS`. `COMPLETED` and `CANCELLED` missions are not counted. High priority means `priority = 1`.

The Manager Robot Task Board follows the same workload definition as `RobotAssignmentService`. Assigned `PENDING` missions count as active workload because the existing assignment service already treats assigned `PENDING`, `ASSIGNED`, and `IN_PROGRESS` missions as active. Unassigned `PENDING` missions are shown separately.

If no candidate robot is available, mission processing does not crash, does not call the rule evaluator or strategy dispatcher, does not mark the mission completed, and keeps the mission `PENDING` with the message: `No available robot found for this mission.`

Zone policy assignment usage is intentionally simple:

* If the mission zone has an assigned active Admin rule and that assigned rule matches the mission input, that policy rule's strategy is dispatched.
* If the assigned rule is missing, inactive, unavailable in active evaluation, or does not match, processing falls back to the first matched active rule from the existing evaluator.
* If no active rule matches, the mission stores `No Rule Matched` and `NoStrategy`.

Processed missions are set to `ASSIGNED`. They are not automatically marked `COMPLETED`.

Fields added to `Mission`:

* `assignedRobotId`
* `assignedRobotName`
* `assignmentReason`
* `matchedRuleName`
* `selectedStrategyName`
* `actionMessage`
* `decisionSummary`
* `processedAt`

The existing technical core remains unchanged: `RuleParser`, `RuleEvaluator`, `RuleService`, `StrategyContext`, strategy classes, simulation flow, persistent execution history, System Flow Visualization, Robot Management, Staff Pickup Request, and Manager Rule / Policy Assignment remain intact.

Latest verification on May 29, 2026:

* `node --check src/main/resources/static/js/staff-live-map.js` passed
* `mvn test` passed
* 44 tests passed
* `GET /staff/missions` is covered and returns HTTP 200
* `GET /staff/missions/{id}` is covered for an existing mission and returns HTTP 200
* `GET /staff/live-map` is covered and returns HTTP 200
* `/staff/live-map` is covered as a fullscreen standalone page without the normal app sidebar
* `/staff/live-map` is covered for Zone A, Zone B, Zone C, Base Station, Charging Station, Picker Alpha, Mover Beta, Carrier Gamma, the `Show All Robots` button, the initial Mission Flow instruction, the fixed pickup tree, and current-step highlighting data
* `/staff/live-map` is covered to verify an `IN_PROGRESS` robot mission is used before a newer `ASSIGNED` mission
* `/staff/live-map` is covered to verify the oldest `ASSIGNED` robot mission is used when there is no `IN_PROGRESS` mission
* `/staff/live-map` is covered to verify `COMPLETED` and `CANCELLED` missions are not treated as current Live Map work
* `/staff/live-map` is covered to verify current mission zone and exact location code are rendered for the Live Map script
* Static JavaScript coverage verifies selected-robot focus removes non-selected robots, hides non-selected Mission Flow cards, exposes `showAllLiveMapRobots`, renders an all-zone overview for Show All, maps `B5` and `C5` to the center slot coordinates, filters robots by current zone to avoid duplicates, and no longer uses dimmed robot state
* Static JavaScript coverage verifies `startLiveMapRouteAnimation` is exposed, `Start Route Animation` uses active mission targets, the animation starts from `baseStationPosition`, builds a simple waypoint pickup route, reuses exact slot coordinates, highlights `Move to Target Zone`, `Pick up Cargo`, and `Return to Base Station` during the visual phases, shows the final `Visual pickup route completed. Mission status was not changed automatically.` message, and no `fetch` or `XMLHttpRequest` backend call exists in the Live Map script
* `/staff/live-map` is covered to ensure `Saved Mission Status` and `Robot Status` panels are not rendered
* Staff sidebar coverage verifies `Live Warehouse Map` points to `/staff/live-map` and opens in a new tab
* `GET /manager/robot-tasks` is covered and returns HTTP 200
* Existing route coverage still includes `/dashboard`, `/robots`, `/rules`, `/simulation`, `/system-flow`, `/staff/pickup-request`, `/staff/missions`, `/staff/live-map`, `/manager/policy-assignment`, and `/manager/robot-tasks`

This Live Map integration added complete frontend-only visual pickup route animation. It did not add WebSocket / realtime backend, polling, scheduler, barcode scan, customer priority queue, authentication, Spring Security, login/logout, user accounts, task interruption logic, complex mission scheduling, fake backend missions, fake mission completion, automatic mission completion, backend mission status updates, database schema changes, a `RuleParser` rewrite, a `RuleEvaluator` rewrite, a `RuleEngine` rewrite, a `RuleService` rewrite, a `StrategyContext` rewrite, or strategy class rewrites.

May 23, 2026 live map status: a standalone static Live Warehouse Map demo exists under `docs/live-map-demo` for visual testing only. It is not connected to backend mission data, does not add realtime infrastructure, and does not change the planned backend integration order. Backend / Thymeleaf integration remains pending until mission status data is ready to drive it.

May 25, 2026 live map status: the standalone static Live Warehouse Map demo now shows 2 small station connector markers only in Zone C. The left blue connector marker indicates the route branch to Base Station, and the right green connector marker indicates the route branch to Charging Station. The standalone live map remains front-end only; no Spring Boot, Thymeleaf, backend logic, WebSocket, polling, or fake realtime backend data was added.

May 26, 2026 live map status: the standalone static Live Warehouse Map legend now supports three robot types. Picker Alpha, Mover Beta, and Carrier Gamma were added as green, red, and blue robot legend entries; the generic Active Route legend item was removed. Reusable robot color classes were prepared for future robot-specific route patterns.

May 28, 2026 live map status: the standalone visual demo remains under `docs/live-map-demo`, and an adapted copy is integrated into the Spring Boot app at `/staff/live-map`. The integrated page now renders as a fullscreen standalone map-only Thymeleaf view, without the shared sidebar shell or saved data tables. It shows the three required robot markers and a selected-robot Mission Flow panel that uses real active assigned mission data when available, otherwise a `No active mission assigned.` fallback. Dynamic mission movement remains planned future work.

Manager Mission Monitor remains a planned future page. What remains planned next is optional Manager Mission Monitor refinement and simple future Live Map mission movement using real mission state.

---

# PROJECT OVERVIEW

This project demonstrates how Interpreter Pattern and Strategy Pattern work together to create a dynamic robotics decision engine for warehouse automation.

The system evaluates robot conditions at runtime and dynamically changes robot behavior without modifying Java business logic.

The architecture focuses on:

* Clean OOP design
* Runtime rule evaluation
* Dynamic strategy switching
* Educational software architecture
* Scalable rule engine structure

The confirmed business roles are:

* Admin: configures rules, strategies, robots, and system behavior.
* Manager: assigns existing rules or policies to suitable robots or warehouse zones and controls robot availability.
* Staff: creates customer pickup requests and tracks mission status from an operational perspective.

This role workflow keeps the project realistic and educational. It should extend the current rule engine gradually without adding unnecessary enterprise complexity.

---

# COMPLETED FEATURES

## Foundation Setup

* Spring Boot project initialized
* Maven dependencies configured
* Clean package architecture created
* Bootstrap UI integrated
* SQL Server datasource configured
* Repository structure configured
* Dashboard UI created

---

## Core Entities

* Robot entity
* Rule entity
* Strategy entity
* Rule repository structure
* Database-ready entity models

---

## Interpreter Pattern

* Expression interface
* BatteryExpression
* ObstacleExpression
* RobotLoadExpression
* DistanceExpression
* PriorityExpression
* AndExpression
* OrExpression
* Dynamic expression evaluation
* Runtime condition parsing structure
* Multi-condition rule evaluation

---

## Strategy Pattern

* Strategy interface
* ChargingStrategy
* ObstacleAvoidanceStrategy
* HeavyLoadStrategy
* EnergySavingStrategy
* FastRouteStrategy
* SafeRouteStrategy
* StrategyContext
* Runtime strategy switching

---

## Dynamic Rule Engine

* Expression-based rule matching
* Dynamic condition composition
* Runtime rule evaluation
* AND / OR logical operators
* Priority-based rule evaluation
* Database-backed active rule loading
* Runtime rule create, edit, delete, enable, and disable
* Removal of giant hardcoded if-else chains

---

## Simulation Module

* Battery input
* Obstacle input
* Robot load input
* Distance input
* Task priority input
* Runtime simulation flow
* Rule evaluation visualization
* Matched condition display
* Selected strategy display
* Robot action result display
* Persistent simulation execution history saved after each run
* Input summary displayed after each simulation
* Final decision summary displayed after each simulation
* Rule Evaluation Trace table displays every evaluated active rule in priority order
* Selected matched rule is highlighted in the simulator trace
* Interpreter leaf condition results are displayed as TRUE / FALSE details

---

## Rule Execution History

* RuleExecutionHistory entity added
* RuleExecutionHistoryRepository added
* Simulation executions persisted after Interpreter evaluation and Strategy dispatch
* Recent simulation execution history displayed on the dashboard

---

## System Flow Visualization

* /system-flow route added
* SystemFlowController added
* Flow overview added for Input -> Interpreter -> Rule Match -> Strategy Dispatch -> Robot Action -> History
* Interpreter Pattern section added with RuleParser and Expression components
* Strategy Pattern section added with active Strategy classes
* Current active rules displayed from the database in priority order
* Recent execution history displayed on the System Flow page
* Sidebar navigation link added to Dashboard, Rule Management, Simulation, and System Flow pages

---

## Focused Architecture Tests

* RuleParser tests cover supported single conditions for battery, obstacleDetected, robotLoad, distance, and priority
* RuleParser tests cover AND and OR expression behavior
* RuleService priority selection test verifies that the first matched rule in priority order is selected
* StrategyContext dispatch tests verify ChargingStrategy, FastRouteStrategy, SafeRouteStrategy, ObstacleAvoidanceStrategy, HeavyLoadStrategy, and EnergySavingStrategy
* StrategyContext unknown strategy fallback test verifies NoStrategy behavior
* SimulationService trace test verifies input values, matched rule, selected strategy, action message, ruleResults, and conditionResults

---

## Final Documentation and Presentation Preparation

* UML documentation added in docs/UML_DIAGRAMS.md
* UML documentation includes Mermaid use case, class, sequence, and activity diagrams
* Final demo script added in docs/FINAL_DEMO_SCRIPT.md
* README.md updated with project overview, technology stack, design patterns, main features, run commands, routes, tests, and demo flow summary
* Documentation explains where the Interpreter Pattern and Strategy Pattern are used
* Documentation explains the simulation flow from input through history persistence

---

## Final UI Polish and Screenshot Preparation

* Dashboard readability improved with clearer empty states, priority badges, and an Active Strategy Catalog using existing active strategy data
* Rule Management syntax helper added for supported fields, operators, logical keywords, and example expressions
* Simulation page trace readability improved with a shorter educational explanation, clearer empty state, centered MATCHED / SKIPPED status column, and a neutral final decision summary accent
* System Flow tables and empty states polished for presentation consistency
* Shared CSS improved for table padding, table hover readability, badge consistency, strategy chip wrapping, syntax helper styling, and reusable empty states
* Screenshot checklist added in docs/SCREENSHOT_CHECKLIST.md
* No backend rule evaluation, parser, strategy dispatch, or history persistence logic was changed

---

## Robot Management Page and Final UI Completion

* Robot Management page added
* `/robots` route added
* Robot cards display database robot state
* Battery bars display robot battery percentage with low-battery styling
* Status badges display Low Battery, Obstacle Detected, or the robot status value
* Current strategy is displayed from the robot's assigned strategy class/name
* Navigation updated with a Robot Management link across main pages
* Placeholder sidebar links were removed from the main templates
* Page remains read-only; no robot CRUD or decision behavior changes were added

---

## Role-Based Workflow Documentation

* Admin, Manager, and Staff responsibilities documented
* Business flow documented from Staff Pickup Request through robot selection, Interpreter evaluation, Strategy dispatch, mission status, live map visualization, and execution history
* Documentation clarifies that the role workflow wraps the existing Interpreter Pattern and Strategy Pattern core
* Roadmap updated so the next implementation begins with role-based navigation and Staff Pickup Request
* Live Warehouse Map UI documented as a later step after mission/request data exists
* No Java code, Thymeleaf templates, CSS, JavaScript, or database schema was changed for this documentation update

---

## Standalone Live Warehouse Map HTML Demo

* Created standalone Live Warehouse Map HTML demo for visual review before Spring Boot / Thymeleaf integration
* Added `docs/live-map-demo/live-map.html`
* Added `docs/live-map-demo/live-map.css`
* Added `docs/live-map-demo/live-map.js`
* Demo includes dark futuristic header, legend, zone controls, right-side cargo guide, right-side vertical mission flow, active zone map, shared SVG route network, package pads, Base Station, and Charging Station
* Live map demo simplified to single-zone view so only the selected Zone A, Zone B, or Zone C appears in the center board
* Zone buttons now switch the visible zone and preserve each zone color theme: green for Zone A, blue for Zone B, and purple for Zone C
* Mission Flow moved from the bottom of the page to the right vertical panel above Cargo Guide
* Refined live map flow to match Zone C -> Zone B -> Zone A
* Removed extra bridge connector marks from Zone A
* Restricted Base Station and Charging Station visibility to Zone C only
* Refactored standalone live map zone rendering so each active zone uses one shared SVG route network instead of per-cell route drawing
* Preserved exactly 9 package pads per zone, with small dots for Zone A, medium dots for Zone B, and large dots for Zone C
* Preserved the single-zone active view and the demo route order Zone C -> Zone B -> Zone A
* Updated Live Map legend to support three robot types: Picker Alpha, Mover Beta, and Carrier Gamma
* Removed the generic Active Route legend item while keeping the Route Network legend item
* Prepared reusable robot color classes for future robot-specific route patterns
* Demo remains available as a standalone UI prototype and can be opened directly in a browser
* Spring Boot / Thymeleaf integration has since been implemented separately at `/staff/live-map`
* Future work can add dynamic mission movement only after it is driven by real mission/request state
* No Interpreter Pattern, Strategy Pattern, database, WebSocket, authentication, or existing simulation behavior was changed by the standalone demo work

---

## Role-Based Navigation Structure

* Shared Thymeleaf sidebar fragment added for the main application pages
* Sidebar navigation grouped into Admin, Manager, and Staff sections
* Admin section links to the implemented pages: Dashboard, Rule Management, Robot Management, Simulation, and System Flow
* Admin Robot Management still links to `/robots`
* Manager section links Robot Task Board to `/manager/robot-tasks`
* Manager Rule / Policy Assignment links to `/manager/policy-assignment`
* Manager Mission Monitor remains a disabled Planned item
* Staff Create Pickup Request is now implemented as `/staff/pickup-request`
* Staff My Missions now links to `/staff/missions`
* Staff Live Warehouse Map now links to `/staff/live-map`
* Existing routes remain working: `/dashboard`, `/robots`, `/rules`, `/simulation`, `/system-flow`, `/manager/robot-tasks`, `/manager/policy-assignment`, `/staff/pickup-request`, `/staff/missions`, and `/staff/live-map`
* No authentication, Spring Security, login/logout, rule engine logic, or strategy dispatch logic was added or changed

---

## Staff Pickup Request Page

* `/staff/pickup-request` route added
* StaffPickupRequestController added
* StaffPickupRequestDto added as a Staff form object
* Staff Pickup Request Thymeleaf page added
* Staff can enter request code, customer name / customer code, cargo type, cargo location, priority, and optional notes
* Supported cargo types are Small Cargo, Medium Cargo, and Large Cargo
* Cargo type now automatically determines warehouse zone
* Small Cargo maps to Zone A
* Medium Cargo maps to Zone B
* Large Cargo maps to Zone C
* Staff can no longer manually choose a different zone in the UI
* Zone remains visible through a read-only zone display
* Cargo location changed from free-text input to a 3x3 selectable location grid
* Zone A supports A1 through A9
* Zone B supports B1 through B9
* Zone C supports C1 through C9
* Selected location is submitted through the form and shown in the saved mission summary
* Supported priorities are 1 = High, 2 = Medium, and 3 = Low
* POST submit validates required fields, allowed values, cargo-zone mapping, and zone-location mapping
* Valid submit saves a Mission with status PENDING and displays a same-page saved mission summary
* Invalid submit displays validation messages on the same page
* Staff Create Pickup Request is now a clickable active sidebar link
* Staff My Missions now links to `/staff/missions`
* Staff Live Warehouse Map is now implemented at `/staff/live-map`
* Manager Rule / Policy Assignment and Manager Mission Monitor remain disabled Planned items
* No robot selection, RuleEvaluator connection, StrategyContext connection, authentication, or live map integration was added

---

## Mission Model and Staff Missions Page

* Mission entity added
* MissionStatus enum added with PENDING, ASSIGNED, IN_PROGRESS, COMPLETED, and CANCELLED
* MissionRepository added
* MissionService added for Staff pickup request validation, zone confirmation, mission creation, and newest-first mission listing
* Mission fields include id, requestCode, customerName, cargoType, zone, locationCode, priority, status, notes, createdAt, updatedAt, and decision output fields after processing
* New Staff pickup requests are saved with status PENDING
* `/staff/missions` route added
* Staff My Missions page lists saved missions newest first
* The mission list was initially review-only; mission processing is now added in the Mission Processing Flow section
* No barcode scan, customer priority queue, authentication, Spring Security, WebSocket, realtime backend, or live map integration was added

---

## Manager Rule / Policy Assignment Page

* `/manager/policy-assignment` route added
* ManagerPolicyAssignmentController added
* ZonePolicyAssignment entity added
* ZonePolicyAssignmentRepository added
* ZonePolicyAssignmentService added
* ZonePolicyAssignmentDto added
* Manager Rule / Policy Assignment Thymeleaf page added
* Manager sidebar Rule / Policy Assignment item is now clickable
* Manager Mission Monitor remains disabled Planned
* Staff Live Warehouse Map is now implemented at `/staff/live-map`
* Manager can assign active Admin-created rules as operational policies for Zone A, Zone B, and Zone C
* Zone A is validated as Small Cargo
* Zone B is validated as Medium Cargo
* Zone C is validated as Large Cargo
* POST submit saves or updates one assignment per zone
* Assignments are database-backed in `zone_policy_assignments`
* Persistence uses `ruleId` and validates the selected rule exists and is active on save
* No RuleParser, RuleEvaluator, RuleEngine, StrategyContext, strategy class, robot selection, mission execution, mission status automation, authentication, barcode scan, customer priority queue, fake robot movement, or live map integration was changed

---

## Mission Processing Flow

* `MissionProcessingService` added as a business workflow layer around the existing technical core
* `RobotAssignmentService` added as a focused service for mission robot selection
* `POST /staff/missions/{id}/process` route added
* Staff Missions page now shows a Process Mission button for PENDING missions
* Staff Missions page now displays assigned robot, assignment reason, matched rule, selected strategy, action message, decision summary, processed time, and updated mission status
* Mission processing reads mission cargo type, zone, location, and priority
* Mission processing selects the best available robot by current workload instead of simply selecting the first robot
* Candidate robots are ranked by active high-priority mission count ascending, active mission count ascending, battery descending, and robot id/name as tie breakers
* Active mission count uses assigned missions with status `PENDING`, `ASSIGNED`, or `IN_PROGRESS`
* Active high-priority mission count uses assigned active missions where `priority = 1`
* `COMPLETED` and `CANCELLED` missions are not counted as active workload
* Clearly unavailable robot statuses such as offline, maintenance, error, charging, unavailable, disabled, and out-of-service are excluded from assignment
* Mission processing builds a transient Robot condition input with `battery`, `obstacleDetected`, `robotLoad`, `distance`, and `priority`
* Robot battery and obstacle state come from the selected robot
* `robotLoad` is derived from mission cargo type: Small Cargo, Medium Cargo, or Large Cargo
* `distance` is derived from mission zone and location code
* `priority` comes from the Staff mission priority
* Existing `RuleEvaluator.evaluate(robot)` is reused for Interpreter Pattern evaluation
* Existing `StrategyContext.executeStrategy(strategyName, robot)` is reused for Strategy Pattern dispatch
* Manager `ZonePolicyAssignment` is read for the mission zone when available
* If the assigned zone policy rule is active and matches the mission input, that policy rule's strategy is dispatched
* If no zone policy exists, or the assigned rule is inactive, missing, unavailable, or not matched, processing falls back to the existing active-rule evaluator result
* Processed missions are set to `ASSIGNED`
* If no robot candidate is available, the mission remains `PENDING`, stores a clear no-robot message, and is not sent through RuleEvaluator or StrategyContext
* Missions are not automatically marked `COMPLETED`
* Mission fields added: `assignedRobotId`, `assignedRobotName`, `assignmentReason`, `matchedRuleName`, `selectedStrategyName`, `actionMessage`, `decisionSummary`, and `processedAt`
* Non-existing mission processing requests redirect back to `/staff/missions` with an error message instead of crashing
* No RuleParser, RuleEvaluator, RuleService, StrategyContext, strategy class, simulation behavior, authentication, barcode scan, customer priority queue, WebSocket, complex scheduling, task queue, fake movement animation, completion automation, or Live Warehouse Map integration was added or rewritten

---

## Manager Robot Task Board Page

* `/manager/robot-tasks` route added
* `ManagerRobotTaskBoardController` added
* `ManagerRobotTaskBoardService` added
* `RobotTaskBoardDto` and `RobotTaskGroupDto` added for the grouped view model
* `MissionRepository` now provides read queries for active assigned missions and pending missions
* `RobotAssignmentService` exposes the same active workload statuses used by robot selection
* `manager-robot-tasks.html` Thymeleaf page added
* Manager sidebar changed from Robot Operations to Robot Task Board
* Robot Task Board sidebar link points to `/manager/robot-tasks`
* Admin Robot Management still links to `/robots`; `/robots` was not removed
* Rule / Policy Assignment still links to `/manager/policy-assignment`
* Manager Mission Monitor remains disabled Planned future work
* The page shows one card per existing database Robot record
* Each robot card shows robot name/code, status, battery level, active mission count, high-priority active mission count, and assigned active mission details
* Mission rows show request code/customer identifier, cargo type, zone, location code, priority label, mission status, matched rule, selected strategy, and decision/action summary when stored
* The page shows robots with no active missions using a clear empty state
* The page shows Unassigned Pending Missions for PENDING missions without an assigned robot id/name
* Active workload follows the existing assignment service logic: assigned `PENDING`, `ASSIGNED`, and `IN_PROGRESS` missions count as active; `COMPLETED` and `CANCELLED` do not count
* High priority means `priority = 1`; medium means `priority = 2`; low means `priority = 3`
* The board uses existing `Mission` and `Robot` data only; no fake task data, fake missions, fake robot movement, or automatic completion was added
* No RuleParser, RuleEvaluator, RuleEngine, StrategyContext, strategy class, existing simulation behavior, Live Map integration, barcode scan, customer priority queue, WebSocket, authentication, Spring Security, login/logout, user accounts, task interruption logic, complex scheduler, drag-and-drop, queue reordering, or database schema change was added

---

## Mission Status and Mission History Polish

* Existing `/staff/missions` route polished as the Staff mission status and history page
* `/staff/missions` keeps missions sorted newest first through `MissionService.getMissionsNewestFirst()`
* Active missions remain visible under Mission Status
* `COMPLETED` and `CANCELLED` missions remain visible under Mission History
* Staff mission rows show request code, customer/request identifier, cargo type, zone, location code, priority label, status, assigned robot, matched rule, selected strategy, decision/action summary, created time, and processed time when available
* PENDING missions show the `Process Mission / Assign Robot` button
* Processed missions show stored decision output and a neutral decision-stored state without being automatically marked `COMPLETED`
* Clear badges are used for `PENDING`, `ASSIGNED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, High, Medium, and Low
* `GET /staff/missions/{id}` read-only mission detail page added/finished
* Mission detail shows request information, cargo information, assignment information, rule evaluation result, strategy result, decision/action summary, current status, created time, and processed time
* Existing `/manager/robot-tasks` display polished while preserving robot grouping
* Robot task board mission rows show request code, customer/request identifier, cargo type, zone, location code, priority label, status, matched rule, selected strategy, and short action/decision summary when stored
* `/staff/missions` remains the simple mission history page for now
* No fake mission data, fake robot movement, automatic completion, complex scheduler, authentication, WebSocket, barcode scan, customer priority queue, Live Map integration, RuleParser rewrite, RuleEvaluator rewrite, RuleEngine rewrite, StrategyContext rewrite, or strategy class rewrite was added

---

## Staff Live Warehouse Map Integration

* `/staff/live-map` route added
* `StaffLiveMapController` added
* `staff-live-map.html` Thymeleaf page added
* Staff sidebar `Live Warehouse Map` link now points to `/staff/live-map` and opens in a new tab
* Existing standalone demo assets from `docs/live-map-demo` were adapted into Spring Boot static assets
* `src/main/resources/static/css/staff-live-map.css` contains scoped styling based on `docs/live-map-demo/live-map.css`
* `src/main/resources/static/js/staff-live-map.js` preserves the existing standalone demo zone behavior and now adds selected robot focus
* The integrated map preserves Zone A, Zone B, Zone C, route network, package pads, Base Station, Charging Station, cargo guide, mission flow panel, and robot legend
* The page is now a fullscreen standalone map-only view and does not render the normal application sidebar, top bar, Admin / Manager / Staff navigation, saved mission status table, or robot status table
* The page shows Picker Alpha as green, Mover Beta as red, and Carrier Gamma as blue
* The selected robot is highlighted while the other two robots are dimmed
* The Mission Flow panel shows only the selected robot's current work
* Mission Flow uses real active assigned mission data from `MissionService` when available; when no active mission exists, it shows `No active mission assigned.`
* Active current work uses the existing simple workload statuses: `PENDING`, `ASSIGNED`, and `IN_PROGRESS`
* The page does not create fake missions, does not show fake completed movement, and does not claim realtime execution
* The original `docs/live-map-demo` files remain in place as the standalone visual reference
* No WebSocket, realtime backend, polling, barcode scan, customer priority queue, task interruption logic, complex scheduling, authentication, RuleParser rewrite, RuleEvaluator rewrite, RuleEngine rewrite, StrategyContext rewrite, strategy class change, or simulation behavior change was added

---

## Rule Management System

* /rules management route
* Dynamic runtime rule loading
* Database-driven behavior switching
* Rule enable / disable support
* Rule priority support
* Rule create / edit / delete support
* Strategy dropdown populated from active strategies

---

# CURRENT WORKING FLOW

1. User inputs robot conditions
2. Simulator sends robot state to Rule Engine
3. Interpreter Pattern evaluates dynamic expressions
4. Matching rules are identified
5. Highest priority rule is selected
6. Strategy Pattern switches robot behavior dynamically
7. Robot action is executed and displayed on UI
8. Simulation execution result is saved to persistent rule execution history
9. System Flow page explains the same process using live rules, active strategies, and recent history
10. Simulation page shows the step-by-step rule trace and Interpreter leaf condition results
11. User can now view robot fleet state from `/robots` before running simulations or reviewing decisions.
12. Staff can create a pickup request at `/staff/pickup-request`; valid requests are saved as PENDING missions.
13. Staff can review saved missions at `/staff/missions`.
14. Manager can assign existing active Admin rules as zone-based operational policies at `/manager/policy-assignment`.
15. Staff can process a PENDING mission from `/staff/missions` through `POST /staff/missions/{id}/process`.
16. Mission processing selects a workload-aware robot, evaluates mission-derived robot input through the existing `RuleEvaluator`, dispatches the selected strategy through the existing `StrategyContext`, stores the assignment reason and decision on the mission, and sets status to `ASSIGNED`.
17. Manager can monitor assigned robot workload at `/manager/robot-tasks`.
18. The Robot Task Board groups active assigned missions by robot and shows unassigned PENDING missions separately.

This implemented technical flow is preserved:

```text
Robot Input -> Interpreter -> Rule Match -> Strategy Dispatch -> Robot Action -> History
```

The confirmed role-based business flow now demonstrated is:

```text
Staff Pickup Request -> Robot Selection -> Rule/Policy Assignment from Manager -> Interpreter Evaluation -> Strategy Dispatch -> Robot Action -> Mission Status -> Manager Robot Task Board
```

Manager Mission Monitor, Live Map visualization, and expanded mission history remain planned after this mission status data exists.

---

# CURRENT SUPPORTED CONDITIONS

## Numeric Conditions

* battery
* robotLoad
* distance
* priority

---

## Boolean Conditions

* obstacleDetected

---

# CURRENT SUPPORTED OPERATORS

## Comparison Operators

* <
* >
* ==
* <=
* >=

---

## Logical Operators

* AND
* OR

---

# CURRENT SUPPORTED STRATEGIES

* ChargingStrategy
* ObstacleAvoidanceStrategy
* HeavyLoadStrategy
* EnergySavingStrategy
* FastRouteStrategy
* SafeRouteStrategy

---

# CURRENT ARCHITECTURE STATUS

| Module                 | Status        |
| ---------------------- | ------------- |
| Dashboard UI           | Working       |
| Rule Engine            | Dynamic       |
| Strategy Module        | Working       |
| Robot Module           | Working       |
| Simulation Module      | Working       |
| Rule Management UI     | Working       |
| System Flow UI         | Working       |
| SQL Server Integration | Working Local |
| Database Rule Loading  | Working       |
| Role Workflow Documentation | Documented |
| Role-Based Navigation | Working |
| Staff Pickup Request | Working; Saves PENDING Mission with Auto-Zone Location Grid |
| Mission / Request Data | Working; Stores Processing Decision Fields |
| Mission Status / History | Working; `/staff/missions` Lists Active and History Missions with Decision Output |
| Mission Detail | Working; `/staff/missions/{id}` Read-only Detail Page |
| Manager Rule / Policy Assignment | Working; Database-backed ZonePolicyAssignment by Zone |
| Live Warehouse Map UI | Working; `/staff/live-map` Fullscreen Standalone Robot-Focused Map |

---

# DATABASE STATUS

## Current State

* SQL Server connection verified locally
* JPA repositories configured
* Datasource structure configured
* Seed strategies configured
* Seed rules configured
* Runtime CRUD structure working
* Rule execution history table managed through the RuleExecutionHistory entity
* RuleExecutionHistoryRepository retrieves recent simulation executions by newest first
* Mission table managed through the Mission entity
* Mission now stores assignment and decision output fields for processed missions
* MissionRepository retrieves saved Staff missions by newest first
* ZonePolicyAssignment table managed through the ZonePolicyAssignment entity
* ZonePolicyAssignmentRepository retrieves zone policy assignments by zone

---

## Seeded Rule Examples

* Critical Battery With Obstacle Rule -> ChargingStrategy
* Obstacle Detection Rule -> ObstacleAvoidanceStrategy
* Heavy Load Rule -> HeavyLoadStrategy
* Low Battery Rule -> ChargingStrategy
* Urgent Task Fast Route Rule -> FastRouteStrategy
* Long Distance Safe Route Rule -> SafeRouteStrategy

---

# CURRENT LOCALHOST ROUTES

* http://localhost:8080
* http://localhost:8080/dashboard
* http://localhost:8080/robots
* http://localhost:8080/rules
* http://localhost:8080/simulation
* http://localhost:8080/system-flow
* http://localhost:8080/manager/robot-tasks
* http://localhost:8080/manager/policy-assignment
* http://localhost:8080/staff/pickup-request
* http://localhost:8080/staff/missions
* http://localhost:8080/staff/missions/{id}
* http://localhost:8080/staff/live-map
* POST http://localhost:8080/staff/missions/{id}/process

Manager Rule / Policy Assignment, Manager Robot Task Board, Staff Mission processing, Staff Mission Status / History, Staff Mission Detail, and Staff Live Warehouse Map are implemented. Manager Mission Monitor is currently a disabled Planned navigation item.

During verification, the updated application was also started on:

* http://localhost:8081
* http://localhost:8081/dashboard
* http://localhost:8081/robots
* http://localhost:8081/rules
* http://localhost:8081/simulation
* http://localhost:8081/system-flow
* http://localhost:8081/staff/pickup-request
* http://localhost:8081/staff/missions

Latest route verification for Mission persistence used:

* http://localhost:8085/staff/pickup-request
* http://localhost:8085/staff/missions
* http://localhost:8085/dashboard
* http://localhost:8085/robots
* http://localhost:8085/rules
* http://localhost:8085/simulation
* http://localhost:8085/system-flow

Latest mission processing manual verification used:

* http://localhost:8087/dashboard
* http://localhost:8087/robots
* http://localhost:8087/rules
* http://localhost:8087/simulation
* http://localhost:8087/system-flow
* http://localhost:8087/manager/policy-assignment
* http://localhost:8087/staff/pickup-request
* http://localhost:8087/staff/missions
* POST http://localhost:8087/staff/missions/{id}/process

---

# LATEST CHANGED FILES

## Staff Live Warehouse Map Fullscreen Standalone Refinement

* src/main/java/com/warehouse/controller/StaffLiveMapController.java
* src/main/resources/templates/staff-live-map.html
* src/main/resources/templates/fragments/sidebar.html
* src/main/resources/static/css/staff-live-map.css
* src/main/resources/static/js/staff-live-map.js
* src/test/java/com/warehouse/controller/StaffLiveMapControllerTest.java
* src/test/java/com/warehouse/controller/RoleNavigationControllerTest.java
* PROJECT_STATUS.md

Refined the existing integrated `docs/live-map-demo` visual design in the Spring Boot app at `/staff/live-map`.

The page now renders as a fullscreen standalone map-only Thymeleaf page. It does not use the shared app shell or Staff sidebar on the Live Map page itself. The Staff sidebar link on normal app pages still points to `/staff/live-map` and now opens the map in a new browser tab.

The page preserves the standalone demo's Zone A, Zone B, Zone C, package pads, route network, Base Station, Charging Station, robot legend, mission flow, and cargo guide. Picker Alpha is green, Mover Beta is red, and Carrier Gamma is blue. Selecting a robot from the map or Robot Legend / Control panel highlights that robot, dims the other two, and shows only that robot's current Mission Flow.

Saved Mission Status and Robot Status panels were removed from the Live Map because `/staff/missions` and `/manager/robot-tasks` already cover those views.

Mission Flow uses real active assigned mission data from `MissionService` when available. If the selected robot has no active assigned mission, the panel shows `No active mission assigned.`

What remains planned next: optional Manager Mission Monitor and future Live Map mission movement driven by real mission state.

No WebSocket, realtime backend, polling, barcode scan, customer priority queue, task interruption logic, complex scheduler, fake backend mission data, fake completion, authentication, Spring Security, login/logout, RuleParser rewrite, RuleEvaluator rewrite, RuleEngine rewrite, StrategyContext rewrite, strategy class change, or simulation behavior change was added.

Verification on May 28, 2026:

* `node --check src/main/resources/static/js/staff-live-map.js` passed
* `mvn test` passed
* 40 tests passed
* `StaffLiveMapControllerTest` verifies `GET /staff/live-map` returns HTTP 200, renders `staff-live-map`, includes Zone A, Zone B, Zone C, Base Station, Charging Station, Picker Alpha, Mover Beta, Carrier Gamma, selected robot Mission Flow, and real assigned mission data
* `StaffLiveMapControllerTest` verifies `/staff/live-map` does not render the normal `app-layout`, `side-nav`, `Saved Mission Status`, or `Robot Status`
* `RoleNavigationControllerTest` verifies existing app routes still render role navigation and the Staff sidebar `Live Warehouse Map` link opens `/staff/live-map` in a new tab
* Temporary app verification on `http://localhost:8094` confirmed `/staff/live-map` returns HTTP 200, includes the three required robots, and omits the old saved mission and robot status panels
* Temporary app verification on `http://localhost:8094/staff/missions` confirmed the Staff sidebar Live Warehouse Map link points to `/staff/live-map` and includes `target="_blank"`
* Existing simulation service and strategy tests still pass, confirming simulation behavior remains intact

---

## Mission Status and Mission History Polish

* src/main/java/com/warehouse/controller/StaffPickupRequestController.java
* src/main/java/com/warehouse/model/Mission.java
* src/main/java/com/warehouse/service/MissionService.java
* src/main/resources/templates/staff-missions.html
* src/main/resources/templates/staff-mission-detail.html
* src/main/resources/templates/manager-robot-tasks.html
* src/main/resources/static/css/app.css
* src/test/java/com/warehouse/controller/StaffPickupRequestControllerTest.java
* src/test/java/com/warehouse/controller/ManagerRobotTaskBoardControllerTest.java
* PROJECT_STATUS.md

Polished `/staff/missions` so it works as the current mission status and mission history page. The page now shows request code or customer/request identifier, cargo type, zone, location code, priority label, status, assigned robot, matched rule, selected strategy, decision/action summary, created time, and processed time when available.

PENDING missions still show `Process Mission / Assign Robot`. Processed missions show stored decision output clearly and are not automatically marked `COMPLETED`.

Finished the read-only mission detail route at `GET /staff/missions/{id}`. The detail page shows request information, cargo information, assignment information, rule evaluation result, strategy result, decision/action summary, current status, created time, and processed time.

Polished `/manager/robot-tasks` while preserving the existing grouping by robot. Each mission under a robot now shows request code, customer/request identifier, cargo type, zone, location code, priority label, status, selected strategy, matched rule, and short action/decision summary when available.

Live Map integration has since been completed. What remains planned next: optionally add Manager Mission Monitor using saved mission decision data and future Live Map mission movement driven by real mission state.

That mission status polish itself did not add barcode scan, customer priority queue, WebSocket, authentication, Spring Security, login/logout, user accounts, task interruption logic, complex scheduler, fake mission data, fake robot movement, automatic completion, RuleParser rewrite, RuleEvaluator rewrite, RuleEngine rewrite, StrategyContext rewrite, or strategy class rewrite.

Verification on May 28, 2026:

* `mvn test` passed
* 38 tests passed
* `StaffPickupRequestControllerTest` verifies `GET /staff/missions` returns HTTP 200 and displays status, priority labels, assigned robot, matched rule, selected strategy, decision summary, pending action, and history rows
* `StaffPickupRequestControllerTest` verifies `GET /staff/missions/{id}` returns HTTP 200 for an existing mission and renders the read-only detail page
* `ManagerRobotTaskBoardControllerTest` verifies `GET /manager/robot-tasks` returns HTTP 200 and displays assigned active missions grouped by robot
* Existing route coverage still verifies `/dashboard`, `/robots`, `/rules`, `/simulation`, `/system-flow`, `/staff/pickup-request`, `/staff/missions`, `/manager/policy-assignment`, and `/manager/robot-tasks`
* Existing simulation service and strategy tests still pass, confirming simulation behavior remains intact

---

## Workload-Aware Robot Assignment

* src/main/java/com/warehouse/model/Mission.java
* src/main/java/com/warehouse/repository/MissionRepository.java
* src/main/java/com/warehouse/service/RobotAssignmentService.java
* src/main/java/com/warehouse/service/MissionProcessingService.java
* src/main/java/com/warehouse/controller/StaffPickupRequestController.java
* src/main/resources/templates/staff-missions.html
* src/test/java/com/warehouse/service/RobotAssignmentServiceTest.java
* src/test/java/com/warehouse/controller/StaffPickupRequestControllerTest.java
* PROJECT_STATUS.md

Added workload-aware robot assignment for Staff mission processing from `/staff/missions` through `POST /staff/missions/{id}/process`. Processing now selects a candidate robot using database-backed workload counts before continuing into the existing RuleEvaluator and StrategyContext flow.

Active mission count uses assigned missions with status `PENDING`, `ASSIGNED`, or `IN_PROGRESS`. Active high-priority mission count uses assigned active missions where `priority = 1`. The selection order is active high-priority mission count ascending, active mission count ascending, battery descending, then robot id/name as a stable tie breaker.

`COMPLETED` and `CANCELLED` missions do not count as active workload. If no suitable robot is available, the mission remains `PENDING`, stores `No available robot found for this mission.`, and does not run rule evaluation or strategy dispatch.

Mission Status / Mission History polish and Live Map integration have since been completed. What remains planned next: optionally add a Manager Mission Monitor using saved mission decision data and future Live Map mission movement driven by real mission state.

That mission processing update itself did not add barcode scan, customer priority queue, WebSocket, authentication, Spring Security, login/logout, user accounts, complex scheduler, task queue, fake robot movement, automatic mission completion, RuleParser rewrite, RuleEvaluator rewrite, RuleService rewrite, StrategyContext rewrite, or strategy class rewrite.

Verification on May 25, 2026:

* `mvn test` passed
* 36 tests passed
* `RobotAssignmentServiceTest` verifies fewer active missions are preferred
* `RobotAssignmentServiceTest` verifies fewer active high-priority missions are preferred
* `RobotAssignmentServiceTest` verifies completed and cancelled missions are ignored for active workload
* `StaffPickupRequestControllerTest` verifies mission processing stores assigned robot, assignment reason, matched rule, selected strategy, action message, decision summary, and processed time
* `StaffPickupRequestControllerTest` verifies no available robot keeps the mission `PENDING`, does not mark it `COMPLETED`, and shows `No available robot found for this mission.`
* Existing route coverage still verifies `/dashboard`, `/robots`, `/rules`, `/simulation`, `/system-flow`, `/staff/pickup-request`, `/staff/missions`, and `/manager/policy-assignment` return HTTP 200
* Existing simulation tests still pass, confirming simulation behavior remains intact
* Temporary app started on http://localhost:8091 with the configured local datasource for manual HTTP verification
* Manual route sweep returned HTTP 200 for `/dashboard`, `/robots`, `/rules`, `/simulation`, `/system-flow`, `/staff/pickup-request`, `/staff/missions`, and `/manager/policy-assignment`
* Manual verification created two high-priority Staff pickup missions and processed both from `/staff/missions`
* Manual verification confirmed the second high-priority mission was assigned to a different robot when another robot had less active high-priority workload
* Manual `/staff/missions` check confirmed assigned robot, assignment reason, matched rule, selected strategy, action message, and decision summary were displayed
* Manual `POST /simulation` returned HTTP 200 and still displayed `Critical Battery With Obstacle Rule`, `ChargingStrategy`, and Rule Evaluation Trace
* Temporary verification app process was stopped

---

## Manager Rule / Policy Assignment Page

* src/main/java/com/warehouse/model/ZonePolicyAssignment.java
* src/main/java/com/warehouse/repository/ZonePolicyAssignmentRepository.java
* src/main/java/com/warehouse/service/ZonePolicyAssignmentService.java
* src/main/java/com/warehouse/controller/ManagerPolicyAssignmentController.java
* src/main/java/com/warehouse/dto/ZonePolicyAssignmentDto.java
* src/main/resources/templates/manager-policy-assignment.html
* src/main/resources/templates/fragments/sidebar.html
* src/test/java/com/warehouse/controller/ManagerPolicyAssignmentControllerTest.java
* src/test/java/com/warehouse/controller/RoleNavigationControllerTest.java
* PROJECT_ROADMAP.md
* PROJECT_STATUS.md

Added database-backed Manager zone policy assignment. Manager can assign existing active Admin rules to Zone A / Small Cargo, Zone B / Medium Cargo, and Zone C / Large Cargo. The implementation stores `ruleId`, validates active rules on save, updates an existing zone assignment instead of creating duplicates, and leaves rule engine, strategy, robot selection, mission execution, authentication, barcode scan, queue logic, and live map behavior unchanged.

## PickupRequest / Mission Model and Service

* src/main/java/com/warehouse/model/Mission.java
* src/main/java/com/warehouse/model/MissionStatus.java
* src/main/java/com/warehouse/repository/MissionRepository.java
* src/main/java/com/warehouse/service/MissionService.java
* src/main/java/com/warehouse/controller/StaffPickupRequestController.java
* src/main/java/com/warehouse/dto/StaffPickupRequestDto.java
* src/main/resources/templates/staff-pickup-request.html
* src/main/resources/templates/staff-missions.html
* src/main/resources/templates/fragments/sidebar.html
* src/test/java/com/warehouse/controller/StaffPickupRequestControllerTest.java
* src/test/java/com/warehouse/controller/RoleNavigationControllerTest.java
* PROJECT_ROADMAP.md
* PROJECT_STATUS.md

Added database-backed Mission persistence for Staff pickup requests. The Staff form now saves PENDING missions, `/staff/missions` lists saved missions newest first, and Staff My Missions is a clickable navigation link. No rule engine, strategy, robot selection, Manager assignment, authentication, barcode scan, queue logic, realtime backend, or live map integration was changed.

## Standalone Live Warehouse Map Zone Flow Refinement

* docs/live-map-demo/live-map.html
* docs/live-map-demo/live-map.css
* docs/live-map-demo/live-map.js
* PROJECT_STATUS.md

Standalone static UI prototype refined to match the intended Zone C -> Zone B -> Zone A flow. Zone A no longer renders extra upper bridge connector marks, Base Station and Charging Station are visible only in Zone C, and backend / Thymeleaf integration remains intentionally pending.

## Staff Pickup Request Auto-Zone Location Refinement

* src/main/java/com/warehouse/controller/StaffPickupRequestController.java
* src/main/resources/templates/staff-pickup-request.html
* src/main/resources/static/css/app.css
* src/test/java/com/warehouse/controller/StaffPickupRequestControllerTest.java
* PROJECT_STATUS.md

Staff Pickup Request refined so cargo type automatically determines warehouse zone, the zone is visible but not manually selectable, and cargo location is selected from a 3x3 zone-based grid. Server-side validation rejects cargo/zone/location mismatches. No database persistence, Mission model, robot selection, RuleEvaluator connection, StrategyContext connection, authentication, or live map integration was added.

## Standalone Live Warehouse Map Single-Zone Refinement

* docs/live-map-demo/live-map.html
* docs/live-map-demo/live-map.css
* docs/live-map-demo/live-map.js
* PROJECT_STATUS.md

Standalone static UI prototype refined to show one selected zone at a time. Zone buttons now switch the visible zone, Mission Flow is displayed vertically in the right sidebar, and backend / Thymeleaf integration remains intentionally pending.

## Staff Pickup Request Page

* src/main/java/com/warehouse/controller/StaffPickupRequestController.java
* src/main/java/com/warehouse/dto/StaffPickupRequestDto.java
* src/main/resources/templates/staff-pickup-request.html
* src/main/resources/templates/fragments/sidebar.html
* src/main/resources/static/css/app.css
* src/test/java/com/warehouse/controller/StaffPickupRequestControllerTest.java
* src/test/java/com/warehouse/controller/RoleNavigationControllerTest.java
* PROJECT_ROADMAP.md
* PROJECT_STATUS.md

Staff Pickup Request page added as the first Staff workflow screen. It now supports validated Staff form entry and saves PENDING missions through the Mission model and service. Robot selection, RuleEvaluator connection, StrategyContext connection, authentication, and live map behavior remain unimplemented.

## Role-Based Navigation Structure

* src/main/resources/templates/fragments/sidebar.html
* src/main/resources/templates/dashboard.html
* src/main/resources/templates/robots.html
* src/main/resources/templates/rules.html
* src/main/resources/templates/simulation.html
* src/main/resources/templates/system-flow.html
* src/main/resources/static/css/app.css
* src/test/java/com/warehouse/controller/RoleNavigationControllerTest.java
* PROJECT_ROADMAP.md
* PROJECT_STATUS.md

Navigation changed to use a shared Thymeleaf sidebar grouped into Admin, Manager, and Staff sections. Implemented links still point to the existing routes. Staff Create Pickup Request, Staff My Missions, and Staff Live Warehouse Map are implemented links. Manager Mission Monitor remains disabled and labeled Planned.

## Standalone Live Warehouse Map HTML Demo

* docs/live-map-demo/live-map.html
* docs/live-map-demo/live-map.css
* docs/live-map-demo/live-map.js
* PROJECT_STATUS.md

Standalone static UI prototype added and refined for visual testing only. The current demo uses a single-zone center map with JavaScript zone switching, a cleaner shared SVG route network, 9 positioned package pads per zone, and a right-side vertical Mission Flow panel. Backend / Thymeleaf integration is intentionally not implemented in this task, and a future task can integrate the demo into a `/live-map` page after mission/request data exists.

## Admin / Manager / Staff Workflow Documentation Update

* DESIGN.md
* PROJECT_ROADMAP.md
* PROJECT_STATUS.md
* README.md

Documentation changed to describe the confirmed Admin, Manager, and Staff responsibilities, the larger warehouse pickup workflow, and the next implementation order. No implementation files were changed.

## Phase 5.2 Simulator Trace Details

* src/main/resources/templates/simulation.html
* src/main/resources/static/css/app.css
* src/test/java/com/warehouse/simulator/SimulationServiceTest.java
* PROJECT_STATUS.md

## Phase 5.3 Focused Architecture Tests

* src/test/java/com/warehouse/interpreter/RuleParserTest.java
* src/test/java/com/warehouse/service/RulePrioritySelectionTest.java
* src/test/java/com/warehouse/strategy/StrategyContextTest.java
* src/test/java/com/warehouse/simulator/SimulationServiceTest.java
* PROJECT_STATUS.md

## Phase 8 Final Documentation and Demo Preparation

* docs/UML_DIAGRAMS.md
* docs/FINAL_DEMO_SCRIPT.md
* README.md
* PROJECT_STATUS.md

## Phase 8 Final UI Polish and Screenshot Preparation

* src/main/resources/templates/dashboard.html
* src/main/resources/templates/rules.html
* src/main/resources/templates/simulation.html
* src/main/resources/templates/system-flow.html
* src/main/resources/static/css/app.css
* docs/SCREENSHOT_CHECKLIST.md
* PROJECT_STATUS.md

## Robot Management Page and Final UI Completion

* src/main/java/com/warehouse/controller/RobotManagementController.java
* src/main/java/com/warehouse/service/RobotService.java
* src/main/java/com/warehouse/repository/RobotRepository.java
* src/main/resources/templates/robots.html
* src/main/resources/templates/dashboard.html
* src/main/resources/templates/rules.html
* src/main/resources/templates/simulation.html
* src/main/resources/templates/system-flow.html
* src/test/java/com/warehouse/controller/RobotManagementControllerTest.java
* PROJECT_STATUS.md

---

# VERIFICATION STATUS

Mission processing flow verification on May 24, 2026:

* `mvn test` passed
* 32 tests passed
* `StaffPickupRequestControllerTest` verifies `GET /staff/missions` returns HTTP 200 and shows Process Mission for PENDING missions
* Test coverage verifies a valid Staff mission is created as `PENDING`
* Test coverage verifies `POST /staff/missions/{id}/process` processes a PENDING mission and redirects back to `/staff/missions`
* Test coverage verifies processed missions are stored as `ASSIGNED`, not `COMPLETED`
* Test coverage verifies processed missions store assigned robot, matched rule, selected strategy, action message, decision summary, and processed time
* Test coverage verifies mission processing can use a matching Zone A policy assignment for `Urgent Task Fast Route Rule`
* Test coverage verifies mission processing falls back cleanly when no zone policy assignment exists
* Test coverage verifies processing a non-existing mission redirects with an error message instead of crashing
* Existing simulation tests still pass
* Existing route coverage still verifies `/dashboard`, `/robots`, `/rules`, `/simulation`, `/system-flow`, `/manager/policy-assignment`, `/staff/pickup-request`, and `/staff/missions` return HTTP 200 and render role-based navigation
* Local app started on http://localhost:8087 using the current local SQL Server configuration
* Manual route sweep returned HTTP 200 for `/dashboard`, `/robots`, `/rules`, `/simulation`, `/system-flow`, `/staff/pickup-request`, `/staff/missions`, and `/manager/policy-assignment`
* Manual HTTP verification created a Staff pickup request from `/staff/pickup-request` and confirmed the mission appeared as `PENDING` on `/staff/missions`
* Manual HTTP verification submitted `POST /staff/missions/{id}/process` and received HTTP 302 back to `/staff/missions`
* Manual HTTP verification confirmed the processed mission displayed `ASSIGNED`, `FastRouteStrategy`, and assigned robot output
* Manual simulation POST still displayed `Low Battery Rule` and `ChargingStrategy`
* Manual Manager Policy Assignment page check still displayed `Current Policy`, `Zone A`, and `Active Admin Rules Available as Policies`
* No RuleParser, RuleEvaluator, RuleService, StrategyContext, strategy class, authentication, barcode scan, customer priority queue, WebSocket, complex scheduling, fake moving robot animation, automatic completion, or Live Warehouse Map integration was added or rewritten

---

Manager Rule / Policy Assignment verification on May 23, 2026:

* `mvn test` passed
* 29 tests passed
* `ManagerPolicyAssignmentControllerTest` verifies `GET /manager/policy-assignment` returns HTTP 200 and renders the `manager-policy-assignment` view
* Test coverage verifies the page shows Manager Rule / Policy Assignment
* Test coverage verifies the page shows Zone A, Zone B, Zone C, Small Cargo, Medium Cargo, and Large Cargo
* Test coverage verifies active Admin-created rules are listed as selectable policy assignments
* Test coverage verifies valid POST submit saves a Zone A / Small Cargo assignment
* Test coverage verifies a second valid POST for the same zone updates the existing assignment instead of creating a duplicate
* Test coverage verifies cargo type mismatch is rejected and does not save an assignment
* Test coverage verifies inactive rules are rejected and do not save an assignment
* `RoleNavigationControllerTest` verifies `/dashboard`, `/robots`, `/rules`, `/simulation`, `/system-flow`, `/manager/policy-assignment`, `/staff/pickup-request`, and `/staff/missions` return HTTP 200 and render the role-based navigation
* Temporary app started on http://localhost:8086 using the test H2 datasource through the Maven test classpath
* `/manager/policy-assignment` returned HTTP 200 and displayed Manager Rule / Policy Assignment, Zone A, and an active rule
* Local route sweep returned HTTP 200 for `/dashboard`, `/robots`, `/rules`, `/simulation`, `/system-flow`, `/staff/pickup-request`, `/staff/missions`, and `/manager/policy-assignment`
* No RuleParser, RuleEvaluator, RuleEngine, StrategyContext, strategy class, robot selection, mission execution, mission status automation, authentication, Spring Security, barcode scan, customer priority queue, fake robot movement, or live map integration was changed

Standalone live map SVG route network refinement verification on May 23, 2026:

* `node --check docs/live-map-demo/live-map.js` passed
* Static inspection confirmed the active demo files no longer use `.route-cell`, `.package-grid`, `.zone-bridge-indicator`, or `.bridge-route` for the zone map rendering
* Local Node DOM-stub verification confirmed Zone A renders one active zone, 9 package pads, 1 SVG route network, 13 route lines, 12 route nodes, 0 bridge lines, small cargo dots, and hides Base Station / Charging Station
* Local Node DOM-stub verification confirmed Zone B renders one active zone, 9 package pads, 1 SVG route network, 13 route lines, 12 route nodes, 2 minimal SVG bridge lines, medium cargo dots, and hides Base Station / Charging Station
* Local Node DOM-stub verification confirmed Zone C renders one active zone, 9 package pads, 1 SVG route network, 13 route lines, 12 route nodes, 2 minimal SVG bridge lines, large cargo dots, and shows Base Station / Charging Station
* Local Node DOM-stub verification confirmed Next Zone and Start Demo Route preserve the Zone C -> Zone B -> Zone A demo order
* Browser open command completed for `docs/live-map-demo/live-map.html`
* `mvn test` was not necessary because only standalone static demo files and `PROJECT_STATUS.md` were changed
* Standalone live map remains front-end only; backend / Thymeleaf integration is still pending and no backend, rule engine, strategy, database, WebSocket, polling, or authentication logic was changed

Standalone live map Zone C station connector verification on May 25, 2026:

* Added 2 station connector markers for Zone C
* Blue connector marker indicates route to Base Station
* Green connector marker indicates route to Charging Station
* Station connector markers appear only in Zone C
* Chrome headless browser verification confirmed Zone A renders 0 station connector markers and hides Base Station / Charging Station
* Chrome headless browser verification confirmed Zone B renders 0 station connector markers and hides Base Station / Charging Station
* Chrome headless browser verification confirmed Zone C renders 2 station connector markers, with the left marker blue and the right marker green
* Chrome headless browser verification confirmed Base Station and Charging Station cards still only appear in Zone C
* `node --check docs/live-map-demo/live-map.js` passed
* Standalone live map remains front-end only; no Spring Boot, Thymeleaf, backend logic, WebSocket, polling, or fake realtime backend data was added

Standalone live map robot legend update verification on May 26, 2026:

* Updated Live Map legend to support three robot types
* Added Picker Alpha, Mover Beta, and Carrier Gamma legend entries
* Picker Alpha uses the green robot class, Mover Beta uses the red robot class, and Carrier Gamma uses the blue robot class
* Removed the generic Active Route legend item
* Kept Small Cargo, Medium Cargo, Large Cargo, and Route Network legend items
* Prepared robot color classes for future robot-specific route patterns
* `node --check docs/live-map-demo/live-map.js` passed
* Headless Chrome verification confirmed the legend labels are Picker Alpha, Mover Beta, Carrier Gamma, Small Cargo, Medium Cargo, Large Cargo, and Route Network
* Headless Chrome verification confirmed Picker Alpha renders green, Mover Beta renders red, and Carrier Gamma renders blue, including robot pseudo-elements
* Headless Chrome verification confirmed the Active Route legend item is absent
* Headless Chrome verification confirmed Zone A, Zone B, and Zone C switching still renders 9 packages per zone with the expected cargo sizes
* Headless Chrome verification confirmed Zone C still renders 2 station connector markers and Base Station / Charging Station remain hidden outside Zone C
* Headless Chrome verification confirmed Next Zone and Start Demo Route still preserve the Zone C -> Zone B -> Zone A demo order
* Standalone live map remains front-end only; no Spring Boot, Thymeleaf, backend logic, WebSocket, polling, database logic, or fake realtime backend data was added

PickupRequest / Mission model and service verification on May 23, 2026:

* `mvn test` passed
* 25 tests passed
* `StaffPickupRequestControllerTest` verifies `GET /staff/pickup-request` returns HTTP 200
* Test coverage verifies valid Small Cargo with A1 saves a Mission with Zone A and status PENDING
* Test coverage verifies valid Medium Cargo with B5 saves a Mission with Zone B and status PENDING
* Test coverage verifies valid Large Cargo with C9 saves a Mission with Zone C and status PENDING
* Test coverage verifies invalid Small Cargo with Zone B and B1 is rejected and does not save a Mission
* Test coverage verifies `GET /staff/missions` returns HTTP 200 and displays saved missions
* `RoleNavigationControllerTest` verifies `/dashboard`, `/robots`, `/rules`, `/simulation`, `/system-flow`, `/staff/pickup-request`, and `/staff/missions` render the Admin, Manager, and Staff navigation sections
* Temporary app started on http://localhost:8085 using the default local SQL Server configuration
* `/staff/pickup-request` returned HTTP 200
* `/dashboard` returned HTTP 200
* `/robots` returned HTTP 200
* `/rules` returned HTTP 200
* `/simulation` returned HTTP 200
* `/system-flow` returned HTTP 200
* HTTP POST verification confirmed a valid Small Cargo / Zone A / A1 pickup request saves and displays a PENDING mission summary
* `/staff/missions` returned HTTP 200 and displayed the saved verification mission
* Temporary verification app process was stopped after route checks
* No RuleParser, RuleEvaluator, RuleEngine, StrategyContext, strategy class, robot selection, Manager assignment, barcode scan, customer priority queue, authentication, Spring Security, WebSocket, realtime backend, fake robot movement, fake mission completion, or live map integration was changed

Standalone live map Zone C -> Zone B -> Zone A flow refinement verification on May 23, 2026:

* `node --check docs/live-map-demo/live-map.js` passed
* Local Node DOM-stub verification confirmed Zone A renders 9 package cells, 0 bridge indicators, and hides Base Station / Charging Station
* Local Node DOM-stub verification confirmed Zone B renders 9 package cells, keeps a bridge indicator, and hides Base Station / Charging Station
* Local Node DOM-stub verification confirmed Zone C renders 9 package cells, keeps a bridge indicator, and shows Base Station / Charging Station
* Local Node DOM-stub verification confirmed the demo route data follows Zone C -> Zone B -> Zone A
* Browser open command completed for `docs/live-map-demo/live-map.html`
* Mission Flow remains in the right sidebar as a vertical step panel
* `mvn test` was not necessary because only standalone static demo files and `PROJECT_STATUS.md` were changed
* Standalone live map remains front-end only; backend / Thymeleaf integration is still pending and no backend, rule engine, strategy, database, WebSocket, polling, or authentication logic was changed

Staff Pickup Request auto-zone location refinement on May 23, 2026:

* `mvn test` passed
* 24 tests passed
* `StaffPickupRequestControllerTest` verifies `GET /staff/pickup-request` returns HTTP 200
* Test coverage verifies the page includes cargo type options Small Cargo, Medium Cargo, and Large Cargo
* Test coverage verifies the page documents Small Cargo -> Zone A, Medium Cargo -> Zone B, and Large Cargo -> Zone C
* Test coverage verifies valid Small Cargo with A1 returns HTTP 200 and shows Zone A and A1 in the summary
* Test coverage verifies valid Medium Cargo with B5 returns HTTP 200 and shows Zone B and B5 in the summary
* Test coverage verifies valid Large Cargo with C9 returns HTTP 200 and shows Zone C and C9 in the summary
* Test coverage verifies invalid Small Cargo with Zone B and B1 is rejected with validation messages
* Temporary app started on http://localhost:8082 using the local SQL Server configuration
* `/staff/pickup-request` returned HTTP 200
* `/dashboard` returned HTTP 200
* `/robots` returned HTTP 200
* `/rules` returned HTTP 200
* `/simulation` returned HTTP 200
* `/system-flow` returned HTTP 200
* HTTP verification confirmed the Staff page contains the cargo-zone mappings and location codes A1, A9, B5, and C9
* HTTP POST verification confirmed valid Small/A1, Medium/B5, and Large/C9 submissions show the expected zone and location in the request summary
* HTTP POST verification confirmed Small Cargo with Zone B and B1 is rejected
* Temporary verification app process was stopped after route checks
* No database schema, Mission model, repository, service, rule engine, RuleParser, RuleEvaluator, StrategyContext, strategy logic, authentication, Spring Security, robot selection, fake mission completion, fake robot movement, or live map integration was changed

Standalone live map single-zone refinement verification on May 23, 2026:

* `docs/live-map-demo/live-map.html` now contains one `#active-zone-map` container instead of the old full `zone-stack`
* Static inspection confirmed no static `package-pad` elements remain in the HTML; the active zone grid is rendered by `live-map.js`
* `live-map.js` keeps Zone A, Zone B, and Zone C definitions and renders 9 package route cells for the selected zone
* Local Node DOM-stub verification confirmed Zone A renders 9 small cargo cells, Zone B renders 9 medium cargo cells, and Zone C renders 9 large cargo cells
* Local Node DOM-stub verification confirmed the active zone button and status text update for Zone A, Zone B, and Zone C
* `node --check docs/live-map-demo/live-map.js` passed
* Browser open command completed for `docs/live-map-demo/live-map.html`
* Mission Flow is now inside the right sidebar above Cargo Guide and uses a vertical step layout
* `mvn test` was not necessary because only standalone static demo files and `PROJECT_STATUS.md` were changed
* Backend / Thymeleaf integration remains pending and no backend, rule engine, strategy, database, WebSocket, polling, or authentication logic was changed

Staff Pickup Request update on May 23, 2026:

* `mvn test` passed
* 21 tests passed
* `StaffPickupRequestControllerTest` verifies `GET /staff/pickup-request` returns HTTP 200, renders the `staff-pickup-request` view, provides form options, and shows the Staff page content
* `StaffPickupRequestControllerTest` verifies valid POST submit returns HTTP 200 and displays the saved mission summary
* `StaffPickupRequestControllerTest` verifies invalid POST submit returns HTTP 200 and displays validation messages
* `RoleNavigationControllerTest` now verifies `/dashboard`, `/robots`, `/rules`, `/simulation`, `/system-flow`, and `/staff/pickup-request` render the Admin, Manager, and Staff navigation sections
* Temporary app started on http://localhost:8081 using the local SQL Server configuration
* `/staff/pickup-request` returned HTTP 200
* `/dashboard` returned HTTP 200
* `/robots` returned HTTP 200
* `/rules` returned HTTP 200
* `/simulation` returned HTTP 200
* `/system-flow` returned HTTP 200
* Temporary verification app process was stopped after route checks
* No rule engine, RuleParser, RuleEvaluator, StrategyContext, strategy class, database schema, authentication, Spring Security, Mission entity, robot selection, or live map integration was changed

Role-based navigation update on May 23, 2026:

* `mvn test` passed
* 18 tests passed
* `RoleNavigationControllerTest` verifies `/dashboard`, `/robots`, `/rules`, `/simulation`, and `/system-flow` return HTTP 200 and render the Admin, Manager, and Staff navigation sections
* Existing implemented routes remain `/dashboard`, `/robots`, `/rules`, `/simulation`, and `/system-flow`
* Planned role-based pages are disabled sidebar items only: Manager Rule / Policy Assignment, Manager Mission Monitor, Staff My Missions, and Staff Live Warehouse Map
* No authentication, Spring Security, login/logout, pickup request logic, mission model, database schema, RuleParser, RuleEvaluator, RuleEngine, StrategyContext, or strategy classes were changed

Standalone live map demo verification on May 23, 2026:

* Created standalone static HTML/CSS/JS files under `docs/live-map-demo`
* Static inspection confirmed 27 package pads total, representing 9 packages per zone
* Static inspection confirmed 2 centered bridge groups and 4 bridge route elements, representing 2 routes between Zone A / Zone B and 2 routes between Zone B / Zone C
* `live-map.html` links directly to `live-map.css` and `live-map.js`
* Browser open command completed for `docs/live-map-demo/live-map.html`
* `mvn test` was not necessary because only standalone static demo files and `PROJECT_STATUS.md` were added
* No backend files, Thymeleaf templates, Interpreter Pattern logic, Strategy Pattern logic, database schema, WebSocket, or authentication behavior were changed

Documentation-only update on May 23, 2026:

* DESIGN.md updated with Admin / Manager / Staff workflow and pattern responsibilities
* PROJECT_ROADMAP.md updated with the planned role-based workflow task order
* PROJECT_STATUS.md updated with the current role-based workflow direction and next implementation priorities
* README.md updated with the new business flow overview
* No application code was changed
* Tests were not run because this task only changed documentation

Latest verification on May 22, 2026:

* `mvn test` passed
* 17 tests passed
* `RobotManagementControllerTest` verified `GET /robots` returns HTTP 200, the `robots` view, and a `robots` model attribute
* Temporary app started on http://localhost:8081 using the test H2 datasource for route verification
* `/robots` returned HTTP 200
* `/dashboard` returned HTTP 200
* `/rules` returned HTTP 200
* `/simulation` returned HTTP 200
* `/system-flow` returned HTTP 200
* `/robots` rendered database-backed robot cards without hardcoded fake robot or strategy names
* No RuleParser, rule priority, StrategyContext dispatch, or SimulationService behavior was changed

Previous verification on May 20, 2026:

* `mvn test` passed
* 16 tests passed
* Temporary app started on http://localhost:8081 using the test H2 datasource for route verification
* `/dashboard` returned HTTP 200
* `/rules` returned HTTP 200
* `/simulation` returned HTTP 200
* `/system-flow` returned HTTP 200
* Low Battery simulation returned HTTP 200 and displayed Low Battery Rule, ChargingStrategy, Rule Evaluation Trace, and Condition Evaluation Details
* Multiple-match simulation returned HTTP 200 and displayed Critical Battery With Obstacle Rule, ChargingStrategy, MATCHED, and Selected
* No-match simulation returned HTTP 200 and displayed No Rule Matched, NoStrategy, and SKIPPED
* Dashboard displayed persisted SIM-001 simulation history after manual simulation checks
* docs/SCREENSHOT_CHECKLIST.md reviewed

Previous verification on May 20, 2026:

* `mvn test` passed
* 16 tests passed
* docs/UML_DIAGRAMS.md reviewed
* docs/FINAL_DEMO_SCRIPT.md reviewed
* README.md reviewed
* Mermaid diagrams reviewed for simple Markdown-compatible syntax using flowchart, classDiagram, and sequenceDiagram blocks

Previous verification on May 20, 2026:

* `mvn test` passed
* 16 tests passed
* RuleParser single-condition tests passed for battery, obstacleDetected, robotLoad, distance, and priority
* RuleParser logical expression tests passed for AND and OR
* RuleService priority selection test passed for a robot state matching multiple active rules
* StrategyContext dispatch tests passed for all supported strategies and unknown strategy fallback
* SimulationService trace output test passed for input values, matched rule, selected strategy, action message, ruleResults, and conditionResults

Previous verification on May 19, 2026:

* `mvn test` passed
* 7 tests passed
* RuleParser tests passed
* SimulationService persistence test passed
* SimulationService rule trace and condition trace test passed
* RuleExecutionHistoryRepository newest-first retrieval test passed
* SystemFlowController route/model test passed
* Updated app started successfully on port 8080
* `/dashboard` returned HTTP 200
* `/rules` returned HTTP 200
* `/simulation` returned HTTP 200
* `/system-flow` returned HTTP 200
* `POST /simulation` returned HTTP 200
* Low Battery simulation selected Low Battery Rule and ChargingStrategy
* Obstacle simulation selected Obstacle Detection Rule and ObstacleAvoidanceStrategy
* Multiple-match simulation selected Critical Battery With Obstacle Rule and highlighted the selected trace row
* No-match simulation displayed No Rule Matched and NoStrategy without highlighting a selected rule
* Rule Evaluation Trace and Condition Evaluation Details appeared on simulation results
* Dashboard and System Flow pages still showed persisted SIM-001 execution history

---

# CURRENT PRIORITIES

## Priority 1

Documentation update for Admin / Manager / Staff workflow completed.

---

## Priority 2

Role-based navigation structure completed. Admin, Manager, and Staff sections now appear in the shared sidebar across the existing pages.

---

## Priority 3

Staff Pickup Request page completed as a basic form with automatic cargo type to zone mapping, a zone-based location grid, and saved PENDING mission summary.

---

## Priority 4

PickupRequest / Mission model and service completed. Staff pickup requests now save PENDING missions and can be reviewed at `/staff/missions`.

---

## Priority 5

Manager rule/policy assignment support completed for zone-based assignment of existing active Admin rules.

---

## Priority 6

Mission processing flow completed. Saved PENDING missions can now be processed with workload-aware robot selection, Manager zone policy lookup, existing `RuleEvaluator` evaluation, existing `StrategyContext` dispatch, assignment reason storage, decision output storage, and `ASSIGNED` status.

---

## Priority 7

Manager Robot Task Board completed. Manager can now monitor assigned mission workload per robot at `/manager/robot-tasks`.

---

## Priority 8

Mission status and mission history review completed. `/staff/missions` now shows active and historical mission data clearly, and `/staff/missions/{id}` provides read-only mission detail.

---

## Priority 9

Live Warehouse Map UI completed at `/staff/live-map` as a fullscreen standalone robot-focused map. It shows selected-robot Mission Flow with real active assigned mission data when available and a clean fallback when no active mission exists. Optional Manager Mission Monitor and future real mission movement remain planned.

---

# CURRENT RISKS

## Scope Risk

Avoid turning the project into a full AI system too early.

---

## Workflow Scope Risk

Keep the Admin / Manager / Staff workflow realistic for a graduation project. Avoid adding advanced automation until the final report and presentation are complete.

---

## Architecture Risk

Keep the Interpreter implementation educational and maintainable.

---

## Parser Complexity Risk

Avoid building a complicated DSL parser too soon.

---

## UI Risk

Keep the dashboard simple and educational.

---

# SUCCESS STATUS

* Interpreter Pattern fully demonstrated
* Strategy Pattern fully demonstrated
* Runtime behavior switching working
* Multi-condition evaluation working
* Dynamic rule composition working
* Database-driven rule management working
* SQL Server runtime verified locally
* Role-based Admin / Manager / Staff navigation structure working
* Staff Pickup Request form, auto-zone mapping, location grid, and database-backed PENDING mission creation working
* Staff My Missions review and PENDING mission processing working
* Staff Mission Status / History page working with priority badges, status badges, assignment data, rule/strategy output, and decision summaries
* Staff Mission Detail page working at `/staff/missions/{id}`
* Staff Live Warehouse Map working at `/staff/live-map`
* Manager Rule / Policy Assignment page working with database-backed zone policy assignments
* Manager Robot Task Board working with database-backed robot workload grouping
* Mission processing reuses existing RuleEvaluator and StrategyContext
* Educational robotics decision engine established

---

# NEXT MAJOR MILESTONE

## Final Presentation Preparation

### Goals

* Keep the existing Interpreter Pattern and Strategy Pattern core stable.
* Use the updated final demo script for the presentation flow.
* Capture screenshots for all completed Admin, Manager, Staff, Live Map, Simulation, and System Flow pages.
* Prepare the final report and presentation slides around the verified integrated workflow.

### Next Recommended Work

* UI polish.
* Screenshot checklist.
* Final report and presentation preparation.
* Optional later Live Map polish.
* Optional later barcode scan.
* Optional later customer priority queue.

---

# LONG TERM VISION

The project may later evolve into:

* Robotics Decision Middleware
* Warehouse Automation Simulator
* Runtime Logistics Rule Engine
* Educational Rule-Based Behavior System

without changing the core Interpreter + Strategy architecture.
