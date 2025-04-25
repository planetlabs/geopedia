package com.sinergise.geopedia.client.ui.feature;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.gwt.ui.controls.ClickableLabel;


public class WidgetLinker {
    
    public static Panel link(final Widget widget, String label) {
        
        widget.setVisible(false);
        
        ClickHandler clickListener = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				widget.setVisible(! widget.isVisible());
			}
        };
        
        ClickableLabel link = new ClickableLabel(Messages.INSTANCE.generalShowHide() + label, false, clickListener);//"Pokaži/skrij "
        
        final VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(link);
        verticalPanel.add(widget);
        
        return verticalPanel;
    }
}
