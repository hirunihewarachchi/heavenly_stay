package com.ijse.heavenlyStay.repository;

import com.ijse.heavenlyStay.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FacilityRepository extends JpaRepository<Facility, Long> {

    @Query("SELECT f FROM Facility f WHERE f.boarding.boardingId = :boardingId")
    List<Facility> findByBoardingId(@Param("boardingId") Long boardingId);

}
