package com.ijse.heavenlyStay.controller;

import com.ijse.heavenlyStay.dto.CommonResponse;
import com.ijse.heavenlyStay.entity.Payment;
import com.ijse.heavenlyStay.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1/payments")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/listing-fee")
    public CommonResponse payListingFee(
            @RequestParam Long boardingId,
            @RequestParam Long ownerId,
            @RequestParam(required = false) BigDecimal amount) {
        Payment p = paymentService.processListingFee(boardingId, ownerId, amount);
        return new CommonResponse(200, p, "Listing fee paid successfully. Listing submitted for approval.");
    }

    @PostMapping("/key-money")
    public CommonResponse payKeyMoney(
            @RequestParam Long bookingId,
            @RequestParam Long seekerId,
            @RequestParam(required = false) BigDecimal amount) {
        Payment p = paymentService.processKeyMoney(bookingId, seekerId, amount);
        return new CommonResponse(200, p, "Key money payment successful. Lease agreement created.");
    }

    @GetMapping("/user/{userId}")
    public CommonResponse getPaymentsByUser(@PathVariable Long userId) {
        List<Payment> list = paymentService.getPaymentsByUser(userId);
        return new CommonResponse(200, list, "User payments fetched");
    }
}
