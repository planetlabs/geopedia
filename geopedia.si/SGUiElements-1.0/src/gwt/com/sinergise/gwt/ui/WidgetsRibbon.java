package com.sinergise.gwt.ui;

import com.google.gwt.user.client.ui.HasOneWidget;
import com.sinergise.common.util.event.selection.SourcesToggleEvents;
import com.sinergise.common.util.event.selection.ToggleListener;
import com.sinergise.gwt.ui.controls.EnsureVisibilityUtil;

public class WidgetsRibbon extends WidgetsBox {
	
	public WidgetsRibbon() {
		setStyleName("widgetsRibbon");
	}
	
	
	public void bindToContainer(HasOneWidget container, SourcesToggleEvents selectable) {
		selectable.addToggleListener(new RibbonVisibilityListener(this, container));
	}

	public static class RibbonVisibilityListener implements ToggleListener {
		
		final WidgetsRibbon ribbon;
		final HasOneWidget container;
		
		public RibbonVisibilityListener(WidgetsRibbon ribbon, HasOneWidget container) {
			this.ribbon = ribbon;
			this.container = container;
		}
		
		@Override
		public void toggleStateChanged(SourcesToggleEvents source, boolean newOn) {
			if (newOn) {
				container.setWidget(ribbon);
				EnsureVisibilityUtil.ensureVisibility(ribbon);
			} else if (container.getWidget() == ribbon) {
				container.setWidget(null);
			}
		}
		
	}

}
