package com.sinergise.geopedia.client.ui.map.controls;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.gwt.gis.map.ui.DisplayCoordUtilGWT;
import com.sinergise.gwt.gis.map.ui.IMapComponent;

public class ZoomBtnCtrl extends FlowPanel{

	protected static final int ZOOM_IN = 0;
	protected static final int ZOOM_OUT = 1;
	private IMapComponent map;
	
	private class ZoomPanel extends FlowPanel implements MouseUpHandler,
		MouseDownHandler {
		private int zoom;
		public ZoomPanel(String name, int zoom) {
			setStyleName(name);
			addDomHandler(this, MouseDownEvent.getType());
			addDomHandler(this, MouseUpEvent.getType());
			this.zoom = zoom;
		}

		@Override
		public void onMouseUp(MouseUpEvent event) {
			removeStyleDependentName("clicked");
			event.stopPropagation();
			event.preventDefault();
		}

		@Override
		public void onMouseDown(MouseDownEvent event) {
			addStyleDependentName("clicked");	
			handleZoom(zoom);
			event.stopPropagation();
			event.preventDefault();
		}
	}
	
	protected final ZoomPanel butZoomIn;
	protected final ZoomPanel butZoomOut;
	
	public ZoomBtnCtrl(IMapComponent map) {
		this.map=map;
		setStyleName("geopedia-zoomCtrl");		
		add(butZoomIn = new ZoomPanel("geopedia-zoomCtrl-in", ZOOM_IN));
		add(butZoomOut = new ZoomPanel("geopedia-zoomCtrl-out", ZOOM_OUT));
	}
	
	protected void handleZoom(int zoomDirection) {
		if (zoomDirection == ZOOM_IN) {
	        DisplayCoordUtilGWT.zoomWithInteger(map, 1, MathUtil.invertIfSmall(1));
		} else if (zoomDirection == ZOOM_OUT) {
	        DisplayCoordUtilGWT.zoomWithInteger(map, -1, MathUtil.invertIfSmall(.1));
		}
	}

}
