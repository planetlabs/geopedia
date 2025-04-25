/*
 *
 */
package com.sinergise.common.gis.map.model.layer;

import com.sinergise.common.util.geom.DimI;

public interface LegendImageSource {
    String getLegendImageURL(LayerTreeElement el, DimI size, boolean trans);
}
