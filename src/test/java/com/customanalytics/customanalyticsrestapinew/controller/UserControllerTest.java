package com.customanalytics.customanalyticsrestapinew.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.customanalytics.customanalyticsrestapinew.contract.request.AuthRequest;
import com.customanalytics.customanalyticsrestapinew.contract.request.UserRequest;
import com.customanalytics.customanalyticsrestapinew.contract.response.AuthResponse;
import com.customanalytics.customanalyticsrestapinew.contract.response.UserResponse;
import com.customanalytics.customanalyticsrestapinew.service.JwtService;
import com.customanalytics.customanalyticsrestapinew.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
public class UserControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private UserService userService;
    @MockBean private JwtService jwtService;
    @MockBean private AuthenticationManager authenticationManager;
    @MockBean private PasswordEncoder passwordEncoder;

    @Test
    public void testAddUser() throws Exception {
        UserRequest userRequest =
                UserRequest.builder().name("test").email("test@gmail.com").password("pass").build();
        UserResponse response =
                UserResponse.builder()
                        .id(1L)
                        .name("test")
                        .email("test@gmail.com")
                        .password("pass")
                        .build();

        when(userService.addUser(userRequest)).thenReturn(response);
        String path = "/custom-analytics/sign-up";
        mockMvc.perform(
                        post(path)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(userRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void authenticateAndGetToken_ValidAuthentication_ReturnsToken() throws Exception {

        AuthRequest authRequest = new AuthRequest("username", "password");
        Authentication authenticated = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticated);
        when(authenticated.isAuthenticated()).thenReturn(true);
        when(jwtService.generateToken("username")).thenReturn(new AuthResponse());

        mockMvc.perform(post("/custom-analytics/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

    }

    @Test
    void authenticateAndGetToken_InvalidAuthentication_ThrowsException() throws Exception {

        AuthRequest authRequest = new AuthRequest("username", "password");
        Authentication authenticated = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticated);
        when(authenticated.isAuthenticated()).thenReturn(false);

        mockMvc.perform(post("/custom-analytics/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest());
    }
}
