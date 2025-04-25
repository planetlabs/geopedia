/*
 *
 */
package com.sinergise.gwt.gis.map.ui.overlays.highlight;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.gis.feature.FeaturesSource;
import com.sinergise.common.gis.map.model.layer.system.SelectionSetLayer;
import com.sinergise.gwt.gis.map.ui.OverlayComponent;


public interface HighlightOverlayProvider {
    OverlayComponent<?> createHighlightOverlay(CRS mapCRS, FeaturesSource dataSource, SelectionSetLayer highlightLayer);
	boolean canHandle(FeaturesSource dataSource);
}
