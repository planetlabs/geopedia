package com.sinergise.java.web.proxy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.util.web.MimeType;

public class SimpleProxy extends HttpServlet {
	
	private final Logger logger = LoggerFactory.getLogger(SimpleProxy.class);
	
	public static final String PROXY_PREFIX = "_spxy_";
	public static final String PARAM_SP_PATH_PREFIX = "proxyPath.";
	
	public static final String PARAM_AUTH_USER_PREFIX = "proxyAuth.user.";
	public static final String PARAM_AUTH_PASS_PREFIX = "proxyAuth.pass.";
	public static final String PARAM_AUTH_SESSION_KEY_PREFIX = "proxyAuth.session.key.";
	public static final String PARAM_AUTH_SESSION_VAL_PREFIX = "proxyAuth.session.val.";
	
	
	private static final long serialVersionUID = -2003825325292846897L;

	private static final String[] splitRequest(HttpServletRequest req) {
		String str = req.getPathInfo();
		int start = str.indexOf(PROXY_PREFIX);
		int idx = str.indexOf('/', start);

		return new String[] { str.substring(start, idx), str.substring(idx + 1) };
	}

	private HashMap<String, String> targetMappings = new HashMap<String, String>();
	private HashMap<String, ProxyUserPassAuthenticator> authMappings = new HashMap<String, ProxyUserPassAuthenticator>();

	public String getMappingString(int mappingId) {
		return PROXY_PREFIX+String.valueOf(mappingId);
	}
	
	public void registerMapping(String pathDir, String targetURL) {
		if (targetURL == null) targetMappings.remove(pathDir);
		else {
			targetMappings.put(pathDir, targetURL);
		}
	}
	
	public String registerMapping(String targetURL) {
		int i=1;
		while (targetMappings.containsKey(String.valueOf(i))) {
			i++;
		}
		String key = String.valueOf(i);
		registerMapping(key, targetURL);
		return key;
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String[] data = splitRequest(req);
		String token = data[0];
		if ((PROXY_PREFIX+"register").equals(token)) {
			String path = req.getParameter("path");
			if (path == null || path.length() < 0) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			String newToken = registerMapping(path);
			resp.setContentType(MimeType.MIME_PLAIN_TEXT.createContentTypeString());
			Writer wr = resp.getWriter();
			wr.write(newToken);
			wr.close();
			return;
		}
		processRequest("POST", req, resp);
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest("DELETE", req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest("GET", req, resp);
	}

	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest("HEAD", req, resp);
	}

	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest("OPTIONS", req, resp);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest("PUT", req, resp);
	}

	@Override
	protected void doTrace(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest("TRACE", req, resp);
	}

	protected void processRequest(String method, HttpServletRequest req,
			HttpServletResponse resp) {
		String[] data = splitRequest(req);
		String prefix = targetMappings.get(data[0]);
		String path = data[1];
		String qry = req.getQueryString();
		String newURL = prefix + (path == null ? "" : path)
				+ (qry == null ? "" : '?' + qry);
		
		ProxyUserPassAuthenticator auth = authMappings.get(data[0]);
		if (auth != null) {
			if (!auth.canAuthenticate(req.getSession())) {
				try {
					resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authorized");
				} catch(IOException e) {
					logger.error(e.getMessage(), e);
				}
				return;
			} 
			Authenticator.setDefault(auth);  
		} else {
			Authenticator.setDefault(null);
		}
		
		logger.trace(">>Proxy<< " + method + " " + data[0] + "... ==> " + newURL);
		redirectRequest(method, newURL, req, resp);
	}

	protected void redirectRequest(String method, String targetURL,
			HttpServletRequest req, HttpServletResponse resp) {
		try {
			// TODO: Use Apache HTTP client to implement this
			URL url = null;
			try {
				url = new URL(targetURL);
			} catch (MalformedURLException e) {
				logger.error(e.getMessage(), e);
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal referred URL: " + e.getMessage());
				return;
			}

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(method);
			int cLen = copyHeaders(req, conn);
			conn.setDoInput(true);
			if (!"GET".equals(method)) { //no payload
				conn.setDoOutput(true);
				InputStream rIs = req.getInputStream();
				OutputStream os = conn.getOutputStream();
				try {
					if(cLen > 0) {
						copyStream(cLen, rIs, os);
					}
				} finally {
					rIs.close();
					os.close();
				}
			}
			cLen = copyHeaders(conn, resp);

			InputStream is = conn.getInputStream();
			try {
//				if (conn.getResponseCode()!=200) {
//					resp.sendError(conn.getResponseCode(), conn.getResponseMessage());
//				} else {
					OutputStream rOs = resp.getOutputStream();
					try {
						copyStream(cLen, is, rOs);
						resp.setStatus(conn.getResponseCode());
					} finally {
						rOs.close();
					}
//				}
			} finally {
				is.close();
			}
		} catch (FileNotFoundException e) {
			try {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
			} catch (IOException e1) {
				logger.error(e1.getMessage(), e1);
			}
		} catch (Exception e) {
			try {
				logger.error(e.getMessage(), e);
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
						.getMessage());
			} catch (IOException e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
	}

	private static final int copyHeaders(HttpURLConnection srcConn, HttpServletResponse resp) {
		int cLen;
		resp.setContentType(srcConn.getContentType());
		resp.setContentLength(cLen = srcConn.getContentLength());

		Map<String, List<String>> hdrs = srcConn.getHeaderFields();
		for (Entry<String, List<String>> en : hdrs.entrySet()) {
			List<String> vals = en.getValue();
			
			if (en.getKey() == null) continue;
			if (vals == null || vals.size() == 0) {
				resp.setHeader(en.getKey(), "");
			} else {
				for (int i = 0; i < vals.size(); i++) {
					resp.addHeader(en.getKey(), vals.get(i));
				}
			}
		}

		return cLen;
	}

	private static final int copyHeaders(HttpServletRequest srcReq, HttpURLConnection tgtConn) {
		Enumeration<String> enm = srcReq.getHeaderNames();
		while (enm.hasMoreElements()) {
			String headNm = enm.nextElement();
//			if (headNm == null || headNm.equals("If-None-Match"))
//				continue;
			tgtConn.setRequestProperty(headNm, srcReq.getHeader(headNm));
		}
		
		String cType = srcReq.getContentType();
		if (cType != null) tgtConn.setRequestProperty("Content-Type", cType);
		
		int cLen = srcReq.getContentLength();
		if (cLen >= 0) tgtConn.setRequestProperty("Content-Length", String.valueOf(cLen));
		return cLen;
	}

	private static final void copyStream(int cLen, InputStream is,
			OutputStream os) throws IOException 
	{
		if (cLen < 0) return;
		
		byte[] buf = new byte[Math.min(4096, cLen)];
		int read = 0;
		do {
			read = is.read(buf);
			if (read > 0) {
				os.write(buf, 0, read);
			}
		} while (read > 0);
	}

	@Override
	public void init() throws ServletException {
		super.init();
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		Enumeration<String> en=config.getInitParameterNames();
		
		while (en.hasMoreElements()) {
			String key=en.nextElement();
			if (key.startsWith(PARAM_SP_PATH_PREFIX)) {
				String val=key.substring(PARAM_SP_PATH_PREFIX.length());
				String path=config.getInitParameter(key);
				registerMapping(val, path);
			}
			
			//authenticators
			if (key.startsWith(PARAM_AUTH_USER_PREFIX)) {
				String proxy = key.substring(PARAM_AUTH_USER_PREFIX.length());
				String user = config.getInitParameter(key);
				String pass = config.getInitParameter(PARAM_AUTH_PASS_PREFIX+proxy);
				String authSessionKey = config.getInitParameter(PARAM_AUTH_SESSION_KEY_PREFIX+proxy);
				String authSessionVal = config.getInitParameter(PARAM_AUTH_SESSION_VAL_PREFIX+proxy);
				authMappings.put(proxy, new ProxyUserPassAuthenticator(user, pass, authSessionKey, authSessionVal));
			}
		}
	}
	
	
	public static class ProxyUserPassAuthenticator extends Authenticator {  
	      
		private String authSessionKey;
		private String authSessionVal;
	    private String user;  
	    private String password;  
	      
	    public ProxyUserPassAuthenticator(String user,String password, String authSessionKey, String authSessionVal) {  
	      this.user = user;  
	      this.password = password;  
	      this.authSessionKey = authSessionKey;
	      this.authSessionVal = authSessionVal;
	    }  
	    
	    boolean canAuthenticate(HttpSession session) {
	    	return authSessionKey == null
	    		|| String.valueOf(session.getAttribute(authSessionKey)).equals(authSessionVal);
	    }
	    
	  
	    @Override  
	    protected PasswordAuthentication getPasswordAuthentication() {  
	        return new PasswordAuthentication(user,password.toCharArray());  
	    }  
	}  
}
