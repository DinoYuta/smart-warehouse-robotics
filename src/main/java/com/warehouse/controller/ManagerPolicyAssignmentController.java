package com.warehouse.controller;

import com.warehouse.dto.ZonePolicyAssignmentDto;
import com.warehouse.service.ZonePolicyAssignmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ManagerPolicyAssignmentController {

    private final ZonePolicyAssignmentService assignmentService;

    public ManagerPolicyAssignmentController(ZonePolicyAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping("/manager/policy-assignment")
    public String showPolicyAssignment(Model model) {
        if (!model.containsAttribute("assignmentForm")) {
            model.addAttribute("assignmentForm", new ZonePolicyAssignmentDto());
        }
        addPolicyAssignmentAttributes(model);
        return "manager-policy-assignment";
    }

    @PostMapping("/manager/policy-assignment")
    public String savePolicyAssignment(@ModelAttribute("assignmentForm") ZonePolicyAssignmentDto assignmentForm,
                                       RedirectAttributes redirectAttributes) {
        try {
            assignmentService.assignPolicy(assignmentForm);
            redirectAttributes.addFlashAttribute("successMessage", "Zone policy assignment saved.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("assignmentForm", assignmentForm);
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/manager/policy-assignment";
    }

    private void addPolicyAssignmentAttributes(Model model) {
        model.addAttribute("zoneCargoMappings", assignmentService.getCargoTypeByZone());
        model.addAttribute("activeRules", assignmentService.getActiveRules());
        model.addAttribute("assignmentsByZone", assignmentService.getAssignmentsByZone());
        model.addAttribute("assignedRulesByZone", assignmentService.getAssignedRulesByZone());
    }
}
