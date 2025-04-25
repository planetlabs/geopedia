package com.sinergise.gwt.ui.resources;

import com.google.gwt.user.cellview.client.DataGrid.Resources;
import com.google.gwt.user.cellview.client.DataGrid.Style;

public interface GridResources extends Resources {

	public interface GridCss extends Style {
		String clickableRow();
	}
	
	public GridCss dataGridStyle();
}
