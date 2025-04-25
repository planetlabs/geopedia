package com.sinergise.common.geometry.geom;

import java.util.List;

import com.sinergise.common.geometry.util.GeometryVisitor;

public final class MultiPolygon extends GeometryCollection<Polygon> {
	private static final long serialVersionUID = 1L;

	public MultiPolygon() {
		// ...
	}

	public MultiPolygon(Polygon ... polys) {
		super(polys);
	}

	public MultiPolygon(List<? extends Polygon> polys) {
		super(polys);
	}
	
	@Override
	String getCollectionName() {
		return "MULTIPOLYGON";
	}
	
	@Override
	public void accept(GeometryVisitor visitor) {
		visitor.visitMultiPolygon(this);
	}
	
	//Should not have @Override annotation as GWT does not have clone() implemented
	@SuppressWarnings("all") 
	public MultiPolygon clone() {
		MultiPolygon col = new MultiPolygon();
		cloneInto(col);
		return col;
	}
}
