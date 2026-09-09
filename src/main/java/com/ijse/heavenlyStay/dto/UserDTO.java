package com.ijse.heavenlyStay.dto;

import com.ijse.heavenlyStay.enumeration.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long userId;
    private String userName;
    private String email;
    private String phone;
    private String password;
    private UserRole userRoles;
    private Boolean isRestricted;

    public UserDTO(Long userId, String userName, String password, UserRole userRoles) {
        this.userId = userId;
        this.userName = userName;
        this.password = password;
        this.userRoles = userRoles;
    }
}
