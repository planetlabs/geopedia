package com.sinergise.java.web.session;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sinergise.common.util.ServiceException;
import com.sinergise.common.util.collections.safe.DefaultTypeSafeKey;
import com.sinergise.common.util.collections.safe.DefaultTypeSafeMap;
import com.sinergise.common.web.session.GetSessionActivityRequest;
import com.sinergise.common.web.session.GetSessionActivityResponse;
import com.sinergise.common.web.session.GetSessionValuesRequest;
import com.sinergise.common.web.session.GetSessionValuesResponse;
import com.sinergise.common.web.session.SendHeartbeatMsgRequest;
import com.sinergise.common.web.session.SendHeartbeatMsgResponse;
import com.sinergise.common.web.session.SessionService;
import com.sinergise.common.web.session.SessionVariable;
import com.sinergise.common.web.session.SessionVariableValue;
import com.sinergise.common.web.session.SetSessionValuesRequest;
import com.sinergise.common.web.session.SetSessionValuesResponse;
import com.sinergise.java.util.UtilJava;
import com.sinergise.java.web.ServletUtil;

/**
 * {@link SessionService} implementation. 
 * 
 * Register {@link SessionRegister} listener to use session activity methods.
 * 
 * @author tcerovski
 */

@SuppressWarnings("serial")
public class SessionServiceImpl extends RemoteServiceServlet implements SessionService {
	
	private static final String PARAM_ALLOWED_KEYS = "allowedKeys";
	private static final String PARAM_ALLOWED_KEYS_SEPARATOR_REGEX = ",";
	private static final String PARAM_KEY_LAST_HEARTBEAT = "LAST_HEARBEAT";

	private static final String PARAM_ALLOWED_REGEX = "allowedRegex";

	
	private final Set<String> allowedKeys = new HashSet<String>();
	private Pattern allowedRegex =  null;
	
	public SetSessionValuesResponse setSessionValues(SetSessionValuesRequest request) throws ServiceException {
		if (request.isEmpty()) {
			throw new ServiceException("Invalid SetSessionValuesRequest: "+request);
		}
		
		HttpSession httpSession = super.getThreadLocalRequest().getSession();
		
		List<SessionVariable<?>> keysSet = new ArrayList<SessionVariable<?>>(request.getValuesMap().size());
		
		for (DefaultTypeSafeKey<?> key : request.getValuesMap().keySet()) {
			@SuppressWarnings("unchecked")
			SessionVariable<SessionVariableValue> var = (SessionVariable<SessionVariableValue>)key;
			if (isAllowed(var.getKeyName())) {
				httpSession.setAttribute(var.getKeyName(), request.getSafe(var));
				keysSet.add(var);
			}
		}
		
		return new SetSessionValuesResponse(keysSet);
	}
	
	@SuppressWarnings("unchecked")
	public GetSessionValuesResponse getSessionValues(GetSessionValuesRequest request) throws ServiceException {
		if (request.isEmpty()) {
			throw new ServiceException("Invalid GetSessionValuesRequest: "+request);
		}
		
		HttpSession httpSession = super.getThreadLocalRequest().getSession();
		
		DefaultTypeSafeMap<SessionVariableValue> valuesMap = new DefaultTypeSafeMap<SessionVariableValue>();
		
		for (SessionVariable<?> var : request.getVariables()) {
			String key = var.getKeyName();
			if (isAllowed(key) && httpSession.getAttribute(key) instanceof SessionVariableValue) 
			{
				valuesMap.putSafe((SessionVariable<SessionVariableValue>)var, (SessionVariableValue)httpSession.getAttribute(key));
			}
		}
		
		return new GetSessionValuesResponse(valuesMap);
	}

	private boolean isAllowed(String key) {
		if (allowedRegex != null && allowedRegex.matcher(key).matches()) {
			return true;
		}
		return allowedKeys.contains(key);
	}
	
	public SendHeartbeatMsgResponse sendHeartbeatMsg(SendHeartbeatMsgRequest request) throws ServiceException {
		long t = System.currentTimeMillis();
		super.getThreadLocalRequest().getSession().setAttribute(PARAM_KEY_LAST_HEARTBEAT, String.valueOf(t));
		return new SendHeartbeatMsgResponse(t);
	}
	
	public GetSessionActivityResponse getSessionActivity(GetSessionActivityRequest request) throws ServiceException {
		HttpSession session = SessionRegister.getRegisteredSession(getServletContext(), request.getSessionID());
		
		if (session == null) {
			return new GetSessionActivityResponse(request.getSessionID(), true); //expired
		}
		
		boolean expired = (System.currentTimeMillis()-session.getLastAccessedTime())/1000 > session.getMaxInactiveInterval();
		GetSessionActivityResponse response = new GetSessionActivityResponse(request.getSessionID(), expired);
		response.setMaxInactiveInterval(session.getMaxInactiveInterval());
		response.setCreationTime(new Date(session.getCreationTime()));
		response.setLastAccessedTime(new Date(session.getLastAccessedTime()));
		
		return response;
	}
	
	@Override
	public void init() throws ServletException {
		super.init();
		UtilJava.initStaticUtils();
		
		String allowedKeysStr = ServletUtil.findInitParameter(this, PARAM_ALLOWED_KEYS);
		if (allowedKeysStr != null) {
			for (String allowedKey : allowedKeysStr.split(PARAM_ALLOWED_KEYS_SEPARATOR_REGEX)) {
				allowedKeys.add(allowedKey.trim());
			}
		}
		
		String regex = ServletUtil.findInitParameter(getServletConfig(), PARAM_ALLOWED_REGEX, null);
		allowedRegex = regex == null ? null : Pattern.compile(regex);
	}
	
}
