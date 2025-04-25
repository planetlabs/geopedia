package com.sinergise.geopedia.server.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.ThemeTableLink;
import com.sinergise.geopedia.core.entities.utils.EntityConsts.DataScope;

@Path("/table")
public class TableResource extends AGeopediaResource {

	private static final Logger logger = LoggerFactory.getLogger(TableResource.class);
	
	@GET
	@Path("/{id}")
	@Produces({MediaType.APPLICATION_JSON+"; charset=utf-8"})
	public Table getTableForId(@PathParam("id") int id,
		@HeaderParam("Authorization") String authHeader) 
	{
		return getTableForId(createSession(authHeader), id, DataScope.ALL);
	}
	
	@GET
	@Path("/{id}/meta")
	@Produces({MediaType.APPLICATION_JSON+"; charset=utf-8"})
	public Table getTableMetaForId(@PathParam("id") int id,
		@HeaderParam("Authorization") String authHeader) 
	{
		return getTableForId(createSession(authHeader), id, DataScope.MEDIUM);
	}
	
	@GET
	@Path("/{id}/lastMetaChange")
	@Produces({MediaType.APPLICATION_JSON+"; charset=utf-8"})
	public long getTableLastMetaChange(@PathParam("id") int tableId,
		@HeaderParam("Authorization") String authHeader) 
	{
		Table t = getTableForId(createSession(authHeader), tableId, DataScope.MEDIUM);
		if (t != null) {
			return t.lastMetaChange;
		}
		return 0;
	}
	
	@GET
	@Path("/{id}/lastDataWrite")
	@Produces({MediaType.APPLICATION_JSON+"; charset=utf-8"})
	public long getTableLastDataWrite(@PathParam("id") int tableId,
		@HeaderParam("Authorization") String authHeader) 
	{
		Table t = getTableForId(createSession(authHeader), tableId, DataScope.MEDIUM);
		if (t != null) {
			return t.lastDataWrite;
		}
		return 0;
	}
	
	private Table getTableForId(Session session, int id, DataScope scope) {
		try {
			return getMetaService(session).getTableById(id, 0, scope);
		} catch (Exception e) {
			String msg = "Failed to get Table for id: "+id+", "+e.getMessage();
			logger.error(msg, e);
			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
					.entity(msg).type(MediaType.TEXT_PLAIN).build());
		}
	}
	
	@GET
	@Path("/meta")
	@Produces({MediaType.APPLICATION_JSON+"; charset=utf-8"})
	public Table[] queryTablesMeta(@QueryParam(value = "theme") int themeId,
		@HeaderParam("Authorization") String authHeader)
	{
		return getTablesForThemeId(createSession(authHeader), themeId, DataScope.MEDIUM );
	}
	
	
	@GET
	@Produces({MediaType.APPLICATION_JSON+"; charset=utf-8"})
	public Table[] queryTables(@QueryParam(value = "theme") int themeId,
		@HeaderParam("Authorization") String authHeader) 
	{
		return getTablesForThemeId(createSession(authHeader), themeId, DataScope.ALL );
	}
	
	
	private Table[] getTablesForThemeId(Session session, int themeId, DataScope scope){
		try {
			
			if (themeId > 0) {
				Theme theme = getMetaService(session).getThemeById(themeId, 0, DataScope.ALL);
				
				List<Table> tables = new ArrayList<Table>();
				
				if (theme != null) {
					for (ThemeTableLink link : theme.tables) {
						tables.add(getMetaService(session).getTableById(link.tableId, 0, scope));
					}
				}
				
				return  tables.toArray(new Table[tables.size()]);	
			} 
			
			
			//do not allow listing of all tables
			throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
					.entity("Cannot return all tables, query parameters are mandatory.").type(MediaType.TEXT_PLAIN).build());
		} catch (Exception e) {
			String msg = "Failed to get Table for id: "+themeId+", "+e.getMessage();
			logger.error(msg, e);
			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
					.entity(msg).type(MediaType.TEXT_PLAIN).build());
		}		
	}
	
	
}
