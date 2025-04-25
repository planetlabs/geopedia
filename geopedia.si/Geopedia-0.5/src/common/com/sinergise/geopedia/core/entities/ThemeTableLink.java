package com.sinergise.geopedia.core.entities;


import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.core.entities.utils.EntityConsts.DataScope;


public class ThemeTableLink extends AbstractNamedEntity {
	private static final long serialVersionUID = -7319656043226015224L;
	public static final int ON_LINE = 1; 
    public static final int ON_FILL = 2; 
    public static final int ON_SYMBOL = 4; 
    public static final int ON_TEXT = 8;
    public static final int ON_DISPLAY = 16;
    public static final int ALL_ON = ON_LINE + ON_FILL + ON_SYMBOL + ON_TEXT + ON_DISPLAY;

    public int themeId;
	public Theme theme;
    public int tableId;
    public Table table;
    public int orderInTheme;
//    public String styleString;
//    public StyleSpec styleSpec;
    public String styleJS;
    public String group;
    public StateGWT properties;
    public int on;
    
    
	@Override
	public ThemeTableLink clone(DataScope scope) {
		ThemeTableLink cloned = new ThemeTableLink();
		cloned.dataScope=scope;
		cloned.id=id;
		cloned.tableId=tableId;
		cloned.themeId=themeId;
		cloned.setName(getName());
		
		if(scope==DataScope.MEDIUM || scope == DataScope.ALL) {
			cloned.on=on;
			cloned.group=group;
			cloned.orderInTheme=orderInTheme;
			cloned.table = table.clone(scope);
		}
		if (scope == DataScope.ALL) {
			if (styleJS!=null)
				cloned.styleJS = new String(styleJS);
			if (properties!=null && !properties.isEmpty()) {
				cloned.properties = new StateGWT();
				cloned.properties.setFrom(properties, true);
			}
		}		
		return cloned;
	}
	
    public ThemeTableLink() 
    {
    	// ...
    }
    

    
    public ThemeTableLink(Theme theme, Table table, String group) {
    	this.on = ALL_ON;
    	this.group=group;
    	this.table=table;
    	this.tableId=table.id;
    	if (theme!=null) {
	    	this.theme=theme;
	    	this.themeId=theme.id;
    	}
    }
//    /**
//     * @param theme
//     * @param table
//     * @param orderInTheme
//     * @param styleString
//     */
//    public ThemeTableLink(int id, Theme theme, Table table, int orderInTheme, String styleString, int onBits, String altName)
//    {
//        this(id,theme.id,table.id,orderInTheme,styleString,onBits,altName);
//        this.table=table;
//        this.theme=theme;
//    }
//    
//    /**
//     * @param theme
//     * @param table
//     * @param orderInTheme
//     * @param styleString
//     */
//    public ThemeTableLink(int id, int themeId, int tableId, int orderInTheme, String styleString, int onBits, String altName)
//    {
//        this.id = id;
//        this.themeId=themeId;
//        this.tableId=tableId;
//        this.orderInTheme = orderInTheme;
//        this.styleString = styleString;
//        this.on=onBits;
//        this.altName=altName;
//    }
    
    public static final ThemeTableLink[] emptyArray = new ThemeTableLink[0];
    
    public boolean isOn() {
        return (on & ON_DISPLAY) != 0;
    }
    /**
     * shortcut for theme=*; themeId=*.id
     * @param th
     */
    public void setTheme(Theme th) {
        this.theme=th;
        this.themeId=th.id;
    }
    /**
     * shortcut for theme=*; themeId=*.id
     * @param th
     */
    public void setTable(Table table) {
        this.table = table;
        this.tableId = table.id;
    }

	public void setOn(boolean newOn)
    {
		if (newOn) {
			on |= ON_DISPLAY;
		} else {
			on &= ~ON_DISPLAY;
		}
    }



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ThemeTableLink other = (ThemeTableLink) obj;
		if (id != other.id)
			return false;
		return true;
	}



	@Override
	public String getName() {
		if (!StringUtil.isNullOrEmpty(name))
			return name;
		if (table!=null)
			return table.name;
		return "ID: "+id;
	}



	public int getTableId() {
		return tableId;
	}



	public Table getTable() {
		return table;
	}


	//FIXME: I need a commet why on earth is this like this...  name.equals(table.name) then return null !?!? and the setter the same..
	public String getAlternativeName() {
		if (StringUtil.isNullOrEmpty(name) || name.equals(table.name))
			return null;
		return name;
	}

	public void setAlternativeName(String _name) {
		if (StringUtil.isNullOrEmpty(_name) || _name.equals(table.name)) {
			name=null;
		} else {
			this.name = _name;
		}
	}

	public String getStyle() {
		return styleJS;
	}

	public void setStyleJS(String styleJS) {
		this.styleJS = styleJS;
	}


	
}
