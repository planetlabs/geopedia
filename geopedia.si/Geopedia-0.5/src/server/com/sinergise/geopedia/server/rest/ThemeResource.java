package com.sinergise.geopedia.server.rest;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.utils.EntityConsts.DataScope;

@Path("/theme")
public class ThemeResource extends AGeopediaResource {
	
	private static final Logger logger = LoggerFactory.getLogger(ThemeResource.class);
	
	@GET
	@Path("/{id}")
	@Produces({MediaType.APPLICATION_JSON+"; charset=utf-8"})
	public Theme getThemeById(@PathParam("id") int id,
		@HeaderParam("Authorization") String authHeader) 
	{
		return getThemeById(createSession(authHeader), id, DataScope.ALL);
	}
	
	@GET
	@Path("/{id}/meta")
	@Produces({MediaType.APPLICATION_JSON+"; charset=utf-8"})
	public Theme getThemeMetaById(@PathParam("id") int id,
		@HeaderParam("Authorization") String authHeader) 
	{
		return getThemeById(createSession(authHeader), id, DataScope.MEDIUM);
	}
	
	@GET
	@Path("/{id}/lastMetaChange")
	@Produces({MediaType.APPLICATION_JSON+"; charset=utf-8"})
	public long getThemeLastMetaChange(@PathParam("id") int tableId,		
		@HeaderParam("Authorization") String authHeader) 
	{
		Theme t = getThemeById(createSession(authHeader), tableId, DataScope.MEDIUM);
		if (t != null) {
			return t.lastMetaChange;
		}
		return 0;
	}
	
	private Theme getThemeById(Session session, int id, DataScope scope) {
		try {
			return getMetaService(session).getThemeById(id, 0, scope);
		} catch (Exception e) {
			String msg = "Failed to get Theme for id: "+id+", "+e.getMessage();
			logger.error(msg, e);
			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
					.entity(msg).type(MediaType.TEXT_PLAIN).build());
		}
	}
	
}
