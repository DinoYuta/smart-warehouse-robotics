package com.warehouse.model;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public enum CargoType {
    // robotLoad is derived from cargo type: Small=30, Medium=60, Large=90.
    SMALL("Small Cargo", "Zone A", 30),
    MEDIUM("Medium Cargo", "Zone B", 60),
    LARGE("Large Cargo", "Zone C", 90);

    private final String displayName;
    private final String warehouseZone;
    private final int estimatedLoadPercent;

    CargoType(String displayName, String warehouseZone, int estimatedLoadPercent) {
        this.displayName = displayName;
        this.warehouseZone = warehouseZone;
        this.estimatedLoadPercent = estimatedLoadPercent;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getWarehouseZone() {
        return warehouseZone;
    }

    public int getEstimatedLoadPercent() {
        return estimatedLoadPercent;
    }

    public static Optional<CargoType> fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(cargoType -> cargoType.displayName.equals(displayName))
                .findFirst();
    }

    public static List<String> displayNames() {
        return Arrays.stream(values())
                .map(CargoType::getDisplayName)
                .toList();
    }

    public static Map<String, String> zoneByCargoType() {
        Map<String, String> zoneByCargoType = new LinkedHashMap<>();
        Arrays.stream(values()).forEach(cargoType ->
                zoneByCargoType.put(cargoType.displayName, cargoType.warehouseZone)
        );
        return zoneByCargoType;
    }

    public static Map<String, Integer> loadByCargoType() {
        Map<String, Integer> loadByCargoType = new LinkedHashMap<>();
        Arrays.stream(values()).forEach(cargoType ->
                loadByCargoType.put(cargoType.displayName, cargoType.estimatedLoadPercent)
        );
        return loadByCargoType;
    }

    public static Map<String, String> cargoTypeByZone() {
        Map<String, String> cargoTypeByZone = new LinkedHashMap<>();
        Arrays.stream(values()).forEach(cargoType ->
                cargoTypeByZone.put(cargoType.warehouseZone, cargoType.displayName)
        );
        return cargoTypeByZone;
    }

    public static int estimatedLoadPercentFor(String displayName) {
        return fromDisplayName(displayName)
                .map(CargoType::getEstimatedLoadPercent)
                .orElse(50);
    }
}
