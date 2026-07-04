package com.warehouse.controller;

import com.warehouse.dto.RobotFleetStatusDto;
import com.warehouse.model.Robot;
import com.warehouse.service.RobotFleetStatusService;
import com.warehouse.service.RobotService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RobotManagementController {

    private final RobotService robotService;
    private final RobotFleetStatusService robotFleetStatusService;

    public RobotManagementController(RobotService robotService,
                                     RobotFleetStatusService robotFleetStatusService) {
        this.robotService = robotService;
        this.robotFleetStatusService = robotFleetStatusService;
    }

    @GetMapping("/robots")
    public String showRobots(Model model) {
        List<Robot> robots = robotService.getRobots();
        List<RobotFleetStatusDto> robotFleet = robotFleetStatusService.getRobotFleetStatus();
        model.addAttribute("robots", robots);
        model.addAttribute("robotFleet", robotFleet);
        return "robots";
    }
}
