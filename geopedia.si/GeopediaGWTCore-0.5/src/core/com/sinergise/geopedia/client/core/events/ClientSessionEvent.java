package com.sinergise.geopedia.client.core.events;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.sinergise.geopedia.core.entities.User;

public class ClientSessionEvent extends Event<ClientSessionEvent.Handler> {

	private enum LoginEventType {AUTOLOGIN, LOGIN, LOGOUT, SESSION_CHANGED};
	
	public interface Handler {
		void onAutoLoginEvent(ClientSessionEvent event);
		void onLogin(ClientSessionEvent event);
		void onLogout(ClientSessionEvent event);
		void onSessionChanged(ClientSessionEvent event);
	}

	private String parameterString = null;
	private LoginEventType type;
	private String sessionValue = null;
	private User user=null;
	
	public static Event<?> createOnLogoutEvent(User user) {
		ClientSessionEvent ule =  new ClientSessionEvent(LoginEventType.LOGOUT);
		ule.user=user;
		return ule;
		
	}
	public static Event<?> createOnLoginEvent(User user) {
		ClientSessionEvent ule =  new ClientSessionEvent(LoginEventType.LOGIN);
		ule.user=user;
		return ule;
	}
	
	
	public static Event<?> createSessionChangedEvent(User user, String sessionValue) {
		ClientSessionEvent ule =  new ClientSessionEvent(LoginEventType.SESSION_CHANGED);
		ule.sessionValue = sessionValue;
		return ule;
	}
	

	public static ClientSessionEvent createAutologinEvent(String parameterString) {
		ClientSessionEvent ule =  new ClientSessionEvent(LoginEventType.AUTOLOGIN);
		ule.parameterString = parameterString;
		return ule;
	}
	
	
	
	
	private ClientSessionEvent(LoginEventType type) {
		this.type=type;
	}
	
	private static final Type<ClientSessionEvent.Handler> TYPE =
		        new Type<ClientSessionEvent.Handler>();
	
	public static HandlerRegistration register(EventBus eventBus, ClientSessionEvent.Handler handler) {
		return eventBus.addHandler(TYPE, handler);
	} 

	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		switch (type) {
		case AUTOLOGIN:
			handler.onAutoLoginEvent(this);
			break;
		case LOGIN:
			handler.onLogin(this);
			break;
		case LOGOUT:
			handler.onLogout(this);
			break;
		case SESSION_CHANGED:
			handler.onSessionChanged(this);
			break;
		}
		
				
	}
	
	public String getParameterString() {
		return parameterString;
	}
	public String getSessionValue() {
		return sessionValue;
	}
}