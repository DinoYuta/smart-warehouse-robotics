package com.warehouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "robots")
public class Robot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    private Integer battery;

    private Boolean obstacleDetected;

    private Integer robotLoad;

    private Double distance;

    private Integer priority;

    @Column(nullable = false, length = 50)
    private String status;

    private Boolean chargingRequired;

    private Boolean charging;

    private LocalDateTime chargingStartedAt;

    private LocalDateTime chargingCompletedAt;

    private Integer batteryBeforeCharging;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_strategy_id")
    private Strategy currentStrategy;

    public Robot() {
    }

    public Robot(String code, String name, Integer battery, Boolean obstacleDetected,
                 Integer robotLoad, Double distance, Integer priority, String status,
                 Strategy currentStrategy) {
        this.code = code;
        this.name = name;
        this.battery = battery;
        this.obstacleDetected = obstacleDetected;
        this.robotLoad = robotLoad;
        this.distance = distance;
        this.priority = priority;
        this.status = status;
        this.currentStrategy = currentStrategy;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getBattery() {
        return battery;
    }

    public void setBattery(Integer battery) {
        this.battery = battery;
    }

    public Boolean getObstacleDetected() {
        return obstacleDetected;
    }

    public void setObstacleDetected(Boolean obstacleDetected) {
        this.obstacleDetected = obstacleDetected;
    }

    public Integer getRobotLoad() {
        return robotLoad;
    }

    public void setRobotLoad(Integer robotLoad) {
        this.robotLoad = robotLoad;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getChargingRequired() {
        return chargingRequired;
    }

    public void setChargingRequired(Boolean chargingRequired) {
        this.chargingRequired = chargingRequired;
    }

    public Boolean getCharging() {
        return charging;
    }

    public void setCharging(Boolean charging) {
        this.charging = charging;
    }

    public LocalDateTime getChargingStartedAt() {
        return chargingStartedAt;
    }

    public void setChargingStartedAt(LocalDateTime chargingStartedAt) {
        this.chargingStartedAt = chargingStartedAt;
    }

    public LocalDateTime getChargingCompletedAt() {
        return chargingCompletedAt;
    }

    public void setChargingCompletedAt(LocalDateTime chargingCompletedAt) {
        this.chargingCompletedAt = chargingCompletedAt;
    }

    public Integer getBatteryBeforeCharging() {
        return batteryBeforeCharging;
    }

    public void setBatteryBeforeCharging(Integer batteryBeforeCharging) {
        this.batteryBeforeCharging = batteryBeforeCharging;
    }

    public Strategy getCurrentStrategy() {
        return currentStrategy;
    }

    public void setCurrentStrategy(Strategy currentStrategy) {
        this.currentStrategy = currentStrategy;
    }
}
