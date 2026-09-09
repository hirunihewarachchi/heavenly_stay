package com.ijse.heavenlyStay.service.impl;

import com.ijse.heavenlyStay.dto.ReviewDTO;
import com.ijse.heavenlyStay.entity.Boarding;
import com.ijse.heavenlyStay.entity.Review;
import com.ijse.heavenlyStay.entity.User;
import com.ijse.heavenlyStay.exception.CustomerException;
import com.ijse.heavenlyStay.repository.BoardingRepository;
import com.ijse.heavenlyStay.repository.BookingRepository;
import com.ijse.heavenlyStay.repository.ReviewRepository;
import com.ijse.heavenlyStay.repository.UserRepository;
import com.ijse.heavenlyStay.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BoardingRepository boardingRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public ReviewDTO postReview(ReviewDTO dto, Long reviewerId) {
        log.info("Executing method postReview() for reviewerId: {}", reviewerId);
        try {
            if (dto.getRating() == null || dto.getRating() < 1 || dto.getRating() > 5) {
                throw new CustomerException(400, "Rating must be between 1 and 5 stars");
            }

            if (dto.getComment() == null || dto.getComment().trim().isEmpty()) {
                throw new CustomerException(400, "Review comment cannot be empty");
            }

            Long boardingId = dto.getBoardingId();
            if (!canUserReview(reviewerId, boardingId)) {
                throw new CustomerException(403, "Only verified tenants who have booked/stayed at this boarding can post a review.");
            }

            Boarding boarding = boardingRepository.findById(boardingId)
                    .orElseThrow(() -> new CustomerException(404, "Boarding place not found"));

            User reviewer = userRepository.findById(reviewerId)
                    .orElseThrow(() -> new CustomerException(404, "Reviewer user not found"));

            Review review = new Review();
            review.setRating(dto.getRating());
            review.setComment(dto.getComment().trim());
            review.setBoarding(boarding);
            review.setReviewer(reviewer);

            Review saved = reviewRepository.save(review);
            return mapToDTO(saved);
        } catch (Exception e) {
            log.error("Error in postReview() " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<ReviewDTO> getBoardingReviews(Long boardingId) {
        log.info("Executing method getBoardingReviews() for boardingId: {}", boardingId);
        try {
            return reviewRepository.findByBoardingId(boardingId)
                    .stream().map(this::mapToDTO).toList();
        } catch (Exception e) {
            log.error("Error in getBoardingReviews() " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Double getBoardingAvgRating(Long boardingId) {
        log.info("Executing method getBoardingAvgRating() for boardingId: {}", boardingId);
        try {
            Double avg = reviewRepository.findAvgRatingByBoardingId(boardingId);
            return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
        } catch (Exception e) {
            log.error("Error in getBoardingAvgRating() " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean canUserReview(Long userId, Long boardingId) {
        log.info("Executing method canUserReview() for userId: {}, boardingId: {}", userId, boardingId);
        try {
            if (userId == null || boardingId == null) return false;
            return bookingRepository.existsTenantBooking(userId, boardingId);
        } catch (Exception e) {
            log.error("Error in canUserReview() " + e.getMessage());
            throw e;
        }
    }

    private ReviewDTO mapToDTO(Review r) {
        ReviewDTO dto = new ReviewDTO();
        dto.setReviewId(r.getReviewId());
        dto.setRating(r.getRating());
        dto.setComment(r.getComment());
        dto.setCreatedAt(r.getCreatedAt());
        if (r.getBoarding() != null) {
            dto.setBoardingId(r.getBoarding().getBoardingId());
            dto.setBoardingName(r.getBoarding().getName());
        }
        if (r.getReviewer() != null) {
            dto.setReviewerId(r.getReviewer().getUserId());
            dto.setReviewerName(r.getReviewer().getUserName());
        }
        return dto;
    }
}
