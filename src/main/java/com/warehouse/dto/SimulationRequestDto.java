package com.warehouse.dto;

public class SimulationRequestDto {

    private Integer battery;
    private Boolean obstacleDetected;
    private Integer robotLoad;
    private Double distance;
    private Integer priority;

    public SimulationRequestDto() {
    }

    public SimulationRequestDto(Integer battery) {
        this(battery, false, 45, 12.5, 2);
    }

    public SimulationRequestDto(Integer battery, Boolean obstacleDetected, Integer robotLoad) {
        this(battery, obstacleDetected, robotLoad, 12.5, 2);
    }

    public SimulationRequestDto(Integer battery, Boolean obstacleDetected, Integer robotLoad,
                                Double distance, Integer priority) {
        this.battery = battery;
        this.obstacleDetected = obstacleDetected;
        this.robotLoad = robotLoad;
        this.distance = distance;
        this.priority = priority;
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
}
