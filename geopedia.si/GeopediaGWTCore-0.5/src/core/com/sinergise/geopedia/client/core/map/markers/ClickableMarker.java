package com.sinergise.geopedia.client.core.map.markers;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.sinergise.common.util.event.ActionPerformedListener;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.gwt.gis.map.ui.vector.AbstractMarker;
import com.sinergise.gwt.ui.maingui.DecoratedAnchor;
import com.sinergise.gwt.util.html.CSS;

public class ClickableMarker<T> extends AbstractMarker{

	private DecoratedAnchor markerWidget;
	private HandlerRegistration handlers = null;
	private T referenceObject;
	private String style;
	public HasCoordinate worldPos;
	public ClickableMarker(String title, String text, String style, HasCoordinate position) {
		this.style=style;
		this.worldPos = position;
		markerWidget = new DecoratedAnchor();
		if (text!=null)
			markerWidget.setText(text);
		if (title!=null)
			markerWidget.setTitle(title);
		markerWidget.setStyleName("clickableMarker");
		markerWidget.addStyleName(style);
		setElement(markerWidget.getElement());		
		CSS.position(getElement(), CSS.POS_ABSOLUTE);
	}
	
	@Override
	public void prepareToRender() {
	}
	
	public void setReference (T reference) {
		this.referenceObject = reference;
	}
	
	public void setText(String text) {
		markerWidget.setText(text);
	}
	public void updateStyle(String newStyle) {
		markerWidget.removeStyleName(this.style);
		this.style=newStyle;
		markerWidget.addStyleName(this.style);
	}
	
	public void setActionPerformedListener (final ActionPerformedListener<T> listener) {
		if (handlers!=null) {
			handlers.removeHandler();
		}
		handlers = addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				listener.onActionPerformed(referenceObject);
				
			}
		});
	}

	@Override
	public void positionPx(double l, double t) {
		CSS.leftTop(getElement(), l,t);
	}

	@Override
	public HasCoordinate getWorldPosition() {
		return worldPos;
	}

}
