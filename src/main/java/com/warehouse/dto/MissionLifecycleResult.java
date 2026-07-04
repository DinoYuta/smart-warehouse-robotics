package com.warehouse.dto;

import com.warehouse.model.Mission;

public record MissionLifecycleResult(Mission mission,
                                     boolean chargingStarted,
                                     int reassignedMissionCount,
                                     int unassignedMissionCount) {
}
