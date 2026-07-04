package com.warehouse.repository;

import com.warehouse.model.Strategy;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StrategyRepository extends JpaRepository<Strategy, Long> {

    Optional<Strategy> findByCode(String code);

    long countByActiveTrue();

    List<Strategy> findByActiveTrueOrderByNameAsc();
}
