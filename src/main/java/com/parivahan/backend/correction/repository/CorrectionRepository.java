package com.parivahan.backend.correction.repository;

import com.parivahan.backend.correction.entity.CorrectionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CorrectionRepository extends JpaRepository<CorrectionRequest, Long> {
    List<CorrectionRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
}
