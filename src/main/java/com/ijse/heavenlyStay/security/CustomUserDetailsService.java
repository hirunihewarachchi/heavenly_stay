package com.ijse.heavenlyStay.security;

import com.ijse.heavenlyStay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<com.ijse.heavenlyStay.entity.User> optionalUser = userRepository.findByUserName(username);

        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        com.ijse.heavenlyStay.entity.User user = optionalUser.get();
        String roleStr = user.getUserRoles() != null ? user.getUserRoles().name() : "USER";

        return User.builder()
                .username(user.getUserName())
                .password(user.getPassword())
                .roles(roleStr)
                .build();
    }
}
