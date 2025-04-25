package com.sinergise.themebundle.ui.sigov;

import com.sinergise.themebundle.ui.basic.BasicResources;
import com.sinergise.themebundle.ui.sigov.layout.SigovLayoutResources;
import com.sinergise.themebundle.ui.sigov.button.SigovButtonResources;

public interface SigovResources extends BasicResources {
	@Override
	@Source({BasicResources.DEFAULT_CSS, "defaultStyle.css"})
	ThemeCss defaultStyle();
	
	@Override
	SigovButtonResources buttonBundle();
	@Override
	SigovLayoutResources layoutBundle();
	@Override
	SigovIcons standardIcons();
}
