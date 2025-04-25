/**
 * 
 */
package com.sinergise.common.util.math;

import java.io.Serializable;

/**
 * @author tcerovski
 */
public class Angle implements Serializable {

	private static final long serialVersionUID = -5029629581839193941L;
	
	private double angleDeg;
	
	/** Serialization only */
	@Deprecated
	protected Angle() { 
		this(Double.NaN);
	}
	
	public Angle(double angleDeg) {
		this.angleDeg = angleDeg;
	}
	
	public Angle(double deg, double min, double sec) {
		this(AngleUtil.toDeg(deg, min, sec));
	}
	
	public double getAngleDeg() {
		return angleDeg;
	}
	
	public double getAngleRad() {
		return Math.toRadians(angleDeg);
	}
	
	public String getAngleLabel() {
		boolean neg = angleDeg<0;
		double absBear = Math.abs(angleDeg); 
		int deg = (int)absBear;
		double minT = (absBear-deg)*60;
		int min = (int)(minT);
		int sek = (int)(Math.round((minT - min)*60));
		return (neg ? -deg : deg) +"\u00b0 "+min+"\" "+sek+ "'";
	}
	
	@Override
	public String toString() {
		return getAngleLabel();
	}

	public String getAngleDegreesLabel() {
		boolean neg = angleDeg<0;
		double absBear = Math.abs(angleDeg); 
		int deg = (int)absBear;
		return (neg ? -deg : deg) + "\u00b0 ";
	}
	
	public static Angle parseAngle(String s) {
		double deg = Double.NaN;
		double min = Double.NaN;
		double sec = Double.NaN;
		
		s += "X"; //end char
		String part = "";
		for (int i=0; i<s.length(); i++) {
			char c = s.charAt(i);
			if (Character.isDigit(c) || c == '.' || c == '-') {
				part += c;
			} else if (part.length() > 0) {
				try {
					double val = Double.parseDouble(part);
					if (Double.isNaN(deg)) {
						deg = val;
					} else if (Double.isNaN(min)) {
						min = val;
					} else if (Double.isNaN(sec)) {
						sec = val;
						break;
					}
				} catch (NumberFormatException ignore) { }
				part = "";
			} else {
				continue;
			}
		}
		
		if (!Double.isNaN(deg)) {
			return new Angle(deg, min, sec);
		}
		
		return null;
	}
}
