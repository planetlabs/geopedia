package com.sinergise.themebundle.ui.light.layout;

import com.google.gwt.resources.client.ImageResource;
import com.sinergise.gwt.ui.resources.LayoutResources;
import com.sinergise.gwt.ui.resources.ThemeResources;
import com.sinergise.themebundle.ui.light.icons.LightStandardIcons;

public interface LightLayoutResources extends LayoutResources {
	String LAYOUT_CSS = "com/sinergise/themebundle/ui/light/layout/layoutStyle.css";
	String NEW_LAYOUT_CSS = "com/sinergise/themebundle/ui/light/layout/tabLayout.css";
	String LAYOUT_COMPONENTS_CSS = "com/sinergise/themebundle/ui/light/layout/layoutComponents.css";
	
	@Override
	@Source({ThemeResources.COLORS,LAYOUT_CSS})
	LayoutCss layoutStyle();
	@Override
	@Source({ThemeResources.COLORS,NEW_LAYOUT_CSS})
	TabLayoutCss tabLayout();
	@Override
	@Source({ThemeResources.COLORS,LAYOUT_COMPONENTS_CSS})
	LayoutComponentsCss layoutComponents();
	
	@Source(LightStandardIcons.LIGHT_ICONS_PATH+"closeMini.png")
	ImageResource miniClose();
	@Source(LightStandardIcons.LIGHT_ICONS_PATH+"search.png")
	ImageResource search();
	@Source(LightStandardIcons.LIGHT_ICONS_PATH+"save.png")
	ImageResource filter();
	@Source(LightStandardIcons.LIGHT_ICONS_PATH+"pin.png")
	ImageResource pin();
	@Source(LightStandardIcons.LIGHT_ICONS_PATH+"pinned.png")
	ImageResource pinned();
	
	@Override
	@Source(LightStandardIcons.LIGHT_ICONS_PATH+"closePanel.png")
	ImageResource closePanel();
	@Override
	@Source(LightStandardIcons.LIGHT_ICONS_PATH+"openPanel.png")
	ImageResource openPanel();
}
