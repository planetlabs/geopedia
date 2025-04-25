package com.sinergise.geopedia.core.service;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sinergise.geopedia.core.common.util.PagableHolder;
import com.sinergise.geopedia.core.config.Configuration;
import com.sinergise.geopedia.core.constants.Globals.PersonalGroup;
import com.sinergise.geopedia.core.entities.Category;
import com.sinergise.geopedia.core.entities.Permissions.GeopediaEntity;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.UpdateException;
import com.sinergise.geopedia.core.entities.utils.EntityConsts.DataScope;
import com.sinergise.geopedia.core.exceptions.GeopediaException;


public interface MetaService extends RemoteService
{
	public static final String SERVICE_URI = "meta";

	public Configuration getConfiguration() throws GeopediaException;

	
	public Theme getThemeById(int id, long lastMetaTimestamp, DataScope scope)
	                throws GeopediaException;

	public Table getTableById(int id, long lastMetaTimestamp, DataScope scope)
	                throws GeopediaException;

	
	

	/**
	 * Updates theme's data and table links.
	 * <br /><br />
	 * The user must have EDIT permission for theme and at least VIEW permission
	 * for all added tables.
	 * <br /><br />
	 * You must always provide theme id and meta timestamp. If the user changed
	 * theme's basic data (name, description, public permissions or base layers),
	 * send the Theme parameter with those filled in. If not, send null. The
	 * following restrictions apply to non-null Theme parameter:
	 * <ul>
	 * <li>id must match themeId parameter
	 * <li>name must be filled in and not empty
	 * <li>public_perms must be valid
	 * <li>lastMetaChange must be the same as themeMeta
	 * </ul>
	 * <br /><br />
	 * For tables that were added, the following applies:
	 * <ul>
	 * <li>tables must have geometries
	 * <li>user must have at least view permission
	 * <li>on/off flags must be valid
	 * <li>styleSpec must be null
	 * <li>styleString must either be null or a valid style specification
	 * <li>the tables will be added at the end of theme in the order provided
	 * <li>id, theme, themeId, table fields are ignored
	 * <li>newOrder must be set, with 0 TTL id for each new table
	 * </ul>
	 * For tables that were changed, the following applies:
	 * <ul>
	 * <li>TTL must already exist in theme
	 * <li>table and theme IDs must be correctly set (and match the existing state)
	 * <li>on/off flags must be valid
	 * <li>styleSpec must be null
	 * <li>styleString must either be null or a valid style specification
	 * <li>the order field is ignored - to change order, use newOrder parameter
	 * <li>the TTL cannot also be deleted at the same time
	 * </ul>
	 * For tables that were deleted, the following applies:
	 * <ul>
	 * <li>TTL ID must exist in current theme
	 * <li>all deleted IDs cannot also appear in updated or newOrder parameters
	 * </ul>
	 * If order is to be changed, the following applies:
	 * <ul>
	 * <li>the array must contain exactly all existing TTL IDs, except those to be deleted
	 * <li>each ID must appear exactly once
	 * <li>the order of IDs in array sets the order of TTLs in theme
	 * <li>for new tables, a 0 must be present for each new table
	 * </ul>
	 * Finally:
	 * <ul>
	 * <li>you can skip any of the parameters (with null) if there is no change of the
	 * given type
	 * <li>it is still an error to not change anything (all nulls)
	 * <li>it is an error if null elements are within arrays or if any TTL appears twice.
	 * </ul>
	 * @param sid session ID
	 * @param themeId theme ID
	 * @param themeMeta theme meta timestamp
	 * @param updated TTLs that were changed
	 * @param created TTLs that are to be added
	 * @param removed TTL IDs that are to be removed
	 * @param newOrder list of existing TTL IDs in the new order
	 * @return new theme meta timestamp
	 * @throws UpdateException
	 */
	//public long updateTheme(int themeId, long themeMeta, Theme themeBasics, ThemeTableLink[] updated, ThemeTableLink[] created, int[] removed, int[] newOrder) throws UpdateException;

	
	
	
	
	/**
	 * Returns (COPY_BASIC) themes that match the fulltext query.
	 * 
	 * @param query fulltext query
	 * @return array of found themes
	 * @throws UpdateException
	 */
	public Theme[] findThemesFulltext(String query) throws UpdateException, GeopediaException;
	
	/**
	 * Returns (COPY_BASIC) tables that match the fulltext query.
	 * 
	 * @param query fulltext query
	 * @return array of found tables
	 * @throws UpdateException
	 */
	public Table[] findTablesFulltext(String query) throws UpdateException,GeopediaException;
	
		

	/**
	 * ------------------------------------------------- NEW -------------------------------------------
	 * 
	 * hopefully deprecate everything above this line!
	 * **/
	
	public ArrayList<Category> queryCategories(Category filter)  throws GeopediaException;
	public PagableHolder<ArrayList<Table>> queryTables(Integer categoryId, String tableName, int dataStartIdx, int dataEndIdx) throws GeopediaException;
	public PagableHolder<ArrayList<Theme>> queryThemes(Integer categoryId, String themeName, int dataStartIdx, int dataEndIdx) throws GeopediaException;
	
	public PagableHolder<ArrayList<Table>> queryUserTables(PersonalGroup group, int dataStartIdx, int dataEndIdx) throws GeopediaException;
	
	public PagableHolder<ArrayList<Theme>> queryUserThemes(PersonalGroup group, int dataStartIdx, int dataEndIdx) throws GeopediaException;
	
	public ArrayList<Object> queryUserTablesAndThemes(PersonalGroup group) throws GeopediaException;
	
	public Theme saveTheme(Theme theme) throws GeopediaException;
	public Theme loadTheme(long themeId) throws GeopediaException;
	
	public Table saveTable(Table table) throws GeopediaException;
	
	public void modifyPersonalGroup(GeopediaEntity entity, int entityId, PersonalGroup group, boolean delete) throws GeopediaException;


	

}