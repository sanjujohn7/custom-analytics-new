package com.customanalytics.customanalyticsrestapinew.configuration;

import com.customanalytics.customanalyticsrestapinew.exception.UserNotFoundException;
import com.customanalytics.customanalyticsrestapinew.model.User;
import com.customanalytics.customanalyticsrestapinew.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserInfoUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UserNotFoundException {
        Optional<User> userOptional = userRepository.findByName(username);
        return userOptional.map(UserInfoUserDetails::new)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
