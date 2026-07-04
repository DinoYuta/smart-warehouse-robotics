package com.warehouse.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.warehouse.model.Mission;
import com.warehouse.model.MissionExecutionStep;
import com.warehouse.model.MissionStatus;
import com.warehouse.model.Robot;
import com.warehouse.repository.MissionRepository;
import com.warehouse.repository.RobotRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ManagerRobotTaskBoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MissionRepository missionRepository;

    @Autowired
    private RobotRepository robotRepository;

    @BeforeEach
    void cleanMissions() {
        missionRepository.deleteAll();
        resetSeededRobots();
    }

    @Test
    void robotTaskBoardRouteShowsGroupedRobotWorkloads() throws Exception {
        Robot alpha = findRobotByCode("RB-100");
        Robot gamma = findRobotByCode("RB-300");
        gamma.setBattery(5);
        robotRepository.save(gamma);

        saveAssignedMission(alpha, "REQ-ALPHA-HIGH", 1, MissionStatus.ASSIGNED);
        saveAssignedMission(alpha, "REQ-ALPHA-PENDING", 2, MissionStatus.PENDING);
        Mission inProgressMission = saveAssignedMission(alpha, "REQ-ALPHA-MOVING", 2, MissionStatus.IN_PROGRESS);
        inProgressMission.setExecutionStep(MissionExecutionStep.MOVING_TO_TARGET);
        inProgressMission.setCurrentPositionKey("base-station");
        missionRepository.save(inProgressMission);
        Mission completedMission = saveAssignedMission(alpha, "REQ-COMPLETED", 1, MissionStatus.COMPLETED);
        completedMission.setExecutionStep(MissionExecutionStep.MOVING_TO_TARGET);
        completedMission.setCurrentPositionKey("zone-a-entry");
        missionRepository.save(completedMission);
        Mission cancelledMission = saveAssignedMission(alpha, "REQ-CANCELLED", 1, MissionStatus.CANCELLED);
        cancelledMission.setExecutionStep(MissionExecutionStep.RETURNING_TO_BASE);
        cancelledMission.setCurrentPositionKey("bridge-b-a-1");
        cancelledMission.setCancellationReasonCode("WRONG_LOCATION");
        cancelledMission.setCancellationNote("Customer corrected the shelf location.");
        cancelledMission.setCancelledBy("Nova001");
        cancelledMission.setCancelledAt(LocalDateTime.of(2026, 5, 30, 12, 10));
        missionRepository.save(cancelledMission);
        Mission deletedActiveMission = saveAssignedMission(alpha, "REQ-DELETED-ACTIVE", 1, MissionStatus.ASSIGNED);
        deletedActiveMission.setExecutionStep(MissionExecutionStep.PICKING_UP);
        deletedActiveMission.setCurrentPositionKey("A1");
        deletedActiveMission.setDeletedAt(LocalDateTime.of(2026, 5, 30, 12, 0));
        missionRepository.save(deletedActiveMission);
        saveUnassignedMission("REQ-UNASSIGNED", 1);
        Mission deletedUnassignedMission = saveUnassignedMission("REQ-DELETED-PENDING", 1);
        deletedUnassignedMission.setDeletedAt(LocalDateTime.of(2026, 5, 30, 12, 5));
        missionRepository.save(deletedUnassignedMission);

        mockMvc.perform(get("/manager/robot-tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager-robot-tasks"))
                .andExpect(model().attributeExists("taskBoard"))
                .andExpect(content().string(containsString("Manager Robot Task Board")))
                .andExpect(content().string(containsString("Monitor robot workload, priority pressure, and charging availability.")))
                .andExpect(content().string(containsString("COMPLETED, CANCELLED, and deleted missions do not count")))
                .andExpect(content().string(containsString("Picker Alpha")))
                .andExpect(content().string(containsString("Mover Beta")))
                .andExpect(content().string(containsString("Carrier Gamma")))
                .andExpect(content().string(containsString("Energy Saving")))
                .andExpect(content().string(containsString("Energy saving mode active.")))
                .andExpect(content().string(containsString("Charging Required")))
                .andExpect(content().string(containsString("Critical Battery")))
                .andExpect(content().string(containsString("Charging required after this task.")))
                .andExpect(content().string(containsString("3 Active Missions")))
                .andExpect(content().string(containsString("1 High Priority")))
                .andExpect(content().string(containsString("Strategy / Action")))
                .andExpect(content().string(containsString("REQ-ALPHA-HIGH")))
                .andExpect(content().string(containsString("REQ-ALPHA-PENDING")))
                .andExpect(content().string(containsString("REQ-ALPHA-MOVING")))
                .andExpect(content().string(containsString("Zone A")))
                .andExpect(content().string(containsString("A1")))
                .andExpect(content().string(containsString("1 = High")))
                .andExpect(content().string(containsString("ASSIGNED")))
                .andExpect(content().string(containsString("IN_PROGRESS")))
                .andExpect(content().string(containsString("MOVING_TO_TARGET")))
                .andExpect(content().string(containsString("base-station")))
                .andExpect(content().string(containsString("FastRouteStrategy")))
                .andExpect(content().string(containsString("Fast Mode")))
                .andExpect(content().string(containsString("Robot action stored for board display.")))
                .andExpect(content().string(containsString("Decision summary stored for board display.")))
                .andExpect(content().string(not(containsString("REQ-COMPLETED"))))
                .andExpect(content().string(not(containsString("REQ-DELETED-ACTIVE"))))
                .andExpect(content().string(containsString("Cancelled / Stopped Missions")))
                .andExpect(content().string(containsString("REQ-CANCELLED")))
                .andExpect(content().string(containsString("Wrong location")))
                .andExpect(content().string(containsString("Customer corrected the shelf location.")))
                .andExpect(content().string(containsString("Nova001")))
                .andExpect(content().string(containsString("No active missions assigned to this robot.")))
                .andExpect(content().string(containsString("Unassigned Pending Missions")))
                .andExpect(content().string(containsString("REQ-UNASSIGNED")))
                .andExpect(content().string(not(containsString("REQ-DELETED-PENDING"))))
                .andExpect(content().string(containsString("Robot Task Board")))
                .andExpect(content().string(containsString("/manager/robot-tasks")));
    }

    private Mission saveAssignedMission(Robot robot,
                                        String requestCode,
                                        Integer priority,
                                        MissionStatus status) {
        Mission mission = new Mission(
                requestCode,
                "Customer " + requestCode,
                "Small Cargo",
                "Zone A",
                "A1",
                priority,
                status,
                "Board test mission"
        );
        mission.setAssignedRobotId(robot.getId());
        mission.setAssignedRobotName(robot.getName() + " (" + robot.getCode() + ")");
        mission.setMatchedRuleName("Urgent Task Fast Route Rule");
        mission.setSelectedStrategyName("FastRouteStrategy");
        mission.setActionMessage("Robot action stored for board display.");
        mission.setDecisionSummary("Decision summary stored for board display.");
        return missionRepository.save(mission);
    }

    private Mission saveUnassignedMission(String requestCode, Integer priority) {
        return missionRepository.save(new Mission(
                requestCode,
                "Unassigned Customer",
                "Medium Cargo",
                "Zone B",
                "B5",
                priority,
                MissionStatus.PENDING,
                "Waiting for assignment"
        ));
    }

    private Robot findRobotByCode(String code) {
        return robotRepository.findAllByOrderByIdAsc()
                .stream()
                .filter(robot -> code.equals(robot.getCode()))
                .findFirst()
                .orElseThrow();
    }

    private void resetSeededRobots() {
        var robots = robotRepository.findAllByOrderByIdAsc();
        robots.forEach(robot -> {
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
        robotRepository.saveAll(robots);
    }

    private void clearChargingState(com.warehouse.model.Robot robot) {
        robot.setChargingRequired(false);
        robot.setCharging(false);
        robot.setChargingStartedAt(null);
        robot.setChargingCompletedAt(null);
        robot.setBatteryBeforeCharging(null);
    }
}
