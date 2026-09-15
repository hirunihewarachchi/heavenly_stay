package com.ijse.heavenlyStay.repository;

import com.ijse.heavenlyStay.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.boarding.boardingId = :boardingId ORDER BY r.createdAt DESC")
    List<Review> findByBoardingId(@Param("boardingId") Long boardingId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.boarding.boardingId = :boardingId")
    Double findAvgRatingByBoardingId(@Param("boardingId") Long boardingId);

    @Query("SELECT r.boarding.boardingId, COUNT(r) as reviewCount FROM Review r GROUP BY r.boarding.boardingId ORDER BY reviewCount DESC")
    List<Object[]> findBoardingIdsByReviewCountDesc();
}
