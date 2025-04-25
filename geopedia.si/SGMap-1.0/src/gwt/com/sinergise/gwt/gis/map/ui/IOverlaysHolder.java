package com.sinergise.gwt.gis.map.ui;

import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Focusable;

public interface IOverlaysHolder extends Focusable{
	public void insertOverlay(OverlayComponent<?> overlay, int zIndex, boolean end);
	public void removeOverlay(OverlayComponent<?> ovr);
	public <H extends EventHandler> HandlerRegistration addDomHandler(final H handler, DomEvent.Type<H> type);

}
