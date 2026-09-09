package com.ijse.heavenlyStay.service;

import com.ijse.heavenlyStay.dto.BookingDTO;
import com.ijse.heavenlyStay.enumeration.BookingStatus;

import java.util.List;

public interface BookingService {
    BookingDTO createBooking(BookingDTO dto, Long seekerId);
    List<BookingDTO> getBookingsBySeeker(Long seekerId);
    List<BookingDTO> getBookingsByOwner(Long ownerId);
    BookingDTO updateBookingStatus(Long bookingId, BookingStatus status);
}
