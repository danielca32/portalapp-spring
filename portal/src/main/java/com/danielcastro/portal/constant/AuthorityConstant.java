package com.danielcastro.portal.constant;

public class AuthorityConstant {
	public static final String[] USER_AUTHORITIES = {"user_read"};
	public static final String[] HR_AUTHORITIES = {"user_read", "user_update"};
	public static final String[] MANAGER_AUTHORITIES = {"user_read", "user_update"};
	public static final String[] ADMIN_AUTHORITIES = {"user_read", "user_create", "user_update"};
	public static final String[] SUPER_ADMIN_AUTHORITIES = {"user_read", "user_create", "user_update", "user_delete"};


}
