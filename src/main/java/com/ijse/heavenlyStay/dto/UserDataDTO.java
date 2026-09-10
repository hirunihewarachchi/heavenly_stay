package com.ijse.heavenlyStay.dto;

import com.ijse.heavenlyStay.enumeration.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDataDTO {

    private long userId;
    private String token;
    private UserRole userRoles;
}
