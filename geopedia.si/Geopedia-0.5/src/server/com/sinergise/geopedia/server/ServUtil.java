package com.sinergise.geopedia.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.util.url.URLUtil;
import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.app.session.SessionStorage;
import com.sinergise.geopedia.app.session.SessionUtils;
import com.sinergise.geopedia.core.common.TileUtil;
import com.sinergise.geopedia.core.constants.Globals;
import com.sinergise.geopedia.core.entities.User;
import com.sinergise.geopedia.core.entities.WidgetInfo;
import com.sinergise.geopedia.core.exceptions.GeopediaException;

public class ServUtil {
	static final Logger logger = LoggerFactory.getLogger(ServUtil.class);
    public static final String SYSPROP_DEV_HOST = "geopedia.development.hostname";

	private static String createFirstCharRegExp(char ch) {
        if (ch=='0') return "0";
        if (ch=='1') return "[01]";
        if (Character.isDigit(ch)) return "[0-"+Character.toUpperCase(ch)+"]";
        if (ch=='A') return "[0-9A]";
        if (ch=='B') return "[0-9AB]";
        return "[0-9A-"+Character.toUpperCase(ch)+"]";
    }

//    public static final HashMap<Character, Pattern> TILE_PATTERNS = new HashMap<Character, Pattern>(); 
//    static {
//        for (Map.Entry<Character, TileCoordSpace> e : TileUtil1.spaces.entrySet()) {
//            TILE_PATTERNS.put(e.getKey(), Pattern.compile(createTilePattern(e.getValue())));
//        }
//    }

    private static String createTileCoordsRegExp(TiledCRS tiledCRS, int level) {
        StringBuffer buf=new StringBuffer();
        long maxIdx=(1<<(level-tiledCRS.getMinLevelId()))-1;
        String hx=Long.toHexString(maxIdx);
        buf.append(createFirstCharRegExp(hx.charAt(0)));
        for (int i = 1; i < hx.length(); i++) {
            buf.append("[0-9A-F]");
        }
        return buf.toString();
    }
    
	public static String createTilePattern(TiledCRS tiledCRS) {
		StringBuffer buf = new StringBuffer();
		for (int level = tiledCRS.getMinLevelId(); level <= tiledCRS
				.getMaxLevelId(); level++) {
			if (level > tiledCRS.getMinLevelId())
				buf.append('|');
			String crds = createTileCoordsRegExp(tiledCRS, level);
			buf.append(tiledCRS.getTilePrefixChar());
			buf.append(TileUtil.tileLevelCharFromZoomLevel(tiledCRS, level)
					+ crds + crds);
		}
		return buf.toString();
	}
    
//    public static boolean isValidTileID(TileCoordSpace space, String tileIDs)
//	{
//		return TILE_PATTERNS.get(space.prefix).matcher(tileIDs).matches();
//	}
	
	
	public static void setSessionHeader(Session session, HttpServletResponse resp) {		
		resp.setHeader(Globals.SESSION_HEADER, session.getSessionHeaderValue());
	}

    
    public static Session extractSession(HttpServletRequest req) throws GeopediaException {
    	String sidValue=req.getParameter(Globals.SESSION_PARAM_NAME);
    	logger.trace("Param sid="+sidValue);
    	if (sidValue==null) {
    		sidValue = req.getHeader(Globals.SESSION_HEADER);
        	logger.trace("session header="+sidValue);

    	}
    	return extractSession(sidValue, getInstanceId(req));
    }

    
//    public static Session extractSession(HttpServletRequest req, String cookieName) throws GeopediaException {
//    	String sidValue=req.getParameter(Globals.SESSION_PARAM_NAME);
//		
//		if (sidValue==null) {
//			Cookie cookies[] = req.getCookies();
//			if (cookies!=null) {
//				for (Cookie c:cookies) {
//					if (c.getName().equals(cookieName)) {
//						sidValue = c.getValue();
//						break;
//					}
//				}
//			}
//		}
//		
//		
//	}
//    
    public static ServerInstance getInstance(HttpServletRequest req) throws GeopediaException {
    	String urlString = req.getRequestURL().toString();
    	String host = URLUtil.getHost(urlString);
    	String developmentHostname = System.getProperty(SYSPROP_DEV_HOST, null);
    	if (developmentHostname != null) {
    		host = developmentHostname;
    	}
    	String domain = host;
    	int idx = host.indexOf(".");
    	if (idx>=0 && host.lastIndexOf(".")!=idx) {
    		domain = host.substring(idx+1);
    	}
    		ServerInstance instance = ServerInstance.getInstance(domain);
    		return instance;
    }
    
    
    public static int getInstanceId(HttpServletRequest req) throws GeopediaException {
    	return getInstance(req).getId();
    }
    
    public static Session extractSession(String sidValue, int instanceId) {
    	if (sidValue!=null) {
			String [] vals = sidValue.split("_");
			if (vals!=null && vals.length==2) {
				try {
					String sid = vals[0];
					long timestamp = Long.parseLong(vals[1]);
					Session sess = SessionStorage.getSession(sid, instanceId, timestamp);
					return sess;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
		}
		return null;
    }
	
    public static Session prepareWidgetSession(Session sess, HttpServletRequest httpReq, 
			WidgetInfo widgetInfo) throws GeopediaException{
    	
    	if (sess == null) {
    		sess = extractSession(httpReq);
			if (sess == null) {
				sess = SessionStorage.createNewSession(getInstanceId(httpReq), true, false);
			}

    	}
		synchronized (sess) {
			setWidgetSessionInfo(sess,widgetInfo);
		}
		return sess;
    }

	public static void setWidgetSessionInfo(Session sess, WidgetInfo widgetInfo) throws GeopediaException {
		sess.setUser(User.NO_USER); // remove user to prevent login from throwing an exception
		widgetInfo.user = User.NO_USER;
		if (widgetInfo!=null) {
			if (widgetInfo.asUserId>0) {
				widgetInfo.user =  SessionUtils.login(sess, widgetInfo.asUserId);     					
				SessionStorage.updateSession(sess);
			}
		}
		
	}
    
}
