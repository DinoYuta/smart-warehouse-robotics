package com.warehouse.controller;

import com.warehouse.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping({"/", "/dashboard"})
    public String showDashboard(Model model) {
        model.addAttribute("summary", dashboardService.getSummary());
        model.addAttribute("robots", dashboardService.getRobots());
        model.addAttribute("activeRules", dashboardService.getActiveRules());
        model.addAttribute("activeStrategies", dashboardService.getActiveStrategies());
        model.addAttribute("ruleExecutions", dashboardService.getRuleExecutions());
        model.addAttribute("recentExecutionHistory", dashboardService.getRecentExecutionHistory());
        return "dashboard";
    }
}
