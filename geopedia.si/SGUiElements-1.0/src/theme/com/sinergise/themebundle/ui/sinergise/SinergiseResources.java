package com.sinergise.themebundle.ui.sinergise;

import com.sinergise.themebundle.ui.basic.BasicResources;
import com.sinergise.themebundle.ui.sinergise.button.SinergiseButtonResources;
import com.sinergise.themebundle.ui.sinergise.layout.SinergiseLayoutResources;

public interface SinergiseResources extends BasicResources {
	@Override
	@Source({BasicResources.DEFAULT_CSS, "defaultStyle.css"})
	ThemeCss defaultStyle();
	
	@Override
	SinergiseButtonResources buttonBundle();
	@Override
	SinergiseLayoutResources layoutBundle();
	@Override
	@Source({BasicResources.NOTIFICATION_CSS, "notification.css"})
	NotificationCss notification();
}
