package com.sinergise.geopedia.style.symbology.rhino;

import java.awt.Color;

import org.mozilla.javascript.annotations.JSGetter;
import org.mozilla.javascript.annotations.JSSetter;

import com.sinergise.common.util.geom.Position2D;
import com.sinergise.geopedia.core.symbology.SymbolizerFont;
import com.sinergise.geopedia.core.symbology.TextSymbolizer;

public class TextSymbolizerImpl extends SymbolizerImpl implements TextSymbolizer {
	private static final long serialVersionUID = -6402816165639017121L;
	
	
	private String label = TextSymbolizer.DEFAULT_LABEL;
	private SymbolizerFont symbolizerFont = new SymbolizerFontImpl();
	private Color fillColor = TextSymbolizer.DEFAULT_FILLCOLOR;
	
	
	public TextSymbolizerImpl() {
		displacement = new Position2D(TextSymbolizer.DEFAULT_DISPLACEMENTX, TextSymbolizer.DEFAULT_DISPLACEMENTY);
		opacity = TextSymbolizer.DEFAULT_OPACITY;
		symbolizerFont = new SymbolizerFontImpl();
	}
	
	@Override
	@JSGetter
	public String getLabel() {
		return label;
	}

	@JSSetter
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	@JSGetter
	public SymbolizerFontImpl getFont() {
		return (SymbolizerFontImpl) symbolizerFont;
	}
	
	@JSSetter
	public void setFont(SymbolizerFontImpl font) {
		this.symbolizerFont = font;
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

	@Override
	public String getClassName() {
		return "TextSymbolizer";
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
