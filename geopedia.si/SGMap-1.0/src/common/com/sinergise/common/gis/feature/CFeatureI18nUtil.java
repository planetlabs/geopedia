package com.sinergise.common.gis.feature;

import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;
import com.sinergise.common.web.i18n.I18nUtil;
import com.sinergise.common.web.i18n.LookupStringProvider;

public class CFeatureI18nUtil {
	
	private CFeatureI18nUtil() {
		//hide constructor
	}
	
	public static void resolveExternalizedStrings(FeatureInfoCollection features, LookupStringProvider lookup) {
		CFeatureDescriptor lastDesc = null;
		for (int i=0; i<features.getItemCount(); i++) {
			if (features.getItem(i).f.auxProps != null) {
				I18nUtil.resolveExternalizedStrings(features.getItem(i).f.auxProps.storeInternalState(null), lookup);
			}
			CFeatureDescriptor desc = features.getItem(i).f.getDescriptor();
			if (lastDesc == null || lastDesc != desc) {
				resolveExternalizedStrings(desc, lookup);
				lastDesc = desc;
			}
		}
	}
	
	public static void resolveExternalizedStrings(CFeatureDescriptor desc, LookupStringProvider lookup) {
		if (lookup != null) {
			I18nUtil.resolveExternalizedStrings(desc.storeInternalState(null), lookup);
			I18nUtil.resolveExternalizedStrings(desc.getPropertyDefaults().storeInternalState(null), lookup);
			for (PropertyDescriptor<?> propDesc : desc) {
				I18nUtil.resolveExternalizedStrings(propDesc.storeInternalState(null), lookup);
			}
		}
	}

}
