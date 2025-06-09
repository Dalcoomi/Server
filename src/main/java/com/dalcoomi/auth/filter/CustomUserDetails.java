package com.dalcoomi.auth.filter;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;

public class CustomUserDetails implements UserDetails {

	@Getter
	private final Long memberId;
	private final String username;
	private final Collection<? extends GrantedAuthority> authorities;

	public CustomUserDetails(Long memberId, String username, Collection<? extends GrantedAuthority> authorities) {
		this.memberId = memberId;
		this.username = username;
		this.authorities = authorities;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {
		return username;
	}
}
