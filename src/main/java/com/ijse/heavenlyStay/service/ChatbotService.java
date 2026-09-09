package com.ijse.heavenlyStay.service;

import com.ijse.heavenlyStay.dto.ChatRequestDTO;
import com.ijse.heavenlyStay.dto.ChatResponseDTO;

public interface ChatbotService {
    ChatResponseDTO processQuery(ChatRequestDTO request);
}
