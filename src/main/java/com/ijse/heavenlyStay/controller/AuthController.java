package com.ijse.heavenlyStay.controller;

import com.ijse.heavenlyStay.dto.*;
import com.ijse.heavenlyStay.security.JwtUtil;
import com.ijse.heavenlyStay.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
//@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/v1/auth/signup")
    public CommonResponse signup(@RequestBody SignupRequestDTO signupDTO) {
        log.info("Attempting signup for username: {}", signupDTO.getUserName());
        userService.saveUser(signupDTO);
        return new CommonResponse(200, "User registered successfully");
    }

    @PostMapping({"/v1/auth/login", "/login/auth"})
    public CommonResponse login(@RequestBody LoginRequestDTO loginDTO) {
        log.info("Attempting login for username: {}", loginDTO.getUsername());
//        AuthResponseDTO response = userService.authenticate(loginDTO);

        UserDTO userDTO = userService.authenticate(loginDTO);
        String token = jwtUtil.generateToken(userDTO);

        UserDataDTO userDataDTO = new UserDataDTO();
        userDataDTO.setUserId(userDTO.getUserId());
        userDataDTO.setUserRoles(userDTO.getUserRoles());
        userDataDTO.setToken(token);


        return new CommonResponse(200, userDataDTO, "Login successful");
    }

}


