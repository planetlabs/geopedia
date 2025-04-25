package com.sinergise.themebundle.ui.light.button;

import com.google.gwt.resources.client.ImageResource;
import com.sinergise.gwt.ui.resources.ButtonResources;
import com.sinergise.gwt.ui.resources.ThemeResources;
import com.sinergise.themebundle.ui.light.icons.LightStandardIcons;

public interface LightButtonResources extends ButtonResources {
	String BUTTON_CSS = "com/sinergise/themebundle/ui/light/button/buttonStyle.css";

	@Override
	@Source({ThemeResources.COLORS,BUTTON_CSS})
	ButtonCss buttonStyle();
	
	@Override
	@Source(LightStandardIcons.LIGHT_ICONS_PATH + "pin.png")
	public ImageResource pin();
	@Override
	@Source(LightStandardIcons.LIGHT_ICONS_PATH + "pinned.png")
	public ImageResource pinned();
}
