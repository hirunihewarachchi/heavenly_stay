package com.ijse.heavenlyStay.service;

import com.ijse.heavenlyStay.dto.ReviewDTO;

import java.util.List;

public interface ReviewService {
    ReviewDTO postReview(ReviewDTO dto, Long reviewerId);
    List<ReviewDTO> getBoardingReviews(Long boardingId);
    Double getBoardingAvgRating(Long boardingId);
    boolean canUserReview(Long userId, Long boardingId);
}
