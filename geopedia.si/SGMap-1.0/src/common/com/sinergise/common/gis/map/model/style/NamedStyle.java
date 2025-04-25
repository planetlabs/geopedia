/*
 *
 */
package com.sinergise.common.gis.map.model.style;

import com.sinergise.common.util.state.gwt.PropertyMap;

public class NamedStyle extends PropertyMap implements Style {
    public static final String PROP_NAME="name";
    public NamedStyle(String name) {
        super();
        set(PROP_NAME, name);
    }
    
    public String getName() {
        return getString(PROP_NAME, null);
    }
    
    @Override
	public void reset() {
        //Nothing to do
    }
}
