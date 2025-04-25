package com.sinergise.java.raster.admin;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.java.swing.map.OrDisplay;
import com.sinergise.java.swing.map.OrDisplayComponent;
import com.sinergise.java.swing.map.layer.OrLayer;

public class RasterAdminMap extends OrDisplayComponent {
	OrLayer datasetLayer;
	OrLayer sourcesLayer;
	
	public RasterAdminMap() {
		super(new OrDisplay(CRS.NONAME_WORLD_CRS));
		initialize();
	}
	protected void initialize() {
		repaint();
	}
	
	public void setDatasetLayer(OrLayer rasterLayer) {
		if (this.datasetLayer != null) {
			getDisplay().removeLayer(this.datasetLayer);
		}
		this.datasetLayer = rasterLayer;
		getDisplay().addLayer(0, rasterLayer);
		getDisplay().updateDisplay();
	}

	public void setSourcesLayer(OrLayer sourcesLayer) {
		if (this.sourcesLayer != null) {
			getDisplay().removeLayer(this.sourcesLayer);
		}
		this.sourcesLayer = sourcesLayer;
		int idx = this.datasetLayer == null ? 0 : 1;
		getDisplay().addLayer(idx, sourcesLayer);
		getDisplay().updateDisplay();
	}

}
