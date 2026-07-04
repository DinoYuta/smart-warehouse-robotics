# Battery, Charging, and Reassignment

## Battery drain model

Battery drain is waypoint-based. Integer division means a partial group of waypoints does not consume the next 1% until the group is complete.

| Movement mode | Waypoints per 1% | Typical source |
| --- | ---: | --- |
| `FAST` | 3 | `FastRouteStrategy` |
| `NORMAL` | 5 | Normal/default movement |
| `SAFE` | 5 | `SafeRouteStrategy` |
| `ENERGY_SAVING` | 7 | `EnergySavingStrategy` or low battery |
| `HEAVY_LOAD` | 5 | `HeavyLoadStrategy` or Large Cargo return |
| `OBSTACLE_AVOIDANCE` | 5 | Bridge wait or obstacle strategy |
| `CHARGING` | 5 | Charging display mode; route drain is not applied while charging |

These values come from `RobotMovementMode`. `RobotBatteryDrainService.calculateBatteryDrainPercent(...)` applies them. `RobotMissionBatteryService` uses the mission's start battery and persists a lower robot battery as route waypoints are traveled.

Low battery is `< 20%`. Critical battery is `<= 5%`. Critical battery sets charging required. A rule-selected `ChargingStrategy` also requires charging even when battery is above the critical threshold.

## Charging lifecycle

Normal mission flow:

1. The robot continues its current mission.
2. Return-to-Base processing calculates final traveled-route battery.
3. `RobotChargingService.prepareChargingDecision(...)` checks critical battery and rule-selected charging behavior.
4. If charging is not required, the robot becomes `IDLE` and can receive work.
5. If required, `startChargingAfterMissionClosure(...)` sets battery-at-start, charging flags, start time, status `CHARGING`, and position `charging-station` for Live Map output.
6. Charging recovers 5 percentage points per completed 10-second interval.
7. A later state read that reaches 100% calls `completeCharging(...)`, clears charging flags, and restores status `IDLE`.

There is no charging scheduler. `currentBatteryStatus(...)` calculates progress when Robot Management, Live Map, or Manager Task Board reads state.

Staff Stop on an in-progress mission is the explicit operator-driven exception to completing the normal route; it calculates current battery and may start the same charging workflow after cancellation.

## Queued mission reassignment

Reassignment runs only when the robot actually enters charging:

* only missions assigned to that robot with `PENDING` or `ASSIGNED` status are considered;
* the closed/current mission is excluded;
* the charging robot is excluded from candidate selection;
* `RobotAssignmentService.selectRobotForMissionExcluding(...)` selects another available robot by high-priority workload, total workload, battery, then stable order;
* if another robot exists, the queued mission remains `ASSIGNED` with a reassignment reason;
* if none exists, it becomes unassigned `PENDING`;
* `RuleEvaluator` and `StrategyContext` are not rerun during this reassignment.

Robots marked charging, offline, maintenance, error, unavailable, disabled, or out-of-service are excluded from new assignment.

## Code map

| Concern | Class/method |
| --- | --- |
| Drain calculation | `RobotBatteryDrainService` |
| Start battery and persisted drain | `RobotMissionBatteryService` |
| Strategy-to-movement mapping | `RobotExecutionBehaviorService` |
| Charging decision/state | `RobotChargingService` |
| Reassignment selection | `RobotAssignmentService` |
| Return trigger | `MissionService.markReturnedToBase(...)` |
| Persistent charging fields | `Robot.chargingRequired`, `charging`, `chargingStartedAt`, `chargingCompletedAt`, `batteryBeforeCharging` |

![Manager Robot Task Board](images/manager-robot-task-board.png)

Add the screenshot with this exact filename under `docs/images/`.

![Selected Robot Live Map](images/staff-live-map-selected-robot.png)

Add the screenshot with this exact filename under `docs/images/`.
