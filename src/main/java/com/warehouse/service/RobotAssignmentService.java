package com.warehouse.service;

import com.warehouse.model.Mission;
import com.warehouse.model.MissionStatus;
import com.warehouse.model.Robot;
import com.warehouse.repository.MissionRepository;
import com.warehouse.repository.RobotRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RobotAssignmentService {

    public static final int HIGH_PRIORITY = 1;
    public static final List<MissionStatus> ACTIVE_WORKLOAD_STATUSES = List.of(
            MissionStatus.PENDING,
            MissionStatus.ASSIGNED,
            MissionStatus.IN_PROGRESS
    );

    private final RobotRepository robotRepository;
    private final MissionRepository missionRepository;

    public RobotAssignmentService(RobotRepository robotRepository,
                                  MissionRepository missionRepository) {
        this.robotRepository = robotRepository;
        this.missionRepository = missionRepository;
    }

    public Optional<RobotAssignment> selectRobotForMission(Mission mission) {
        return selectRobotForMissionExcluding(mission, Set.of());
    }

    public Optional<RobotAssignment> selectRobotForMissionExcluding(Mission mission, Set<Long> excludedRobotIds) {
        List<RobotWorkload> candidates = robotRepository.findAllByOrderByIdAsc()
                .stream()
                .filter(this::isAvailableForAssignment)
                .filter(robot -> !isExcludedRobot(robot, excludedRobotIds))
                .map(this::buildWorkload)
                .sorted(workloadComparator())
                .toList();

        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        RobotWorkload selectedWorkload = candidates.get(0);
        return Optional.of(new RobotAssignment(
                selectedWorkload.robot(),
                selectedWorkload.activeMissionCount(),
                selectedWorkload.activeHighPriorityMissionCount(),
                buildAssignmentReason(mission, selectedWorkload, candidates)
        ));
    }

    private boolean isExcludedRobot(Robot robot, Set<Long> excludedRobotIds) {
        return robot.getId() != null && excludedRobotIds != null && excludedRobotIds.contains(robot.getId());
    }

    private RobotWorkload buildWorkload(Robot robot) {
        long activeMissionCount = missionRepository.countByAssignedRobotIdAndStatusInAndDeletedAtIsNull(
                robot.getId(),
                ACTIVE_WORKLOAD_STATUSES
        );
        long activeHighPriorityMissionCount = missionRepository
                .countByAssignedRobotIdAndPriorityAndStatusInAndDeletedAtIsNull(
                        robot.getId(),
                        HIGH_PRIORITY,
                        ACTIVE_WORKLOAD_STATUSES
                );
        return new RobotWorkload(robot, activeMissionCount, activeHighPriorityMissionCount);
    }

    private boolean isAvailableForAssignment(Robot robot) {
        if (Boolean.TRUE.equals(robot.getCharging())) {
            return false;
        }

        String status = robot.getStatus();
        if (status == null || status.isBlank()) {
            return true;
        }

        String normalizedStatus = status.trim()
                .toUpperCase(Locale.US)
                .replace('-', '_')
                .replace(' ', '_');

        return !normalizedStatus.contains("OFFLINE")
                && !normalizedStatus.contains("MAINTENANCE")
                && !normalizedStatus.contains("ERROR")
                && !normalizedStatus.contains("CHARGING")
                && !normalizedStatus.contains("UNAVAILABLE")
                && !normalizedStatus.contains("DISABLED")
                && !normalizedStatus.contains("OUT_OF_SERVICE");
    }

    private Comparator<RobotWorkload> workloadComparator() {
        return Comparator
                .comparingLong(RobotWorkload::activeHighPriorityMissionCount)
                .thenComparingLong(RobotWorkload::activeMissionCount)
                .thenComparing(Comparator.comparingInt(this::batteryForRanking).reversed())
                .thenComparing(workload -> workload.robot().getId(), Comparator.nullsLast(Long::compareTo))
                .thenComparing(workload -> safeText(workload.robot().getName()));
    }

    private int batteryForRanking(RobotWorkload workload) {
        Integer battery = workload.robot().getBattery();
        return battery != null ? battery : -1;
    }

    private String buildAssignmentReason(Mission mission,
                                         RobotWorkload selectedWorkload,
                                         List<RobotWorkload> candidates) {
        String robotName = formatRobotName(selectedWorkload.robot());
        boolean anotherRobotHasMoreHighPriorityWork = candidates.stream()
                .anyMatch(candidate -> candidate.activeHighPriorityMissionCount()
                        > selectedWorkload.activeHighPriorityMissionCount());
        boolean anotherRobotHasMoreActiveWork = candidates.stream()
                .anyMatch(candidate -> candidate.activeMissionCount()
                        > selectedWorkload.activeMissionCount());

        if (Integer.valueOf(HIGH_PRIORITY).equals(mission.getPriority())
                && anotherRobotHasMoreHighPriorityWork) {
            return "Assigned to " + robotName
                    + " because it has fewer active high-priority missions.";
        }
        if (anotherRobotHasMoreHighPriorityWork) {
            return "Assigned to " + robotName
                    + " because it has fewer active high-priority missions.";
        }
        if (anotherRobotHasMoreActiveWork) {
            return "Assigned to " + robotName
                    + " because it has fewer active missions.";
        }

        return String.format(
                Locale.US,
                "Assigned to %s because available robots had equal active workload; "
                        + "battery and stable robot order were used as tie breakers.",
                robotName
        );
    }

    private String formatRobotName(Robot robot) {
        if (robot.getCode() == null || robot.getCode().isBlank()) {
            return robot.getName();
        }
        return robot.getName() + " (" + robot.getCode() + ")";
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private record RobotWorkload(Robot robot,
                                 long activeMissionCount,
                                 long activeHighPriorityMissionCount) {
    }

    public record RobotAssignment(Robot robot,
                                  long activeMissionCount,
                                  long activeHighPriorityMissionCount,
                                  String assignmentReason) {
    }
}
