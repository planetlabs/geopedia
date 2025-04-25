/*
 *
 */
package com.sinergise.common.gis.map.model.style;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.sinergise.common.util.state.gwt.PropertyChangeListener;
import com.sinergise.common.util.state.gwt.PropertyChangeListenerCollection;


public class ComponentStyle implements Style {
    public static final String COMP_TEXT="T";
    public static final String COMP_LINE="L";
    public static final String COMP_FILL="F";
    public static final String COMP_SYMBOL="S";
    public static final String COMP_CLUSTER="C";
    
    /**
     */
	private HashMap<String, StyleComponent> components = new LinkedHashMap<String, StyleComponent>();

    PropertyChangeListenerCollection<Object> listeners;
    
    PropertyChangeListener<Object> compList=new PropertyChangeListener<Object>() {
		@Override
		public void propertyChange(Object sender, String propertyName, Object oldValue, Object newValue) {
			if (listeners==null) return;
			String fullName=((StyleComponent)sender).getComponentName()+"."+propertyName;
			listeners.fireChange(ComponentStyle.this, fullName, oldValue, newValue);
		}
	};
    
    protected void addComponent(StyleComponent comp) {
    	components.put(comp.getComponentName(), comp);
    	comp.addPropertyChangeListener(compList);
    }
    
    protected void removeComponent(StyleComponent comp) {
    	if (comp==components.remove(comp)) {
    		comp.removePropertyChangeListener(compList);
    	}
    }
    
    @Override
	public void addPropertyChangeListener(PropertyChangeListener<Object> listener) {
    	if (listeners==null) listeners=new PropertyChangeListenerCollection<Object>();
    	listeners.add(listener);
    }
    @Override
	public void removePropertyChangeListener(PropertyChangeListener<Object> listener) {
    	if (listeners==null) return;
    	listeners.remove(listener);
    }
    
    public StyleComponent getComponent(String compName) {
        return components.get(compName);
    }
    public void setOn(String compName, boolean on) {
        getComponent(compName).setOn(on);
    }
    
    public Iterator<java.lang.String> namesIterator() {
        return components.keySet().iterator();
    }
    
    @Override
	public void reset() {
        for (StyleComponent cmp: components.values()) {
            cmp.reset();
        } 
    }
    
    protected Iterator<com.sinergise.common.gis.map.model.style.StyleComponent> valuesIterator() {
    	return components.values().iterator();
    }
}
