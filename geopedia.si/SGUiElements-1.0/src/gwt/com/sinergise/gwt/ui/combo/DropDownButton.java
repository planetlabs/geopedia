package com.sinergise.gwt.ui.combo;


import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.sinergise.common.ui.action.ActionListener;
import com.sinergise.common.ui.action.ActionListenerCollection;
import com.sinergise.common.ui.action.SourcesActionEvents;
import com.sinergise.common.ui.core.KeyCodes;
import com.sinergise.gwt.util.html.ExtDOM;

@Deprecated
public class DropDownButton extends Button implements SourcesActionEvents
{
	private static int cnt = 0;

	private String tdId = "cosylab_DropDownButtonTD_" + (cnt++);

	protected ActionListenerCollection als;
	protected Element renderEl = null;

	public DropDownButton()
	{
		super();
		setHTML("<TABLE height=\"100%\" width=\"0\" style=\"margin: 0 -2px; font-size: 0;\" cellspacing=\"0\" cellpadding=\"0\"><TR>\r\n"
		                + "			<TD id=\""
		                + tdId
		                + "\"></TD>\r\n"
		                + "			<TD style=\"padding-left: 3px;\" width=\"0\"><IMG src=\"style/widgets/smallDownArrow.gif\"/></TD>\r\n"
		                + "		</TR></TABLE>");
		setStyleName("cosylab-dropDownButton");
		sinkEvents(Event.ONCLICK | Event.ONKEYDOWN);
	}

	public void setRenderElement(Element el)
	{
		Element td = ExtDOM.getChildById(getElement(), tdId);
		if (renderEl != null)
			DOM.removeChild(td, renderEl);
		renderEl = el;
		if (renderEl != null)
			DOM.appendChild(td, el);
	}

	@Override
	protected void onAttach()
	{
		super.onAttach();
		DOM.setEventListener(getElement(), this);
	}

	public Element getRenderEl()
	{
		return renderEl;
	}

	@Override
	public void onBrowserEvent(Event event)
	{
		switch (DOM.eventGetType(event)) {
		case Event.ONCLICK:
			buttonPressed();
			break;
		case Event.ONKEYDOWN:
			if (DOM.eventGetKeyCode(event) == KeyCodes.KEY_DOWN) {
				click();
			}
			break;
		default:
			break;
		}
	}

	protected void buttonPressed()
	{
		if (als == null)
			return;
		als.fireActionPerformed(this, null);
	}

	public void addActionListener(ActionListener listener)
	{
		if (als == null)
			als = new ActionListenerCollection();
		als.add(listener);
	}

	public void removeActionListener(ActionListener listener)
	{
		if (als == null)
			return;
		als.remove(listener);
	}
}
