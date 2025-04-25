package com.sinergise.java.web.session;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionRegister implements HttpSessionListener {
	
	public static final String SESSION_ID_PREFIX = SessionRegister.class.getName()+".ID.";

	public void sessionCreated(HttpSessionEvent event) {
		HttpSession session = event.getSession();
		session.getServletContext().setAttribute(sessionKey(session.getId()), session);
	}

	public void sessionDestroyed(HttpSessionEvent event) {
		HttpSession session = event.getSession();
		session.getServletContext().setAttribute(sessionKey(session.getId()), null);
	}
	
	private static String sessionKey(String sessionID) {
		return SESSION_ID_PREFIX+sessionID;
	}
	
	public static HttpSession getRegisteredSession(ServletContext context, String sessionID) {
		Object o = context.getAttribute(sessionKey(sessionID));
		if (o instanceof HttpSession) return (HttpSession)o;
		return null;
	}

}
