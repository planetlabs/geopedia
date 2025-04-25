package com.sinergise.common.geometry.property;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.io.wkt.WKTReader;
import com.sinergise.common.geometry.io.wkt.WKTWriter;
import com.sinergise.common.util.io.ObjectReader.ObjectReadException;
import com.sinergise.common.util.property.Property;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;
import com.sinergise.common.util.property.descriptor.PropertyType;

public class GeometryPropertyType extends PropertyType<Geometry> {
	private static final long serialVersionUID = 1643232015676321724L;

	//this is hidden so it is not used with the normal PropertyType which can yield non expected results
	private static final String VALUE_TYPE_GEOMETRY = "Geometry";
	
	public static final PropertyType<Geometry> GENERIC_GEOMETRY = new GeometryPropertyType().lock();

	public GeometryPropertyType() {
		super(VALUE_TYPE_GEOMETRY);
	}

	@Override
	public Property<Geometry> createProperty(Geometry value) {
		return new GeometryProperty(value);
	}
	
	@Override
	public boolean isTypeCompatible(Property<?> prop) {
		if (prop == null || prop.isNull()) {
			return true;
		}
		Object value = prop.getValue();
		
		return value instanceof Geometry;	
	}
	
	@Override
	public String toCanonicalString(Geometry value) {
		return WKTWriter.write(value);
	}
	
	@Override
	public Geometry fromCanonicalString(String sValue) {
		try {
			return WKTReader.read(sValue);
		} catch(ObjectReadException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static final boolean isGeometryProperty(PropertyDescriptor<?> pDesc) {
		return pDesc.getType().isType(VALUE_TYPE_GEOMETRY);
	}
}
