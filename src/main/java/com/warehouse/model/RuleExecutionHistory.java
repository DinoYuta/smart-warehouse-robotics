package com.warehouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "rule_execution_history")
public class RuleExecutionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String robotCode;

    @Column(nullable = false, length = 100)
    private String robotName;

    private Integer battery;

    private Boolean obstacleDetected;

    private Integer robotLoad;

    private Double distance;

    private Integer priority;

    @Column(nullable = false)
    private boolean matched;

    @Column(length = 100)
    private String matchedRuleName;

    @Column(length = 500)
    private String matchedConditionExpression;

    @Column(length = 100)
    private String selectedStrategy;

    @Column(length = 1000)
    private String actionMessage;

    @Column(nullable = false)
    private LocalDateTime executedAt;

    public RuleExecutionHistory() {
    }

    public RuleExecutionHistory(String robotCode, String robotName, Integer battery,
                                Boolean obstacleDetected, Integer robotLoad, Double distance,
                                Integer priority, boolean matched, String matchedRuleName,
                                String matchedConditionExpression, String selectedStrategy,
                                String actionMessage, LocalDateTime executedAt) {
        this.robotCode = robotCode;
        this.robotName = robotName;
        this.battery = battery;
        this.obstacleDetected = obstacleDetected;
        this.robotLoad = robotLoad;
        this.distance = distance;
        this.priority = priority;
        this.matched = matched;
        this.matchedRuleName = matchedRuleName;
        this.matchedConditionExpression = matchedConditionExpression;
        this.selectedStrategy = selectedStrategy;
        this.actionMessage = actionMessage;
        this.executedAt = executedAt;
    }

    @PrePersist
    public void applyDefaults() {
        if (executedAt == null) {
            executedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRobotCode() {
        return robotCode;
    }

    public void setRobotCode(String robotCode) {
        this.robotCode = robotCode;
    }

    public String getRobotName() {
        return robotName;
    }

    public void setRobotName(String robotName) {
        this.robotName = robotName;
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

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public String getMatchedRuleName() {
        return matchedRuleName;
    }

    public void setMatchedRuleName(String matchedRuleName) {
        this.matchedRuleName = matchedRuleName;
    }

    public String getMatchedConditionExpression() {
        return matchedConditionExpression;
    }

    public void setMatchedConditionExpression(String matchedConditionExpression) {
        this.matchedConditionExpression = matchedConditionExpression;
    }

    public String getSelectedStrategy() {
        return selectedStrategy;
    }

    public void setSelectedStrategy(String selectedStrategy) {
        this.selectedStrategy = selectedStrategy;
    }

    public String getActionMessage() {
        return actionMessage;
    }

    public void setActionMessage(String actionMessage) {
        this.actionMessage = actionMessage;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }
}
