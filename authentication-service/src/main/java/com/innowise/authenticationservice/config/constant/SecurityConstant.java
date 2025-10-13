package com.innowise.authenticationservice.config.constant;

public class SecurityConstant {

    private SecurityConstant() {}

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_BEARER_PREFIX = "Bearer ";
    public static final int AUTHORIZATION_TOKEN_POSITION = 7;

    public static final String TOKEN_CLAIM_TYPE = "type";
    public static final String TOKEN_CLAIM_USER_ID = "userId";
    public static final String TOKEN_CLAIM_ROLES = "roles";

    public static final String TOKEN_ACCESS_TYPE = "access";
    public static final String TOKEN_REFRESH_TYPE = "refresh";

}
