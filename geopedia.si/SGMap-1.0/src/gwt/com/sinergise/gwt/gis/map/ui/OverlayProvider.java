/*
 *
 */
package com.sinergise.gwt.gis.map.ui;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.gis.map.model.layer.Layer;


public interface OverlayProvider {
    OverlayComponent<?> addLayer(CRS mapCRS, OverlayComponent<?> previous, Layer layer);
}
