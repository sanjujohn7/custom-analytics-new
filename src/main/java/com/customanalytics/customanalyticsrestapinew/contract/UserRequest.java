package com.customanalytics.customanalyticsrestapinew.contract;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    private String name;
    private String email;
    private String password;
}
