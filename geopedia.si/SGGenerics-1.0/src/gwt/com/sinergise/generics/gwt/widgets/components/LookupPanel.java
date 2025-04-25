package com.sinergise.generics.gwt.widgets.components;

import com.google.gwt.user.client.ui.FlowPanel;
import com.sinergise.generics.gwt.core.GenericWidget;
import com.sinergise.gwt.ui.dialog.AbstractDialogBox;

public class LookupPanel extends AbstractDialogBox {
	
    public LookupPanel() {
    	super(false, true, true);
    	addStyleName("lookupPanel");
    }

    @Override
	public void setTitle(String title) {
    	setText(title);
    	setTitle(title);
    }
    public void setContent(GenericWidget w) {
    	FlowPanel content = new FlowPanel();
    	content.add(w);
    	content.add(createCloseButton());
    	setWidget(content);
    }
    
    public static LookupPanel createLookupDialog(GenericWidget w) {
    	LookupPanel lp = new LookupPanel();
    	lp.setContent(w);
    	lp.center();
    	return lp;
    }
}