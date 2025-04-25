package com.sinergise.geopedia.server;

import java.io.IOException;

import javax.servlet.ServletException;

import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.core.exceptions.GeopediaException;

public abstract class PediaServlet extends EasyServlet
{
	protected abstract boolean executePedia(Request req) throws ServletException, IOException, GeopediaException;
	
	final protected void execute(Request req) throws ServletException, IOException, GeopediaException 
    {
		Session sess = ServUtil.extractSession(req.getReq());
		req.setSession(sess);		
		executePedia(req);
    }
}