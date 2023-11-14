package com.customanalytics.customanalyticsrestapinew.contract;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthRequest {
    private String name;
    private String password;
}
