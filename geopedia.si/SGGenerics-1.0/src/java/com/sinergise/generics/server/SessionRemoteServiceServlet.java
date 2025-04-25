package com.sinergise.generics.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public abstract class SessionRemoteServiceServlet extends RemoteServiceServlet{
	
	private static final long serialVersionUID = 4741468434800395526L;
	protected final GenericsSessionManager sessionManager = createSessionManager();
	
	
	
	protected GenericsSessionManager createSessionManager() {
		return new GenericsSessionManager();
	}
	
	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
		sessionManager.preProcessRequest(arg0);
		try {
			super.service(arg0, arg1);
		} finally {
			sessionManager.postProcessRequest(arg0);
		}
	}

	@Override
	protected void checkPermutationStrongName() throws SecurityException {
		return;
	}
}
