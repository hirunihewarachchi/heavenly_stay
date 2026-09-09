package com.ijse.heavenlyStay.service.impl;

import com.ijse.heavenlyStay.dto.BookingDTO;
import com.ijse.heavenlyStay.entity.*;
import com.ijse.heavenlyStay.enumeration.BookingStatus;
import com.ijse.heavenlyStay.exception.CustomerException;
import com.ijse.heavenlyStay.repository.*;
import com.ijse.heavenlyStay.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BoardingRepository boardingRepository;
    private final RoomRepository roomRepository;
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public BookingDTO createBooking(BookingDTO dto, Long seekerId) {
        log.info("Executing method createBooking() for seekerId: {}", seekerId);
        try {
            User seeker = userRepository.findById(seekerId)
                    .orElseThrow(() -> new CustomerException(404, "Seeker user not found"));

            Boarding boarding = boardingRepository.findById(dto.getBoardingId())
                    .orElseThrow(() -> new CustomerException(404, "Boarding not found"));

            Room room = null;
            if (dto.getRoomId() != null) {
                room = roomRepository.findById(dto.getRoomId()).orElse(null);
            }

            Booking booking = new Booking();
            booking.setMoveInDate(dto.getMoveInDate());
            booking.setOccupantCount(dto.getOccupantCount() != null ? dto.getOccupantCount() : 1);
            booking.setNotes(dto.getNotes());
            booking.setStatus(BookingStatus.PENDING);
            booking.setSeeker(seeker);
            booking.setBoarding(boarding);
            booking.setRoom(room);

            Booking saved = bookingRepository.save(booking);

            // Send Notification to Owner
            Notification notif = new Notification();
            notif.setTitle("New Booking Request");
            notif.setMessage("User " + seeker.getUserName() + " sent a booking request for " + boarding.getName());
            notif.setRecipient(boarding.getOwner());
            notificationRepository.save(notif);

            return mapToDTO(saved);
        } catch (Exception e) {
            log.error("Error in createBooking() " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<BookingDTO> getBookingsBySeeker(Long seekerId) {
        log.info("Executing method getBookingsBySeeker() for seekerId: {}", seekerId);
        try {
            return bookingRepository.findBySeekerUserId(seekerId)
                    .stream().map(this::mapToDTO).toList();
        } catch (Exception e) {
            log.error("Error in getBookingsBySeeker() " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<BookingDTO> getBookingsByOwner(Long ownerId) {
        log.info("Executing method getBookingsByOwner() for ownerId: {}", ownerId);
        try {
            return bookingRepository.findByOwnerUserId(ownerId)
                    .stream().map(this::mapToDTO).toList();
        } catch (Exception e) {
            log.error("Error in getBookingsByOwner() " + e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public BookingDTO updateBookingStatus(Long bookingId, BookingStatus status) {
        log.info("Executing method updateBookingStatus() for bookingId: {}, status: {}", bookingId, status);
        try {
            Booking b = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new CustomerException(404, "Booking not found"));

            b.setStatus(status);
            Booking updated = bookingRepository.save(b);

            // Notify Seeker
            Notification notif = new Notification();
            notif.setTitle("Booking " + status);
            notif.setMessage("Your booking request for " + b.getBoarding().getName() + " has been " + status.name().toLowerCase());
            notif.setRecipient(b.getSeeker());
            notificationRepository.save(notif);

            return mapToDTO(updated);
        } catch (Exception e) {
            log.error("Error in updateBookingStatus() " + e.getMessage());
            throw e;
        }
    }

    private BookingDTO mapToDTO(Booking b) {
        BookingDTO dto = new BookingDTO();
        dto.setBookingId(b.getBookingId());
        dto.setMoveInDate(b.getMoveInDate());
        dto.setOccupantCount(b.getOccupantCount());
        dto.setNotes(b.getNotes());
        dto.setStatus(b.getStatus());
        dto.setCreatedAt(b.getCreatedAt());

        if (b.getSeeker() != null) {
            dto.setSeekerId(b.getSeeker().getUserId());
            dto.setSeekerName(b.getSeeker().getUserName());
            dto.setSeekerPhone(b.getSeeker().getPhone());
        }

        if (b.getBoarding() != null) {
            dto.setBoardingId(b.getBoarding().getBoardingId());
            dto.setBoardingName(b.getBoarding().getName());
            dto.setMonthlyRent(b.getBoarding().getMonthlyRent());
            dto.setKeyMoney(b.getBoarding().getKeyMoney());
        }

        if (b.getRoom() != null) {
            dto.setRoomId(b.getRoom().getRoomId());
        }

        return dto;
    }
}
