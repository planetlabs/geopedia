package com.sinergise.gwt.gis.map.ui.overlays;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.layer.system.SelectionSetLayer;
import com.sinergise.gwt.gis.map.ui.OverlayComponent;
import com.sinergise.gwt.gis.map.ui.overlays.highlight.HighlightOverlay;


public class SystemOverlaysProvider extends DefaultOverlaysProvider {
	
	@Override
	protected boolean isCompatible(OverlayComponent<?> previous) {
		return false;
	}

	@Override
	protected OverlayComponent<?> addLayerToOverlay(CRS mapCRS, OverlayComponent<?> previous, Layer layer) {
		if (layer instanceof SelectionSetLayer) {
			return new HighlightOverlay((SelectionSetLayer)layer);
		}
		return null;
	}
}
