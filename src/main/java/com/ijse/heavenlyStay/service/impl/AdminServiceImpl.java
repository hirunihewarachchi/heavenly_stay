package com.ijse.heavenlyStay.service.impl;

import com.ijse.heavenlyStay.dto.BoardingDTO;
import com.ijse.heavenlyStay.dto.SignupRequestDTO;
import com.ijse.heavenlyStay.dto.UserDTO;
import com.ijse.heavenlyStay.entity.Boarding;
import com.ijse.heavenlyStay.entity.Notification;
import com.ijse.heavenlyStay.entity.User;
import com.ijse.heavenlyStay.enumeration.BoardingStatus;
import com.ijse.heavenlyStay.enumeration.UserRole;
import com.ijse.heavenlyStay.exception.CustomerException;
import com.ijse.heavenlyStay.repository.BoardingRepository;
import com.ijse.heavenlyStay.repository.NotificationRepository;
import com.ijse.heavenlyStay.repository.PaymentRepository;
import com.ijse.heavenlyStay.repository.UserRepository;
import com.ijse.heavenlyStay.service.AdminService;
import com.ijse.heavenlyStay.service.BoardingService;
import com.ijse.heavenlyStay.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final BoardingRepository boardingRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;
    private final BoardingService boardingService;
    private final UserService userService;

    @Override
    public List<BoardingDTO> getPendingBoardings() {
        log.info("Executing method getPendingBoardings()");
        try {
            return boardingRepository.findByStatus(BoardingStatus.PENDING_APPROVAL)
                    .stream().map(b -> boardingService.getBoardingById(b.getBoardingId())).toList();
        } catch (Exception e) {
            log.error("Error in getPendingBoardings() " + e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public BoardingDTO approveBoarding(Long boardingId) {
        log.info("Executing method approveBoarding() for boardingId: {}", boardingId);
        try {
            Boarding b = boardingRepository.findById(boardingId)
                    .orElseThrow(() -> new CustomerException(404, "Boarding not found"));

            b.setStatus(BoardingStatus.APPROVED);
            Boarding saved = boardingRepository.save(b);

            Notification notif = new Notification();
            notif.setTitle("Advertisement Approved!");
            notif.setMessage("Your boarding advertisement '" + b.getName() + "' has been approved by admin and is now public.");
            notif.setRecipient(b.getOwner());
            notificationRepository.save(notif);

            return boardingService.getBoardingById(saved.getBoardingId());
        } catch (Exception e) {
            log.error("Error in approveBoarding() " + e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public BoardingDTO rejectBoarding(Long boardingId, String feedback) {
        log.info("Executing method rejectBoarding() for boardingId: {}", boardingId);
        try {
            Boarding b = boardingRepository.findById(boardingId)
                    .orElseThrow(() -> new CustomerException(404, "Boarding not found"));

            b.setStatus(BoardingStatus.REJECTED);
            b.setAdminFeedback(feedback);
            Boarding saved = boardingRepository.save(b);

            Notification notif = new Notification();
            notif.setTitle("Advertisement Rejected");
            notif.setMessage("Your advertisement '" + b.getName() + "' was rejected. Reason: " + feedback);
            notif.setRecipient(b.getOwner());
            notificationRepository.save(notif);

            return boardingService.getBoardingById(saved.getBoardingId());
        } catch (Exception e) {
            log.error("Error in rejectBoarding() " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<UserDTO> getAllUsers() {
        log.info("Executing method getAllUsers()");
        try {
            return userRepository.findAll().stream()
                    .map(u -> new UserDTO(u.getUserId(), u.getUserName(), u.getEmail(), u.getPhone(), null, u.getUserRoles(), u.getIsRestricted()))
                    .toList();
        } catch (Exception e) {
            log.error("Error in getAllUsers() " + e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public UserDTO toggleUserRestriction(Long userId, boolean restrict) {
        log.info("Executing method toggleUserRestriction() for userId: {}, restrict: {}", userId, restrict);
        try {
            User u = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomerException(404, "User not found"));

            u.setIsRestricted(restrict);
            User saved = userRepository.save(u);
            return new UserDTO(saved.getUserId(), saved.getUserName(), saved.getEmail(), saved.getPhone(), null, saved.getUserRoles(), saved.getIsRestricted());
        } catch (Exception e) {
            log.error("Error in toggleUserRestriction() " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<BoardingDTO> getAllPublishedBoardings() {
        log.info("Executing method getAllPublishedBoardings()");
        try {
            return boardingRepository.findAll().stream()
                    .map(b -> boardingService.getBoardingById(b.getBoardingId()))
                    .toList();
        } catch (Exception e) {
            log.error("Error in getAllPublishedBoardings() " + e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void deletePublishedBoarding(Long boardingId) {
        log.info("Executing method deletePublishedBoarding() for boardingId: {}", boardingId);
        try {
            Boarding b = boardingRepository.findById(boardingId)
                    .orElseThrow(() -> new CustomerException(404, "Boarding ad not found"));

            // Notify to  owner
            Notification notif = new Notification();
            notif.setTitle("Advertisement Deleted");
            notif.setMessage("Your advertisement '" + b.getName() + "' was deleted by system admin.");
            notif.setRecipient(b.getOwner());
            notificationRepository.save(notif);

            boardingService.deleteBoarding(boardingId);
        } catch (Exception e) {
            log.error("Error in deletePublishedBoarding() " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void createAdminUser(SignupRequestDTO dto) {
        log.info("Executing method createAdminUser()");
        try {
            dto.setUserRoles(UserRole.ADMIN);
            userService.saveUser(dto);
        } catch (Exception e) {
            log.error("Error in createAdminUser() " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Map<String, Object> getAdminDashboardStats() {
        log.info("Executing method getAdminDashboardStats()");
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", userRepository.countByUserRoles(UserRole.USER));
            stats.put("totalAdmins", userRepository.countByUserRoles(UserRole.ADMIN));
            stats.put("pendingApprovals", boardingRepository.countByStatus(BoardingStatus.PENDING_APPROVAL));
            stats.put("approvedBoardings", boardingRepository.countByStatus(BoardingStatus.APPROVED));
            stats.put("rejectedBoardings", boardingRepository.countByStatus(BoardingStatus.REJECTED));

            BigDecimal totalListingRevenue = paymentRepository.sumTotalListingRevenue();
            BigDecimal totalCommission = paymentRepository.sumTotalPlatformCommission();
            if (totalListingRevenue == null) totalListingRevenue = BigDecimal.ZERO;
            if (totalCommission == null) totalCommission = BigDecimal.ZERO;
            BigDecimal totalEarn = totalListingRevenue.add(totalCommission);

            stats.put("totalListingRevenue", totalListingRevenue);
            stats.put("totalCommission", totalCommission);
            stats.put("totalEarn", totalEarn);
            stats.put("totalRevenue", totalEarn);

            return stats;
        } catch (Exception e) {
            log.error("Error in getAdminDashboardStats() " + e.getMessage());
            throw e;
        }
    }
}
