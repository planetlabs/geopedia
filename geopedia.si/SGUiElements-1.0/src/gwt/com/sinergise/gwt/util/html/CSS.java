package com.sinergise.gwt.util.html;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.util.UtilGWT;

public class CSS {
	public static final String POS_RELATIVE = "relative";
	public static final String POS_ABSOLUTE = "absolute";
	public static final String OVR_HIDDEN = "hidden";
	public static final String OVR_AUTO = "auto";
	public static final String OVR_VISIBLE = "visible";

	public static final String DISP_BLOCK = "block";
	public static final String DISP_NONE = "none";
	public static final String DISP_DEFAULT = "";
	public static final String DISP_INLINE = "inline";

	public static final String PERC_100 = "100%";
	public static final String FLOAT_LEFT = "left";
	public static final String FLOAT_RIGHT = "right";

	public static final String TRANSPARENT = "transparent";

	public static final String NONE = "none";

	public static final String U_PIXELS = "px";
	public static final String RIGHT = "right";
	public static final String CENTER = "center";

	public static final String CURSOR_DEFAULT = "default";
	public static final String CURSOR_HAND = "pointer";
	public static final String CURSOR_WAIT = "wait";
	public static final String CURSOR_PLUS = "crosshair";
	public static final String CURSOR_TEXT = "text";
	public static final String CURSOR_HELP = "help";

	public static final String CURSOR_MOVE = "move";
	public static final String CURSOR_RES_E = "e-resize";
	public static final String CURSOR_RES_W = "w-resize";
	public static final String CURSOR_RES_N = "n-resize";
	public static final String CURSOR_RES_S = "s-resize";
	public static final String CURSOR_RES_NE = "ne-resize";
	public static final String CURSOR_RES_NW = "nw-resize";
	public static final String CURSOR_RES_SE = "se-resize";
	public static final String CURSOR_RES_SW = "sw-resize";

	public static final String WS_NOWRAP = "nowrap";
	public static final String WS_NORMAL = "normal";
	public static final String WS_PRE = "pre";
	public static final String WS_PRE_WRAP = "pre-wrap";
	public static final String WS_PRE_LINE = "pre-line";

	public static final String IE6_IMG_INTERPOLATION = "msInterpolationMode"; // "-ms-interpolation-mode";
	public static final String IE6_IMG_INT_BICUBIC = "bicubic"; // "-ms-interpolation-mode:bicubic";
	public static final String IE6_IMG_INT_NEAREST_NEIGHBOR = "nearest-neighbor"; // "-ms-interpolation-mode:nearest-neighbor";

	public static final String BG_REPEAT = "repeat";
	public static final String BG_REPEAT_X = "repeat-x";
	public static final String BG_REPEAT_Y = "repeat-y";
	public static final String BG_REPEAT_NO = "no-repeat";

	public static final String AUTO = "auto";

	private CSS() {
		// Private constructor to prevent instantiation
	}

	public static void position(Element el, String pos) {
		el.getStyle().setProperty("position", pos);
	}

	public static void overflow(Element el, String ovr) {
		el.getStyle().setProperty("overflow", ovr);
	}

	public static void display(Element el, String disp) {
		el.getStyle().setProperty("display", disp);
	}

	public static void width(Element el, String w) {
		el.getStyle().setProperty("width", w);
	}

	public static void height(Element el, String h) {
		el.getStyle().setProperty("height", h);
	}

	public static void margin0(Element elem) {
		elem.getStyle().setMargin(0, Unit.PX);
	}

	public static void marginTop(UIObject widget, double px) {
		marginTop(widget.getElement(), px);
	}

	public static void marginTop(Element el, double px) {
		el.getStyle().setMarginTop(px, Unit.PX);
	}

	public static void marginBottom(Element el, double px) {
		el.getStyle().setMarginBottom(px, Unit.PX);
	}

	public static void marginLeft(Element el, double px) {
		el.getStyle().setMarginLeft(px, Unit.PX);
	}

	public static void marginRight(Element el, double px) {
		el.getStyle().setMarginRight(px, Unit.PX);
	}

	public static void padding(Element el, int px) {
		el.getStyle().setPadding(px, Unit.PX);
	}

	public static void padding(Element el, double pxTop, double pxRight, double pxBottom, double pxLeft) {
		el.getStyle().setPaddingTop(pxTop, Unit.PX);
		el.getStyle().setPaddingRight(pxRight, Unit.PX);
		el.getStyle().setPaddingBottom(pxBottom, Unit.PX);
		el.getStyle().setPaddingLeft(pxLeft, Unit.PX);
	}

	public static void floating(Element el, String floatStr) {
		if (UtilGWT.isIE6or7()) {
			el.getStyle().setProperty("styleFloat", floatStr);
		} else {
			el.getStyle().setProperty("cssFloat", floatStr);
		}
	}

	public static void size(Element el, String w, String h) {
		width(el, w);
		height(el, h);
	}

	public static void leftTop(Element el, double l, double t) {
		left(el, l);
		top(el, t);
	}

	public static void top(Element el, double t) {
		el.getStyle().setTop(t, Unit.PX);
	}

	public static void top(Element el, String dist) {
		el.getStyle().setProperty("top", dist);
	}

	public static void left(Element el, double l) {
		el.getStyle().setLeft(l, Unit.PX);
	}

	public static void left(Element el, String dist) {
		el.getStyle().setProperty("left", dist);
	}

	public static void className(Element el, String cls) {
		el.setClassName(cls);
	}

	public static void zIndex(Element el, int zIndex) {
		el.getStyle().setZIndex(zIndex);
	}

	public static void zIndex(UIObject w, int zIndex) {
		zIndex(w.getElement(), zIndex);
	}

	public static void background(Element el, String background) {
		el.getStyle().setProperty("background", background);
	}

	public static void backgroundColor(Element el, String color) {
		el.getStyle().setBackgroundColor(color);
	}

	public static void backgroundImageURL(Element el, String imageURL) {
		el.getStyle().setBackgroundImage("url('" + imageURL + "')");
	}

	public static void backgroundPositionPx(Element el, int xposPx, int yposPx) {
		el.getStyle().setProperty("backgroundPosition", xposPx + "px " + yposPx + "px");
	}

	public static void backgroundRepeat(Element el, String repeat) {
		el.getStyle().setProperty("backgroundRepeat", repeat);
	}

	public static void cursor(Element el, String cursor) {
		el.getStyle().setProperty("cursor", cursor);
	}

	public static void fontSize(Element el, String size) {
		el.getStyle().setProperty("fontSize", size);
	}

	public static void fontSize(Element el, double size) {
		el.getStyle().setFontSize(size, Unit.PX);
	}

	public static void fontWeight(Element el, FontWeight weight) {
		el.getStyle().setFontWeight(weight);
	}
	
	public static void sizePx(Element el, int w, int h) {
		if (w < 0) {
			w = 0;
		}
		if (h < 0) {
			h = 0;
		}
		el.getStyle().setWidth(w, Unit.PX);
		el.getStyle().setHeight(h, Unit.PX);
	}

	public static native int getZIndex(Element el) /*-{
													return el.style.zIndex || 0;
													}-*/;

	public static int getOffsetHeight(Element el) {
		return el.getOffsetHeight();
	}

	public static int getOffsetWidth(Element el) {
		return el.getOffsetWidth();
	}

	public static String getHeight(Element el) {
		return el.getStyle().getHeight();
	}

	public static String getWidth(Element el) {
		return el.getStyle().getWidth();
	}

	public static Element textAlign(Element el, String align) {
		el.getStyle().setProperty("textAlign", align);
		return el;
	}

	public static String getTextAlign(Element el) {
		return el.getStyle().getProperty("textAlign");
	}

	public static Element whiteSpace(Element el, String whiteSpace) {
		el.getStyle().setProperty("whiteSpace", whiteSpace);
		return el;
	}

	public static void fullSize(Element el) {
		size(el, PERC_100, PERC_100);
		leftTop(el, 0, 0);
		position(el, POS_ABSOLUTE);
	}

	//TODO: Fix this - browser selection or error handling
	public static native void opacity(Element el, int opc) /*-{
		var obj=el.style;
		obj.opacity = (opc / 100);
		obj.MozOpacity = (opc / 100); 
		obj.KhtmlOpacity = (opc / 100); 
		obj.filter = "alpha(opacity=" + opc + ")";
	}-*/;

	public static void position(UIObject w, String position) {
		position(w.getElement(), position);
	}

	public static void overflow(UIObject w, String overflow) {
		overflow(w.getElement(), overflow);
	}

	public static void top(UIObject w, double t) {
		top(w.getElement(), t);
	}

	public static void left(UIObject w, double l) {
		left(w.getElement(), l);
	}

	public static void width(UIObject w, String size) {
		width(w.getElement(), size);
	}

	public static void size(UIObject w, String width, String height) {
		size(w.getElement(), width, height);
	}

	public static void background(UIObject w, String bg) {
		background(w.getElement(), bg);
	}

	public static void backgroundColor(UIObject w, String color) {
		backgroundColor(w.getElement(), color);
	}

	public static void leftTop(UIObject w, int left, int top) {
		leftTop(w.getElement(), left, top);
	}

	public static void size(Element tl, DimI size) {
		size(tl, size.w(), size.h());
	}

	public static void height(Element el, double h) {
		el.getStyle().setHeight(h, Unit.PX);
	}

	public static void width(Element el, double w) {
		el.getStyle().setWidth(w, Unit.PX);
	}

	public static void size(UIObject w, int width, int height) {
		size(w.getElement(), width, height);
	}

	public static void width(UIObject w, int width) {
		width(w.getElement(), width);
	}

	public static void display(UIObject w, String display) {
		display(w.getElement(), display);
	}

	public static void setVisible(Element e, boolean visible) {
		e.getStyle().setVisibility(visible ? Visibility.VISIBLE : Visibility.HIDDEN);
	}

	public static void setVisible(UIObject w, boolean visible) {
		setVisible(w.getElement(), visible);
	}

	public static boolean isVisible(Element e) {
		String val = e.getStyle().getVisibility();
		return Visibility.VISIBLE.getCssName().equals(val) || StringUtil.isNullOrEmpty(val);
	}
	
	public static void margin(Element el, int topBottomPx, int leftRightPx) {
		marginTop(el, topBottomPx);
		marginBottom(el, topBottomPx);
		marginLeft(el, leftRightPx);
		marginRight(el, leftRightPx);
	}

	public static void color(Element el, String color) {
		el.getStyle().setColor(color);
	}

	public static void ie6Interpolation(Element imageEl, String value) {
		if (UtilGWT.isIE6or7()) {
			imageEl.getStyle().setProperty(IE6_IMG_INTERPOLATION, value);
		}
	}

	public static void setID(Widget w, String value) {
		w.getElement().setId(value);

	}

	public static void borderWidth(Element element, String borderWidth) {
		element.getStyle().setProperty("borderWidth", borderWidth);
	}

	public static void size(Element el, double w, double h) {
		el.getStyle().setWidth(w, Unit.PX);
		el.getStyle().setHeight(h, Unit.PX);
	}

	public static void bottom(UIObject w, int bottom) {
		w.getElement().getStyle().setBottom(bottom, Unit.PX);
	}
}
