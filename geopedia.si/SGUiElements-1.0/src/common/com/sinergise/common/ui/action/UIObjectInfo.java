/*
 *
 */
package com.sinergise.common.ui.action;

import java.util.HashMap;

import com.sinergise.common.util.Util;
import com.sinergise.common.util.state.gwt.PropertyChangeListener;
import com.sinergise.common.util.state.gwt.PropertyChangeListenerCollection;
import com.sinergise.common.util.state.gwt.SourcesPropertyChangeEvents;



public class UIObjectInfo implements SourcesPropertyChangeEvents<Object> {
    public static final String NAME="name";
    public static final String DESC="desc";
    public static final String CURSOR = "cursorCSS";
    
    @Deprecated
    public static final String ICON_16 = "icon16";
    @Deprecated
    public static final String SMALL_ICON_URL=ICON_16;
    @Deprecated
    public static final String DISABLED_ICON_16 = "disabledIcon16";
    @Deprecated
    public static final String SMALL_DISABLED_ICON_URL=DISABLED_ICON_16;
    @Deprecated
    public static final String LARGE_ICON_URL="largeIcon";

    public static final String ICON_RES_16 = "iconResource16";
    public static final String DISABLED_ICON_RES_16 = "disabledIconResource16";
    public static final String LARGE_ICON_RES="largeIconResource";

    
    public static final String STYLE="style";
    public static final String PRIMARY_STYLE="primary-style";
    
    public static final String ENABLED="enabled";
    public static final String EXTERNAL_ENABLED="enabledOverride";
    /**
     * A read-only property used internally for enabling/disabling the action
     * (e.g. if the internal state of the action doesn't permit it to be executed)
     * This has to be different from ENABLED, which is set by the user and is a real read-write property. 
     */
    protected static final String INTERNAL_ENABLED="internalEnabled";
    public static final String VISIBLE="visible";
    private HashMap<String, Object> props=new HashMap<String, Object>();
    private PropertyChangeListenerCollection<Object> listeners; 
    
    public UIObjectInfo setProperty(String name, Object newValue) {
        boolean oldEn = isEnabled();
        Object oldValue = silentSetProperty(name, newValue);
        if (listeners==null || Util.safeEquals(oldValue, newValue)) {
        	return this;
        }
        
        if (EXTERNAL_ENABLED.equals(name) || INTERNAL_ENABLED.equals(name)) {
        	boolean newEn=isEnabled();
            if (oldEn!=newEn) {
            	firePropertyChange(ENABLED, new Boolean(oldEn), new Boolean(newEn));
            }
        }
        firePropertyChange(name, oldValue, newValue);
        return this;
    }

	protected void firePropertyChange(String name, Object oldValue, Object newValue) {
		if (listeners != null) {
			listeners.fireChange(this, name, oldValue, newValue);
		}
	}

	protected Object silentSetProperty(String name, Object value) {
		return props.put(name, value);
	}
    
    public void addPropertyChangeListener(PropertyChangeListener<Object> listener) {
        if (listeners==null) {
            listeners=new PropertyChangeListenerCollection<Object>();   
        }
        listeners.add(listener);
    }
    public void removePropertyChangeListener(PropertyChangeListener<Object> listener) {
        if (listeners!=null) {
            listeners.remove(listener);
        }
    }
    
    public void setName(String name) {
        setProperty(NAME,name);
    }
    
    public Object getProperty(String name) {
        if (ENABLED.equals(name)) {
            return  Boolean.valueOf(isEnabled());
        }
        return props.get(name);
    }
    
    public void setDescription(String htmlDesc) {
        setProperty(DESC, htmlDesc);
    }
    
    public String getDescription() {
        return (String)getProperty(DESC);
    }
    
    public String getName() {
        return (String)getProperty(NAME);
    }
    public void setExternalEnabled(boolean b) {
        setProperty(EXTERNAL_ENABLED, Boolean.valueOf(b));
    }
    public boolean isEnabled() {
        return getBooleanValue(EXTERNAL_ENABLED, true) && getBooleanValue(INTERNAL_ENABLED, true);
    }
    
    protected void setInternalEnabled(boolean en) {
    	setProperty(INTERNAL_ENABLED, Boolean.valueOf(en));
    }
    
    protected boolean getBooleanValue(String name, boolean defVal) {
    	return Util.isTrue((Boolean)getProperty(name), defVal);
    }
    
    public void setVisible(boolean b) {
        setProperty(VISIBLE, Boolean.valueOf(b));
    }
    public boolean isVisible() {
        return getBooleanValue(VISIBLE, true);
    }
    
    @Override
	public String toString() {
        return getName();
    }
    
    public void setStyle(String style) {
    	setProperty(STYLE, style);
    }
    
    public String getStyle() {
    	return (String)getProperty(STYLE);
    }
    
    public void setPrimaryStyle(String style) {
    	setProperty(PRIMARY_STYLE, style);
    }
    
    public String getPrimaryStyle() {
    	return (String)getProperty(PRIMARY_STYLE);
    }
}