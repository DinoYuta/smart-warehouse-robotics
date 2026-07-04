package com.warehouse.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.warehouse.dto.MissionExecutionProgressDto;
import com.warehouse.dto.MissionRouteStep;
import com.warehouse.model.Mission;
import com.warehouse.model.MissionExecutionStep;
import com.warehouse.model.MissionStatus;
import com.warehouse.model.RobotMovementMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class MissionExecutionProgressServiceTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-06-02T03:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);

    private final WarehouseRouteService warehouseRouteService = new WarehouseRouteService();
    private final MissionExecutionProgressService progressService = new MissionExecutionProgressService(FIXED_CLOCK);

    @Test
    void movementTimingConstantsAreSlowAndEasyToAdjust() {
        assertThat(MissionExecutionProgressService.FAST_WAYPOINT_SECONDS).isEqualTo(9);
        assertThat(MissionExecutionProgressService.FAST_BRIDGE_SECONDS).isEqualTo(8);
        assertThat(MissionExecutionProgressService.NORMAL_WAYPOINT_SECONDS).isEqualTo(15);
        assertThat(MissionExecutionProgressService.BRIDGE_WAYPOINT_SECONDS).isEqualTo(12);
        assertThat(MissionExecutionProgressService.ENERGY_SAVING_WAYPOINT_SECONDS).isEqualTo(18);
        assertThat(MissionExecutionProgressService.ENERGY_SAVING_BRIDGE_SECONDS).isEqualTo(16);
        assertThat(MissionExecutionProgressService.HEAVY_LOAD_WAYPOINT_SECONDS).isEqualTo(20);
        assertThat(MissionExecutionProgressService.HEAVY_LOAD_BRIDGE_SECONDS).isEqualTo(18);
        assertThat(MissionExecutionProgressService.PICKUP_PAUSE_SECONDS).isEqualTo(5);
    }

    @Test
    void inProgressMissionAtElapsedZeroStartsAtBaseStation() {
        Mission mission = inProgressMission("REQ-START", "Zone C", "C5", 0);
        List<MissionRouteStep> route = warehouseRouteService.buildExecutionRoute(mission);

        MissionExecutionProgressDto progress = progressService.calculateProgress(mission, route);

        assertThat(progress.positionKey()).isEqualTo("base-station");
        assertThat(progress.executionStep()).isEqualTo(MissionExecutionStep.MOVING_TO_TARGET);
        assertThat(progress.phase()).isEqualTo("MOVE_TO_TARGET");
        assertThat(progress.elapsedSeconds()).isZero();
        assertThat(progress.nextPositionKey()).isEqualTo("zone-c-left-entry");
        assertThat(progress.segmentProgress()).isZero();
        assertThat(progress.traveledWaypointCount()).isZero();
    }

    @Test
    void earlyElapsedTimeReturnsMoveToTargetWaypoint() {
        Mission mission = inProgressMission("REQ-EARLY", "Zone B", "B5", 46);
        List<MissionRouteStep> route = warehouseRouteService.buildExecutionRoute(mission);

        MissionExecutionProgressDto progress = progressService.calculateProgress(mission, route);

        assertThat(progress.positionKey()).isEqualTo("zone-c-left-main-2");
        assertThat(progress.nextPositionKey()).isEqualTo("bridge-c-b-left-1");
        assertThat(progress.segmentProgress()).isBetween(0.08, 0.09);
        assertThat(progress.executionStep()).isEqualTo(MissionExecutionStep.MOVING_TO_TARGET);
        assertThat(progress.phase()).isEqualTo("MOVE_TO_TARGET");
        assertThat(progress.message()).isEqualTo("Moving to target location.");
        assertThat(progress.traveledWaypointCount()).isEqualTo(3);
    }

    @Test
    void reachingTargetKeepsMissionPickingUpDuringPickupPause() {
        Mission mission = inProgressMission("REQ-PICKUP", "Zone C", "C5", 60);
        List<MissionRouteStep> route = warehouseRouteService.buildExecutionRoute(mission);

        MissionExecutionProgressDto progress = progressService.calculateProgress(mission, route);

        assertThat(progress.positionKey()).isEqualTo("C5");
        assertThat(progress.nextPositionKey()).isNull();
        assertThat(progress.segmentProgress()).isZero();
        assertThat(progress.executionStep()).isEqualTo(MissionExecutionStep.PICKING_UP);
        assertThat(progress.phase()).isEqualTo("PICKUP");
        assertThat(progress.message()).isEqualTo("Picking up cargo.");
        assertThat(progress.traveledWaypointCount()).isEqualTo(4);
    }

    @Test
    void afterPickupPauseRouteReturnsTowardBaseStation() {
        Mission mission = inProgressMission("REQ-RETURN", "Zone B", "B5", 132);
        List<MissionRouteStep> route = warehouseRouteService.buildExecutionRoute(mission);

        MissionExecutionProgressDto progress = progressService.calculateProgress(mission, route);

        assertThat(progress.positionKey()).isEqualTo("B5");
        assertThat(progress.nextPositionKey()).isEqualTo("B5-approach");
        assertThat(progress.segmentProgress()).isBetween(0.06, 0.07);
        assertThat(progress.executionStep()).isEqualTo(MissionExecutionStep.RETURNING_TO_BASE);
        assertThat(progress.phase()).isEqualTo("RETURN_TO_BASE");
        assertThat(progress.message()).isEqualTo("Returning to Base Station.");
        assertThat(progress.traveledWaypointCount()).isEqualTo(9);
    }

    @Test
    void afterFinalRouteTimeMissionReturnsToBaseButDoesNotComplete() {
        Mission mission = inProgressMission("REQ-RETURNED", "Zone B", "B5", 257);
        List<MissionRouteStep> route = warehouseRouteService.buildExecutionRoute(mission);

        MissionExecutionProgressDto progress = progressService.calculateProgress(mission, route);

        assertThat(progress.positionKey()).isEqualTo("base-station");
        assertThat(progress.nextPositionKey()).isNull();
        assertThat(progress.segmentProgress()).isZero();
        assertThat(progress.executionStep()).isEqualTo(MissionExecutionStep.RETURNED_TO_BASE);
        assertThat(progress.phase()).isEqualTo("RETURN_TO_BASE");
        assertThat(progress.message()).isEqualTo("Returned to Base. Waiting for confirmation.");
        assertThat(progress.traveledWaypointCount()).isEqualTo(18);
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.IN_PROGRESS);
        assertThat(mission.getCompletedAt()).isNull();
    }

    @Test
    void traveledWaypointCountUsesCompletedWaypointBoundariesOnly() {
        List<MissionRouteStep> route = warehouseRouteService.buildExecutionRoute("Zone B", "B5");

        assertThat(progressFor(route, inProgressMission("REQ-BATTERY-0", "Zone B", "B5", 0)).traveledWaypointCount())
                .isZero();
        assertThat(progressFor(route, inProgressMission("REQ-BATTERY-9", "Zone B", "B5", 145)).traveledWaypointCount())
                .isEqualTo(9);
        assertThat(progressFor(route, inProgressMission("REQ-BATTERY-10", "Zone B", "B5", 146)).traveledWaypointCount())
                .isEqualTo(10);
        assertThat(progressFor(route, inProgressMission("REQ-BATTERY-18", "Zone B", "B5", 257)).traveledWaypointCount())
                .isEqualTo(18);
    }

    @Test
    void strategyMovementModesChangeRouteTimingForSameElapsedTime() {
        List<MissionRouteStep> route = warehouseRouteService.buildExecutionRoute("Zone B", "B5");
        Mission mission = inProgressMission("REQ-MODE-TIMING", "Zone B", "B5", 90);

        MissionExecutionProgressDto fastProgress = progressService.calculateProgress(
                mission,
                route,
                RobotMovementMode.FAST
        );
        MissionExecutionProgressDto normalProgress = progressService.calculateProgress(
                mission,
                route,
                RobotMovementMode.NORMAL
        );
        MissionExecutionProgressDto energySavingProgress = progressService.calculateProgress(
                mission,
                route,
                RobotMovementMode.ENERGY_SAVING
        );

        assertThat(fastProgress.traveledWaypointCount()).isGreaterThan(normalProgress.traveledWaypointCount());
        assertThat(energySavingProgress.traveledWaypointCount()).isLessThan(normalProgress.traveledWaypointCount());
    }

    @Test
    void heavyLoadReturnModeSlowsOnlyReturnPhaseAfterPickup() {
        List<MissionRouteStep> route = warehouseRouteService.buildExecutionRoute("Zone C", "C5");
        Mission mission = inProgressMission("REQ-HEAVY-RETURN", "Zone C", "C5", 52);

        MissionExecutionProgressDto fastOnlyProgress = progressService.calculateProgress(
                mission,
                route,
                RobotMovementMode.FAST,
                RobotMovementMode.FAST
        );
        MissionExecutionProgressDto fastOutboundHeavyReturnProgress = progressService.calculateProgress(
                mission,
                route,
                RobotMovementMode.FAST,
                RobotMovementMode.HEAVY_LOAD
        );

        assertThat(fastOnlyProgress.executionStep()).isEqualTo(MissionExecutionStep.RETURNING_TO_BASE);
        assertThat(fastOutboundHeavyReturnProgress.executionStep()).isEqualTo(MissionExecutionStep.RETURNING_TO_BASE);
        assertThat(fastOutboundHeavyReturnProgress.traveledWaypointCount())
                .isLessThan(fastOnlyProgress.traveledWaypointCount());
        assertThat(fastOutboundHeavyReturnProgress.positionKey()).isEqualTo("C5");
    }

    @Test
    void zoneCTargetProgressesFromBaseThroughZoneCTargetAndBackToBase() {
        List<MissionRouteStep> route = warehouseRouteService.buildExecutionRoute("Zone C", "C5");

        assertProgressAt(route, inProgressMission("REQ-C0", "Zone C", "C5", 0), "base-station", MissionExecutionStep.MOVING_TO_TARGET);
        assertSegmentAt(route, inProgressMission("REQ-C16", "Zone C", "C5", 16), "zone-c-left-entry", "zone-c-left-main-1", MissionExecutionStep.MOVING_TO_TARGET);
        assertSegmentAt(route, inProgressMission("REQ-C31", "Zone C", "C5", 31), "zone-c-left-main-1", "C5-approach", MissionExecutionStep.MOVING_TO_TARGET);
        assertProgressAt(route, inProgressMission("REQ-C60", "Zone C", "C5", 60), "C5", MissionExecutionStep.PICKING_UP);
        assertSegmentAt(route, inProgressMission("REQ-C66", "Zone C", "C5", 66), "C5", "C5-approach", MissionExecutionStep.RETURNING_TO_BASE);
        assertProgressAt(route, inProgressMission("REQ-C125", "Zone C", "C5", 125), "base-station", MissionExecutionStep.RETURNED_TO_BASE);
    }

    @Test
    void zoneBTargetProgressesThroughZoneCZoneBTargetAndBackToBase() {
        List<MissionRouteStep> route = warehouseRouteService.buildExecutionRoute("Zone B", "B5");

        assertProgressAt(route, inProgressMission("REQ-B0", "Zone B", "B5", 0), "base-station", MissionExecutionStep.MOVING_TO_TARGET);
        assertSegmentAt(route, inProgressMission("REQ-B37", "Zone B", "B5", 37), "zone-c-left-main-1", "zone-c-left-main-2", MissionExecutionStep.MOVING_TO_TARGET);
        assertSegmentAt(route, inProgressMission("REQ-B46", "Zone B", "B5", 46), "zone-c-left-main-2", "bridge-c-b-left-1", MissionExecutionStep.MOVING_TO_TARGET);
        assertSegmentAt(route, inProgressMission("REQ-B79", "Zone B", "B5", 79), "bridge-c-b-left-2", "zone-b-left-entry", MissionExecutionStep.MOVING_TO_TARGET);
        assertSegmentAt(route, inProgressMission("REQ-B102", "Zone B", "B5", 102), "zone-b-left-main-1", "B5-approach", MissionExecutionStep.MOVING_TO_TARGET);
        assertProgressAt(route, inProgressMission("REQ-B126", "Zone B", "B5", 126), "B5", MissionExecutionStep.PICKING_UP);
        assertSegmentAt(route, inProgressMission("REQ-B132", "Zone B", "B5", 132), "B5", "B5-approach", MissionExecutionStep.RETURNING_TO_BASE);
        assertProgressAt(route, inProgressMission("REQ-B257", "Zone B", "B5", 257), "base-station", MissionExecutionStep.RETURNED_TO_BASE);
    }

    @Test
    void zoneATargetProgressesThroughZoneCZoneBZoneATargetAndBackToBase() {
        List<MissionRouteStep> route = warehouseRouteService.buildExecutionRoute("Zone A", "A1");

        assertProgressAt(route, inProgressMission("REQ-A0", "Zone A", "A1", 0), "base-station", MissionExecutionStep.MOVING_TO_TARGET);
        assertSegmentAt(route, inProgressMission("REQ-A37", "Zone A", "A1", 37), "zone-c-left-main-1", "zone-c-left-main-2", MissionExecutionStep.MOVING_TO_TARGET);
        assertSegmentAt(route, inProgressMission("REQ-A86", "Zone A", "A1", 86), "zone-b-left-entry", "zone-b-left-main-1", MissionExecutionStep.MOVING_TO_TARGET);
        assertSegmentAt(route, inProgressMission("REQ-A120", "Zone A", "A1", 120), "zone-b-left-main-2", "bridge-b-a-left-1", MissionExecutionStep.MOVING_TO_TARGET);
        assertSegmentAt(route, inProgressMission("REQ-A180", "Zone A", "A1", 180), "zone-a-left-main-2", "A1-approach", MissionExecutionStep.MOVING_TO_TARGET);
        assertProgressAt(route, inProgressMission("REQ-A207", "Zone A", "A1", 207), "A1", MissionExecutionStep.PICKING_UP);
        assertSegmentAt(route, inProgressMission("REQ-A230", "Zone A", "A1", 230), "A1-approach", "zone-a-right-main-2", MissionExecutionStep.RETURNING_TO_BASE);
        assertProgressAt(route, inProgressMission("REQ-A419", "Zone A", "A1", 419), "base-station", MissionExecutionStep.RETURNED_TO_BASE);
    }

    private void assertProgressAt(List<MissionRouteStep> route,
                                  Mission mission,
                                  String expectedPositionKey,
                                  MissionExecutionStep expectedExecutionStep) {
        MissionExecutionProgressDto progress = progressService.calculateProgress(mission, route);

        assertThat(progress.positionKey()).isEqualTo(expectedPositionKey);
        assertThat(progress.executionStep()).isEqualTo(expectedExecutionStep);
    }

    private void assertSegmentAt(List<MissionRouteStep> route,
                                 Mission mission,
                                 String expectedPositionKey,
                                 String expectedNextPositionKey,
                                 MissionExecutionStep expectedExecutionStep) {
        MissionExecutionProgressDto progress = progressService.calculateProgress(mission, route);

        assertThat(progress.positionKey()).isEqualTo(expectedPositionKey);
        assertThat(progress.nextPositionKey()).isEqualTo(expectedNextPositionKey);
        assertThat(progress.segmentProgress()).isBetween(0.0, 1.0);
        assertThat(progress.executionStep()).isEqualTo(expectedExecutionStep);
    }

    private MissionExecutionProgressDto progressFor(List<MissionRouteStep> route, Mission mission) {
        return progressService.calculateProgress(mission, route);
    }

    private Mission inProgressMission(String requestCode, String zone, String locationCode, long elapsedSeconds) {
        Mission mission = new Mission(
                requestCode,
                "Progress Customer",
                cargoTypeFor(zone),
                zone,
                locationCode,
                2,
                MissionStatus.IN_PROGRESS,
                "Progress test mission"
        );
        mission.setAssignedRobotName("Picker Alpha (RB-100)");
        mission.setExecutionStep(MissionExecutionStep.MOVING_TO_TARGET);
        mission.setCurrentPositionKey("base-station");
        mission.setExecutionStartedAt(LocalDateTime.ofInstant(FIXED_NOW.minusSeconds(elapsedSeconds), ZoneOffset.UTC));
        return mission;
    }

    private String cargoTypeFor(String zone) {
        return switch (zone) {
            case "Zone A" -> "Small Cargo";
            case "Zone B" -> "Medium Cargo";
            default -> "Large Cargo";
        };
    }
}
