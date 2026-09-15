package com.ijse.heavenlyStay.repository;

import com.ijse.heavenlyStay.entity.Subscription;
import com.ijse.heavenlyStay.enumeration.SubcriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("SELECT s FROM Subscription s WHERE s.boarding.boardingId = :boardingId")
    Optional<Subscription> findByBoardingId(@Param("boardingId") Long boardingId);
}
