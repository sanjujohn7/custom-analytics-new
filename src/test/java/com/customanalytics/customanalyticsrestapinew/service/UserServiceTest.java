package com.customanalytics.customanalyticsrestapinew.service;

import com.customanalytics.customanalyticsrestapinew.contract.request.UserRequest;
import com.customanalytics.customanalyticsrestapinew.contract.response.UserResponse;
import com.customanalytics.customanalyticsrestapinew.model.User;
import com.customanalytics.customanalyticsrestapinew.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class UserServiceTest {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;
    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        userRepository = Mockito.mock(UserRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        userService = new UserService(userRepository, passwordEncoder);
    }
    @Test
    public void testAddUser() {
        UserRequest request = new UserRequest("akhil", "akhil@gmail.com", "password");
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        UserResponse expectedResponse = UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                .build();

        when(userRepository.findByName(request.getName())).thenReturn(null);

        when(userRepository.save(any())).thenReturn(user);

        UserResponse actualResponse = userService.addUser(request);

        assertEquals(expectedResponse.getId(), actualResponse.getId());
        assertEquals(expectedResponse.getName(), actualResponse.getName());
        assertEquals(expectedResponse.getEmail(), actualResponse.getEmail());
        assertEquals(expectedResponse.getPassword(), actualResponse.getPassword());
    }
}
