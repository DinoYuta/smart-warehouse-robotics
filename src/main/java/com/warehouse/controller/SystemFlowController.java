package com.warehouse.controller;

import com.warehouse.service.DashboardService;
import com.warehouse.service.RuleService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SystemFlowController {

    private final RuleService ruleService;
    private final DashboardService dashboardService;

    public SystemFlowController(RuleService ruleService, DashboardService dashboardService) {
        this.ruleService = ruleService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/system-flow")
    public String showSystemFlow(Model model) {
        model.addAttribute("flowSteps", List.of(
                "Robot Condition Input",
                "Interpreter Pattern",
                "Rule Matching",
                "Strategy Pattern",
                "Robot Action",
                "Execution History"
        ));
        model.addAttribute("robotConditions", List.of(
                "battery",
                "obstacleDetected",
                "robotLoad",
                "distance",
                "priority"
        ));
        model.addAttribute("interpreterComponents", List.of(
                "RuleParser",
                "Expression",
                "BatteryExpression",
                "ObstacleExpression",
                "RobotLoadExpression",
                "DistanceExpression",
                "PriorityExpression",
                "AndExpression",
                "OrExpression"
        ));
        model.addAttribute("activeRules", ruleService.loadActiveRules());
        model.addAttribute("activeStrategies", dashboardService.getActiveStrategies());
        model.addAttribute("recentExecutionHistory", dashboardService.getRecentExecutionHistory());
        return "system-flow";
    }
}
