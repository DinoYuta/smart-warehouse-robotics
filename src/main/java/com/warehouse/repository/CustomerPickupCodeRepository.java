package com.warehouse.repository;

import com.warehouse.model.CustomerPickupCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerPickupCodeRepository extends JpaRepository<CustomerPickupCode, Long> {

    Optional<CustomerPickupCode> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<CustomerPickupCode> findTop20ByOrderByCreatedAtDescIdDesc();
}
