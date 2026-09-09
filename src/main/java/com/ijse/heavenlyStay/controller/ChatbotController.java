package com.ijse.heavenlyStay.controller;

import com.ijse.heavenlyStay.dto.ChatRequestDTO;
import com.ijse.heavenlyStay.dto.ChatResponseDTO;
import com.ijse.heavenlyStay.dto.CommonResponse;
import com.ijse.heavenlyStay.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/chatbot")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/query")
    public CommonResponse queryChatbot(@RequestBody ChatRequestDTO request) {
        ChatResponseDTO response = chatbotService.processQuery(request);
        return new CommonResponse(200, response, "Chatbot response processed");
    }
}
