package com.sinergise.gwt.ui.core;

import com.google.gwt.event.dom.client.DomEvent;

public interface EventOverrider
{
	public boolean handleEvent(DomEvent<?> e);
	public String getCursor();
}
