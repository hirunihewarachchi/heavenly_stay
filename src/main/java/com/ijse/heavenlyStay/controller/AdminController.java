package com.ijse.heavenlyStay.controller;

import com.ijse.heavenlyStay.dto.BoardingDTO;
import com.ijse.heavenlyStay.dto.CommonResponse;
import com.ijse.heavenlyStay.dto.SignupRequestDTO;
import com.ijse.heavenlyStay.dto.UserDTO;
import com.ijse.heavenlyStay.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public CommonResponse getStats() {
        Map<String, Object> stats = adminService.getAdminDashboardStats();
        return new CommonResponse(200, stats, "Dashboard stats fetched");
    }

    @GetMapping("/pending-boardings")
    public CommonResponse getPendingBoardings() {
        List<BoardingDTO> list = adminService.getPendingBoardings();
        return new CommonResponse(200, list, "Pending boardings fetched");
    }

    @PutMapping("/approve-boarding/{id}")
    public CommonResponse approveBoarding(@PathVariable Long id) {
        BoardingDTO dto = adminService.approveBoarding(id);
        return new CommonResponse(200, dto, "Boarding approved successfully");
    }

    @PutMapping("/reject-boarding/{id}")
    public CommonResponse rejectBoarding(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Does not meet guidelines") String feedback) {
        BoardingDTO dto = adminService.rejectBoarding(id, feedback);
        return new CommonResponse(200, dto, "Boarding rejected with feedback");
    }

    @GetMapping("/users")
    public CommonResponse getAllUsers() {
        List<UserDTO> users = adminService.getAllUsers();
        return new CommonResponse(200, users, "Users list fetched");
    }

    @PutMapping("/users/{userId}/restrict")
    public CommonResponse restrictUser(
            @PathVariable Long userId,
            @RequestParam boolean restrict) {
        UserDTO u = adminService.toggleUserRestriction(userId, restrict);
        return new CommonResponse(200, u, "User restriction status updated");
    }

    @GetMapping("/all-boardings")
    public CommonResponse getAllPublishedBoardings() {
        List<BoardingDTO> list = adminService.getAllPublishedBoardings();
        return new CommonResponse(200, list, "All boardings fetched");
    }

    @DeleteMapping("/boardings/{id}")
    public CommonResponse deleteBoarding(@PathVariable Long id) {
        adminService.deletePublishedBoarding(id);
        return new CommonResponse(200, "Boarding ad deleted successfully");
    }

    @PostMapping("/add-admin")
    public CommonResponse addAdmin(@RequestBody SignupRequestDTO dto) {
        adminService.createAdminUser(dto);
        return new CommonResponse(200, "New Admin created successfully");
    }
}
