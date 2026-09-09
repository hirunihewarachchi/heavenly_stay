package com.ijse.heavenlyStay.controller;

import com.ijse.heavenlyStay.dto.AuthResponseDTO;
import com.ijse.heavenlyStay.dto.CommonResponse;
import com.ijse.heavenlyStay.dto.LoginRequestDTO;
import com.ijse.heavenlyStay.dto.SignupRequestDTO;
import com.ijse.heavenlyStay.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/test")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/save-user")
    public CommonResponse saveUser(@RequestBody SignupRequestDTO userDTO) {
        userService.saveUser(userDTO);
        return new CommonResponse(200, "User Saved Successfully");
    }

    @PostMapping("/login")
    public CommonResponse loginUser(@RequestBody LoginRequestDTO loginDTO) {
        AuthResponseDTO auth = userService.authenticate(loginDTO);
        return new CommonResponse(200, auth, "Login Successful");
    }
}
