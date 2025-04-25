package com.sinergise.gwt.gis.map.ui.overlays.highlight;

import com.sinergise.common.gis.map.render.RenderInfo;
import com.sinergise.gwt.gis.map.ui.OverlayComponent;

public class HighlightRenderInfo extends RenderInfo {
	public final RenderInfo[] infos;
	public final OverlayComponent<?>[] overlays;

	public HighlightRenderInfo(int size) {
		infos = new RenderInfo[size];
		overlays = new OverlayComponent[size];
		if (size == 0) {
			hasAnything = false;
		}
	}
}