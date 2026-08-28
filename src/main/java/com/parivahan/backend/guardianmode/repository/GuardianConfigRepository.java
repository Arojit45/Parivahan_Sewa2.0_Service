package com.parivahan.backend.guardianmode.repository;

import com.parivahan.backend.guardianmode.entity.GuardianConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuardianConfigRepository extends JpaRepository<GuardianConfig, Long> {
    Optional<GuardianConfig> findByVehicleId(Long vehicleId);
}
