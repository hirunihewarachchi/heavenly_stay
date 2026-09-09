package com.ijse.heavenlyStay.controller;

import com.ijse.heavenlyStay.dto.AuthDTO;
import com.ijse.heavenlyStay.dto.CommonResponse;
import com.ijse.heavenlyStay.dto.UserDTO;
import com.ijse.heavenlyStay.dto.UserDataDTO;
import com.ijse.heavenlyStay.security.JwtUtil;
import com.ijse.heavenlyStay.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/login")
@CrossOrigin
@RequiredArgsConstructor

public class LogginController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping(value = "/testing")
    public String testSecurity(){
        return "API Security Successful";
    }
}
