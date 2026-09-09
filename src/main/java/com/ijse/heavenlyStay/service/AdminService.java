package com.ijse.heavenlyStay.service;

import com.ijse.heavenlyStay.dto.BoardingDTO;
import com.ijse.heavenlyStay.dto.SignupRequestDTO;
import com.ijse.heavenlyStay.dto.UserDTO;

import java.util.List;
import java.util.Map;

public interface AdminService {
    List<BoardingDTO> getPendingBoardings();
    BoardingDTO approveBoarding(Long boardingId);
    BoardingDTO rejectBoarding(Long boardingId, String feedback);
    List<UserDTO> getAllUsers();
    UserDTO toggleUserRestriction(Long userId, boolean restrict);
    List<BoardingDTO> getAllPublishedBoardings();
    void deletePublishedBoarding(Long boardingId);
    void createAdminUser(SignupRequestDTO dto);
    Map<String, Object> getAdminDashboardStats();
}
