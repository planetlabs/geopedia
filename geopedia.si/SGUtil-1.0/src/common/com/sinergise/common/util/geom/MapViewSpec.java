/**
 * 
 */
package com.sinergise.common.util.geom;

import com.sinergise.common.util.math.MathUtil;

public class MapViewSpec implements HasCoordinate {
	public double worldCenterX = Double.NaN;
	public double worldCenterY = Double.NaN;
	public double worldLenPerDisp = Double.POSITIVE_INFINITY;

	public MapViewSpec() {
	}
	
	@Override
	public double x() {
		return worldCenterX;
	}
	
	@Override
	public double y() {
		return worldCenterY;
	}

	public MapViewSpec(double worldCenterX, double worldCenterY, double scale) {
		this.worldCenterX = worldCenterX;
		this.worldCenterY = worldCenterY;
		this.worldLenPerDisp = scale;
	}
	
	public MapViewSpec(HasCoordinate worldCenter, double scale) {
		this.worldCenterX = worldCenter.x();
		this.worldCenterY = worldCenter.y();
		this.worldLenPerDisp = scale;
	}

	public final double getScale() {
		return worldLenPerDisp;
	}

	protected void internalSetWorldCenterAndScale(double wCenterX, double wCenterY, double scale) {
		this.worldCenterX = wCenterX;
		this.worldCenterY = wCenterY;
		this.worldLenPerDisp = scale;
	}
	
	public boolean isValid() {
		if(Double.isNaN(worldCenterX) || Double.isNaN(worldCenterY))
			return false;
		
		return worldLenPerDisp > 0 && worldLenPerDisp < Double.POSITIVE_INFINITY;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + MathUtil.hashCode(worldCenterX);
		result = prime * result + MathUtil.hashCode(worldCenterY);
		result = prime * result + MathUtil.hashCode(worldLenPerDisp);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapViewSpec other = (MapViewSpec) obj;
		if (!MathUtil.equals(worldCenterX, other.worldCenterX))
			return false;
		if (!MathUtil.equals(worldCenterY, other.worldCenterY))
			return false;
		if (!MathUtil.equals(worldLenPerDisp, other.worldLenPerDisp))
			return false;
		return true;
	}
}