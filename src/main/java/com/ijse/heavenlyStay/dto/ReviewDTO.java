package com.ijse.heavenlyStay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {
    private Long reviewId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private Long boardingId;
    private String boardingName;
    private Long reviewerId;
    private String reviewerName;
}
