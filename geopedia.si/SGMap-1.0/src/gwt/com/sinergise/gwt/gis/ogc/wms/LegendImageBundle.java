package com.sinergise.gwt.gis.ogc.wms;

import static com.sinergise.common.gis.map.model.layer.LayersSource.CAPABILITY_LEGEND_IMAGE_SIZE;
import static com.sinergise.common.gis.map.model.layer.MapContextLayers.LAYERS_VIEW_SHOW_LEGEND;
import static com.sinergise.common.util.collections.CollectionUtil.isNullOrEmpty;

import java.util.HashMap;
import java.util.Map;

import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.gis.map.model.layer.MapContextLayers;
import com.sinergise.common.gis.ogc.wms.request.ext.WMSLegendImageBundleRequest;
import com.sinergise.common.util.collections.tree.TreeListenerAdapter;
import com.sinergise.common.util.geom.DimI;

public class LegendImageBundle {
	
	//requires service to return image bundle with images from left to right
	
	public static final String CAPABILITY_LEGEND_BUNDLE_IMAGE = "legendBundleImage";
	
	public static final DimI DEFAULT_SIZE = new DimI(24, 24);
	
	private final WMSLayersSource source;
	private final DimI size;
	private final MapContextLayers mapLayers;
	private Map<String, Integer> layerPositions;
	private Map<String, WMSLayer> layersMap;
	
	private transient String[] bundleLayerNames = null;
	private transient String[] bundleLayerStyles = null;
	
	private boolean shouldReloadLayers = true;
	
	public LegendImageBundle(WMSLayersSource source, MapContextLayers mapLayers) {
		this(source, getLegendImageSizeFromCapabilities(source), mapLayers);
	}
	
	public LegendImageBundle(WMSLayersSource source, DimI size, final MapContextLayers mapLayers) {
		this.source = source;
		this.size = size;
		this.mapLayers = mapLayers;
		
		// listen for new layers
		mapLayers.addTreeListener(new TreeListenerAdapter<LayerTreeElement>() {
			@Override
			public void nodeAdded(LayerTreeElement parent, LayerTreeElement added, int newIndex) {
				shouldReloadLayers = true;
			}
		});
	}
	
	private void readLayers() {
		layerPositions = new HashMap<String, Integer>();
		layersMap = new HashMap<String, WMSLayer>();
		
		int idx = 0;
		for (LayerTreeElement layer : mapLayers.getNamedLayersView(LAYERS_VIEW_SHOW_LEGEND)) {
			if (layer instanceof WMSLayer && ((WMSLayer)layer).getSource().getLocalID().equals(source.getLocalID())) {
				String layerName = ((WMSLayer)layer).getWMSName();
				layerPositions.put(layerName, Integer.valueOf(idx++));
				layersMap.put(layerName, (WMSLayer)layer);
			}
		}
		
		bundleLayerNames = null;
		bundleLayerStyles = null;
		shouldReloadLayers = false;
	}
	
	public String getImageBundleURL(boolean transparent) {
		if (shouldReloadLayers) readLayers();
		if (isNullOrEmpty(layerPositions)) return null;
		
		//do not cache URL in case any of the parameters changes
		WMSLegendImageBundleRequest req = new WMSLegendImageBundleRequest();
		req.setDefaults(source.getRequestDefaults());
		req.setImageSize(size);
		req.setTransparent(transparent);
		req.setLayers(getBundleLayerNames());
		req.setStyles(getBundleLayerStyles());
		return req.createRequestURL(source.baseURL);
	}
	
	protected String[] getBundleLayerNames() {
		if (bundleLayerNames == null) {
			bundleLayerNames = new String[layerPositions.size()];
			for (String layer : layerPositions.keySet()) {
				bundleLayerNames[layerPositions.get(layer).intValue()] = layer;
			}
		}
		return bundleLayerNames;
	}
	
	protected String[] getBundleLayerStyles() {
		if (bundleLayerStyles == null) {
			bundleLayerStyles = new String[layerPositions.size()];
			for (String layerName : layerPositions.keySet()) {
				bundleLayerStyles[layerPositions.get(layerName).intValue()] = layersMap.get(layerName).getWMSStyleName();
			}
		}
		return bundleLayerStyles;
	}
	
	public DimI getImageSize() {
		return size;
	}
	
	public int getImageLeft(String layerName) {
		if (!layerPositions.containsKey(layerName)) return Integer.MAX_VALUE;
		return - size.w() * layerPositions.get(layerName).intValue();
	}
	
	@SuppressWarnings("unused")
	public int getImageTop(String layerName) {
		return 0;
	}
	
	public static DimI getLegendImageSizeFromCapabilities(WMSLayersSource source) {
		if (source.getCapability(CAPABILITY_LEGEND_IMAGE_SIZE) instanceof DimI) {
			return (DimI)source.getCapability(CAPABILITY_LEGEND_IMAGE_SIZE);
		}
		return DEFAULT_SIZE;
	}

}
