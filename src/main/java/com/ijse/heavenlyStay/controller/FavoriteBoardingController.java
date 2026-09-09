package com.ijse.heavenlyStay.controller;

import com.ijse.heavenlyStay.dto.CommonResponse;
import com.ijse.heavenlyStay.dto.FavoriteBoardingDTO;
import com.ijse.heavenlyStay.service.FavoriteBoardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/favorites")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class FavoriteBoardingController {

    private final FavoriteBoardingService favoriteBoardingService;

    @PostMapping("/toggle")
    public CommonResponse toggleFavorite(
            @RequestParam Long userId,
            @RequestParam Long boardingId) {
        boolean isFavorite = favoriteBoardingService.toggleFavorite(userId, boardingId);
        Map<String, Object> data = new HashMap<>();
        data.put("isFavorite", isFavorite);
        String msg = isFavorite ? "Saved to favorites" : "Removed from favorites";
        return new CommonResponse(200, data, msg);
    }

    @GetMapping("/user/{userId}")
    public CommonResponse getUserFavorites(@PathVariable Long userId) {
        List<FavoriteBoardingDTO> favorites = favoriteBoardingService.getUserFavorites(userId);
        return new CommonResponse(200, favorites, "User favorites fetched");
    }

    @GetMapping("/check")
    public CommonResponse checkIsFavorite(
            @RequestParam Long userId,
            @RequestParam Long boardingId) {
        boolean isFav = favoriteBoardingService.isFavorite(userId, boardingId);
        Map<String, Boolean> res = new HashMap<>();
        res.put("isFavorite", isFav);
        return new CommonResponse(200, res, "Favorite status checked");
    }
}
