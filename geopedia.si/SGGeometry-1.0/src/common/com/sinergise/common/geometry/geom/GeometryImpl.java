package com.sinergise.common.geometry.geom;

import java.io.Serializable;

import com.sinergise.common.util.crs.CrsIdentifier;

/**
 * @author tcerovski
 *
 */
public abstract class GeometryImpl implements Serializable, Geometry {

	private static final long serialVersionUID = 1L;

	protected CrsIdentifier crsRef = null;
	
	@Override
	public CrsIdentifier getCrsId() {
		return crsRef;
	}
	
	@Override
	public void setCrsId(CrsIdentifier crsRef) {
		this.crsRef = crsRef;
	}
	
	//Should not have @Override annotation as GWT does not have clone() implemented
	@SuppressWarnings("all") 
	public abstract GeometryImpl clone();
	
	@Override
	public Geometry getGeometry() {
		return this;
	}
}
