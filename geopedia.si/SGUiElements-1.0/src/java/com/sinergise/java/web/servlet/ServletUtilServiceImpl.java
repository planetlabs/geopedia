package com.sinergise.java.web.servlet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sinergise.common.util.ServiceException;
import com.sinergise.common.web.servlet.GetServletInitParamsRequest;
import com.sinergise.common.web.servlet.GetServletInitParamsResponse;
import com.sinergise.common.web.servlet.ServletUtilService;
import com.sinergise.java.util.UtilJava;
import com.sinergise.java.web.ServletUtil;

@SuppressWarnings("serial")
public class ServletUtilServiceImpl extends RemoteServiceServlet implements ServletUtilService {

	private static final String PARAM_ALLOWED_KEYS = "allowedKeys";
	private static final String PARAM_ALLOWED_KEYS_SEPARATOR_REGEX = ",";

	private static final String PARAM_ALLOWED_REGEX = "allowedRegex";
	
	private final Set<String> allowedKeys = new HashSet<String>();
	private Pattern allowedRegex =  null;
	
	@Override
	public GetServletInitParamsResponse getServletInitParams(GetServletInitParamsRequest request) throws ServiceException {
		Map<String, String> paramsMap = new HashMap<String, String>();
		
		for (String key : request.getParamsNames()) {
			if (isParamKeyAllowed(key)) {
				paramsMap.put(key, ServletUtil.findInitParameter(this, key));
			}
		}
		
		return new GetServletInitParamsResponse(paramsMap);
	}
	
	private boolean isParamKeyAllowed(String key) {
		if (allowedRegex != null && allowedRegex.matcher(key).matches()) {
			return true;
		}
		return allowedKeys.contains(key);
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
