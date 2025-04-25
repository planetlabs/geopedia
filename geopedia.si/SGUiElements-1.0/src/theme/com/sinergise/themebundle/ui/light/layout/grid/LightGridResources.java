package com.sinergise.themebundle.ui.light.layout.grid;

import com.sinergise.gwt.ui.resources.GridResources;
import com.sinergise.gwt.ui.resources.ThemeResources;
import com.sinergise.themebundle.ui.basic.layout.grid.BasicGridResources;

public interface LightGridResources extends GridResources {

	@Override
	@Source(value = {ThemeResources.COLORS, BasicGridResources.DATA_GRID_CSS })
	GridCss dataGridStyle();
}
