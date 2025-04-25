/**
 * 
 */
package com.sinergise.common.util.geom;

import java.io.Serializable;

import com.sinergise.common.util.math.Angle;

/**
 * @author tcerovski
 */
public class Bearing implements Serializable {

	private static final long serialVersionUID = 8987235533310690842L;
	
	private double bearing;
	
	/** Serialization only */
	@Deprecated
	protected Bearing() { 
		this(Double.NaN,Double.NaN,Double.NaN,Double.NaN);
	}
	
	public Bearing(double bearingDeg) {
		this.bearing = bearingDeg;
	}
	
	public Bearing(HasCoordinate c1, HasCoordinate c2) {
		this(c1.x(), c1.y(), c2.x(), c2.y());
	}
	
	public Bearing(HasCoordinate c1, HasCoordinate c2, boolean southToNorth) {
		this(c1.x(), c1.y(), c2.x(), c2.y(), southToNorth);
	}
	
	public Bearing(double x1, double y1, double x2, double y2) {
		this(x1, y1, x2, y2, false);
	}
	
	public Bearing(double x1, double y1, double x2, double y2, boolean southToNorth) {
		//orient South to North
		if(southToNorth && y1 > y2) {
			double tx1 = x1;
			double ty1 = y1;
			x1 = x2;
			y1 = y2;
			x2 = tx1;
			y2 = ty1;
		} 
		
		bearing = Math.toDegrees(Math.atan2(x2-x1, y2-y1));
	}
	
	public double getBearing() {
		return bearing;
	}
	
	public double getBearingRad() {
		return Math.toRadians(bearing);
	}
	
	public String getBearingLabel() {
		double absBear = Math.abs(bearing); 
		int deg = (int)absBear;
		double minT = (absBear-deg)*60;
		int min = (int)(minT);
		int sek = (int)(Math.round((minT - min)*60));
		return deg+"\u00b0 "+min+"\" "+sek+ "' "+(bearing<0?"W":"E");
	}
	
	public String getBearingDegreesLabel() {
		return (int)Math.abs(bearing)+"\u00b0 "+(bearing<0?"W":"E");
	}
	
	@Override
	public String toString() {
		return getBearingLabel();
	}
	
	public static Bearing parseBearing(String s) {
		Angle angle = Angle.parseAngle(s);
		if (angle == null) {
			return null;
		}
		
		double deg = angle.getAngleDeg();
		s = s.trim();
		if(s.substring(s.length()-1).equalsIgnoreCase("W")) {
			deg = -deg;
		}
		
		return new Bearing(deg);
	}
	
	/**
	 * TODO: This treats Bearing as south oriented; refactor to have consistent orientation or take orientation into account 
	 */
	public static HasCoordinate getCoordinateFromDistAndBearing(HasCoordinate start, double dist, Bearing bearing) {
		double angle = Math.toRadians(bearing.getBearing());
		return new Position2D(
				start.x() + Math.sin(angle)*dist,
				start.y() + Math.cos(angle)*dist);
	}
	
	
}
