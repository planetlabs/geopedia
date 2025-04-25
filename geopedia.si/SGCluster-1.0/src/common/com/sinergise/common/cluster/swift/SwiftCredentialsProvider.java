package com.sinergise.common.cluster.swift;

import java.util.HashMap;

import com.sinergise.common.cluster.PasswordCredentials;

public abstract class SwiftCredentialsProvider {
	private static SwiftCredentialsProvider INSTANCE = null;
	
	protected abstract PasswordCredentials internalGetCredentialsForAccount (SwiftAccount account);
	
	
	public static SwiftCredentialsProvider initialize (SwiftCredentialsProvider instance) {
		if (INSTANCE == null) {
			INSTANCE = instance;
		}
		return INSTANCE;
	}
	
	public static PasswordCredentials getCredentialsForAccount(SwiftAccount account) {
		return getInstance().internalGetCredentialsForAccount(account);
	}
	
	private static SwiftCredentialsProvider getInstance() {
		if (INSTANCE == null) {
			throw new RuntimeException("SwiftCredentialsProvider not initialized. Did you forget to initialize it?");
		}
		return INSTANCE;
	}
	
	
	public static class Map extends SwiftCredentialsProvider {

		private HashMap<SwiftAccount, PasswordCredentials> credentialsMap = new HashMap<SwiftAccount, PasswordCredentials>();
		
		public void addCredentials(SwiftAccount account, PasswordCredentials credentials) {
			credentialsMap.put(account, credentials);
		}
		
		@Override
		protected PasswordCredentials internalGetCredentialsForAccount(
				SwiftAccount account) {
			return credentialsMap.get(account);
		}
		
	}
}
