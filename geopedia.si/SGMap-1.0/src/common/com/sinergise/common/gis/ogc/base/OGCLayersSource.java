package com.sinergise.common.gis.ogc.base;

import com.sinergise.common.gis.map.model.layer.LayersSource;

public interface OGCLayersSource extends LayersSource {
	long getLastChanged();
	void invalidate(long changeTimestamp);
}
