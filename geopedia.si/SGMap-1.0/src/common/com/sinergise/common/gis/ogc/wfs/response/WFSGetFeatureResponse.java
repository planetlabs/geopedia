package com.sinergise.common.gis.ogc.wfs.response;

import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;

public interface WFSGetFeatureResponse extends WFSResponse, FeatureInfoCollection {
	boolean hasData();
}
