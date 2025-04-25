/*
 *
 */
package com.sinergise.common.gis.map.model.style;

import java.util.ArrayList;

import com.sinergise.common.util.state.gwt.PropertyMap;
import com.sinergise.common.util.state.gwt.StateGWT;

public abstract class StyleComponent extends PropertyMap {
    public static final String PROP_ON="on";
    public static final String PROP_COMPONENT_NAME="componentName";
	//protected PopupPanel auxDataPanel = null;
	
	protected StateGWT state;
	protected ArrayList<StylePropertyElement> auxParams;
	protected String styleSessionParamName;
    
    public StyleComponent(String componentName) {
    	set(PROP_COMPONENT_NAME, componentName);
	}
    
    public boolean setOn(boolean on) {
        Boolean ret=(Boolean)set(PROP_ON, Boolean.valueOf(on));
        if (ret==null) return false;
        return ret.booleanValue();
    }
    public boolean isOn() {
        return getBoolean(PROP_ON, false);
    }
    public String getComponentName() {
    	return super.getString(PROP_COMPONENT_NAME, null);
    }
    public abstract void reset();

	public ArrayList<StylePropertyElement> getAuxParams() {
		return auxParams;
	}

	public boolean canHandleAuxData(String compName) {
		boolean canHandel = false;
		
		if(auxParams != null && state != null && styleSessionParamName != null && styleSessionParamName.length() > 0){
			for (StylePropertyElement spe : auxParams) {
				if (compName.equals(spe.styleType)) {
					return true;
				}
			}
		}
		
		return canHandel;
	}

	public void setAuxParams(ArrayList<StylePropertyElement> auxParams) {
		this.auxParams = auxParams;
	}

	public StateGWT getState() {
		return state;
	}

	public String getStyleSessionParamName() {
		return styleSessionParamName;
	}

}
