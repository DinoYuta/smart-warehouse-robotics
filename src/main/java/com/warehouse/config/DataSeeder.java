package com.warehouse.config;

import com.warehouse.model.AppUser;
import com.warehouse.model.Robot;
import com.warehouse.model.Strategy;
import com.warehouse.repository.AppUserRepository;
import com.warehouse.repository.RobotRepository;
import com.warehouse.repository.RuleRepository;
import com.warehouse.repository.StrategyRepository;
import com.warehouse.service.RuleService;
import java.util.Optional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedInitialData(StrategyRepository strategyRepository,
                                      RobotRepository robotRepository,
                                      RuleRepository ruleRepository,
                                      RuleService ruleService,
                                      AppUserRepository appUserRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            seedUserIfMissing(appUserRepository, passwordEncoder, "Admin", "admin", "ADMIN");
            seedUserIfMissing(appUserRepository, passwordEncoder, "Manage", "manage", "MANAGE");
            seedUserIfMissing(appUserRepository, passwordEncoder, "Nova001", "nova001", "STAFF");

            Strategy chargingStrategy = saveStrategyIfMissing(
                    strategyRepository,
                    "CHARGING",
                    "Charging Strategy",
                    "ChargingStrategy",
                    "Route the robot to a charging station when the battery is low."
            );

            Strategy fastRouteStrategy = saveStrategyIfMissing(
                    strategyRepository,
                    "FAST_ROUTE",
                    "Fast Route Strategy",
                    "FastRouteStrategy",
                    "Prefer the shortest travel time for urgent tasks."
            );

            Strategy safeRouteStrategy = saveStrategyIfMissing(
                    strategyRepository,
                    "SAFE_ROUTE",
                    "Safe Route Strategy",
                    "SafeRouteStrategy",
                    "Prefer safer routes when risk or priority requires caution."
            );

            Strategy obstacleAvoidanceStrategy = saveStrategyIfMissing(
                    strategyRepository,
                    "OBSTACLE_AVOIDANCE",
                    "Obstacle Avoidance Strategy",
                    "ObstacleAvoidanceStrategy",
                    "Recalculate movement when an obstacle is detected."
            );

            Strategy heavyLoadStrategy = saveStrategyIfMissing(
                    strategyRepository,
                    "HEAVY_LOAD",
                    "Heavy Load Strategy",
                    "HeavyLoadStrategy",
                    "Reduce speed and choose safer paths when the robot carries a heavy load."
            );

            Strategy energySavingStrategy = saveStrategyIfMissing(
                    strategyRepository,
                    "ENERGY_SAVING",
                    "Energy Saving Strategy",
                    "EnergySavingStrategy",
                    "Reduce speed and conserve energy during normal operations."
            );

            if (robotRepository.count() == 0) {
                robotRepository.save(new Robot(
                        "RB-100",
                        "Picker Alpha",
                        68,
                        false,
                        40,
                        12.5,
                        2,
                        "IDLE",
                        energySavingStrategy
                ));
                robotRepository.save(new Robot(
                        "RB-200",
                        "Mover Beta",
                        15,
                        true,
                        75,
                        8.2,
                        1,
                        "MOVING",
                        chargingStrategy
                ));
                robotRepository.save(new Robot(
                        "RB-300",
                        "Carrier Gamma",
                        76,
                        false,
                        88,
                        18.7,
                        3,
                        "LOADED",
                        heavyLoadStrategy
                ));
            }

            saveRuleIfMissing(
                    ruleRepository,
                    ruleService,
                    "Critical Battery With Obstacle Rule",
                    "battery < 10 AND obstacleDetected == TRUE",
                    chargingStrategy.getClassName(),
                    1
            );
            saveRuleIfMissing(
                    ruleRepository,
                    ruleService,
                    "Obstacle Detection Rule",
                    "obstacleDetected == TRUE",
                    obstacleAvoidanceStrategy.getClassName(),
                    2
            );
            saveRuleIfMissing(
                    ruleRepository,
                    ruleService,
                    "Heavy Load Rule",
                    "robotLoad > 80",
                    heavyLoadStrategy.getClassName(),
                    3
            );
            saveRuleIfMissing(
                    ruleRepository,
                    ruleService,
                    "Low Battery Rule",
                    "battery < 20",
                    energySavingStrategy.getClassName(),
                    4
            );
            saveRuleIfMissing(
                    ruleRepository,
                    ruleService,
                    "Urgent Task Fast Route Rule",
                    "priority == 1",
                    fastRouteStrategy.getClassName(),
                    5
            );
            saveRuleIfMissing(
                    ruleRepository,
                    ruleService,
                    "Long Distance Safe Route Rule",
                    "distance > 15",
                    safeRouteStrategy.getClassName(),
                    6
            );
        };
    }

    private void seedUserIfMissing(AppUserRepository appUserRepository,
                                   PasswordEncoder passwordEncoder,
                                   String username,
                                   String rawPassword,
                                   String role) {
        if (appUserRepository.findByUsernameIgnoreCase(username).isPresent()) {
            return;
        }

        appUserRepository.save(new AppUser(
                username,
                passwordEncoder.encode(rawPassword),
                role,
                true
        ));
    }

    private Strategy saveStrategyIfMissing(StrategyRepository strategyRepository,
                                           String code,
                                           String name,
                                           String className,
                                           String description) {
        Optional<Strategy> existingStrategy = strategyRepository.findByCode(code);
        return existingStrategy.orElseGet(() -> strategyRepository.save(
                new Strategy(code, name, className, true, description)
        ));
    }

    private void saveRuleIfMissing(RuleRepository ruleRepository,
                                   RuleService ruleService,
                                   String name,
                                   String conditionExpression,
                                   String strategyName,
                                   Integer priority) {
        Optional<com.warehouse.model.Rule> existingRule = ruleRepository.findByRuleName(name);
        if (existingRule.isPresent()) {
            com.warehouse.model.Rule rule = existingRule.get();
            if (seedRuleDiffers(rule, conditionExpression, strategyName, priority)) {
                com.warehouse.dto.RuleFormDto form = new com.warehouse.dto.RuleFormDto();
                form.setId(rule.getId());
                form.setRuleName(name);
                form.setConditionExpression(conditionExpression);
                form.setStrategyName(strategyName);
                form.setActiveStatus(true);
                form.setPriority(priority);
                ruleService.saveRule(form);
            }
            return;
        }

        com.warehouse.dto.RuleFormDto form = new com.warehouse.dto.RuleFormDto();
        form.setRuleName(name);
        form.setConditionExpression(conditionExpression);
        form.setStrategyName(strategyName);
        form.setActiveStatus(true);
        form.setPriority(priority);
        ruleService.saveRule(form);
    }

    private boolean seedRuleDiffers(com.warehouse.model.Rule rule,
                                    String conditionExpression,
                                    String strategyName,
                                    Integer priority) {
        return !conditionExpression.equals(rule.getConditionExpression())
                || !strategyName.equals(rule.getStrategyName())
                || !priority.equals(rule.getPriority())
                || !Boolean.TRUE.equals(rule.getActiveStatus());
    }
}
