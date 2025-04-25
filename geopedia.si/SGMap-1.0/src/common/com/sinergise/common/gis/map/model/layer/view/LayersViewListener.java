package com.sinergise.common.gis.map.model.layer.view;

import com.sinergise.common.gis.map.model.layer.LayerTreeElement;

/**
 * @author tcerovski
 *
 */
public interface LayersViewListener {

	void layerViewChanged();
	
	void layerNodeChanged(LayerTreeElement node, String propertyName);
	
}
