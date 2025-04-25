package com.sinergise.gwt.gis.ui.gfx;


import com.google.gwt.user.client.DOM;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Visibility;
import com.sinergise.gwt.gis.map.ui.vector.AbstractTextMarker;
import com.sinergise.gwt.gis.map.ui.vector.ClosedMarkerStyle;
import com.sinergise.gwt.gis.map.ui.vector.LineMarkerStyle;
import com.sinergise.gwt.gis.map.ui.vector.TextMarkerStyle;
import com.sinergise.gwt.ui.core.MouseHandler;
import com.sinergise.gwt.util.html.CSS;


public class CanvasVML extends Canvas {
//	Element inner;
	static final String vmlns = VML.vmlNs;

	public CanvasVML() {}

	@Override
	protected CanvasVML init() {
		setElement(holder = DOM.createDiv());
		if (!internalMBR.isEmpty()) {
			CSS.size(holder, internalMBR.getWidth(), internalMBR.getHeight());
		}
		holder.getStyle().setOverflow(Overflow.HIDDEN);
		MouseHandler.preventContextMenu(holder);

		outer = DOM.createElement(vmlns + ":group");
		CSS.leftTop(outer, 0, 0);
		outer.getStyle().setPosition(Position.ABSOLUTE);
		outer.setPropertyString("coordorigin", "0 0");
		if (!internalMBR.isEmpty()) {
			CSS.size(outer, internalMBR.getWidth(), internalMBR.getHeight());
			outer.setPropertyString("coordsize", internalMBR.getWidth() + " " + internalMBR.getHeight());
		}
		outer.getStyle().setOverflow(Overflow.HIDDEN);
		holder.appendChild(outer);
		MouseHandler.preventContextMenu(outer);
		return this;
	}

	@Override
	public void setSize(int w, int h) {
		super.setSize(w, h);
		outer.setPropertyString("coordsize", w + " " + h);
	}

	@Override
	protected void hideElement(Element el) {
		el.setPropertyString("path", "m 0,0 l 1,1 e");
		el.getStyle().setVisibility(Visibility.HIDDEN);
	}

	@Override
	public Element addLine(final double x1, final double y1, final double x2, final double y2, final float width, final LineMarkerStyle style) {
		final Element el = DOM.createElement(vmlns + ":line");
		outer.setPropertyString("coordorigin", outer.getPropertyString("coordorigin"));
		
		updateElementStyle(el, width, style);
		
		outer.appendChild(el);
		updateLine(el, x1, y1, x2, y2);
		return el;
	}

	@Override
	protected void showLine(Element line, double x1, double y1, double x2, double y2) {
		line.setPropertyString("from", (int)Math.round(x1) + "," + (int)Math.round(y1));
		line.setPropertyString("to", (int)Math.round(x2) + "," + (int)Math.round(y2));
		line.getStyle().setVisibility(Visibility.VISIBLE);
	}

	@Override
	public Element addRectangle(double x1, double y1, double x2, double y2, float strokeWidth, ClosedMarkerStyle style) {
		Element el = DOM.createElement(vmlns + ":rect");
		el.getStyle().setPosition(Position.ABSOLUTE);
		
		updateClosedElementStyle(el, strokeWidth, style);
		
		outer.appendChild(el);
		updateRectangle(el, x1, y1, x2, y2);
		return el;
	}
	
	@Override
	protected void showRectangle(Element rect, double x1, double y1, double x2, double y2) {
		CSS.size(rect, (int)Math.round(x2-x1), (int)Math.round(y2-y1));
		CSS.leftTop(rect, (int)Math.round(x1), (int)Math.round(y1));
		rect.getStyle().setVisibility(Visibility.VISIBLE);
	}

	@Override
	public Element addCircle(double cx, double cy, double r, float strokeWidth, ClosedMarkerStyle style) {
		Element el = DOM.createElement(vmlns + ":oval");
		el.getStyle().setPosition(Position.ABSOLUTE);
		
		updateClosedElementStyle(el, strokeWidth, style);
		
		outer.appendChild(el);
		updateCircle(el, cx, cy, r);
		return el;
	}
	

	@Override
	protected void showCircle(Element circle, double cx, double cy, double r) {
		CSS.size(circle, (int)(2 * r), (int)(2 * r));
		CSS.leftTop(circle, (int)(cx - r), (int)(cy - r));
		circle.getStyle().setVisibility(Visibility.VISIBLE);
	}
	
	@Override
	public void updateCirclePos(Element circle, double cx, double cy, double r) {
		CSS.leftTop(circle, (int)(cx - r), (int)(cy - r));
	}
	
	@Override
	public void updateCircleSize(Element circle, double cx, double cy, double r) {
		CSS.size(circle, (int)(2 * r), (int)(2 * r));
		CSS.leftTop(circle, (int)(cx - r), (int)(cy - r));
	}
	
	@Override
	public void updateClosedElementStyle(Element el, float strokeWidth, ClosedMarkerStyle style) {
		updateElementStyle(el, strokeWidth, style);
		
		if (style.hasFill()) {
			float fillOpacity = style.getFillOpacity();
			el.setPropertyBoolean("filled", true);
			el.setPropertyString("fillcolor", style.getFillColor());
			el.getStyle().setProperty("opacity", String.valueOf(fillOpacity));
			el.getStyle().setProperty("filter","alpha(opacity="+(int)(fillOpacity*100.99)+")"); /* For IE8 and earlier */
		} else {
			el.setPropertyBoolean("filled", false);
		}
	}
	
	@Override
	public void updateElementStyle(Element el, float strokeWidth, LineMarkerStyle style) {
		if (style.hasStroke(strokeWidth)) {
			float strokeOpacity = style.getStrokeOpacity();
			el.setPropertyBoolean("stroked", true);
			el.setPropertyString("strokecolor", style.getStrokeColor());
			el.setPropertyString("strokeweight", strokeWidth + "px");
			el.getStyle().setProperty("opacity", String.valueOf(strokeOpacity));
			el.getStyle().setProperty("filter","alpha(opacity="+(int)(strokeOpacity*100.99)+")"); /* For IE8 and earlier */
		} else {
			el.setPropertyBoolean("stroked", false);
		}
	}

	@Override
	protected void showText(Element element, double x, double y, AbstractTextMarker textM) {
		// TODO Auto-generated method stub
	}

	@Override
	public Element addText(double x, double y, AbstractTextMarker text) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public double getComputedTextLength(Element el) {
		//TODO Implement this
		return 0;
	}

	@Override
	public void updateTextElementStyle(Element el, float strokeWidth, TextMarkerStyle style) {
		
	}

}
