package com.sinergise.geopedia.server.service;


import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.app.session.SessionStorage;
import com.sinergise.geopedia.app.session.SessionUtils;
import com.sinergise.geopedia.core.common.HasSession;
import com.sinergise.geopedia.core.entities.News;
import com.sinergise.geopedia.core.entities.Translation.Language;
import com.sinergise.geopedia.core.entities.User;
import com.sinergise.geopedia.core.entities.WidgetInfo;
import com.sinergise.geopedia.core.entities.baselayers.BaseLayer;
import com.sinergise.geopedia.core.entities.baselayers.TiledBaseLayer;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.core.filter.AbstractFilter;
import com.sinergise.geopedia.core.service.SessionService;
import com.sinergise.geopedia.core.util.LanguageSettings;
import com.sinergise.geopedia.server.PediaRemoteServiceServlet;
import com.sinergise.geopedia.server.ServUtil;

@SuppressWarnings("serial")
public class SessionServiceImpl extends PediaRemoteServiceServlet implements SessionService
{
	
	private static final Logger logger = LoggerFactory.getLogger(SessionServiceImpl.class);	
    public SessionServiceImpl() {
     
    }
    
    @Override
	public void init() throws ServletException {
    	
    	
	}

	/**
	 * Throws:
	 * 
	 * T_NO_SESSION if session ID is missing or invalid
	 * T_INVALID_STATE if the session is already logged in
	 * T_UPDATE_FAILED on SQL exception
	 * T_INVALID_DATA if username/password doesn't match anything in database
	 */
	public HasSession<User> login(String username, String password) throws GeopediaException {
		Session sess = getThreadLocalSession();
		return new HasSession<User>(sess.getSessionHeaderValue(), SessionUtils.login(sess, username, password));
    }
	
	

	public HasSession<Void> logout() throws GeopediaException
    {
		Session sess = getThreadLocalSession();
		SessionUtils.logout(sess);
		return new HasSession<Void>(sess.getSessionHeaderValue());
    }
	
	@Override
	public void destroy()
	{
	//	SessionManager.destroy(sid);
	}
	
	public HasSession<User> createSession() throws GeopediaException
    {
		Session sess = getThreadLocalSession();
		if (sess==null) { 
			HttpServletRequest httpReq = getThreadLocalRequest();
			sess = SessionStorage.createNewSession(ServUtil.getInstanceId(httpReq));
			setThreadLocalSession(sess);
		} else {
			sess.setMaster();
		}
		if (sess.getUser()!=null) {
			return new HasSession<User>(sess.getSessionHeaderValue(), sess.getUser());
		}
		return new HasSession<User>(sess.getSessionHeaderValue(),  User.NO_USER);
    }
	
	
	
	public HasSession<Void> ping() throws GeopediaException	
	{
		Session sess = getThreadLocalSession();
		if (sess == null)
			throw new GeopediaException(GeopediaException.Type.NO_SESSION);
		synchronized(sess) {
			if (sess.ping()) {
				SessionStorage.updateSession(sess);
			}
		}	
		return new HasSession<Void>(sess.getSessionHeaderValue());
	}

	
	
	public LanguageSettings setLanguage(Language language)  throws GeopediaException {
		Session sess = getThreadLocalSession();
		if (sess == null) {			
			logger.trace("Session not found!");
			throw new GeopediaException(GeopediaException.Type.NO_SESSION);
		}
		synchronized(sess) {
			sess.setLanguage(language);
			SessionStorage.updateSession(sess);
		}
		ServerInstance instance = sess.getServerInstance();
		LanguageSettings ls = new LanguageSettings();
		
		ls.webLinks = instance.getLinkStorages().getLinksForLanguage(language.code().toLowerCase());
		return ls;
	}
	
	public void setFilterByLanguage(boolean filterByLanguage) throws GeopediaException {
		Session sess = getThreadLocalSession();
		if (sess == null)
			throw new GeopediaException(GeopediaException.Type.NO_SESSION);		
	}
	
	
	public WidgetInfo registerWidget(String widgetId) throws GeopediaException {
		Session sess = getThreadLocalSession();
    	try  {
    		HttpServletRequest httpReq = getThreadLocalRequest();
    		ServerInstance instance = ServerInstance.getInstance(ServUtil.getInstanceId(httpReq));
    		WidgetInfo widgetInfo = instance.getDB().getWidgetInfo(widgetId);
    		if (widgetInfo==null) throw new GeopediaException(GeopediaException.Type.INVALID_WIDGET_ID);
    		sess = ServUtil.prepareWidgetSession(sess, httpReq, widgetInfo);
			setThreadLocalSession(sess);
    		return widgetInfo;
    	} catch (SQLException e) {
    		logger.error("DB error while registering widget!", e);
			throw new GeopediaException(GeopediaException.Type.DATABASE_ERROR);
		}
    }

	@Override
	public News[] getNews(Date date, int count, boolean next)
			throws GeopediaException {
		Session sess = ensureSession();
		ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
    	try  {
    		return  instance.getDB().getNews(date, count, next);
    	} catch (SQLException e) {
    		logger.error("DB error while getting news!", e);
			throw new GeopediaException(GeopediaException.Type.DATABASE_ERROR);
		}
    }


	@Override
	public HasSession<Void> updateFilters(AbstractFilter[] filters) throws GeopediaException {
		Session sess = getThreadLocalSession();
		if (sess == null)
			throw new GeopediaException(GeopediaException.Type.NO_SESSION);
		synchronized(sess) {
			sess.setFilters(filters);
			SessionStorage.updateSession(sess);
		}
		return new HasSession<Void>(sess.getSessionHeaderValue());
	}

	@Override
	public HasSession<Void> setSessionVariable(String name, String value) throws GeopediaException {
		Session sess = getThreadLocalSession();
		if (sess == null)
			throw new GeopediaException(GeopediaException.Type.NO_SESSION);
		return new HasSession<Void>(sess.getSessionHeaderValue());
	}

	@Override
	public User getUserUpdate(int id) throws GeopediaException {
		Session sess = getThreadLocalSession();
		if (sess == null)
			throw new GeopediaException(GeopediaException.Type.NO_SESSION);
		if (sess.getUser().getId()!=id) {
			throw new GeopediaException(GeopediaException.Type.PERMISSION_DENIED);
		}
		sess.getUser().updateUser(null);
		return sess.getUser();
	}

	@Override
	public String getDatasetConfiguration(int datasetID, String reason)
			throws GeopediaException {
		if (!StringUtil.isNullOrEmpty(reason)) {
			logger.error("failed to retrieve clusterDataset (id="+datasetID+") Config: '"+reason+"'");
		}
		Session sess = getThreadLocalSession();
		if (sess == null)
			throw new GeopediaException(GeopediaException.Type.NO_SESSION);
		
		try {
    		ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
    		BaseLayer bLayer = instance.getCommonConfiguration().getBaseLayerConfiguration(datasetID);
    		if (!(bLayer instanceof TiledBaseLayer)) 
    			return null;
    		TiledBaseLayer tbl = (TiledBaseLayer)bLayer;
			URL propURL = new URL(tbl.getTileProvider().getDatasetPropertiesConfigurationURL());
			String data = new Scanner(propURL.openStream(),"UTF-8").useDelimiter("\\A").next();
			return data;
		} catch (Exception e) {
			logger.error("Failed to get dataset configuration",e);
			throw new GeopediaException(GeopediaException.Type.UNKNOWN, e);
		}
	}
}