package com.parivahan.backend.vehicle.repository;

import com.parivahan.backend.vehicle.domain.RcRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RcRegistryRepository extends JpaRepository<RcRegistry, Long> {
    Optional<RcRegistry> findByRegistrationNumber(String registrationNumber);
}
