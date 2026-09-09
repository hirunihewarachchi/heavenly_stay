package com.ijse.heavenlyStay.service.impl;

import com.ijse.heavenlyStay.entity.*;
import com.ijse.heavenlyStay.enumeration.*;
import com.ijse.heavenlyStay.exception.CustomerException;
import com.ijse.heavenlyStay.repository.*;
import com.ijse.heavenlyStay.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BoardingRepository boardingRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final LeaseAgreementRepository leaseAgreementRepository;
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public Payment processListingFee(Long boardingId, Long ownerId, BigDecimal amount) {
        log.info("Executing method processListingFee() for boardingId: {}, ownerId: {}", boardingId, ownerId);
        try {
            Boarding b = boardingRepository.findById(boardingId)
                    .orElseThrow(() -> new CustomerException(404, "Boarding not found"));

            User owner = userRepository.findById(ownerId)
                    .orElseThrow(() -> new CustomerException(404, "Owner not found"));

            BigDecimal fee = (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) ? amount : new BigDecimal("1000.00");

            Payment payment = new Payment();
            payment.setAmount(fee);
            payment.setPlatformCommission(BigDecimal.ZERO);
            payment.setTransactionRef("TXN-FEE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            payment.setPaymentType(PaymentType.LISTING_FEE);
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setPayer(owner);
            payment.setBoarding(b);

            Payment savedPayment = paymentRepository.save(payment);

            // Create or Update 3-month Subscription
            Subscription sub = subscriptionRepository.findByBoardingId(boardingId)
                    .orElse(new Subscription());
            sub.setListingFee(fee);
            sub.setStartDate(LocalDateTime.now());
            sub.setExpiryDate(LocalDateTime.now().plusMonths(3));
            sub.setStatus(SubcriptionStatus.ACTIVE);
            sub.setBoarding(b);
            sub.setOwner(owner);
            subscriptionRepository.save(sub);

            // Update Boarding status to PENDING_APPROVAL
            b.setStatus(BoardingStatus.PENDING_APPROVAL);
            boardingRepository.save(b);

            // Send notification
            Notification n = new Notification();
            n.setTitle("Listing Fee Paid");
            n.setMessage("LKR " + fee + " paid for " + b.getName() + ". Pending admin approval.");
            n.setRecipient(owner);
            notificationRepository.save(n);

            return savedPayment;
        } catch (Exception e) {
            log.error("Error in processListingFee() " + e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public Payment processKeyMoney(Long bookingId, Long seekerId, BigDecimal amount) {
        log.info("Executing method processKeyMoney() for bookingId: {}, seekerId: {}", bookingId, seekerId);
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new CustomerException(404, "Booking not found"));

            User seeker = userRepository.findById(seekerId)
                    .orElseThrow(() -> new CustomerException(404, "Seeker not found"));

            BigDecimal keyMoneyAmount = amount != null ? amount : booking.getBoarding().getKeyMoney();
            if (keyMoneyAmount == null) keyMoneyAmount = BigDecimal.ZERO;

            // 5% platform commission added to key money
            BigDecimal commission = keyMoneyAmount.multiply(new BigDecimal("0.05"));
            BigDecimal totalPaid = keyMoneyAmount.add(commission);

            Payment payment = new Payment();
            payment.setAmount(totalPaid);
            payment.setPlatformCommission(commission);
            payment.setTransactionRef("TXN-KEY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            payment.setPaymentType(PaymentType.KEY_MONEY);
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setPayer(seeker);
            payment.setBoarding(booking.getBoarding());
            payment.setBooking(booking);

            Payment savedPayment = paymentRepository.save(payment);

            // Update Booking Status to PAID
            booking.setStatus(BookingStatus.PAID);
            bookingRepository.save(booking);

            // Live update Boarding Room & Bed Availability
            Boarding boarding = booking.getBoarding();
            if (boarding != null) {
                int occupants = (booking.getOccupantCount() != null && booking.getOccupantCount() > 0) ? booking.getOccupantCount() : 1;
                int bedsPerRoom = (boarding.getBedsPerRoom() != null && boarding.getBedsPerRoom() > 0) ? boarding.getBedsPerRoom() : 1;

                int currentBeds = (boarding.getAvailableBeds() != null) ? boarding.getAvailableBeds() : (boarding.getAvailableRooms() * bedsPerRoom);
                int newAvailableBeds = Math.max(0, currentBeds - occupants);
                boarding.setAvailableBeds(newAvailableBeds);

                int newAvailableRooms = (int) Math.ceil((double) newAvailableBeds / bedsPerRoom);
                boarding.setAvailableRooms(newAvailableRooms);
                boardingRepository.save(boarding);
            }

            // Generate Lease Agreement
            LeaseAgreement lease = new LeaseAgreement();
            lease.setStartDate(booking.getMoveInDate());
            lease.setEndDate(booking.getMoveInDate().plusYears(1));
            lease.setMonthlyRent(booking.getBoarding().getMonthlyRent());
            lease.setKeyMoneyPaid(keyMoneyAmount);
            lease.setAgreementTerms("Standard 1-year residential lease agreement for HeavenlyStay.");
            lease.setStatus(LeaseStatus.ACTIVE);
            lease.setBooking(booking);
            lease.setTenant(seeker);
            lease.setOwner(booking.getBoarding().getOwner());
            lease.setBoarding(booking.getBoarding());
            leaseAgreementRepository.save(lease);

            return savedPayment;
        } catch (Exception e) {
            log.error("Error in processKeyMoney() " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Payment> getPaymentsByUser(Long userId) {
        log.info("Executing method getPaymentsByUser() for userId: {}", userId);
        try {
            return paymentRepository.findByPayerUserId(userId);
        } catch (Exception e) {
            log.error("Error in getPaymentsByUser() " + e.getMessage());
            throw e;
        }
    }
}
