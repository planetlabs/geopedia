package com.sinergise.gwt.gis.map.ui.attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sinergise.common.gis.feature.HasFeatureRepresentations;
import com.sinergise.common.ui.action.Action;

public class CombinedFeatureActionsProvider implements FeatureActionsProvider {
	
	private List<FeatureActionsProvider> providers = new ArrayList<FeatureActionsProvider>();

	public CombinedFeatureActionsProvider() {}
	
	public CombinedFeatureActionsProvider(CombinedFeatureActionsProvider src) {
		providers.addAll(src.providers);
	}

	@Override
	public List<Action> getFeatureActions(HasFeatureRepresentations fRep, Object requestor) {
		List<Action> actions = new ArrayList<Action>();
		for (FeatureActionsProvider provider : providers) {
			actions.addAll(provider.getFeatureActions(fRep, requestor));
		}
		return Collections.unmodifiableList(actions);
	}
	
	public void registerProvider(FeatureActionsProvider provider) {
		providers.add(provider);
	}
	
	public void registerProvider(int index, FeatureActionsProvider provider) {
		providers.add(index, provider);
	}
	
	public boolean deregisterProvider(FeatureActionsProvider provider) {
		return providers.remove(provider);
	}

}
