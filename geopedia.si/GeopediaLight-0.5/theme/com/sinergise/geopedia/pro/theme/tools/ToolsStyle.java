package com.sinergise.geopedia.pro.theme.tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface ToolsStyle extends ClientBundle {
	public static interface ToolsCss extends CssResource {
	}
	ToolsCss toolsStyle();
	
	ImageResource importIcon();
	ImageResource measureIcon();
	ImageResource converterIcon();
	ImageResource contourIcon();
	
	public static class App {
        private static synchronized ToolsStyle createInstance() {
            return GWT.create(ToolsStyle.class);
        }
	}
	
	public static ToolsStyle INSTANCE = App.createInstance();
}
