package com.ijse.heavenlyStay.repository;

import com.ijse.heavenlyStay.entity.Payment;
import com.ijse.heavenlyStay.enumeration.PaymentStatus;
import com.ijse.heavenlyStay.enumeration.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p WHERE p.payer.userId = :payerId ORDER BY p.paymentDate DESC")
    List<Payment> findByPayerUserId(@Param("payerId") Long payerId);

    @Query("SELECT p FROM Payment p WHERE p.paymentType = :type AND p.paymentStatus = :status")
    List<Payment> findByTypeAndStatus(@Param("type") PaymentType type, @Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = 'SUCCESS'")
    BigDecimal sumTotalSuccessfulPayments();

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentType = com.ijse.heavenlyStay.enumeration.PaymentType.LISTING_FEE AND p.paymentStatus = com.ijse.heavenlyStay.enumeration.PaymentStatus.SUCCESS")
    BigDecimal sumTotalListingRevenue();

    @Query("SELECT COALESCE(SUM(p.platformCommission), 0) FROM Payment p WHERE p.paymentStatus = 'SUCCESS'")
    BigDecimal sumTotalPlatformCommission();
}
