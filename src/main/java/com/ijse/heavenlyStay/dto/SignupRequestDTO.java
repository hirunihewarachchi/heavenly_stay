package com.ijse.heavenlyStay.dto;

import com.ijse.heavenlyStay.enumeration.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequestDTO {
    private String userName;
    private String email;
    private String phone;
    private String password;
    private UserRole userRoles;
}
