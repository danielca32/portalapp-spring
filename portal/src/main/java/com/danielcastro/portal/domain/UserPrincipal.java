package com.danielcastro.portal.domain;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {
	private User user;

	public UserPrincipal(User user) {
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Arrays.stream(this.user.getAuthorities()).map(SimpleGrantedAuthority::new).collect(Collectors.toList()); //Makes a stream of the String Authorities and maps (uses the constructor of SimpleGrantedAuthorities)
		//To return SimpleGrantedAuthorities (which are an object that basically only has one field: A string) and returns a Collection of type java.util.List.
	}

	@Override
	public String getPassword() {
		return this.user.getPassword();
	}

	@Override
	public String getUsername() {
		return this.user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true; //Not used
	}

	@Override
	public boolean isAccountNonLocked() {
		return this.user.isNotLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true; //Not used
	}

	@Override
	public boolean isEnabled() {
		return this.user.isActive();
	}

}
