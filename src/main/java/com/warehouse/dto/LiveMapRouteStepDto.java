package com.warehouse.dto;

public record LiveMapRouteStepDto(String positionKey, String label, String phase) {

    public static LiveMapRouteStepDto from(MissionRouteStep routeStep) {
        return new LiveMapRouteStepDto(
                routeStep.positionKey(),
                routeStep.label(),
                routeStep.phase().name()
        );
    }
}
