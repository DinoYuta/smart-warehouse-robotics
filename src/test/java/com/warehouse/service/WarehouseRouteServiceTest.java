package com.warehouse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.warehouse.dto.MissionRouteStep;
import com.warehouse.dto.MissionRouteStep.Phase;
import java.util.List;
import org.junit.jupiter.api.Test;

class WarehouseRouteServiceTest {

    private final WarehouseRouteService warehouseRouteService = new WarehouseRouteService();

    @Test
    void c5RouteStartsAtBaseMovesThroughZoneCReachesExactTargetAndReturnsToBase() {
        List<MissionRouteStep> route = warehouseRouteService.buildExecutionRoute("Zone C", "C5");

        assertThat(positionKeys(route)).containsExactly(
                "base-station",
                "zone-c-left-entry",
                "zone-c-left-main-1",
                "C5-approach",
                "C5",
                "C5-approach",
                "zone-c-right-main-1",
                "zone-c-right-exit",
                "base-station"
        );
        assertPickupStep(route, "C5");
        assertThat(route.get(0).phase()).isEqualTo(Phase.MOVE_TO_TARGET);
        assertThat(route.get(route.size() - 1).phase()).isEqualTo(Phase.RETURN_TO_BASE);
    }

    @Test
    void b5RouteMovesThroughZoneCBridgeAndZoneBThenReturnsThroughZoneBZoneCToBase() {
        List<MissionRouteStep> route = warehouseRouteService.buildExecutionRoute("Zone B", "B5");

        assertThat(positionKeys(route)).containsExactly(
                "base-station",
                "zone-c-left-entry",
                "zone-c-left-main-1",
                "zone-c-left-main-2",
                "bridge-c-b-left-1",
                "bridge-c-b-left-2",
                "zone-b-left-entry",
                "zone-b-left-main-1",
                "B5-approach",
                "B5",
                "B5-approach",
                "zone-b-right-main-1",
                "zone-b-right-exit",
                "bridge-b-c-right-1",
                "bridge-b-c-right-2",
                "zone-c-right-main-2",
                "zone-c-right-main-1",
                "zone-c-right-exit",
                "base-station"
        );
        assertPickupStep(route, "B5");
    }

    @Test
    void a1RouteMovesThroughZoneCZoneBBridgeToZoneAThenReturnsThroughZoneAZoneBZoneCToBase() {
        List<MissionRouteStep> route = warehouseRouteService.buildExecutionRoute("Zone A", "A1");

        assertThat(positionKeys(route)).containsExactly(
                "base-station",
                "zone-c-left-entry",
                "zone-c-left-main-1",
                "zone-c-left-main-2",
                "bridge-c-b-left-1",
                "bridge-c-b-left-2",
                "zone-b-left-entry",
                "zone-b-left-main-1",
                "zone-b-left-main-2",
                "bridge-b-a-left-1",
                "bridge-b-a-left-2",
                "zone-a-left-entry",
                "zone-a-left-main-1",
                "zone-a-left-main-2",
                "A1-approach",
                "A1",
                "A1-approach",
                "zone-a-right-main-2",
                "zone-a-right-main-1",
                "zone-a-right-exit",
                "bridge-a-b-right-1",
                "bridge-a-b-right-2",
                "zone-b-right-main-2",
                "zone-b-right-main-1",
                "zone-b-right-exit",
                "bridge-b-c-right-1",
                "bridge-b-c-right-2",
                "zone-c-right-main-2",
                "zone-c-right-main-1",
                "zone-c-right-exit",
                "base-station"
        );
        assertPickupStep(route, "A1");
        assertThat(positionKeys(route).subList(0, pickupIndex(route))).noneMatch(positionKey -> positionKey.contains("right"));
        assertThat(positionKeys(route).subList(pickupIndex(route) + 1, route.size())).noneMatch(positionKey -> positionKey.contains("left"));
    }

    @Test
    void outboundAndReturnRoutesExposeMovementPickupAndReturnPhases() {
        List<MissionRouteStep> outboundRoute = warehouseRouteService.buildOutboundRoute("B5");
        List<MissionRouteStep> returnRoute = warehouseRouteService.buildReturnRoute("B5");

        assertThat(positionKeys(outboundRoute)).containsExactly(
                "base-station",
                "zone-c-left-entry",
                "zone-c-left-main-1",
                "zone-c-left-main-2",
                "bridge-c-b-left-1",
                "bridge-c-b-left-2",
                "zone-b-left-entry",
                "zone-b-left-main-1",
                "B5-approach",
                "B5"
        );
        assertThat(outboundRoute.subList(0, outboundRoute.size() - 1))
                .allSatisfy(step -> assertThat(step.phase()).isEqualTo(Phase.MOVE_TO_TARGET));
        assertThat(outboundRoute.get(outboundRoute.size() - 1).phase()).isEqualTo(Phase.PICKUP);

        assertThat(positionKeys(returnRoute)).containsExactly(
                "B5",
                "B5-approach",
                "zone-b-right-main-1",
                "zone-b-right-exit",
                "bridge-b-c-right-1",
                "bridge-b-c-right-2",
                "zone-c-right-main-2",
                "zone-c-right-main-1",
                "zone-c-right-exit",
                "base-station"
        );
        assertThat(returnRoute).allSatisfy(step -> assertThat(step.phase()).isEqualTo(Phase.RETURN_TO_BASE));
    }

    @Test
    void a3RouteUsesApproachWaypointBeforeAndAfterExactTargetSlot() {
        List<MissionRouteStep> route = warehouseRouteService.buildExecutionRoute("Zone A", "A3");

        assertThat(positionKeys(route)).containsSubsequence(
            "zone-a-left-main-1",
            "zone-a-left-main-2",
            "A3-approach",
            "A3",
            "A3-approach",
            "zone-a-right-main-2",
            "zone-a-right-main-1"
        );
        assertPickupStep(route, "A3");
    }

    @Test
    void c9RouteUsesLowerRowApproachWithoutMiddleLaneShortcutToSlot() {
        List<MissionRouteStep> route = warehouseRouteService.buildExecutionRoute("Zone C", "C9");

        assertThat(positionKeys(route)).containsExactly(
                "base-station",
                "zone-c-left-entry",
                "C9-approach",
                "C9",
                "C9-approach",
                "zone-c-right-exit",
                "base-station"
        );
        assertPickupStep(route, "C9");
        assertThat(positionKeys(route)).doesNotContain("zone-c-left-main-1", "zone-c-right-main-1");
    }

    @Test
    void everyCargoSlotUsesApproachWaypointBeforePickupAndBeforeReturnLane() {
        List<String> locationCodes = List.of(
                "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9",
                "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9",
                "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9"
        );

        locationCodes.forEach(locationCode -> {
            List<MissionRouteStep> route = warehouseRouteService.buildExecutionRoute("Zone " + locationCode.charAt(0), locationCode);
            List<String> positionKeys = positionKeys(route);
            int pickupIndex = pickupIndex(route);

            assertThat(positionKeys.get(pickupIndex - 1)).isEqualTo(locationCode + "-approach");
            assertThat(positionKeys.get(pickupIndex)).isEqualTo(locationCode);
            assertThat(positionKeys.get(pickupIndex + 1)).isEqualTo(locationCode + "-approach");
        });
    }

    @Test
    void invalidLocationCodeAndMismatchedZoneAreRejected() {
        assertThatThrownBy(() -> warehouseRouteService.buildExecutionRoute("Zone A", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mission locationCode is required to calculate a route.");
        assertThatThrownBy(() -> warehouseRouteService.buildExecutionRoute("Zone A", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mission locationCode is required to calculate a route.");
        assertThatThrownBy(() -> warehouseRouteService.buildExecutionRoute("Zone A", "D5"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mission locationCode must be A1-A9, B1-B9, or C1-C9.");
        assertThatThrownBy(() -> warehouseRouteService.buildExecutionRoute("Zone A", "A10"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mission locationCode must be A1-A9, B1-B9, or C1-C9.");
        assertThatThrownBy(() -> warehouseRouteService.buildExecutionRoute("Zone B", "A1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mission zone must match the target locationCode.");
    }

    private void assertPickupStep(List<MissionRouteStep> route, String expectedPositionKey) {
        assertThat(route)
                .filteredOn(step -> step.phase() == Phase.PICKUP)
                .singleElement()
                .satisfies(step -> assertThat(step.positionKey()).isEqualTo(expectedPositionKey));
    }

    private int pickupIndex(List<MissionRouteStep> route) {
        for (int index = 0; index < route.size(); index++) {
            if (route.get(index).phase() == Phase.PICKUP) {
                return index;
            }
        }
        throw new AssertionError("Route did not include pickup step.");
    }

    private List<String> positionKeys(List<MissionRouteStep> route) {
        return route.stream()
                .map(MissionRouteStep::positionKey)
                .toList();
    }
}
