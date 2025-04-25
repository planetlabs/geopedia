package com.sinergise.themebundle.ui.basic.layout.grid;

import com.sinergise.gwt.ui.resources.GridResources;
import com.sinergise.gwt.ui.resources.ThemeResources;

public interface BasicGridResources extends GridResources {

	String DATA_GRID_CSS = "com/sinergise/themebundle/ui/basic/layout/grid/dataGrid.css";

	@Override
	@Source(value = {ThemeResources.COLORS, DATA_GRID_CSS })
	GridCss dataGridStyle();
}
