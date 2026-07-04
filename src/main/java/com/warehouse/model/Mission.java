package com.warehouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "missions")
public class Mission {

    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String requestCode;

    @Column(length = 150)
    private String customerName;

    @Column(nullable = false, length = 50)
    private String cargoType;

    @Column(nullable = false, length = 50)
    private String zone;

    @Column(nullable = false, length = 20)
    private String locationCode;

    @Column(nullable = false)
    private Integer priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MissionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private MissionExecutionStep executionStep;

    @Column(length = 80)
    private String currentPositionKey;

    @Column(length = 1000)
    private String notes;

    private Long assignedRobotId;

    @Column(length = 100)
    private String assignedRobotName;

    @Column(length = 1000)
    private String assignmentReason;

    @Column(length = 100)
    private String matchedRuleName;

    @Column(length = 100)
    private String selectedStrategyName;

    @Column(length = 1000)
    private String actionMessage;

    @Column(length = 2000)
    private String decisionSummary;

    private LocalDateTime processedAt;

    private LocalDateTime executionStartedAt;

    private Integer batteryAtExecutionStart;

    private LocalDateTime pickupReachedAt;

    private LocalDateTime returnedAt;

    private LocalDateTime completedAt;

    private LocalDateTime cancelledAt;

    @Column(length = 80)
    private String cancellationReasonCode;

    @Column(length = 1000)
    private String cancellationNote;

    @Column(length = 100)
    private String cancelledBy;

    private LocalDateTime deletedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Mission() {
    }

    public Mission(String requestCode, String customerName, String cargoType, String zone,
                   String locationCode, Integer priority, MissionStatus status, String notes) {
        this.requestCode = requestCode;
        this.customerName = customerName;
        this.cargoType = cargoType;
        this.zone = zone;
        this.locationCode = locationCode;
        this.priority = priority;
        this.status = status;
        this.notes = notes;
    }

    @PrePersist
    public void applyCreateDefaults() {
        LocalDateTime now = LocalDateTime.now();
        if (status == null) {
            status = MissionStatus.PENDING;
        }
        applyExecutionDefaults();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }

    @PreUpdate
    public void applyUpdateDefaults() {
        applyExecutionDefaults();
        updatedAt = LocalDateTime.now();
    }

    private void applyExecutionDefaults() {
        if (executionStep == null) {
            executionStep = MissionExecutionStep.NOT_STARTED;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCargoType() {
        return cargoType;
    }

    public void setCargoType(String cargoType) {
        this.cargoType = cargoType;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public MissionStatus getStatus() {
        return status;
    }

    public void setStatus(MissionStatus status) {
        this.status = status;
    }

    public MissionExecutionStep getExecutionStep() {
        return executionStep;
    }

    public void setExecutionStep(MissionExecutionStep executionStep) {
        this.executionStep = executionStep;
    }

    public String getCurrentPositionKey() {
        return currentPositionKey;
    }

    public void setCurrentPositionKey(String currentPositionKey) {
        this.currentPositionKey = currentPositionKey;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getAssignedRobotId() {
        return assignedRobotId;
    }

    public void setAssignedRobotId(Long assignedRobotId) {
        this.assignedRobotId = assignedRobotId;
    }

    public String getAssignedRobotName() {
        return assignedRobotName;
    }

    public void setAssignedRobotName(String assignedRobotName) {
        this.assignedRobotName = assignedRobotName;
    }

    public String getAssignmentReason() {
        return assignmentReason;
    }

    public void setAssignmentReason(String assignmentReason) {
        this.assignmentReason = assignmentReason;
    }

    public String getMatchedRuleName() {
        return matchedRuleName;
    }

    public void setMatchedRuleName(String matchedRuleName) {
        this.matchedRuleName = matchedRuleName;
    }

    public String getSelectedStrategyName() {
        return selectedStrategyName;
    }

    public void setSelectedStrategyName(String selectedStrategyName) {
        this.selectedStrategyName = selectedStrategyName;
    }

    public String getActionMessage() {
        return actionMessage;
    }

    public void setActionMessage(String actionMessage) {
        this.actionMessage = actionMessage;
    }

    public String getDecisionSummary() {
        return decisionSummary;
    }

    public void setDecisionSummary(String decisionSummary) {
        this.decisionSummary = decisionSummary;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public LocalDateTime getExecutionStartedAt() {
        return executionStartedAt;
    }

    public void setExecutionStartedAt(LocalDateTime executionStartedAt) {
        this.executionStartedAt = executionStartedAt;
    }

    public Integer getBatteryAtExecutionStart() {
        return batteryAtExecutionStart;
    }

    public void setBatteryAtExecutionStart(Integer batteryAtExecutionStart) {
        this.batteryAtExecutionStart = batteryAtExecutionStart;
    }

    public LocalDateTime getPickupReachedAt() {
        return pickupReachedAt;
    }

    public void setPickupReachedAt(LocalDateTime pickupReachedAt) {
        this.pickupReachedAt = pickupReachedAt;
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancellationReasonCode() {
        return cancellationReasonCode;
    }

    public void setCancellationReasonCode(String cancellationReasonCode) {
        this.cancellationReasonCode = cancellationReasonCode;
    }

    public String getCancellationNote() {
        return cancellationNote;
    }

    public void setCancellationNote(String cancellationNote) {
        this.cancellationNote = cancellationNote;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPriorityLabel() {
        if (priority == null) {
            return "N/A";
        }
        return switch (priority) {
            case 1 -> "1 = High";
            case 2 -> "2 = Medium";
            case 3 -> "3 = Low";
            default -> String.valueOf(priority);
        };
    }

    public String getPriorityBadgeClass() {
        if (priority == null) {
            return "priority-default";
        }
        return switch (priority) {
            case 1 -> "priority-high";
            case 2 -> "priority-medium";
            case 3 -> "priority-low";
            default -> "priority-default";
        };
    }

    public String getStatusBadgeClass() {
        if (status == null) {
            return "mission-status-default";
        }
        return switch (status) {
            case PENDING -> "mission-status-pending";
            case ASSIGNED -> "mission-status-assigned";
            case IN_PROGRESS -> "mission-status-in-progress";
            case WAITING_CONFIRMATION -> "mission-status-waiting-confirmation";
            case COMPLETED -> "mission-status-completed";
            case CANCELLED -> "mission-status-cancelled";
        };
    }

    public boolean isPendingMission() {
        return status == MissionStatus.PENDING;
    }

    public boolean isStartableMission() {
        return status == MissionStatus.ASSIGNED
                && deletedAt == null
                && hasAssignedRobot()
                && isExecutionNotStarted();
    }

    public boolean isCompletableMission() {
        return deletedAt == null
                && (status == MissionStatus.WAITING_CONFIRMATION
                || ((status == MissionStatus.ASSIGNED || status == MissionStatus.IN_PROGRESS) && hasReturnedToBase()));
    }

    public boolean hasReturnedToBase() {
        return returnedAt != null || executionStep == MissionExecutionStep.RETURNED_TO_BASE;
    }

    public boolean isAwaitingReturnForCompletion() {
        return deletedAt == null
                && (status == MissionStatus.ASSIGNED || status == MissionStatus.IN_PROGRESS)
                && !hasReturnedToBase();
    }

    public boolean isStoppableMission() {
        return status == MissionStatus.PENDING
                || status == MissionStatus.ASSIGNED
                || status == MissionStatus.IN_PROGRESS
                || status == MissionStatus.WAITING_CONFIRMATION;
    }

    public boolean isDeletableMission() {
        return status == MissionStatus.CANCELLED && deletedAt == null;
    }

    public boolean isCompletedMission() {
        return status == MissionStatus.COMPLETED;
    }

    public boolean isCancelledMission() {
        return status == MissionStatus.CANCELLED;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public String getExecutionStepDisplay() {
        return executionStep != null ? executionStep.name() : MissionExecutionStep.NOT_STARTED.name();
    }

    public boolean isExecutionNotStarted() {
        return executionStep == null || executionStep == MissionExecutionStep.NOT_STARTED;
    }

    public String getCurrentPositionKeyDisplay() {
        return isBlank(currentPositionKey) ? "No backend position recorded" : currentPositionKey;
    }

    public boolean hasAssignedRobot() {
        return !isBlank(assignedRobotName);
    }

    public boolean hasDecisionOutput() {
        return processedAt != null
                || !isBlank(matchedRuleName)
                || !isBlank(selectedStrategyName)
                || !isBlank(actionMessage)
                || !isBlank(decisionSummary);
    }

    public String getCreatedAtDisplay() {
        return formatDateTime(createdAt, "Not recorded");
    }

    public String getProcessedAtDisplay() {
        return formatDateTime(processedAt, "Not processed");
    }

    public String getExecutionStartedAtDisplay() {
        return formatDateTime(executionStartedAt, "Not started");
    }

    public String getPickupReachedAtDisplay() {
        return formatDateTime(pickupReachedAt, "Not reached");
    }

    public String getReturnedAtDisplay() {
        return formatDateTime(returnedAt, "Not returned");
    }

    public String getCompletedAtDisplay() {
        return formatDateTime(completedAt, "Not completed");
    }

    public String getCancelledAtDisplay() {
        return formatDateTime(cancelledAt, "Not cancelled");
    }

    public String getCancellationReasonDisplay() {
        if (isBlank(cancellationReasonCode)) {
            return "Not recorded";
        }
        return CancellationReason.displayNameFor(cancellationReasonCode);
    }

    public String getCancellationNoteDisplay() {
        return isBlank(cancellationNote) ? "No cancellation note provided." : cancellationNote;
    }

    public String getCancelledByDisplay() {
        return isBlank(cancelledBy) ? "Not recorded" : cancelledBy;
    }

    public String getDeletedAtDisplay() {
        return formatDateTime(deletedAt, "Not deleted");
    }

    private String formatDateTime(LocalDateTime dateTime, String fallback) {
        if (dateTime == null) {
            return fallback;
        }
        return DISPLAY_DATE_TIME_FORMATTER.format(dateTime);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
