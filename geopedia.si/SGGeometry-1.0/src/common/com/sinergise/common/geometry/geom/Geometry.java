package com.sinergise.common.geometry.geom;

import com.sinergise.common.geometry.util.GeometryVisitor;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.crs.HasCrsIdentifier;
import com.sinergise.common.util.geom.HasEnvelope;

public interface Geometry extends Cloneable, HasGeometry, HasEnvelope, HasCrsIdentifier {

	public double getLength();

	public double getArea();
	
	//TODO: Remove this from Geometry; Geometry instances should be immutable
	public void setCrsId(CrsIdentifier crsId);
	
	public void accept(GeometryVisitor visitor);
	
	public Geometry clone();
	
	public boolean isEmpty();
}
