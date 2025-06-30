package com.omori.taskmanagement.springboot.security.utils;

/**
 * Security related constants
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // Private constructor to prevent instantiation
    }

    /**
     * Token Prefix
     * We will use this prefix when parsing JWT Token
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * Authorization Prefix in HttpServletRequest
     * Authorization: <type> <credentials>
     * For Example : Authorization: Bearer YWxhZGxa1qea32GVuc2VzYW1l
     */
    public static final String HEADER_STRING = "Authorization";

    public static final String LOGIN_REQUEST_URI = "/login";

    public static final String REGISTRATION_REQUEST_URI = "/register";
}
