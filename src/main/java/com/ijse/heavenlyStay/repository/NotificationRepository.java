package com.ijse.heavenlyStay.repository;

import com.ijse.heavenlyStay.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.recipient.userId = :recipientId ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientUserId(@Param("recipientId") Long recipientId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient.userId = :recipientId AND n.isRead = false")
    long countUnreadByRecipientUserId(@Param("recipientId") Long recipientId);
}
