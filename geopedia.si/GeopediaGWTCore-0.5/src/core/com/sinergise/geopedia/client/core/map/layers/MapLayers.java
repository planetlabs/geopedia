package com.sinergise.geopedia.client.core.map.layers;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.entities.EntityHolder;
import com.sinergise.geopedia.client.core.entities.Repo;
import com.sinergise.geopedia.client.core.entities.ThemeHolder;
import com.sinergise.geopedia.client.core.entities.ThemeHolder.ThemeSettings;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.ThemeTableLink;
import com.sinergise.geopedia.core.util.event.IsEntityChangedSource;
import com.sinergise.geopedia.core.util.event.IsEntityChangedSource.EntityChangedListener;


public class MapLayers implements IsEntityChangedSource, EntityChangedListener<Theme> {
	
	private EntityChangeListenerCollection<Theme> listeners = new EntityChangeListenerCollection<Theme>();
	private ArrayList<ThemeHolder> themesHolder = new ArrayList<ThemeHolder>();
	private ThemeHolder defaultThemeHolder; // holds currently selected theme
	private VirtualThemeHolder virtualThemeHolder; // holds "virtual" theme that contains selected tables 
	public EntityHolder<Table> activeTable = new EntityHolder<Table>();
	
	private static Theme createVirtualTheme() {
		return new Theme();
	}
	public static class VirtualThemeHolder extends ThemeHolder {
		
		private int virtualTTLId=-1; 
		public VirtualThemeHolder() {
			Theme virtualTheme = createVirtualTheme();
			setEntity(virtualTheme);
		}

		private void addTable(int tableId, final String groupName) {
			addTable(tableId, groupName, null);
		}

		private void addTable(int tableId, final String groupName, final AsyncCallback<Table> callback) {
			Repo.instance().getTable(tableId, 0,  new AsyncCallback<Table>() {

				@Override
				public void onFailure(Throwable caught) {
					if (callback!=null)
						callback.onFailure(caught);
				}

				@Override
				public void onSuccess(Table result) {
					addTable(result,groupName);
					if (callback!=null) {
						callback.onSuccess(result);
					}
				}
			});
		}
		private void addTable(Table table, String groupName) {			
			if (entity.hasTable(table.getId()))
				return;
			
			ThemeTableLink ttlToAdd = new ThemeTableLink(entity,table,groupName);
			ttlToAdd.setId(virtualTTLId--);  // can't really go through Integer.MIN_VALUE ids in one session ;)
			ArrayList<ThemeTableLink> tablesList = entity.getThemeTables();
			boolean added=false;
		
			for (int i=0;i<tablesList.size();i++) {
				ThemeTableLink ttl = tablesList.get(i);
				if (added==false && groupName.equals(ttl.group)) {
					tablesList.add(i, ttlToAdd);
					added=true;
					break;
				}
			}
			if (!added) {
				tablesList.add(ttlToAdd);
			}
			
			for (int i=0;i<tablesList.size();i++) {
				ThemeTableLink ttl = tablesList.get(i);
				ttl.orderInTheme = i;
			}

			entity.tables = tablesList.toArray(new ThemeTableLink[tablesList.size()]);
			setEntity(entity);
		}
	}
	
	
	public void addTableToVirtualTheme(int tableId, final String groupName) {
		if (defaultThemeHolder.hasTable(tableId) || virtualThemeHolder.hasTable(tableId))
			return;
		virtualThemeHolder.addTable(tableId, groupName);
	}
	
	public MapLayers () {
		Repo entityRepository = Repo.instance();
		defaultThemeHolder = new ThemeHolder();
		defaultThemeHolder.addEntityChangedListener(this);
		themesHolder.add(defaultThemeHolder);
		
		virtualThemeHolder = new VirtualThemeHolder();
		virtualThemeHolder.addEntityChangedListener(this);
		themesHolder.add(virtualThemeHolder);
		entityRepository.addThemeChangedListener(new EntityChangedListener<Theme>() {

			@Override
			public void onEntityChanged(IsEntityChangedSource source, Theme value) {
				updateThemeInHolders(value);
			}
		});
		
		entityRepository.addTableChangedListener(new EntityChangedListener<Table>() {

			@Override
			public void onEntityChanged(IsEntityChangedSource source, Table value) {
				updateTableInAllThemes(value);
			}
		});
	}
	
	
	private void updateTableInAllThemes(Table table) {
		for (ThemeHolder tHolder:themesHolder) {
			tHolder.updateTable(table);
		}
	}
	
	private void updateThemeInHolders (Theme theme) {
		if (theme==null)
			return;
		if (theme.isDeleted()) {
			if (theme.equals(defaultThemeHolder.getEntity())) {
				Repo.instance().getTheme(ClientGlobals.configuration.defaultThemeId, 0, new AsyncCallback<Theme>() {

					@Override
					public void onFailure(Throwable caught) {
						defaultThemeHolder.setEntity(new Theme());// Report error instead?
					}

					@Override
					public void onSuccess(Theme result) {
						defaultThemeHolder.setEntity(result);
					}
				});
			} else if (theme.equals(virtualThemeHolder.getEntity())) {
				virtualThemeHolder.setEntity(createVirtualTheme());
			}
		} else {
			for (ThemeHolder tHolder:themesHolder) {
				if (tHolder.contains(theme)) {
					tHolder.setEntity(theme);
				}
			}
		}
	}
	
	public ThemeTableLink getThemeTableLinkForTable(Table table, boolean onlyVisible){
		ArrayList<ThemeTableLink> themeTables = new ArrayList<ThemeTableLink>();
		getThemeTables(themeTables, onlyVisible);
		
		for(ThemeTableLink ttl : themeTables){
			if(ttl.getTableId() == table.getId()){
				return ttl;
			}
		}
		return null;
	}

	
	public void getThemeTables(ArrayList<ThemeTableLink> output, boolean onlyVisible) {
	  for (ThemeHolder th:themesHolder) {
		  th.getThemeTables(output,onlyVisible);
	  }
	}
	
	public void getTables(ArrayList<Table> output, boolean onlyVisible) {
	  for (ThemeHolder th:themesHolder) {
		  th.getTables(output,onlyVisible);
	  }
	}
	public void getTablesIds(ArrayList<Integer> output, boolean onlyVisible) {
		  for (ThemeHolder th:themesHolder) {
			  th.getTablesIds(output,onlyVisible);
		  }
		}
	public ThemeSettings getThemeTablesSetting(EntityHolder<Theme> themeHolder) {
		for (ThemeHolder th:themesHolder) {
			if (th == themeHolder) {
				return th.getSettings();
			}
		}
		return null;
	}
	
	
	
	public boolean enableThemeTable(ThemeTableLink ttl, boolean on) {
		if (ttl==null)
			return false;
		for (ThemeHolder th:themesHolder) {
			if (th.enableThemeTable(ttl, on)){
				return true;
			}
		}
		return false;
	}
	public boolean enableThemeTable(int themeTableId, boolean on) {
		ThemeTableLink ttl = null;
		for (ThemeHolder th:themesHolder) {
			if (th.getEntity()!=null) {
				ttl = th.getEntity().getThemeTable(themeTableId);
				if (ttl!=null)
					break;
			}
		}
			return enableThemeTable(ttl, on);
	}
	
	
	
	public void appendThemeLayers(StringBuffer buf) {
		boolean comma = false;
		for (ThemeHolder th:themesHolder) {
			StringBuffer themeBuffer = new StringBuffer();
			th.appendThemeLayers(themeBuffer);
			if (themeBuffer.length()>0) {
				if (comma) {
					buf.append(",");				
				}
				buf.append(themeBuffer);
				comma=true;
			}
			
		}		
	}

	
	public void enableTable(int tableId, boolean on, final AsyncCallback<Table> callback) {
		if (defaultThemeHolder.enableTable(tableId, on)) {
			callback.onSuccess(defaultThemeHolder.getEntity().getTable(tableId));
			return;
		}
		if (virtualThemeHolder.enableTable(tableId, on)) {
			callback.onSuccess(virtualThemeHolder.getEntity().getTable(tableId));
			return;
		}
		
		virtualThemeHolder.addTable(tableId, Messages.INSTANCE.virtualLayersGroupTitle(), new AsyncCallback<Table>() {

			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			@Override
			public void onSuccess(Table result) {
				callback.onSuccess(result);
			}
		});
		
	}
	
	
	public void setDefaultTheme(int themeId) {
		setDefaultTheme(themeId, null);
	}
	
	
	public void setDefaultThemeAndEnableTable(int themeId, final Integer tableId, final AsyncCallback<Void> callback) {
		setDefaultTheme(themeId, new AsyncCallback<ThemeHolder>() {

			@Override
			public void onFailure(Throwable caught) {
				if (callback!=null)
					callback.onFailure(caught);
			}

			@Override
			public void onSuccess(ThemeHolder result) {
				boolean success = true;
				if (tableId!=null) {
					success = result.enableTable(tableId, true);
				}
				if (callback!=null) {
					if (success) {
						callback.onSuccess(null);
					} else {
						callback.onFailure(new IllegalArgumentException("No such table "+tableId+" exists among current themes and tables"));
					}
				}
				
			}
		});
		
	}
	public void setDefaultTheme(int themeId, final AsyncCallback<ThemeHolder> callback) {
		Theme activeTheme = defaultThemeHolder.getEntity();
		if (activeTheme!=null && activeTheme.getId()==themeId) {
			if (callback!=null)
				callback.onSuccess(defaultThemeHolder);
			return;
		}
		
		Repo.instance().getTheme(themeId,0, new AsyncCallback<Theme>() {
			@Override
			public void onFailure(Throwable caught) {
				if (callback!=null) {
					callback.onFailure(caught);
				}
			}

			@Override
			public void onSuccess(Theme result) {
				defaultThemeHolder.setEntity(result);
				if (callback!=null) {
					callback.onSuccess(defaultThemeHolder);
				}
			}
		});
	}

	
	public VirtualThemeHolder getVirtualThemeHolder() {
		return virtualThemeHolder;
	}
	public ThemeHolder getDefaultTheme() {
		return defaultThemeHolder;
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	


	@Override
	public void onEntityChanged(IsEntityChangedSource source, Theme value) {
		listeners.fireValueChanged(source, value);
	}

	public void addValueChangeListener(EntityChangedListener<Theme> entityChangedListener) {
		listeners.add(entityChangedListener);
	}

}
