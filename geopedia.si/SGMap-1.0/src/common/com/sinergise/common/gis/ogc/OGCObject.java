/*
 *
 */
package com.sinergise.common.gis.ogc;


import java.io.Serializable;

import com.sinergise.common.util.state.gwt.StateGWT;

public class OGCObject implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	protected StateGWT properties;

	public OGCObject() {
	}

	public OGCObject(StateGWT sourceState) {
		this.properties = sourceState;
	}

	public StateGWT getProperties() {
		return properties;
	}
}
