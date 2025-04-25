package com.sinergise.themebundle.ui.blue.button;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.themebundle.ui.basic.button.BasicButtonResources;

public interface BlueButtonResources extends BasicButtonResources {
	@Override
	@Source({BasicButtonResources.BUTTON_CSS, "buttonStyle.css"})
	ButtonCss buttonStyle();
	
	@Override
	ImageResource btn_ie_up();
	@Override
	ImageResource btn_ie_down();
	@Override
	ImageResource btn_ie_disabled();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource aUp();
	ImageResource aUpL();
	ImageResource aUpR();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource aDown();
	ImageResource aDownL();
	ImageResource aDownR();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource aDis();
	ImageResource aDisL();
	ImageResource aDisR();
}
