package com.warehouse.repository;

import com.warehouse.model.Robot;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RobotRepository extends JpaRepository<Robot, Long> {

    @EntityGraph(attributePaths = "currentStrategy")
    List<Robot> findTop10ByOrderByIdAsc();

    @EntityGraph(attributePaths = "currentStrategy")
    List<Robot> findAllByOrderByIdAsc();
}
