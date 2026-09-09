package com.ijse.heavenlyStay.controller;

import com.ijse.heavenlyStay.dto.BookingDTO;
import com.ijse.heavenlyStay.dto.CommonResponse;
import com.ijse.heavenlyStay.enumeration.BookingStatus;
import com.ijse.heavenlyStay.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/bookings")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public CommonResponse createBooking(
            @RequestBody BookingDTO dto,
            @RequestParam(required = false, defaultValue = "1") Long seekerId) {
        Long seekerToUse = dto.getSeekerId() != null ? dto.getSeekerId() : seekerId;
        BookingDTO created = bookingService.createBooking(dto, seekerToUse);
        return new CommonResponse(200, created, "Booking request submitted");
    }

    @GetMapping("/seeker/{seekerId}")
    public CommonResponse getBookingsBySeeker(@PathVariable Long seekerId) {
        List<BookingDTO> list = bookingService.getBookingsBySeeker(seekerId);
        return new CommonResponse(200, list, "Seeker bookings fetched");
    }

    @GetMapping("/owner/{ownerId}")
    public CommonResponse getBookingsByOwner(@PathVariable Long ownerId) {
        List<BookingDTO> list = bookingService.getBookingsByOwner(ownerId);
        return new CommonResponse(200, list, "Owner bookings fetched");
    }

    @PutMapping("/{id}/status")
    public CommonResponse updateBookingStatus(
            @PathVariable Long id,
            @RequestParam BookingStatus status) {
        BookingDTO updated = bookingService.updateBookingStatus(id, status);
        return new CommonResponse(200, updated, "Booking status updated to " + status);
    }
}
