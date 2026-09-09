package com.ijse.heavenlyStay.repository;

import com.ijse.heavenlyStay.entity.FavoriteBoarding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteBoardingRepository extends JpaRepository<FavoriteBoarding, Long> {

    @Query("SELECT f FROM FavoriteBoarding f WHERE f.user.userId = :userId ORDER BY f.savedAt DESC")
    List<FavoriteBoarding> findByUserUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM FavoriteBoarding f WHERE f.user.userId = :userId AND f.boarding.boardingId = :boardingId")
    Optional<FavoriteBoarding> findByUserUserIdAndBoardingBoardingId(@Param("userId") Long userId, @Param("boardingId") Long boardingId);

    boolean existsByUserUserIdAndBoardingBoardingId(Long userId, Long boardingId);

    void deleteByUserUserIdAndBoardingBoardingId(Long userId, Long boardingId);
}
