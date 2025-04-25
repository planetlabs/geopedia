package com.sinergise.generics.gwt.widgetprocessors;

import java.util.HashMap;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.gwt.core.GenericWidget;
import com.sinergise.generics.gwt.core.WidgetProcessor;

public class ActionPerformedWidgetProcessor extends WidgetProcessor{
	public interface ActionPerformedHandler {
		void actionPerformed(Widget widget, GenericObjectProperty property, GenericWidget gw, int idx);
	}
	
	private HashMap<String, ActionPerformedHandler> handlersMap = new HashMap<String,ActionPerformedHandler>();
	
	
	public void registerHandler(String actionName, ActionPerformedHandler handler) {
		if (handlersMap.containsKey(actionName)) throw new IllegalArgumentException("Handler for action '"+actionName+" is already registered!");
		handlersMap.put(actionName,handler);		
	}
	
	@Override
	public Widget bind(final Widget widget, final int idx, final GenericObjectProperty property,
			final GenericWidget gw) {
		
		if (property.isAction()) {
			if (widget instanceof HasClickHandlers) {
				((HasClickHandlers)widget).addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						ActionPerformedHandler handler = handlersMap.get(property.getName());
						if (handler!=null) {
							handler.actionPerformed(widget, property, gw, idx);
						}
					}
				});
			} else {
				throw new RuntimeException("Unsupported widget "+widget);
			}
		}
		return widget;
	}

}
