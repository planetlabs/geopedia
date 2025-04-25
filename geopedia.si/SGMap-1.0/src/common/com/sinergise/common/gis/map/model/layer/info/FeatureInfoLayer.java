/*
 *
 */
package com.sinergise.common.gis.map.model.layer.info;

import com.sinergise.common.gis.map.model.layer.FeaturesLayer;


public interface FeatureInfoLayer extends FeaturesLayer {
	public static final String PROP_FEATURE_INFO_ENABLED="queryable";
    @Override
	public FeatureInfoSource getFeaturesSource();
    public boolean isFeatureInfoEnabled();
}
