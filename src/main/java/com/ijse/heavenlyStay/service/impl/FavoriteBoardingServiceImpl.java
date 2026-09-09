package com.ijse.heavenlyStay.service.impl;

import com.ijse.heavenlyStay.dto.BoardingDTO;
import com.ijse.heavenlyStay.dto.FavoriteBoardingDTO;
import com.ijse.heavenlyStay.entity.Boarding;
import com.ijse.heavenlyStay.entity.FavoriteBoarding;
import com.ijse.heavenlyStay.entity.User;
import com.ijse.heavenlyStay.exception.CustomerException;
import com.ijse.heavenlyStay.repository.BoardingRepository;
import com.ijse.heavenlyStay.repository.FavoriteBoardingRepository;
import com.ijse.heavenlyStay.repository.UserRepository;
import com.ijse.heavenlyStay.service.BoardingService;
import com.ijse.heavenlyStay.service.FavoriteBoardingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FavoriteBoardingServiceImpl implements FavoriteBoardingService {

    private final FavoriteBoardingRepository favoriteBoardingRepository;
    private final UserRepository userRepository;
    private final BoardingRepository boardingRepository;
    private final BoardingService boardingService;

    @Override
    @Transactional
    public boolean toggleFavorite(Long userId, Long boardingId) {
        log.info("Executing method toggleFavorite() for userId: {}, boardingId: {}", userId, boardingId);
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomerException(404, "User not found"));

            Boarding boarding = boardingRepository.findById(boardingId)
                    .orElseThrow(() -> new CustomerException(404, "Boarding not found"));

            Optional<FavoriteBoarding> existing = favoriteBoardingRepository.findByUserUserIdAndBoardingBoardingId(userId, boardingId);
            if (existing.isPresent()) {
                favoriteBoardingRepository.delete(existing.get());
                return false; // Removed from wishlist
            } else {
                FavoriteBoarding fav = new FavoriteBoarding();
                fav.setUser(user);
                fav.setBoarding(boarding);
                favoriteBoardingRepository.save(fav);
                return true; // Added to wishlist
            }
        } catch (Exception e) {
            log.error("Error in toggleFavorite() " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<FavoriteBoardingDTO> getUserFavorites(Long userId) {
        log.info("Executing method getUserFavorites() for userId: {}", userId);
        try {
            return favoriteBoardingRepository.findByUserUserId(userId)
                    .stream().map(f -> {
                        BoardingDTO boardingDTO = boardingService.getBoardingById(f.getBoarding().getBoardingId());
                        return new FavoriteBoardingDTO(f.getFavoriteId(), f.getSavedAt(), userId, boardingDTO);
                    }).toList();
        } catch (Exception e) {
            log.error("Error in getUserFavorites() " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean isFavorite(Long userId, Long boardingId) {
        log.info("Executing method isFavorite() for userId: {}, boardingId: {}", userId, boardingId);
        try {
            if (userId == null || boardingId == null) return false;
            return favoriteBoardingRepository.existsByUserUserIdAndBoardingBoardingId(userId, boardingId);
        } catch (Exception e) {
            log.error("Error in isFavorite() " + e.getMessage());
            throw e;
        }
    }
}
