package com.parivahan.backend.drivinglicense.repository;

import com.parivahan.backend.drivinglicense.entity.DrivingLicenseApplication;
import com.parivahan.backend.drivinglicense.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DrivingLicenseApplicationRepository extends JpaRepository<DrivingLicenseApplication, Long> {

    List<DrivingLicenseApplication> findByUserId(Long userId);

    Optional<DrivingLicenseApplication> findByApplicationNumber(String applicationNumber);

    /**
     * Finds the single in-progress application for a user — excludes completed terminal states.
     */
    Optional<DrivingLicenseApplication> findTopByUserIdAndApplicationStatusNotInOrderByCreatedAtDesc(
            Long userId,
            List<ApplicationStatus> excludedStatuses
    );
}
