package com.sinergise.gwt.gis.feature;

import com.google.gwt.view.client.ProvidesKey;
import com.sinergise.common.gis.feature.CFeature;

public class CFeatureKeyProvider implements ProvidesKey<CFeature> {
	
	public static final CFeatureKeyProvider INSTANCE = new CFeatureKeyProvider();
	
	private CFeatureKeyProvider() {
		//hide constructor
	}

	@Override
	public Object getKey(CFeature item) {
		return item.getIdentifier();
	}

}
