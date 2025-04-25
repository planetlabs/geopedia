package com.sinergise.geopedia.style.symbology.rhino;

import java.awt.Color;

import org.mozilla.javascript.annotations.JSGetter;
import org.mozilla.javascript.annotations.JSSetter;

import com.sinergise.common.util.geom.Position2D;
import com.sinergise.geopedia.core.symbology.FillSymbolizer;

public class FillSymbolizerImpl extends SymbolizerImpl implements FillSymbolizer{
	private static final long serialVersionUID = -2995008299717704798L;

	private Color fillColor = FillSymbolizer.DEFAULT_FILL;
	private Color backgroundColor = FillSymbolizer.DEFAULT_FILLBACKGROUND;
	private GPFillType fillType = FillSymbolizer.DEFAULT_FILLTYPE;
	
	public FillSymbolizerImpl() {
		displacement = new Position2D(FillSymbolizer.DEFAULT_DISPLACEMENTX, FillSymbolizer.DEFAULT_DISPLACEMENTY);
		opacity = FillSymbolizer.DEFAULT_OPACITY;
	}

	
	@Override
	public String getClassName() {
		return "FillSymbolizer";
	}

	
	@JSSetter
	public void setFill(double fillColorRaw) {
		this.fillColor = fromRawColorARGB(fillColorRaw);
	}
	
	@Override
	@JSGetter
	public Color getFill() {
		return fillColor;
	}
	
	@JSSetter
	public void setFillBackground(double fillBackgroundColorRaw) {
		this.backgroundColor = fromRawColorARGB(fillBackgroundColorRaw);
	}
	
	@Override
	@JSGetter
	public Color getFillBackground() {
		return backgroundColor;
	}
	
	
	@JSSetter
	public void setFillType(String fillTypeString) {
		for (GPFillType ft: GPFillType.values()) {
			if (ft.name().equalsIgnoreCase(fillTypeString)) {
				fillType=ft;
				break;
			}
		}
	}	
	
	@Override
	@JSGetter
	public GPFillType getFillType() {
		return fillType;
	}
	
	
	/**** BEGIN from SymbolizerImpl, must be copied for codegenerator to work  ****/
	@Override
	@JSSetter
	public void setOpacity(double opacity) {	
		super.setOpacity(opacity);
	}
	
	@Override
	@JSGetter
	public double getOpacity() {
		return super.getOpacity();
	}
	
	@Override
	@JSGetter
	public double getDisplacementX() {
		return super.getDisplacementX();
	}
	
	@Override
	@JSSetter
	public void setDisplacementX(double x) {
		super.setDisplacementX(x);
	}
	
	@Override
	@JSGetter
	public double getDisplacementY() {
		return super.getDisplacementY();
	}
	
	@Override
	@JSSetter
	public void setDisplacementY(double y) {
		super.setDisplacementY(y);
	}
	/**** END from SymbolizerImpl  ****/
}
