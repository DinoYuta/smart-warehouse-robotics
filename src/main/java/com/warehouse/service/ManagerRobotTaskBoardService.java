package com.warehouse.service;

import com.warehouse.dto.LiveMapRobotStateDto;
import com.warehouse.dto.RobotTaskBoardDto;
import com.warehouse.dto.RobotTaskGroupDto;
import com.warehouse.model.Mission;
import com.warehouse.model.MissionStatus;
import com.warehouse.model.Robot;
import com.warehouse.repository.MissionRepository;
import com.warehouse.repository.RobotRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ManagerRobotTaskBoardService {

    private final RobotRepository robotRepository;
    private final MissionRepository missionRepository;
    private final RobotChargingService robotChargingService;
    private final LiveMapStateService liveMapStateService;

    public ManagerRobotTaskBoardService(RobotRepository robotRepository,
                                        MissionRepository missionRepository,
                                        RobotChargingService robotChargingService,
                                        LiveMapStateService liveMapStateService) {
        this.robotRepository = robotRepository;
        this.missionRepository = missionRepository;
        this.robotChargingService = robotChargingService;
        this.liveMapStateService = liveMapStateService;
    }

    public RobotTaskBoardDto getRobotTaskBoard() {
        Map<Long, LiveMapRobotStateDto> liveStateByRobotId = liveMapStateService.getLiveMapState()
                .robots()
                .stream()
                .filter(state -> state.robotId() != null)
                .collect(Collectors.toMap(
                        LiveMapRobotStateDto::robotId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
        List<Robot> robots = robotRepository.findAllByOrderByIdAsc();
        Map<Long, List<Mission>> activeMissionsByRobotId = missionRepository
                .findByAssignedRobotIdIsNotNullAndStatusInAndDeletedAtIsNullOrderByPriorityAscCreatedAtAscIdAsc(
                        RobotAssignmentService.ACTIVE_WORKLOAD_STATUSES
                )
                .stream()
                .collect(Collectors.groupingBy(Mission::getAssignedRobotId));
        Map<Long, Long> pendingConfirmationCountByRobotId = missionRepository
                .findByAssignedRobotIdIsNotNullAndStatusInAndDeletedAtIsNullOrderByPriorityAscCreatedAtAscIdAsc(
                        List.of(MissionStatus.WAITING_CONFIRMATION)
                )
                .stream()
                .collect(Collectors.groupingBy(Mission::getAssignedRobotId, Collectors.counting()));

        List<RobotTaskGroupDto> robotTaskGroups = robots.stream()
                .map(robot -> buildRobotTaskGroup(
                        robot,
                        activeMissionsByRobotId,
                        pendingConfirmationCountByRobotId,
                        liveStateByRobotId.get(robot.getId())
                ))
                .toList();

        List<Mission> unassignedPendingMissions = missionRepository
                .findByStatusAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(MissionStatus.PENDING)
                .stream()
                .filter(this::hasNoAssignedRobot)
                .toList();

        List<Mission> cancelledMissions = missionRepository
                .findByStatusAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(MissionStatus.CANCELLED);

        return new RobotTaskBoardDto(robotTaskGroups, unassignedPendingMissions, cancelledMissions);
    }

    private RobotTaskGroupDto buildRobotTaskGroup(Robot robot,
                                                  Map<Long, List<Mission>> activeMissionsByRobotId,
                                                  Map<Long, Long> pendingConfirmationCountByRobotId,
                                                  LiveMapRobotStateDto liveState) {
        List<Mission> activeMissions = activeMissionsByRobotId.getOrDefault(robot.getId(), List.of());
        long highPriorityMissionCount = activeMissions.stream()
                .filter(mission -> Integer.valueOf(RobotAssignmentService.HIGH_PRIORITY).equals(mission.getPriority()))
                .count();
        return new RobotTaskGroupDto(
                robot,
                activeMissions,
                pendingConfirmationCountByRobotId.getOrDefault(robot.getId(), 0L),
                highPriorityMissionCount,
                robotChargingService.currentBatteryStatus(robot),
                liveState
        );
    }

    private boolean hasNoAssignedRobot(Mission mission) {
        return mission.getAssignedRobotId() == null
                && (mission.getAssignedRobotName() == null || mission.getAssignedRobotName().isBlank());
    }
}
