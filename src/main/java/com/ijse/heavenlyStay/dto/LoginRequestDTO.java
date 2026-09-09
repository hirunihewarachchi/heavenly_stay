package com.ijse.heavenlyStay.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.ijse.heavenlyStay.enumeration.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDTO {

    @JsonAlias({"userName", "username", "userNameTxt"})
    private String username;

    private String password;

    private UserRole role;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }
}
