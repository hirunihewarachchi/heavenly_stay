package com.ijse.heavenlyStay.dto;

import com.ijse.heavenlyStay.enumeration.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDTO {
    private Long bookingId;
    private LocalDate moveInDate;
    private Integer occupantCount;
    private String notes;
    private BookingStatus status;
    private LocalDateTime createdAt;
    private Long seekerId;
    private String seekerName;
    private String seekerPhone;
    private Long boardingId;
    private String boardingName;
    private BigDecimal monthlyRent;
    private BigDecimal keyMoney;
    private Long roomId;
}
