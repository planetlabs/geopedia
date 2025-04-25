package com.sinergise.themebundle.ui.dark.button;

import com.google.gwt.resources.client.ImageResource;
import com.sinergise.themebundle.ui.dark.icons.DarkStandardIcons;
import com.sinergise.themebundle.ui.light.button.LightButtonResources;

public interface DarkButtonResources extends LightButtonResources {
	
	//TODO: when needed, prepare some new style
//	@Override
//	@Source({BUTTON_CSS, "buttonStyle.css"})
//	ButtonCss buttonStyle();
	
	@Override
	@Source(DarkStandardIcons.DARK_ICONS_PATH + "pin.png")
	public ImageResource pin();
	@Override
	@Source(DarkStandardIcons.DARK_ICONS_PATH + "pinned.png")
	public ImageResource pinned();
}
