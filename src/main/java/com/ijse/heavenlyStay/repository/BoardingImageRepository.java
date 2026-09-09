package com.ijse.heavenlyStay.repository;

import com.ijse.heavenlyStay.entity.BoardingImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardingImageRepository extends JpaRepository<BoardingImage, Long> {

    @Query("SELECT bi FROM BoardingImage bi WHERE bi.boarding.boardingId = :boardingId")
    List<BoardingImage> findByBoardingId(@Param("boardingId") Long boardingId);

    @Query("DELETE FROM BoardingImage bi WHERE bi.boarding.boardingId = :boardingId")
    void deleteByBoardingId(@Param("boardingId") Long boardingId);
}
