package com.sinergise.common.geometry.property;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.io.wkt.WKTGeomTagVisitor;
import com.sinergise.common.util.auxprops.PropertyAuxiliaryData;
import com.sinergise.common.util.property.ScalarPropertyImpl;


public final class GeometryProperty extends ScalarPropertyImpl<Geometry> {
	private static final long serialVersionUID = 1L;
	
	public static class GeomPropertyAuxData extends PropertyAuxiliaryData {
		private static final long serialVersionUID = 1L;
		
		public static final String KEY_GEOMINFO = "GeomInfo";
		
		private GeomAuxiliaryProperties geomInfo;
		
	    public GeomAuxiliaryProperties getGeomInfo() {
	    	if (geomInfo == null) {
	    		geomInfo = GeomAuxiliaryProperties.getFrom(this, KEY_GEOMINFO);
	    	}
	    	return geomInfo;
	    }
	}

	/**
	 * @deprecated for serialization only
	 */
	@Deprecated
	public GeometryProperty() {
	}
	
	public GeometryProperty(Geometry value) {
		super(value);
	}
	
	@Override
	public GeomPropertyAuxData getAuxData(boolean create) {
		return (GeomPropertyAuxData)super.getAuxData(create);
	}
	
	@Override
	protected GeomPropertyAuxData constructAuxData() {
		return new GeomPropertyAuxData();
	}
	
	@Override
	public void setValue(Geometry value) {
		super.setValue(value);
	}
	
    @Override
	public String toString() {
    	if (value == null) {
    		return null;
    	}
    	return WKTGeomTagVisitor.getTagFor(value);
    }
    
    public GeomAuxiliaryProperties getGeomAuxData() {
    	return getAuxData(true).getGeomInfo();
    }
}
