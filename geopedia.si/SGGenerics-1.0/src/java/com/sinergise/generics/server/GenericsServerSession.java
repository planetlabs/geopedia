package com.sinergise.generics.server;


import javax.servlet.http.HttpServletRequest;

import com.sinergise.common.util.format.Locale;


public class GenericsServerSession {
	private static ThreadLocal<GenericsServerSession> threadSessions = new ThreadLocal<GenericsServerSession>();
	
	protected HttpServletRequest httpRequest = null;
	protected Locale locale=Locale.getDefault();
	
	public void initRequest(HttpServletRequest req) {
		synchronized(this) {
			this.httpRequest = req;
			threadSessions.set(this);
		}
	}
	
	public void releaseRequest(){
		synchronized(this) {
			this.httpRequest = null;
			threadSessions.remove();
		}
	}
	
	public static GenericsServerSession getLocal() {
	    return threadSessions.get();
	}
	
	
	public Locale updateLocale(String language) {
		this.locale = Locale.forName(language);
		return locale;
	}
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	public Locale getLocale() {
		return locale;
	}
	
	public HttpServletRequest getRequest() {
		return httpRequest;
	}
}
