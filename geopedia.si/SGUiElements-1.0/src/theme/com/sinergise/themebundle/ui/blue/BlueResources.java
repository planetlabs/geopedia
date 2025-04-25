package com.sinergise.themebundle.ui.blue;

import com.sinergise.themebundle.ui.basic.BasicResources;
import com.sinergise.themebundle.ui.blue.layout.BlueLayoutResources;
import com.sinergise.themebundle.ui.blue.button.BlueButtonResources;

public interface BlueResources extends BasicResources {
	@Override
	@Source({BasicResources.DEFAULT_CSS, "defaultStyle.css"})
	ThemeCss defaultStyle();
	
	@Override
	BlueButtonResources buttonBundle();
	@Override
	BlueLayoutResources layoutBundle();
}
