package com.customanalytics.customanalyticsrestapinew.contract.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

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
