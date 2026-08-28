package com.parivahan.backend.challan.repository;

import com.parivahan.backend.challan.entity.Challan;
import com.parivahan.backend.challan.enums.ChallanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallanRepository extends JpaRepository<Challan, Long> {

    List<Challan> findByVehicleId(Long vehicleId);

    List<Challan> findByVehicleIdAndStatus(Long vehicleId, ChallanStatus status);

    // Get all challans for all vehicles owned by a user
    @Query("SELECT c FROM Challan c WHERE c.vehicle.user.id = :userId ORDER BY c.challanDate DESC")
    List<Challan> findAllByUserId(Long userId);

    // Get pending challans for a vehicle (used by dashboard)
    @Query("SELECT c FROM Challan c WHERE c.vehicle.id = :vehicleId AND c.status = 'PENDING'")
    List<Challan> findPendingByVehicleId(Long vehicleId);
}
