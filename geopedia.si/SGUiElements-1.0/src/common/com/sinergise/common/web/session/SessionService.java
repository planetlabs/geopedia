package com.sinergise.common.web.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.util.ServiceException;

@RemoteServiceRelativePath("session")
public interface SessionService extends RemoteService {
	
	public static final String JSESSIONID = "JSESSIONID";
	
	SetSessionValuesResponse setSessionValues(SetSessionValuesRequest request) throws ServiceException;
	
	GetSessionValuesResponse getSessionValues(GetSessionValuesRequest request) throws ServiceException;
	
	SendHeartbeatMsgResponse sendHeartbeatMsg(SendHeartbeatMsgRequest request) throws ServiceException;
	
	GetSessionActivityResponse getSessionActivity(GetSessionActivityRequest request) throws ServiceException;
	
	public static class App {
		
		protected static Logger logger = LoggerFactory.getLogger(SessionService.class);
		
        public static synchronized SessionServiceAsync createInstance() {
        	if (GWT.isClient()) {
        		return GWT.create(SessionService.class);
            }
            return null;
        }
        
        public static String getSessionID() {
    		return Cookies.getCookie(JSESSIONID);
    	}
        
        public static void startHeartbeatTimer(int periodMillis) {
        	new Timer() {
				@Override
				public void run() {
					INSTANCE.sendHeartbeatMsg(new SendHeartbeatMsgRequest(), new AsyncCallback<SendHeartbeatMsgResponse>() {
						public void onSuccess(SendHeartbeatMsgResponse result) {
							logger.trace("Heartbeat message successfully sent");
						}
						
						public void onFailure(Throwable caught) {
							logger.error("Error while sending heartbeat message: "+caught.getMessage(), caught);
						}
					});
				}
			}.scheduleRepeating(periodMillis);
        }
        
        public static void startSessionExpirationListenerTimer(final Action sessionExpiredAction) {
        	startSessionExpirationListenerTimer(new SessionExpiredListener() {
				@Override
				public void sessionExpired() {
					sessionExpiredAction.performAction();
				}
			});
        }
        
        public static void startSessionExpirationListenerTimer(SessionExpiredListener listener) {
        	checkIfSessionExpired(null, listener);
        }
        
        private static void checkIfSessionExpired(GetSessionActivityResponse activity, final SessionExpiredListener listener) {
        	//check one second after it should expire
        	int delayMilis = activity != null ? activity.getTimeToExpiration()*1000 +1000 : 1;
        	
        	new Timer() {
				@Override
				public void run() {
					final String sessionID = getSessionID();
					Cookies.removeCookie(JSESSIONID);
					
					INSTANCE.getSessionActivity(new GetSessionActivityRequest(sessionID), 
						new AsyncCallback<GetSessionActivityResponse>() {
			        		public void onSuccess(GetSessionActivityResponse response) {
		        				Cookies.setCookie(JSESSIONID, sessionID);
			        			if (response.isExpired()) {
			        				listener.sessionExpired();
			        			} else {
			        				checkIfSessionExpired(response, listener);
			        			}
			        		}
			        		
			        		public void onFailure(Throwable caught) {
			        			Cookies.setCookie(JSESSIONID, sessionID);
			        			logger.error("Error while getting session activity data: "+caught.getMessage(), caught);
			        		}
			        	});
    				Cookies.setCookie(JSESSIONID, sessionID);
				}
			}.schedule(delayMilis);
        }
        
    }
	
	public static SessionServiceAsync INSTANCE = App.createInstance();

}
