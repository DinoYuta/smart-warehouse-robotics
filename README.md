# Smart Warehouse Robotics Decision Engine

## Project Overview

Smart Warehouse Robotics Decision Engine is a Java Spring Boot graduation project that simulates how warehouse robots can choose actions at runtime.

The system demonstrates a dynamic rule engine using:

* Interpreter Pattern for rule condition evaluation
* Strategy Pattern for runtime robot behavior dispatch

The project now includes a role-based warehouse workflow around the same technical core:

* Admin manages rules, strategies, robots, dashboard, simulation, and system flow.
* Manager assigns existing Admin-created rules as zone policies and monitors robot workloads.
* Staff creates pickup requests, processes missions, and manages mission lifecycle actions.

Robot decisions are based on values such as:

* battery level
* obstacle detection
* robot load
* travel distance
* task priority

In the mission workflow, `robotLoad` means estimated cargo load percentage, not zone size:

* Small Cargo = 30% load -> Zone A
* Medium Cargo = 60% load -> Zone B
* Large Cargo = 90% load -> Zone C

The rule threshold controls which cargo types trigger heavy-load behavior. For example, `robotLoad >= 80` matches Large Cargo, while `robotLoad >= 60` matches Medium Cargo and Large Cargo.

## Core Flow

Technical decision flow:

```text
Robot Input -> Interpreter -> Rule Match -> Strategy Dispatch -> Robot Action -> History
```

Role-based business flow:

```text
Staff Pickup Request -> Workload-aware Robot Assignment -> Manager Zone Policy -> RuleEvaluator -> StrategyContext -> Mission Status / Live Map
```

The Live Map remains a polling visualization layer. When backend progress reaches Base Station, it can persist the returned-to-base / waiting-confirmation state and robot availability, but it does not automatically complete missions.

## Customer Pickup Code Flow

Manager and Admin users can create customer pickup codes at `/manager/customer-pickup-codes`.

Flow:

1. Manager enters the customer name, email, phone number, cargo type, and pickup location.
2. The system validates the customer data and creates a unique uppercase pickup code.
3. The page shows a demo email preview containing the pickup code. No Gmail, SMTP, external email API, app password, or secret is used.
4. The customer gives the pickup code to Staff.
5. Staff enters the code on `/staff/pickup-request`.
6. If the code is unused, the system shows the customer, cargo, location, and `Priority: High`.
7. Staff processes the pickup code.
8. The system creates a High priority mission from the code data and processes it through the existing `RuleEvaluator` and `StrategyContext` flow.
9. Staff continues with the existing Staff Missions, Start Execution, and Live Map workflow.
10. The pickup code becomes `USED` and cannot create another mission.

## Technology Stack

* Java 17
* Spring Boot
* Spring MVC
* Spring Data JPA
* Thymeleaf
* Bootstrap
* Microsoft SQL Server for local runtime data
* H2 database for tests
* Maven

## Main Design Patterns

### Interpreter Pattern

The Interpreter Pattern parses and evaluates rule expressions such as:

```text
battery < 20 AND obstacleDetected == TRUE
```

Important classes:

* `RuleParser`
* `RuleEvaluator`
* `Expression`
* `BatteryExpression`
* `ObstacleExpression`
* `RobotLoadExpression`
* `DistanceExpression`
* `PriorityExpression`
* `AndExpression`
* `OrExpression`

### Strategy Pattern

The Strategy Pattern selects robot behavior dynamically after a rule matches.

Supported strategies include:

* `ChargingStrategy`
* `ObstacleAvoidanceStrategy`
* `HeavyLoadStrategy`
* `EnergySavingStrategy`
* `FastRouteStrategy`
* `SafeRouteStrategy`

`StrategyContext` receives the selected strategy name and dispatches the correct strategy implementation.

## Main Features

* Dashboard with robot overview, active rules, active strategies, and recent execution history
* Admin Rule Management with create, edit, delete, enable, and disable actions
* Priority-based Interpreter rule evaluation
* Strategy-based robot behavior dispatch
* Robot Management page
* Simulation page with rule trace and condition details
* System Flow page explaining the architecture
* Staff Pickup Request page with cargo-to-zone mapping and A1-C9 location grids
* Manager Customer Pickup Codes page with demo email preview
* Staff Customer Pickup Code lookup and process flow
* Admin User Management page with BCrypt password changes
* Staff Missions page with process, completed, stop, and delete-after-stop lifecycle actions
* Manager Rule / Policy Assignment for Zone A, Zone B, and Zone C
* Workload-aware robot assignment during mission processing
* Manager Robot Task Board grouped by assigned robot workload
* Fullscreen Live Warehouse Map visualization page

## Authentication & Authorization

Authentication is session-based Spring Security backed by the database table `app_users`.
Passwords are stored in `password_hash` as BCrypt hashes, never as plain text.

The demo seed creates these accounts idempotently if they do not already exist:

* `Admin / admin` with role `ADMIN`
* `Manage / manage` with role `MANAGE`
* `Nova001 / nova001` with role `STAFF`

Role hierarchy is preserved in Spring Security authorities:

```text
ADMIN > MANAGE > STAFF
```

Admin can access Admin, Manager, and Staff routes. Manager can access Manager
and Staff routes. Staff can access Staff routes only. The public login page
shows only the Staff demo credentials (`Nova001 / nova001`); Admin and Manager
accounts still exist in the database but are not displayed publicly.

These demo passwords are for classroom presentation only. Do not use them in
production.

## User Management

Admin users can manage classroom demo accounts at `/admin/users`.

The User Management page lists usernames, roles, enabled status, created time,
updated time, and a Change Password action. It never displays raw passwords or
stored password hashes.

When an Admin changes a password, the new value is validated, encoded with
`BCryptPasswordEncoder`, and saved back to `app_users.password_hash`. The old
password no longer works for new login attempts after the update.

This feature is intentionally limited to Admin maintenance. It does not add
registration, forgot password, email reset, OTP, JWT, OAuth, or external login
providers.

## How To Run The Project

Start the application on the default port:

```bash
mvn spring-boot:run
```

Start the application on port 8081:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

The runtime application is configured for Microsoft SQL Server. Check `src/main/resources/application.properties` for local database settings before starting the app.

## Main Routes

* `http://localhost:8080/dashboard`
* `http://localhost:8080/admin/users`
* `http://localhost:8080/robots`
* `http://localhost:8080/rules`
* `http://localhost:8080/simulation`
* `http://localhost:8080/system-flow`
* `http://localhost:8080/staff/pickup-request`
* `http://localhost:8080/staff/missions`
* `http://localhost:8080/staff/live-map`
* `http://localhost:8080/manager/policy-assignment`
* `http://localhost:8080/manager/robot-tasks`
* `http://localhost:8080/manager/customer-pickup-codes`

## How To Run Tests

Run the full test suite:

```bash
mvn test
```

Tests use the H2 database configuration in `src/test/resources/application.properties`.

## Shared Local Mobile Demo Using Public Tunnel

This method lets the teacher open the Staff workflow on a phone while the application still runs on the user's laptop. The teacher phone and the laptop browser see the same missions because both devices connect to the same running local Spring Boot app instance.

Demo path:

```text
Teacher phone -> public tunnel URL -> user's laptop localhost Spring Boot app -> same local database/session/runtime
```

Requirements:

* The laptop must stay on.
* The Spring Boot app must keep running.
* The public tunnel command must keep running.
* If the terminal or tunnel is closed, the teacher link stops working.
* If the app uses the local SQL Server database, that database is kept unchanged and both devices use the same local data.
* If the app is run with an in-memory demo database, both devices still share the same running app instance, but data resets when the app restarts.

### Option A: Cloudflare Tunnel

1. Start the Spring Boot app on the laptop:

```bash
mvn spring-boot:run
```

If the project is being tested on a different local port, start it with the current project run command, for example:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8095
```

2. Start Cloudflare Tunnel against the same local port.

For an app running on port `8080`:

```bash
cloudflared tunnel --url http://localhost:8080
```

For an app running on port `8095`:

```bash
cloudflared tunnel --url http://localhost:8095
```

3. Copy the generated HTTPS URL.
4. Send the HTTPS URL to the teacher.
5. The teacher logs in with:

```text
username: Nova001
password: nova001
```

### Option B: ngrok

1. Start the Spring Boot app on the laptop.
2. Start ngrok against the same local port.

For an app running on port `8080`:

```bash
ngrok http 8080
```

For an app running on port `8095`:

```bash
ngrok http 8095
```

3. Copy the generated HTTPS URL.
4. Send the HTTPS URL to the teacher.
5. The teacher logs in with `Nova001 / nova001`.

### Shared Demo Check

1. Open localhost or the same tunnel URL on the laptop and log in as Staff.
2. Open the tunnel URL on the teacher phone and log in as Staff.
3. Create a pickup request on the phone.
4. Refresh or open Staff Missions on the laptop.
5. Confirm the laptop sees the mission created from the phone.
6. Process and start the mission from either device.
7. Open Live Map on both devices.
8. Confirm both devices show the same robot state from `/staff/live-map/state`.
9. Wait for the robot to return to Base Station.
10. Confirm Complete becomes available only after return to Base.

### Troubleshooting

If the teacher cannot open the link:

* Check laptop internet.
* Check that Spring Boot is still running.
* Check that the tunnel command is still running.
* Check that the tunnel targets the correct port.
* Check any firewall or security prompt on the laptop.

If the phone does not update:

* Refresh the page.
* Check both devices use the same tunnel URL.
* Do not use `localhost` on the phone. `localhost` on the phone means the phone itself, not the laptop.

If Live Map state fails:

* Open `/staff/live-map/state` from the tunnel URL after logging in.
* Verify the endpoint returns JSON from the same tunnel host.

If login redirects incorrectly:

* Check that the app is being opened through the tunnel host or laptop localhost consistently.
* Check security configuration and relative URLs.

## Optional Existing Cloud Run Demo Deployment

This is an existing optional deployment path. It is not required for the shared local tunnel demo above.

### Demo Purpose

This deployment mode is for a public teacher demo where the Staff workflow can be tested directly from a mobile phone using a Cloud Run URL.

Primary Staff workflow:

```text
Login -> Staff Pickup Request -> Staff Missions -> Start Execution -> Live Map -> Returned to Base -> Complete
```

### Public Demo Login

Only the Staff demo account is shown on the public login page:

```text
username: Nova001
password: nova001
```

Admin and Manager accounts still exist for local/admin testing, but they are not displayed on the public login screen.

### Local Demo Profile

Run the demo profile without requiring local SQL Server:

```bash
mvn clean package
java -jar target/*.jar --spring.profiles.active=demo
```

The app listens on `server.port=${PORT:8080}`, so Cloud Run can provide the runtime port through the `PORT` environment variable.

### Docker

Build and run the demo image locally:

```bash
docker build -t warehouse-robotics-demo .
docker run -p 8080:8080 -e PORT=8080 -e SPRING_PROFILES_ACTIVE=demo warehouse-robotics-demo
```

### Cloud Run

Deploy from source:

```bash
gcloud run deploy warehouse-robotics-demo --source . --region asia-southeast1 --allow-unauthenticated --set-env-vars SPRING_PROFILES_ACTIVE=demo
```

After deployment, open the Cloud Run service URL on a phone and log in as `Nova001 / nova001`.

### Demo Data and Limitations

* The `demo` profile uses H2 and does not require localhost SQL Server.
* Seed data is idempotent and includes the three demo robots: Picker Alpha, Mover Beta, and Carrier Gamma.
* Active rules and strategies are seeded for mission processing.
* Staff cargo locations are available as Zone A / A1-A9, Zone B / B1-B9, and Zone C / C1-C9.
* Demo accounts are for classroom testing only.
* No real warehouse hardware is connected.
* Live Map state comes from backend mission/robot simulation endpoints, not frontend fake data.
* H2 demo data may reset when the Cloud Run instance restarts.

## Demo Flow Summary

1. Open `/dashboard` for the system overview.
2. Open `/rules` to show Admin-created rules and strategy selection.
3. Open `/manager/policy-assignment` to assign rules to Zone A, Zone B, and Zone C.
4. Open `/manager/robot-tasks` to show robot workload distribution.
5. Open `/staff/pickup-request` and create a pickup mission.
6. Open `/staff/missions`, process the mission, and show assigned robot, matched rule, selected strategy, and decision summary.
7. Demonstrate Completed, Stop, and Delete-after-Stop lifecycle behavior.
8. Open `/staff/live-map` to show the visual route flow.
9. Open `/simulation` and `/system-flow` to explain Interpreter Pattern and Strategy Pattern.

## Documentation

The maintainer and reviewer guide starts at [Documentation Index](docs/README.md).

* [Project overview](docs/00-project-overview.md)
* [Staff workflow](docs/02-staff-workflow.md)
* [Interpreter Pattern](docs/06-rule-engine-interpreter-pattern.md)
* [Strategy Pattern](docs/07-strategy-pattern.md)
* [Live Map simulation](docs/08-live-map-simulation.md)
* [Demo and test guide](docs/11-demo-test-guide.md)
* [Central code map](docs/12-code-map.md)

## Project Goal

The goal is to keep the system modular, educational, and presentation-ready while showing how Interpreter and Strategy patterns can work together in an enterprise-style warehouse robotics decision engine.
