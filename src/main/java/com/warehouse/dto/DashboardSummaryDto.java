package com.warehouse.dto;

public class DashboardSummaryDto {

    private final long robotCount;
    private final long activeMissionCount;
    private final long activeRuleCount;
    private final long activeStrategyCount;
    private final String phase;

    public DashboardSummaryDto(long robotCount,
                               long activeMissionCount,
                               long activeRuleCount,
                               long activeStrategyCount,
                               String phase) {
        this.robotCount = robotCount;
        this.activeMissionCount = activeMissionCount;
        this.activeRuleCount = activeRuleCount;
        this.activeStrategyCount = activeStrategyCount;
        this.phase = phase;
    }

    public long getRobotCount() {
        return robotCount;
    }

    public long getActiveMissionCount() {
        return activeMissionCount;
    }

    public long getActiveRuleCount() {
        return activeRuleCount;
    }

    public long getActiveStrategyCount() {
        return activeStrategyCount;
    }

    public String getPhase() {
        return phase;
    }
}
