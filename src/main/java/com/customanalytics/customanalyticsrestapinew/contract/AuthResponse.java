package com.customanalytics.customanalyticsrestapinew.contract;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String name;
    private String token;
}
