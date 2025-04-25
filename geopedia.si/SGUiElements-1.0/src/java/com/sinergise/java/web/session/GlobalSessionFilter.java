package com.sinergise.java.web.session;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.core.EntryView;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.util.UuidUtil;



/***
 * Global clustered session provider. Replaces default server http session
 * 
 * Shutdown exception  'Internal executor rejected task: ....' is hazelcast related and has been fixed in latest snapshot
 * 
 * @author pkolaric
 *
 */
@SuppressWarnings("deprecation")
public class GlobalSessionFilter implements Filter {

	public static final String INIT_PARAM_HAZELCAST_CLIENT_CONFIG = "hazelcastClientConfigURL";
	public static final String INIT_PARAM_HAZELCAST_SESSION_MAP_NAME = "hazelcastSessionMap";
	
	public static final String INIT_PARAM_SESSION_COOKIE = "sessionCookie";
	public static final String INIT_PARAM_SESSION_COOKIE_DOMAIN = "sessionCookieDomain";
	public static final String INIT_PARAM_SESSION_COOKIE_SECURE = "sessionCookieSecure";
	public static final String INIT_PARAM_SESSION_COOKIE_HTTPONLY = "sessionCookieHttpOnly";
	
	public static final String INIT_PARAM_SESSION_HEADER = "sessionHeader";
	public static final String INIT_PARAM_SESSION_PARAMETER = "sessionParameter";
	
	

	private static final int DEFAULT_SESSION_MAX_INACTIVE_INTERVAL_SECONDS = (int) TimeUnit.MINUTES.toSeconds(60);

	private ServletContext servletContext;
	private HazelcastInstance hazelcastInstance;
	private String clusterMapName = "sessions";

	private String sessionHeaderName = null;

	private String sessionParameterName = null;
	
	private String sessionCookieName = null;
	private String sessionCookieDomain = null;
	private boolean sessionCookieHttpOnly = false;
	private boolean sessionCookieSecure = false;

	private static class GlobalHttpSession implements HttpSession, DataSerializable {

		private ServletContext servletContext;
		private boolean isNew = false;
		private long lastAccessedTime = 0;
		private long creationTime;

		private volatile boolean valid = true;
		private boolean dirty = false;

		// serialized
		private String id;
		private int maxInactiveInterval = DEFAULT_SESSION_MAX_INACTIVE_INTERVAL_SECONDS;
		private ConcurrentHashMap<String, Object> localAttributes;

		public GlobalHttpSession(String sessionId) {
			this.id = sessionId;
			this.localAttributes = new ConcurrentHashMap<String, Object>();
			this.creationTime = System.currentTimeMillis();
			this.lastAccessedTime = creationTime;
			this.isNew = true;
		}

		/** Only for deserialization **/
		@SuppressWarnings("unused")
		private GlobalHttpSession() {
		}

		@Override
		public void readData(ObjectDataInput in) throws IOException {
			id = in.readUTF();
			maxInactiveInterval = in.readInt();
			localAttributes = in.readObject();
			if (localAttributes == null) { //
				throw new IOException("Serialized data is illegal!");
			}
		}

		@Override
		public void writeData(ObjectDataOutput out) throws IOException {
			out.writeUTF(id);
			out.writeInt(maxInactiveInterval);
			out.writeObject(localAttributes);
		}

		@Override
		public Object getAttribute(String key) {
			if (key == null)
				throw new IllegalArgumentException("key should not be null!");
			return localAttributes.get(key);
		}

		@Override
		public Enumeration<String> getAttributeNames() {
			final Set<String> namesSet = localAttributes.keySet();
			return new Enumeration<String>() {
				private final String[] elements = namesSet.toArray(new String[namesSet.size()]);
				private int idx = 0;

				@Override
				public boolean hasMoreElements() {
					return idx < elements.length;
				}

				@Override
				public String nextElement() {
					return elements[idx++];
				}
			};
		}

		@Override
		public void removeAttribute(String key) {
			if (key == null)
				throw new IllegalArgumentException("key should not be null!");
			localAttributes.remove(key);
		}

		@Override
		public void setAttribute(String key, Object value) {
			if (key == null)
				throw new IllegalArgumentException("key should not be null!");
			if (value==null) {
				localAttributes.remove(key);
			} else {
				localAttributes.put(key, value);
			}
			dirty = true;
		}

		@Override
		public long getCreationTime() {
			return creationTime;
		}

		@Override
		public String getId() {
			return id;
		}

		private void setLastAccessedTime(long lastAccessedTime) {
			this.lastAccessedTime = lastAccessedTime;
		}

		private void setCreationTime(long creationTime) {
			this.creationTime = creationTime;
		}

		@Override
		public long getLastAccessedTime() {
			return lastAccessedTime;
		}

		@Override
		public int getMaxInactiveInterval() {
			return maxInactiveInterval;
		}

		@Override
		public void setMaxInactiveInterval(int maxInactiveIntervalSeconds) {
			this.maxInactiveInterval = maxInactiveIntervalSeconds;
		}

		@Override
		public ServletContext getServletContext() {
			return servletContext;
		}

		@Override
		public HttpSessionContext getSessionContext() {
			throw new UnsupportedOperationException("Operation is deprecated and wasn't implemented!");
		}

		@Override
		public Object getValue(String key) {
			return getAttribute(key);
		}

		@Override
		public String[] getValueNames() {
			Set<String> keySet = localAttributes.keySet();
			return keySet.toArray(new String[keySet.size()]);
		}

		@Override
		public void putValue(String key, Object value) {
			setAttribute(key, value);
		}

		@Override
		public void removeValue(String key) {
			removeAttribute(key);
		}

		@Override
		public void invalidate() {
			valid = false;
		}

		@Override
		public boolean isNew() {
			return isNew;
		}

		private boolean isValid() {
			return valid;
		}

		private boolean isDirty() {
			return dirty;
		}

		private void setServletContext(ServletContext servletContext) {
			this.servletContext = servletContext;
		}

	}

	private class HttpRequestWrapper extends HttpServletRequestWrapper {

		private GlobalHttpSession globalSession;
		private HttpServletResponse response;
		private String requestSessionId = null;

		public HttpRequestWrapper(HttpServletRequest request, HttpServletResponse response) {
			super(request);
			this.response = response;
		}

		private String getSessionId() {
			if (requestSessionId != null) {
				return requestSessionId;
			}
			
			requestSessionId = getSessionIdFromCookie();
			if (requestSessionId != null)
				return requestSessionId;
			
			requestSessionId = getSessionIdFromHeader();
			if (requestSessionId != null)
				return requestSessionId;
			
			requestSessionId = getSessionIdFromParameter();
			return requestSessionId;
		}

		private String getSessionIdFromParameter() {
			if (sessionParameterName == null) // skip parameter
				return null;
			String sessionId = getParameter(sessionParameterName);
			if (sessionId!=null && sessionId.trim().isEmpty())
				return null;
			return sessionId;
		}
		
		private void addSessionToResponse() {
			addSessionCookie();
			addResponseHeader();
		}
		
		private void addResponseHeader() {
			if (sessionHeaderName == null) // skip header
				return;
			if (globalSession == null)
				return;
			response.setHeader(sessionHeaderName, globalSession.getId());
		}
		
		private String getSessionIdFromHeader() {
			if (sessionHeaderName == null)
				return null;
			String sessionId = getHeader(sessionHeaderName);
			if (sessionId != null && sessionId.trim().isEmpty())
				return null;
			return sessionId;
		}

		private void addSessionCookie() {
			if (sessionCookieName == null) // skip cookies
				return;

			if (globalSession == null)
				return;

			final Cookie sessionCookie = new Cookie(sessionCookieName, globalSession.getId());
			String path = getContextPath();
			if ("".equals(path)) {
				path = "/";
			}
			sessionCookie.setPath(path);
			sessionCookie.setMaxAge(-1);
			if (sessionCookieDomain != null) {
				sessionCookie.setDomain(sessionCookieDomain);
			}
			sessionCookie.setHttpOnly(sessionCookieHttpOnly);
			sessionCookie.setSecure(sessionCookieSecure);
			response.addCookie(sessionCookie);
		}

		
		private String getSessionIdFromCookie() {
			if (sessionCookieName == null)
				return null;

			final Cookie[] cookies = getCookies();
			if (cookies != null) {
				for (final Cookie cookie : cookies) {
					final String name = cookie.getName();
					final String value = cookie.getValue();
					if (name.equalsIgnoreCase(sessionCookieName)) {
						return value;
					}
				}
			}
			return null;
		}

		@Override
		public HttpSession getSession(boolean create) {
			if (globalSession != null && !globalSession.isValid()) {
				storeGlobalSession();
				globalSession = null;
			}

			if (globalSession != null) {
				return globalSession;
			}

			String sessionId = getSessionId();
			if (sessionId != null) {
				try {
					EntryView<String, Object> entry = getGlobalSessionsMap().getEntryView(sessionId);
					globalSession = (GlobalHttpSession) entry.getValue();
					globalSession.setLastAccessedTime(entry.getLastAccessTime());
					globalSession.setCreationTime(entry.getCreationTime());
				} catch (Throwable ex) {
					globalSession = null;
				}
			}

			if (globalSession == null && create) {
				globalSession = new GlobalHttpSession(generateSessionId());
				requestSessionId = globalSession.getId();
				addSessionToResponse();
			}

			if (globalSession != null) {
				globalSession.setServletContext(servletContext);
			}
			return globalSession;
		}

		private void storeGlobalSession() {
			if (globalSession != null) {
				if (!globalSession.isValid()) {
					getGlobalSessionsMap().remove(globalSession.getId());
				} else if (globalSession.isDirty()) {
					getGlobalSessionsMap().put(globalSession.getId(), globalSession);
				}
			}

		}
	}

	private static String generateSessionId() {
		final String uuid = UuidUtil.buildRandomUuidString().toUpperCase();

		StringBuilder sb = new StringBuilder("ses");
		sb.append(uuid.replaceAll("-", ""));
		return sb.toString();
	}

	private IMap<String, Object> getGlobalSessionsMap() {
		return hazelcastInstance.getMap(clusterMapName);
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		if (!(req instanceof HttpServletRequest)) { // skip non http requests
			chain.doFilter(req, res);
			return;
		}
		if (req instanceof HttpRequestWrapper) {
			chain.doFilter(req, res); // already wrapped
			return;
		}

		HttpServletRequest httpRequest = (HttpServletRequest) req;
		final HttpRequestWrapper gpdRequestWrapper = new HttpRequestWrapper(httpRequest, (HttpServletResponse) res);
		chain.doFilter(gpdRequestWrapper, res);
		gpdRequestWrapper.storeGlobalSession();
	}

	@Override
	public void destroy() {		
		hazelcastInstance.shutdown();
		hazelcastInstance=null;
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		servletContext = config.getServletContext();
		String clientConfigURLString = config.getInitParameter(INIT_PARAM_HAZELCAST_CLIENT_CONFIG);
		if (clientConfigURLString != null) {
			try {
				URL clientConfigURL = servletContext.getResource(clientConfigURLString);
				ClientConfig hazelClientConfig = new XmlClientConfigBuilder(clientConfigURL).build();
				hazelcastInstance = HazelcastClient.newHazelcastClient(hazelClientConfig);

			} catch (MalformedURLException e) {
				throw new ServletException("Failed to load hazelcast client configuration!", e);
			} catch (IOException e) {
				throw new ServletException("Failed to load hazelcast client configuration!", e);
			}
		} else {
			throw new ServletException("Missing hazelcast client configuration URL!");
		}
		
		sessionCookieName = config.getInitParameter(INIT_PARAM_SESSION_COOKIE);
		sessionHeaderName = config.getInitParameter(INIT_PARAM_SESSION_HEADER);
		sessionParameterName = config.getInitParameter(INIT_PARAM_SESSION_PARAMETER);
		
		if (sessionCookieName == null && sessionHeaderName == null && sessionParameterName == null) {
			throw new ServletException("You must define at least method of passing sessionId (cookie,header, parameter)!");
		}
		
		String hazelcastSessionMapName = config.getInitParameter(INIT_PARAM_HAZELCAST_SESSION_MAP_NAME);
		if (hazelcastSessionMapName!= null && !hazelcastSessionMapName.trim().isEmpty()) {
			clusterMapName = hazelcastSessionMapName; 
		}
		
		sessionCookieDomain = config.getInitParameter(INIT_PARAM_SESSION_COOKIE_DOMAIN);
		
		String cookieHttpOnly = config.getInitParameter(INIT_PARAM_SESSION_COOKIE_HTTPONLY);
		if (cookieHttpOnly!=null) {
			sessionCookieHttpOnly = Boolean.parseBoolean(cookieHttpOnly);
		}
		String cookieSecure = config.getInitParameter(INIT_PARAM_SESSION_COOKIE_SECURE);
		if (cookieSecure!=null) {
			sessionCookieSecure = Boolean.parseBoolean(cookieSecure);
		}
		

	}

}
