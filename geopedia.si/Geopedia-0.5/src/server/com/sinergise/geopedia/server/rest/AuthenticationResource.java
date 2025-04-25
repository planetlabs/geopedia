package com.sinergise.geopedia.server.rest;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.core.entities.User;

@Path("/auth")
public class AuthenticationResource extends AGeopediaResource{
	
	private static final Logger logger = LoggerFactory.getLogger(AuthenticationResource.class);

	@GET 
	public String authenticateUser(@HeaderParam("Authorization") String authHeader) 
	{
		Session session = createSession(authHeader);
		User user = session.getUser();
		return user.fullName;
	}
	
}
