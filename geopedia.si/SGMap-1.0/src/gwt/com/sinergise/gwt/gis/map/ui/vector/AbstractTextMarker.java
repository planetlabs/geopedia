package com.sinergise.gwt.gis.map.ui.vector;

import com.google.gwt.dom.client.Element;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.gwt.gis.map.ui.vector.VectorFilter.GlowFilter;
import com.sinergise.gwt.gis.ui.gfx.Canvas;

public abstract class AbstractTextMarker implements IOverlayShape {
	public static final GlowFilter DEFAULT_WHITE_GLOW = new GlowFilter("white", 2);
	public static final GlowFilter DEFAULT_BLACK_GLOW = new GlowFilter("black", 2);
	protected Element el;
	private TextPosition position;
	protected TextMarkerStyle style;
	protected boolean visible = true;
	
	public AbstractTextMarker(TextPosition position, TextMarkerStyle style) {
		this.style = style;
		this.position = position;
	}
	
	public HasCoordinate getCoordinates(){
		return position.getCoordinates();
	}
 
	public float getRotation(){
		return position.getRotation();
	}
	
	public abstract String getText();
	
	@Override
	public Element getElement() {
		return el;
	}

	@Override
	public void setElement(Element el) {
		this.el = el;		
	}
	
	public TextMarkerStyle getStyle() {
		return style;
	}
	
	public boolean isVisible() {
		return visible ;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * @param dca  Coordinates currently used to render the marker
	 * @param vectorCanvas The canvas that currently renders the marker
	 */
	public void updateStyle(DisplayCoordinateAdapter dca, Canvas vectorCanvas) {
		
	}
}
