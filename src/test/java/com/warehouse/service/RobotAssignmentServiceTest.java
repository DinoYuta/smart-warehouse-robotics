package com.warehouse.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.warehouse.model.Mission;
import com.warehouse.model.MissionExecutionStep;
import com.warehouse.model.MissionStatus;
import com.warehouse.model.Robot;
import com.warehouse.repository.MissionRepository;
import com.warehouse.repository.RobotRepository;
import com.warehouse.service.RobotAssignmentService.RobotAssignment;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RobotAssignmentServiceTest {

    @Autowired
    private RobotAssignmentService robotAssignmentService;

    @Autowired
    private RobotRepository robotRepository;

    @Autowired
    private MissionRepository missionRepository;

    @BeforeEach
    void cleanWorkload() {
        missionRepository.deleteAll();
        resetSeededRobotAvailability();
    }

    @Test
    void prefersRobotWithFewerActiveMissions() {
        Robot alpha = findRobotByCode("RB-100");
        Robot gamma = findRobotByCode("RB-300");

        saveAssignedMission(alpha, "REQ-ACTIVE-A1", 2, MissionStatus.ASSIGNED);
        saveAssignedMission(alpha, "REQ-ACTIVE-A2", 2, MissionStatus.IN_PROGRESS);
        saveAssignedMission(gamma, "REQ-ACTIVE-G1", 2, MissionStatus.ASSIGNED);

        Optional<RobotAssignment> assignment = robotAssignmentService.selectRobotForMission(newMission(2));

        assertThat(assignment).isPresent();
        assertThat(assignment.get().robot().getCode()).isEqualTo("RB-200");
        assertThat(assignment.get().activeMissionCount()).isZero();
        assertThat(assignment.get().activeHighPriorityMissionCount()).isZero();
        assertThat(assignment.get().assignmentReason()).contains("fewer active missions");
    }

    @Test
    void prefersRobotWithFewerHighPriorityActiveMissions() {
        Robot alpha = findRobotByCode("RB-100");
        Robot beta = findRobotByCode("RB-200");
        Robot gamma = findRobotByCode("RB-300");

        saveAssignedMission(alpha, "REQ-HIGH-A", 1, MissionStatus.ASSIGNED);
        saveAssignedMission(beta, "REQ-MEDIUM-B", 2, MissionStatus.ASSIGNED);
        saveAssignedMission(gamma, "REQ-HIGH-G", 1, MissionStatus.IN_PROGRESS);

        Optional<RobotAssignment> assignment = robotAssignmentService.selectRobotForMission(newMission(1));

        assertThat(assignment).isPresent();
        assertThat(assignment.get().robot().getCode()).isEqualTo("RB-200");
        assertThat(assignment.get().activeMissionCount()).isEqualTo(1);
        assertThat(assignment.get().activeHighPriorityMissionCount()).isZero();
        assertThat(assignment.get().assignmentReason()).contains("fewer active high-priority missions");
    }

    @Test
    void completedAndCancelledMissionsDoNotCountAsActiveWorkload() {
        Robot alpha = findRobotByCode("RB-100");
        Robot beta = findRobotByCode("RB-200");
        Robot gamma = findRobotByCode("RB-300");

        saveAssignedMission(alpha, "REQ-ACTIVE-A", 2, MissionStatus.ASSIGNED);
        saveAssignedMission(beta, "REQ-ACTIVE-B", 2, MissionStatus.IN_PROGRESS);
        Mission completedMission = saveAssignedMission(gamma, "REQ-COMPLETED-G", 1, MissionStatus.COMPLETED);
        completedMission.setExecutionStep(MissionExecutionStep.MOVING_TO_TARGET);
        completedMission.setCurrentPositionKey("zone-c-entry");
        missionRepository.save(completedMission);
        Mission cancelledMission = saveAssignedMission(gamma, "REQ-CANCELLED-G", 1, MissionStatus.CANCELLED);
        cancelledMission.setExecutionStep(MissionExecutionStep.RETURNING_TO_BASE);
        cancelledMission.setCurrentPositionKey("bridge-c-b-1");
        missionRepository.save(cancelledMission);
        Mission deletedActiveMission = saveAssignedMission(gamma, "REQ-DELETED-G", 1, MissionStatus.ASSIGNED);
        deletedActiveMission.setExecutionStep(MissionExecutionStep.PICKING_UP);
        deletedActiveMission.setCurrentPositionKey("A1");
        deletedActiveMission.setDeletedAt(LocalDateTime.of(2026, 5, 30, 12, 0));
        missionRepository.save(deletedActiveMission);

        Optional<RobotAssignment> assignment = robotAssignmentService.selectRobotForMission(newMission(2));

        assertThat(assignment).isPresent();
        assertThat(assignment.get().robot().getCode()).isEqualTo("RB-300");
        assertThat(assignment.get().activeMissionCount()).isZero();
        assertThat(assignment.get().activeHighPriorityMissionCount()).isZero();
    }

    @Test
    void waitingConfirmationMissionsDoNotBlockRobotFromNextAssignment() {
        Robot gamma = findRobotByCode("RB-300");
        Mission waitingMission = saveAssignedMission(
                gamma,
                "REQ-WAITING-G",
                2,
                MissionStatus.WAITING_CONFIRMATION
        );
        waitingMission.setExecutionStep(MissionExecutionStep.RETURNED_TO_BASE);
        waitingMission.setCurrentPositionKey("base-station");
        waitingMission.setReturnedAt(LocalDateTime.of(2026, 6, 16, 10, 0));
        missionRepository.save(waitingMission);

        Optional<RobotAssignment> assignment = robotAssignmentService.selectRobotForMission(newMission(2));

        assertThat(assignment).isPresent();
        assertThat(assignment.get().robot().getCode()).isEqualTo("RB-300");
        assertThat(assignment.get().activeMissionCount()).isZero();
        assertThat(assignment.get().activeHighPriorityMissionCount()).isZero();
    }

    @Test
    void chargingRobotsAndExplicitlyExcludedRobotsAreNotSelected() {
        Robot alpha = findRobotByCode("RB-100");
        Robot beta = findRobotByCode("RB-200");
        Robot gamma = findRobotByCode("RB-300");
        alpha.setStatus("CHARGING");
        alpha.setCharging(true);
        alpha.setChargingRequired(true);
        alpha.setChargingStartedAt(LocalDateTime.now().minusSeconds(10));

        Optional<RobotAssignment> assignment = robotAssignmentService.selectRobotForMission(newMission(2));

        assertThat(assignment).isPresent();
        assertThat(assignment.get().robot().getCode()).isEqualTo("RB-300");

        Optional<RobotAssignment> excludedAssignment = robotAssignmentService.selectRobotForMissionExcluding(
                newMission(2),
                Set.of(beta.getId(), gamma.getId())
        );

        assertThat(excludedAssignment).isEmpty();

        alpha.setStatus("IDLE");
        robotRepository.save(alpha);

        Optional<RobotAssignment> chargingFlagAssignment = robotAssignmentService.selectRobotForMissionExcluding(
                newMission(2),
                Set.of(beta.getId(), gamma.getId())
        );

        assertThat(chargingFlagAssignment).isEmpty();
    }

    private Mission newMission(Integer priority) {
        return new Mission(
                "REQ-NEW-" + priority,
                "Customer",
                "Small Cargo",
                "Zone A",
                "A1",
                priority,
                MissionStatus.PENDING,
                "New assignment"
        );
    }

    private Mission saveAssignedMission(Robot robot,
                                        String requestCode,
                                        Integer priority,
                                        MissionStatus status) {
        Mission mission = new Mission(
                requestCode,
                "Existing Customer",
                "Small Cargo",
                "Zone A",
                "A1",
                priority,
                status,
                "Existing robot workload"
        );
        mission.setAssignedRobotId(robot.getId());
        mission.setAssignedRobotName(robot.getName() + " (" + robot.getCode() + ")");
        return missionRepository.save(mission);
    }

    private Robot findRobotByCode(String code) {
        return robotRepository.findAllByOrderByIdAsc()
                .stream()
                .filter(robot -> code.equals(robot.getCode()))
                .findFirst()
                .orElseThrow();
    }

    private void resetSeededRobotAvailability() {
        robotRepository.findAllByOrderByIdAsc().forEach(robot -> {
            clearChargingState(robot);
            if ("RB-100".equals(robot.getCode())) {
                robot.setStatus("IDLE");
                robot.setBattery(68);
            } else if ("RB-200".equals(robot.getCode())) {
                robot.setStatus("MOVING");
                robot.setBattery(15);
            } else if ("RB-300".equals(robot.getCode())) {
                robot.setStatus("LOADED");
                robot.setBattery(76);
            }
        });
    }

    private void clearChargingState(Robot robot) {
        robot.setChargingRequired(false);
        robot.setCharging(false);
        robot.setChargingStartedAt(null);
        robot.setChargingCompletedAt(null);
        robot.setBatteryBeforeCharging(null);
    }
}
