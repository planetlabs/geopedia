package com.sinergise.gwt.gis.feature;

import com.google.gwt.view.client.ProvidesKey;
import com.sinergise.common.gis.feature.CFeatureIdentifier;

public class CFeatureIdentifierKeyProvider implements ProvidesKey<CFeatureIdentifier> {
	
	public static final CFeatureIdentifierKeyProvider INSTANCE = new CFeatureIdentifierKeyProvider();
	
	private CFeatureIdentifierKeyProvider() {
		//hide constructor
	}

	@Override
	public Object getKey(CFeatureIdentifier item) {
		return item;
	}

}
