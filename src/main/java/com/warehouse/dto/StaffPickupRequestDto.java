package com.warehouse.dto;

public class StaffPickupRequestDto {

    private String requestCode;
    private String customerName;
    private String cargoType;
    private String locationCode;
    private String warehouseZone;
    private Integer priority;
    private String notes;

    public StaffPickupRequestDto() {
        this.priority = 2;
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

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public String getWarehouseZone() {
        return warehouseZone;
    }

    public void setWarehouseZone(String warehouseZone) {
        this.warehouseZone = warehouseZone;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
