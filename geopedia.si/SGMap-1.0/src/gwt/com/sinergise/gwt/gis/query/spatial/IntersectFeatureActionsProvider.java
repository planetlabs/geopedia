package com.sinergise.gwt.gis.query.spatial;

import static com.sinergise.common.gis.map.model.layer.LayerTreeElement.INHERITANCE_NONE;
import static com.sinergise.common.util.string.StringUtil.isNullOrEmpty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.CFeatureUtils;
import com.sinergise.common.gis.feature.HasFeatureRepresentations;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.gis.map.model.layer.MapContextLayers;
import com.sinergise.common.gis.map.model.layer.info.FeatureItemCollector;
import com.sinergise.gwt.gis.map.ui.attributes.FeatureActionsProvider;

public class IntersectFeatureActionsProvider implements FeatureActionsProvider {
	
	public static final String PROP_KEY_CAN_INTERSECT_WITH = "canIntersectWith";
	
	protected final MapContextLayers mapLayers;
	protected final FeatureItemCollector collector;
	
	public IntersectFeatureActionsProvider(MapContextLayers mapLayers, FeatureItemCollector collector) {
		this.mapLayers = mapLayers;
		this.collector = collector;
	}
	
	@Override
	public List<IntersectFeatureAction> getFeatureActions(HasFeatureRepresentations fRep, Object requestor) {
		CFeature feature = CFeatureUtils.getCFeatureIfHasExactlyOne(fRep);
		if(feature == null) {
			return Collections.emptyList();
		}
		
		FeatureDataLayer featureLayer = mapLayers.findByFeatureType(feature.getFeatureTypeName());
		
		String intersectWithStr = ((LayerTreeElement)featureLayer).getGenericProperty(PROP_KEY_CAN_INTERSECT_WITH, INHERITANCE_NONE);
		
		//can intersect with any layers?
		if (isNullOrEmpty(intersectWithStr)) {
			return Collections.emptyList();
		}
		
		List<FeatureDataLayer> intersectWith = new ArrayList<FeatureDataLayer>();
		for (String featureType : intersectWithStr.split(",")) {
			featureType = featureType.trim();
			if (isNullOrEmpty(featureType)) {
				continue;
			}
			FeatureDataLayer layer = mapLayers.findByFeatureType(featureType);
			if (layer != null) {
				intersectWith.add(layer);
			}
		}
		
		if (intersectWith.isEmpty()) return Collections.emptyList(); 
		
		return Collections.singletonList(createAction(feature, featureLayer, intersectWith.toArray(new FeatureDataLayer[intersectWith.size()])));
	}

	protected IntersectFeatureAction createAction(CFeature feature, FeatureDataLayer featureLayer, FeatureDataLayer[] layers) {
		return new IntersectFeatureAction(feature, featureLayer, collector, layers);
	}

}
