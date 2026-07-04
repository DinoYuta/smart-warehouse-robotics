package com.warehouse.dto;

public record MissionRouteStep(String positionKey, String label, Phase phase) {

    public enum Phase {
        MOVE_TO_TARGET,
        PICKUP,
        RETURN_TO_BASE
    }
}
