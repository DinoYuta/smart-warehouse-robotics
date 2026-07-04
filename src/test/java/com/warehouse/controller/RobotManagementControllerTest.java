package com.warehouse.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import com.warehouse.model.Mission;
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
class RobotManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RobotRepository robotRepository;

    @Autowired
    private MissionRepository missionRepository;

    @BeforeEach
    void resetSeededRobots() {
        missionRepository.deleteAll();
        var robots = robotRepository.findAllByOrderByIdAsc();
        robots.forEach(robot -> {
            robot.setChargingRequired(false);
            robot.setCharging(false);
            robot.setChargingStartedAt(null);
            robot.setChargingCompletedAt(null);
            robot.setBatteryBeforeCharging(null);
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

    @Test
    void robotManagementRouteLoadsWithRobotsModel() throws Exception {
        mockMvc.perform(get("/robots"))
                .andExpect(status().isOk())
                .andExpect(view().name("robots"))
                .andExpect(model().attributeExists("robots", "robotFleet"));
    }

    @Test
    void robotManagementShowsChargingStatusAndChargingBatteryProgress() throws Exception {
        Robot alpha = findRobotByCode("RB-100");
        alpha.setStatus("CHARGING");
        alpha.setBattery(5);
        alpha.setBatteryBeforeCharging(5);
        alpha.setChargingRequired(true);
        alpha.setCharging(true);
        alpha.setChargingStartedAt(LocalDateTime.now().minusSeconds(20));
        alpha.setChargingCompletedAt(null);
        robotRepository.save(alpha);

        mockMvc.perform(get("/robots"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Picker Alpha")))
                .andExpect(content().string(containsString("CHARGING")))
                .andExpect(content().string(containsString("15% battery (charging +10%)")))
                .andExpect(content().string(containsString("Charging at station.")))
                .andExpect(content().string(containsString("ChargingStrategy")));
    }

    @Test
    void robotManagementDoesNotShowSeededStrategyForIdleRobots() throws Exception {
        mockMvc.perform(get("/robots"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Current Strategy")))
                .andExpect(content().string(containsString("Idle")))
                .andExpect(content().string(not(containsString("EnergySavingStrategy"))))
                .andExpect(content().string(not(containsString("ChargingStrategy"))))
                .andExpect(content().string(not(containsString("HeavyLoadStrategy"))));
    }

    @Test
    void robotManagementShowsActiveMissionStrategyFromBackendState() throws Exception {
        Robot alpha = findRobotByCode("RB-100");
        Mission mission = new Mission(
                "REQ-ROBOT-FAST",
                "Robot Management Customer",
                "Small Cargo",
                "Zone A",
                "A3",
                1,
                MissionStatus.ASSIGNED,
                "Robot Management active strategy test"
        );
        mission.setAssignedRobotId(alpha.getId());
        mission.setAssignedRobotName(alpha.getName() + " (" + alpha.getCode() + ")");
        mission.setSelectedStrategyName("FastRouteStrategy");
        missionRepository.save(mission);

        mockMvc.perform(get("/robots"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Picker Alpha")))
                .andExpect(content().string(containsString("FastRouteStrategy")))
                .andExpect(content().string(containsString("Fast Mode")))
                .andExpect(content().string(not(containsString("EnergySavingStrategy"))));
    }

    private Robot findRobotByCode(String code) {
        return robotRepository.findAllByOrderByIdAsc()
                .stream()
                .filter(robot -> code.equals(robot.getCode()))
                .findFirst()
                .orElseThrow();
    }
}
