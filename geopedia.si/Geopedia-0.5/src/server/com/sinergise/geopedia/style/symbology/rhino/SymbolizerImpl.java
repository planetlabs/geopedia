package com.sinergise.geopedia.style.symbology.rhino;

import java.awt.Color;

import org.mozilla.javascript.ScriptableObject;

import com.sinergise.common.util.geom.Position2D;
import com.sinergise.geopedia.core.symbology.Symbolizer;

public abstract class SymbolizerImpl extends ScriptableObject implements Symbolizer {

	private static final long serialVersionUID = 4322412196753655650L;

	protected Position2D displacement = new Position2D(0,0);
	protected double opacity = 1;
	
	
	protected static Color fromRawColorARGB(double rawColor) {
		int rgba = (int) (new Double(rawColor)).longValue();
		return  new Color(rgba, true);
	}
	
	public void setDisplacementX(double x) {
		displacement.x = x;
	}
	public double getDisplacementX() {
		return displacement.x;
	}

	public void setDisplacementY(double y) {
		displacement.y = y;
	}

	
	public double getDisplacementY() {
		return displacement.y;
	}
	
	
	@Override
	public Position2D getDisplacement() {
		return displacement;
	}
	

	
	public void setOpacity(double opacity) {
		this.opacity = opacity;
	}
	
	
	@Override	
	public double getOpacity() {
		return opacity;
	}


}
