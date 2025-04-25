package com.sinergise.themebundle.ui.sigov.button;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.themebundle.ui.basic.button.BasicButtonResources;

public interface SigovButtonResources extends BasicButtonResources {
	@Override
	@Source({BasicButtonResources.BUTTON_CSS, "buttonStyle.css"})
	ButtonCss buttonStyle();	
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource btn();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource btnOver();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource btnOn();
}
