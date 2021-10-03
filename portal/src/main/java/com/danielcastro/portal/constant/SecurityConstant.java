package com.danielcastro.portal.constant;

public class SecurityConstant {
public static final long EXPIRATION_TIME = 432_000_000;// 5 DAYS IN MS
public static final String TOKEN_PREFIX = "Bearer ";
public static final String JWT_TOKEN_HEADER = "Jwt-Token";
public static final String TOKEN_CANNOT_BE_VERIFIED = "Token cannot be verified";
public static final String FAKE_COMPANY_LLC = "Fake Company, LLC";
public static final String FAKE_COMPANY_ADMINISTRATION = "Fake Company Portal";
public static final String AUTHORITIES = "authorities";
public static final String FORBIDDEN_MESSAGE = "You need to log in to access this page";
public static final String ACCESS_DENIED_MESSAGE = "You do not have permission to access this page";
public static final String OPTIONS_HTTP_METHOD = "OPTIONS";
public static final String[] PUBLIC_URLS = {"/user/register", "/user/login", "/user/image/**"}; 
}
