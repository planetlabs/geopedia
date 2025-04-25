package com.sinergise.geopedia.core.service;

import java.util.Date;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sinergise.geopedia.core.common.HasSession;
import com.sinergise.geopedia.core.entities.News;
import com.sinergise.geopedia.core.entities.Translation.Language;
import com.sinergise.geopedia.core.entities.User;
import com.sinergise.geopedia.core.entities.UserPermissions;
import com.sinergise.geopedia.core.entities.WidgetInfo;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.core.filter.AbstractFilter;
import com.sinergise.geopedia.core.util.LanguageSettings;

public interface SessionService extends RemoteService
{
	
	public static final String SERVICE_URI = "sess";

	public static class Util
	{
	}
	

	
	public HasSession<User> createSession() throws GeopediaException;
    /**
     * 
     * @param sid
     * @param username
     * @param password
     * @return userType
     * @throws UserPermissions
     */
	public HasSession<User> login(String username, String password) throws GeopediaException;
	public HasSession<Void> logout() throws GeopediaException;
	
	/**
	 * Session service must be periodically pinged to keep the session alive
	 * TODO: pinger callback could carry notification flags
	 */
	public HasSession<Void> ping() throws GeopediaException;
	
	public LanguageSettings setLanguage(Language language) throws GeopediaException;
	public void setFilterByLanguage(boolean filterByLanguage) throws GeopediaException;
	public HasSession<Void> setSessionVariable(String name, String value)throws GeopediaException;
	public WidgetInfo registerWidget(String widgetId) throws GeopediaException;

	
	public News[] getNews(Date date, int count, boolean next) throws GeopediaException;
	
	/**
	 * Filters
	 */
	public HasSession<Void> updateFilters(AbstractFilter[] filters) throws GeopediaException;
	
	public User getUserUpdate(int id) throws GeopediaException;
	//TODO: migrate to common
	public String getDatasetConfiguration(int datasetID, String reason) throws GeopediaException;
}
