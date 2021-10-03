package com.danielcastro.portal.enumeration;
import com.danielcastro.portal.constant.AuthorityConstant;

public enum Role {
	ROLE_USER(AuthorityConstant.USER_AUTHORITIES),
	ROLE_HR(AuthorityConstant.HR_AUTHORITIES),
	ROLE_MANAGER(AuthorityConstant.MANAGER_AUTHORITIES),
	ROLE_ADMIN(AuthorityConstant.ADMIN_AUTHORITIES),
	ROLE_SUPER_ADMIN(AuthorityConstant.SUPER_ADMIN_AUTHORITIES);

	private String[] authorities;
	
	Role(String... authorities) {
	this.authorities = authorities;
	}
	
	public String[] getAuthorities() {
		return authorities;
	}

}
