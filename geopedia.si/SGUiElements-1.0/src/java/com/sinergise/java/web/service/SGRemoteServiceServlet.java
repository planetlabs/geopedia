package com.sinergise.java.web.service;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;

public class SGRemoteServiceServlet  extends RemoteServiceServlet {
	private static final long serialVersionUID = -1983876812847594361L;

	/**
	 * Attempt to load the RPC serialization policy normally. If it isn't found,
	 * try loading it using the context path instead of the URL.
	 */
	@Override
	protected SerializationPolicy doGetSerializationPolicy(HttpServletRequest request, String moduleBaseURL, String strongName) {
		SerializationPolicy policy = super.doGetSerializationPolicy(request, moduleBaseURL, strongName);
		if (policy == null) {
			return loadSerializationPolicyFixContext(request, moduleBaseURL, strongName);
		} 
		return policy;
	}

	/**
	 * Load the RPC serialization policy via the context path.
	 */
	private SerializationPolicy loadSerializationPolicyFixContext(HttpServletRequest request, String moduleBaseURL, String strongName) {
		try {
			// The request can tell you the path of the web app relative to the
			// container root.
			String contextPath = request.getContextPath();
			if (!contextPath.startsWith("/")) {
				contextPath = "/" + contextPath;
			}
			String modulePath = new URL(moduleBaseURL).getPath();
			String newModuleBaseURL = "http://dummy"+contextPath + modulePath.substring(modulePath.lastIndexOf('/', modulePath.length()-2));
	    	return super.doGetSerializationPolicy(request, newModuleBaseURL, strongName);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	    
	}

}
