package com.parivahan.backend.drivinglicense.repository;

import com.parivahan.backend.drivinglicense.entity.DrivingSchool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrivingSchoolRepository extends JpaRepository<DrivingSchool, Long> {

    List<DrivingSchool> findByStateIgnoreCaseAndCityIgnoreCase(String state, String city);

    List<DrivingSchool> findByStateIgnoreCase(String state);

    boolean existsByName(String name);
}
