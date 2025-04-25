package com.sinergise.geopedia.themebundle.ui.button;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.themebundle.ui.basic.button.BasicButtonResources;

public interface GeopediaButtonResources extends BasicButtonResources {
	@Override
	@Source({BasicButtonResources.BUTTON_CSS, "buttonStyle.css"})
	ButtonCss buttonStyle();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource btn();
	ImageResource btnL();
	ImageResource btnR();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal, preventInlining=true)
	ImageResource btnAct();
	ImageResource btnActL();
	ImageResource btnActR();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource btnBlue();
	ImageResource btnBlueL();
	ImageResource btnBlueR();
}
