package com.sinergise.common.geometry.geom;

import java.util.List;

import com.sinergise.common.geometry.util.GeometryVisitor;

public final class MultiPoint extends GeometryCollection<Point> {
	private static final long serialVersionUID = 1L;

	public MultiPoint() {
		// ...
	}

	public MultiPoint(Point ... points) {
		super(points);
	}

	public MultiPoint(List<? extends Point> points) {
		super(points);
	}
	
	@Override
	public double getArea() {
		return 0;
	}

	@Override
	public double getLength() {
		return 0;
	}

	@Override
	String getCollectionName() {
		return "MULTIPOINT";
	}
	
	@Override
	public void accept(GeometryVisitor visitor) {
		visitor.visitMultiPoint(this);
	}
	
	//Should not have @Override annotation as GWT does not have clone() implemented
	@SuppressWarnings("all") 
	public MultiPoint clone() {
		MultiPoint col = new MultiPoint();
		cloneInto(col);
		return col;
	}	
}
