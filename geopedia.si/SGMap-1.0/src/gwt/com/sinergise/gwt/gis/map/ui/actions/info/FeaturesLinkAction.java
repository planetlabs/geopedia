package com.sinergise.gwt.gis.map.ui.actions.info;

import java.util.Collections;
import java.util.List;

import com.sinergise.common.gis.feature.HasFeatureRepresentations;
import com.sinergise.common.gis.feature.auxprops.FeatureAuxiliaryProps;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.util.collections.CollectionUtil;
import com.sinergise.common.util.lang.Function;
import com.sinergise.common.util.naming.Identifier;
import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.gwt.gis.map.ui.actions.MapActionsToolbar.OpenURLAction;
import com.sinergise.gwt.gis.map.ui.attributes.AbstractFeatureActionsProvider;

public class FeaturesLinkAction extends OpenURLAction {
	private static final String PARAM_FEATURE_IDS = "feature_ids";
	private static final String PARAM_FEATURE_TYPE = "feature_type";

	public static class FeaturesLinkActionProvider extends AbstractFeatureActionsProvider {
		public static final String TYPE = "LINK";

		public static final Function<StateGWT, FeaturesLinkActionProvider> FACTORY = new Function<StateGWT, FeaturesLinkActionProvider>() {
			@Override
			public FeaturesLinkActionProvider execute(StateGWT param) {
				return new FeaturesLinkActionProvider(param);
			}
		};
		
		private static final String STKEY_HREF = "href";
		
		private String urlTemplate;
		
		public FeaturesLinkActionProvider(StateGWT param) {
			super(param);
			urlTemplate = param.getString(STKEY_HREF, null);
		}

		@Override
		public List<? extends Action> getFeatureActions(HasFeatureRepresentations features, Object requestor) {
			if (super.isApplicable(features)) {
				FeaturesLinkAction act = new FeaturesLinkAction(features, urlTemplate, title, icon);
				return Collections.singletonList(act);
			}
			return Collections.emptyList();
		}
	}

	@SuppressWarnings("deprecation")
	public FeaturesLinkAction(HasFeatureRepresentations features, String urlTemplate, String actionName, String icon) {
		super(actionName, createURL(urlTemplate, features), icon);
		setTarget("_blank");
	}

	private static String createURL(String urlTemplate, HasFeatureRepresentations features) {
		String ret = urlTemplate;
		
		String featureIDs = StringUtil.collectionToString(Identifier.extractLocalIdsFromIdentifiable(features.getFeatures()), ",");
		ret = FeatureAuxiliaryProps.replaceExpressionVar(ret, PARAM_FEATURE_IDS, featureIDs);
		
		String featureType = CollectionUtil.first(features.getFeatures()).getQualifiedID().getFeatureTypeName();
		ret = FeatureAuxiliaryProps.replaceExpressionVar(ret, PARAM_FEATURE_TYPE, featureType);
		
		return ret;
	}

}
