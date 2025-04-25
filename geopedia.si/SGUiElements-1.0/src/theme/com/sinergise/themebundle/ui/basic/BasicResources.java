package com.sinergise.themebundle.ui.basic;


import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.gwt.ui.resources.ThemeResources;
import com.sinergise.themebundle.ui.basic.button.BasicButtonResources;
import com.sinergise.themebundle.ui.basic.layout.BasicLayoutResources;
import com.sinergise.themebundle.ui.basic.splash.BasicSplashScreenBundle;


public interface BasicResources extends ThemeResources {
	String DEFAULT_CSS = "com/sinergise/themebundle/ui/basic/defaultStyle.css";
	String NOTIFICATION_CSS = "com/sinergise/themebundle/ui/basic/notification.css";

	@Override
	ThemeCss defaultStyle();
	@Override
	NotificationCss notification();

	@Override
	BasicSplashScreenBundle splashScreenBundle();
	
	@Override
	BasicButtonResources buttonBundle();
	@Override
	BasicLayoutResources layoutBundle();
	
	@ImageOptions(repeatStyle=RepeatStyle.Both,preventInlining=true)
	ImageResource processWorking();
}
