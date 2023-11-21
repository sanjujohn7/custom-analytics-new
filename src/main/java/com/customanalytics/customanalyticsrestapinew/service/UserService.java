package com.customanalytics.customanalyticsrestapinew.service;

import com.customanalytics.customanalyticsrestapinew.contract.request.UserRequest;
import com.customanalytics.customanalyticsrestapinew.contract.response.UserResponse;
import com.customanalytics.customanalyticsrestapinew.exception.UserNotFoundException;
import com.customanalytics.customanalyticsrestapinew.model.User;
import com.customanalytics.customanalyticsrestapinew.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse addUser(UserRequest userRequest) {
    Optional<User> userOptional = userRepository.findByName(userRequest.getName().trim());
    if (userOptional != null && userOptional.isPresent()){
    throw new UserNotFoundException("User name already exists !");
    }
        User user =
                User.builder()
                        .name(userRequest.getName().trim())
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
