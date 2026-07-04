package com.warehouse.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.warehouse.model.Mission;
import com.warehouse.model.MissionExecutionStep;
import com.warehouse.model.MissionStatus;
import com.warehouse.model.Robot;
import com.warehouse.model.Rule;
import com.warehouse.model.ZonePolicyAssignment;
import com.warehouse.repository.MissionRepository;
import com.warehouse.repository.RobotRepository;
import com.warehouse.repository.RuleRepository;
import com.warehouse.repository.ZonePolicyAssignmentRepository;
import com.warehouse.service.MissionService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class StaffPickupRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MissionRepository missionRepository;

    @Autowired
    private RobotRepository robotRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private ZonePolicyAssignmentRepository assignmentRepository;

    @BeforeEach
    void cleanMissions() {
        assignmentRepository.deleteAll();
        missionRepository.deleteAll();
        resetSeededRobots();
    }

    @Test
    void pickupRequestRouteLoadsWithFormOptions() throws Exception {
        mockMvc.perform(get("/staff/pickup-request"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff-pickup-request"))
                .andExpect(model().attributeExists(
                        "pickupRequest",
                        "cargoTypes",
                        "warehouseZones",
                        "priorities",
                        "zoneByCargoType",
                        "loadByCargoType",
                        "locationsByZone"
                ))
                .andExpect(content().string(containsString("Create Pickup Request")))
                .andExpect(content().string(containsString("Staff")))
                .andExpect(content().string(containsString("Small Cargo")))
                .andExpect(content().string(containsString("Medium Cargo")))
                .andExpect(content().string(containsString("Large Cargo")))
                .andExpect(content().string(containsString("Small Cargo -&gt; Zone A")))
                .andExpect(content().string(containsString("Medium Cargo -&gt; Zone B")))
                .andExpect(content().string(containsString("Large Cargo -&gt; Zone C")))
                .andExpect(content().string(containsString("robotLoad means estimated cargo load percentage.")))
                .andExpect(content().string(containsString("Small Cargo -&gt; Zone A / 30% load")))
                .andExpect(content().string(containsString("Medium Cargo -&gt; Zone B / 60% load")))
                .andExpect(content().string(containsString("Large Cargo -&gt; Zone C / 90% load")))
                .andExpect(content().string(containsString("Select a cargo type to display available storage locations.")))
                .andExpect(content().string(containsString("saves it as a PENDING mission")));
    }

    @Test
    void pickupRequestSubmitSavesSmallCargoMission() throws Exception {
        mockMvc.perform(post("/staff/pickup-request")
                        .param("requestCode", "REQ-A1")
                        .param("customerName", "Customer A")
                        .param("cargoType", "Small Cargo")
                        .param("locationCode", "A1")
                        .param("warehouseZone", "Zone A")
                        .param("priority", "1")
                        .param("notes", "Customer is waiting at counter 1."))
                .andExpect(status().isOk())
                .andExpect(view().name("staff-pickup-request"))
                .andExpect(model().attributeExists("pickupRequest", "savedMission", "successMessage"))
                .andExpect(content().string(containsString("Saved Mission Summary")))
                .andExpect(content().string(containsString("REQ-A1")))
                .andExpect(content().string(containsString("Customer A")))
                .andExpect(content().string(containsString("Small Cargo")))
                .andExpect(content().string(containsString("Zone A")))
                .andExpect(content().string(containsString("A1")))
                .andExpect(content().string(containsString("1 = High")))
                .andExpect(content().string(containsString("PENDING")));

        Mission mission = missionRepository.findAll().get(0);
        assertThat(mission.getRequestCode()).isEqualTo("REQ-A1");
        assertThat(mission.getCustomerName()).isEqualTo("Customer A");
        assertThat(mission.getCargoType()).isEqualTo("Small Cargo");
        assertThat(mission.getZone()).isEqualTo("Zone A");
        assertThat(mission.getLocationCode()).isEqualTo("A1");
        assertThat(mission.getPriority()).isEqualTo(1);
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.PENDING);
        assertThat(mission.getExecutionStep()).isEqualTo(MissionExecutionStep.NOT_STARTED);
        assertThat(mission.getCurrentPositionKey()).isNull();
        assertThat(mission.getExecutionStartedAt()).isNull();
        assertThat(mission.getPickupReachedAt()).isNull();
        assertThat(mission.getReturnedAt()).isNull();
    }

    @Test
    void pickupRequestSubmitSavesMediumCargoMission() throws Exception {
        mockMvc.perform(post("/staff/pickup-request")
                        .param("requestCode", "REQ-1001")
                        .param("customerName", "Customer B")
                        .param("cargoType", "Medium Cargo")
                        .param("locationCode", "B5")
                        .param("priority", "1")
                        .param("notes", "Customer is waiting at counter 2."))
                .andExpect(status().isOk())
                .andExpect(view().name("staff-pickup-request"))
                .andExpect(model().attributeExists("pickupRequest", "savedMission", "successMessage"))
                .andExpect(content().string(containsString("Saved Mission Summary")))
                .andExpect(content().string(containsString("REQ-1001")))
                .andExpect(content().string(containsString("Medium Cargo")))
                .andExpect(content().string(containsString("Zone B")))
                .andExpect(content().string(containsString("B5")))
                .andExpect(content().string(containsString("1 = High")))
                .andExpect(content().string(containsString("PENDING")));

        Mission mission = missionRepository.findAll().get(0);
        assertThat(mission.getRequestCode()).isEqualTo("REQ-1001");
        assertThat(mission.getCargoType()).isEqualTo("Medium Cargo");
        assertThat(mission.getZone()).isEqualTo("Zone B");
        assertThat(mission.getLocationCode()).isEqualTo("B5");
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.PENDING);
    }

    @Test
    void pickupRequestSubmitSavesLargeCargoMission() throws Exception {
        mockMvc.perform(post("/staff/pickup-request")
                        .param("requestCode", "REQ-C9")
                        .param("customerName", "Customer C")
                        .param("cargoType", "Large Cargo")
                        .param("locationCode", "C9")
                        .param("warehouseZone", "Zone C")
                        .param("priority", "3")
                        .param("notes", "Move after manager confirms robot availability."))
                .andExpect(status().isOk())
                .andExpect(view().name("staff-pickup-request"))
                .andExpect(model().attributeExists("pickupRequest", "savedMission", "successMessage"))
                .andExpect(content().string(containsString("Saved Mission Summary")))
                .andExpect(content().string(containsString("REQ-C9")))
                .andExpect(content().string(containsString("Large Cargo")))
                .andExpect(content().string(containsString("Zone C")))
                .andExpect(content().string(containsString("C9")))
                .andExpect(content().string(containsString("3 = Low")))
                .andExpect(content().string(containsString("PENDING")));

        Mission mission = missionRepository.findAll().get(0);
        assertThat(mission.getRequestCode()).isEqualTo("REQ-C9");
        assertThat(mission.getCargoType()).isEqualTo("Large Cargo");
        assertThat(mission.getZone()).isEqualTo("Zone C");
        assertThat(mission.getLocationCode()).isEqualTo("C9");
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.PENDING);
    }

    @Test
    void pickupRequestSubmitRejectsInvalidValues() throws Exception {
        mockMvc.perform(post("/staff/pickup-request")
                        .param("requestCode", "")
                        .param("customerName", "")
                        .param("cargoType", "Invalid Cargo")
                        .param("locationCode", "")
                        .param("warehouseZone", "Zone D")
                        .param("priority", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff-pickup-request"))
                .andExpect(model().attributeExists("validationErrors"))
                .andExpect(content().string(containsString("Please fix the request details.")))
                .andExpect(content().string(containsString("Cargo type must be Small Cargo, Medium Cargo, or Large Cargo.")))
                .andExpect(content().string(containsString("Priority must be 1 = High, 2 = Medium, or 3 = Low.")));

        assertThat(missionRepository.count()).isZero();
    }

    @Test
    void pickupRequestSubmitRejectsCargoZoneLocationMismatchAndDoesNotSave() throws Exception {
        mockMvc.perform(post("/staff/pickup-request")
                        .param("requestCode", "REQ-MISMATCH")
                        .param("cargoType", "Small Cargo")
                        .param("locationCode", "B1")
                        .param("warehouseZone", "Zone B")
                        .param("priority", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff-pickup-request"))
                .andExpect(model().attributeExists("validationErrors"))
                .andExpect(content().string(containsString("Please fix the request details.")))
                .andExpect(content().string(containsString("Small Cargo must be assigned to Zone A.")))
                .andExpect(content().string(containsString("Cargo location must match the assigned zone.")));

        assertThat(missionRepository.count()).isZero();
    }

    @Test
    void missionsRouteLoadsSavedMissions() throws Exception {
        missionRepository.save(new Mission(
                "REQ-LIST",
                "Customer List",
                "Small Cargo",
                "Zone A",
                "A1",
                2,
                MissionStatus.PENDING,
                "List page note"
        ));
        Mission completedMission = new Mission(
                "REQ-HISTORY",
                "Customer History",
                "Large Cargo",
                "Zone C",
                "C9",
                3,
                MissionStatus.COMPLETED,
                "Completed history note"
        );
        completedMission.setAssignedRobotName("Carrier Gamma (RB-300)");
        completedMission.setMatchedRuleName("Safe Route Rule");
        completedMission.setSelectedStrategyName("SafeRouteStrategy");
        completedMission.setActionMessage("RB-300 follows the safe route.");
        completedMission.setDecisionSummary("Completed mission decision summary.");
        completedMission.setProcessedAt(LocalDateTime.of(2026, 5, 24, 10, 30));
        completedMission.setCompletedAt(LocalDateTime.of(2026, 5, 24, 10, 45));
        missionRepository.save(completedMission);

        mockMvc.perform(get("/staff/missions"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff-missions"))
                .andExpect(model().attributeExists(
                        "missions",
                        "activeMissions",
                        "completedMissions",
                        "cancelledMissions",
                        "historyMissions",
                        "visibleMissions",
                        "selectedMissionFilter",
                        "visibleMissionGroupTitle"
                ))
                .andExpect(content().string(containsString("My Missions")))
                .andExpect(content().string(containsString("Mission Status")))
                .andExpect(content().string(containsString("Active / Waiting")))
                .andExpect(content().string(containsString("Cancelled / Stopped")))
                .andExpect(content().string(containsString("All non-deleted missions")))
                .andExpect(content().string(containsString("REQ-LIST")))
                .andExpect(content().string(containsString("Customer List")))
                .andExpect(content().string(containsString("Small Cargo")))
                .andExpect(content().string(containsString("Zone A")))
                .andExpect(content().string(containsString("A1")))
                .andExpect(content().string(containsString("2 = Medium")))
                .andExpect(content().string(containsString("PENDING")))
                .andExpect(content().string(containsString("No robot assigned yet.")))
                .andExpect(content().string(containsString("Waiting for mission processing.")))
                .andExpect(content().string(containsString("NOT_STARTED")))
                .andExpect(content().string(containsString("Process Mission / Assign Robot")))
                .andExpect(content().string(containsString("Stop")))
                .andExpect(content().string(not(containsString("REQ-HISTORY"))))
                .andExpect(content().string(not(containsString("Start Execution"))))
                .andExpect(content().string(not(containsString("Delete"))));

        mockMvc.perform(get("/staff/missions").param("filter", "completed"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Completed")))
                .andExpect(content().string(containsString("REQ-HISTORY")))
                .andExpect(content().string(containsString("COMPLETED")))
                .andExpect(content().string(containsString("3 = Low")))
                .andExpect(content().string(containsString("Carrier Gamma (RB-300)")))
                .andExpect(content().string(containsString("SafeRouteStrategy")))
                .andExpect(content().string(containsString("Completed mission decision summary.")))
                .andExpect(content().string(containsString("Completed history")))
                .andExpect(content().string(not(containsString("Process Mission / Assign Robot"))))
                .andExpect(content().string(not(containsString(">Stop<"))))
                .andExpect(content().string(not(containsString(">Delete<"))));
    }

    @Test
    void missionDetailRouteLoadsReadOnlyMissionDecisionOutput() throws Exception {
        Mission mission = new Mission(
                "REQ-DETAIL",
                "Customer Detail",
                "Small Cargo",
                "Zone A",
                "A7",
                1,
                MissionStatus.ASSIGNED,
                "Detail note"
        );
        mission.setAssignedRobotName("Picker Alpha (RB-100)");
        mission.setAssignmentReason("Assigned to Picker Alpha because it has fewer active missions.");
        mission.setMatchedRuleName("Urgent Task Fast Route Rule");
        mission.setSelectedStrategyName("FastRouteStrategy");
        mission.setActionMessage("RB-100 selects the fastest available route.");
        mission.setDecisionSummary("Mission detail decision summary.");
        mission.setProcessedAt(LocalDateTime.of(2026, 5, 24, 11, 15));
        Mission savedMission = missionRepository.save(mission);

        mockMvc.perform(get("/staff/missions/{id}", savedMission.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("staff-mission-detail"))
                .andExpect(model().attributeExists("mission"))
                .andExpect(content().string(containsString("Mission detail")))
                .andExpect(content().string(containsString("Current Status")))
                .andExpect(content().string(containsString("Request Information")))
                .andExpect(content().string(containsString("Cargo Information")))
                .andExpect(content().string(containsString("Assignment Information")))
                .andExpect(content().string(containsString("Rule and Strategy Result")))
                .andExpect(content().string(containsString("Execution Step")))
                .andExpect(content().string(containsString("NOT_STARTED")))
                .andExpect(content().string(containsString("Current Position")))
                .andExpect(content().string(containsString("No backend position recorded")))
                .andExpect(content().string(containsString("Execution Started")))
                .andExpect(content().string(containsString("Not started")))
                .andExpect(content().string(containsString("Pickup Reached")))
                .andExpect(content().string(containsString("Not reached")))
                .andExpect(content().string(containsString("Returned To Base")))
                .andExpect(content().string(containsString("Not returned")))
                .andExpect(content().string(containsString("REQ-DETAIL")))
                .andExpect(content().string(containsString("Customer Detail")))
                .andExpect(content().string(containsString("Small Cargo")))
                .andExpect(content().string(containsString("Zone A")))
                .andExpect(content().string(containsString("A7")))
                .andExpect(content().string(containsString("1 = High")))
                .andExpect(content().string(containsString("ASSIGNED")))
                .andExpect(content().string(containsString("Picker Alpha (RB-100)")))
                .andExpect(content().string(containsString("Urgent Task Fast Route Rule")))
                .andExpect(content().string(containsString("FastRouteStrategy")))
                .andExpect(content().string(containsString("Mission detail decision summary.")))
                .andExpect(content().string(not(containsString("Process Mission / Assign Robot"))));
    }

    @Test
    void processPendingMissionAssignsRobotAndStoresDecision() throws Exception {
        Rule fastRouteRule = ruleRepository.findByRuleName("Urgent Task Fast Route Rule").orElseThrow();
        assignmentRepository.save(new ZonePolicyAssignment("Zone A", "Small Cargo", fastRouteRule.getId()));
        Mission mission = missionRepository.save(new Mission(
                "REQ-PROCESS",
                "Customer Process",
                "Small Cargo",
                "Zone A",
                "A1",
                1,
                MissionStatus.PENDING,
                "Process this mission"
        ));

        mockMvc.perform(post("/staff/missions/{id}/process", mission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions"))
                .andExpect(flash().attribute(
                        "successMessage",
                        "Mission REQ-PROCESS processed through RuleEvaluator and StrategyContext."
                ));

        Mission processedMission = missionRepository.findById(mission.getId()).orElseThrow();
        assertThat(processedMission.getStatus()).isEqualTo(MissionStatus.ASSIGNED);
        assertThat(processedMission.getStatus()).isNotEqualTo(MissionStatus.COMPLETED);
        assertThat(processedMission.getAssignedRobotName()).isEqualTo("Carrier Gamma (RB-300)");
        assertThat(processedMission.getAssignmentReason()).contains("equal active workload");
        assertThat(processedMission.getMatchedRuleName()).isEqualTo("Urgent Task Fast Route Rule");
        assertThat(processedMission.getSelectedStrategyName()).isEqualTo("FastRouteStrategy");
        assertThat(processedMission.getActionMessage()).contains("RB-300 selects the fastest available route.");
        assertThat(processedMission.getDecisionSummary()).contains("Zone A policy 'Urgent Task Fast Route Rule'");
        assertThat(processedMission.getDecisionSummary()).contains("Active workload for selected robot: 0 active mission(s), 0 active high-priority mission(s)");
        assertThat(processedMission.getDecisionSummary()).contains("robotLoad=30");
        assertThat(processedMission.getDecisionSummary()).contains("StrategyContext dispatched FastRouteStrategy");
        assertThat(processedMission.getProcessedAt()).isNotNull();
        assertThat(processedMission.getExecutionStep()).isEqualTo(MissionExecutionStep.NOT_STARTED);
        assertThat(processedMission.getCurrentPositionKey()).isNull();
        assertThat(processedMission.getExecutionStartedAt()).isNull();
        assertThat(processedMission.getPickupReachedAt()).isNull();
        assertThat(processedMission.getReturnedAt()).isNull();

        mockMvc.perform(get("/staff/missions"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ASSIGNED")))
                .andExpect(content().string(containsString("Carrier Gamma (RB-300)")))
                .andExpect(content().string(containsString("equal active workload")))
                .andExpect(content().string(containsString("Urgent Task Fast Route Rule")))
                .andExpect(content().string(containsString("FastRouteStrategy")))
                .andExpect(content().string(containsString("Start Execution")))
                .andExpect(content().string(containsString("Robot is still working.")))
                .andExpect(content().string(containsString("Stop")))
                .andExpect(content().string(not(containsString("/staff/missions/" + processedMission.getId() + "/complete"))))
                .andExpect(content().string(not(containsString("Process Mission / Assign Robot"))));
    }

    @Test
    void processPendingLargeCargoUsesNinetyLoadAndSelectsHeavyLoadStrategy() throws Exception {
        Mission mission = missionRepository.save(new Mission(
                "REQ-LARGE-HEAVY",
                "Customer Large",
                "Large Cargo",
                "Zone C",
                "C9",
                3,
                MissionStatus.PENDING,
                "Large cargo should be evaluated as heavy load"
        ));

        mockMvc.perform(post("/staff/missions/{id}/process", mission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions"))
                .andExpect(flash().attribute(
                        "successMessage",
                        "Mission REQ-LARGE-HEAVY processed through RuleEvaluator and StrategyContext."
                ));

        Mission processedMission = missionRepository.findById(mission.getId()).orElseThrow();
        assertThat(processedMission.getStatus()).isEqualTo(MissionStatus.ASSIGNED);
        assertThat(processedMission.getMatchedRuleName()).isEqualTo("Heavy Load Rule");
        assertThat(processedMission.getSelectedStrategyName()).isEqualTo("HeavyLoadStrategy");
        assertThat(processedMission.getDecisionSummary()).contains("robotLoad=90");
        assertThat(processedMission.getDecisionSummary()).contains("StrategyContext dispatched HeavyLoadStrategy");
    }

    @Test
    void startExecutionMovesAssignedMissionToInProgressWithoutReprocessing() throws Exception {
        Mission mission = saveMission("REQ-START", MissionStatus.ASSIGNED);
        mission.setAssignedRobotId(100L);
        mission.setAssignedRobotName("Picker Alpha (RB-100)");
        mission.setAssignmentReason("Stored assignment reason.");
        mission.setMatchedRuleName("Stored Rule");
        mission.setSelectedStrategyName("FastRouteStrategy");
        mission.setActionMessage("Stored action message.");
        mission.setDecisionSummary("Stored decision summary.");
        mission.setProcessedAt(LocalDateTime.of(2026, 6, 2, 8, 30));
        mission.setPickupReachedAt(LocalDateTime.of(2026, 6, 2, 8, 45));
        mission.setReturnedAt(LocalDateTime.of(2026, 6, 2, 9, 0));
        missionRepository.save(mission);
        long missionCountBeforeStart = missionRepository.count();

        mockMvc.perform(post("/staff/missions/{id}/start-execution", mission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions"))
                .andExpect(flash().attribute(
                        "successMessage",
                        "Mission REQ-START execution started from Base Station."
                ));

        Mission startedMission = missionRepository.findById(mission.getId()).orElseThrow();
        assertThat(missionRepository.count()).isEqualTo(missionCountBeforeStart);
        assertThat(startedMission.getStatus()).isEqualTo(MissionStatus.IN_PROGRESS);
        assertThat(startedMission.getStatus()).isNotEqualTo(MissionStatus.COMPLETED);
        assertThat(startedMission.getExecutionStep()).isEqualTo(MissionExecutionStep.MOVING_TO_TARGET);
        assertThat(startedMission.getExecutionStartedAt()).isNotNull();
        assertThat(startedMission.getBatteryAtExecutionStart()).isEqualTo(68);
        assertThat(startedMission.getCurrentPositionKey()).isEqualTo("base-station");
        assertThat(startedMission.getPickupReachedAt()).isNull();
        assertThat(startedMission.getReturnedAt()).isNull();
        assertThat(startedMission.getCompletedAt()).isNull();
        assertThat(startedMission.getAssignedRobotId()).isEqualTo(100L);
        assertThat(startedMission.getAssignedRobotName()).isEqualTo("Picker Alpha (RB-100)");
        assertThat(startedMission.getAssignmentReason()).isEqualTo("Stored assignment reason.");
        assertThat(startedMission.getMatchedRuleName()).isEqualTo("Stored Rule");
        assertThat(startedMission.getSelectedStrategyName()).isEqualTo("FastRouteStrategy");
        assertThat(startedMission.getActionMessage()).isEqualTo("Stored action message.");
        assertThat(startedMission.getDecisionSummary()).isEqualTo("Stored decision summary.");
        assertThat(startedMission.getProcessedAt()).isEqualTo(LocalDateTime.of(2026, 6, 2, 8, 30));

        mockMvc.perform(get("/staff/missions"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("REQ-START")))
                .andExpect(content().string(containsString("IN_PROGRESS")))
                .andExpect(content().string(containsString("MOVING_TO_TARGET")))
                .andExpect(content().string(containsString("base-station")))
                .andExpect(content().string(containsString("Robot is still working.")))
                .andExpect(content().string(containsString("Stop")))
                .andExpect(content().string(not(containsString("/staff/missions/" + startedMission.getId() + "/complete"))))
                .andExpect(content().string(not(containsString("Start Execution"))))
                .andExpect(content().string(not(containsString("Process Mission / Assign Robot"))));
    }

    @Test
    void startExecutionRejectsInvalidStatusesDeletedMissionsMissingRobotAndStartedSteps() throws Exception {
        assertStartExecutionRejected(
                saveMission("REQ-START-PENDING", MissionStatus.PENDING),
                "Only ASSIGNED missions can start execution."
        );

        Mission inProgressMission = saveMission("REQ-START-IN-PROGRESS", MissionStatus.IN_PROGRESS);
        inProgressMission.setAssignedRobotName("Picker Alpha (RB-100)");
        missionRepository.save(inProgressMission);
        assertStartExecutionRejected(inProgressMission, "Only ASSIGNED missions can start execution.");

        Mission completedMission = saveMission("REQ-START-DONE", MissionStatus.COMPLETED);
        completedMission.setAssignedRobotName("Picker Alpha (RB-100)");
        missionRepository.save(completedMission);
        assertStartExecutionRejected(completedMission, "Only ASSIGNED missions can start execution.");

        Mission cancelledMission = saveMission("REQ-START-CANCELLED", MissionStatus.CANCELLED);
        cancelledMission.setAssignedRobotName("Picker Alpha (RB-100)");
        missionRepository.save(cancelledMission);
        assertStartExecutionRejected(cancelledMission, "Only ASSIGNED missions can start execution.");

        Mission missingRobotMission = saveMission("REQ-START-NO-ROBOT", MissionStatus.ASSIGNED);
        assertStartExecutionRejected(
                missingRobotMission,
                "Mission must have an assigned robot before execution can start."
        );

        Mission invalidRouteMission = saveMission("REQ-START-BAD-ROUTE", MissionStatus.ASSIGNED);
        invalidRouteMission.setAssignedRobotName("Picker Alpha (RB-100)");
        invalidRouteMission.setLocationCode("A10");
        missionRepository.save(invalidRouteMission);
        assertStartExecutionRejected(
                invalidRouteMission,
                "Mission locationCode must be A1-A9, B1-B9, or C1-C9."
        );

        Mission movingMission = saveStartRejectedAssignedMission("REQ-START-MOVING", MissionExecutionStep.MOVING_TO_TARGET);
        assertStartExecutionRejected(
                movingMission,
                "Only missions with NOT_STARTED execution can start execution."
        );

        Mission pickingMission = saveStartRejectedAssignedMission("REQ-START-PICKING", MissionExecutionStep.PICKING_UP);
        assertStartExecutionRejected(
                pickingMission,
                "Only missions with NOT_STARTED execution can start execution."
        );

        Mission returningMission = saveStartRejectedAssignedMission("REQ-START-RETURNING", MissionExecutionStep.RETURNING_TO_BASE);
        assertStartExecutionRejected(
                returningMission,
                "Only missions with NOT_STARTED execution can start execution."
        );

        Mission returnedMission = saveStartRejectedAssignedMission("REQ-START-RETURNED", MissionExecutionStep.RETURNED_TO_BASE);
        assertStartExecutionRejected(
                returnedMission,
                "Only missions with NOT_STARTED execution can start execution."
        );

        Mission deletedMission = saveMission("REQ-START-DELETED", MissionStatus.ASSIGNED);
        deletedMission.setAssignedRobotName("Picker Alpha (RB-100)");
        deletedMission.setDeletedAt(LocalDateTime.of(2026, 6, 2, 9, 0));
        missionRepository.save(deletedMission);

        mockMvc.perform(post("/staff/missions/{id}/start-execution", deletedMission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions"))
                .andExpect(flash().attribute("errorMessage", "Mission not found: " + deletedMission.getId()));

        Mission unchangedDeletedMission = missionRepository.findById(deletedMission.getId()).orElseThrow();
        assertThat(unchangedDeletedMission.getStatus()).isEqualTo(MissionStatus.ASSIGNED);
        assertThat(unchangedDeletedMission.getExecutionStep()).isEqualTo(MissionExecutionStep.NOT_STARTED);
        assertThat(unchangedDeletedMission.getExecutionStartedAt()).isNull();
    }

    @Test
    void processPendingMissionFallsBackWhenNoZonePolicyExists() throws Exception {
        Mission mission = missionRepository.save(new Mission(
                "REQ-NO-POLICY",
                "Customer No Policy",
                "Medium Cargo",
                "Zone B",
                "B5",
                2,
                MissionStatus.PENDING,
                "No manager policy assigned"
        ));

        mockMvc.perform(post("/staff/missions/{id}/process", mission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions"))
                .andExpect(flash().attribute(
                        "successMessage",
                        "Mission REQ-NO-POLICY processed through RuleEvaluator and StrategyContext."
                ));

        Mission processedMission = missionRepository.findById(mission.getId()).orElseThrow();
        assertThat(processedMission.getStatus()).isEqualTo(MissionStatus.ASSIGNED);
        assertThat(processedMission.getStatus()).isNotEqualTo(MissionStatus.COMPLETED);
        assertThat(processedMission.getMatchedRuleName()).isEqualTo("No Rule Matched");
        assertThat(processedMission.getSelectedStrategyName()).isEqualTo("NoStrategy");
        assertThat(processedMission.getDecisionSummary()).contains("No zone policy assignment was found for Zone B");
        assertThat(processedMission.getDecisionSummary()).contains("robotLoad=60");
    }

    @Test
    void processPendingMissionWithoutAvailableRobotKeepsMissionPending() throws Exception {
        var robots = robotRepository.findAllByOrderByIdAsc();
        robots.forEach(robot -> robot.setStatus("OFFLINE"));
        robotRepository.saveAll(robots);
        Mission mission = missionRepository.save(new Mission(
                "REQ-NO-ROBOT",
                "Customer No Robot",
                "Small Cargo",
                "Zone A",
                "A1",
                1,
                MissionStatus.PENDING,
                "No available robot"
        ));

        mockMvc.perform(post("/staff/missions/{id}/process", mission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions"))
                .andExpect(flash().attribute(
                        "errorMessage",
                        "No available robot found for this mission."
                ));

        Mission pendingMission = missionRepository.findById(mission.getId()).orElseThrow();
        assertThat(pendingMission.getStatus()).isEqualTo(MissionStatus.PENDING);
        assertThat(pendingMission.getStatus()).isNotEqualTo(MissionStatus.COMPLETED);
        assertThat(pendingMission.getAssignedRobotName()).isNull();
        assertThat(pendingMission.getAssignmentReason()).isEqualTo("No available robot found for this mission.");
        assertThat(pendingMission.getDecisionSummary()).contains("Mission remains PENDING");
        assertThat(pendingMission.getSelectedStrategyName()).isNull();
        assertThat(pendingMission.getProcessedAt()).isNull();
        assertThat(pendingMission.getExecutionStep()).isEqualTo(MissionExecutionStep.NOT_STARTED);
        assertThat(pendingMission.getCurrentPositionKey()).isNull();
        assertThat(pendingMission.getExecutionStartedAt()).isNull();
        assertThat(pendingMission.getPickupReachedAt()).isNull();
        assertThat(pendingMission.getReturnedAt()).isNull();
    }

    @Test
    void processPendingMissionCanReuseRobotWithOlderMissionWaitingForConfirmation() throws Exception {
        Robot gamma = findRobotByCode("RB-300");
        gamma.setStatus("IDLE");
        robotRepository.save(gamma);

        Mission waitingConfirmationMission = new Mission(
                "REQ-WAITING-OLD",
                "Customer Old",
                "Medium Cargo",
                "Zone B",
                "B5",
                2,
                MissionStatus.WAITING_CONFIRMATION,
                "Returned mission awaiting Staff confirmation"
        );
        waitingConfirmationMission.setAssignedRobotId(gamma.getId());
        waitingConfirmationMission.setAssignedRobotName(formatRobotName(gamma));
        waitingConfirmationMission.setExecutionStep(MissionExecutionStep.RETURNED_TO_BASE);
        waitingConfirmationMission.setCurrentPositionKey("base-station");
        waitingConfirmationMission.setReturnedAt(LocalDateTime.of(2026, 6, 16, 10, 0));
        Mission savedWaitingMission = missionRepository.save(waitingConfirmationMission);

        Mission nextMission = missionRepository.save(new Mission(
                "REQ-NEXT-BEFORE-COMPLETE",
                "Customer Next",
                "Small Cargo",
                "Zone A",
                "A2",
                2,
                MissionStatus.PENDING,
                "Next mission before Staff closes previous record"
        ));

        mockMvc.perform(post("/staff/missions/{id}/process", nextMission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions"))
                .andExpect(flash().attribute(
                        "successMessage",
                        "Mission REQ-NEXT-BEFORE-COMPLETE processed through RuleEvaluator and StrategyContext."
                ));

        Mission processedNextMission = missionRepository.findById(nextMission.getId()).orElseThrow();
        Mission stillWaitingMission = missionRepository.findById(savedWaitingMission.getId()).orElseThrow();
        assertThat(processedNextMission.getStatus()).isEqualTo(MissionStatus.ASSIGNED);
        assertThat(processedNextMission.getAssignedRobotId()).isEqualTo(gamma.getId());
        assertThat(processedNextMission.getAssignmentReason()).contains("equal active workload");
        assertThat(processedNextMission.getDecisionSummary()).contains("Active workload for selected robot: 0 active mission(s)");
        assertThat(stillWaitingMission.getStatus()).isEqualTo(MissionStatus.WAITING_CONFIRMATION);
        assertThat(stillWaitingMission.getCompletedAt()).isNull();
    }

    @Test
    void completeReturnedMissionMarksCompletedWithoutReprocessing() throws Exception {
        Mission mission = saveMission("REQ-COMPLETE", MissionStatus.IN_PROGRESS);
        mission.setAssignedRobotName("Picker Alpha (RB-100)");
        mission.setMatchedRuleName("Stored Rule");
        mission.setSelectedStrategyName("FastRouteStrategy");
        mission.setProcessedAt(LocalDateTime.of(2026, 5, 30, 9, 0));
        mission.setExecutionStep(MissionExecutionStep.RETURNED_TO_BASE);
        mission.setCurrentPositionKey("base-station");
        mission.setExecutionStartedAt(LocalDateTime.of(2026, 5, 30, 9, 5));
        mission.setReturnedAt(LocalDateTime.of(2026, 5, 30, 9, 15));
        missionRepository.save(mission);

        mockMvc.perform(post("/staff/missions/{id}/complete", mission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions"))
                .andExpect(flash().attribute("successMessage", "Mission completed."))
                .andExpect(flash().attribute("notificationKey", "mission:" + mission.getId() + ":completed"))
                .andExpect(flash().attribute("notificationMessage", "Mission completed."))
                .andExpect(flash().attribute("notificationType", "success"));

        Mission completedMission = missionRepository.findById(mission.getId()).orElseThrow();
        assertThat(completedMission.getStatus()).isEqualTo(MissionStatus.COMPLETED);
        assertThat(completedMission.getCompletedAt()).isNotNull();
        assertThat(completedMission.getProcessedAt()).isEqualTo(LocalDateTime.of(2026, 5, 30, 9, 0));
        assertThat(completedMission.getSelectedStrategyName()).isEqualTo("FastRouteStrategy");
        assertThat(completedMission.getExecutionStep()).isEqualTo(MissionExecutionStep.RETURNED_TO_BASE);
        assertThat(completedMission.getReturnedAt()).isEqualTo(LocalDateTime.of(2026, 5, 30, 9, 15));

        mockMvc.perform(get("/staff/missions").param("filter", "completed"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("REQ-COMPLETE")))
                .andExpect(content().string(containsString("COMPLETED")))
                .andExpect(content().string(containsString("Completed history")))
                .andExpect(content().string(not(containsString("Process Mission / Assign Robot"))))
                .andExpect(content().string(not(containsString(">Delete<"))));
    }

    @Test
    void completeMissionGetFallbackCompletesReturnedMissionWithoutWhitelabel() throws Exception {
        Mission mission = saveMission("REQ-COMPLETE-GET", MissionStatus.WAITING_CONFIRMATION);
        mission.setExecutionStep(MissionExecutionStep.RETURNED_TO_BASE);
        mission.setCurrentPositionKey("base-station");
        mission.setExecutionStartedAt(LocalDateTime.of(2026, 5, 30, 10, 0));
        mission.setReturnedAt(LocalDateTime.of(2026, 5, 30, 10, 10));
        missionRepository.save(mission);

        mockMvc.perform(get("/staff/missions/{id}/complete", mission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions"))
                .andExpect(flash().attribute("successMessage", "Mission completed."));

        Mission completedMission = missionRepository.findById(mission.getId()).orElseThrow();
        assertThat(completedMission.getStatus()).isEqualTo(MissionStatus.COMPLETED);
        assertThat(completedMission.getCompletedAt()).isNotNull();
    }

    @Test
    void completeMissionClosesWaitingConfirmationRecordAfterRobotReturned() throws Exception {
        Robot gamma = findRobotByCode("RB-300");
        gamma.setStatus("IDLE");
        robotRepository.save(gamma);

        Mission mission = saveMission("REQ-WAITING-COMPLETE", MissionStatus.WAITING_CONFIRMATION);
        mission.setAssignedRobotId(gamma.getId());
        mission.setAssignedRobotName(formatRobotName(gamma));
        mission.setExecutionStep(MissionExecutionStep.RETURNED_TO_BASE);
        mission.setCurrentPositionKey("base-station");
        mission.setExecutionStartedAt(LocalDateTime.now().minusMinutes(8));
        mission.setReturnedAt(LocalDateTime.now().minusMinutes(1));
        missionRepository.save(mission);

        mockMvc.perform(post("/staff/missions/{id}/complete", mission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions"))
                .andExpect(flash().attribute("successMessage", "Mission completed."));

        Mission completedMission = missionRepository.findById(mission.getId()).orElseThrow();
        Robot stillAvailableRobot = findRobotByCode("RB-300");
        assertThat(completedMission.getStatus()).isEqualTo(MissionStatus.COMPLETED);
        assertThat(completedMission.getCompletedAt()).isNotNull();
        assertThat(completedMission.getReturnedAt()).isNotNull();
        assertThat(stillAvailableRobot.getStatus()).isEqualTo("IDLE");
        assertThat(stillAvailableRobot.getCharging()).isFalse();
    }

    @Test
    void completingReturnedCriticalBatteryMissionReassignsQueuedTasksAndStartsCharging() throws Exception {
        Robot alpha = findRobotByCode("RB-100");
        Robot beta = findRobotByCode("RB-200");
        Robot gamma = findRobotByCode("RB-300");
        alpha.setBattery(5);
        robotRepository.save(alpha);

        Mission currentMission = saveMission("REQ-CRITICAL-COMPLETE", MissionStatus.IN_PROGRESS);
        currentMission.setAssignedRobotId(alpha.getId());
        currentMission.setAssignedRobotName(formatRobotName(alpha));
        currentMission.setExecutionStep(MissionExecutionStep.MOVING_TO_TARGET);
        currentMission.setCurrentPositionKey("base-station");
        currentMission.setExecutionStartedAt(LocalDateTime.now().minusSeconds(900));
        missionRepository.save(currentMission);

        Mission highPriorityQueuedMission = saveQueuedMission(
                alpha,
                "REQ-REASSIGN-P1",
                1,
                MissionStatus.ASSIGNED,
                LocalDateTime.of(2026, 6, 3, 8, 0)
        );
        Mission mediumPriorityQueuedMission = saveQueuedMission(
                alpha,
                "REQ-REASSIGN-P2",
                2,
                MissionStatus.ASSIGNED,
                LocalDateTime.of(2026, 6, 3, 8, 5)
        );
        Mission completedMission = saveQueuedMission(
                alpha,
                "REQ-NOT-REASSIGNED-DONE",
                1,
                MissionStatus.COMPLETED,
                LocalDateTime.of(2026, 6, 3, 8, 10)
        );
        Mission cancelledMission = saveQueuedMission(
                alpha,
                "REQ-NOT-REASSIGNED-CANCEL",
                1,
                MissionStatus.CANCELLED,
                LocalDateTime.of(2026, 6, 3, 8, 15)
        );
        Mission deletedMission = saveQueuedMission(
                alpha,
                "REQ-NOT-REASSIGNED-DELETED",
                1,
                MissionStatus.ASSIGNED,
                LocalDateTime.of(2026, 6, 3, 8, 20)
        );
        deletedMission.setDeletedAt(LocalDateTime.of(2026, 6, 3, 8, 30));
        missionRepository.save(deletedMission);

        mockMvc.perform(post("/staff/missions/{id}/complete", currentMission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions"))
                .andExpect(flash().attribute(
                        "successMessage",
                        containsString("Mission completed.")
                ))
                .andExpect(flash().attribute(
                        "successMessage",
                        containsString("Remaining tasks were reassigned and robot sent to Charging Station.")
                ))
                .andExpect(flash().attribute("successMessage", containsString("Reassigned queued missions: 2.")))
                .andExpect(flash().attribute("notificationMessage", "Mission completed."))
                .andExpect(flash().attribute(
                        "chargingStartedNotificationKey",
                        "mission:" + currentMission.getId() + ":charging-started"
                ))
                .andExpect(flash().attribute("chargingStartedNotificationMessage", "Robot is charging at station."))
                .andExpect(flash().attribute("chargingStartedNotificationType", "info"))
                .andExpect(flash().attribute(
                        "taskReassignedNotificationKey",
                        "mission:" + currentMission.getId() + ":task-reassigned"
                ))
                .andExpect(flash().attribute("taskReassignedNotificationMessage", "Remaining tasks were reassigned."))
                .andExpect(flash().attribute("taskReassignedNotificationType", "warning"));

        Mission completedCurrentMission = missionRepository.findById(currentMission.getId()).orElseThrow();
        assertThat(completedCurrentMission.getStatus()).isEqualTo(MissionStatus.COMPLETED);
        assertThat(completedCurrentMission.getCompletedAt()).isNotNull();

        Robot chargingAlpha = findRobotByCode("RB-100");
        assertThat(chargingAlpha.getStatus()).isEqualTo("CHARGING");
        assertThat(chargingAlpha.getCharging()).isTrue();
        assertThat(chargingAlpha.getChargingRequired()).isTrue();
        assertThat(chargingAlpha.getChargingStartedAt()).isNotNull();
        assertThat(chargingAlpha.getChargingCompletedAt()).isNull();
        assertThat(chargingAlpha.getBatteryBeforeCharging()).isLessThanOrEqualTo(5);
        assertThat(chargingAlpha.getBattery()).isLessThanOrEqualTo(5);

        Mission reassignedHighPriorityMission = missionRepository.findById(highPriorityQueuedMission.getId()).orElseThrow();
        Mission reassignedMediumPriorityMission = missionRepository.findById(mediumPriorityQueuedMission.getId()).orElseThrow();
        assertThat(reassignedHighPriorityMission.getAssignedRobotId()).isEqualTo(gamma.getId());
        assertThat(reassignedMediumPriorityMission.getAssignedRobotId()).isEqualTo(beta.getId());
        assertThat(reassignedHighPriorityMission.getStatus()).isEqualTo(MissionStatus.ASSIGNED);
        assertThat(reassignedMediumPriorityMission.getStatus()).isEqualTo(MissionStatus.ASSIGNED);
        assertThat(reassignedHighPriorityMission.getAssignmentReason()).contains("Reassigned from Picker Alpha (RB-100)");
        assertThat(reassignedMediumPriorityMission.getAssignmentReason()).contains("charging is required");

        assertThat(missionRepository.findById(completedMission.getId()).orElseThrow().getAssignedRobotId()).isEqualTo(alpha.getId());
        assertThat(missionRepository.findById(cancelledMission.getId()).orElseThrow().getAssignedRobotId()).isEqualTo(alpha.getId());
        assertThat(missionRepository.findById(deletedMission.getId()).orElseThrow().getAssignedRobotId()).isEqualTo(alpha.getId());

        mockMvc.perform(get("/manager/robot-tasks"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("CHARGING")))
                .andExpect(content().string(containsString("REQ-REASSIGN-P1")))
                .andExpect(content().string(containsString("REQ-REASSIGN-P2")));
    }

    @Test
    void completingMissionWithRuleSelectedChargingStrategyStartsChargingEvenAboveCriticalBattery() throws Exception {
        Robot alpha = findRobotByCode("RB-100");
        alpha.setBattery(10);
        robotRepository.save(alpha);

        Mission currentMission = saveMission("REQ-CHARGING-RULE-COMPLETE", MissionStatus.IN_PROGRESS);
        currentMission.setAssignedRobotId(alpha.getId());
        currentMission.setAssignedRobotName(formatRobotName(alpha));
        currentMission.setSelectedStrategyName("ChargingStrategy");
        currentMission.setExecutionStep(MissionExecutionStep.RETURNED_TO_BASE);
        currentMission.setCurrentPositionKey("base-station");
        currentMission.setExecutionStartedAt(LocalDateTime.now().minusSeconds(180));
        currentMission.setReturnedAt(LocalDateTime.now().minusSeconds(5));
        missionRepository.save(currentMission);

        mockMvc.perform(post("/staff/missions/{id}/complete", currentMission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions"))
                .andExpect(flash().attribute(
                        "successMessage",
                        containsString("Remaining tasks were reassigned and robot sent to Charging Station.")
                ));

        Mission completedMission = missionRepository.findById(currentMission.getId()).orElseThrow();
        assertThat(completedMission.getStatus()).isEqualTo(MissionStatus.COMPLETED);
        assertThat(completedMission.getCompletedAt()).isNotNull();
        assertThat(completedMission.getSelectedStrategyName()).isEqualTo("ChargingStrategy");

        Robot chargingAlpha = findRobotByCode("RB-100");
        assertThat(chargingAlpha.getStatus()).isEqualTo("CHARGING");
        assertThat(chargingAlpha.getCharging()).isTrue();
        assertThat(chargingAlpha.getChargingRequired()).isTrue();
        assertThat(chargingAlpha.getChargingStartedAt()).isNotNull();
        assertThat(chargingAlpha.getBatteryBeforeCharging()).isLessThanOrEqualTo(10);
        assertThat(chargingAlpha.getBattery()).isLessThanOrEqualTo(10);
    }

    @Test
    void completeMissionRejectsActiveMissionsBeforeRobotReturnsToBase() throws Exception {
        Mission assignedMission = saveMission("REQ-COMPLETE-ASSIGNED", MissionStatus.ASSIGNED);
        assignedMission.setAssignedRobotName("Picker Alpha (RB-100)");
        missionRepository.save(assignedMission);
        assertCompleteRejected(assignedMission, MissionService.COMPLETION_REQUIRES_RETURN_MESSAGE);

        assertCompleteRejected(
                saveInProgressMissionAtStep("REQ-COMPLETE-MOVING", MissionExecutionStep.MOVING_TO_TARGET),
                MissionService.COMPLETION_REQUIRES_RETURN_MESSAGE
        );
        assertCompleteRejected(
                saveInProgressMissionAtStep("REQ-COMPLETE-PICKUP", MissionExecutionStep.PICKING_UP),
                MissionService.COMPLETION_REQUIRES_RETURN_MESSAGE
        );
        assertCompleteRejected(
                saveInProgressMissionAtStep("REQ-COMPLETE-RETURNING", MissionExecutionStep.RETURNING_TO_BASE),
                MissionService.COMPLETION_REQUIRES_RETURN_MESSAGE
        );
    }

    @Test
    void completeMissionRejectsPendingCompletedAndCancelledStatuses() throws Exception {
        assertCompleteRejected(
                saveMission("REQ-COMPLETE-PENDING", MissionStatus.PENDING),
                MissionService.COMPLETION_REQUIRES_RETURN_MESSAGE
        );
        assertCompleteRejected(
                saveMission("REQ-COMPLETE-DONE", MissionStatus.COMPLETED),
                MissionService.MISSION_ALREADY_COMPLETED_MESSAGE
        );
        assertCompleteRejected(
                saveMission("REQ-COMPLETE-CANCELLED", MissionStatus.CANCELLED),
                MissionService.CANCELLED_MISSION_CANNOT_BE_COMPLETED_MESSAGE
        );
    }

    @Test
    void completeMissingMissionRedirectsWithFriendlyError() throws Exception {
        mockMvc.perform(post("/staff/missions/999999/complete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions"))
                .andExpect(flash().attribute("errorMessage", MissionService.MISSION_NOT_FOUND_MESSAGE));
    }

    @Test
    void completeMissionGetFallbackRejectsEarlyMissionWithoutWhitelabel() throws Exception {
        Mission mission = saveInProgressMissionAtStep("REQ-COMPLETE-GET-EARLY", MissionExecutionStep.MOVING_TO_TARGET);

        mockMvc.perform(get("/staff/missions/{id}/complete", mission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions"))
                .andExpect(flash().attribute("errorMessage", MissionService.COMPLETION_REQUIRES_RETURN_MESSAGE));

        Mission unchangedMission = missionRepository.findById(mission.getId()).orElseThrow();
        assertThat(unchangedMission.getCompletedAt()).isNull();
    }

    @Test
    void stopMissionCancelsActiveMissionAndShowsDeleteAction() throws Exception {
        Mission mission = saveMission("REQ-STOP", MissionStatus.PENDING);

        mockMvc.perform(post("/staff/missions/{id}/stop", mission.getId())
                        .param("cancellationReasonCode", "WRONG_LOCATION")
                        .param("cancellationNote", "Customer corrected the pickup shelf.")
                        .principal(new TestingAuthenticationToken("Nova001", null)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions?filter=cancelled"))
                .andExpect(flash().attribute("successMessage", "Mission REQ-STOP stopped as CANCELLED."))
                .andExpect(flash().attribute("notificationKey", "mission:" + mission.getId() + ":stopped"))
                .andExpect(flash().attribute("notificationMessage", "Mission stopped."))
                .andExpect(flash().attribute("notificationType", "warning"));

        Mission cancelledMission = missionRepository.findById(mission.getId()).orElseThrow();
        assertThat(cancelledMission.getStatus()).isEqualTo(MissionStatus.CANCELLED);
        assertThat(cancelledMission.getCancelledAt()).isNotNull();
        assertThat(cancelledMission.getCancellationReasonCode()).isEqualTo("WRONG_LOCATION");
        assertThat(cancelledMission.getCancellationReasonDisplay()).isEqualTo("Wrong location");
        assertThat(cancelledMission.getCancellationNote()).isEqualTo("Customer corrected the pickup shelf.");
        assertThat(cancelledMission.getCancelledBy()).isEqualTo("Nova001");
        assertThat(cancelledMission.getExecutionStep()).isEqualTo(MissionExecutionStep.NOT_STARTED);
        assertThat(cancelledMission.getExecutionStartedAt()).isNull();
        assertThat(cancelledMission.getPickupReachedAt()).isNull();
        assertThat(cancelledMission.getReturnedAt()).isNull();

        mockMvc.perform(get("/staff/missions").param("filter", "cancelled"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("REQ-STOP")))
                .andExpect(content().string(containsString("CANCELLED")))
                .andExpect(content().string(containsString("Cancelled / Stopped")))
                .andExpect(content().string(containsString("Wrong location")))
                .andExpect(content().string(containsString("Customer corrected the pickup shelf.")))
                .andExpect(content().string(containsString("Nova001")))
                .andExpect(content().string(containsString("Delete")))
                .andExpect(content().string(not(containsString("Process Mission / Assign Robot"))))
                .andExpect(content().string(not(containsString("task_alt"))));
    }

    @Test
    void stoppingCriticalBatteryMissionReassignsQueuedTasksAndEmitsChargingNotifications() throws Exception {
        Robot alpha = findRobotByCode("RB-100");
        Robot gamma = findRobotByCode("RB-300");
        alpha.setBattery(5);
        robotRepository.save(alpha);

        Mission currentMission = saveMission("REQ-CRITICAL-STOP", MissionStatus.IN_PROGRESS);
        currentMission.setAssignedRobotId(alpha.getId());
        currentMission.setAssignedRobotName(formatRobotName(alpha));
        missionRepository.save(currentMission);

        Mission queuedMission = saveQueuedMission(
                alpha,
                "REQ-STOP-REASSIGN",
                1,
                MissionStatus.ASSIGNED,
                LocalDateTime.of(2026, 6, 3, 9, 0)
        );

        mockMvc.perform(post("/staff/missions/{id}/stop", currentMission.getId())
                        .param("cancellationReasonCode", "ROBOT_ISSUE")
                        .param("cancellationNote", "Robot battery reached critical level."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions?filter=cancelled"))
                .andExpect(flash().attribute(
                        "successMessage",
                        containsString("Critical battery robot sent to Charging Station.")
                ))
                .andExpect(flash().attribute("notificationMessage", "Mission stopped."))
                .andExpect(flash().attribute(
                        "chargingStartedNotificationKey",
                        "mission:" + currentMission.getId() + ":charging-started"
                ))
                .andExpect(flash().attribute("chargingStartedNotificationMessage", "Robot is charging at station."))
                .andExpect(flash().attribute("chargingStartedNotificationType", "info"))
                .andExpect(flash().attribute(
                        "taskReassignedNotificationKey",
                        "mission:" + currentMission.getId() + ":task-reassigned"
                ))
                .andExpect(flash().attribute("taskReassignedNotificationMessage", "Remaining tasks were reassigned."))
                .andExpect(flash().attribute("taskReassignedNotificationType", "warning"));

        Robot chargingAlpha = findRobotByCode("RB-100");
        assertThat(chargingAlpha.getStatus()).isEqualTo("CHARGING");
        assertThat(chargingAlpha.getCharging()).isTrue();

        Mission reassignedMission = missionRepository.findById(queuedMission.getId()).orElseThrow();
        assertThat(reassignedMission.getStatus()).isEqualTo(MissionStatus.ASSIGNED);
        assertThat(reassignedMission.getAssignedRobotId()).isEqualTo(gamma.getId());
        assertThat(reassignedMission.getAssignmentReason()).contains("Reassigned from Picker Alpha (RB-100)");
    }

    @Test
    void stopMissionRejectsMissingCancellationReason() throws Exception {
        Mission mission = saveMission("REQ-STOP-NO-REASON", MissionStatus.ASSIGNED);

        mockMvc.perform(post("/staff/missions/{id}/stop", mission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions?filter=cancelled"))
                .andExpect(flash().attribute("errorMessage", MissionService.CANCELLATION_REASON_REQUIRED_MESSAGE));

        Mission unchangedMission = missionRepository.findById(mission.getId()).orElseThrow();
        assertThat(unchangedMission.getStatus()).isEqualTo(MissionStatus.ASSIGNED);
        assertThat(unchangedMission.getCancelledAt()).isNull();
        assertThat(unchangedMission.getCancellationReasonCode()).isNull();
    }

    @Test
    void stopMissionRejectsCompletedAndAlreadyCancelledMissions() throws Exception {
        assertStopRejected(saveMission("REQ-STOP-DONE", MissionStatus.COMPLETED));
        assertStopRejected(saveMission("REQ-STOP-CANCELLED", MissionStatus.CANCELLED));
    }

    @Test
    void deleteMissionIsAllowedOnlyAfterStopAndSoftDeletesRecord() throws Exception {
        Mission mission = saveMission("REQ-DELETE", MissionStatus.CANCELLED);
        mission.setCancelledAt(LocalDateTime.of(2026, 5, 30, 10, 0));
        missionRepository.save(mission);

        mockMvc.perform(post("/staff/missions/{id}/delete", mission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions?filter=cancelled"))
                .andExpect(flash().attribute(
                        "successMessage",
                        "Cancelled mission REQ-DELETE deleted from the main mission lists."
                ));

        Mission deletedMission = missionRepository.findById(mission.getId()).orElseThrow();
        assertThat(deletedMission.getStatus()).isEqualTo(MissionStatus.CANCELLED);
        assertThat(deletedMission.getDeletedAt()).isNotNull();
        assertThat(deletedMission.getExecutionStep()).isEqualTo(MissionExecutionStep.NOT_STARTED);

        mockMvc.perform(get("/staff/missions").param("filter", "all"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("REQ-DELETE"))));

        mockMvc.perform(get("/staff/missions").param("filter", "cancelled"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("REQ-DELETE"))));
    }

    @Test
    void deleteMissionRejectsPendingAssignedInProgressAndCompletedStatuses() throws Exception {
        assertDeleteRejected(saveMission("REQ-DELETE-PENDING", MissionStatus.PENDING));
        assertDeleteRejected(saveMission("REQ-DELETE-ASSIGNED", MissionStatus.ASSIGNED));
        assertDeleteRejected(saveMission("REQ-DELETE-IN-PROGRESS", MissionStatus.IN_PROGRESS));
        assertDeleteRejected(saveMission("REQ-DELETE-COMPLETED", MissionStatus.COMPLETED));
    }

    @Test
    void processMissionRejectsCompletedCancelledAndDeletedMissions() throws Exception {
        Mission completedMission = saveMission("REQ-PROCESS-DONE", MissionStatus.COMPLETED);
        Mission cancelledMission = saveMission("REQ-PROCESS-CANCELLED", MissionStatus.CANCELLED);
        Mission deletedMission = saveMission("REQ-PROCESS-DELETED", MissionStatus.PENDING);
        deletedMission.setDeletedAt(LocalDateTime.of(2026, 5, 30, 11, 0));
        missionRepository.save(deletedMission);

        mockMvc.perform(post("/staff/missions/{id}/process", completedMission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions"))
                .andExpect(flash().attribute("errorMessage", "Only PENDING missions can be processed."));

        mockMvc.perform(post("/staff/missions/{id}/process", cancelledMission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions"))
                .andExpect(flash().attribute("errorMessage", "Only PENDING missions can be processed."));

        mockMvc.perform(post("/staff/missions/{id}/process", deletedMission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions"))
                .andExpect(flash().attribute("errorMessage", "Mission not found: " + deletedMission.getId()));
    }

    @Test
    void processMissingMissionRedirectsWithError() throws Exception {
        mockMvc.perform(post("/staff/missions/999999/process"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions"))
                .andExpect(flash().attribute("errorMessage", "Mission not found: 999999"));
    }

    private Mission saveMission(String requestCode, MissionStatus status) {
        return missionRepository.save(new Mission(
                requestCode,
                "Customer " + requestCode,
                "Small Cargo",
                "Zone A",
                "A1",
                2,
                status,
                "Lifecycle test mission"
        ));
    }

    private Mission saveStartRejectedAssignedMission(String requestCode, MissionExecutionStep executionStep) {
        Mission mission = saveMission(requestCode, MissionStatus.ASSIGNED);
        mission.setAssignedRobotName("Picker Alpha (RB-100)");
        mission.setExecutionStep(executionStep);
        mission.setCurrentPositionKey("base-station");
        return missionRepository.save(mission);
    }

    private Mission saveInProgressMissionAtStep(String requestCode, MissionExecutionStep executionStep) {
        Robot alpha = findRobotByCode("RB-100");
        Mission mission = saveMission(requestCode, MissionStatus.IN_PROGRESS);
        mission.setAssignedRobotId(alpha.getId());
        mission.setAssignedRobotName(formatRobotName(alpha));
        mission.setExecutionStep(executionStep);
        mission.setCurrentPositionKey("base-station");
        mission.setExecutionStartedAt(LocalDateTime.now().minusSeconds(1));
        return missionRepository.save(mission);
    }

    private Mission saveQueuedMission(Robot robot,
                                      String requestCode,
                                      Integer priority,
                                      MissionStatus status,
                                      LocalDateTime createdAt) {
        Mission mission = new Mission(
                requestCode,
                "Customer " + requestCode,
                "Small Cargo",
                "Zone A",
                "A1",
                priority,
                status,
                "Queued mission for reassignment"
        );
        mission.setAssignedRobotId(robot.getId());
        mission.setAssignedRobotName(formatRobotName(robot));
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

    private String formatRobotName(Robot robot) {
        if (robot.getCode() == null || robot.getCode().isBlank()) {
            return robot.getName();
        }
        return robot.getName() + " (" + robot.getCode() + ")";
    }

    private void assertCompleteRejected(Mission mission, String expectedMessage) throws Exception {
        mockMvc.perform(post("/staff/missions/{id}/complete", mission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions"))
                .andExpect(flash().attribute("errorMessage", expectedMessage));

        Mission unchangedMission = missionRepository.findById(mission.getId()).orElseThrow();
        assertThat(unchangedMission.getCompletedAt()).isNull();
    }

    private void assertStopRejected(Mission mission) throws Exception {
        mockMvc.perform(post("/staff/missions/{id}/stop", mission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions?filter=cancelled"))
                .andExpect(flash().attribute(
                        "errorMessage",
                        "Only PENDING, ASSIGNED, IN_PROGRESS, or WAITING_CONFIRMATION missions can be stopped."
                ));

        Mission unchangedMission = missionRepository.findById(mission.getId()).orElseThrow();
        assertThat(unchangedMission.getCancelledAt()).isNull();
    }

    private void assertDeleteRejected(Mission mission) throws Exception {
        mockMvc.perform(post("/staff/missions/{id}/delete", mission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions?filter=cancelled"))
                .andExpect(flash().attribute("errorMessage", "Only CANCELLED missions can be deleted."));

        Mission unchangedMission = missionRepository.findById(mission.getId()).orElseThrow();
        assertThat(unchangedMission.getDeletedAt()).isNull();
    }

    private void assertStartExecutionRejected(Mission mission, String expectedMessage) throws Exception {
        mockMvc.perform(post("/staff/missions/{id}/start-execution", mission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/missions"))
                .andExpect(flash().attribute("errorMessage", expectedMessage));

        Mission unchangedMission = missionRepository.findById(mission.getId()).orElseThrow();
        assertThat(unchangedMission.getStatus()).isEqualTo(mission.getStatus());
        assertThat(unchangedMission.getExecutionStartedAt()).isNull();
        assertThat(unchangedMission.getCompletedAt()).isNull();
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
