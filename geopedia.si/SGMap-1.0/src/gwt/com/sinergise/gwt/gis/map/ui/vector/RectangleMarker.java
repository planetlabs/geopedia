package com.sinergise.gwt.gis.map.ui.vector;

import com.sinergise.common.util.geom.Envelope;

public class RectangleMarker extends AbstractOverlayShape.Closed {
	private Envelope data;
	public RectangleMarker(Envelope position, ClosedMarkerStyle style) {
		super(style);
		this.data = position;
	}
	
	public Envelope getPositionData() {
		return data;
	}
}
