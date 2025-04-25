package com.sinergise.geopedia.style.symbology.rhino;

import java.awt.Color;

import org.mozilla.javascript.annotations.JSGetter;
import org.mozilla.javascript.annotations.JSSetter;

import com.sinergise.common.util.geom.Position2D;
import com.sinergise.geopedia.core.symbology.PointSymbolizer;

public class PointSymbolizerImpl extends SymbolizerImpl implements PointSymbolizer {
	private static final long serialVersionUID = -3649392150968742714L;

	private int symbolId = PointSymbolizer.DEFAULT_SYMBOLID;
	private double size = PointSymbolizer.DEFAULT_SIZE;
	private Color fill = PointSymbolizer.DEFAULT_FILL;
	
	
	public PointSymbolizerImpl() {
		displacement = new Position2D(PointSymbolizer.DEFAULT_DISPLACEMENTX, PointSymbolizer.DEFAULT_DISPLACEMENTY);
		opacity = PointSymbolizer.DEFAULT_OPACITY;
	}
	
	@Override
	public String getClassName() {
		return "PointSymbolizer";
	}

	@Override
	@JSGetter
	public double getSize() {
		return size;
	}
	
	@JSSetter
	public void setSize(double size) {
		this.size=size;
	}

	
	@JSSetter
	public void setFill(double fillColorRaw) {
		this.fill = fromRawColorARGB(fillColorRaw);
	}
	
	@Override
	@JSGetter
	public Color getFill() {
		return fill;
	}

	@Override
	@JSGetter
	public int getSymbolId() {
		return symbolId;
	}

	@JSSetter
	public void setSymbolId(int symbolId) {
		this.symbolId=symbolId;
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
