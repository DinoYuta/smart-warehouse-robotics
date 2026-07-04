package com.warehouse.repository;

import com.warehouse.model.ZonePolicyAssignment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZonePolicyAssignmentRepository extends JpaRepository<ZonePolicyAssignment, Long> {

    Optional<ZonePolicyAssignment> findByZone(String zone);

    List<ZonePolicyAssignment> findAllByOrderByZoneAsc();
}
