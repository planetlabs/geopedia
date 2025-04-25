package com.sinergise.common.cluster.swift;

import java.io.Serializable;

import com.sinergise.common.util.server.IsObjectURLProvider;

public class SwiftObjectURLProvider implements IsObjectURLProvider, Serializable {
	private static final long serialVersionUID = -3101759939222274278L;
	
	protected SwiftAccount account;

	public SwiftObjectURLProvider (SwiftAccount account) {
		this.account=account;
	}
	/** Serialization only **/
	@Deprecated
	protected SwiftObjectURLProvider() {
	}
	
	
	public SwiftAccount getAccount() {
		return account;
	}

	@Override
	public String getObjectURL(String objectName) {
		if (objectName==null) return null;		
		StringBuilder builder = new StringBuilder();
		if (!objectName.startsWith("/")) {
			builder.append('/');
		}
		builder.append(objectName);		
		return  account.getURL(builder.toString());
		
	}

}
