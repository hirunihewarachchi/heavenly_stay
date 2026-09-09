package com.ijse.heavenlyStay.repository;

import com.ijse.heavenlyStay.entity.Boarding;
import com.ijse.heavenlyStay.enumeration.BoardingStatus;
import com.ijse.heavenlyStay.enumeration.GenderPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface BoardingRepository extends JpaRepository<Boarding, Long> {

    @Query("SELECT b FROM Boarding b WHERE b.status = :status ORDER BY b.createdAt DESC")
    List<Boarding> findByStatus(@Param("status") BoardingStatus status);

    @Query("SELECT b FROM Boarding b WHERE b.owner.userId = :ownerId ORDER BY b.createdAt DESC")
    List<Boarding> findByOwnerUserId(@Param("ownerId") Long ownerId);

    @Query("SELECT b FROM Boarding b WHERE b.status = 'APPROVED' AND LOWER(b.district) = LOWER(:district)")
    List<Boarding> findApprovedByDistrict(@Param("district") String district);

    @Query("SELECT b FROM Boarding b WHERE b.status = 'APPROVED' AND " +
           "(:district IS NULL OR LOWER(b.district) LIKE LOWER(CONCAT('%', :district, '%'))) AND " +
           "(:gender IS NULL OR b.genderPreference = :gender OR b.genderPreference = 'ANY') AND " +
           "(:maxRent IS NULL OR b.monthlyRent <= :maxRent)")
    List<Boarding> searchApprovedBoardings(
            @Param("district") String district,
            @Param("gender") GenderPreference gender,
            @Param("maxRent") BigDecimal maxRent);

    @Query("SELECT b FROM Boarding b WHERE b.status = 'APPROVED' AND b.latitude IS NOT NULL AND b.longitude IS NOT NULL")
    List<Boarding> findApprovedWithCoordinates();

    @Query("SELECT COUNT(b) FROM Boarding b WHERE b.status = :status")
    long countByStatus(@Param("status") BoardingStatus status);
}
