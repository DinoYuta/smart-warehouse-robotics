package com.warehouse.controller;

import com.warehouse.dto.SimulationRequestDto;
import com.warehouse.simulator.SimulationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @GetMapping("/simulation")
    public String showSimulation(Model model) {
        model.addAttribute("simulationRequest", new SimulationRequestDto(15, true, 45, 8.2, 1));
        return "simulation";
    }

    @PostMapping("/simulation")
    public String runSimulation(@ModelAttribute("simulationRequest") SimulationRequestDto simulationRequest,
                                Model model) {
        model.addAttribute("result", simulationService.simulate(simulationRequest));
        return "simulation";
    }
}
