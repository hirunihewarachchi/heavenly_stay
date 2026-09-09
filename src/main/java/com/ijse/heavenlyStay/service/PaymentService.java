package com.ijse.heavenlyStay.service;

import com.ijse.heavenlyStay.entity.Payment;
import com.ijse.heavenlyStay.enumeration.PaymentType;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    Payment processListingFee(Long boardingId, Long ownerId, BigDecimal amount);
    Payment processKeyMoney(Long bookingId, Long seekerId, BigDecimal amount);
    List<Payment> getPaymentsByUser(Long userId);
}
