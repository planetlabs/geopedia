/*
 *
 */
package com.sinergise.java.swing.map.layer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;

import javax.swing.Action;

import com.sinergise.java.swing.actions.Actions;


public class LayerProperties extends HashMap<String, String> {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		public static final String LAYER_ON = "on";
    public static final String MIN_SCALE = "minScale";
    public static final String MAX_SCALE = "maxScale";
    public static final String DESCRIPTION = "description";
    public static final String SMALL_ICON = Action.SMALL_ICON;
    public static final String LARGE_ICON = Actions.LARGE_ICON;
    public static final String CACHING_POLICY = "cachingPolicy";
    public static final String BACKGROUND = "background";
    
    transient private PropertyChangeSupport pcs; 

    public LayerProperties() {
        pcs=new PropertyChangeSupport(this);
        set(BACKGROUND, Boolean.toString(false));
        set(LAYER_ON, Boolean.toString(true));
        set(MIN_SCALE,Double.toString(0));
        set(MAX_SCALE,Double.toString(Double.POSITIVE_INFINITY));
    }
    
    public void set(String key, String value) {
        firePropertyChange(key, super.put(key, value), value);
    }
    @Override
	public String put(String key, String value) {
        String ret=super.put(key, value);
        firePropertyChange(key, ret, value);
        return ret;
    }

    @Override
	public String remove(Object key) {
        String ret=super.remove(key);
        firePropertyChange((String)key, ret, null);
        return ret;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    protected void firePropertyChange(String name, String oldVal, String newVal) {
        pcs.firePropertyChange(name, oldVal, newVal);
    }
    
    public boolean isOn() {
        return Boolean.parseBoolean(get(LAYER_ON));
    }
    public void setOn(boolean onOff) {
        set(LAYER_ON,Boolean.toString(onOff));
    }
    
    public boolean checkScale(double scale) {
        double max=Double.parseDouble(get(MAX_SCALE));
        double min=Double.parseDouble(get(MIN_SCALE));
        return (scale<max && scale>min);
    }
    public void setScaleLimits(double minScale, double maxScale) {
        set(MIN_SCALE,Double.toString(minScale));
        set(MAX_SCALE,Double.toString(maxScale));
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    public boolean isBackground() {
        return Boolean.parseBoolean(get(BACKGROUND));
    }
}
