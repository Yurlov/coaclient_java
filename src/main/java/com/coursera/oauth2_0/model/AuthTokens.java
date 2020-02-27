package com.coursera.oauth2_0.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AuthTokens {
    private String refreshToken;
    private String accessToken;
    private String expiredIn;
}
