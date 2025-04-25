/*
 *
 */
package com.sinergise.common.gis.ogc;

import com.sinergise.common.util.state.gwt.StateGWT;

public class OGCRequestContext extends OGCObject {
    public static final String PROP_USER_NAME="username";
    protected transient Object localSession;
    public OGCRequestContext() {
        this(new StateGWT());
    }

    public OGCRequestContext(StateGWT initState) {
        super(initState);
    }
    
    public String getUserName() {
        return properties.getString(PROP_USER_NAME, null);
    }
    
    public String getProperty(String propertyName, String defaultValue) {
        return properties.getString(propertyName, defaultValue);
    }
    
    public void setLocalSession(Object sessionObject) {
    	this.localSession=sessionObject;
    }
    
    public Object getLocalSession() {
		return localSession;
	}
}
