package com.sinergise.gwt.gis.map.ui.vector;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.topo.Edge;
import com.sinergise.common.gis.app.ApplicationContext;
import com.sinergise.common.util.format.NumberFormatter;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.gwt.gis.ui.gfx.Canvas;

public class LineLengthTextMarker extends AbstractTextMarker {
	private static NumberFormatter LEN_FORMATTER = ApplicationContext.getInstance().getDefaultLengthFormatter();
	
	public LineMarker lineMarker;
	
	double sreenLineLenSq = Double.NaN;
	double screenTextLenSq = Double.NaN;


	public LineLengthTextMarker(LineMarker lineMarker) {
		this(lineMarker, new TextMarkerStyle("#444444", DEFAULT_WHITE_GLOW));
	}
	
	public LineLengthTextMarker(LineMarker lineMarker, TextMarkerStyle textStyle) {
		super(new TextPositionFromLine(lineMarker), textStyle);
		this.lineMarker = lineMarker;
	}
	
	@Override
	public String getText() {
		return LEN_FORMATTER.format(lineMarker.getLength());
	}
	
	public void setTextStyle(VectorFilter filter, String color) {
		getStyle().setFilter(filter);
		getStyle().setFillColor(color);
	}
	
	@Override
	public void updateStyle(DisplayCoordinateAdapter dca, Canvas canvas) {
		if (!super.isVisible()) {
			return;
		}
		sreenLineLenSq = dca.pixFromWorld.area(lineMarker.getLengthSq());
		screenTextLenSq = MathUtil.sqr(getText().length() * 8 + 6); //canvas.getComputedTextLength(el) + 6;
	}
	
	@Override
	public boolean isVisible() {
		return super.isVisible() && screenTextLenSq < sreenLineLenSq;
	}

	public Edge getEdge() {
		return (Edge)lineMarker.getLocationData();
	}
	
}
