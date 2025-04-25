package com.sinergise.geopedia.server;

import javax.servlet.http.HttpServletRequest;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.core.entities.User;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.java.util.UtilJava;

public class PediaRemoteServiceServlet extends RemoteServiceServlet
{
	private static final long serialVersionUID = 6037790220076953559L;

	public static final String REQUEST_SESSION ="reqSession"; 

	static {
		UtilJava.initStaticUtils();
	}
	
	protected Session defaultSession = null;
	
	public PediaRemoteServiceServlet(Session defaultSession) {
		this.defaultSession = defaultSession;
	}
	public PediaRemoteServiceServlet() {
		this.defaultSession = null;
	}
	
	
	protected Session ensureSession() throws GeopediaException {
		Session sess = getThreadLocalSession();
		if (sess==null) 
			throw new GeopediaException(GeopediaException.Type.NO_SESSION);
		return sess;
	}
	
	protected Session ensureLoggedInUser() throws GeopediaException {
		Session sess = ensureSession();
		if (sess.getUser()==null || User.NO_USER.equals(sess.getUser()))
			throw new GeopediaException(GeopediaException.Type.NOT_LOGGED_IN);
		return sess;
	}
	
	protected void onAfterResponseSerialized(String arg0)
	{
		Session sess = getThreadLocalSession();
		if (sess==null)
			return;		
		ServUtil.setSessionHeader(sess, getThreadLocalResponse());
	}
	
	
	
	protected void onBeforeRequestDeserialized(String serializedRequest) {
		extractAndLoadSession(getThreadLocalRequest());		  
	}
	
	
	protected void setThreadLocalSession(Session sess) {
		getThreadLocalRequest().setAttribute(REQUEST_SESSION, sess);
	}

	protected Session getThreadLocalSession() {
		if (defaultSession!=null)
			return defaultSession;
		return (Session)getThreadLocalRequest().getAttribute(REQUEST_SESSION);
	}

	protected void extractAndLoadSession(HttpServletRequest req) {
		try {
		Session sess = ServUtil.extractSession(req);
		setThreadLocalSession(sess);
		} catch (GeopediaException ex) {
			//silently ignore 
		}
	}
	
	@Override
	protected void checkPermutationStrongName() throws SecurityException {
		// TODO Remove this when X-GWT-PERMUTATION issue in firefox is fixed...
		super.checkPermutationStrongName();
	}
		
}
