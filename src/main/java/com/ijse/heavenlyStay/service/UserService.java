package com.ijse.heavenlyStay.service;

import com.ijse.heavenlyStay.dto.AuthResponseDTO;
import com.ijse.heavenlyStay.dto.LoginRequestDTO;
import com.ijse.heavenlyStay.dto.SignupRequestDTO;
import com.ijse.heavenlyStay.dto.UserDTO;

public interface UserService {
    UserDTO getUserDetails(String username, String password);
    void saveUser(SignupRequestDTO signupDTO);
    AuthResponseDTO authenticate(LoginRequestDTO loginDTO);
}
