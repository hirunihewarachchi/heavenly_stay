package com.ijse.heavenlyStay.controller;

import com.ijse.heavenlyStay.dto.CommonResponse;
import com.ijse.heavenlyStay.entity.Notification;
import com.ijse.heavenlyStay.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/notifications")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping("/user/{userId}")
    public CommonResponse getNotificationsByUser(@PathVariable Long userId) {
        List<Notification> list = notificationRepository.findByRecipientUserId(userId);
        return new CommonResponse(200, list, "Notifications fetched");
    }

    @PutMapping("/{id}/read")
    public CommonResponse markAsRead(@PathVariable Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
        return new CommonResponse(200, "Notification marked as read");
    }
}
