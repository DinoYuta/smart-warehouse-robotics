package com.warehouse.controller;

import com.warehouse.dto.RuleFormDto;
import com.warehouse.service.RuleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RuleManagementController {

    private final RuleService ruleService;

    public RuleManagementController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @GetMapping("/rules")
    public String showRules(Model model) {
        if (!model.containsAttribute("ruleForm")) {
            model.addAttribute("ruleForm", ruleService.createEmptyForm());
        }
        addRulePageAttributes(model, false);
        return "rules";
    }

    @GetMapping("/rules/{id}/edit")
    public String editRule(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("ruleForm")) {
            model.addAttribute("ruleForm", ruleService.findFormById(id));
        }
        addRulePageAttributes(model, true);
        return "rules";
    }

    @PostMapping("/rules")
    public String saveRule(@ModelAttribute("ruleForm") RuleFormDto ruleForm,
                           RedirectAttributes redirectAttributes) {
        try {
            ruleService.saveRule(ruleForm);
            redirectAttributes.addFlashAttribute("successMessage", "Rule saved successfully.");
            return "redirect:/rules";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("ruleForm", ruleForm);
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            if (ruleForm.getId() != null) {
                return "redirect:/rules/" + ruleForm.getId() + "/edit";
            }
            return "redirect:/rules";
        }
    }

    @PostMapping("/rules/{id}/toggle")
    public String toggleRule(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        ruleService.toggleRule(id);
        redirectAttributes.addFlashAttribute("successMessage", "Rule status updated.");
        return "redirect:/rules";
    }

    @PostMapping("/rules/{id}/delete")
    public String deleteRule(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        ruleService.deleteRule(id);
        redirectAttributes.addFlashAttribute("successMessage", "Rule deleted.");
        return "redirect:/rules";
    }

    private void addRulePageAttributes(Model model, boolean editMode) {
        model.addAttribute("rules", ruleService.findAllRules());
        model.addAttribute("strategyNames", ruleService.findStrategyNames());
        model.addAttribute("editMode", editMode);
    }
}
