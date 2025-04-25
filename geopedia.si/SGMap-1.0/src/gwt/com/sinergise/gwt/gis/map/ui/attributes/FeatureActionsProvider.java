package com.sinergise.gwt.gis.map.ui.attributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sinergise.common.gis.feature.HasFeatureRepresentations;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.util.lang.Function;
import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.gwt.gis.map.ui.actions.info.FeaturesLinkAction.FeaturesLinkActionProvider;

/**
 * @author tcerovski
 *
 */
public interface FeatureActionsProvider {
	
	public static class FeatureActionsProviderSettings {
		protected FeatureActionsProvider ap;
		protected Integer index;
	}
	
	public static final class Util {
		public static final String STKEY_TYPE = "type";
		public static final String STKEY_INDEX = "index";
		
		private static Map<String, Function<StateGWT, ? extends FeatureActionsProvider>> registeredTypes = new HashMap<String, Function<StateGWT, ? extends FeatureActionsProvider>>();
		
		public static void registerActionProviderType(String typeName, Function<StateGWT, ? extends FeatureActionsProvider> factory) {
			registeredTypes.put(typeName, factory);
		}
		
		public static FeatureActionsProviderSettings fromState(StateGWT stateGWT) {
			FeatureActionsProviderSettings ret = new FeatureActionsProviderSettings();
			
			ret.index = stateGWT.getInteger(STKEY_INDEX, null);
			
			String typeStr = stateGWT.getString(STKEY_TYPE, null);
			Function<StateGWT, ? extends FeatureActionsProvider> factory = registeredTypes.get(typeStr);
			if (factory == null) {
				throw new IllegalArgumentException("Unknown action type: "+typeStr);
			}
			ret.ap = factory.execute(stateGWT);
			return ret;
		}
		
		static {
			registerActionProviderType(FeaturesLinkActionProvider.TYPE, FeaturesLinkActionProvider.FACTORY);
		}
	}

	List<? extends Action> getFeatureActions(HasFeatureRepresentations features, Object requestor);
}
