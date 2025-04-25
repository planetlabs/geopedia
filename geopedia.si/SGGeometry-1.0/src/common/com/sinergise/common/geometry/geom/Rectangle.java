/**
 * 
 */
package com.sinergise.common.geometry.geom;

import com.sinergise.common.geometry.util.GeometryVisitor;
import com.sinergise.common.geometry.util.RectangleVisitor;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.common.util.geom.HasCoordinate;


/**
 * @author tcerovski
 */
public class Rectangle extends GeometryImpl {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Envelope env;
	
	/**
	 * @deprecated default constructor (serialization)
	 */
	@Deprecated
	public Rectangle() {
	}
	
	public Rectangle(HasCoordinate p1, HasCoordinate p2) {
		this(p1.x(), p1.y(), p2.x(), p2.y());
	}
	
	public Rectangle(double x1, double y1, double x2, double y2) {
		EnvelopeBuilder builder = new EnvelopeBuilder(crsRef);
		builder.setMBR(x1, y1, x2, y2);
		env = builder.getEnvelope();
	}
	
	private double a() {
		return Math.abs(env.getWidth());
	}
	
	private double b() {
		return Math.abs(env.getHeight());
	}

	@Override
	public double getArea() {
		return a()*b();
	}

	@Override
	public Envelope getEnvelope() {
		return env;
	}

	@Override
	public double getLength() {
		return 2*a()+2*b();
	}
	
	public LinearRing getOuter() {
		return LinearRing.forEnvelope(env);
	}
	
	public Polygon toPolygon() {
		return Polygon.forEnvelope(env);
	}
	
	@Override
	public boolean isEmpty() {
		return env.isEmpty();
	}
	
	@Override
	public void accept(GeometryVisitor visitor) {
		((RectangleVisitor)visitor).visitRectangle(this);
	}
	
	//Should not have @Override annotation as GWT does not have clone() implemented
	@SuppressWarnings("all") 
	public Rectangle clone() {
		Rectangle r = new Rectangle();
		r.crsRef = crsRef;
		
		if (env != null) {
			r.env = env;
		}
		
		return r;
	}
}
