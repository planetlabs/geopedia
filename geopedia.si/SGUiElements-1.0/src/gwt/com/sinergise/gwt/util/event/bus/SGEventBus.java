package com.sinergise.gwt.util.event.bus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

/**
 * Use event bus in GWT judiciously to avoid application-level memory leaks:</br>
 * If possible, utilize the event bus only when the event source and the event handler are of the same scope.</br>
 * When the event source or the event handler scope is longer-lived than the other, store the {@link HandlerRegistration} 
 * and call <code>removeHandler</code> when the life cycle of the shorter-lived scope ends to prevent memory leaks.  
 */
public class SGEventBus extends SimpleEventBus {
	
	/**
	 * Register an event dispatcher. 
	 * @see SGEventDispatcher
	 */
	@SuppressWarnings("unchecked")
	public <H extends EventHandler> HandlerRegistration addDispatcher(Type<H> type, SGEventDispatcher<H> dispatcher) {
		//trick the compiler in allowing registration of dispatcher as a handler as generic type will be deleted at runtime.
		return super.addHandler(type, (H)dispatcher);
	}

	  
	/**
	 * Event type that can be dispatched to a {@link SGEventDispatcher}.
	 */
	public static abstract class SGEvent<H extends EventHandler> extends GwtEvent<H> {
		
		@SuppressWarnings("unchecked")
		@Override
		protected final void dispatch(H handler) {
			if (handler instanceof SGEventDispatcher<?>) {
				for (EventHandler childHandler : ((SGEventDispatcher<H>)handler).getHandlersForDispatch(this)) {
					directDispatch((H) childHandler);
				}
			} else {
				directDispatch(handler);
			}
		}
		
		/**
		 * Directly dispatch this event to the handler, i.e. implementing classes should 
		 * only call handlers event handle method. 
		 */
		protected abstract void directDispatch(H handler);
		
		/**
		 *  @return <code>true</code> if this event can be handled by the provided handler, <code>false</code> otherwise.</br>
		 *  <code>instanceof</code> comparison should be used in most cases.
		 */
		protected abstract boolean canHandle(EventHandler handler);
		
	}
	
	/**
	 * A dispatcher should be used to disseminate events to transient event handlers by 
	 * a handler of the same life scope as the event source.
	 */
	public interface SGEventDispatcher<H extends EventHandler> extends EventHandler {
		Collection<H> getHandlersForDispatch(SGEvent<H> event);
	}
	
	/**
	 * Dispatches events to all child widgets that can handle the event. The child widget handlers
	 * must decide on their own if they want to handle the event.
	 * @see SGEventDispatcher 
	 */
	public static class SGChildWidgetsEventDispatcher<H extends EventHandler> implements SGEventDispatcher<H> {
		
		private final HasWidgets container;
		
		public SGChildWidgetsEventDispatcher(HasWidgets container) {
			this.container = container;
		}
		
		
		@SuppressWarnings("unchecked")
		@Override
		public Collection<H> getHandlersForDispatch(SGEvent<H> event) {
			List<H> handlers = new ArrayList<H>();
			for (Widget w : container) {
				if (w instanceof EventHandler && event.canHandle((EventHandler)w)) {
					handlers.add((H)w);
				}
			}
			return handlers;
		}
	}

}
