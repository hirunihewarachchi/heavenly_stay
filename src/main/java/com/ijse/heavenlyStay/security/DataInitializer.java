package com.ijse.heavenlyStay.security;

import com.ijse.heavenlyStay.entity.User;
import com.ijse.heavenlyStay.enumeration.UserRole;
import com.ijse.heavenlyStay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        if (userRepository.count() == 0) {

//            User superAdmin = User.builder()
//                    .userId("SA001")
//                    .userName("superadmin")
//                    .password(passwordEncoder.encode("Admin@1234"))
//                    .userRole(UserRole.ADMIN)
//                    .build();
//
//            userRepository.save(superAdmin);

            User user = new User();
            user.setUserName("admin");
            user.setEmail("hiru@heavenlystay.lk");
            user.setPhone("0770000000");
            user.setPassword(passwordEncoder.encode("admin123"));
            user.setUserRoles(UserRole.ADMIN);
            user.setIsRestricted(false);

            userRepository.save(user);
            log.info("Super Admin created successfully!");
        } else {
            log.info("Users already exist in database. Skipping Super Admin initialization.");
        }
    }
}