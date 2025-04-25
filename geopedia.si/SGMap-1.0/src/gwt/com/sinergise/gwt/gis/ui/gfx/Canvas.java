package com.sinergise.gwt.gis.ui.gfx;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.EnvelopeI;
import com.sinergise.gwt.gis.map.ui.vector.AbstractTextMarker;
import com.sinergise.gwt.gis.map.ui.vector.ClosedMarkerStyle;
import com.sinergise.gwt.gis.map.ui.vector.LineMarkerStyle;
import com.sinergise.gwt.gis.map.ui.vector.TextMarkerStyle;
import com.sinergise.gwt.util.html.CSS;

public abstract class Canvas extends Widget
{
	public static Canvas create(DimI size) {
		Canvas ret = create();
		ret.setSize(size);
		return ret;
	}

	public static Canvas create()
	{
		return ((Canvas) GWT.create(Canvas.class)).init();
	}

	EnvelopeI internalMBR = EnvelopeI.EMPTY;
	Element holder;
	Element outer;

	protected abstract Canvas init();

	public void setSize(DimI size) {
		setSize(size.w(), size.h());
	}
	
	public void setSize(int w, int h)
	{
		internalMBR = EnvelopeI.withSize(0, 0, w, h);
		CSS.size(holder, w, h);
		CSS.size(outer, w, h);
	}

	public void removeElement(Element el)
	{
		outer.removeChild(el);
	}

	protected abstract void hideElement(Element el);

	public abstract Element addCircle(double cx, double cy, double r, float strokeWidth, ClosedMarkerStyle style);

	public void updateCircle(Element circle, double cx, double cy, double r) {
		if (!internalMBR.intersectsDouble(cx-r, cy-r, cx+r, cy+r)) { // trivially outside
			hideElement(circle);
			return;
		}
		showCircle(circle, cx, cy, r);
	}

	public abstract void updateCirclePos(Element circle, double cx, double cy, double r);

	public abstract void updateCircleSize(Element circle, double cx, double cy, double r);

	protected abstract void showCircle(Element circle, double cx, double cy, double r);

	public abstract Element addLine(double x1, double y1, double x2, double y2, float width, LineMarkerStyle style);

	public void updateLine(Element line, double x1, double y1, double x2, double y2)
	{
		if (!internalMBR.intersectsDouble(x1, y1, x2, y2)) { // trivially outside
			hideElement(line);
			return;
		}
		showLine(line, x1, y1, x2, y2); // Don't bother clipping - browser should do this
	}

	protected abstract void showLine(Element line, double x1, double y1, double x2, double y2);

	public abstract Element addRectangle(double x1, double y1, double x2, double y2, float strokeWidth, ClosedMarkerStyle style);

	public void updateRectangle(Element rect, double x1, double y1, double x2, double y2) {
		if (!internalMBR.intersectsDouble(x1, y1, x2, y2)) { // trivially outside
			hideElement(rect);
			return;
		}		
		
		showRectangle(rect, x1, y1, x2, y2); // Don't bother clipping - browser should do this
	}

	protected abstract void showRectangle(Element rectEl, double x1, double y1, double x2, double y2);

	public abstract Element addText(double x, double y, AbstractTextMarker text);//mg

	public void updateText(Element elem, double x, double y, AbstractTextMarker text) {
		if (!internalMBR.containsDouble(x, y)) {
			hideElement(elem);
			return;
		}
		showText(elem, x,  y, text);		
	}

	public abstract void updateTextElementStyle(Element el, float strokeWidth, TextMarkerStyle style);

	public abstract double getComputedTextLength(Element el);

	protected abstract void showText(Element element, double x, double y, AbstractTextMarker textM);

	public abstract void updateElementStyle(Element el, float strokeWidth, LineMarkerStyle style);
	public abstract void updateClosedElementStyle(Element el, float strokeWidth, ClosedMarkerStyle style);
}
