package com.ijse.heavenlyStay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponseDTO {
    private String replyMessage;
    private String intent;
    private List<String> suggestedPills;
    private List<BoardingDTO> matchedBoardings;
}
