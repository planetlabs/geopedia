package com.sinergise.geopedia.client.core.entities;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.util.event.ValueChangeListener;
import com.sinergise.common.util.lang.SGAsyncCallback;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.ClientSession;
import com.sinergise.geopedia.client.core.RemoteServices;
import com.sinergise.geopedia.client.core.events.ClientSessionEvent;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.User;
import com.sinergise.geopedia.core.entities.utils.EntityConsts.DataScope;
import com.sinergise.geopedia.core.service.MetaServiceAsync;
import com.sinergise.geopedia.core.util.event.IsEntityChangedSource;



public class Repo implements IsEntityChangedSource {
	
	private static Repo INSTANCE = null;
	
	
	public static Repo instance() {
		return INSTANCE;
	}
	
	public static void createInstance() {
		if (INSTANCE!=null) {
			throw new RuntimeException("Only one instance per runtime!");
		}
		INSTANCE = new Repo();
		

		ClientSessionEvent.register(ClientGlobals.eventBus, new ClientSessionEvent.Handler() {

			@Override
			public void onAutoLoginEvent(ClientSessionEvent event) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onLogin(ClientSessionEvent event) {
				INSTANCE.onLoginStateChanged();
			}

			@Override
			public void onLogout(ClientSessionEvent event) {
				INSTANCE.onLoginStateChanged();
			}
			@Override
			public void onSessionChanged(ClientSessionEvent event) {
			}
			
		});
		
	}
	
	
	private void onLoginStateChanged() {
		for (Theme th:themes.values()) {
			getTheme(th.getId(), th.lastMetaChange, true, null);
		}
		for (Table table:tables.values()) {
			getTable(table.getId(), table.lastMetaChange,true, null);
		}
	}


	private MetaServiceAsync metaService;
	
	private HashMap<Integer,Theme> themes = new HashMap<Integer,Theme>();
	private HashMap<Integer, Table> tables = new HashMap<Integer, Table>();
	
	private Repo() {
		metaService = RemoteServices.getMetaServiceInstance();
	}
	
	private EntityChangeListenerCollection<Table> tableListeners = new EntityChangeListenerCollection<Table>();
	private EntityChangeListenerCollection<Theme> themeListeners = new EntityChangeListenerCollection<Theme>();
	
	public void addThemeChangedListener(EntityChangedListener<Theme> listener) {
		themeListeners.add(listener);
	}
	
	public void addTableChangedListener(EntityChangedListener<Table> listener) {
		tableListeners.add(listener);
	}
	
	public void removeThemeChangedListener(EntityChangedListener<Theme> listener) {
		themeListeners.remove(listener);
	}
	
	public void removeTableChangedListener(ValueChangeListener<Table> listener) {
		themeListeners.remove(listener);
	}

	
	public boolean updateTableDataTimestamp(int tableId, long tableDataTS) {
		Table table =getLocalTable(tableId);
		if (table==null) return false;
		table.lastDataWrite = tableDataTS;
		setTable(table);
		return true;
	}
	
	
	private Table getLocalTable(int tableId) {
		Table table = tables.get(tableId);
		if (table==null) 
			table = getTableFromTheme(tableId);
		return table;
	}
	private Table getTableFromTheme(int tableId) {
		for (Theme th:themes.values()) {
			Table t = th.getTable(tableId);
			if (t!=null)
				return t;
		}
		return null;
	}
	
	public void getTable(int tableId, long changeTS, final AsyncCallback<Table> callback) {		
		getTable(tableId, changeTS, false, callback);
	}
	
	public void getTable(final int tableId, long changeTS, boolean forceRelaod, final AsyncCallback<Table> callback) {		
		Table table = getLocalTable(tableId);
		if (!forceRelaod && table!=null && changeTS<=table.getLastChangeTS()) {
			if (callback!=null)
				callback.onSuccess(table);
			return;
		} else {
			metaService.getTableById(tableId, changeTS,DataScope.ALL , new AsyncCallback<Table>() {

				@Override
				public void onFailure(Throwable caught) {
					Table toDelete = new Table();
					toDelete.setId(tableId);
					toDelete.setDeleted(true);
					setTable(toDelete);
					if (callback!=null)
						callback.onFailure(caught);
				}

				@Override
				public void onSuccess(Table result) {
					setTable(result);
					if (callback!=null)
						callback.onSuccess(result);
				}
				
			});
		}
	}
	
	public void getTheme(int themeId, long metaTS, final AsyncCallback<Theme> callback) {
		getTheme(themeId, metaTS, false, callback);
	}
	public void getTheme(final int themeId, long metaTS, boolean forceReload, final AsyncCallback<Theme> callback) {
		Theme th = themes.get(themeId);
		if (!forceReload && th!=null && metaTS>0 && th.lastMetaChange>=metaTS) {
			if (callback!=null)
				callback.onSuccess(th);
			return;
		} else {
			metaService.getThemeById(themeId, metaTS, DataScope.ALL, new AsyncCallback<Theme>() {

				@Override
				public void onFailure(Throwable caught) {
					Theme toDelete = new Theme();
					toDelete.setId(themeId);
					toDelete.setDeleted(true);
					setTheme(toDelete);
					if (callback!=null)
						callback.onFailure(caught);
				}

				@Override
				public void onSuccess(Theme theme) {
					setTheme(theme);
					if (callback!=null)
						callback.onSuccess(theme);					
				}
			});
		}
	}
	
	public void setTheme(Theme theme) {
		if (theme.isDeleted()) {
			if (themes.remove(theme.getId())!=null) {
				themeListeners.fireValueChanged(this, theme);				
			}
		} else {
			themes.put(theme.getId(),theme);
			themeListeners.fireValueChanged(this, theme);
		}
	}
	
	
	public void setTable(Table table) {
		if (table.isDeleted()) {
			for (Theme theme:themes.values()) {
				if (theme.hasTable(table.getId())) {
					getTheme(theme.getId(), theme.lastMetaChange+1, true, null);
				}
			}
			if (tables.remove(table.getId())!=null) {
				tableListeners.fireValueChanged(this, table);
			}
			
		} else {
			boolean anythingUpdated = false;
			for (Theme theme:themes.values()) {
				if (theme.updateTable(table)) {
					if (theme.lastMetaChange<table.lastMetaChange) {  // not a perticularly good solution, since table meta can be higher..
						theme.lastMetaChange=table.lastMetaChange;
					}
					anythingUpdated=true;
					themeListeners.fireValueChanged(this, theme);
				}
			}
			if (!anythingUpdated) {
				tables.put(table.getId(),table);
			}
			tableListeners.fireValueChanged(this, table);
		}
	}

	public void saveTable(final Table table, final AsyncCallback<Table> callback) {
		metaService.saveTable(table, new AsyncCallback<Table>() {

			@Override
			public void onFailure(Throwable caught) {
				if (callback!=null)
					callback.onFailure(caught);
			}

			@Override
			public void onSuccess(final Table result) {
				setTable(result);
				if (!table.hasValidId()) {
					ClientSession.getUser().updateUser(new SGAsyncCallback<User>() {

						@Override
						public void onFailure(Throwable caught) {
							if (callback!=null)
								callback.onFailure(caught);
						}

						@Override
						public void onSuccess(User userResult) {
							if (callback!=null)
								callback.onSuccess(result);
						}
						
					});
				}
				if (callback!=null)
					callback.onSuccess(result);
			}
		});
	}
	public void saveTheme(final Theme theme,final AsyncCallback<Theme> callback) {
		metaService.saveTheme(theme, new AsyncCallback<Theme>() {

			@Override
			public void onFailure(Throwable caught) {
				if (callback!=null)
					callback.onFailure(caught);
			}

			@Override
			public void onSuccess(final Theme result) {
				setTheme(result);
				if (!theme.hasValidId()) {
					ClientSession.getUser().updateUser(new SGAsyncCallback<User>() {

						@Override
						public void onFailure(Throwable caught) {
							if (callback!=null)
								callback.onFailure(caught);
						}

						@Override
						public void onSuccess(User userResult) {
							if (callback!=null)
								callback.onSuccess(result);
						}
						
					});
				} else {
					if (callback!=null)
						callback.onSuccess(result);
				}
			}
		});
	}

}