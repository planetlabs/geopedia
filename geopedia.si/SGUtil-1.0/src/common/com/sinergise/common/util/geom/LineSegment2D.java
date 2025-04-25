package com.sinergise.common.util.geom;

import static com.sinergise.common.util.math.MathUtil.roundToNearestMultiple;

import java.io.Serializable;

import com.sinergise.common.util.math.MathUtil;

/**
 * @author tcerovski
 *
 */
public class LineSegment2D implements CoordinatePairMutable, Serializable, Cloneable {

	private static final long serialVersionUID = 1L;
	
	public HasCoordinate c1, c2;
	
	public LineSegment2D() {
		this(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
	}
	
	public LineSegment2D(double x1, double y1, double x2, double y2) {
		this(new Position2D(x1, y1), new Position2D(x2, y2));
	}
	
	
	public LineSegment2D(HasCoordinate c1, HasCoordinate c2) {
		this.c1 = c1;
		this.c2 = c2;
	}
	
	public LineSegment2D(CoordinatePair seg) {
		this(seg.c1(), seg.c2());
	}

	@Override
	public HasCoordinate c1() {
		return c1;
	}
	
	@Override
	public HasCoordinate c2() {
		return c2;
	}
	
	@Override
	public double x1() {
		if (c1 == null) {
			return Double.NaN;
		}
		return c1.x();
	}
	
	@Override
	public double y1() {
		if (c1 == null) {
			return Double.NaN;
		}
		return c1.y();
	}
	
	@Override
	public double x2() {
		if (c2 == null) {
			return Double.NaN;
		}
		return c2.x();
	}
	
	@Override
	public double y2() {
		if (c2 == null) {
			return Double.NaN;
		}
		return c2.y();
	}
	
	@Override
	public LineSegment2D setCoordinate1(HasCoordinate c) {
		c1 = c;
		return this;
	}
	
	public void setCoordinate1(double x, double y) {
		c1 = new Position2D(x, y);
	}
	
	@Override
	public LineSegment2D setCoordinate2(HasCoordinate c) {
		c2 = c;
		return this;
	}
	
	public void setCoordinate2(double x, double y) {
		c2 = new Position2D(x, y);
	}
	
	public Envelope getEnvelope() {
		return new Envelope(x1(), y1(), x2(), y2());
	}
	
//	@Override Should not have this annotation because GWT does not have clone() implemented
	@SuppressWarnings("all")
	public LineSegment2D clone() {
	    return new LineSegment2D(x1(), y1(), x2(), y2());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((c1 == null) ? 0 : c1.hashCode());
		result = prime * result + ((c2 == null) ? 0 : c2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		LineSegment2D other = (LineSegment2D)obj;
		if (c1 == null) {
			if (other.c1 != null) return false;
		} else if (!c1.equals(other.c1)) return false;
		if (c2 == null) {
			if (other.c2 != null) return false;
		} else if (!c2.equals(other.c2)) return false;
		return true;
	}
	
	public boolean equals(double x1, double y1, double x2, double y2) {
		return (MathUtil.fastCompare(x1(), x1) == 0
			&& MathUtil.fastCompare(y1(), y1) == 0
			&& MathUtil.fastCompare(x2(), x2) == 0
			&& MathUtil.fastCompare(y2(), y2) == 0)
			|| (MathUtil.fastCompare(x1(), x2) == 0
			&& MathUtil.fastCompare(y1(), y2) == 0
			&& MathUtil.fastCompare(x2(), x1) == 0
			&& MathUtil.fastCompare(y2(), y1) == 0 );
	}
	
	@Override
	public String toString() {
		return "(" + x1() + "," + y1() + " " + x2()+ "," + y2() + ")";
	}

	public double getMaxY() {
		return Math.max(y1(), y2());
	}
	
	public void snapToGrid(double gridSize) {
		if (gridSize <= 0) {
			return;
		}
		
		setCoordinate1(
			roundToNearestMultiple(x1(), gridSize),
			roundToNearestMultiple(y1(), gridSize));
		setCoordinate2(
			roundToNearestMultiple(x2(), gridSize),
			roundToNearestMultiple(y2(), gridSize));
	}
	
}
