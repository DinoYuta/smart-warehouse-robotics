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
@Table(name = "customer_pickup_codes")
public class CustomerPickupCode {

    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 150)
    private String customerName;

    @Column(nullable = false, length = 254)
    private String customerEmail;

    @Column(nullable = false, length = 30)
    private String customerPhone;

    @Column(nullable = false, length = 50)
    private String cargoType;

    @Column(nullable = false, length = 20)
    private String pickupLocation;

    @Column(nullable = false)
    private Integer priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CustomerPickupCodeStatus status;

    private Long missionId;

    @Column(length = 100)
    private String createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 100)
    private String usedBy;

    private LocalDateTime usedAt;

    @Column(length = 1000)
    private String note;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public CustomerPickupCode() {
    }

    public CustomerPickupCode(String code,
                              String customerName,
                              String customerEmail,
                              String customerPhone,
                              String cargoType,
                              String pickupLocation,
                              Integer priority,
                              CustomerPickupCodeStatus status,
                              String createdBy,
                              String note) {
        this.code = code;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.cargoType = cargoType;
        this.pickupLocation = pickupLocation;
        this.priority = priority;
        this.status = status;
        this.createdBy = createdBy;
        this.note = note;
    }

    @PrePersist
    public void applyCreateDefaults() {
        LocalDateTime now = LocalDateTime.now();
        if (priority == null) {
            priority = 1;
        }
        if (status == null) {
            status = CustomerPickupCodeStatus.UNUSED;
        }
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }

    @PreUpdate
    public void applyUpdateDefaults() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCargoType() {
        return cargoType;
    }

    public void setCargoType(String cargoType) {
        this.cargoType = cargoType;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public CustomerPickupCodeStatus getStatus() {
        return status;
    }

    public void setStatus(CustomerPickupCodeStatus status) {
        this.status = status;
    }

    public Long getMissionId() {
        return missionId;
    }

    public void setMissionId(Long missionId) {
        this.missionId = missionId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUsedBy() {
        return usedBy;
    }

    public void setUsedBy(String usedBy) {
        this.usedBy = usedBy;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getWarehouseZone() {
        return CargoType.fromDisplayName(cargoType)
                .map(CargoType::getWarehouseZone)
                .orElse("Unknown");
    }

    public String getPriorityLabel() {
        return "Priority: High";
    }

    public String getStatusDisplay() {
        if (status == CustomerPickupCodeStatus.USED) {
            return "Used";
        }
        return "Unused";
    }

    public boolean isUnused() {
        return status == CustomerPickupCodeStatus.UNUSED;
    }

    public String getCreatedAtDisplay() {
        return formatDateTime(createdAt, "Not recorded");
    }

    public String getUsedAtDisplay() {
        return formatDateTime(usedAt, "Not used");
    }

    public String getUsedByDisplay() {
        return isBlank(usedBy) ? "Not used" : usedBy;
    }

    public String getLinkedMissionDisplay() {
        return missionId == null ? "Not linked" : "Mission #" + missionId;
    }

    public String getEmailPreviewSubject() {
        return "Smart Warehouse Pickup Code";
    }

    public String getEmailPreviewText() {
        return "Hello " + customerName + ",\n\n"
                + "Your pickup code is: " + code + "\n\n"
                + "Please show this code to Staff when you arrive at the warehouse.\n\n"
                + "Cargo type: " + cargoType + "\n"
                + "Pickup location: " + pickupLocation + "\n"
                + "Priority: High\n\n"
                + "Thank you.";
    }

    public String getVietnameseEmailPreviewSubject() {
        return "Mã nhận hàng Smart Warehouse";
    }

    public String getVietnameseEmailPreviewText() {
        return "Xin chào " + customerName + ",\n\n"
                + "Mã nhận hàng của bạn là: " + code + "\n\n"
                + "Vui lòng cung cấp mã này cho nhân viên khi bạn đến kho.\n\n"
                + "Loại hàng: " + cargoType + "\n"
                + "Vị trí lấy hàng: " + pickupLocation + "\n"
                + "Độ ưu tiên: Cao\n\n"
                + "Xin cảm ơn.";
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
