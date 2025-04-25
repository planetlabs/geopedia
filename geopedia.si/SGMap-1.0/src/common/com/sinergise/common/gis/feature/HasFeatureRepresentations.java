package com.sinergise.common.gis.feature;

import java.util.Collection;

public interface HasFeatureRepresentations {

	Collection<? extends RepresentsFeature> getFeatures();
	
}
