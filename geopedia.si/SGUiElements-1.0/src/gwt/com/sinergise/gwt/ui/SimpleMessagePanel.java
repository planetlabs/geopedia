package com.sinergise.gwt.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.sinergise.common.ui.i18n.Buttons;
import com.sinergise.common.util.messages.MessageType;

public class SimpleMessagePanel extends FlowPanel {
	Anchor closeBut = new Anchor(Buttons.INSTANCE.close());
	{
		closeBut.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				SimpleMessagePanel.this.setVisible(false);
			}
		});
	}
	HTML lbl = new HTML();
	MessageType curType = null;
	public SimpleMessagePanel() {
		setStyleName(StyleConsts.MESSAGE_PANEL_SIMPLE);
		add(lbl);
		add(closeBut);
		setVisible(false);
	}
	
	public void show(MessageType type, String message) {
		lbl.setText(message == null ? "" : message);
		doShow(type);
	}

	public void show(MessageType type, SafeHtml message) {
		lbl.setHTML(message == null ? "" : message.asString());
		doShow(type);
	}
	
	private void doShow(MessageType type) {
		if (curType != null) {
			removeStyleName(curType.name());
		}
		curType = type;
		if (type != null) {
			addStyleName(type.name());
			setVisible(true);
		} else {
			setVisible(false);
		}
	}

	/** 
	 *  Prevent events from propagating to the map.
	 *  */
	@Override
    public void onBrowserEvent(Event event) {  
         int t = DOM.eventGetType(event);  
         if (Integer.bitCount(t & (Event.ONMOUSEDOWN | Event.ONMOUSEUP)) > 0) {  
              DOM.eventPreventDefault(event);  
              DOM.eventCancelBubble(event, true);  
         }  
         super.onBrowserEvent(event);  
    }
}
