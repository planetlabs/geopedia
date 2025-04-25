package com.sinergise.common.geometry.geom;

import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.geometry.util.GeometryVisitor;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.geom.Position2D;
import com.sinergise.common.util.math.MathUtil;


public final class Point extends Position2D implements Geometry {

	private static final long serialVersionUID = 1L;

	/**
	 * (x,y) -> (lat, lon)
	 */
	public double z = Double.NaN;

	protected CrsIdentifier crsRef = null;

	public Point() {
		super();
	}

	public Point(double x, double y) {
		super(x, y);
	}

	public Point(Double x, Double y) {
		super(x.doubleValue(), y.doubleValue());
	}

	public Point(double x, double y, double z) {
		super(x, y);
		this.z = z;
	}

	public Point(Point p) {
		this(p.x, p.y, p.z);
	}

	public Point(HasCoordinate c) {
		this(c.x(), c.y());
	}

	public double z() {
		return z;
	}

	@Override
	public CrsIdentifier getCrsId() {
		return crsRef;
	}

	@Override
	public void setCrsId(CrsIdentifier crsRef) {
		this.crsRef = crsRef;
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
	public Envelope getEnvelope() {
		EnvelopeBuilder builder = new EnvelopeBuilder(crsRef);
		if(!isEmpty()){
			builder.expandToInclude(this);
		}
		return builder.getEnvelope();
	}

	/**
	 * @return z value (ellipsoidal height or ) or NaN
	 */
	public double getZ() {
		return z;
	}

	@Override
	public String toString() {
		if (!Double.isNaN(z)) {
			return "POINT (" + x + " " + y + " " + z + ")";
		}
		return "POINT (" + x + " " + y + ")";
	}

	public void setFrom(Point other) {
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
		this.crsRef = other.crsRef;
	}

	public Point setZ(double z) {
		this.z = z;
		return this;
	}

	@Override
	public Point setLocation(double x, double y) {
		super.setLocation(x, y);
		return this;
	}

	@Override
	public Point plus(HasCoordinate other) {
		if (other instanceof Point) {
			return new Point(x + other.x(), y + other.y(), z + ((Point)other).z);
		}
		return new Point(x + other.x(), y + other.y());
	}

	@Override
	public Point minus(HasCoordinate other) {
		if (other instanceof Point) {
			return new Point(x - other.x(), y - other.y(), z - ((Point)other).z);
		}
		return new Point(x - other.x(), y - other.y());
	}

	@Override
	public Point times(double fact) {
		return new Point(x * fact, y * fact, z * fact);
	}

	public double distanceSq2D(Point other) {
		return GeomUtil.distanceSq(x, y, other.x, other.y);
	}

	protected Point cloneInto(Point p) {
		p.crsRef = crsRef;
		p.x = x;
		p.y = y;
		p.z = z;
		return p;
	}

	//	@Override Should not have this annotation because GWT does not have clone() implemented
	@SuppressWarnings("all")
	public Point clone() {
		return cloneInto(new Point());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + MathUtil.hashCode(z);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Point)) {
			return false;
		}
		Point cmp = (Point)obj;
		return (MathUtil.equals(cmp.x,x)
			&& MathUtil.equals(cmp.y, y)
			&& MathUtil.equals(cmp.z, z));
	}

	@Override
	public void accept(GeometryVisitor visitor) {
		visitor.visitPoint(this);
	}

	public double distanceSq3D(Point other) {
		double d = x - other.x;
		double sum = d * d;
		d = y - other.y;
		sum += d * d;
		d = z - other.z;
		sum += d * d;
		return sum;
	}
	
	@Override
	public Geometry getGeometry() {
		return this;
	}
}
