package com.sinergise.common.cluster.swift;

import java.io.Serializable;

import com.sinergise.common.cluster.PasswordCredentials;
import com.sinergise.common.util.naming.Identifier;
import com.sinergise.common.util.server.ServersCluster;
import com.sinergise.common.util.server.ServersClusterMap;
import com.sinergise.common.util.string.StringUtil;

public class SwiftAccount implements Serializable{
	
	private static final long AUTH_TOKEN_VALIDITY = 1000*60*10; // valid for at least 10 minutes
	/**
	 * 
	 */
	private static final long serialVersionUID = -7570428062676093701L;
	
	protected Identifier swiftClusterIdentifier;
	protected transient ServersCluster swiftCluster;
	protected String accountToken;
	protected String accountName;
	protected transient String authToken;
	protected transient long lastAuthTokenChangeTS = 0;
	
	
	public SwiftAccount(ServersCluster swiftCluster, String accountName) {
		this(swiftCluster.getIdentifier(), accountName, null);
	}
	
	public SwiftAccount(ServersCluster swiftCluster, String accountName, String accountToken) {
		this(swiftCluster.getIdentifier(), accountName, accountToken);
	}
	public SwiftAccount(Identifier swiftClusterIdentifier, String accountName, String accountToken) {
		this.swiftClusterIdentifier = swiftClusterIdentifier;
		this.accountToken = accountToken;
		this.accountName = accountName;
	}

	
	/**Serialization only **/
	@Deprecated
	protected SwiftAccount() {		
	}
	
	public ServersCluster getCluster() {
		if (swiftCluster==null) {
			swiftCluster = ServersClusterMap.getServersCluster(swiftClusterIdentifier);
			if (swiftCluster==null) {
				throw new RuntimeException("Unable to find cluster for identifier: "+ swiftClusterIdentifier);
			}
		}
		return swiftCluster;
	}
	
	protected String getURL(String objectPath)  {
		return getCluster().getURL(accountToken+objectPath);
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
		lastAuthTokenChangeTS = System.currentTimeMillis();
	}

	public String getAuthToken() {
		return this.authToken;
	}
	
	public boolean hasCredentials() {
		if (getCredentials()==null)
			return false;
		return true;
	}
	
	public PasswordCredentials getCredentials() {
		return SwiftCredentialsProvider.getCredentialsForAccount(this);
	}

	
	public String getAccountName() {
		return accountName;
	}
	public String getAccountToken() {
		return accountToken;
	}

	public boolean hasAccountToken() {
		return !StringUtil.isNullOrEmpty(accountToken);
	}

	public void setAccountToken(String accountToken) {
		assert(accountToken.startsWith("/"));
		this.accountToken = accountToken;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((accountName == null) ? 0 : accountName.hashCode());
		result = prime
				* result
				+ ((swiftClusterIdentifier == null) ? 0
						: swiftClusterIdentifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SwiftAccount other = (SwiftAccount) obj;
		if (accountName == null) {
			if (other.accountName != null)
				return false;
		} else if (!accountName.equals(other.accountName))
			return false;
		if (swiftClusterIdentifier == null) {
			if (other.swiftClusterIdentifier != null)
				return false;
		} else if (!swiftClusterIdentifier.equals(other.swiftClusterIdentifier))
			return false;
		return true;
	}

	
	public boolean isAuthTokenValid() {
		if ((lastAuthTokenChangeTS+AUTH_TOKEN_VALIDITY)>System.currentTimeMillis())
			return true;
		return false;
	}
	
	
}
