package com.sinergise.geopedia.light.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.ui.panels.ActivatableTabPanel;
import com.sinergise.geopedia.light.client.events.BottomPanelControlEvent;
import com.sinergise.geopedia.light.client.i18n.LightMessages;
import com.sinergise.geopedia.themebundle.ui.GeopediaStandardIcons;
import com.sinergise.gwt.ui.ImageAnchor;

public abstract class ClosableBottomPanel extends ActivatableTabPanel {
	 
	private ImageAnchor btnClose;
	protected ClosableBottomPanel() {
		addStyleName("closeableBottomPanel");
		btnClose = new ImageAnchor(GeopediaStandardIcons.INSTANCE.closeWhite());
		btnClose.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (canDeactivate())
					onDestroy();	
			}
		});
		btnClose.addStyleName("fl-right");
		btnClose.setTitle(LightMessages.INSTANCE.closeTab());
		addButton(btnClose);
	}
	
	protected void removeButtons (boolean removeCloseButton) {
		getButtonPanel().clear();
		if (!removeCloseButton) {
			addButton(btnClose);
		}
	}
	
	@Override
	protected void onDestroy() {
		ClientGlobals.eventBus.fireEvent(BottomPanelControlEvent.closePanel(ClosableBottomPanel.this));
	}
}
