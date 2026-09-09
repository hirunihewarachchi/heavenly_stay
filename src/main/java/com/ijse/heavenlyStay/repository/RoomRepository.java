package com.ijse.heavenlyStay.repository;

import com.ijse.heavenlyStay.entity.Room;
import com.ijse.heavenlyStay.enumeration.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r FROM Room r WHERE r.boarding.boardingId = :boardingId")
    List<Room> findByBoardingId(@Param("boardingId") Long boardingId);

    @Query("SELECT r FROM Room r WHERE r.boarding.boardingId = :boardingId AND r.status = :status")
    List<Room> findByBoardingIdAndStatus(@Param("boardingId") Long boardingId, @Param("status") RoomStatus status);
}
