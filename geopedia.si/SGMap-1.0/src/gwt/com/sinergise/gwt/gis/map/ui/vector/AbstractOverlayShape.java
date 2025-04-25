package com.sinergise.gwt.gis.map.ui.vector;

import com.google.gwt.dom.client.Element;

public class AbstractOverlayShape implements IOverlayShape {
	public static class Closed extends AbstractOverlayShape {
		public Closed(ClosedMarkerStyle style) {
			super(style);
		}
		
		@Override
		public ClosedMarkerStyle getStyle() {
			return (ClosedMarkerStyle)super.getStyle();
		}
	}
	
	protected Element el;
	protected LineMarkerStyle style;

	public AbstractOverlayShape(LineMarkerStyle style) {
		this.style = style;
	}
	
	public LineMarkerStyle getStyle() {
		return style;
	}
	
	@Override
	public Element getElement() {
		return el;
	}

	@Override
	public void setElement(Element el) {
		this.el = el;
	}
}
