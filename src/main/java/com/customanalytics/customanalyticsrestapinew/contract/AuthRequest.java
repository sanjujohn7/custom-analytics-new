package com.customanalytics.customanalyticsrestapinew.contract;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthRequest {
    @NotEmpty(message = "Name should not be empty")
    private String name;

    @NotEmpty(message = "Password should not be empty")
    private String password;
}
