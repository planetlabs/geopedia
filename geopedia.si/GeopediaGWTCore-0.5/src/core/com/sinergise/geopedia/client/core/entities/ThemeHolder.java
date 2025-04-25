package com.sinergise.geopedia.client.core.entities;

import java.util.ArrayList;
import java.util.HashMap;

import com.sinergise.geopedia.core.common.TileUtil;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.ThemeTableLink;

public class ThemeHolder extends EntityHolder<Theme>{
	
	private ThemeSettings settings;
	public static class TableSettings {
		boolean on = false;
		public TableSettings(boolean on) {
			this.on=on;
		}
		public boolean isOn() {
			return on;
		}
		public void setOn(boolean on) {
			this.on=on;
		}
	}
	
	public static class ThemeSettings {
		public HashMap<ThemeTableLink, TableSettings> themeTablesSettings = new HashMap<ThemeTableLink,TableSettings>();
		public ThemeSettings (Theme theme, ThemeSettings oldThemeSettings) {
			if (theme!=null) {
				for (ThemeTableLink ttl: theme.tables) {
					TableSettings settings = null;					
					if (oldThemeSettings!=null) {
						settings = oldThemeSettings.getThemeTableSettings(ttl);
					}
					if (settings==null) {
						settings = new TableSettings(ttl.isOn());
					}
					themeTablesSettings.put(ttl, settings);
				}
			}
		}
		
		public void enableAll(boolean on) {
			for (TableSettings settings:themeTablesSettings.values()) {
				settings.setOn(on);
			}
			
		}

		public TableSettings getThemeTableSettings (ThemeTableLink ttl) {
			return themeTablesSettings.get(ttl);
		}
	}
	/**
	 * If wrapped theme contains the table (check by id) it is replaced by the teme in argument.
	 * TODO: check timestamp/versionID
	 * @param table
	 */
	public void updateTable(Table table) {
		Theme theme = getEntity();
		if (theme==null) return;
		if (theme.updateTable(table))
			onEntityChanged();
	}
	
	public void setEntity(Theme entity) {
		if (getEntity()!=null && getEntity().equals(entity)) {
			settings = new ThemeSettings(entity,settings);
		} else {
			settings = new ThemeSettings(entity,null);
		}
		super.setEntity(entity);
	}
	
	public ThemeSettings getSettings() {
		return settings;
	}
	
	
	public void getTablesIds(ArrayList<Integer> output, boolean onlyVisible) {
		Theme theme = getEntity();
		if (theme==null) return;
		for (ThemeTableLink ttl:theme.tables) {
			TableSettings ts = settings.getThemeTableSettings(ttl);
			if (onlyVisible) {
				if (ts!=null && ts.isOn()) {
					output.add(ttl.tableId);	
				}
			} else {
				output.add(ttl.tableId);
			}
		}
	}
	
	
	public void getThemeTables(ArrayList<ThemeTableLink> output, boolean onlyVisible) {
		Theme theme = getEntity();
		if (theme==null) return;
		for (ThemeTableLink ttl:theme.tables) {
			TableSettings ts = settings.getThemeTableSettings(ttl);
			if (onlyVisible) {
				if (ts!=null && ts.isOn() && !output.contains(ttl.table)) {
					output.add(ttl);	
				}
			} else if (!output.contains(ttl)){
				output.add(ttl);
			}
		}
		
	}
		
	public void getTables(ArrayList<Table> output, boolean onlyVisible) {
		Theme theme = getEntity();
		if (theme==null) return;
		for (ThemeTableLink ttl:theme.tables) {
			TableSettings ts = settings.getThemeTableSettings(ttl);
			if (onlyVisible) {
				if (ts!=null && ts.isOn() && !output.contains(ttl.table)) {
					output.add(ttl.table);	
				}
			} else if (!output.contains(ttl.table)){
				output.add(ttl.table);
			}
		}
	}
	
	public boolean enableThemeTable(ThemeTableLink ttl, boolean enable) {
		if (settings!=null) {
			TableSettings tblSettings = settings.getThemeTableSettings(ttl);
			if (tblSettings!=null) {
				tblSettings.setOn(enable);
				onEntityChanged();
				return true;
			}
		}
		return false;
	}
	
	public boolean enableTable(Integer tableId, boolean on) {
		if (getEntity()!=null) {
			for (ThemeTableLink ttl:getEntity().tables) {
				if (ttl.getTableId() == tableId) {
					return enableThemeTable(ttl, on);
				}
			}
		}
		return false;
	}
	

	
	public void enableAll(boolean on) {
		if (settings!=null) {
			settings.enableAll(on);
			onEntityChanged();
		}
		
	}


	public void appendThemeLayers(StringBuffer buf) {
		Theme theme = getEntity();
		if (theme==null) return;
		
		boolean comma = false;
		for (int i=theme.tables.length-1;i>=0;i--) {
			ThemeTableLink ttl = theme.tables[i];
			TableSettings ts = settings.getThemeTableSettings(ttl);
			if (ts!=null) {
				if (ttl.table!=null && !ttl.table.getGeometryType().isGeom())
					continue;
				if (ts.isOn()) {
					if (comma) {
						buf.append(',');
					}
					if (!ttl.hasValidId() && ttl.table != null) {
						TileUtil.appendLayerSpec(ttl.table, ttl.on, buf);
					} else {
						TileUtil.appendThemeLayerSpec(ttl, ttl.on, buf);
					}
					comma = true;						
				}
			}
		}
	}

	public boolean hasTable(int tableId) {
		if (getEntity()==null) return false;
		return getEntity().hasTable(tableId);
		
	}
	
	
}
