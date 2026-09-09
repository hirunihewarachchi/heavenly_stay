package com.ijse.heavenlyStay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequestDTO {
    private String userMessage;
    private Double userLat;
    private Double userLng;
}
