package com.sinergise.geopedia.client.ui.feature;

import com.google.gwt.user.client.ui.FlowPanel;
import com.sinergise.geopedia.client.ui.panels.results.FeatureInfoWidget;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Table;

public abstract class FeatureInfoContentPanel extends FlowPanel {
	 
	 protected interface PanelCreator {
		 FeatureInfoContentPanel createPanel(FeatureInfoWidget featureInfoWidget);
	 }	 
	 public static PanelCreator CREATORINSTANCE = new DivFeatureInfoContentPanel.Creator();
	 
	 public static FeatureInfoContentPanel createNew(FeatureInfoWidget featureInfoWidget) {
		 return CREATORINSTANCE.createPanel(featureInfoWidget);
	 }
	 
	 public abstract void setFeature(final Feature feature, final Table featureTable);
}
