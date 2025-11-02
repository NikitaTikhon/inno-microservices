package com.innowise.orderservice.config;

public class SecurityConstant {

    private SecurityConstant() {}

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_BEARER_PREFIX = "Bearer ";
    public static final int AUTHORIZATION_TOKEN_POSITION = 7;

    public static final String TOKEN_CLAIM_USER_ID = "userId";
    public static final String TOKEN_CLAIM_ROLES = "roles";

}
