package com.sinergise.geopedia.style.symbology.rhino;

import java.awt.Color;

import org.mozilla.javascript.annotations.JSGetter;
import org.mozilla.javascript.annotations.JSSetter;

import com.sinergise.common.util.geom.Position2D;
import com.sinergise.geopedia.core.symbology.LineSymbolizer;

public class LineSymbolizerImpl  extends SymbolizerImpl implements LineSymbolizer{

	private static final long serialVersionUID = -401458133716362047L;
	
	
	private Color stroke = LineSymbolizer.DEFAULT_STROKE;	
	private double strokeWidth=LineSymbolizer.DEFAULT_STROKEWIDTH;
	private LineType lineType = LineSymbolizer.DEFAULT_LINETYPE;
	
	public LineSymbolizerImpl() {
		displacement = new Position2D(LineSymbolizer.DEFAULT_DISPLACEMENTX, LineSymbolizer.DEFAULT_DISPLACEMENTY);
		opacity = LineSymbolizer.DEFAULT_OPACITY;
	}
	
	@Override
	public String getClassName() {
		return "LineSymbolizer";
	}
		
	
	@JSSetter
	public void setStroke(double rawColorARGB) {
		stroke = fromRawColorARGB(rawColorARGB);
	}
	
	
	@Override
	@JSGetter
	public Color getStroke() {
		return stroke;
	}

	@JSSetter
	public void setStrokeWidth(double strokeWidth) {
		this.strokeWidth=strokeWidth;
	}
	
	
	@Override
	@JSGetter
	public double getStrokeWidth() {
		return this.strokeWidth;
	}

	
	@JSSetter
	public void setLineType(String lineTypeString) {		
		for (LineType lt: LineType.values()) {
			if (lt.name().equalsIgnoreCase(lineTypeString)) {
				lineType=lt;
				break;
			}
		}
	}
	
	
	@Override
	@JSGetter
	public LineType getLineType() {
		return lineType;
	}
	
	
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
	
}
