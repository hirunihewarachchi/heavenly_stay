package com.ijse.heavenlyStay.controller;

import com.ijse.heavenlyStay.dto.CommonResponse;
import com.ijse.heavenlyStay.dto.ReviewDTO;
import com.ijse.heavenlyStay.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/reviews")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public CommonResponse postReview(
            @RequestBody ReviewDTO dto,
            @RequestParam(required = false, defaultValue = "1") Long reviewerId) {
        Long userIdToUse = dto.getReviewerId() != null ? dto.getReviewerId() : reviewerId;
        ReviewDTO saved = reviewService.postReview(dto, userIdToUse);
        return new CommonResponse(200, saved, "Review posted successfully");
    }

    @GetMapping("/boarding/{boardingId}")
    public CommonResponse getBoardingReviews(@PathVariable Long boardingId) {
        List<ReviewDTO> reviews = reviewService.getBoardingReviews(boardingId);
        Double avgRating = reviewService.getBoardingAvgRating(boardingId);

        Map<String, Object> data = new HashMap<>();
        data.put("reviews", reviews);
        data.put("avgRating", avgRating);
        data.put("totalReviews", reviews.size());

        return new CommonResponse(200, data, "Boarding reviews fetched");
    }

    @GetMapping("/can-review")
    public CommonResponse canReview(
            @RequestParam Long userId,
            @RequestParam Long boardingId) {
        boolean canReview = reviewService.canUserReview(userId, boardingId);
        Map<String, Boolean> res = new HashMap<>();
        res.put("canReview", canReview);
        return new CommonResponse(200, res, "Review eligibility checked");
    }
}
