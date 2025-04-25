package com.sinergise.common.geometry.geom;

import java.util.List;

import com.sinergise.common.geometry.util.GeometryVisitor;

public final class MultiLineString extends GeometryCollection<LineString> {
	private static final long serialVersionUID = 1L;

	public MultiLineString() {
		// ...
	}

	public MultiLineString(LineString ... lines) {
		super(lines);
	}
	
	public MultiLineString(List<? extends LineString> lines) {
		super(lines);
	}

	@Override
	public double getArea() {
		return 0;
	}

	@Override
	String getCollectionName() {
		return "MULTILINESTRING";
	}
	
	@Override
	public void accept(GeometryVisitor visitor) {
		visitor.visitMultiLineString(this);
	}
	
	//Should not have @Override annotation as GWT does not have clone() implemented
	@SuppressWarnings("all") 
	public MultiLineString clone() {
		MultiLineString col = new MultiLineString();
		cloneInto(col);
		return col;
	}	
}
