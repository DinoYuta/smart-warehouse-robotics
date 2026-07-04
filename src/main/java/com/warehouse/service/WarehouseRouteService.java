package com.warehouse.service;

import com.warehouse.dto.MissionRouteStep;
import com.warehouse.dto.MissionRouteStep.Phase;
import com.warehouse.model.Mission;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class WarehouseRouteService {

    private static final String BASE_STATION = "base-station";

    private static final List<String> OUTBOUND_ZONE_C = List.of(
            BASE_STATION,
            "zone-c-left-entry"
    );

    private static final List<String> OUTBOUND_ZONE_B = List.of(
            BASE_STATION,
            "zone-c-left-entry",
            "zone-c-left-main-1",
            "zone-c-left-main-2",
            "bridge-c-b-left-1",
            "bridge-c-b-left-2",
            "zone-b-left-entry"
    );

    private static final List<String> OUTBOUND_ZONE_A = List.of(
            BASE_STATION,
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
            "zone-a-left-entry"
    );

    private static final List<String> RETURN_ZONE_C = List.of(
            "zone-c-right-exit",
            BASE_STATION
    );

    private static final List<String> RETURN_ZONE_B = List.of(
            "zone-b-right-exit",
            "bridge-b-c-right-1",
            "bridge-b-c-right-2",
            "zone-c-right-main-2",
            "zone-c-right-main-1",
            "zone-c-right-exit",
            BASE_STATION
    );

    private static final List<String> RETURN_ZONE_A = List.of(
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
            BASE_STATION
    );

    public List<MissionRouteStep> buildExecutionRoute(Mission mission) {
        if (mission == null) {
            throw new IllegalArgumentException("Mission is required to calculate a route.");
        }
        return buildExecutionRoute(mission.getZone(), mission.getLocationCode());
    }

    public List<MissionRouteStep> buildExecutionRoute(String zone, String locationCode) {
        String normalizedLocationCode = normalizeAndValidateLocationCode(locationCode);
        validateZoneMatchesLocation(zone, normalizedLocationCode);

        List<MissionRouteStep> executionRoute = new ArrayList<>(buildOutboundRoute(normalizedLocationCode));
        List<MissionRouteStep> returnRoute = buildReturnRoute(normalizedLocationCode);
        executionRoute.addAll(returnRoute.subList(1, returnRoute.size()));
        return List.copyOf(executionRoute);
    }

    public List<MissionRouteStep> buildOutboundRoute(String locationCode) {
        String normalizedLocationCode = normalizeAndValidateLocationCode(locationCode);
        List<String> positionKeys = new ArrayList<>(outboundWaypointsFor(normalizedLocationCode));
        positionKeys.addAll(outboundTargetRowWaypointsFor(normalizedLocationCode));
        positionKeys.add(approachWaypointFor(normalizedLocationCode));
        positionKeys.add(normalizedLocationCode);
        return positionKeys.stream()
                .map(positionKey -> new MissionRouteStep(
                        positionKey,
                        labelFor(positionKey),
                        positionKey.equals(normalizedLocationCode) ? Phase.PICKUP : Phase.MOVE_TO_TARGET
                ))
                .toList();
    }

    public List<MissionRouteStep> buildReturnRoute(String locationCode) {
        String normalizedLocationCode = normalizeAndValidateLocationCode(locationCode);
        List<String> positionKeys = new ArrayList<>();
        positionKeys.add(normalizedLocationCode);
        positionKeys.add(approachWaypointFor(normalizedLocationCode));
        positionKeys.addAll(returnTargetRowWaypointsFor(normalizedLocationCode));
        positionKeys.addAll(returnWaypointsFor(normalizedLocationCode));
        return positionKeys.stream()
                .map(positionKey -> new MissionRouteStep(positionKey, labelFor(positionKey), Phase.RETURN_TO_BASE))
                .toList();
    }

    private List<String> outboundWaypointsFor(String normalizedLocationCode) {
        return switch (normalizedLocationCode.charAt(0)) {
            case 'A' -> OUTBOUND_ZONE_A;
            case 'B' -> OUTBOUND_ZONE_B;
            case 'C' -> OUTBOUND_ZONE_C;
            default -> throw new IllegalArgumentException("Mission locationCode must be A1-A9, B1-B9, or C1-C9.");
        };
    }

    private List<String> outboundTargetRowWaypointsFor(String normalizedLocationCode) {
        String zoneKey = String.valueOf(Character.toLowerCase(normalizedLocationCode.charAt(0)));
        return switch (targetRow(normalizedLocationCode)) {
            case 1 -> List.of("zone-" + zoneKey + "-left-main-1", "zone-" + zoneKey + "-left-main-2");
            case 2 -> List.of("zone-" + zoneKey + "-left-main-1");
            case 3 -> List.of();
            default -> throw new IllegalArgumentException("Mission locationCode must be A1-A9, B1-B9, or C1-C9.");
        };
    }

    private String approachWaypointFor(String normalizedLocationCode) {
        return normalizedLocationCode + "-approach";
    }

    private List<String> returnTargetRowWaypointsFor(String normalizedLocationCode) {
        String zoneKey = String.valueOf(Character.toLowerCase(normalizedLocationCode.charAt(0)));
        return switch (targetRow(normalizedLocationCode)) {
            case 1 -> List.of("zone-" + zoneKey + "-right-main-2", "zone-" + zoneKey + "-right-main-1");
            case 2 -> List.of("zone-" + zoneKey + "-right-main-1");
            case 3 -> List.of();
            default -> throw new IllegalArgumentException("Mission locationCode must be A1-A9, B1-B9, or C1-C9.");
        };
    }

    private int targetRow(String normalizedLocationCode) {
        int slotNumber = Character.digit(normalizedLocationCode.charAt(1), 10);
        return ((slotNumber - 1) / 3) + 1;
    }

    private List<String> returnWaypointsFor(String normalizedLocationCode) {
        return switch (normalizedLocationCode.charAt(0)) {
            case 'A' -> RETURN_ZONE_A;
            case 'B' -> RETURN_ZONE_B;
            case 'C' -> RETURN_ZONE_C;
            default -> throw new IllegalArgumentException("Mission locationCode must be A1-A9, B1-B9, or C1-C9.");
        };
    }

    private String normalizeAndValidateLocationCode(String locationCode) {
        if (locationCode == null || locationCode.isBlank()) {
            throw new IllegalArgumentException("Mission locationCode is required to calculate a route.");
        }

        String normalizedLocationCode = locationCode.trim().toUpperCase(Locale.US);
        if (!normalizedLocationCode.matches("[ABC][1-9]")) {
            throw new IllegalArgumentException("Mission locationCode must be A1-A9, B1-B9, or C1-C9.");
        }
        return normalizedLocationCode;
    }

    private void validateZoneMatchesLocation(String zone, String normalizedLocationCode) {
        if (zone == null || zone.isBlank()) {
            return;
        }

        char expectedZoneKey = normalizedLocationCode.charAt(0);
        char actualZoneKey = resolveZoneKey(zone);
        if (actualZoneKey != expectedZoneKey) {
            throw new IllegalArgumentException("Mission zone must match the target locationCode.");
        }
    }

    private char resolveZoneKey(String zone) {
        String normalizedZone = zone.trim().toUpperCase(Locale.US);
        if (normalizedZone.equals("A") || normalizedZone.endsWith(" A") || normalizedZone.contains("ZONE A")) {
            return 'A';
        }
        if (normalizedZone.equals("B") || normalizedZone.endsWith(" B") || normalizedZone.contains("ZONE B")) {
            return 'B';
        }
        if (normalizedZone.equals("C") || normalizedZone.endsWith(" C") || normalizedZone.contains("ZONE C")) {
            return 'C';
        }
        throw new IllegalArgumentException("Mission zone must be Zone A, Zone B, or Zone C.");
    }

    private String labelFor(String positionKey) {
        if (positionKey.matches("[ABC][1-9]-approach")) {
            return "Target " + positionKey.substring(0, 2) + " Approach";
        }
        if (positionKey.matches("[ABC][1-9]")) {
            return "Target " + positionKey;
        }
        return switch (positionKey) {
            case BASE_STATION -> "Base Station";
            case "zone-c-left-entry" -> "Zone C Left Entry";
            case "zone-c-left-main-1" -> "Zone C Left Main 1";
            case "zone-c-left-main-2" -> "Zone C Left Main 2";
            case "zone-c-right-main-2" -> "Zone C Right Main 2";
            case "zone-c-right-main-1" -> "Zone C Right Main 1";
            case "zone-c-right-exit" -> "Zone C Right Exit";
            case "bridge-c-b-left-1" -> "Bridge C to B Left 1";
            case "bridge-c-b-left-2" -> "Bridge C to B Left 2";
            case "bridge-b-c-right-1" -> "Bridge B to C Right 1";
            case "bridge-b-c-right-2" -> "Bridge B to C Right 2";
            case "zone-b-left-entry" -> "Zone B Left Entry";
            case "zone-b-left-main-1" -> "Zone B Left Main 1";
            case "zone-b-left-main-2" -> "Zone B Left Main 2";
            case "zone-b-right-main-2" -> "Zone B Right Main 2";
            case "zone-b-right-main-1" -> "Zone B Right Main 1";
            case "zone-b-right-exit" -> "Zone B Right Exit";
            case "bridge-b-a-left-1" -> "Bridge B to A Left 1";
            case "bridge-b-a-left-2" -> "Bridge B to A Left 2";
            case "bridge-a-b-right-1" -> "Bridge A to B Right 1";
            case "bridge-a-b-right-2" -> "Bridge A to B Right 2";
            case "zone-a-left-entry" -> "Zone A Left Entry";
            case "zone-a-left-main-1" -> "Zone A Left Main 1";
            case "zone-a-left-main-2" -> "Zone A Left Main 2";
            case "zone-a-right-main-2" -> "Zone A Right Main 2";
            case "zone-a-right-main-1" -> "Zone A Right Main 1";
            case "zone-a-right-exit" -> "Zone A Right Exit";
            default -> positionKey;
        };
    }
}
