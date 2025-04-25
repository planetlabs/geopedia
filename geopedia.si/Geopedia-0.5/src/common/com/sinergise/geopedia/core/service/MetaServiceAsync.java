package com.sinergise.geopedia.core.service;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.geopedia.core.common.util.PagableHolder;
import com.sinergise.geopedia.core.config.Configuration;
import com.sinergise.geopedia.core.constants.Globals.PersonalGroup;
import com.sinergise.geopedia.core.entities.Category;
import com.sinergise.geopedia.core.entities.Permissions.GeopediaEntity;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.utils.EntityConsts.DataScope;


public interface MetaServiceAsync
{
	public void getConfiguration(AsyncCallback<Configuration> callback);
	
	public void getThemeById(int id, long lastMetaTimestamp, DataScope scope, AsyncCallback<Theme> callback);

	public void getTableById(int id, long lastMetaTimestamp, DataScope scope, AsyncCallback<Table> callback);
	

	/**
	 * Returns (COPY_BASIC) themes that match the fulltext query.
	 * 
	 * @param query fulltext query
	 * @return array of found themes
	 */
	public void findThemesFulltext(String query, AsyncCallback<Theme[]> callback);
	
	/**
	 * Returns (COPY_BASIC) tables that match the fulltext query.
	 * 
	 * @param query fulltext query
	 * @return array of found tables
	 */
	public void findTablesFulltext(String query, AsyncCallback<Table[]> callback);
	
		
	/* new */
	
	public void queryCategories(Category filter, AsyncCallback<ArrayList<Category>> callback);
	
	public void queryTables(Integer categoryId, String tableName, int dataStartIdx, int dataEndIdx, AsyncCallback<PagableHolder<ArrayList<Table>>> callback);
	public void queryThemes(Integer categoryId, String themeName, int dataStartIdx, int dataEndIdx, AsyncCallback<PagableHolder<ArrayList<Theme>>> callback);
	
	public void queryUserTables(PersonalGroup group, int dataStartIdx, int dataEndIdx, AsyncCallback<PagableHolder<ArrayList<Table>>> callback);
	
	public void queryUserThemes(PersonalGroup group, int dataStartIdx, int dataEndIdx, AsyncCallback<PagableHolder<ArrayList<Theme>>> callback);
	
	
	public void queryUserTablesAndThemes(PersonalGroup group, AsyncCallback<ArrayList<Object>> callback); 
	
	
	public void saveTheme(Theme theme,  AsyncCallback<Theme> callback);
	
	public void loadTheme(long themeId, AsyncCallback<Theme> callback);
	
	// tables
	public void saveTable(Table table, AsyncCallback<Table> callback);
	
	public void modifyPersonalGroup(GeopediaEntity entity, int entityId, PersonalGroup group, boolean delete, AsyncCallback<Void> callback);

	
}