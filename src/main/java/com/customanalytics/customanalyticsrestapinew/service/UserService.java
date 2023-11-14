package com.customanalytics.customanalyticsrestapinew.service;

import com.customanalytics.customanalyticsrestapinew.contract.UserRequest;
import com.customanalytics.customanalyticsrestapinew.contract.UserResponse;
import com.customanalytics.customanalyticsrestapinew.model.User;
import com.customanalytics.customanalyticsrestapinew.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse addUser(UserRequest userRequest) {
        User user = User.builder()
                .name(userRequest.getName())
                .email(userRequest.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .build();
        User saved = userRepository.save(user);
        return UserResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .password(saved.getPassword())
                .build();
    }
}
