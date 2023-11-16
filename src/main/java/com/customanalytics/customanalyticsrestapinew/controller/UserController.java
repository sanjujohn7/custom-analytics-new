package com.customanalytics.customanalyticsrestapinew.controller;

import com.customanalytics.customanalyticsrestapinew.contract.request.AuthRequest;
import com.customanalytics.customanalyticsrestapinew.contract.request.UserRequest;
import com.customanalytics.customanalyticsrestapinew.contract.response.AuthResponse;
import com.customanalytics.customanalyticsrestapinew.contract.response.UserResponse;
import com.customanalytics.customanalyticsrestapinew.exception.UserNotFoundException;
import com.customanalytics.customanalyticsrestapinew.service.JwtService;
import com.customanalytics.customanalyticsrestapinew.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/custom-analytics")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/sign-up")
    public ResponseEntity<UserResponse> addUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse response = userService.addUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateAndGetToken(
            @Valid @RequestBody AuthRequest authRequest) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                authRequest.getName(), authRequest.getPassword()));
        if (authentication.isAuthenticated()) {
            AuthResponse response = jwtService.generateToken(authRequest.getName());
            return ResponseEntity.ok(response);
        } else {
            throw new UserNotFoundException("Invalid user request !");
        }
    }
}
