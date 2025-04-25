package com.sinergise.themebundle.ui.light;


import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.gwt.ui.resources.ThemeResources;
import com.sinergise.themebundle.ui.basic.splash.BasicSplashScreenBundle;
import com.sinergise.themebundle.ui.light.button.LightButtonResources;
import com.sinergise.themebundle.ui.light.icons.LightStandardIcons;
import com.sinergise.themebundle.ui.light.layout.LightLayoutResources;


public interface LightResources extends ThemeResources {
	String DEFAULT_CSS = "com/sinergise/themebundle/ui/light/defaultStyle.css";
	String NOTIFICATION_CSS = "com/sinergise/themebundle/ui/light/notification.css";

	@Override
	@Source({COLORS,DEFAULT_CSS})
	ThemeCss defaultStyle();
	@Override
	NotificationCss notification();

	@Override
	BasicSplashScreenBundle splashScreenBundle();
	
	@Override
	LightStandardIcons standardIcons();
	
	LightButtonResources buttonBundle();
	@Override
	LightLayoutResources layoutBundle();
	
	@ImageOptions(repeatStyle=RepeatStyle.Both,preventInlining=true)
	ImageResource processWorking();
}
