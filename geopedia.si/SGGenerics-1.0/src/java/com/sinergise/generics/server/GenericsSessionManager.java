package com.sinergise.generics.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class GenericsSessionManager {

	public static final String ATTR_GENERICS_SESSION = "GENERICS_SESSION";
	
	
	protected void validateSession(GenericsServerSession gSession, HttpServletRequest req) {
	}
	
	protected GenericsServerSession createSession (HttpServletRequest req) {
		GenericsServerSession  gSession =new GenericsServerSession();  // dummy session
		return gSession;
	}
	private GenericsServerSession getSessionFromHttpRequest(HttpServletRequest req ) {
		HttpSession httpSession = req.getSession(true);
		return (GenericsServerSession)httpSession.getAttribute(ATTR_GENERICS_SESSION);
	}
	
	
	public  GenericsServerSession getCurrentSession() {
    	return GenericsServerSession.getLocal();
    }
	public void preProcessRequest(HttpServletRequest req) {
		GenericsServerSession gSession = getSessionFromHttpRequest(req);
		if (gSession == null) {
			gSession = createSession(req);
			req.getSession().setAttribute(ATTR_GENERICS_SESSION, gSession);
		}
		
		validateSession(gSession, req);
		gSession.initRequest(req);
		
	}
	
	public void postProcessRequest(HttpServletRequest arg0) {
		GenericsServerSession gSession= getCurrentSession();
    	if (gSession!=null) gSession.releaseRequest();
	}
	
	
	
	
}
