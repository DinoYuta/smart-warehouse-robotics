package com.warehouse.controller;

import com.warehouse.service.ManagerRobotTaskBoardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ManagerRobotTaskBoardController {

    private final ManagerRobotTaskBoardService taskBoardService;

    public ManagerRobotTaskBoardController(ManagerRobotTaskBoardService taskBoardService) {
        this.taskBoardService = taskBoardService;
    }

    @GetMapping("/manager/robot-tasks")
    public String showRobotTaskBoard(Model model) {
        model.addAttribute("taskBoard", taskBoardService.getRobotTaskBoard());
        return "manager-robot-tasks";
    }
}
