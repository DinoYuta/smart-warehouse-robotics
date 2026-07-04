# Manager Workflow

The `Manage` account has both `MANAGE` and `STAFF` roles. A Manager can therefore use the Staff workflow and the two Manager pages, but cannot access Admin-only configuration pages.

## Rule / Policy Assignment

Route: `/manager/policy-assignment`

The page assigns one active Admin-created `Rule` to each supported zone:

| Zone | Required cargo type |
| --- | --- |
| Zone A | Small Cargo |
| Zone B | Medium Cargo |
| Zone C | Large Cargo |

`ZonePolicyAssignmentService.assignPolicy(...)` validates that the zone/cargo pair is correct and that the selected rule exists and is active. The assignment stores `zone`, `cargoType`, and `ruleId` in `ZonePolicyAssignment`.

During mission processing, `MissionProcessingService` evaluates all active rules. If the assigned zone policy rule is active and matches the mission input, that matched policy rule is selected. If the policy is missing, inactive, unavailable, or does not match, processing falls back to the first matched active rule in normal priority order.

## Robot Task Board

Route: `/manager/robot-tasks`

`ManagerRobotTaskBoardService.getRobotTaskBoard()` groups active missions by assigned robot and shows:

* active `PENDING`, `ASSIGNED`, and `IN_PROGRESS` workload;
* high-priority workload;
* `WAITING_CONFIRMATION` count separately;
* battery, strategy, obstacle, and charging state from backend Live Map data;
* unassigned pending missions;
* cancelled missions with reason, note, user, and time.

`WAITING_CONFIRMATION` is deliberately excluded from active assignment workload because the robot has already returned to Base.

## Code map

| Layer | Policy Assignment | Robot Task Board |
| --- | --- | --- |
| Controller | `ManagerPolicyAssignmentController` | `ManagerRobotTaskBoardController` |
| Service | `ZonePolicyAssignmentService` | `ManagerRobotTaskBoardService` |
| Template | `manager-policy-assignment.html` | `manager-robot-tasks.html` |
| Entity/DTO | `ZonePolicyAssignment`, `ZonePolicyAssignmentDto` | `RobotTaskBoardDto`, `RobotTaskGroupDto`, `Mission`, `Robot` |
| Repository | `ZonePolicyAssignmentRepository`, `RuleRepository` | `MissionRepository`, `RobotRepository` |
| Supporting live state | Not required to save a policy | `LiveMapStateService`, `RobotChargingService` |

![Manager Policy Assignment](images/manager-policy-assignment.png)

Add the screenshot with this exact filename under `docs/images/`.

![Manager Robot Task Board](images/manager-robot-task-board.png)

Add the screenshot with this exact filename under `docs/images/`.
