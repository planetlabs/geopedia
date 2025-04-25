package com.sinergise.gwt.ui.resources.dock;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

public interface DockLayoutResources extends ClientBundle {
	
	String DOCK_CSS = "com/sinergise/gwt/ui/resources/dock/dockLayout.css"; 
	public static final DockLayoutResources INSTANCE =  GWT.create(DockLayoutResources.class);
	
	public static interface DockCss extends CssResource {
		String minimizer();
		String content();
		String contentHolder();
		String splitter();
		String splitPanel();
	}
	DockCss dockLayout();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource northMinimizer();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource southMinimizer();
	@ImageOptions(repeatStyle=RepeatStyle.Vertical)
	ImageResource westMinimizer();
	@ImageOptions(repeatStyle=RepeatStyle.Vertical)
	ImageResource eastMinimizer();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource borderS();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource borderN();
	@ImageOptions(repeatStyle=RepeatStyle.Vertical)
	ImageResource borderE();
	@ImageOptions(repeatStyle=RepeatStyle.Vertical)
	ImageResource borderW();

	ImageResource minimizeDown();
	ImageResource minimizeUp();
	ImageResource minimizeLeft();
	ImageResource minimizeRight();
}
