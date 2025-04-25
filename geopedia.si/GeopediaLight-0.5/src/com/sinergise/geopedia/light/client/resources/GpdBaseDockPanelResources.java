package com.sinergise.geopedia.light.client.resources;

import com.google.gwt.resources.client.ImageResource;
import com.sinergise.gwt.ui.resources.dock.DockLayoutResources;

public interface GpdBaseDockPanelResources extends DockLayoutResources {
	@Override
	@Source(value = {DockLayoutResources.DOCK_CSS, "dockLayout.css"})
	public DockCss dockLayout();
	
	ImageResource leftToggleButton();
}
