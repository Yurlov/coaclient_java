package com.coursera.oauth2_0.service;

import com.coursera.oauth2_0.util.CourseraOAuth2ServiceType;

public class CourseraOAuth2ServiceFactory {
    public static CourseraOAuth2Service getInstance(CourseraOAuth2ServiceType type) {
        if (type.equals(CourseraOAuth2ServiceType.FILE)) {
            return new FileOAuth2Service();
        }
        return null;
    }
}
