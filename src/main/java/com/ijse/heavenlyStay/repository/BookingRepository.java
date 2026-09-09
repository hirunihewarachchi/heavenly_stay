package com.ijse.heavenlyStay.repository;

import com.ijse.heavenlyStay.entity.Booking;
import com.ijse.heavenlyStay.enumeration.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.seeker.userId = :seekerId ORDER BY b.createdAt DESC")
    List<Booking> findBySeekerUserId(@Param("seekerId") Long seekerId);

    @Query("SELECT b FROM Booking b WHERE b.boarding.owner.userId = :ownerId ORDER BY b.createdAt DESC")
    List<Booking> findByOwnerUserId(@Param("ownerId") Long ownerId);

    @Query("SELECT b FROM Booking b WHERE b.boarding.boardingId = :boardingId ORDER BY b.createdAt DESC")
    List<Booking> findByBoardingId(@Param("boardingId") Long boardingId);

    @Query("SELECT b FROM Booking b WHERE b.status = :status ORDER BY b.createdAt DESC")
    List<Booking> findByStatus(@Param("status") BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status")
    long countByStatus(@Param("status") BookingStatus status);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.seeker.userId = :seekerId AND b.boarding.boardingId = :boardingId AND (b.status = 'PAID' OR b.status = 'APPROVED')")
    boolean existsTenantBooking(@Param("seekerId") Long seekerId, @Param("boardingId") Long boardingId);
}
