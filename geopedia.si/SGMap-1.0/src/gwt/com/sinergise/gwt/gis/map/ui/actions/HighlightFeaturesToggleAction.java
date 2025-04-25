package com.sinergise.gwt.gis.map.ui.actions;

import java.util.Collection;
import java.util.Collections;

import com.sinergise.common.gis.feature.HasFeatureRepresentations;
import com.sinergise.common.gis.feature.RepresentsFeature;
import com.sinergise.common.gis.map.model.layer.system.SelectionSetLayer;
import com.sinergise.common.ui.action.ToggleAction;
import com.sinergise.gwt.gis.i18n.Tooltips;
import com.sinergise.gwt.gis.resources.GisTheme;

public class HighlightFeaturesToggleAction extends ToggleAction implements HasFeatureRepresentations {
	
	protected final SelectionSetLayer highlightLayer;
	protected HasFeatureRepresentations toHighlight;
	
	public HighlightFeaturesToggleAction(SelectionSetLayer highlightLayer, HasFeatureRepresentations toHighlight) {
		super("toggleHiglight");
		
		setDescription(Tooltips.INSTANCE.toolbar_highlight());
		setIcon(GisTheme.getGisTheme().gisStandardIcons().highlight());
		setStyle("highlightFeatures");
		this.highlightLayer = highlightLayer;
		this.toHighlight = toHighlight;
		this.highlightLayer.addCollection(this);
	}

	@Override
	protected void selectionChanged(boolean newSelected) {
		highlightLayer.updateCollection(this);
		setIcon(newSelected ? GisTheme.getGisTheme().gisStandardIcons().highlightOn() : GisTheme.getGisTheme().gisStandardIcons().highlight());
	}
	
	/**
	 * Returns features to highlight. If the toggle button is not selected it does not return anything.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Collection<RepresentsFeature> getFeatures() {
		if (isSelected()) {
			return (Collection<RepresentsFeature>)toHighlight.getFeatures();
		}
		return Collections.emptyList();
	}
	
	public SelectionSetLayer getHighlightLayer() {
		return highlightLayer;
	}

}
