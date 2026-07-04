package com.warehouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "rules")
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_name", length = 100)
    private String ruleName;

    @Column(length = 500)
    private String conditionExpression;

    @Column(length = 100)
    private String strategyName;

    private Boolean activeStatus;

    private Integer priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_strategy_id")
    private Strategy legacyTargetStrategy;

    public Rule() {
    }

    public Rule(String ruleName, String conditionExpression, String strategyName,
                Boolean activeStatus, Integer priority) {
        this.ruleName = ruleName;
        this.conditionExpression = conditionExpression;
        this.strategyName = strategyName;
        this.activeStatus = activeStatus;
        this.priority = priority;
    }

    @PrePersist
    @PreUpdate
    public void applyDefaults() {
        if (activeStatus == null) {
            activeStatus = true;
        }
        if (priority == null) {
            priority = 100;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getConditionExpression() {
        return conditionExpression;
    }

    public void setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public Boolean getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(Boolean activeStatus) {
        this.activeStatus = activeStatus;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(activeStatus);
    }

    public Strategy getLegacyTargetStrategy() {
        return legacyTargetStrategy;
    }

    public void setLegacyTargetStrategy(Strategy legacyTargetStrategy) {
        this.legacyTargetStrategy = legacyTargetStrategy;
    }
}
