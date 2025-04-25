package com.sinergise.common.util.server;

import com.sinergise.common.util.naming.Identifier;

public class ClusterPlainHTTPServer extends ClusterServer {

	private static final long serialVersionUID = 1L;

	/*serialization*/
	@Deprecated
	protected ClusterPlainHTTPServer() {
	}
	
	public ClusterPlainHTTPServer(Identifier identifier, String serverURL) {
		super(identifier, serverURL);
	}
	
	@Override
	public String getValidationRequestURL() {
		return null;
	}

	@Override
	public Status validateValidationResponse(String response) {
		return null;
	}

}
