package com.warehouse.model;

public enum RobotMovementMode {
    FAST("Fast Mode", 3),
    NORMAL("Normal Mode", 5),
    SAFE("Safe Route Mode", 5),
    ENERGY_SAVING("Energy Saving Mode", 7),
    HEAVY_LOAD("Heavy Load Mode", 5),
    OBSTACLE_AVOIDANCE("Obstacle Avoidance Mode", 5),
    CHARGING("Charging Mode", 5);

    private final String displayName;
    private final int waypointsPerBatteryPercent;

    RobotMovementMode(String displayName, int waypointsPerBatteryPercent) {
        this.displayName = displayName;
        this.waypointsPerBatteryPercent = waypointsPerBatteryPercent;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getWaypointsPerBatteryPercent() {
        return waypointsPerBatteryPercent;
    }
}
