/*
 *
 */
package com.sinergise.gwt.gis.map.ui.vector;


import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.geom.HasCoordinateMutable;
import com.sinergise.gwt.gis.map.ui.vector.signs.Sign;

/**
 * This is only mutable if the provided position is mutable
 * 
 * @author Miha
 */
public class Marker extends AbstractMarker implements HasCoordinateMutable {
	private Sign sign;
	public HasCoordinate worldPos;
	
	public Marker(Sign sign, HasCoordinate position) {
		this.sign = sign;
		this.worldPos = position;
	}
	
	@Override
	public void prepareToRender() {
		if (el == null) {
			setElement(sign.createContent(this));
		}
	}
	
	@Override
	public void positionPx(double l, double t) {
		if (el != null) {
			sign.position(getElement(), l, t);
		}
	}
	
	@Override
	public Marker setLocation(HasCoordinate c) {
		HasCoordinateMutable worldPosM = (HasCoordinateMutable)worldPos;
		worldPosM.setLocation(c);
		return this;
	}
	
//	@Override //GWT doesn't have clone on Object
	@SuppressWarnings("all")
	public Marker clone() {
		return new Marker(sign, ((HasCoordinateMutable)worldPos).clone());
	}

	@Override
	public double x() {
		return worldPos.x();
	}

	@Override
	public double y() {
		return worldPos.y();
	}

	@Override
	public HasCoordinate getWorldPosition() {
		return worldPos;
	}
}
