package com.warehouse.controller;

import com.warehouse.dto.CustomerPickupCodeFormDto;
import com.warehouse.model.CustomerPickupCode;
import com.warehouse.service.CustomerPickupCodeService;
import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ManagerCustomerPickupCodeController {

    private final CustomerPickupCodeService pickupCodeService;

    public ManagerCustomerPickupCodeController(CustomerPickupCodeService pickupCodeService) {
        this.pickupCodeService = pickupCodeService;
    }

    @GetMapping("/manager/customer-pickup-codes")
    public String showCustomerPickupCodes(Model model) {
        if (!model.containsAttribute("pickupCodeForm")) {
            model.addAttribute("pickupCodeForm", new CustomerPickupCodeFormDto());
        }
        preparePageModel(model);
        return "manager-customer-pickup-codes";
    }

    @PostMapping("/manager/customer-pickup-codes")
    public String createCustomerPickupCode(@ModelAttribute("pickupCodeForm") CustomerPickupCodeFormDto form,
                                           Principal principal,
                                           Model model) {
        pickupCodeService.normalize(form);
        List<String> validationErrors = pickupCodeService.validateCreateForm(form);
        if (validationErrors.isEmpty()) {
            CustomerPickupCode createdPickupCode = pickupCodeService.createPickupCode(
                    form,
                    principal != null ? principal.getName() : null
            );
            model.addAttribute("createdPickupCode", createdPickupCode);
            model.addAttribute("successMessage", "Pickup code created. Email preview is ready.");
            model.addAttribute("pickupCodeForm", new CustomerPickupCodeFormDto());
        } else {
            model.addAttribute("validationErrors", validationErrors);
        }
        preparePageModel(model);
        return "manager-customer-pickup-codes";
    }

    private void preparePageModel(Model model) {
        model.addAttribute("pickupCodes", pickupCodeService.getRecentPickupCodes());
        model.addAttribute("cargoTypes", pickupCodeService.getCargoTypes());
        model.addAttribute("zoneByCargoType", pickupCodeService.getZoneByCargoType());
        model.addAttribute("locationsByZone", pickupCodeService.getLocationsByZone());
    }
}
