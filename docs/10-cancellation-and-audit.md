# Cancellation and Audit

## Staff Stop/Cancel flow

Staff can stop missions in `PENDING`, `ASSIGNED`, `IN_PROGRESS`, or `WAITING_CONFIRMATION`. The form posts to `/staff/missions/{id}/stop` with:

* `cancellationReasonCode` - required and validated against `CancellationReason`;
* `cancellationNote` - optional free text;
* authenticated `Principal` name - stored as the cancelling user.

`MissionService.stopMission(...)` rejects a missing or unknown reason with `Please select a cancellation reason.` Successful Stop changes status to `CANCELLED` and stores the audit metadata.

## Current reason options

| Code | Display label |
| --- | --- |
| `CUSTOMER_CHANGED_REQUEST` | Customer changed request |
| `WRONG_CARGO_TYPE` | Wrong cargo type |
| `WRONG_LOCATION` | Wrong location |
| `DUPLICATE_REQUEST` | Duplicate request |
| `ROBOT_ISSUE` | Robot issue |
| `PACKAGE_NOT_FOUND` | Package not found |
| `OTHER` | Other |

These values are defined in `src/main/java/com/warehouse/model/CancellationReason.java`.

## Stored fields

| `Mission` field | Purpose |
| --- | --- |
| `cancellationReasonCode` | Stable reason code. |
| `cancellationNote` | Optional operational detail. |
| `cancelledBy` | Authenticated username when available. |
| `cancelledAt` | Cancellation timestamp. |
| `deletedAt` | Soft-delete timestamp if Staff later selects Delete. |

The data helps Manager review operational issues instead of seeing only a generic cancelled status.

## Visibility

* `staff-missions.html` shows cancellation reason, note, user, and time in history/details.
* `staff-mission-detail.html` shows the same metadata for one mission.
* `manager-robot-tasks.html` includes a Cancelled / Stopped Missions section with reason, note, user, and time.
* `ManagerRobotTaskBoardService` loads non-deleted `CANCELLED` missions for the Manager page.

Delete is a soft delete allowed only for `CANCELLED` missions. It sets `deletedAt`; it does not physically call repository delete.

## Code map

| Layer | Current code |
| --- | --- |
| Enum | `CancellationReason` |
| Persistent fields/display helpers | `Mission` |
| Validation and state change | `MissionService.stopMission(...)` |
| Staff route | `StaffPickupRequestController.stopMission(...)` |
| Staff forms | `staff-missions.html` |
| Manager aggregation | `ManagerRobotTaskBoardService.getRobotTaskBoard()` |
| Manager view | `manager-robot-tasks.html` |
| Repository query | `MissionRepository.findByStatusAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(...)` |

![Staff Missions Cancellation](images/staff-missions.png)

Add the screenshot with this exact filename under `docs/images/`.

![Manager Cancellation Review](images/manager-robot-task-board.png)

Add the screenshot with this exact filename under `docs/images/`.
