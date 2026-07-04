package com.warehouse.dto;

import com.warehouse.model.Mission;
import java.util.List;

public class RobotTaskBoardDto {

    private final List<RobotTaskGroupDto> robotTaskGroups;
    private final List<Mission> unassignedPendingMissions;
    private final List<Mission> cancelledMissions;

    public RobotTaskBoardDto(List<RobotTaskGroupDto> robotTaskGroups,
                             List<Mission> unassignedPendingMissions) {
        this(robotTaskGroups, unassignedPendingMissions, List.of());
    }

    public RobotTaskBoardDto(List<RobotTaskGroupDto> robotTaskGroups,
                             List<Mission> unassignedPendingMissions,
                             List<Mission> cancelledMissions) {
        this.robotTaskGroups = robotTaskGroups;
        this.unassignedPendingMissions = unassignedPendingMissions;
        this.cancelledMissions = cancelledMissions;
    }

    public List<RobotTaskGroupDto> getRobotTaskGroups() {
        return robotTaskGroups;
    }

    public List<Mission> getUnassignedPendingMissions() {
        return unassignedPendingMissions;
    }

    public List<Mission> getCancelledMissions() {
        return cancelledMissions;
    }

    public long getTotalActiveMissionCount() {
        return robotTaskGroups.stream()
                .mapToLong(RobotTaskGroupDto::getActiveMissionCount)
                .sum();
    }

    public long getTotalHighPriorityMissionCount() {
        return robotTaskGroups.stream()
                .mapToLong(RobotTaskGroupDto::getHighPriorityMissionCount)
                .sum();
    }

    public long getTotalPendingConfirmationCount() {
        return robotTaskGroups.stream()
                .mapToLong(RobotTaskGroupDto::getPendingConfirmationCount)
                .sum();
    }
}
