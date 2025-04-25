package com.sinergise.geopedia.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.geopedia.core.exceptions.GeopediaException;


public abstract class EasyServlet extends HttpServlet
{
	private static final Logger logger = LoggerFactory.getLogger(EasyServlet.class); 
	protected abstract void execute(Request req) throws ServletException, IOException, GeopediaException;

	protected void doRequest(Request req) throws ServletException, IOException
	{
		try {
			try {
				execute(req);
			} catch (GeopediaException ex) {
				logger.trace("Failed to execute request!", ex);
				//TODO: handle
			}
		} finally {
			req.cleanUp();
		}
	}

	protected boolean isMethodSupported(int methodId)
	{
		return methodId == Request.M_GET || methodId == Request.M_POST || methodId == Request.M_HEAD;
	}

	protected void fail(HttpServletRequest req, HttpServletResponse resp, String msg) throws IOException
	{
		if (req.getProtocol().endsWith("1.1")) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, msg);
		} else {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
		}
	}

	protected void doIt(int reqType, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		if (isMethodSupported(reqType)) {
			doRequest(new Request(reqType, req, resp));
		} else {
			fail(req, resp, "Method " + methodName(reqType) + " not supported");
		}
	}
	
	public static String methodName(int reqType)
	{
		switch (reqType) {
		case Request.M_DELETE:
			return "DELETE";
		case Request.M_GET:
			return "GET";
		case Request.M_HEAD:
			return "HEAD";
		case Request.M_OPTIONS:
			return "OPTIONS";
		case Request.M_POST:
			return "POST";
		case Request.M_PUT:
			return "PUT";
		case Request.M_TRACE:
			return "TRACE";
		}
		return "unknown";
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
	                IOException
	{
		doIt(Request.M_GET, req, resp);
	}

	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
	                IOException
	{
		doIt(Request.M_DELETE, req, resp);
	}

	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
	                IOException
	{
		doIt(Request.M_HEAD, req, resp);
	}

	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
	                IOException
	{
		doIt(Request.M_OPTIONS, req, resp);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
	                IOException
	{
		doIt(Request.M_POST, req, resp);
	}

	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
	                IOException
	{
		doIt(Request.M_PUT, req, resp);
	}

	protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
	                IOException
	{
		doIt(Request.M_TRACE, req, resp);
	}
}
