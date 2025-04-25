package com.sinergise.common.cluster;

public class PasswordCredentials {
	private String username;
	private String password;
	
	public PasswordCredentials(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
}
