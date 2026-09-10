package com.ijse.heavenlyStay.service.impl;

import com.ijse.heavenlyStay.dto.AuthResponseDTO;
import com.ijse.heavenlyStay.dto.LoginRequestDTO;
import com.ijse.heavenlyStay.dto.SignupRequestDTO;
import com.ijse.heavenlyStay.dto.UserDTO;
import com.ijse.heavenlyStay.entity.User;
import com.ijse.heavenlyStay.enumeration.UserRole;
import com.ijse.heavenlyStay.exception.CustomerException;
import com.ijse.heavenlyStay.repository.UserRepository;
import com.ijse.heavenlyStay.security.JwtUtil;
import com.ijse.heavenlyStay.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserDTO getUserDetails(String username, String password) {
        log.info("Executing method getUserDetails() for username: {}", username);
        try {
            Optional<User> optionalUser = userRepository.findByUserName(username);
            if (optionalUser.isEmpty()) {
                throw new CustomerException(404, "User not found");
            }
            User user = optionalUser.get();
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new CustomerException(401, "Invalid password");
            }
            return new UserDTO(user.getUserId(), user.getUserName(), user.getPassword(), user.getUserRoles());
        } catch (Exception e) {
            log.error("Error in getUserDetails() " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void saveUser(SignupRequestDTO signupDTO) {
        log.info("Executing method saveUser()");
        try {
            if (signupDTO.getUserName() == null || signupDTO.getUserName().trim().isEmpty()) {
    log.warn("Signup attempt with missing username");
    throw new CustomerException(400, "Username is required");
}
if (signupDTO.getPassword() == null || signupDTO.getPassword().trim().isEmpty()) {
    log.warn("Signup attempt with missing password for username: {}", signupDTO.getUserName());
    throw new CustomerException(400, "Password is required");
}
if (userRepository.findByUserName(signupDTO.getUserName()).isPresent()) {
    log.warn("Signup attempt with existing username: {}", signupDTO.getUserName());
    throw new CustomerException(409, "Username already exists");
}
            if (signupDTO.getEmail() != null && userRepository.findByEmail(signupDTO.getEmail()).isPresent()) {
                throw new CustomerException(409, "Email already registered");
            }

            User user = new User();
            user.setUserName(signupDTO.getUserName().trim());
            user.setEmail(signupDTO.getEmail() != null ? signupDTO.getEmail().trim() : signupDTO.getUserName() + "@heavenlystay.lk");
            user.setPhone(signupDTO.getPhone() != null ? signupDTO.getPhone().trim() : "0770000000");
            user.setPassword(passwordEncoder.encode(signupDTO.getPassword().trim()));
            user.setUserRoles(signupDTO.getUserRoles() != null ? signupDTO.getUserRoles() : UserRole.USER);
            user.setIsRestricted(false);

            userRepository.save(user);
        } catch (Exception e) {
            log.error("Error in saveUser() " + e.getMessage());
            throw e;
        }
    }

    @Override
    public UserDTO authenticate(LoginRequestDTO loginDTO) {
        log.info("Executing method authenticate() for username: {}", loginDTO.getUsername());
        try {
            Optional<User> optionalUser = userRepository.findByUserName(loginDTO.getUsername());
            if (optionalUser.isEmpty()) {
                throw new CustomerException(401, "Username or password incorrect, try again.");
            }
            User user = optionalUser.get();

            if (user.getIsRestricted() != null && user.getIsRestricted()) {
                throw new CustomerException(403, "Your account has been restricted by an administrator.");
            }

            if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
                throw new CustomerException(401, "Username or password incorrect, try again.");
            }

            // Validate role requested matches user''s role
            if (loginDTO.getRole() != null && !user.getUserRoles().equals(loginDTO.getRole())) {
                throw new CustomerException(403, "Role mismatch: Account is registered as " + user.getUserRoles());
            }

            return new UserDTO(user.getUserId(), user.getUserName(), user.getPassword(), user.getUserRoles());

//            UserDTO userDTO = new UserDTO(user.getUserId(), user.getUserName(), user.getPassword(), user.getUserRoles());
//            String token = jwtUtil.generateToken(userDTO);

//            return new AuthResponseDTO(token, user.getUserId(), user.getUserName(), user.getEmail(), user.getUserRoles());
        } catch (Exception e) {
            log.error("Error in authenticate() " + e.getMessage());
            throw e;
        }
    }
}
