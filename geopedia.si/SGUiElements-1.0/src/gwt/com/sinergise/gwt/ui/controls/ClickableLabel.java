package com.sinergise.gwt.ui.controls;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;

public class ClickableLabel extends HTML
{
	public ClickableLabel(String textOrHtml, boolean isHtml, final ClickHandler listener)
	{
		super();
		
		setStyleName("link-like");
		
		if (isHtml) {
			setHTML(textOrHtml);
		} else {
			setText(textOrHtml);
		}
		addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (enabled) listener.onClick(event);
            }
        });
	}
	
	boolean enabled = true;
	
	public void enable()
	{
		if (enabled)
			return;
		
		enabled = true;
		
		setStyleName("link-like");
	}
	
	public void disable()
	{
		if (!enabled)
			return;
		
		enabled = false;
		
		setStyleName("link-like-disabled");
	}
}
