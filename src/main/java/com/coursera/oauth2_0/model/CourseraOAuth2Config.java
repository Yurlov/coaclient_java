package com.coursera.oauth2_0.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class CourseraOAuth2Config {
    private String clientName;
    private String clientId;
    private String clientSecretKey;
    private String clientScope;
}
