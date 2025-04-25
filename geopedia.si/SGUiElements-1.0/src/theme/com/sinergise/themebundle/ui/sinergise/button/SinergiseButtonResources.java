package com.sinergise.themebundle.ui.sinergise.button;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.themebundle.ui.basic.button.BasicButtonResources;

public interface SinergiseButtonResources extends BasicButtonResources {
	@Override
	@Source({BasicButtonResources.BUTTON_CSS, "buttonStyle.css"})
	ButtonCss buttonStyle();
	
	@Override
	ImageResource x();
	
	ImageResource btnL();
	ImageResource btnR();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource btn();
	
	ImageResource btnLAct();
	ImageResource btnRAct();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource btnAct();
}
