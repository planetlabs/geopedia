package com.sinergise.gwt.gis.map.ui.attributes;

import com.sinergise.common.gis.feature.CFeatureIdentifier;

/**
 * @author tcerovski
 *
 */
public interface FeatureAttrDisplay {

	public boolean isDisplaying(CFeatureIdentifier featureID);
	
	public boolean hasFeature(CFeatureIdentifier featureID);
	
	public boolean displayFeature(CFeatureIdentifier featureID);
	
}
