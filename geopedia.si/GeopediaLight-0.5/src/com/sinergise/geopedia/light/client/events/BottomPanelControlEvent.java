package com.sinergise.geopedia.light.client.events;

import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class BottomPanelControlEvent extends Event<BottomPanelControlEvent.Handler> {
	enum EvtType {SETWIDGET,CLOSEPANEL};
	public interface Handler {
		void setWidget(Widget widget);
		void closePanel(Widget widget);
	}


	protected BottomPanelControlEvent() {
	}
	
	private static final Type<BottomPanelControlEvent.Handler> TYPE =
		        new Type<BottomPanelControlEvent.Handler>();
	
	public static HandlerRegistration register(EventBus eventBus, BottomPanelControlEvent.Handler handler) {
		return eventBus.addHandler(TYPE, handler);
	} 

	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		switch (evtType) {
		case SETWIDGET:
			handler.setWidget(widget);
			break;
		case CLOSEPANEL:
			handler.closePanel(widget);
		}
	}
	
	private Widget widget = null;
	private EvtType evtType;
	
	public Widget getWidget() {
		return widget;
	}
	
	public static BottomPanelControlEvent setWidget(Widget widget) {
		BottomPanelControlEvent evt = new BottomPanelControlEvent();
		evt.widget = widget;
		evt.evtType = EvtType.SETWIDGET;
		return evt;
	}

	public static BottomPanelControlEvent closePanel(Widget widget) {
		BottomPanelControlEvent evt = new BottomPanelControlEvent();
		evt.widget = widget;
		evt.evtType = EvtType.CLOSEPANEL;
		return evt;
	}

}