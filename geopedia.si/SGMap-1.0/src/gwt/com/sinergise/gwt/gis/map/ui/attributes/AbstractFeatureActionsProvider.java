package com.sinergise.gwt.gis.map.ui.attributes;

import static com.sinergise.common.util.collections.CollectionUtil.first;

import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.HasFeatureRepresentations;
import com.sinergise.common.gis.feature.RepresentsFeature;
import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.common.util.string.StringUtil;

public abstract class AbstractFeatureActionsProvider implements FeatureActionsProvider {
	private static final String STKEY_FEATURE_TYPES = "featureTypes";
	private static final String STKEY_ICON = "icon";
	private static final String STKEY_TITLE = "title";

	protected String[] includedFeatureTypes = new String[0];
	protected String icon;
	protected String title;

	protected AbstractFeatureActionsProvider() {
	}
	
	protected AbstractFeatureActionsProvider(StateGWT param) {
		String fTypesStr = param.getString(STKEY_FEATURE_TYPES, "");
		if (!fTypesStr.isEmpty()) {
			includedFeatureTypes = StringUtil.split(fTypesStr, ',');
		}
		icon = param.getString(STKEY_ICON, null);
		title = param.getString(STKEY_TITLE, null);
	}

	public boolean isApplicable(HasFeatureRepresentations features) {
		if (features.getFeatures().isEmpty()) {
			return false;
		}
		if (includedFeatureTypes.length == 0) {
			return true;
		}
		RepresentsFeature repf = first(features.getFeatures());
		if (repf instanceof CFeature) {
			String fType = ((CFeature)repf).getFeatureTypeName();
			for (String s : includedFeatureTypes) {
				if (s.equals(fType)) {
					return true;
				}
			}
		}
		return false;
	}
}
