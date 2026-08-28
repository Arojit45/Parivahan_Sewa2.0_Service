package com.parivahan.backend.challan.repository;

import com.parivahan.backend.challan.entity.ChallanDispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChallanDisputeRepository extends JpaRepository<ChallanDispute, Long> {

    Optional<ChallanDispute> findByChallanId(Long challanId);

    Optional<ChallanDispute> findByDisputeNumber(String disputeNumber);

    @Query("SELECT d FROM ChallanDispute d WHERE d.user.id = :userId ORDER BY d.createdAt DESC")
    List<ChallanDispute> findAllByUserId(Long userId);
}
