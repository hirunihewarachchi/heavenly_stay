package com.ijse.heavenlyStay.service;

import com.ijse.heavenlyStay.dto.FavoriteBoardingDTO;

import java.util.List;

public interface FavoriteBoardingService {
    boolean toggleFavorite(Long userId, Long boardingId);
    List<FavoriteBoardingDTO> getUserFavorites(Long userId);
    boolean isFavorite(Long userId, Long boardingId);
}
