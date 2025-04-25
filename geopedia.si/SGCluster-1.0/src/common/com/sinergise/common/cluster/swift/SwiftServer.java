package com.sinergise.common.cluster.swift;

import com.sinergise.common.util.naming.Identifier;
import com.sinergise.common.util.server.ClusterServer;

public class SwiftServer extends ClusterServer{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4694362584506234568L;

	public SwiftServer(Identifier identifier, String serverURL) {
		super(identifier, serverURL);
	}
	/** Serialization only **/
	@Deprecated
	protected SwiftServer() {
	}
	
	@Override
	public String getValidationRequestURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Status validateValidationResponse(String response) {
		// TODO Auto-generated method stub
		return null;
	}

}
