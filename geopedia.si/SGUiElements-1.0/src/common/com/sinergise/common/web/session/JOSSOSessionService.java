package com.sinergise.common.web.session;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sinergise.common.util.ServiceException;

@RemoteServiceRelativePath("sessionJosso")
public interface JOSSOSessionService extends SessionService {
	
	public static final String JSESSIONID = "JOSSO_SESSIONID";
	
	GetSessionActivityResponse getJOSSOSessionActivity(GetSessionActivityRequest request) throws ServiceException;
	
	public static class JossoApp extends App {
		public static synchronized JOSSOSessionServiceAsync createInstance() {
        	if (GWT.isClient()) {
        		return GWT.create(JOSSOSessionService.class);
            }
            return null;
        }
		
		public static void startSessionExpirationListenerTimer(final SessionExpiredListener listener) {
        	checkIfSessionExpired(null, listener);
        }
        
        private static void checkIfSessionExpired(GetSessionActivityResponse activity, final SessionExpiredListener listener) {
        	//check one second after it should expire
        	int delayMilis = activity != null ? activity.getTimeToExpiration()*1000 +1000 : 1;
        	
        	new Timer() {
				@Override
				public void run() {
					final String sessionID = Cookies.getCookie(JSESSIONID);
					if (sessionID==null) {
						listener.sessionExpired();
						return;
					}
					
					INSTANCE.getJOSSOSessionActivity(new GetSessionActivityRequest(sessionID), 
						new AsyncCallback<GetSessionActivityResponse>() {
			        		public void onSuccess(GetSessionActivityResponse response) {
			        			if (response.isExpired()) {
			        				listener.sessionExpired();
			        			} else {
			        				checkIfSessionExpired(response, listener);
			        			}
			        		}
			        		
			        		public void onFailure(Throwable caught) {
			        			logger.error("Error while getting session activity data: "+caught.getMessage(), caught);
			        		}
			        	});
				}
			}.schedule(delayMilis);
        }
        
	}
	
	public static JOSSOSessionServiceAsync INSTANCE = JossoApp.createInstance();

}
