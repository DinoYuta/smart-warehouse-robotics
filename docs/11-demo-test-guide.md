# Demo and Test Guide

## 1. Preparation

### Standard local runtime

The default profile expects Microsoft SQL Server settings from `src/main/resources/application.properties`.

```bash
mvn spring-boot:run
```

### Self-contained classroom demo

The `demo` profile uses an in-memory H2 database:

```bash
mvn clean package
java -jar target/smart-warehouse-rule-engine-0.0.1-SNAPSHOT.jar --spring.profiles.active=demo
```

Open `http://localhost:8080/login`. H2 demo data may reset when the process restarts.

Before presenting, verify:

```bash
mvn test
```

Expected result: all tests pass and Maven reports `BUILD SUCCESS`.

## 2. Staff mobile demo

Use `Nova001 / nova001`.

1. Open `/staff/pickup-request`.
2. Enter a request code such as `REQ-DEMO-001`.
3. Choose a cargo type and confirm that zone and location choices match it.
4. Submit and confirm a `PENDING` mission appears.
5. Open `/staff/missions` and select **Process**.
6. Confirm assigned robot, assignment reason, matched rule, selected strategy, and action message are shown.
7. Select **Start Execution**.
8. Open `/staff/live-map` and select the assigned robot.
9. Confirm `/staff/live-map/state` returns JSON and the robot position changes over repeated polls.
10. Wait until the mission shows `WAITING_CONFIRMATION` / `RETURNED_TO_BASE`.
11. Confirm the robot may receive another task if it is not charging.
12. Select **Complete** and confirm the record becomes `COMPLETED`.

Negative check: try Complete before return. Expected result: the backend redirects safely with `Mission can only be completed after the robot returns to Base Station.`

Cancellation check: create another mission, select Stop without a reason, and confirm rejection. Then provide a reason and confirm `CANCELLED` details appear.

## 3. Rule and Strategy demonstrations

Use `/simulation` for isolated, repeatable rule checks. The current seeded rule order is:

1. Critical battery with obstacle -> `ChargingStrategy`
2. Obstacle -> `ObstacleAvoidanceStrategy`
3. Heavy load (`robotLoad > 80`) -> `HeavyLoadStrategy`
4. Low battery (`battery < 20`) -> `EnergySavingStrategy`
5. Urgent priority (`priority == 1`) -> `FastRouteStrategy`
6. Long distance (`distance > 15`) -> `SafeRouteStrategy`

### Fast route

| Input | Value |
| --- | ---: |
| battery | 70 |
| obstacleDetected | false |
| robotLoad | 30 |
| distance | 10 |
| priority | 1 |

Expected selected strategy: `FastRouteStrategy`.

### Heavy load with Large Cargo equivalent

| Input | Value |
| --- | ---: |
| battery | 70 |
| obstacleDetected | false |
| robotLoad | 90 |
| distance | 14 |
| priority | 2 |

Expected selected strategy: `HeavyLoadStrategy`.

### Medium Cargo after threshold change

1. Log in as Admin and open `/rules`.
2. Edit **Heavy Load Rule** from `robotLoad > 80` to `robotLoad >= 60`.
3. Simulate `robotLoad=60` with normal battery, no obstacle, distance at or below 15, and priority 2.
4. Expected selected strategy: `HeavyLoadStrategy`.
5. Restore `robotLoad > 80` after the demonstration if the original seeded behavior is required.

### Energy saving

Use battery 15, obstacle false, robotLoad 30, distance 10, priority 2.

Expected selected strategy: `EnergySavingStrategy`.

### Charging

Use battery 5, obstacle true, robotLoad 30, distance 10, priority 2.

Expected selected strategy: `ChargingStrategy` through the critical-battery-with-obstacle rule. For a real mission, charging begins after return, not immediately during outbound movement.

## 4. Manager demo

Use `Manage / manage`.

1. Open `/manager/policy-assignment`.
2. Assign an active rule to a zone and save it.
3. Process a matching Staff mission and confirm the decision summary says the zone policy matched.
4. Open `/manager/robot-tasks`.
5. Confirm robot workload, high-priority count, charging/availability, and unassigned pending sections.
6. Cancel a Staff mission with a reason.
7. Return to the task board and confirm reason, note, cancelled user, and time are visible.

## 5. Admin demo

Use `Admin / admin`.

1. Open `/dashboard` and explain the current fleet/rule/history summary.
2. Open `/rules`; create or edit a rule, choose an active strategy, set priority, and save.
3. Toggle the rule off and show that inactive rules are not evaluated.
4. Open `/robots` and explain the read-only backend-derived status.
5. Open `/simulation` and show rule trace plus leaf condition results.
6. Open `/system-flow` and explain:

```text
Robot Input -> Interpreter -> Rule Match -> Strategy Dispatch -> Robot Action -> History
```

## 6. Shared phone demo through a local tunnel

This exposes the running laptop application; it is not a separate cloud deployment.

1. Start the application on the laptop.
2. Start one tunnel targeting the same port:

```bash
cloudflared tunnel --url http://localhost:8080
```

or:

```bash
ngrok http 8080
```

3. Send the generated HTTPS URL to the teacher.
4. Keep the laptop, application process, and tunnel process running.
5. Log in on the phone with `Nova001 / nova001`.
6. Create a mission on the phone.
7. Open the same application instance on the laptop and confirm the mission is visible.
8. Open Live Map on both devices and confirm both use the same `/staff/live-map/state` data.

Do not enter `localhost` on the phone; that points to the phone itself.

## 7. Pass/fail checklist

| Check | Pass condition | Fail symptom |
| --- | --- | --- |
| Login/roles | Each account lands on and accesses only allowed route groups. | Unexpected 403, redirect loop, or cross-role access. |
| Pickup mapping | Small/A/30, Medium/B/60, Large/C/90. | Wrong zone, locations, or `robotLoad`. |
| Mission processing | Robot, rule, strategy, and decision are stored. | Mission remains pending despite available robot, or output missing. |
| Start execution | `ASSIGNED` becomes `IN_PROGRESS`. | Whitelabel page or unchanged status. |
| Live polling | Repeated state requests return HTTP 200 JSON and progress changes. | Poll errors, static fake state, or stuck mission. |
| Completion guard | Early Complete is blocked; returned mission completes. | Early completion succeeds or returned completion fails. |
| Robot availability | Returned non-charging robot can accept new work before Staff confirmation. | Waiting confirmation blocks assignment. |
| Charging | Required robot becomes unavailable and later returns to `IDLE` at 100%. | Charging robot receives new work or never completes on reads. |
| Cancellation | Missing reason rejected; stored reason visible to Manager. | Cancellation without reason or hidden audit data. |
| Interpreter | Rule trace and leaf results match input. | Wrong field/operator result. |
| Strategy | Selected strategy matches winning rule/policy. | Wrong or missing strategy dispatch. |
| Build | `mvn test` reports success. | Test failures/errors. |

![Staff Mobile Login](images/login-staff-mobile.png)

Add the screenshot with this exact filename under `docs/images/`.

![Execution Simulator](images/execution-simulator.png)

Add the screenshot with this exact filename under `docs/images/`.

![System Flow](images/system-flow.png)

Add the screenshot with this exact filename under `docs/images/`.
