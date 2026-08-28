package com.parivahan.backend.guardianmode.repository;

import com.parivahan.backend.guardianmode.entity.GeofenceBreachEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeofenceBreachEventRepository extends JpaRepository<GeofenceBreachEvent, Long> {
    List<GeofenceBreachEvent> findByVehicleIdOrderByBreachedAtDesc(Long vehicleId);
}
