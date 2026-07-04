package com.warehouse.controller;

import com.warehouse.dto.MissionLifecycleResult;
import com.warehouse.dto.CustomerPickupCodeLookupDto;
import com.warehouse.dto.CustomerPickupCodeProcessResult;
import com.warehouse.dto.StaffPickupRequestDto;
import com.warehouse.model.Mission;
import com.warehouse.model.MissionStatus;
import com.warehouse.service.CustomerPickupCodeService;
import com.warehouse.service.MissionProcessingService;
import com.warehouse.service.MissionService;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StaffPickupRequestController {

    private final MissionService missionService;
    private final MissionProcessingService missionProcessingService;
    private final CustomerPickupCodeService pickupCodeService;

    public StaffPickupRequestController(MissionService missionService,
                                        MissionProcessingService missionProcessingService,
                                        CustomerPickupCodeService pickupCodeService) {
        this.missionService = missionService;
        this.missionProcessingService = missionProcessingService;
        this.pickupCodeService = pickupCodeService;
    }

    @GetMapping("/staff/pickup-request")
    public String showPickupRequest(Model model) {
        model.addAttribute("pickupRequest", new StaffPickupRequestDto());
        model.addAttribute("pickupCodeLookup", new CustomerPickupCodeLookupDto());
        prepareOptions(model);
        return "staff-pickup-request";
    }

    @PostMapping("/staff/pickup-request")
    public String submitPickupRequest(@ModelAttribute("pickupRequest") StaffPickupRequestDto pickupRequest,
                                      Model model) {
        missionService.normalize(pickupRequest);
        List<String> validationErrors = missionService.validatePickupRequest(pickupRequest);
        model.addAttribute("pickupCodeLookup", new CustomerPickupCodeLookupDto());
        prepareOptions(model);

        if (validationErrors.isEmpty()) {
            Mission savedMission = missionService.createMission(pickupRequest);
            model.addAttribute("savedMission", savedMission);
            model.addAttribute(
                    "successMessage",
                    "Pickup request saved as a PENDING mission. Process it from My Missions to assign a robot."
            );
        } else {
            model.addAttribute("validationErrors", validationErrors);
        }

        return "staff-pickup-request";
    }

    @PostMapping("/staff/pickup-request/customer-code/lookup")
    public String lookupCustomerPickupCode(
            @ModelAttribute("pickupCodeLookup") CustomerPickupCodeLookupDto pickupCodeLookup,
            Model model) {
        model.addAttribute("pickupRequest", new StaffPickupRequestDto());
        prepareOptions(model);

        try {
            model.addAttribute("lookupPickupCode", pickupCodeService.lookupUnusedCode(
                    pickupCodeLookup.getPickupCode()
            ));
        } catch (IllegalArgumentException ex) {
            model.addAttribute("pickupCodeErrorMessage", ex.getMessage());
        }

        return "staff-pickup-request";
    }

    @PostMapping("/staff/pickup-request/customer-code/process")
    public String processCustomerPickupCode(@RequestParam("pickupCode") String pickupCode,
                                            Principal principal,
                                            RedirectAttributes redirectAttributes) {
        try {
            CustomerPickupCodeProcessResult result = pickupCodeService.processPickupCode(
                    pickupCode,
                    principal != null ? principal.getName() : null
            );
            Mission mission = result.mission();
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Pickup code " + result.pickupCode().getCode()
                            + " processed into mission " + mission.getRequestCode()
                            + " through RuleEvaluator and StrategyContext."
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("pickupCodeErrorMessage", ex.getMessage());
            return "redirect:/staff/pickup-request";
        }
        return "redirect:/staff/missions";
    }

    @GetMapping("/staff/missions")
    public String showMissions(@RequestParam(name = "filter", defaultValue = "active") String filter,
                               Model model) {
        prepareMissionLists(model, filter);
        return "staff-missions";
    }

    @GetMapping("/staff/missions/{id}")
    public String showMissionDetail(@PathVariable("id") Long missionId,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        return missionService.findMissionById(missionId)
                .map(mission -> {
                    model.addAttribute("mission", mission);
                    return "staff-mission-detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Mission not found: " + missionId);
                    return "redirect:/staff/missions";
                });
    }

    @PostMapping("/staff/missions/{id}/process")
    public String processMission(@PathVariable("id") Long missionId,
                                 RedirectAttributes redirectAttributes) {
        try {
            Mission processedMission = missionProcessingService.processPendingMission(missionId);
            if (processedMission.getStatus() == MissionStatus.PENDING
                    && processedMission.getAssignedRobotName() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", processedMission.getAssignmentReason());
            } else {
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Mission " + processedMission.getRequestCode()
                                + " processed through RuleEvaluator and StrategyContext."
                );
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/staff/missions";
    }

    @PostMapping("/staff/missions/{id}/complete")
    public String completeMission(@PathVariable("id") Long missionId,
                                  RedirectAttributes redirectAttributes) {
        return completeMissionSafely(missionId, redirectAttributes);
    }

    @GetMapping("/staff/missions/{id}/complete")
    public String completeMissionGetFallback(@PathVariable("id") Long missionId,
                                             RedirectAttributes redirectAttributes) {
        return completeMissionSafely(missionId, redirectAttributes);
    }

    private String completeMissionSafely(Long missionId, RedirectAttributes redirectAttributes) {
        try {
            MissionLifecycleResult lifecycleResult = missionService.completeMission(missionId);
            Mission completedMission = lifecycleResult.mission();
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Mission completed." + chargingWorkflowMessage(lifecycleResult)
            );
            addNotification(
                    redirectAttributes,
                    "mission:" + completedMission.getId() + ":completed",
                    "Mission completed.",
                    "success"
            );
            addChargingWorkflowNotifications(redirectAttributes, lifecycleResult);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", completionErrorMessage(ex));
        }
        return "redirect:/staff/missions";
    }

    @PostMapping("/staff/missions/{id}/start-execution")
    public String startExecution(@PathVariable("id") Long missionId,
                                 RedirectAttributes redirectAttributes) {
        try {
            Mission startedMission = missionService.startExecution(missionId);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Mission " + startedMission.getRequestCode() + " execution started from Base Station."
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/staff/missions";
    }

    @PostMapping("/staff/missions/{id}/stop")
    public String stopMission(@PathVariable("id") Long missionId,
                              @RequestParam(name = "cancellationReasonCode", required = false)
                              String cancellationReasonCode,
                              @RequestParam(name = "cancellationNote", required = false) String cancellationNote,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {
            MissionLifecycleResult lifecycleResult = missionService.stopMission(
                    missionId,
                    cancellationReasonCode,
                    cancellationNote,
                    principal != null ? principal.getName() : null
            );
            Mission stoppedMission = lifecycleResult.mission();
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Mission " + stoppedMission.getRequestCode() + " stopped as CANCELLED."
                            + stoppedChargingWorkflowMessage(lifecycleResult)
            );
            addNotification(
                    redirectAttributes,
                    "mission:" + stoppedMission.getId() + ":stopped",
                    "Mission stopped.",
                    "warning"
            );
            addChargingWorkflowNotifications(redirectAttributes, lifecycleResult);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/staff/missions?filter=cancelled";
    }

    @PostMapping("/staff/missions/{id}/delete")
    public String deleteStoppedMission(@PathVariable("id") Long missionId,
                                       RedirectAttributes redirectAttributes) {
        try {
            Mission deletedMission = missionService.deleteStoppedMission(missionId);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Cancelled mission " + deletedMission.getRequestCode() + " deleted from the main mission lists."
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/staff/missions?filter=cancelled";
    }

    private void prepareOptions(Model model) {
        model.addAttribute("cargoTypes", missionService.getCargoTypes());
        model.addAttribute("warehouseZones", missionService.getWarehouseZones());
        model.addAttribute("priorities", missionService.getPriorities());
        model.addAttribute("zoneByCargoType", missionService.getZoneByCargoType());
        model.addAttribute("locationsByZone", missionService.getLocationsByZone());
        model.addAttribute("loadByCargoType", missionService.getLoadByCargoType());
    }

    private void prepareMissionLists(Model model, String selectedFilter) {
        List<Mission> missions = missionService.getMissionsNewestFirst();
        List<Mission> activeMissions = missionService.getActiveMissions(missions);
        List<Mission> completedMissions = missionService.getCompletedMissions(missions);
        List<Mission> cancelledMissions = missionService.getCancelledMissions(missions);
        List<Mission> historyMissions = missionService.getHistoryMissions(missions);
        String normalizedFilter = normalizeMissionFilter(selectedFilter);
        Map<Long, Boolean> completionReadyByMissionId = buildCompletionReadyMap(missions);
        Map<Long, Boolean> completionWaitingByMissionId = buildCompletionWaitingMap(
                missions,
                completionReadyByMissionId
        );

        model.addAttribute("missions", missions);
        model.addAttribute("activeMissions", activeMissions);
        model.addAttribute("completedMissions", completedMissions);
        model.addAttribute("cancelledMissions", cancelledMissions);
        model.addAttribute("historyMissions", historyMissions);
        model.addAttribute("completionReadyByMissionId", completionReadyByMissionId);
        model.addAttribute("completionWaitingByMissionId", completionWaitingByMissionId);
        model.addAttribute("cancellationReasonOptions", missionService.getCancellationReasonOptions());
        model.addAttribute("selectedMissionFilter", normalizedFilter);
        model.addAttribute("visibleMissions", selectVisibleMissions(
                normalizedFilter,
                missions,
                activeMissions,
                completedMissions,
                cancelledMissions
        ));
        model.addAttribute("visibleMissionGroupTitle", missionGroupTitle(normalizedFilter));
    }

    private Map<Long, Boolean> buildCompletionReadyMap(List<Mission> missions) {
        Map<Long, Boolean> completionReadyByMissionId = new LinkedHashMap<>();
        for (Mission mission : missions) {
            completionReadyByMissionId.put(mission.getId(), missionService.isMissionReadyForCompletion(mission));
        }
        return completionReadyByMissionId;
    }

    private Map<Long, Boolean> buildCompletionWaitingMap(List<Mission> missions,
                                                         Map<Long, Boolean> completionReadyByMissionId) {
        Map<Long, Boolean> completionWaitingByMissionId = new LinkedHashMap<>();
        for (Mission mission : missions) {
            boolean activeForCompletion = mission.getStatus() == MissionStatus.ASSIGNED
                    || mission.getStatus() == MissionStatus.IN_PROGRESS;
            boolean readyForCompletion = Boolean.TRUE.equals(completionReadyByMissionId.get(mission.getId()));
            completionWaitingByMissionId.put(mission.getId(), activeForCompletion && !readyForCompletion);
        }
        return completionWaitingByMissionId;
    }

    private String normalizeMissionFilter(String selectedFilter) {
        if ("completed".equals(selectedFilter)
                || "cancelled".equals(selectedFilter)
                || "all".equals(selectedFilter)) {
            return selectedFilter;
        }
        return "active";
    }

    private List<Mission> selectVisibleMissions(String selectedFilter,
                                                List<Mission> missions,
                                                List<Mission> activeMissions,
                                                List<Mission> completedMissions,
                                                List<Mission> cancelledMissions) {
        return switch (selectedFilter) {
            case "completed" -> completedMissions;
            case "cancelled" -> cancelledMissions;
            case "all" -> missions;
            default -> activeMissions;
        };
    }

    private String missionGroupTitle(String selectedFilter) {
        return switch (selectedFilter) {
            case "completed" -> "Completed";
            case "cancelled" -> "Cancelled / Stopped";
            case "all" -> "All non-deleted missions";
            default -> "Active / Waiting";
        };
    }

    private String chargingWorkflowMessage(MissionLifecycleResult lifecycleResult) {
        if (!lifecycleResult.chargingStarted()) {
            return "";
        }
        return " Remaining tasks were reassigned and robot sent to Charging Station."
                + " Reassigned queued missions: " + lifecycleResult.reassignedMissionCount()
                + ". Unassigned queued missions: " + lifecycleResult.unassignedMissionCount() + ".";
    }

    private String stoppedChargingWorkflowMessage(MissionLifecycleResult lifecycleResult) {
        if (!lifecycleResult.chargingStarted()) {
            return "";
        }
        return " Critical battery robot sent to Charging Station."
                + " Reassigned queued missions: " + lifecycleResult.reassignedMissionCount()
                + ". Unassigned queued missions: " + lifecycleResult.unassignedMissionCount() + ".";
    }

    private String completionErrorMessage(RuntimeException ex) {
        String message = ex.getMessage();
        if (MissionService.MISSION_NOT_FOUND_MESSAGE.equals(message)
                || MissionService.MISSION_ALREADY_COMPLETED_MESSAGE.equals(message)
                || MissionService.CANCELLED_MISSION_CANNOT_BE_COMPLETED_MESSAGE.equals(message)
                || MissionService.COMPLETION_REQUIRES_RETURN_MESSAGE.equals(message)) {
            return message;
        }
        return MissionService.COMPLETION_REQUIRES_RETURN_MESSAGE;
    }

    private void addNotification(RedirectAttributes redirectAttributes,
                                 String notificationKey,
                                 String notificationMessage,
                                 String notificationType) {
        redirectAttributes.addFlashAttribute("notificationKey", notificationKey);
        redirectAttributes.addFlashAttribute("notificationMessage", notificationMessage);
        redirectAttributes.addFlashAttribute("notificationType", notificationType);
    }

    private void addChargingWorkflowNotifications(RedirectAttributes redirectAttributes,
                                                  MissionLifecycleResult lifecycleResult) {
        if (!lifecycleResult.chargingStarted()) {
            return;
        }

        Long missionId = lifecycleResult.mission().getId();
        redirectAttributes.addFlashAttribute(
                "chargingStartedNotificationKey",
                "mission:" + missionId + ":charging-started"
        );
        redirectAttributes.addFlashAttribute("chargingStartedNotificationMessage", "Robot is charging at station.");
        redirectAttributes.addFlashAttribute("chargingStartedNotificationType", "info");

        if (lifecycleResult.reassignedMissionCount() > 0) {
            redirectAttributes.addFlashAttribute(
                    "taskReassignedNotificationKey",
                    "mission:" + missionId + ":task-reassigned"
            );
            redirectAttributes.addFlashAttribute(
                    "taskReassignedNotificationMessage",
                    "Remaining tasks were reassigned."
            );
            redirectAttributes.addFlashAttribute("taskReassignedNotificationType", "warning");
        }
    }
}
