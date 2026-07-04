package com.warehouse.service;

import com.warehouse.dto.MissionExecutionProgressDto;
import com.warehouse.dto.MissionRouteStep;
import com.warehouse.model.Mission;
import com.warehouse.model.MissionExecutionStep;
import com.warehouse.model.MissionStatus;
import com.warehouse.model.RobotMovementMode;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MissionExecutionProgressService {

    public static final long NORMAL_WAYPOINT_SECONDS = 15;
    public static final long BRIDGE_WAYPOINT_SECONDS = 12;
    public static final long FAST_WAYPOINT_SECONDS = 9;
    public static final long FAST_BRIDGE_SECONDS = 8;
    public static final long ENERGY_SAVING_WAYPOINT_SECONDS = 18;
    public static final long ENERGY_SAVING_BRIDGE_SECONDS = 16;
    public static final long HEAVY_LOAD_WAYPOINT_SECONDS = 20;
    public static final long HEAVY_LOAD_BRIDGE_SECONDS = 18;
    public static final long PICKUP_PAUSE_SECONDS = 5;

    private static final String BASE_STATION_POSITION_KEY = "base-station";
    private static final String WAITING_TO_START_MESSAGE = "Mission assigned and waiting for Staff to start execution.";
    private static final String STARTED_WITHOUT_TIMESTAMP_MESSAGE = "Robot execution started from Base Station.";
    private static final String MOVING_TO_TARGET_MESSAGE = "Moving to target location.";
    private static final String PICKING_UP_MESSAGE = "Picking up cargo.";
    private static final String RETURNING_TO_BASE_MESSAGE = "Returning to Base Station.";
    public static final String RETURNED_TO_BASE_MESSAGE = "Returned to Base. Waiting for confirmation.";

    private final Clock clock;

    public MissionExecutionProgressService() {
        this(Clock.systemDefaultZone());
    }

    MissionExecutionProgressService(Clock clock) {
        this.clock = clock;
    }

    public MissionExecutionProgressDto calculateProgress(Mission mission, List<MissionRouteStep> route) {
        return calculateProgress(mission, route, RobotMovementMode.NORMAL);
    }

    public MissionExecutionProgressDto calculateProgress(Mission mission,
                                                         List<MissionRouteStep> route,
                                                         RobotMovementMode movementMode) {
        return calculateProgress(mission, route, movementMode, movementMode);
    }

    public MissionExecutionProgressDto calculateProgress(Mission mission,
                                                         List<MissionRouteStep> route,
                                                         RobotMovementMode outboundMovementMode,
                                                         RobotMovementMode returnMovementMode) {
        if (mission == null) {
            throw new IllegalArgumentException("Mission is required to calculate execution progress.");
        }
        if (route == null || route.isEmpty()) {
            throw new IllegalArgumentException("Route is required to calculate execution progress.");
        }
        if (mission.getStatus() != MissionStatus.IN_PROGRESS) {
            return missionSnapshotProgress(mission, 0);
        }
        if (mission.getExecutionStartedAt() == null) {
            return missionSnapshotProgress(mission, 0);
        }

        long elapsedSeconds = elapsedSecondsSince(mission.getExecutionStartedAt());
        int pickupIndex = pickupIndex(route);
        RobotMovementMode resolvedOutboundMovementMode = resolveMovementMode(outboundMovementMode);
        RobotMovementMode resolvedReturnMovementMode = resolveMovementMode(returnMovementMode);
        long pickupArrivalSeconds = arrivalSecondsForIndex(route, pickupIndex, resolvedOutboundMovementMode);
        long pickupFinishedSeconds = pickupArrivalSeconds + PICKUP_PAUSE_SECONDS;
        long routeFinishedSeconds = pickupFinishedSeconds + routeSecondsBetween(
                route,
                pickupIndex + 1,
                route.size() - 1,
                resolvedReturnMovementMode
        );

        if (elapsedSeconds >= routeFinishedSeconds) {
            MissionRouteStep finalStep = route.get(route.size() - 1);
            return new MissionExecutionProgressDto(
                    finalStep.positionKey(),
                    MissionExecutionStep.RETURNED_TO_BASE,
                    finalStep.phase().name(),
                    RETURNED_TO_BASE_MESSAGE,
                    elapsedSeconds,
                    null,
                    0.0,
                    traveledWaypointCountForIndex(route.size() - 1)
            );
        }

        if (elapsedSeconds >= pickupFinishedSeconds) {
            long elapsedAfterPickupSeconds = elapsedSeconds - pickupFinishedSeconds;
            MovementSegment segment = segmentForElapsed(
                    route,
                    elapsedAfterPickupSeconds,
                    pickupIndex + 1,
                    route.size() - 1,
                    resolvedReturnMovementMode
            );
            return new MissionExecutionProgressDto(
                    segment.currentStep().positionKey(),
                    MissionExecutionStep.RETURNING_TO_BASE,
                    MissionRouteStep.Phase.RETURN_TO_BASE.name(),
                    RETURNING_TO_BASE_MESSAGE,
                    elapsedSeconds,
                    segment.nextPositionKey(),
                    segment.progress(),
                    traveledWaypointCountForIndex(segment.currentIndex())
            );
        }

        if (elapsedSeconds >= pickupArrivalSeconds) {
            MissionRouteStep targetStep = route.get(pickupIndex);
            return new MissionExecutionProgressDto(
                    targetStep.positionKey(),
                    MissionExecutionStep.PICKING_UP,
                    targetStep.phase().name(),
                    PICKING_UP_MESSAGE,
                    elapsedSeconds,
                    null,
                    0.0,
                    traveledWaypointCountForIndex(pickupIndex)
            );
        }

        MovementSegment segment = segmentForElapsed(
                route,
                elapsedSeconds,
                1,
                pickupIndex,
                resolvedOutboundMovementMode
        );
        return new MissionExecutionProgressDto(
                segment.currentStep().positionKey(),
                MissionExecutionStep.MOVING_TO_TARGET,
                MissionRouteStep.Phase.MOVE_TO_TARGET.name(),
                MOVING_TO_TARGET_MESSAGE,
                elapsedSeconds,
                segment.nextPositionKey(),
                segment.progress(),
                traveledWaypointCountForIndex(segment.currentIndex())
        );
    }

    private MissionExecutionProgressDto missionSnapshotProgress(Mission mission, long elapsedSeconds) {
        MissionExecutionStep executionStep = mission.getExecutionStep() != null
                ? mission.getExecutionStep()
                : MissionExecutionStep.NOT_STARTED;
        String currentPositionKey = mission.getCurrentPositionKey() == null || mission.getCurrentPositionKey().isBlank()
                ? BASE_STATION_POSITION_KEY
                : mission.getCurrentPositionKey();
        String message = mission.getStatus() == MissionStatus.IN_PROGRESS
                ? STARTED_WITHOUT_TIMESTAMP_MESSAGE
                : WAITING_TO_START_MESSAGE;
        if (mission.getStatus() == MissionStatus.WAITING_CONFIRMATION
                || executionStep == MissionExecutionStep.RETURNED_TO_BASE) {
            currentPositionKey = BASE_STATION_POSITION_KEY;
            message = RETURNED_TO_BASE_MESSAGE;
        }

        return new MissionExecutionProgressDto(
                currentPositionKey,
                executionStep,
                null,
                message,
                elapsedSeconds
        );
    }

    private long elapsedSecondsSince(LocalDateTime executionStartedAt) {
        LocalDateTime now = LocalDateTime.now(clock);
        return Math.max(0, Duration.between(executionStartedAt, now).getSeconds());
    }

    private int pickupIndex(List<MissionRouteStep> route) {
        for (int index = 0; index < route.size(); index++) {
            if (route.get(index).phase() == MissionRouteStep.Phase.PICKUP) {
                return index;
            }
        }
        throw new IllegalArgumentException("Route must include a PICKUP step.");
    }

    private long arrivalSecondsForIndex(List<MissionRouteStep> route,
                                        int targetIndex,
                                        RobotMovementMode movementMode) {
        return routeSecondsBetween(route, 1, targetIndex, movementMode);
    }

    private long routeSecondsBetween(List<MissionRouteStep> route,
                                     int startIndex,
                                     int endIndex,
                                     RobotMovementMode movementMode) {
        long routeSeconds = 0;

        for (int index = startIndex; index <= endIndex; index++) {
            routeSeconds += waypointSecondsFor(route, index, movementMode);
        }

        return routeSeconds;
    }

    private MovementSegment segmentForElapsed(List<MissionRouteStep> route,
                                              long elapsedSeconds,
                                              int startIndex,
                                              int endIndex,
                                              RobotMovementMode movementMode) {
        if (elapsedSeconds <= 0 || startIndex > endIndex) {
            int currentIndex = Math.max(0, startIndex - 1);
            MissionRouteStep nextStep = startIndex <= endIndex ? route.get(startIndex) : null;
            return new MovementSegment(route.get(currentIndex), nextStep, 0.0, currentIndex);
        }

        long elapsedBeforeSegmentSeconds = 0;
        for (int index = startIndex; index <= endIndex; index++) {
            long segmentSeconds = waypointSecondsFor(route, index, movementMode);
            long segmentBoundarySeconds = elapsedBeforeSegmentSeconds + segmentSeconds;
            if (elapsedSeconds < segmentBoundarySeconds) {
                double segmentProgress = (double) (elapsedSeconds - elapsedBeforeSegmentSeconds) / segmentSeconds;
                return new MovementSegment(
                        route.get(index - 1),
                        route.get(index),
                        clampProgress(segmentProgress),
                        index - 1
                );
            }
            elapsedBeforeSegmentSeconds = segmentBoundarySeconds;
        }

        return new MovementSegment(route.get(endIndex), null, 1.0, endIndex);
    }

    private int traveledWaypointCountForIndex(int routeIndex) {
        return Math.max(0, routeIndex);
    }

    private double clampProgress(double progress) {
        return Math.max(0.0, Math.min(1.0, progress));
    }

    private long waypointSecondsFor(List<MissionRouteStep> route, int index, RobotMovementMode movementMode) {
        if (index <= 0 || index >= route.size()) {
            return waypointSecondsForMode(movementMode);
        }

        String previousPositionKey = route.get(index - 1).positionKey();
        String currentPositionKey = route.get(index).positionKey();
        if (isBridgeWaypoint(previousPositionKey) || isBridgeWaypoint(currentPositionKey)) {
            return bridgeSecondsForMode(movementMode);
        }

        return waypointSecondsForMode(movementMode);
    }

    private boolean isBridgeWaypoint(String positionKey) {
        return positionKey != null && positionKey.startsWith("bridge-");
    }

    private long waypointSecondsForMode(RobotMovementMode movementMode) {
        return switch (resolveMovementMode(movementMode)) {
            case FAST -> FAST_WAYPOINT_SECONDS;
            case ENERGY_SAVING -> ENERGY_SAVING_WAYPOINT_SECONDS;
            case HEAVY_LOAD -> HEAVY_LOAD_WAYPOINT_SECONDS;
            default -> NORMAL_WAYPOINT_SECONDS;
        };
    }

    private long bridgeSecondsForMode(RobotMovementMode movementMode) {
        return switch (resolveMovementMode(movementMode)) {
            case FAST -> FAST_BRIDGE_SECONDS;
            case ENERGY_SAVING -> ENERGY_SAVING_BRIDGE_SECONDS;
            case HEAVY_LOAD -> HEAVY_LOAD_BRIDGE_SECONDS;
            default -> BRIDGE_WAYPOINT_SECONDS;
        };
    }

    private RobotMovementMode resolveMovementMode(RobotMovementMode movementMode) {
        return movementMode != null ? movementMode : RobotMovementMode.NORMAL;
    }

    private record MovementSegment(MissionRouteStep currentStep,
                                   MissionRouteStep nextStep,
                                   double progress,
                                   int currentIndex) {

        private String nextPositionKey() {
            return nextStep == null ? null : nextStep.positionKey();
        }
    }
}
