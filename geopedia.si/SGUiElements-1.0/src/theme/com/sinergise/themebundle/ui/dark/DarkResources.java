package com.sinergise.themebundle.ui.dark;

import com.sinergise.themebundle.ui.dark.button.DarkButtonResources;
import com.sinergise.themebundle.ui.dark.icons.DarkStandardIcons;
import com.sinergise.themebundle.ui.dark.layout.DarkLayoutResources;
import com.sinergise.themebundle.ui.light.LightResources;

public interface DarkResources extends LightResources {
	
	//TODO: when needed, prepare some new style
//	@Override
//	@Source({DEFAULT_CSS, "defaultStyle.css"})
//	ThemeCss defaultStyle();
	
	@Override
	DarkButtonResources buttonBundle();
	@Override
	DarkLayoutResources layoutBundle();
	@Override
	DarkStandardIcons standardIcons();
}
