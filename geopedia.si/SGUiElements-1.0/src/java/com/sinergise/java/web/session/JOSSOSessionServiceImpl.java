package com.sinergise.java.web.session;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.util.ServiceException;
import com.sinergise.common.web.session.GetSessionActivityRequest;
import com.sinergise.common.web.session.GetSessionActivityResponse;
import com.sinergise.common.web.session.JOSSOSessionService;
import com.sinergise.java.web.ServletUtil;

/**
 * {@link JOSSOSessionService} implementation. 
 * 
 * @author mtomic
 */

@SuppressWarnings("serial")
public class JOSSOSessionServiceImpl extends SessionServiceImpl implements JOSSOSessionService {
	
	private static final String PARAM_JMX_URL = "jmxUrl";
	String jmxUrl;
	
	protected Logger logger = LoggerFactory.getLogger(JOSSOSessionServiceImpl.class);
	
	public GetSessionActivityResponse getJOSSOSessionActivity(GetSessionActivityRequest request) throws ServiceException {
//		logger.debug("getJOSSOSessionActivity IN");
		try {
			String allSessions = getAllSessions();
			int maxInactiveInterval = getMaxInactiveInterval();
			Date lastAccess = parseDate(allSessions, request.getSessionID());
			boolean expired = allSessions == null || !allSessions.toString().contains(request.getSessionID()) ||
								(new Date().getTime() - lastAccess.getTime() > maxInactiveInterval*1000);
//			logger.debug("expired="+expired);
//			if (expired) {
//				logger.debug("allSessions=\""+allSessions+"\"");
//				logger.debug("allSessions == null="+(allSessions == null));
//				if (allSessions != null)
//					logger.debug("!allSessions.toString().contains(request.getSessionID())="+!allSessions.toString().contains(request.getSessionID()));
//				logger.debug("expired = "+new Date().getTime()+"-"+lastAccess.getTime()+">"+maxInactiveInterval*1000);
//			}
			GetSessionActivityResponse response = new GetSessionActivityResponse(request.getSessionID(), expired);
			response.setMaxInactiveInterval(maxInactiveInterval);
			response.setLastAccessedTime(lastAccess);
//			logger.debug("getJOSSOSessionActivity OUT");
			return response;
		} catch(Exception e) {
			throw new SecurityException(e);
		}
	}
	
	@Override
	public void init() throws ServletException {
		super.init();
		jmxUrl = ServletUtil.findInitParameter(this, PARAM_JMX_URL);
	}
	
	String getAllSessions() throws Exception {
		JMXServiceURL url = new JMXServiceURL(jmxUrl);
		JMXConnector conn = JMXConnectorFactory.connect(url);
		Object o = null;
		try {
			MBeanServerConnection mbsc = conn.getMBeanServerConnection();
	
			ObjectName objectName = new ObjectName("josso:type=SSOGatewayInfo");
			Object[] params ={};
			String[] signature = {};
	        o = mbsc.invoke(objectName, "listSessions", params,signature);
	        
//	        logger.debug("RET="+o);
			if (o == null) return null;
			return o.toString();
		} finally {
			conn.close();
		}
	}
	
	int getMaxInactiveInterval() throws Exception {
		JMXServiceURL url = new JMXServiceURL(jmxUrl);
		JMXConnector conn = JMXConnectorFactory.connect(url);
		Object o = null;
		try {
			MBeanServerConnection mbsc = conn.getMBeanServerConnection();
	
			ObjectName objectName = new ObjectName("josso:type=SSOSessionManager");
	        o = mbsc.getAttribute(objectName, "maxInactiveInterval");
	        
//	        logger.debug("maxInactiveInterval="+o);
			if (o == null) return Integer.MIN_VALUE;
			return Integer.parseInt(o.toString()) * 60;
		} finally {
			conn.close();
		}
	}
	
	static Date parseDate(String allSessions, String currentSessionId) throws ParseException {
		if (allSessions == null) return null;
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm:ss yyyy");
		int index = allSessions.indexOf(currentSessionId);
		if (index < 0)
			return new Date(1);
		String dateString = allSessions.substring(index+currentSessionId.length()+1);
		dateString = dateString.substring(dateString.indexOf("]")+6);
		dateString = dateString.substring(0, dateString.indexOf("]"));
		dateString = dateString.substring(0, dateString.indexOf("CET")) + dateString.substring(dateString.indexOf("CET")+4);
		return sdf.parse(dateString);
	}

}
