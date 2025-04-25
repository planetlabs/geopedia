package com.sinergise.common.gis.map.model.layer.view;

import java.util.Vector;

import com.sinergise.common.gis.map.model.layer.LayerTreeElement;

/**
 * @author tcerovski
 *
 */
@SuppressWarnings("serial")
public final class LayersViewListenerCollection extends Vector<LayersViewListener> {

	public void fireLayerViewChanged() {
		for (LayersViewListener listener : this) {
			listener.layerViewChanged();
		}
	}

	public void fireLayerNodeChanged(LayerTreeElement node, String propertyName) {
		for (LayersViewListener listener : this) {
			listener.layerNodeChanged(node, propertyName);
		}
	}

}
