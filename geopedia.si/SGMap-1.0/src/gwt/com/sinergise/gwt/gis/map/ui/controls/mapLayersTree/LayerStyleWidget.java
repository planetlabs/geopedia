package com.sinergise.gwt.gis.map.ui.controls.mapLayersTree;

import com.google.gwt.user.client.ui.Composite;
import com.sinergise.common.gis.map.model.style.Style;


public abstract class LayerStyleWidget extends Composite {
	Style style;
	public LayerStyleWidget(Style style) {
		super();
		this.style=style;
	}
}
