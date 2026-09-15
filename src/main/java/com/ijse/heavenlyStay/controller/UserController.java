package com.ijse.heavenlyStay.controller;

import com.ijse.heavenlyStay.dto.*;
import com.ijse.heavenlyStay.security.JwtUtil;
import com.ijse.heavenlyStay.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/test")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/save-user")
    public CommonResponse saveUser(@RequestBody SignupRequestDTO userDTO) {
        userService.saveUser(userDTO);
        return new CommonResponse(200, "User Saved Successfully");
    }

//    @PostMapping("/login")
//    public CommonResponse loginUser(@RequestBody LoginRequestDTO loginDTO) {
//        UserDTO userDTO = userService.authenticate(loginDTO);
//        String token = jwtUtil.generateToken(userDTO);
//
//        UserDataDTO userDataDTO = new UserDataDTO();
//        userDataDTO.setUserId(userDTO.getUserId());
//        userDataDTO.setToken(token);
//        userDataDTO.setUserRoles(userDTO.getUserRoles());
//
//        return new CommonResponse(200, userDataDTO, "Login Successful");
//    }
}
