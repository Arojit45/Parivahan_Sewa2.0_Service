package com.parivahan.backend.alertsystem.repository;

import com.parivahan.backend.alertsystem.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndReadFalse(Long userId);

    @Query("UPDATE Alert a SET a.read = true WHERE a.user.id = :userId AND a.read = false")
    @org.springframework.data.jpa.repository.Modifying
    void markAllReadByUserId(Long userId);
}
