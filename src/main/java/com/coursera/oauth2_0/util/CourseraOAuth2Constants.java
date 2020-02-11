package com.coursera.oauth2_0.util;

public class CourseraOAuth2Constants {
    public static final String ACCESS_TOKEN_KEY = "access_token";
    public static final String REFRESH_TOKEN_KEY = "refresh_token";
    public static final String CLIENT_SECRET_KEY = "client_secret";
    public static final String CLIENT_ID_KEY = "client_id";
    public static final String CLIENT_APP_NAME = "client_app_name";
    public static final String GRANT_TYPE_KEY = "grant_type";
    public static final String CODE_KEY = "code";
    public static final String REDIRECT_URI_KEY = "redirect_uri";
    public static final String ACCESS_TYPE_KEY = "access_type";
    public static final String ACCESS_TYPE_VALUE = "offline";
    public static final String AUTHORIZATION_CODE_VALUE = "authorization_code";
    public static final String EXPIRES_IN = "expires_in";
    public static final String COURSERA_CODE_URI = "https://accounts.coursera.org/oauth2/v1/auth?scope=%s&redirect_uri=%s&access_type=offline&grant_type=authorization_code&response_type=code&client_id=%s";
    public static final String SCOPE_ACCESS_BUSINESS = "access_business_api";
    public static final String SCOPE_VIEW_PROFILE = "view_profile";
    public static final String SCOPE_PROFILE = "scope_profile";
    public static final String COURSERA_AUTH_TOKEN_URI = "https://accounts.coursera.org/oauth2/v1/token";
    public static final String COURSERA_CALLBACK_URI = "http://localhost:9876/callback?client_id=";
    public static final int PORT = 9876;
}