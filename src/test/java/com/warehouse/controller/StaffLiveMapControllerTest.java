package com.warehouse.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.warehouse.model.Mission;
import com.warehouse.model.MissionExecutionStep;
import com.warehouse.model.MissionStatus;
import com.warehouse.model.Robot;
import com.warehouse.repository.MissionRepository;
import com.warehouse.repository.RobotRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class StaffLiveMapControllerTest {

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
    void notificationScriptTracksLiveMapEventsWithStableKeys() throws Exception {
        String script = Files.readString(Path.of("src/main/resources/static/js/app-notifications.js"));

        assertThat(script).contains("var activeEventKeysBySource = {}");
        assertThat(script).contains("if (!previous[event.key])");
        assertThat(script).contains("notifyActiveEventsForSource(\"live-map:\" + robotKey, collectRobotEvents(robot))");
        assertThat(script).contains(":returned-to-base");
        assertThat(script).contains("Robot returned to Base. Waiting for confirmation.");
        assertThat(script).contains(":battery-low");
        assertThat(script).contains("Robot battery is low.");
        assertThat(script).contains(":charging-required");
        assertThat(script).contains("Charging required after this task.");
        assertThat(script).contains(":charging-started");
        assertThat(script).contains("Robot is charging at station.");
        assertThat(script).contains(":fully-charged");
        assertThat(script).contains("Robot fully charged.");
        assertThat(script).contains(":waiting-path");
        assertThat(script).contains("Robot is waiting for path to clear.");
    }

    @Test
    void liveMapRouteLoadsStandaloneMapWithFixedSelectedRobotMissionFlow() throws Exception {
        Mission mission = new Mission(
                "REQ-MAP",
                "Map Customer",
                "Small Cargo",
                "Zone A",
                "A1",
                2,
                MissionStatus.ASSIGNED,
                "Map panel mission"
        );
        mission.setAssignedRobotName("Picker Alpha (RB-100)");
        mission.setSelectedStrategyName("FastRouteStrategy");
        mission.setActionMessage("Picker Alpha selects the fastest available route.");
        missionRepository.save(mission);

        mockMvc.perform(get("/staff/live-map"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff-live-map"))
                .andExpect(model().attributeExists("robotMissionFlows"))
                .andExpect(content().string(containsString("Live Warehouse Map")))
                .andExpect(content().string(containsString("live-map-standalone")))
                .andExpect(content().string(containsString("fullscreen-live-map")))
                .andExpect(content().string(containsString("Track robot position, battery, strategy, and mission flow from the backend state.")))
                .andExpect(content().string(containsString("Show All Robots")))
                .andExpect(content().string(containsString("Visual Route Preview")))
                .andExpect(content().string(containsString("Select a robot to view status, battery, strategy, and mission flow.")))
                .andExpect(content().string(containsString("Zone A")))
                .andExpect(content().string(containsString("Zone B")))
                .andExpect(content().string(containsString("Zone C")))
                .andExpect(content().string(containsString("Base Station")))
                .andExpect(content().string(containsString("Charging Station")))
                .andExpect(content().string(containsString("Picker Alpha")))
                .andExpect(content().string(containsString("Mover Beta")))
                .andExpect(content().string(containsString("Carrier Gamma")))
                .andExpect(content().string(containsString("Mission Flow")))
                .andExpect(content().string(containsString("Current Target")))
                .andExpect(content().string(containsString("Move to Zone A - A1")))
                .andExpect(content().string(containsString("data-current-zone=\"a\"")))
                .andExpect(content().string(containsString("data-location-code=\"A1\"")))
                .andExpect(content().string(containsString("data-active-step=\"assigned\"")))
                .andExpect(content().string(containsString("Assigned")))
                .andExpect(content().string(containsString("Moving")))
                .andExpect(content().string(containsString("Pickup")))
                .andExpect(content().string(containsString("Returning")))
                .andExpect(content().string(containsString("Returned / Waiting Confirmation")))
                .andExpect(content().string(containsString("No active pickup mission assigned.")))
                .andExpect(content().string(not(containsString("app-layout"))))
                .andExpect(content().string(not(containsString("side-nav"))))
                .andExpect(content().string(not(containsString("Saved Mission Status"))))
                .andExpect(content().string(not(containsString("Robot Status"))))
                .andExpect(content().string(not(containsString("REQ-MAP"))))
                .andExpect(content().string(not(containsString("FastRouteStrategy"))))
                .andExpect(content().string(not(containsString("Picker Alpha selects the fastest available route."))))
                .andExpect(content().string(not(containsString("Decision / Action"))));
    }

    @Test
    void liveMapUsesInProgressMissionBeforeNewerAssignedMission() throws Exception {
        saveAssignedMission(
                "KH004",
                "Carrier Gamma (RB-300)",
                "Medium Cargo",
                "Zone B",
                "B5",
                MissionStatus.IN_PROGRESS,
                LocalDateTime.of(2026, 5, 29, 9, 0)
        );
        saveAssignedMission(
                "KH009",
                "Carrier Gamma (RB-300)",
                "Large Cargo",
                "Zone C",
                "C7",
                MissionStatus.ASSIGNED,
                LocalDateTime.of(2026, 5, 29, 10, 0)
        );

        mockMvc.perform(get("/staff/live-map"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Move to Zone B - B5")))
                .andExpect(content().string(containsString("data-current-zone=\"b\"")))
                .andExpect(content().string(containsString("data-location-code=\"B5\"")))
                .andExpect(content().string(containsString("data-active-step=\"move\"")))
                .andExpect(content().string(not(containsString("Move to Zone C - C7"))))
                .andExpect(content().string(not(containsString("data-location-code=\"C7\""))));
    }

    @Test
    void liveMapUsesOldestAssignedMissionWhenNoInProgressMissionExists() throws Exception {
        saveAssignedMission(
                "KH010",
                "Picker Alpha (RB-100)",
                "Small Cargo",
                "Zone A",
                "A9",
                MissionStatus.ASSIGNED,
                LocalDateTime.of(2026, 5, 29, 9, 0)
        );
        saveAssignedMission(
                "KH011",
                "Picker Alpha (RB-100)",
                "Small Cargo",
                "Zone A",
                "A1",
                MissionStatus.ASSIGNED,
                LocalDateTime.of(2026, 5, 29, 10, 0)
        );

        mockMvc.perform(get("/staff/live-map"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Move to Zone A - A9")))
                .andExpect(content().string(containsString("data-location-code=\"A9\"")))
                .andExpect(content().string(not(containsString("Move to Zone A - A1"))))
                .andExpect(content().string(not(containsString("data-location-code=\"A1\""))));
    }

    @Test
    void liveMapInitialRenderShowsWaitingConfirmationMissionWhenNoCurrentTaskExists() throws Exception {
        Mission waitingMission = saveAssignedMission(
                "KH012",
                "Mover Beta (RB-200)",
                "Medium Cargo",
                "Zone B",
                "B5",
                MissionStatus.WAITING_CONFIRMATION,
                LocalDateTime.of(2026, 5, 29, 9, 0)
        );
        waitingMission.setExecutionStep(MissionExecutionStep.RETURNED_TO_BASE);
        waitingMission.setCurrentPositionKey("base-station");
        waitingMission.setReturnedAt(LocalDateTime.of(2026, 5, 29, 9, 15));
        missionRepository.save(waitingMission);

        mockMvc.perform(get("/staff/live-map"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-current-zone=\"b\"")))
                .andExpect(content().string(containsString("data-location-code=\"B5\"")))
                .andExpect(content().string(containsString("data-active-step=\"returned\"")))
                .andExpect(content().string(containsString("Current Status")))
                .andExpect(content().string(containsString("Returned to Base. Waiting for confirmation.")));
    }

    @Test
    void liveMapIgnoresCompletedAndCancelledMissionsForCurrentPosition() throws Exception {
        Mission completedMission = saveAssignedMission(
                "KH020",
                "Mover Beta (RB-200)",
                "Large Cargo",
                "Zone C",
                "C5",
                MissionStatus.COMPLETED,
                LocalDateTime.of(2026, 5, 29, 9, 0)
        );
        completedMission.setExecutionStep(MissionExecutionStep.MOVING_TO_TARGET);
        completedMission.setCurrentPositionKey("zone-c-entry");
        missionRepository.save(completedMission);
        Mission cancelledMission = saveAssignedMission(
                "KH021",
                "Mover Beta (RB-200)",
                "Medium Cargo",
                "Zone B",
                "B5",
                MissionStatus.CANCELLED,
                LocalDateTime.of(2026, 5, 29, 10, 0)
        );
        cancelledMission.setExecutionStep(MissionExecutionStep.RETURNING_TO_BASE);
        cancelledMission.setCurrentPositionKey("bridge-c-b-1");
        missionRepository.save(cancelledMission);
        Mission deletedMission = saveAssignedMission(
                "KH022",
                "Mover Beta (RB-200)",
                "Medium Cargo",
                "Zone B",
                "B8",
                MissionStatus.ASSIGNED,
                LocalDateTime.of(2026, 5, 29, 11, 0)
        );
        deletedMission.setExecutionStep(MissionExecutionStep.PICKING_UP);
        deletedMission.setCurrentPositionKey("B8");
        deletedMission.setDeletedAt(LocalDateTime.of(2026, 5, 30, 12, 0));
        missionRepository.save(deletedMission);

        mockMvc.perform(get("/staff/live-map"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No active pickup mission assigned.")))
                .andExpect(content().string(containsString("data-current-zone=\"b\"")))
                .andExpect(content().string(not(containsString("Move to Zone C - C5"))))
                .andExpect(content().string(not(containsString("Move to Zone B - B5"))))
                .andExpect(content().string(not(containsString("Move to Zone B - B8"))))
                .andExpect(content().string(not(containsString("data-location-code=\"C5\""))))
                .andExpect(content().string(not(containsString("data-location-code=\"B5\""))))
                .andExpect(content().string(not(containsString("data-location-code=\"B8\""))));
    }

    @Test
    void liveMapStateEndpointReturnsRealRobotsAsJsonWithFallbackState() throws Exception {
        mockMvc.perform(get("/staff/live-map/state"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.robots", hasSize(3)))
                .andExpect(jsonPath("$.robots[0].robotName").value("Picker Alpha"))
                .andExpect(jsonPath("$.robots[0].robotCode").value("RB-100"))
                .andExpect(jsonPath("$.robots[0].color").value("green"))
                .andExpect(jsonPath("$.robots[0].batteryLevel").value(68))
                .andExpect(jsonPath("$.robots[0].batteryPercent").value(68))
                .andExpect(jsonPath("$.robots[0].batteryDrainPercent").value(0))
                .andExpect(jsonPath("$.robots[0].batteryDisplayText").value("68% battery"))
                .andExpect(jsonPath("$.robots[0].batteryWarningLevel").value("NORMAL"))
                .andExpect(jsonPath("$.robots[0].lowBattery").value(false))
                .andExpect(jsonPath("$.robots[0].criticalBattery").value(false))
                .andExpect(jsonPath("$.robots[0].energySavingMode").value(false))
                .andExpect(jsonPath("$.robots[0].chargingRequired").value(false))
                .andExpect(jsonPath("$.robots[0].batteryMessage").value("Battery level normal."))
                .andExpect(jsonPath("$.robots[0].movementMode").value("NORMAL"))
                .andExpect(jsonPath("$.robots[0].movementModeDisplay").value("Normal Mode"))
                .andExpect(jsonPath("$.robots[0].waypointsPerBatteryPercent").value(5))
                .andExpect(jsonPath("$.robots[0].batteryDrainMode").value("NORMAL"))
                .andExpect(jsonPath("$.robots[0].primaryStrategyName").value("NormalStrategy"))
                .andExpect(jsonPath("$.robots[0].currentActiveStrategyName").value("NormalStrategy"))
                .andExpect(jsonPath("$.robots[0].robotStatus").value("IDLE"))
                .andExpect(jsonPath("$.robots[0].missionId").value(nullValue()))
                .andExpect(jsonPath("$.robots[0].executionStep").value(nullValue()))
                .andExpect(jsonPath("$.robots[0].currentPositionKey").value("base-station"))
                .andExpect(jsonPath("$.robots[0].nextPositionKey").value(nullValue()))
                .andExpect(jsonPath("$.robots[0].segmentProgress").value(0.0))
                .andExpect(jsonPath("$.robots[0].route", hasSize(0)))
                .andExpect(jsonPath("$.robots[0].message").value("No active pickup mission assigned."))
                .andExpect(jsonPath("$.robots[1].robotName").value("Mover Beta"))
                .andExpect(jsonPath("$.robots[1].color").value("red"))
                .andExpect(jsonPath("$.robots[1].batteryPercent").value(15))
                .andExpect(jsonPath("$.robots[1].batteryDrainPercent").value(0))
                .andExpect(jsonPath("$.robots[1].batteryWarningLevel").value("LOW"))
                .andExpect(jsonPath("$.robots[1].lowBattery").value(true))
                .andExpect(jsonPath("$.robots[1].criticalBattery").value(false))
                .andExpect(jsonPath("$.robots[1].energySavingMode").value(true))
                .andExpect(jsonPath("$.robots[1].chargingRequired").value(false))
                .andExpect(jsonPath("$.robots[1].batteryMessage").value("Energy saving mode active."))
                .andExpect(jsonPath("$.robots[1].movementMode").value("ENERGY_SAVING"))
                .andExpect(jsonPath("$.robots[1].movementModeDisplay").value("Energy Saving Mode"))
                .andExpect(jsonPath("$.robots[1].waypointsPerBatteryPercent").value(7))
                .andExpect(jsonPath("$.robots[1].batteryDrainMode").value("ENERGY_SAVING"))
                .andExpect(jsonPath("$.robots[1].primaryStrategyName").value("NormalStrategy"))
                .andExpect(jsonPath("$.robots[1].currentActiveStrategyName").value("NormalStrategy"))
                .andExpect(jsonPath("$.robots[1].robotStatus").value("MOVING"))
                .andExpect(jsonPath("$.robots[2].robotName").value("Carrier Gamma"))
                .andExpect(jsonPath("$.robots[2].color").value("blue"))
                .andExpect(jsonPath("$.robots[2].batteryPercent").value(76))
                .andExpect(jsonPath("$.robots[2].batteryDrainPercent").value(0))
                .andExpect(jsonPath("$.robots[2].batteryWarningLevel").value("NORMAL"))
                .andExpect(jsonPath("$.robots[2].movementMode").value("NORMAL"));
    }

    @Test
    void liveMapStateShowsChargingRobotAtChargingStationWithChargingProgress() throws Exception {
        Robot alpha = findRobotByCode("RB-100");
        alpha.setStatus("CHARGING");
        alpha.setBattery(5);
        alpha.setBatteryBeforeCharging(5);
        alpha.setChargingRequired(true);
        alpha.setCharging(true);
        alpha.setChargingStartedAt(LocalDateTime.now().minusSeconds(20));
        alpha.setChargingCompletedAt(null);
        robotRepository.save(alpha);

        mockMvc.perform(get("/staff/live-map/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.robots[0].robotName").value("Picker Alpha"))
                .andExpect(jsonPath("$.robots[0].status").value("CHARGING"))
                .andExpect(jsonPath("$.robots[0].missionId").value(nullValue()))
                .andExpect(jsonPath("$.robots[0].requestCode").value(nullValue()))
                .andExpect(jsonPath("$.robots[0].executionStep").value(nullValue()))
                .andExpect(jsonPath("$.robots[0].currentPositionKey").value("charging-station"))
                .andExpect(jsonPath("$.robots[0].nextPositionKey").value(nullValue()))
                .andExpect(jsonPath("$.robots[0].batteryPercent").value(15))
                .andExpect(jsonPath("$.robots[0].batteryDrainPercent").value(0))
                .andExpect(jsonPath("$.robots[0].batteryDisplayText").value("15% battery (charging +10%)"))
                .andExpect(jsonPath("$.robots[0].charging").value(true))
                .andExpect(jsonPath("$.robots[0].chargingRecoveredPercent").value(10))
                .andExpect(jsonPath("$.robots[0].chargingDisplayText").value("15% battery (charging +10%)"))
                .andExpect(jsonPath("$.robots[0].chargingRequired").value(true))
                .andExpect(jsonPath("$.robots[0].movementMode").value("CHARGING"))
                .andExpect(jsonPath("$.robots[0].batteryDrainMode").value("CHARGING"))
                .andExpect(jsonPath("$.robots[0].primaryStrategyName").value("ChargingStrategy"))
                .andExpect(jsonPath("$.robots[0].currentActiveStrategyName").value("ChargingStrategy"))
                .andExpect(jsonPath("$.robots[0].strategyMessage").value("Charging at Charging Station."))
                .andExpect(jsonPath("$.robots[0].robotStatus").value("CHARGING"))
                .andExpect(jsonPath("$.robots[0].batteryMessage").value("Charging at Charging Station."))
                .andExpect(jsonPath("$.robots[0].message").value("Charging at Charging Station."))
                .andExpect(jsonPath("$.robots[0].route", hasSize(0)));
    }

    @Test
    void liveMapStateFinalizesChargingRobotWhenBatteryReachesOneHundred() throws Exception {
        Robot alpha = findRobotByCode("RB-100");
        alpha.setStatus("CHARGING");
        alpha.setBattery(95);
        alpha.setBatteryBeforeCharging(95);
        alpha.setChargingRequired(true);
        alpha.setCharging(true);
        alpha.setChargingStartedAt(LocalDateTime.now().minusSeconds(20));
        alpha.setChargingCompletedAt(null);
        robotRepository.save(alpha);

        mockMvc.perform(get("/staff/live-map/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.robots[0].batteryPercent").value(100))
                .andExpect(jsonPath("$.robots[0].charging").value(false))
                .andExpect(jsonPath("$.robots[0].chargingRequired").value(false))
                .andExpect(jsonPath("$.robots[0].currentPositionKey").value("base-station"));

        Robot chargedAlpha = findRobotByCode("RB-100");
        assertThat(chargedAlpha.getStatus()).isEqualTo("IDLE");
        assertThat(chargedAlpha.getBattery()).isEqualTo(100);
        assertThat(chargedAlpha.getCharging()).isFalse();
        assertThat(chargedAlpha.getChargingRequired()).isFalse();
        assertThat(chargedAlpha.getChargingCompletedAt()).isNotNull();
    }

    @Test
    void liveMapStateShowsRuleSelectedFastRouteAsPrimaryAndCurrentStrategy() throws Exception {
        Mission mission = saveAssignedMission(
                "REQ-FAST-PRIMARY",
                "Picker Alpha (RB-100)",
                "Small Cargo",
                "Zone A",
                "A1",
                MissionStatus.IN_PROGRESS,
                LocalDateTime.of(2026, 5, 29, 9, 0)
        );
        mission.setExecutionStep(MissionExecutionStep.MOVING_TO_TARGET);
        mission.setCurrentPositionKey("base-station");
        mission.setExecutionStartedAt(LocalDateTime.now().minusSeconds(20));
        mission.setSelectedStrategyName("FastRouteStrategy");
        missionRepository.save(mission);

        mockMvc.perform(get("/staff/live-map/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.robots[0].requestCode").value("REQ-FAST-PRIMARY"))
                .andExpect(jsonPath("$.robots[0].primaryStrategyName").value("FastRouteStrategy"))
                .andExpect(jsonPath("$.robots[0].currentActiveStrategyName").value("FastRouteStrategy"))
                .andExpect(jsonPath("$.robots[0].strategyMessage").value("Fast route mode active."))
                .andExpect(jsonPath("$.robots[0].movementMode").value("FAST"))
                .andExpect(jsonPath("$.robots[0].batteryDrainMode").value("FAST"))
                .andExpect(jsonPath("$.robots[0].waypointsPerBatteryPercent").value(3))
                .andExpect(jsonPath("$.robots[0].robotStatus").value("IDLE"))
                .andExpect(jsonPath("$.robots[0].status").value("IN_PROGRESS"));
    }

    @Test
    void largeCargoMissionUsesHeavyLoadStrategyAfterPickupDuringReturnPhase() throws Exception {
        Robot beta = findRobotByCode("RB-200");
        beta.setBattery(68);
        robotRepository.save(beta);

        Mission mission = saveAssignedMission(
                "REQ-LARGE-HEAVY-RETURN",
                "Mover Beta (RB-200)",
                "Large Cargo",
                "Zone C",
                "C5",
                MissionStatus.IN_PROGRESS,
                LocalDateTime.of(2026, 5, 29, 9, 0)
        );
        mission.setExecutionStep(MissionExecutionStep.MOVING_TO_TARGET);
        mission.setCurrentPositionKey("base-station");
        mission.setExecutionStartedAt(LocalDateTime.now().minusSeconds(50));
        mission.setSelectedStrategyName("FastRouteStrategy");
        missionRepository.save(mission);

        mockMvc.perform(get("/staff/live-map/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.robots[1].requestCode").value("REQ-LARGE-HEAVY-RETURN"))
                .andExpect(jsonPath("$.robots[1].executionStep").value("RETURNING_TO_BASE"))
                .andExpect(jsonPath("$.robots[1].primaryStrategyName").value("FastRouteStrategy"))
                .andExpect(jsonPath("$.robots[1].currentActiveStrategyName").value("HeavyLoadStrategy"))
                .andExpect(jsonPath("$.robots[1].strategyMessage").value("Large cargo picked up. Returning with heavy load behavior."))
                .andExpect(jsonPath("$.robots[1].movementMode").value("HEAVY_LOAD"))
                .andExpect(jsonPath("$.robots[1].batteryDrainMode").value("HEAVY_LOAD"))
                .andExpect(jsonPath("$.robots[1].waypointsPerBatteryPercent").value(5));
    }

    @Test
    void chargingStrategySelectedByRuleShowsChargingRequiredWithoutInterruptingCurrentMission() throws Exception {
        Robot alpha = findRobotByCode("RB-100");
        alpha.setBattery(10);
        robotRepository.save(alpha);

        Mission mission = saveAssignedMission(
                "REQ-CHARGING-STRATEGY",
                "Picker Alpha (RB-100)",
                "Small Cargo",
                "Zone A",
                "A1",
                MissionStatus.IN_PROGRESS,
                LocalDateTime.of(2026, 5, 29, 9, 0)
        );
        mission.setExecutionStep(MissionExecutionStep.MOVING_TO_TARGET);
        mission.setCurrentPositionKey("base-station");
        mission.setExecutionStartedAt(LocalDateTime.now().minusSeconds(1));
        mission.setSelectedStrategyName("ChargingStrategy");
        missionRepository.save(mission);

        mockMvc.perform(get("/staff/live-map/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.robots[0].requestCode").value("REQ-CHARGING-STRATEGY"))
                .andExpect(jsonPath("$.robots[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.robots[0].primaryStrategyName").value("ChargingStrategy"))
                .andExpect(jsonPath("$.robots[0].currentActiveStrategyName").value("ChargingStrategy"))
                .andExpect(jsonPath("$.robots[0].strategyMessage").value("Charging required after current mission."))
                .andExpect(jsonPath("$.robots[0].charging").value(false))
                .andExpect(jsonPath("$.robots[0].chargingRequired").value(true))
                .andExpect(jsonPath("$.robots[0].currentPositionKey").value("base-station"));

        Mission unchangedMission = missionRepository.findById(mission.getId()).orElseThrow();
        assertThat(unchangedMission.getStatus()).isEqualTo(MissionStatus.IN_PROGRESS);
        assertThat(unchangedMission.getCompletedAt()).isNull();
    }

    @Test
    void liveMapStatePrefersInProgressMissionBeforeNewerAssignedMissionForSameRobot() throws Exception {
        Mission inProgressMission = saveAssignedMission(
                "KH004",
                "Carrier Gamma (RB-300)",
                "Medium Cargo",
                "Zone B",
                "B5",
                MissionStatus.IN_PROGRESS,
                LocalDateTime.of(2026, 5, 29, 9, 0)
        );
        inProgressMission.setExecutionStep(MissionExecutionStep.MOVING_TO_TARGET);
        inProgressMission.setCurrentPositionKey("base-station");
        missionRepository.save(inProgressMission);
        saveAssignedMission(
                "KH009",
                "Carrier Gamma (RB-300)",
                "Large Cargo",
                "Zone C",
                "C7",
                MissionStatus.ASSIGNED,
                LocalDateTime.of(2026, 5, 29, 10, 0)
        );

        mockMvc.perform(get("/staff/live-map/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.robots[2].requestCode").value("KH004"))
                .andExpect(jsonPath("$.robots[2].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.robots[2].executionStep").value("MOVING_TO_TARGET"))
                .andExpect(jsonPath("$.robots[2].batteryPercent").value(76))
                .andExpect(jsonPath("$.robots[2].batteryDrainPercent").value(0))
                .andExpect(jsonPath("$.robots[2].currentPositionKey").value("base-station"))
                .andExpect(jsonPath("$.robots[2].nextPositionKey").value(nullValue()))
                .andExpect(jsonPath("$.robots[2].segmentProgress").value(0.0))
                .andExpect(jsonPath("$.robots[2].targetZone").value("Zone B"))
                .andExpect(jsonPath("$.robots[2].targetLocationCode").value("B5"))
                .andExpect(jsonPath("$.robots[2].route[0].positionKey").value("base-station"))
                .andExpect(jsonPath("$.robots[2].route[4].positionKey").value("bridge-c-b-left-1"))
                .andExpect(jsonPath("$.robots[2].route[8].positionKey").value("B5-approach"))
                .andExpect(jsonPath("$.robots[2].route[9].positionKey").value("B5"))
                .andExpect(jsonPath("$.robots[2].route[13].positionKey").value("bridge-b-c-right-1"))
                .andExpect(jsonPath("$.robots[2].route[18].positionKey").value("base-station"))
                .andExpect(jsonPath("$.robots[2].message").value("Robot execution started from Base Station."))
                .andExpect(content().string(not(containsString("KH009"))))
                .andExpect(content().string(not(containsString("\"targetLocationCode\":\"C7\""))));
    }

    @Test
    void liveMapStateUsesOldestAssignedMissionWhenNoInProgressMissionExists() throws Exception {
        saveAssignedMission(
                "KH010",
                "Picker Alpha (RB-100)",
                "Small Cargo",
                "Zone A",
                "A9",
                MissionStatus.ASSIGNED,
                LocalDateTime.of(2026, 5, 29, 9, 0)
        );
        saveAssignedMission(
                "KH011",
                "Picker Alpha (RB-100)",
                "Small Cargo",
                "Zone A",
                "A1",
                MissionStatus.ASSIGNED,
                LocalDateTime.of(2026, 5, 29, 10, 0)
        );

        mockMvc.perform(get("/staff/live-map/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.robots[0].requestCode").value("KH010"))
                .andExpect(jsonPath("$.robots[0].status").value("ASSIGNED"))
                .andExpect(jsonPath("$.robots[0].executionStep").value("NOT_STARTED"))
                .andExpect(jsonPath("$.robots[0].currentPositionKey").value("base-station"))
                .andExpect(jsonPath("$.robots[0].nextPositionKey").value(nullValue()))
                .andExpect(jsonPath("$.robots[0].segmentProgress").value(0.0))
                .andExpect(jsonPath("$.robots[0].targetZone").value("Zone A"))
                .andExpect(jsonPath("$.robots[0].targetLocationCode").value("A9"))
                .andExpect(jsonPath("$.robots[0].route[0].positionKey").value("base-station"))
                .andExpect(jsonPath("$.robots[0].route[1].positionKey").value("zone-c-left-entry"))
                .andExpect(jsonPath("$.robots[0].route[6].positionKey").value("zone-b-left-entry"))
                .andExpect(jsonPath("$.robots[0].route[9].positionKey").value("bridge-b-a-left-1"))
                .andExpect(jsonPath("$.robots[0].route[12].positionKey").value("A9-approach"))
                .andExpect(jsonPath("$.robots[0].route[13].positionKey").value("A9"))
                .andExpect(jsonPath("$.robots[0].route[26].positionKey").value("base-station"))
                .andExpect(jsonPath("$.robots[0].message").value("Mission assigned and waiting for Staff to start execution."))
                .andExpect(content().string(not(containsString("KH011"))))
                .andExpect(content().string(not(containsString("\"targetLocationCode\":\"A1\""))));
    }

    @Test
    void liveMapStateIgnoresCompletedCancelledAndSoftDeletedMissions() throws Exception {
        Mission completedMission = saveAssignedMission(
                "KH020",
                "Mover Beta (RB-200)",
                "Large Cargo",
                "Zone C",
                "C5",
                MissionStatus.COMPLETED,
                LocalDateTime.of(2026, 5, 29, 9, 0)
        );
        completedMission.setExecutionStep(MissionExecutionStep.MOVING_TO_TARGET);
        completedMission.setCurrentPositionKey("zone-c-entry");
        missionRepository.save(completedMission);
        Mission cancelledMission = saveAssignedMission(
                "KH021",
                "Mover Beta (RB-200)",
                "Medium Cargo",
                "Zone B",
                "B5",
                MissionStatus.CANCELLED,
                LocalDateTime.of(2026, 5, 29, 10, 0)
        );
        cancelledMission.setExecutionStep(MissionExecutionStep.RETURNING_TO_BASE);
        cancelledMission.setCurrentPositionKey("bridge-c-b-1");
        missionRepository.save(cancelledMission);
        Mission deletedMission = saveAssignedMission(
                "KH022",
                "Mover Beta (RB-200)",
                "Medium Cargo",
                "Zone B",
                "B8",
                MissionStatus.ASSIGNED,
                LocalDateTime.of(2026, 5, 29, 11, 0)
        );
        deletedMission.setExecutionStep(MissionExecutionStep.PICKING_UP);
        deletedMission.setCurrentPositionKey("B8");
        deletedMission.setDeletedAt(LocalDateTime.of(2026, 5, 30, 12, 0));
        missionRepository.save(deletedMission);

        mockMvc.perform(get("/staff/live-map/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.robots[1].missionId").value(nullValue()))
                .andExpect(jsonPath("$.robots[1].requestCode").value(nullValue()))
                .andExpect(jsonPath("$.robots[1].status").value(nullValue()))
                .andExpect(jsonPath("$.robots[1].currentPositionKey").value("base-station"))
                .andExpect(jsonPath("$.robots[1].nextPositionKey").value(nullValue()))
                .andExpect(jsonPath("$.robots[1].segmentProgress").value(0.0))
                .andExpect(jsonPath("$.robots[1].route", hasSize(0)))
                .andExpect(jsonPath("$.robots[1].message").value("No active pickup mission assigned."))
                .andExpect(content().string(not(containsString("KH020"))))
                .andExpect(content().string(not(containsString("KH021"))))
                .andExpect(content().string(not(containsString("KH022"))));
    }

    @Test
    void liveMapStatePersistsBatteryDrainAndMovesReturnedMissionToWaitingConfirmation() throws Exception {
        Robot beta = findRobotByCode("RB-200");
        Mission mission = saveAssignedMission(
                "REQ-BATTERY-DRAIN",
                "Mover Beta (RB-200)",
                "Medium Cargo",
                "Zone B",
                "B5",
                MissionStatus.IN_PROGRESS,
                LocalDateTime.of(2026, 5, 29, 9, 0)
        );
        mission.setAssignedRobotId(beta.getId());
        mission.setExecutionStep(MissionExecutionStep.MOVING_TO_TARGET);
        mission.setCurrentPositionKey("base-station");
        mission.setExecutionStartedAt(LocalDateTime.now().minusSeconds(400));
        Mission savedMission = missionRepository.saveAndFlush(mission);

        mockMvc.perform(get("/staff/live-map/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.robots[1].requestCode").value("REQ-BATTERY-DRAIN"))
                .andExpect(jsonPath("$.robots[1].status").value("WAITING_CONFIRMATION"))
                .andExpect(jsonPath("$.robots[1].executionStep").value("RETURNED_TO_BASE"))
                .andExpect(jsonPath("$.robots[1].currentPositionKey").value("base-station"))
                .andExpect(jsonPath("$.robots[1].batteryLevel").value(13))
                .andExpect(jsonPath("$.robots[1].batteryPercent").value(13))
                .andExpect(jsonPath("$.robots[1].batteryDrainPercent").value(0))
                .andExpect(jsonPath("$.robots[1].batteryDisplayText").value("13% battery"))
                .andExpect(jsonPath("$.robots[1].batteryWarningLevel").value("LOW"))
                .andExpect(jsonPath("$.robots[1].lowBattery").value(true))
                .andExpect(jsonPath("$.robots[1].criticalBattery").value(false))
                .andExpect(jsonPath("$.robots[1].energySavingMode").value(true))
                .andExpect(jsonPath("$.robots[1].chargingRequired").value(false))
                .andExpect(jsonPath("$.robots[1].batteryMessage").value("Energy saving mode active."))
                .andExpect(jsonPath("$.robots[1].movementMode").value("ENERGY_SAVING"))
                .andExpect(jsonPath("$.robots[1].movementModeDisplay").value("Energy Saving Mode"))
                .andExpect(jsonPath("$.robots[1].waypointsPerBatteryPercent").value(7))
                .andExpect(jsonPath("$.robots[1].message").value("Returned to Base. Waiting for confirmation."));

        Mission returnedMission = missionRepository.findById(savedMission.getId()).orElseThrow();
        Robot drainedRobot = findRobotByCode("RB-200");
        assertThat(returnedMission.getStatus()).isEqualTo(MissionStatus.WAITING_CONFIRMATION);
        assertThat(returnedMission.getExecutionStep()).isEqualTo(MissionExecutionStep.RETURNED_TO_BASE);
        assertThat(returnedMission.getCurrentPositionKey()).isEqualTo("base-station");
        assertThat(returnedMission.getCompletedAt()).isNull();
        assertThat(returnedMission.getReturnedAt()).isNotNull();
        assertThat(returnedMission.getBatteryAtExecutionStart()).isEqualTo(15);
        assertThat(drainedRobot.getBattery()).isEqualTo(13);
        assertThat(drainedRobot.getStatus()).isEqualTo("IDLE");
        assertThat(drainedRobot.getCharging()).isFalse();

        mockMvc.perform(get("/staff/live-map/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.robots[1].batteryPercent").value(13))
                .andExpect(jsonPath("$.robots[1].batteryDisplayText").value("13% battery"));

        mockMvc.perform(get("/robots"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mover Beta")))
                .andExpect(content().string(containsString("13%")));

        mockMvc.perform(get("/manager/robot-tasks"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mover Beta")))
                .andExpect(content().string(containsString("13% battery")));
    }

    @Test
    void criticalBatteryStartsChargingAfterRobotReturnsToBaseWithoutCompletingMission() throws Exception {
        Robot alpha = findRobotByCode("RB-100");
        alpha.setBattery(5);
        robotRepository.save(alpha);
        Mission mission = saveAssignedMission(
                "REQ-CRITICAL-CONTINUE",
                "Picker Alpha (RB-100)",
                "Small Cargo",
                "Zone A",
                "A1",
                MissionStatus.IN_PROGRESS,
                LocalDateTime.of(2026, 5, 29, 9, 0)
        );
        mission.setAssignedRobotId(alpha.getId());
        mission.setExecutionStep(MissionExecutionStep.MOVING_TO_TARGET);
        mission.setCurrentPositionKey("zone-c-entry");
        mission.setExecutionStartedAt(LocalDateTime.now().minusSeconds(600));
        Mission savedMission = missionRepository.saveAndFlush(mission);

        mockMvc.perform(get("/staff/live-map/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.robots[0].requestCode").value(nullValue()))
                .andExpect(jsonPath("$.robots[0].status").value("CHARGING"))
                .andExpect(jsonPath("$.robots[0].executionStep").value(nullValue()))
                .andExpect(jsonPath("$.robots[0].currentPositionKey").value("charging-station"))
                .andExpect(jsonPath("$.robots[0].batteryPercent").value(1))
                .andExpect(jsonPath("$.robots[0].batteryWarningLevel").value("CRITICAL"))
                .andExpect(jsonPath("$.robots[0].lowBattery").value(true))
                .andExpect(jsonPath("$.robots[0].criticalBattery").value(true))
                .andExpect(jsonPath("$.robots[0].energySavingMode").value(true))
                .andExpect(jsonPath("$.robots[0].chargingRequired").value(true))
                .andExpect(jsonPath("$.robots[0].charging").value(true))
                .andExpect(jsonPath("$.robots[0].batteryMessage").value("Charging at Charging Station."))
                .andExpect(jsonPath("$.robots[0].movementMode").value("CHARGING"))
                .andExpect(jsonPath("$.robots[0].currentActiveStrategyName").value("ChargingStrategy"))
                .andExpect(jsonPath("$.robots[0].message").value("Charging at Charging Station."));

        Mission returnedMission = missionRepository.findById(savedMission.getId()).orElseThrow();
        assertThat(returnedMission.getStatus()).isEqualTo(MissionStatus.WAITING_CONFIRMATION);
        assertThat(returnedMission.getExecutionStep()).isEqualTo(MissionExecutionStep.RETURNED_TO_BASE);
        assertThat(returnedMission.getCurrentPositionKey()).isEqualTo("base-station");
        assertThat(returnedMission.getCancelledAt()).isNull();
        assertThat(returnedMission.getCompletedAt()).isNull();
        assertThat(returnedMission.getReturnedAt()).isNotNull();

        Robot chargingRobot = findRobotByCode("RB-100");
        assertThat(chargingRobot.getStatus()).isEqualTo("CHARGING");
        assertThat(chargingRobot.getCharging()).isTrue();
    }

    @Test
    void repeatedLiveMapPollingDoesNotRepeatChargingStartOrQueuedReassignment() throws Exception {
        Robot alpha = findRobotByCode("RB-100");
        alpha.setBattery(5);
        robotRepository.save(alpha);

        Mission currentMission = saveAssignedMission(
                "REQ-CRITICAL-RETURN",
                "Picker Alpha (RB-100)",
                "Small Cargo",
                "Zone A",
                "A1",
                MissionStatus.IN_PROGRESS,
                LocalDateTime.of(2026, 5, 29, 9, 0)
        );
        currentMission.setAssignedRobotId(alpha.getId());
        currentMission.setExecutionStep(MissionExecutionStep.MOVING_TO_TARGET);
        currentMission.setCurrentPositionKey("zone-c-entry");
        currentMission.setExecutionStartedAt(LocalDateTime.now().minusSeconds(600));
        Mission savedCurrentMission = missionRepository.saveAndFlush(currentMission);

        Mission queuedMission = saveAssignedMission(
                "REQ-QUEUED-REASSIGN",
                "Picker Alpha (RB-100)",
                "Small Cargo",
                "Zone A",
                "A2",
                MissionStatus.ASSIGNED,
                LocalDateTime.of(2026, 5, 29, 9, 5)
        );
        queuedMission.setAssignedRobotId(alpha.getId());
        Mission savedQueuedMission = missionRepository.saveAndFlush(queuedMission);

        mockMvc.perform(get("/staff/live-map/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.robots[0].status").value("CHARGING"))
                .andExpect(jsonPath("$.robots[0].charging").value(true));

        Mission returnedMission = missionRepository.findById(savedCurrentMission.getId()).orElseThrow();
        Mission reassignedMission = missionRepository.findById(savedQueuedMission.getId()).orElseThrow();
        Robot chargingRobot = findRobotByCode("RB-100");
        LocalDateTime chargingStartedAt = chargingRobot.getChargingStartedAt();
        Long reassignedRobotId = reassignedMission.getAssignedRobotId();
        String reassignmentReason = reassignedMission.getAssignmentReason();

        assertThat(returnedMission.getStatus()).isEqualTo(MissionStatus.WAITING_CONFIRMATION);
        assertThat(returnedMission.getCompletedAt()).isNull();
        assertThat(chargingRobot.getStatus()).isEqualTo("CHARGING");
        assertThat(chargingRobot.getCharging()).isTrue();
        assertThat(chargingStartedAt).isNotNull();
        assertThat(reassignedRobotId).isNotNull();
        assertThat(reassignedRobotId).isNotEqualTo(alpha.getId());
        assertThat(reassignmentReason).contains("Reassigned from Picker Alpha (RB-100)");

        mockMvc.perform(get("/staff/live-map/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.robots[0].status").value("CHARGING"))
                .andExpect(jsonPath("$.robots[0].charging").value(true));

        Mission stillWaitingMission = missionRepository.findById(savedCurrentMission.getId()).orElseThrow();
        Mission stillReassignedMission = missionRepository.findById(savedQueuedMission.getId()).orElseThrow();
        Robot stillChargingRobot = findRobotByCode("RB-100");
        assertThat(stillWaitingMission.getStatus()).isEqualTo(MissionStatus.WAITING_CONFIRMATION);
        assertThat(stillWaitingMission.getCompletedAt()).isNull();
        assertThat(stillChargingRobot.getChargingStartedAt()).isEqualTo(chargingStartedAt);
        assertThat(stillReassignedMission.getAssignedRobotId()).isEqualTo(reassignedRobotId);
        assertThat(stillReassignedMission.getAssignmentReason()).isEqualTo(reassignmentReason);
    }

    @Test
    void liveMapStateReturnsBackendRoutesForZoneCZoneBAndZoneATargets() throws Exception {
        Mission zoneAMission = saveAssignedMission(
                "REQ-A1",
                "Picker Alpha (RB-100)",
                "Small Cargo",
                "Zone A",
                "A1",
                MissionStatus.IN_PROGRESS,
                LocalDateTime.of(2026, 5, 29, 9, 0)
        );
        zoneAMission.setExecutionStep(MissionExecutionStep.MOVING_TO_TARGET);
        zoneAMission.setCurrentPositionKey("base-station");
        missionRepository.save(zoneAMission);

        Mission zoneBMission = saveAssignedMission(
                "REQ-B5",
                "Mover Beta (RB-200)",
                "Medium Cargo",
                "Zone B",
                "B5",
                MissionStatus.IN_PROGRESS,
                LocalDateTime.of(2026, 5, 29, 9, 5)
        );
        zoneBMission.setExecutionStep(MissionExecutionStep.MOVING_TO_TARGET);
        zoneBMission.setCurrentPositionKey("base-station");
        missionRepository.save(zoneBMission);

        Mission zoneCMission = saveAssignedMission(
                "REQ-C5",
                "Carrier Gamma (RB-300)",
                "Large Cargo",
                "Zone C",
                "C5",
                MissionStatus.IN_PROGRESS,
                LocalDateTime.of(2026, 5, 29, 9, 10)
        );
        zoneCMission.setExecutionStep(MissionExecutionStep.MOVING_TO_TARGET);
        zoneCMission.setCurrentPositionKey("base-station");
        missionRepository.save(zoneCMission);

        mockMvc.perform(get("/staff/live-map/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.robots[0].targetLocationCode").value("A1"))
                .andExpect(jsonPath("$.robots[0].route[1].positionKey").value("zone-c-left-entry"))
                .andExpect(jsonPath("$.robots[0].route[6].positionKey").value("zone-b-left-entry"))
                .andExpect(jsonPath("$.robots[0].route[9].positionKey").value("bridge-b-a-left-1"))
                .andExpect(jsonPath("$.robots[0].route[13].positionKey").value("zone-a-left-main-2"))
                .andExpect(jsonPath("$.robots[0].route[14].positionKey").value("A1-approach"))
                .andExpect(jsonPath("$.robots[0].route[15].positionKey").value("A1"))
                .andExpect(jsonPath("$.robots[0].route[17].positionKey").value("zone-a-right-main-2"))
                .andExpect(jsonPath("$.robots[0].route[20].positionKey").value("bridge-a-b-right-1"))
                .andExpect(jsonPath("$.robots[0].route[25].positionKey").value("bridge-b-c-right-1"))
                .andExpect(jsonPath("$.robots[0].route[30].positionKey").value("base-station"))
                .andExpect(jsonPath("$.robots[1].targetLocationCode").value("B5"))
                .andExpect(jsonPath("$.robots[1].route[1].positionKey").value("zone-c-left-entry"))
                .andExpect(jsonPath("$.robots[1].route[4].positionKey").value("bridge-c-b-left-1"))
                .andExpect(jsonPath("$.robots[1].route[6].positionKey").value("zone-b-left-entry"))
                .andExpect(jsonPath("$.robots[1].route[8].positionKey").value("B5-approach"))
                .andExpect(jsonPath("$.robots[1].route[9].positionKey").value("B5"))
                .andExpect(jsonPath("$.robots[1].route[13].positionKey").value("bridge-b-c-right-1"))
                .andExpect(jsonPath("$.robots[1].route[18].positionKey").value("base-station"))
                .andExpect(jsonPath("$.robots[2].targetLocationCode").value("C5"))
                .andExpect(jsonPath("$.robots[2].route[0].positionKey").value("base-station"))
                .andExpect(jsonPath("$.robots[2].route[1].positionKey").value("zone-c-left-entry"))
                .andExpect(jsonPath("$.robots[2].route[2].positionKey").value("zone-c-left-main-1"))
                .andExpect(jsonPath("$.robots[2].route[3].positionKey").value("C5-approach"))
                .andExpect(jsonPath("$.robots[2].route[4].positionKey").value("C5"))
                .andExpect(jsonPath("$.robots[2].route[8].positionKey").value("base-station"))
                .andExpect(jsonPath("$.robots[2].route[0].phase").value("MOVE_TO_TARGET"))
                .andExpect(jsonPath("$.robots[2].route[4].phase").value("PICKUP"))
                .andExpect(jsonPath("$.robots[2].route[8].phase").value("RETURN_TO_BASE"));
    }

    @Test
    void liveMapStateReportsWaitingWhenFollowingRobotWouldEnterOccupiedBridgeSegment() throws Exception {
        LocalDateTime executionStartedAt = LocalDateTime.now().minusSeconds(30);
        Robot beta = findRobotByCode("RB-200");
        beta.setBattery(68);
        robotRepository.save(beta);

        Mission firstMission = saveAssignedMission(
                "REQ-BRIDGE-FIRST",
                "Picker Alpha (RB-100)",
                "Small Cargo",
                "Zone A",
                "A1",
                MissionStatus.IN_PROGRESS,
                LocalDateTime.of(2026, 5, 29, 9, 0)
        );
        firstMission.setExecutionStep(MissionExecutionStep.MOVING_TO_TARGET);
        firstMission.setCurrentPositionKey("base-station");
        firstMission.setExecutionStartedAt(executionStartedAt);
        firstMission.setSelectedStrategyName("FastRouteStrategy");
        missionRepository.save(firstMission);

        Mission followingMission = saveAssignedMission(
                "REQ-BRIDGE-FOLLOW",
                "Mover Beta (RB-200)",
                "Medium Cargo",
                "Zone B",
                "B5",
                MissionStatus.IN_PROGRESS,
                LocalDateTime.of(2026, 5, 29, 9, 10)
        );
        followingMission.setExecutionStep(MissionExecutionStep.MOVING_TO_TARGET);
        followingMission.setCurrentPositionKey("base-station");
        followingMission.setExecutionStartedAt(executionStartedAt);
        followingMission.setSelectedStrategyName("FastRouteStrategy");
        missionRepository.save(followingMission);

        mockMvc.perform(get("/staff/live-map/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.robots[0].requestCode").value("REQ-BRIDGE-FIRST"))
                .andExpect(jsonPath("$.robots[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.robots[0].currentPositionKey").value("zone-c-left-main-2"))
                .andExpect(jsonPath("$.robots[0].nextPositionKey").value("bridge-c-b-left-1"))
                .andExpect(jsonPath("$.robots[0].primaryStrategyName").value("FastRouteStrategy"))
                .andExpect(jsonPath("$.robots[0].currentActiveStrategyName").value("FastRouteStrategy"))
                .andExpect(jsonPath("$.robots[0].waiting").value(false))
                .andExpect(jsonPath("$.robots[1].requestCode").value("REQ-BRIDGE-FOLLOW"))
                .andExpect(jsonPath("$.robots[1].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.robots[1].currentPositionKey").value("zone-c-left-main-2"))
                .andExpect(jsonPath("$.robots[1].nextPositionKey").value(nullValue()))
                .andExpect(jsonPath("$.robots[1].segmentProgress").value(0.0))
                .andExpect(jsonPath("$.robots[1].executionStep").value("MOVING_TO_TARGET"))
                .andExpect(jsonPath("$.robots[1].primaryStrategyName").value("FastRouteStrategy"))
                .andExpect(jsonPath("$.robots[1].currentActiveStrategyName").value("ObstacleAvoidanceStrategy"))
                .andExpect(jsonPath("$.robots[1].movementMode").value("OBSTACLE_AVOIDANCE"))
                .andExpect(jsonPath("$.robots[1].batteryDrainMode").value("OBSTACLE_AVOIDANCE"))
                .andExpect(jsonPath("$.robots[1].strategyMessage").value("Waiting for path to clear. Will resume FastRouteStrategy."))
                .andExpect(jsonPath("$.robots[1].message").value("Waiting for path to clear. Will resume FastRouteStrategy."))
                .andExpect(jsonPath("$.robots[1].waiting").value(true))
                .andExpect(jsonPath("$.robots[1].blockedSegment").value("bridge-c-b-left"))
                .andExpect(jsonPath("$.robots[1].route[4].positionKey").value("bridge-c-b-left-1"))
                .andExpect(jsonPath("$.robots[1].route[13].positionKey").value("bridge-b-c-right-1"));
    }

    @Test
    void liveMapStateEndpointMovesReturnedMissionToWaitingConfirmation() throws Exception {
        Mission mission = saveAssignedMission(
                "REQ-READ-ONLY",
                "Picker Alpha (RB-100)",
                "Small Cargo",
                "Zone A",
                "A1",
                MissionStatus.IN_PROGRESS,
                LocalDateTime.of(2026, 5, 29, 9, 0)
        );
        mission.setExecutionStep(MissionExecutionStep.MOVING_TO_TARGET);
        mission.setCurrentPositionKey("zone-c-entry");
        mission.setExecutionStartedAt(LocalDateTime.now().minusSeconds(450));
        Mission savedMission = missionRepository.saveAndFlush(mission);

        mockMvc.perform(get("/staff/live-map/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.robots[0].requestCode").value("REQ-READ-ONLY"))
                .andExpect(jsonPath("$.robots[0].status").value("WAITING_CONFIRMATION"))
                .andExpect(jsonPath("$.robots[0].executionStep").value("RETURNED_TO_BASE"))
                .andExpect(jsonPath("$.robots[0].currentPositionKey").value("base-station"))
                .andExpect(jsonPath("$.robots[0].robotStatus").value("IDLE"))
                .andExpect(jsonPath("$.robots[0].message").value("Returned to Base. Waiting for confirmation."));

        Mission returnedMission = missionRepository.findById(savedMission.getId()).orElseThrow();
        assertThat(returnedMission.getStatus()).isEqualTo(MissionStatus.WAITING_CONFIRMATION);
        assertThat(returnedMission.getExecutionStep()).isEqualTo(MissionExecutionStep.RETURNED_TO_BASE);
        assertThat(returnedMission.getCurrentPositionKey()).isEqualTo("base-station");
        assertThat(returnedMission.getCompletedAt()).isNull();
        assertThat(returnedMission.getReturnedAt()).isNotNull();

        Robot availableRobot = findRobotByCode("RB-100");
        assertThat(availableRobot.getStatus()).isEqualTo("IDLE");
        assertThat(availableRobot.getCharging()).isFalse();
    }

    @Test
    void liveMapScriptHidesOtherRobotsWhenOneRobotIsSelectedAndCanShowAllAgain() throws Exception {
        String script = Files.readString(Path.of("src/main/resources/static/js/staff-live-map.js"));
        String styles = Files.readString(Path.of("src/main/resources/static/css/staff-live-map.css"));

        assertThat(script).contains("function showAllRobots()");
        assertThat(script).contains("function startRouteAnimation()");
        assertThat(script).contains("window.startLiveMapRouteAnimation = startRouteAnimation");
        assertThat(script).contains("selectedRobotKey = null");
        assertThat(script).contains("function renderWarehouseOverview()");
        assertThat(script).contains("Object.keys(zoneData).forEach");
        assertThat(script).contains("baseStationPosition");
        assertThat(script).contains("routeWaypoints");
        assertThat(script).contains("\"data-route-point\": routePointName");
        assertThat(script).contains("pad.dataset.location = locationCode");
        assertThat(script).contains("dot.dataset.routePoint = routePointName");
        assertThat(script).contains("var outboundRoutePointSequences = {");
        assertThat(script).contains("c: [\"base-station\", \"zone-c-left-entry\"]");
        assertThat(script).contains("b: [\"base-station\", \"zone-c-left-entry\", \"zone-c-left-main-1\", \"zone-c-left-main-2\", \"bridge-c-b-left-1\", \"bridge-c-b-left-2\", \"zone-b-left-entry\"]");
        assertThat(script).contains("a: [\"base-station\", \"zone-c-left-entry\", \"zone-c-left-main-1\", \"zone-c-left-main-2\", \"bridge-c-b-left-1\", \"bridge-c-b-left-2\", \"zone-b-left-entry\", \"zone-b-left-main-1\", \"zone-b-left-main-2\", \"bridge-b-a-left-1\", \"bridge-b-a-left-2\", \"zone-a-left-entry\"]");
        assertThat(script).contains("var returnRoutePointSequences = {");
        assertThat(script).contains("c: [\"zone-c-right-exit\", \"base-station\"]");
        assertThat(script).contains("b: [\"zone-b-right-exit\", \"bridge-b-c-right-1\", \"bridge-b-c-right-2\", \"zone-c-right-main-2\", \"zone-c-right-main-1\", \"zone-c-right-exit\", \"base-station\"]");
        assertThat(script).contains("\"base-station\": createRouteWaypointDefinition(\"c\", baseStationPosition)");
        assertThat(script).contains("var chargingStationPosition = toRouteWorldPosition(\"c\", toRoutePosition(570, 392))");
        assertThat(script).contains("\"charging-station\": createRouteWaypointDefinition(\"c\", chargingStationPosition)");
        assertThat(script).contains("var OUTBOUND_COLUMN_LEFT = 36.67");
        assertThat(script).contains("var RETURN_COLUMN_LEFT = 63.33");
        assertThat(script).contains("aliasRouteWaypoint(waypoints, \"zone-\" + zoneName + \"-left-entry\", \"zone-\" + zoneName + \"-entry\")");
        assertThat(script).contains("aliasRouteWaypoint(waypoints, \"zone-\" + zoneName + \"-left-main-1\", \"zone-\" + zoneName + \"-main-1\")");
        assertThat(script).contains("aliasRouteWaypoint(waypoints, \"zone-\" + zoneName + \"-left-main-2\", \"zone-\" + zoneName + \"-main-2\")");
        assertThat(script).contains("aliasRouteWaypoint(waypoints, \"zone-\" + zoneName + \"-right-exit\", \"zone-\" + zoneName + \"-lower-cross\")");
        assertThat(script).contains("waypoints[\"bridge-c-b-left-1\"] = createRouteWaypointDefinition(\"c\", { left: OUTBOUND_COLUMN_LEFT, top: 65.22 })");
        assertThat(script).contains("waypoints[\"bridge-a-b-right-1\"] = createRouteWaypointDefinition(\"a\", { left: RETURN_COLUMN_LEFT, top: 26.63 })");
        assertThat(script).contains("addTargetApproachWaypoints(waypoints)");
        assertThat(script).contains("function getTargetApproachLocalPosition(slotPosition)");
        assertThat(script).contains("function getTargetRowRouteLocalPosition(locationCode)");
        assertThat(script).contains("waypoints[locationCode + \"-approach\"]");
        assertThat(script).contains("waypoints[\"bridge-c-b-left-2\"]");
        assertThat(script).contains("waypoints[\"bridge-b-a-left-2\"]");
        assertThat(script).contains("waypoints[\"bridge-b-c-right-1\"]");
        assertThat(script).contains("waypoints[\"bridge-a-b-right-1\"]");
        assertThat(script).contains("function buildPickupRoute(robotKey, zoneName)");
        assertThat(script).contains("function createCargoApproachRoutes(zoneName)");
        assertThat(script).contains("class: \"cargo-approach-line\"");
        assertThat(script).contains("class: \"cargo-approach-node\"");
        assertThat(script).contains("\"data-route-point\": approachRoutePoint");
        assertThat(script).contains("function appendRouteWorldApproachLines(layer)");
        assertThat(script).contains("\"route-world-line is-approach-line\"");
        assertThat(script).contains("function buildRouteToTarget(locationCode)");
        assertThat(script).contains("function buildOutboundTargetRowRoutePointNames(zoneName, locationCode)");
        assertThat(script).contains("function buildReturnTargetRowRoutePointNames(zoneName, locationCode)");
        assertThat(script).contains("buildOutboundTargetRowRoutePointNames(zoneName, normalizedLocationCode).forEach");
        assertThat(script).contains("buildReturnTargetRowRoutePointNames(zoneName, normalizedLocationCode).forEach");
        assertThat(script).contains("var goRoute = buildRouteToTarget(robot ? robot.locationCode : \"\")");
        assertThat(script).contains("var returnRoute = buildReturnRoute(robot ? robot.locationCode : \"\")");
        assertThat(script).contains("return goRoute.concat(returnRoute.slice(1));");
        assertThat(script).contains("function renderRouteAnimationMap(robotKey, targetZoneName, route)");
        assertThat(script).contains("routeMap.className = \"route-animation-map\"");
        assertThat(script).contains("routeMap.appendChild(createRouteAnimationRobotMarker(robotKey, route[0].position))");
        assertThat(script).contains("drawRoutePreviewPath(routeMap, route)");
        assertThat(script).contains("function drawRoutePreviewPath(canvas, route)");
        assertThat(script).contains("addNamedRouteWaypoint(route, normalizedLocationCode + \"-approach\", \"move\")");
        assertThat(script).contains("addRouteWaypoint(route, targetPosition, \"pickup\", zoneName, \"target:\" + normalizedLocationCode, normalizedLocationCode)");
        assertThat(script).contains("addNamedRouteWaypoint(route, normalizedLocationCode + \"-approach\", \"return\")");
        assertThat(script).contains("B5: { left: 50, top: 44.65 }");
        assertThat(script).contains("C5: { left: 50, top: 44.65 }");
        assertThat(script).contains("A1: { left: 27.22, top: 16.74 }");
        assertThat(script).contains("hasRobotMissionTarget");
        assertThat(script).contains("updateMissionFlowStep(robotKey, \"move\")");
        assertThat(script).contains("updateMissionFlowStep(robotKey, \"pickup\")");
        assertThat(script).contains("updateMissionFlowStep(robotKey, \"returned\")");
        assertThat(script).contains("Preview only: pickup at ");
        assertThat(script).contains("Preview only: returning to Base.");
        assertThat(script).contains("Returned to Base. Waiting for confirmation.");
        assertThat(script).contains("marker.dataset.locationCode = robot.locationCode");
        assertThat(script).contains("marker.dataset.currentPositionKey = robot.currentPositionKey || \"\"");
        assertThat(script).contains("marker.dataset.nextPositionKey = robot.nextPositionKey || \"\"");
        assertThat(script).contains("marker.dataset.segmentProgress = String(robot.segmentProgress || 0)");
        assertThat(script).contains("marker.dataset.positionAnchorType = robot.positionAnchor ? robot.positionAnchor.anchorType : \"\"");
        assertThat(script).contains("if (robotZone !== zoneName)");
        assertThat(script).contains("if (selectedRobotKey && !isSelected)");
        assertThat(script).contains("return null;");
        assertThat(script).contains("card.hidden = !isActive");
        assertThat(script).contains("window.showAllLiveMapRobots = showAllRobots");
        assertThat(script).contains("var LIVE_MAP_STATE_URL = \"/staff/live-map/state\"");
        assertThat(script).contains("var LIVE_MAP_STATE_POLL_INTERVAL_MS = 1000");
        assertThat(script).contains("var ROBOT_VISUAL_MOVEMENT_DURATION_MS = ROBOT_VISUAL_NORMAL_MOVEMENT_DURATION_MS");
        assertThat(script).contains("function fetchLiveMapState()");
        assertThat(script).contains("fetch(LIVE_MAP_STATE_URL");
        assertThat(script).contains("window.setInterval(fetchLiveMapState, LIVE_MAP_STATE_POLL_INTERVAL_MS)");
        assertThat(script).contains("window.addEventListener(\"beforeunload\", stopLiveMapPolling)");
        assertThat(script).contains("function renderLiveMapState(state)");
        assertThat(script).contains("window.WarehouseNotifications.trackLiveMapState(state)");
        assertThat(script).contains("function resolvePositionAnchor(currentPositionKey, targetLocationCode)");
        assertThat(script).contains("function resolveSegmentPositionAnchor(currentPositionKey, nextPositionKey, segmentProgress, targetLocationCode)");
        assertThat(script).contains("robot.nextPositionKey = normalizePositionKey(robotState.nextPositionKey || \"\")");
        assertThat(script).contains("robot.segmentProgress = clampSegmentProgress(robotState.segmentProgress)");
        assertThat(script).contains("function getRobotVisualRenderPosition(robotKey, zoneName, targetPosition)");
        assertThat(script).contains("function getInterpolatedRobotVisualPosition(robotKey, timestamp)");
        assertThat(script).contains("var ROBOT_VISUAL_NORMAL_MOVEMENT_DURATION_MS = 15000");
        assertThat(script).contains("var ROBOT_VISUAL_BRIDGE_MOVEMENT_DURATION_MS = 12000");
        assertThat(script).contains("var ROBOT_VISUAL_SEGMENT_SAMPLE_DURATION_MS = 900");
        assertThat(script).contains("function getRobotVisualMovementDurationMs(robotKey)");
        assertThat(script).contains("function updateRobotMarkerAnimationFrame()");
        assertThat(script).contains("function scheduleRobotMarkerAnimations()");
        assertThat(script).contains("window.requestAnimationFrame");
        assertThat(script).contains("applyMarkerCssPosition(marker, visualPosition || targetPosition)");
        assertThat(script).contains("marker.style.setProperty(\"--robot-motion-duration\", getRobotVisualMovementDurationMs(robotKey) + \"ms\")");
        assertThat(script).contains("anchorType: \"data-location\"");
        assertThat(script).contains("anchorType: \"data-route-point\"");
        assertThat(script).contains("function executionStepToFlowStep(executionStep)");
        assertThat(script).contains("executionStep === \"MOVING_TO_TARGET\"");
        assertThat(script).contains("executionStep === \"PICKING_UP\"");
        assertThat(script).contains("executionStep === \"RETURNING_TO_BASE\"");
        assertThat(script).contains("executionStep === \"RETURNED_TO_BASE\"");
        assertThat(script).contains("function createBatteryStatusLine(robot)");
        assertThat(script).contains("function createStrategyStatusLine(robot)");
        assertThat(script).contains("appendStrategyStatusLine(targetBox, robot)");
        assertThat(script).contains("function createBatteryWarningBadge(robot)");
        assertThat(script).contains("function normalizeBatteryWarningLevel(value)");
        assertThat(script).contains("robot.batteryPercent = normalizeBatteryPercent(robotState.batteryPercent)");
        assertThat(script).contains("robot.batteryDrainPercent = Math.max(0, Number(robotState.batteryDrainPercent || 0))");
        assertThat(script).contains("robot.batteryWarningLevel = normalizeBatteryWarningLevel(robotState.batteryWarningLevel)");
        assertThat(script).contains("robot.energySavingMode = Boolean(robotState.energySavingMode)");
        assertThat(script).contains("robot.chargingRequired = Boolean(robotState.chargingRequired)");
        assertThat(script).contains("robot.charging = Boolean(robotState.charging)");
        assertThat(script).contains("robot.chargingRecoveredPercent = Math.max(0, Number(robotState.chargingRecoveredPercent || 0))");
        assertThat(script).contains("robot.movementMode = robotState.movementMode || \"\"");
        assertThat(script).contains("robot.movementModeDisplay = robotState.movementModeDisplay || \"\"");
        assertThat(script).contains("robot.waypointsPerBatteryPercent = Number(robotState.waypointsPerBatteryPercent || 0)");
        assertThat(script).contains("robot.batteryDrainMode = robotState.batteryDrainMode || \"\"");
        assertThat(script).contains("robot.primaryStrategyName = robotState.primaryStrategyName || \"\"");
        assertThat(script).contains("robot.currentActiveStrategyName = robotState.currentActiveStrategyName || \"\"");
        assertThat(script).contains("robot.strategyMessage = robotState.strategyMessage || \"\"");
        assertThat(script).contains("robot.robotStatus = robotState.robotStatus || \"\"");
        assertThat(script).contains("function formatMovementModeText(robot)");
        assertThat(script).contains("robot.movementModeDisplay");
        assertThat(script).contains("Charging at Charging Station.");
        assertThat(script).contains("Battery unavailable");
        assertThat(script).contains("Returned to Base. Waiting for confirmation.");
        assertThat(script).contains("Primary Strategy: ");
        assertThat(script).contains("Active Strategy: ");
        assertThat(script).contains("return normalizedName || \"No active strategy\"");
        assertThat(script).doesNotContain("return \"Fast\"");
        assertThat(script).doesNotContain("return \"Energy Saving\"");
        assertThat(script).doesNotContain("return \"Heavy Load\"");
        assertThat(script).contains("strategy-message-text");
        assertThat(script).contains("robot.waiting = Boolean(robotState.waiting)");
        assertThat(script).contains("No active pickup mission assigned.");
        assertThat(script).contains("Live Map is following backend execution state.");
        assertThat(script).contains("Visual Route Preview");
        assertThat(script).contains("Following Backend State");
        assertThat(styles).contains(".active-zone-map.is-route-animation");
        assertThat(styles).contains(".route-animation-map");
        assertThat(styles).contains(".route-world-zone");
        assertThat(styles).contains(".route-world-dot");
        assertThat(styles).contains(".route-world-dot.is-approach");
        assertThat(styles).contains(".route-world-line.is-approach-line");
        assertThat(styles).contains(".cargo-approach-line");
        assertThat(styles).contains(".cargo-approach-node");
        assertThat(styles).contains(".route-animation-robot");
        assertThat(styles).contains("top var(--robot-motion-duration, 1200ms)");
        assertThat(styles).contains("left var(--robot-motion-duration, 1200ms)");
        assertThat(styles).contains(".battery-status-line");
        assertThat(styles).contains(".battery-warning-badge");
        assertThat(styles).contains(".battery-warning-badge.is-critical");
        assertThat(styles).contains(".battery-warning-badge.is-charging");
        assertThat(styles).contains(".battery-message-text");
        assertThat(styles).contains(".movement-mode-text");
        assertThat(styles).contains(".strategy-status-line");
        assertThat(styles).contains(".strategy-active-text");
        assertThat(styles).contains("will-change: top, left, transform;");
        assertThat(styles).contains(".map-robot-marker.is-smoothing");
        assertThat(styles).contains(".map-robot-marker.is-backend-live.is-smoothing");
        assertThat(script).doesNotContain("is-dimmed");
        assertThat(script).doesNotContain("renderAnimationWaypoint");
        assertThat(script).doesNotContain("visibleRobotZone");
        assertThat(script).doesNotContain("drawRoutePreviewSegment");
        assertThat(script).doesNotContain("fetch(\"/staff/missions");
        assertThat(script).doesNotContain("completeMission");
        assertThat(script).doesNotContain("WebSocket");
        assertThat(script).doesNotContain("bridgeWaiting");
        assertThat(script).doesNotContain("laneOccupancy");
        assertThat(script).doesNotContain("leftLane");
        assertThat(script).doesNotContain("rightLane");
        assertThat(script).doesNotContain("pathfinding");
        assertThat(script).doesNotContain("XMLHttpRequest");
    }

    private Mission saveAssignedMission(String requestCode,
                                        String assignedRobotName,
                                        String cargoType,
                                        String zone,
                                        String locationCode,
                                        MissionStatus status,
                                        LocalDateTime createdAt) {
        Mission mission = new Mission(
                requestCode,
                "Map Customer",
                cargoType,
                zone,
                locationCode,
                2,
                status,
                "Map current mission selection test"
        );
        mission.setAssignedRobotName(assignedRobotName);
        mission.setCreatedAt(createdAt);
        return missionRepository.save(mission);
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

    private void clearChargingState(Robot robot) {
        robot.setChargingRequired(false);
        robot.setCharging(false);
        robot.setChargingStartedAt(null);
        robot.setChargingCompletedAt(null);
        robot.setBatteryBeforeCharging(null);
    }
}
