package com.ijse.heavenlyStay.service;

import com.ijse.heavenlyStay.dto.BoardingDTO;
import com.ijse.heavenlyStay.enumeration.GenderPreference;

import java.math.BigDecimal;
import java.util.List;

public interface BoardingService {
    BoardingDTO createBoarding(BoardingDTO dto, Long ownerId);
    BoardingDTO updateBoarding(Long boardingId, BoardingDTO dto);
    void deleteBoarding(Long boardingId);
    List<BoardingDTO> getApprovedBoardings();
    List<BoardingDTO> getBoardingsByOwner(Long ownerId);
    BoardingDTO getBoardingById(Long boardingId);
    List<BoardingDTO> searchBoardings(String district, GenderPreference gender, BigDecimal maxRent, Double userLat, Double userLng, Double radiusKm);
    List<BoardingDTO> findNearbyBoardings(Double userLat, Double userLng, Double radiusKm);
    void addImageToBoarding(Long boardingId, String imageUrl, boolean isCover);
}
