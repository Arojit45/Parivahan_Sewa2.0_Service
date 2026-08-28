package com.parivahan.backend.vehicleregistration.repository;

import com.parivahan.backend.vehicleregistration.entity.VehicleRegistrationApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRegistrationRepository extends JpaRepository<VehicleRegistrationApplication, Long> {
    List<VehicleRegistrationApplication> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT v FROM VehicleRegistrationApplication v WHERE v.user.id = :userId AND v.applicationStatus = 'DRAFT' ORDER BY v.updatedAt DESC LIMIT 1")
    Optional<VehicleRegistrationApplication> findLatestDraftByUserId(Long userId);

    Optional<VehicleRegistrationApplication> findByApplicationNumber(String applicationNumber);
}
