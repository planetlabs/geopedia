package com.sinergise.geopedia.core.service;

import java.util.Date;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.geopedia.core.common.HasSession;
import com.sinergise.geopedia.core.entities.News;
import com.sinergise.geopedia.core.entities.Translation.Language;
import com.sinergise.geopedia.core.entities.User;
import com.sinergise.geopedia.core.entities.WidgetInfo;
import com.sinergise.geopedia.core.filter.AbstractFilter;
import com.sinergise.geopedia.core.util.LanguageSettings;

public interface SessionServiceAsync
{
	
	public void createSession(AsyncCallback<HasSession<User>> callback);

	/**
     * 
     * @param sid
     * @param username
     * @param password
     * @return UserPermissions
     */
	public void login(String username, String password, AsyncCallback<HasSession<User>> callback);
	public void logout(AsyncCallback<HasSession<Void>> callback);
//	public void createUser(String login, String password, String email, String fullName, String org) throws UpdateException;
	
	/**
	 * Returns null if sid parameter is valid, or new session id if it wasn't
	 * 
	 * @param sid
	 * @return
	 */
	public void ping(AsyncCallback<HasSession<Void>> callback);
	
	/**
	 * Associate session with preferred language
	 * @param sid - sessionId
	 * @param language - language preference
	 */
	public void setLanguage(Language language, AsyncCallback<LanguageSettings> calback);
	/**
	 * @param sid - sessionId
	 * @param filterByLanguage - when set to true client is exposed only to those themes, tables and fields that list preferred language in theme_languages, table_languges and field_languages respectively.
	 */
	public void setFilterByLanguage(boolean filterByLanguage, AsyncCallback<Void> callback);
	
	/**
	 * Set session variable
	 * @param sid - sessionId
	 * @param name - name of the variable
	 * @param value - value for the variable
	 */
	public void setSessionVariable(String name, String value, AsyncCallback<HasSession<Void>> callback);

	
	public void registerWidget(String widgetId,
			AsyncCallback<WidgetInfo> callback);
	
	public void getNews(Date date, int count, boolean next, AsyncCallback<News[]> callback); 
	
	public void updateFilters(AbstractFilter[] filters, AsyncCallback<HasSession<Void>> callback );

	public void getUserUpdate(int id, AsyncCallback<User> callback);
	
	public void getDatasetConfiguration(int datasetID, String reason,  AsyncCallback<String> callback);

}
