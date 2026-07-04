package com.warehouse.dto;

import com.warehouse.model.MissionExecutionStep;

public record MissionExecutionProgressDto(String positionKey,
                                          MissionExecutionStep executionStep,
                                          String phase,
                                          String message,
                                          long elapsedSeconds,
                                          String nextPositionKey,
                                          double segmentProgress,
                                          int traveledWaypointCount,
                                          boolean waiting,
                                          String blockedSegment) {

    public MissionExecutionProgressDto(String positionKey,
                                       MissionExecutionStep executionStep,
                                       String phase,
                                       String message,
                                       long elapsedSeconds) {
        this(positionKey, executionStep, phase, message, elapsedSeconds, null, 0.0, 0, false, null);
    }

    public MissionExecutionProgressDto(String positionKey,
                                       MissionExecutionStep executionStep,
                                       String phase,
                                       String message,
                                       long elapsedSeconds,
                                       String nextPositionKey,
                                       double segmentProgress) {
        this(
                positionKey,
                executionStep,
                phase,
                message,
                elapsedSeconds,
                nextPositionKey,
                segmentProgress,
                0,
                false,
                null
        );
    }

    public MissionExecutionProgressDto(String positionKey,
                                       MissionExecutionStep executionStep,
                                       String phase,
                                       String message,
                                       long elapsedSeconds,
                                       String nextPositionKey,
                                       double segmentProgress,
                                       int traveledWaypointCount) {
        this(
                positionKey,
                executionStep,
                phase,
                message,
                elapsedSeconds,
                nextPositionKey,
                segmentProgress,
                traveledWaypointCount,
                false,
                null
        );
    }

    public MissionExecutionProgressDto(String positionKey,
                                       MissionExecutionStep executionStep,
                                       String phase,
                                       String message,
                                       long elapsedSeconds,
                                       boolean waiting,
                                       String blockedSegment) {
        this(positionKey, executionStep, phase, message, elapsedSeconds, null, 0.0, 0, waiting, blockedSegment);
    }

    public MissionExecutionProgressDto {
        traveledWaypointCount = Math.max(0, traveledWaypointCount);
    }
}
