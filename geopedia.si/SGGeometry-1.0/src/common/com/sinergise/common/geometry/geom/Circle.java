/**
 * 
 */
package com.sinergise.common.geometry.geom;

import com.sinergise.common.geometry.util.CircleVisitor;
import com.sinergise.common.geometry.util.GeometryVisitor;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.geom.Position2D;


/**
 * @author tcerovski
 */
public class Circle extends GeometryImpl {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public HasCoordinate center;
	public double radius;

	/**
	 * Creates an empty circle
	 */
	public Circle() {
		this(new Position2D(), Double.NaN);
	}
	
	public Circle(HasCoordinate center, double radius) {
		this.center = center;
		this.radius = radius;
	}
	
	public Circle(double cx, double cy, double radius) {
		this(new Position2D(cx, cy), radius);
	}

	@Override
	public double getArea() {
		return Math.PI*radius*radius;
	}

	@Override
	public Envelope getEnvelope() {
		EnvelopeBuilder builder = new EnvelopeBuilder(crsRef);
		if (!isEmpty()) {
			builder.setMBR(center.x()-radius,center.y()-radius,center.x()+radius,center.y()+radius);
		}
		return builder.getEnvelope();
	}

	@Override
	public double getLength() {
		return 2*Math.PI*radius;
	}
	
	public Polygon toPolygon(int numPoints) {
		if (isEmpty()) {
			return new Polygon();
		}
		double[] coords = new double[2*numPoints+2];
		for (int a=0; a<=numPoints; a++) {
			double ang = a==numPoints ? 0 : Math.PI*2*a/numPoints;
			double x = center.x()+Math.cos(ang)*radius;
			double y = center.y()+Math.sin(ang)*radius;
			
			coords[2*a] = x;
			coords[2*a+1] = y;
		}
		return new Polygon(new LinearRing(coords), null);
	}
	
	@Override
	public void accept(GeometryVisitor visitor) {
		((CircleVisitor)visitor).visitCircle(this);
	}
	
	//Should not have @Override annotation as GWT does not have clone() implemented
	@SuppressWarnings("all") 
	public Circle clone() {
		if (isEmpty()) {
			return new Circle();
		}
		Circle c = new Circle(center.x(), center.y(), radius);
		c.crsRef = crsRef;
		return c;
	}
	
	@Override
	public boolean isEmpty() {
		return Double.isNaN(center.x()) || Double.isNaN(center.y()) || Double.isNaN(radius);
	}
	
}
