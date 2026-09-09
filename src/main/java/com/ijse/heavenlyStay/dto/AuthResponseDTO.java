package com.ijse.heavenlyStay.dto;

import com.ijse.heavenlyStay.enumeration.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {
    private String token;
    private Long userId;
    private String username;
    private String email;
    private UserRole role;
}
