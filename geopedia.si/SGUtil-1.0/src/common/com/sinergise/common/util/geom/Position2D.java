package com.sinergise.common.util.geom;

import com.sinergise.common.util.math.MathUtil;
import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.common.util.state.gwt.StateGWTOriginator;
import com.sinergise.common.util.string.HasCanonicalStringRepresentation;
import com.sinergise.common.util.string.StringUtil;

/**
 * Basic {@link HasCoordinate} implementation
 * 
 * @author tcerovski
 */
public class Position2D implements HasCoordinateMutable, Cloneable, StateGWTOriginator, HasCanonicalStringRepresentation {
	private static final long serialVersionUID = 1L;

	public static Position2D valueOf(String posStr) {
		posStr = StringUtil.trimNullEmpty(posStr);
		if (posStr == null) return null;
		
		String[] posSplit = posStr.substring(1, posStr.length() - 1).split("\\s+");
		return new Position2D(Double.parseDouble(posSplit[0]), Double.parseDouble(posSplit[1]));
	}
	
	public double x, y;
	
	public Position2D() {
		this.x = Double.NaN;
		this.y = Double.NaN;
	}
	
	public Position2D(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	
	public Position2D(HasCoordinate c) {
		this(c.x(), c.y());
	}
	
	@Override
	public double x() {
		return x;
	}
	
	@Override
	public double y() {
		return y;
	}
	
	@Override
	public String toString() {
		return toCanonicalString();
	}
	
	@Override
	public Position2D setLocation(HasCoordinate other) {
		return setLocation(other.x(), other.y());
	}
	
	public Position2D setLocation(double x, double y) {
		this.x = x;
		this.y = y;
		return this;
	}
	
	public Position2D plus(HasCoordinate other) {
		return new Position2D(x + other.x(), y + other.y());
	}
	
	public Position2D minus(HasCoordinate other) {
		return new Position2D(x - other.x(), y - other.y());
	}
	
	public Position2D times(double scX, double scY) {
		return new Position2D(scX * x, scY * y);
	}
	
	public Position2D times(double fact) {
		return new Position2D(x * fact, y * fact);
	}
	
//	@Override Should not have this annotation because GWT does not have clone() implemented
	@SuppressWarnings("all")
	public Position2D clone() {
	    return new Position2D(x, y);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + MathUtil.hashCode(x);
		result = prime * result + MathUtil.hashCode(y);
		return result;
	}

	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Position2D)) return false;
		Position2D cmp = (Position2D) obj;
		return ((Double.isNaN(cmp.x) && Double.isNaN(x)) || cmp.x == x) 
			&& ((Double.isNaN(cmp.y) && Double.isNaN(y)) || cmp.y == y);
	}
	
	@Override
	public void loadInternalState(StateGWT st) {
		if (st == null) return;
		x = st.getDouble("x", x);
		y = st.getDouble("y", y);
	}
	
	@Override
	public StateGWT storeInternalState(StateGWT target) {
		if (target == null) target = new StateGWT();
		target.putDouble("x", x);
		target.putDouble("y", y);
		return target;
	}
	
	/**
	 * WKT <point text> - in parentheses, numbers separated by space; see http://www.opengeospatial.org/standards/sfa
	 */
	@Override
	public String toCanonicalString() {
		return "(" + x + " " + y + ")";
	}
	
	public boolean isEmpty() {
		return Double.isNaN(x) || Double.isNaN(y);
	}

	public double distanceFromOrigin() {
		return Math.hypot(x, y);
	}

	public boolean equals2D(HasCoordinate other) {
		return MathUtil.equals(x, other.x()) && MathUtil.equals(y, other.y());
	}

	public static Position2D infinite(int signX, int signY) {
		return new Position2D(infinityForSign(signX), infinityForSign(signY));
	}

	private static double infinityForSign(int signX) {
		return signX == 0 ? 0 : signX > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
	}
}
