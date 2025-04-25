package com.sinergise.gwt.gis.ui.gfx;


import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.DOM;
import com.sinergise.gwt.gis.map.ui.vector.AbstractTextMarker;
import com.sinergise.gwt.gis.map.ui.vector.ClosedMarkerStyle;
import com.sinergise.gwt.gis.map.ui.vector.LineMarkerStyle;
import com.sinergise.gwt.gis.map.ui.vector.TextMarkerStyle;
import com.sinergise.gwt.gis.map.ui.vector.VectorFilter;
import com.sinergise.gwt.gis.map.ui.vector.VectorFilter.GlowFilter;
import com.sinergise.gwt.ui.core.MouseHandler;
import com.sinergise.gwt.util.html.CSS;


public class CanvasSVG extends Canvas
{
	private static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";

	private static native void setStyleAttr(Element el, String propName, String propVal) /*-{
		el.style[propName] = propVal;
	}-*/;
	
	private static native void setStyleAttr(Element el, String propName, float propVal) /*-{
		el.style[propName] = propVal;
	}-*/;
	
	@Deprecated
	private static native void setAttr(Element el, String propName, String propVal) /*-{
		el.setAttribute(propName, propVal);
	}-*/;
	@Deprecated
	private static native void setAttr(Element el, String propName, int propVal) /*-{
		el.setAttribute(propName, propVal);
	}-*/;
	@Deprecated
	private static native void setAttr(Element el, String propName, double propVal) /*-{
		el.setAttribute(propName, propVal);
	}-*/;
	
	private static native void setLinePos(Element line, double x1, double y1, double x2, double y2) /*-{
		if (line.x1.baseVal) {
			line.x1.baseVal.value = x1 + 0.5;
			line.y1.baseVal.value = y1 + 0.5;
			line.x2.baseVal.value = x2 + 0.5;
			line.y2.baseVal.value = y2 + 0.5;
		} else {
			line.setAttribute("x1", x1 + 0.5);
			line.setAttribute("y1", y1 + 0.5);
			line.setAttribute("x2", x2 + 0.5);
			line.setAttribute("y2", y2 + 0.5);
		}
	}-*/;
	
	private static native void setLengthProp(Element el, String propName, float value) /*-{
		var lenProp = el[propName];
		if (lenProp.baseVal) {
			lenProp.baseVal.value = value;
		} else {
			el.setAttribute(propName, value);
		}
	}-*/;

	private static int filterDefCnt = 0;
	
	private static void svgSet(Element el, String propName, String value)
	{
		setAttr(el, propName, value);
	}
	
	public static native Element createElementNS(String namespace, String tag) /*-{
		return $doc.createElementNS(namespace, tag);
	}-*/;
	
	private static Element createSvg(String tagName)
	{
		return createElementNS(SVG_NAMESPACE, tagName);
	}
	private Element defs;
	
	private Map<VectorFilter, Element> filterDefs = new HashMap<VectorFilter, Element>();
	
	public CanvasSVG()
	{
	}
	
	@Override
	protected CanvasSVG init()
	{
		setElement(holder = DOM.createDiv());
        MouseHandler.preventContextMenu(holder);
        if (!internalMBR.isEmpty()) {
        	CSS.size(holder, internalMBR.getWidth(), internalMBR.getHeight());
        }
        holder.getStyle().setOverflow(Overflow.HIDDEN);
		
		outer = createSvg("svg");
		try {
	        if (!internalMBR.isEmpty()) {
	        	CSS.size(outer, internalMBR.getWidth(), internalMBR.getHeight());
	        }
			CSS.leftTop(outer, 0, 0);
			outer.getStyle().setPosition(Position.RELATIVE);
			holder.appendChild(outer);
	        MouseHandler.preventContextMenu(outer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		defs = createSvg("defs");
		try {
			outer.appendChild(defs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return this;
	}
	
	@Override
	protected void hideElement(Element el)
    {
		CSS.setVisible(el, false);
    }
	
	@Override
	public void updateClosedElementStyle(Element el, float strokeWidth, ClosedMarkerStyle style) {
		updateElementStyle(el, strokeWidth, style);
		
		if (style.hasFill()) {
			setStyleAttr(el, "fill", style.getFillColor());
			setStyleAttr(el, "fill-opacity", style.getFillOpacity());
		} else {
			setStyleAttr(el, "fill", CSS.NONE);
		}
	}
	@Override
	public void updateElementStyle(Element el, float strokeWidth, LineMarkerStyle style) {
		if (style.hasStroke(strokeWidth)) {
			setStyleAttr(el, "stroke", style.getStrokeColor());
			setStyleAttr(el, "stroke-width", strokeWidth+"px");
			setStyleAttr(el, "stroke-opacity", style.getStrokeOpacity());
		} else {
			setStyleAttr(el, "stroke", CSS.NONE);
		}
		applyStyleFilter(el, style.getFilter());
	}
	protected void applyStyleFilter(Element el, VectorFilter filter) {
		String filterDefName = checkFilterDef(filter);
		if (filterDefName != null) {
			setStyleAttr(el, "filter", "url(\"#"+filterDefName+"\")");
		} else {
			setStyleAttr(el, "filter", "");
		}
	}
	private String checkFilterDef(VectorFilter filter) {
		if (filter == null) {
			return null;
		}
		Element def = filterDefs.get(filter);
		if (def == null) {
			def = createFilterDefinition(filter);		
			if (def == null) {
				return null;
			}
			filterDefs.put(filter, def);
		}
		return def.getAttribute("id");
	}
	@Override
	public Element addLine(double x1, double y1, double x2, double y2, float width, LineMarkerStyle style) {
		Element el = createSvg("line");
		
		updateElementStyle(el, width, style);
		updateLine(el, x1, y1, x2, y2);
		
		outer.appendChild(el);
		return el;
    }
	
	@Override
	protected void showLine(Element line, double x1, double y1, double x2, double y2)
	{
		//Add 0.5 so that the line starts in the centre of the pixel
		setLinePos(line, x1, y1, x2, y2);
		CSS.setVisible(line, true);
	}
	
	@Override
	public Element addText(double x, double y, AbstractTextMarker text){
		Element el = createSvg("text");
		
		//TODO: Refactor to include evaluation of style expressions (e.g. GraphicMeasure)
		updateTextElementStyle(el, 0.0f, text.getStyle());
		el.setAttribute("x", "0");
		el.setAttribute("y", "0");
		el.setAttribute("transform", "translate(0.1, 0)");
		updateText(el,  x,  y, text);
		
		outer.appendChild(el);
		return el;
	}
	protected Element createFilterDefinition(VectorFilter filterDef) {
		if (filterDef instanceof GlowFilter) {
			return createGlowFilterDefinition((GlowFilter)filterDef);
		}
		return null;
	}
	protected Element createGlowFilterDefinition(GlowFilter filterDef) {
		Element filter = createSvg("filter");
		setAttr(filter, "width", 2);
		setAttr(filter, "height", 2);
		setAttr(filter, "x", -0.5);
		setAttr(filter, "y", -0.5);
		filter.setId("canvassvg" + filterDefCnt++);
		
		Element feMorf = createSvg("feMorphology");
		setAttr(feMorf, "in", "SourceAlpha");
		setAttr(feMorf, "result", "fat");
		setAttr(feMorf, "radius", filterDef.width);
		setAttr(feMorf, "operator", "dilate");
		
		Element feFlood = createSvg("feFlood");
		setAttr(feFlood, "flood-color", filterDef.color);
		setAttr(feFlood, "flood-opacity", filterDef.opacity);
		setAttr(feFlood, "in", "fat");
		setAttr(feFlood, "result", "flooded");
		
		Element feComp1 = createSvg("feComposite");
		setAttr(feComp1, "in", "flooded");
		setAttr(feComp1, "in2", "fat");
		setAttr(feComp1, "operator", "in");
		setAttr(feComp1, "result", "fatColored");
		
		Element feGB = createSvg("feGaussianBlur");
		setAttr(feGB, "in", "fatColored");
		setAttr(feGB, "stdDeviation", filterDef.width/2);
		setAttr(feGB, "result", "blur");
		
		Element feComp2 = createSvg("feComposite");
		setAttr(feComp2, "in", "SourceGraphic");
		setAttr(feComp2, "in2", "blur");
		setAttr(feComp2, "operator", "over");
		
		//insert feModules into filter
		filter.appendChild(feMorf);
		filter.appendChild(feFlood);
		filter.appendChild(feComp1);
		filter.appendChild(feGB);
		filter.appendChild(feComp2);
		
		defs.appendChild(filter);
		
		return filter;
	}
	
	
	@Override
	protected void showText(Element elem, double x, double y, AbstractTextMarker text){
		if (text.isVisible() && !Double.isNaN(x) && !Double.isNaN(y)) {
			setTextPos(elem, (float)x, (float)y, text.getRotation());
			elem.setInnerText(text.getText());
			//anchor, weight should be set after setting to visible (FF weird stuff)
			if (!CSS.isVisible(elem)) {
				CSS.setVisible(elem, true);
				updateTextElementStyle(elem, 0.0f, text.getStyle());
			}
		} else {
			CSS.setVisible(elem, false);
		}
	}
	
	private static native void setTextPos(Element elem, float x, float y, float angle) /*-{
		elem.x.baseVal.getItem(0).value = x;
		elem.y.baseVal.getItem(0).value = y - 5;
		elem.transform.baseVal.getItem(0).setRotate(angle, x, y);
	}-*/;
	@Override
	public void updateTextElementStyle(Element el, float strokeWidth, TextMarkerStyle style) {
		updateClosedElementStyle(el, strokeWidth, style);
		CSS.fontSize(el, style.getFontSize());
		CSS.fontWeight(el, FontWeight.BOLD);
		setAttr(el, "text-anchor", "middle");
	}
	@Override
	public native double getComputedTextLength(Element elem) /*-{
		return elem.getComputedTextLength();
	}-*/;
	
	@Override
	public Element addRectangle(double x1, double y1, double x2, double y2, float strokeWidth, ClosedMarkerStyle style) {
		Element el = createSvg("rect");
		
		updateClosedElementStyle(el, strokeWidth, style);
		updateRectangle(el, x1, y1, x2, y2);
		
		outer.appendChild(el);
		return el;
	}
	
	@Override
	protected void showRectangle(Element rectEl, double x1, double y1, double x2, double y2) {
		//TODO: move to native code for performance
		final float dx = (float)(x2 - x1);
		final float dy = (float)(y2 - y1);
		if (dx >= 1) {
			setLengthProp(rectEl, "x", Math.round(x1) + 0.5f);
			setLengthProp(rectEl, "width", dx);
		} else {
			setLengthProp(rectEl, "x", Math.round(0.5*(x1 + x2)));
			setLengthProp(rectEl, "width", 1);
		}
		if (dy >= 1) {
			setLengthProp(rectEl, "y", Math.round(y1) + 0.5f);
			setLengthProp(rectEl, "height", dy);
		} else {
			setLengthProp(rectEl, "y", Math.round(0.5*(y1 + y2)));
			setLengthProp(rectEl, "height", 1);
		}
		CSS.setVisible(rectEl, true);
	}

	@Override
	public Element addCircle(double cx, double cy, double r, float strokeWidth, ClosedMarkerStyle style) {
		Element el = createSvg("circle");
		
		updateClosedElementStyle(el, strokeWidth, style);
		updateCircle(el, cx, cy, r);
		
		outer.appendChild(el);
		return el;
	}

	@Override
	protected void showCircle(Element circle, double cx, double cy, double r) {
		updateCirclePos(circle, cx, cy, r);
		updateCircleSize(circle, cx, cy, r);
		CSS.setVisible(circle, true);
	}
	
	@Override
	public void updateCirclePos(Element circle, double cx, double cy, double r) {
		setLengthProp(circle, "cx", (float)cx);
		setLengthProp(circle, "cy", (float)cy);
	}
	
	@Override
	public void updateCircleSize(Element circle, double cx, double cy, double r) {
		setLengthProp(circle, "r", (float)r);
	}
	
}
