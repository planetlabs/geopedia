package com.sinergise.geopedia.server.rest;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.geopedia.core.config.Configuration;
import com.sinergise.geopedia.core.exceptions.GeopediaException;


@Path("/misc")
public class MiscResource  extends AGeopediaResource {

	private static final Logger logger = LoggerFactory.getLogger(MiscResource.class);
	
	@GET
	@Path("/configuration")
	@Produces({MediaType.APPLICATION_JSON+"; charset=utf-8"})
	public Configuration getConfiguration(@HeaderParam("Authorization") String authHeader) 
	{///TODO: fix fix fix fix
		try {
			return getMetaService(createSession(authHeader)).getConfiguration();
		} catch (GeopediaException e) {
			return null;
		}
	}
	
}
