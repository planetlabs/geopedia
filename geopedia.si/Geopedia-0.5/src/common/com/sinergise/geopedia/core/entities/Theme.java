package com.sinergise.geopedia.core.entities;


import java.util.ArrayList;

import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.geopedia.core.config.ThemeBaseLayers;
import com.sinergise.geopedia.core.entities.utils.EntityConsts;


public class Theme extends AbstractEntityWithDescription implements EntityConsts
{

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_LOGO = "logoUrl";
	
	public ThemeTableLink[] tables = new ThemeTableLink[0];
	public int public_perms;
	public long lastMetaChange;
	public ThemeBaseLayers baseLayers;
    public StateGWT properties;
    
    /** Permissions this user has (or -1, if user isn't logged in) */
	public int user_perms;
    
	
	@Override
	public Theme clone(DataScope scope) {
		Theme cloned = new Theme();
		cloned.dataScope=scope;
		cloned.id =id;
		cloned.setName(name);
		cloned.public_perms = public_perms;
		if (scope == DataScope.MEDIUM || scope == DataScope.ALL) {
			cloned.descDisplayableHtml=descDisplayableHtml;
			cloned.lastMetaChange = lastMetaChange;
			if (baseLayers!=null)
				cloned.baseLayers = baseLayers.clone();

			if (tables!=null) {
				cloned.tables = new ThemeTableLink[tables.length];
				for (int i=0;i<tables.length;i++) {
					cloned.tables[i] = tables[i].clone(scope);
				}
			}
		}
		
		if (scope == DataScope.ALL) {
			cloned.descRawHtml = descRawHtml;
			if (properties != null && !properties.isEmpty()) {
				cloned.properties = new StateGWT();
				cloned.properties.setFrom(properties, true);
				cloned.user_perms=user_perms;
			}
			
		}
		return cloned;
	}
	
    
    public ThemeTableLink getLinkForId(int ttlId) {
    	for (ThemeTableLink l : tables) {
			if (ttlId == l.id) return l;
		}
    	return null;
    }
    
    public boolean hasLink(int themeTableLinkId){
    	for (ThemeTableLink l : tables) {
			if(themeTableLinkId == l.id) return true;
    	}
		return false;
    }
    
    
    public ThemeTableLink getThemeTable(int themeTableId) {
    	for (ThemeTableLink ttl:tables) {
    		if (ttl.getId()==themeTableId)
    			return ttl;
    	}
    	return null;
    }
    public int getTableId(int themeTableLinkId){
    	for (ThemeTableLink l : tables){ 
    		if(themeTableLinkId == l.id) return l.tableId;
    	}
    	throw new IllegalArgumentException("No table for this theme table link ID");
    }

	public boolean hasTable(int id) {
		for (ThemeTableLink l : tables) {
			if (l.tableId == id) return true;
		}
		return false;
	}

	public ThemeTableLink getLinkForTable(int tableId) {
		for (ThemeTableLink l : tables) {
			if (l.tableId == tableId) return l;
		}
		return null;
	}

	public boolean equals(Theme theme) {
		if (theme==null)
			return false;
		if (theme.id==id)
			return true;
		return false;
	}
	
	public ArrayList<ThemeTableLink> getThemeTables() {
		ArrayList<ThemeTableLink> list = new ArrayList<ThemeTableLink>();
		if (tables!=null) {
			for (ThemeTableLink ttl:tables) {
				list.add(ttl);
			}
		}
		return list;
	}

	
	public Table getTable(int tableId) {
		ThemeTableLink ttl = getLinkForTable(tableId);
		if (ttl!=null)
			return ttl.table;
		return null;
	}
	
	public boolean updateTable(Table table) {
		boolean anythingUpdated=false;
		if (table.isDeleted()) {
			ArrayList<ThemeTableLink> list = new ArrayList<ThemeTableLink>();
			for (ThemeTableLink ttl:tables) {
				if (ttl.getTableId()!=table.getId()) {
					list.add(ttl);
				} else {
					anythingUpdated=true;
				}
			}
			if (anythingUpdated) {
				tables = list.toArray(new ThemeTableLink[list.size()]);
			}
		} else {
			for (ThemeTableLink ttl:tables) {
				if (ttl.getTableId()==table.getId() && ttl.getTable().lastMetaChange<table.lastMetaChange) {
					anythingUpdated=true;
					ttl.setTable(table);
				}
			}
		}
		return anythingUpdated;
	}

}
