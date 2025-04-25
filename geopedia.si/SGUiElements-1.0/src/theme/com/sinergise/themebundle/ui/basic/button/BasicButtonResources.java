package com.sinergise.themebundle.ui.basic.button;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.gwt.ui.resources.ButtonResources;

public interface BasicButtonResources extends ButtonResources {
	String BUTTON_CSS = "com/sinergise/themebundle/ui/basic/button/buttonStyle.css";

	@Override
	ButtonCss buttonStyle();
	
	ImageResource btn_ie_up();
	ImageResource btn_ie_down();
	ImageResource btn_ie_disabled();

	@Override
	ImageResource pin();
	@Override
	ImageResource pinned();
	
	ImageResource x();
	ImageResource xHover();
	
	ImageResource spinner();
	
	
	/*** color buttons ****/
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource green();
	ImageResource greenL();
	ImageResource greenR();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal, preventInlining=true)
	ImageResource greenAct();
	ImageResource greenLAct();
	ImageResource greenRAct();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource yellow();
	ImageResource yellowL();
	ImageResource yellowR();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal, preventInlining=true)
	ImageResource yellowAct();
	ImageResource yellowLAct();
	ImageResource yellowRAct();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource orange();
	ImageResource orangeL();
	ImageResource orangeR();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal, preventInlining=true)
	ImageResource orangeAct();
	ImageResource orangeLAct();
	ImageResource orangeRAct();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource red();
	ImageResource redL();
	ImageResource redR();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal, preventInlining=true)
	ImageResource redAct();
	ImageResource redLAct();
	ImageResource redRAct();
}
