package com.warehouse.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class SystemFlowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void systemFlowRouteLoadsWithExpectedModelData() throws Exception {
        mockMvc.perform(get("/system-flow"))
                .andExpect(status().isOk())
                .andExpect(view().name("system-flow"))
                .andExpect(model().attributeExists(
                        "flowSteps",
                        "robotConditions",
                        "interpreterComponents",
                        "activeRules",
                        "activeStrategies",
                        "recentExecutionHistory"
                ));
    }
}
