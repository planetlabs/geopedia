package com.sinergise.common.gis.feature;

import java.util.Collection;

public interface HasFeatures extends HasFeatureRepresentations {

	@Override
	Collection<CFeature> getFeatures();
	
}
