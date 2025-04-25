package com.sinergise.gwt.ui.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Shared;
import com.sinergise.gwt.ui.SplashScreen.SplashScreenBundle;
import com.sinergise.gwt.ui.resources.icons.StandardIcons;

public interface ThemeResources extends ClientBundle {
	
	public static String COLORS = "com/sinergise/gwt/ui/resources/colors.css";
	
	@Shared
	public static interface ThemeCss extends CssResource {
	}
	public static interface NotificationCss extends CssResource {
		String notificationPanel();
		String withImage();
		String big();
		String close();
	}
	
	ThemeCss defaultStyle();
	NotificationCss notification();
	
	SplashScreenBundle splashScreenBundle();
	ButtonResources buttonBundle();
	LayoutResources layoutBundle();
	StandardIcons standardIcons();
}