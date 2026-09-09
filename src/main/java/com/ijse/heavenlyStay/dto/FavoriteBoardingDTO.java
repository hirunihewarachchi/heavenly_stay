package com.ijse.heavenlyStay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteBoardingDTO {
    private Long favoriteId;
    private LocalDateTime savedAt;
    private Long userId;
    private BoardingDTO boarding;
}
