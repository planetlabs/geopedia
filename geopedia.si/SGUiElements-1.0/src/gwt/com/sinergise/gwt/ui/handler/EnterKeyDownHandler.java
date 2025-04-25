package com.sinergise.gwt.ui.handler;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.sinergise.common.ui.core.KeyCodes;

/**
 * Convenience abstract class for listening on Enter key down events.
 * 
 * @author tcerovski
 */
public abstract class EnterKeyDownHandler implements KeyDownHandler {

	/* (non-Javadoc)
	 * @see com.google.gwt.event.dom.client.KeyDownHandler#onKeyDown(com.google.gwt.event.dom.client.KeyDownEvent)
	 */
	public final void onKeyDown(KeyDownEvent event) {
		if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
			onEnterDown(event);
		}
	}
	
	public abstract void onEnterDown(KeyDownEvent event);

}
