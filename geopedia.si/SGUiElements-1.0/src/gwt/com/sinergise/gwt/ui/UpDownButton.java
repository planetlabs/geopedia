package com.sinergise.gwt.ui;


import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.sinergise.common.ui.action.ActionListener;
import com.sinergise.common.ui.action.ActionListenerCollection;
import com.sinergise.common.ui.action.SourcesActionEvents;
import com.sinergise.common.ui.core.KeyCodes;
import com.sinergise.gwt.util.html.ExtDOM;


public class UpDownButton extends Button implements SourcesActionEvents
{
	private ActionListenerCollection acl;

	public UpDownButton()
	{
		super("<span class='up'></span><span class='down'></span>");
		setStyleName("cosylab-upDownButton");
		DOM.sinkEvents(getElement(), Event.ONKEYDOWN | Event.MOUSEEVENTS);
	}

	@Override
	public void onBrowserEvent(Event event)
	{
		switch (DOM.eventGetType(event)) {
		case Event.ONKEYDOWN:
			int key = DOM.eventGetKeyCode(event);
			if (key == KeyCodes.KEY_UP) {
				doClick(true);
			} else if (key == KeyCodes.KEY_DOWN) {
				doClick(false);
			} else if (key != KeyCodes.KEY_SPACE && key != KeyCodes.KEY_ENTER) { // Prevent
																					// space
				break;
			}
			DOM.eventCancelBubble(event, true);
			DOM.eventPreventDefault(event);
			break;
		case Event.ONMOUSEDOWN:
			startFiring();
			break;
		case Event.ONMOUSEMOVE:
			x = ExtDOM.eventGetElementX(event, getElement());
			y = ExtDOM.eventGetElementY(event, getElement());
			break;
		case Event.ONMOUSEUP:
			x = ExtDOM.eventGetElementX(event, getElement());
			y = ExtDOM.eventGetElementY(event, getElement());
			internalClick();
			DOM.eventCancelBubble(event, true);
			DOM.eventPreventDefault(event);
			stopFiring();
			break;
		case Event.ONMOUSEOUT:
			Element myElem = getElement();
			Element toElem = DOM.eventGetToElement(event);
			if (myElem==null || toElem == null || !DOM.isOrHasChild(myElem, toElem)) {
				stopFiring();
			}
			break;
		default:
			break;
		}
		super.onBrowserEvent(event);
	}

	int x = 0;
	int y = 0;
	Timer fireTimer = new Timer() {
		@Override
		public void run()
		{
			internalClick();
			this.schedule(50);
		}
	};

	private void startFiring()
	{
		fireTimer.schedule(500);
	}

	private void stopFiring()
	{
		fireTimer.cancel();
	}

	protected void internalClick()
	{
		int h = getOffsetHeight();
		if (x >= 0 && x < getOffsetWidth()) {
			if (y >= 0 && y <= h / 2) {
				doClick(true);
			} else if (y < h) {
				doClick(false);
			}
		}
	}

	protected void doClick(boolean up)
	{
		if (acl == null)
			return;
		acl.fireActionPerformed(this, Boolean.valueOf(up));
	}

	public void addActionListener(ActionListener listener)
	{
		if (acl == null)
			acl = new ActionListenerCollection();
		acl.add(listener);
	}

	public void removeActionListener(ActionListener listener)
	{
		if (acl == null)
			return;
		acl.remove(listener);
	}
}
