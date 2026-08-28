package com.parivahan.backend.vehicle.repository;

import com.parivahan.backend.vehicle.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);
    List<Vehicle> findByUserId(Long userId);
    void deleteByRegistrationNumberAndUserId(String registrationNumber, Long userId);
}
