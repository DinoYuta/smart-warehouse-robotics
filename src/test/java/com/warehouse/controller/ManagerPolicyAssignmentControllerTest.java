package com.warehouse.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.warehouse.model.Rule;
import com.warehouse.model.ZonePolicyAssignment;
import com.warehouse.repository.RuleRepository;
import com.warehouse.repository.ZonePolicyAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ManagerPolicyAssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private ZonePolicyAssignmentRepository assignmentRepository;

    @BeforeEach
    void cleanAssignments() {
        assignmentRepository.deleteAll();
    }

    @Test
    void policyAssignmentRouteLoadsWithZonesAndActiveRules() throws Exception {
        mockMvc.perform(get("/manager/policy-assignment"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager-policy-assignment"))
                .andExpect(model().attributeExists(
                        "zoneCargoMappings",
                        "activeRules",
                        "assignmentsByZone",
                        "assignedRulesByZone"
                ))
                .andExpect(content().string(containsString("Manager Rule / Policy Assignment")))
                .andExpect(content().string(containsString("Zone A")))
                .andExpect(content().string(containsString("Zone B")))
                .andExpect(content().string(containsString("Zone C")))
                .andExpect(content().string(containsString("Small Cargo")))
                .andExpect(content().string(containsString("Medium Cargo")))
                .andExpect(content().string(containsString("Large Cargo")))
                .andExpect(content().string(containsString("Active Admin Rules Available as Policies")))
                .andExpect(content().string(containsString("Critical Battery With Obstacle Rule")))
                .andExpect(content().string(containsString("existing active Admin-created rules as operational policies")));
    }

    @Test
    void seededBatteryRulesUseEnergySavingForLowBatteryAndChargingForCriticalBattery() {
        Rule lowBatteryRule = ruleRepository.findByRuleName("Low Battery Rule").orElseThrow();
        Rule criticalBatteryRule = ruleRepository.findByRuleName("Critical Battery With Obstacle Rule").orElseThrow();

        assertThat(lowBatteryRule.getConditionExpression()).isEqualTo("battery < 20");
        assertThat(lowBatteryRule.getStrategyName()).isEqualTo("EnergySavingStrategy");
        assertThat(criticalBatteryRule.getConditionExpression()).isEqualTo("battery < 10 AND obstacleDetected == TRUE");
        assertThat(criticalBatteryRule.getStrategyName()).isEqualTo("ChargingStrategy");
    }

    @Test
    void policyAssignmentSubmitSavesAndUpdatesZoneAssignment() throws Exception {
        Rule lowBatteryRule = ruleRepository.findByRuleName("Low Battery Rule").orElseThrow();
        Rule fastRouteRule = ruleRepository.findByRuleName("Urgent Task Fast Route Rule").orElseThrow();

        mockMvc.perform(post("/manager/policy-assignment")
                        .param("zone", "Zone A")
                        .param("cargoType", "Small Cargo")
                        .param("ruleId", lowBatteryRule.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/policy-assignment"))
                .andExpect(flash().attribute("successMessage", "Zone policy assignment saved."));

        ZonePolicyAssignment savedAssignment = assignmentRepository.findByZone("Zone A").orElseThrow();
        assertThat(savedAssignment.getCargoType()).isEqualTo("Small Cargo");
        assertThat(savedAssignment.getRuleId()).isEqualTo(lowBatteryRule.getId());

        mockMvc.perform(post("/manager/policy-assignment")
                        .param("zone", "Zone A")
                        .param("cargoType", "Small Cargo")
                        .param("ruleId", fastRouteRule.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/policy-assignment"))
                .andExpect(flash().attribute("successMessage", "Zone policy assignment saved."));

        ZonePolicyAssignment updatedAssignment = assignmentRepository.findByZone("Zone A").orElseThrow();
        assertThat(assignmentRepository.count()).isEqualTo(1);
        assertThat(updatedAssignment.getId()).isEqualTo(savedAssignment.getId());
        assertThat(updatedAssignment.getCargoType()).isEqualTo("Small Cargo");
        assertThat(updatedAssignment.getRuleId()).isEqualTo(fastRouteRule.getId());
    }

    @Test
    void policyAssignmentSubmitRejectsCargoTypeMismatch() throws Exception {
        Rule lowBatteryRule = ruleRepository.findByRuleName("Low Battery Rule").orElseThrow();

        mockMvc.perform(post("/manager/policy-assignment")
                        .param("zone", "Zone A")
                        .param("cargoType", "Medium Cargo")
                        .param("ruleId", lowBatteryRule.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/policy-assignment"))
                .andExpect(flash().attribute("errorMessage", "Zone A must use Small Cargo."));

        assertThat(assignmentRepository.count()).isZero();
    }

    @Test
    void policyAssignmentSubmitRejectsInactiveRule() throws Exception {
        Rule inactiveRule = ruleRepository.save(new Rule(
                "Inactive Assignment Test Rule",
                "battery < 10",
                "ChargingStrategy",
                false,
                99
        ));

        mockMvc.perform(post("/manager/policy-assignment")
                        .param("zone", "Zone A")
                        .param("cargoType", "Small Cargo")
                        .param("ruleId", inactiveRule.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/policy-assignment"))
                .andExpect(flash().attribute("errorMessage", "Selected rule must be active."));

        assertThat(assignmentRepository.count()).isZero();
    }
}
