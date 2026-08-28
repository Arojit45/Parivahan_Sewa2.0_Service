package com.parivahan.backend.fleet.repository;

import com.parivahan.backend.fleet.domain.FleetRegistration;
import com.parivahan.backend.fleet.enums.FleetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FleetRegistrationRepository extends JpaRepository<FleetRegistration, Long> {
    List<FleetRegistration> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    List<FleetRegistration> findByOwnerIdAndStatus(Long ownerId, FleetStatus status);
    Optional<FleetRegistration> findByFleetRegistrationNumber(String fleetRegistrationNumber);
    boolean existsByOwnerIdAndStatusIn(Long ownerId, List<FleetStatus> statuses);
}
