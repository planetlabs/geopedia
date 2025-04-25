package com.sinergise.geopedia.server.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.core.entities.User;
import com.sinergise.geopedia.core.service.FeatureService;
import com.sinergise.geopedia.core.service.MetaService;
import com.sinergise.geopedia.db.DB;
import com.sinergise.geopedia.server.ServUtil;
import com.sinergise.geopedia.server.service.FeatureServiceImpl;
import com.sinergise.geopedia.server.service.MetaServiceImpl;
import com.sun.jersey.core.util.Base64;

//TODO: FIX FIX FIX!!!
public abstract class AGeopediaResource {

	protected static synchronized MetaService getMetaService(Session session) {
		return new MetaServiceImpl(session);
	}
	protected static synchronized FeatureService getFeatureService(Session session) {
		return new FeatureServiceImpl(session);
	}

	public static Session createSession (String authHeader) {
		
		if (authHeader!=null && authHeader.length()>6 && authHeader.startsWith("Basic ")) {
			//decode
			String decodedPair = new String(Base64.decode(authHeader.substring(6).getBytes()));
			String[] userDetails = decodedPair.split(":", 2);
			if (userDetails!=null && userDetails.length==2) {
				try {
					DB db = ServerInstance.getInstance(0).getDB();
					User user = db.getUser(userDetails[0], userDetails[1]);
					if (user!=null) {
						Session session = new Session("", 0);
						session.setUser(user);
						return session;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
		String msg = "Not authenticated";
		throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN)
				.entity(msg).type(MediaType.TEXT_PLAIN).build());
	}
	
	private static Session extractSession(Cookie cookie) {
		Session session = null;
		if (cookie != null) {
			session = ServUtil.extractSession(cookie.getValue(),0);
		}
		if (session == null) {
			String msg = "Not authenticated";
			throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN)
					.entity(msg).type(MediaType.TEXT_PLAIN).build());
		}
		return session;
	}
	
}
