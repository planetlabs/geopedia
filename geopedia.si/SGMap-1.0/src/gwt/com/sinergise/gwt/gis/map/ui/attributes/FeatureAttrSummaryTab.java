package com.sinergise.gwt.gis.map.ui.attributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.CFeatureIdentifier;
import com.sinergise.common.gis.feature.HasFeatures;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoItem;
import com.sinergise.common.ui.messages.MessageListener;
import com.sinergise.common.util.event.status.StatusListener;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.common.util.naming.Identifier;
import com.sinergise.gwt.gis.map.shapes.editor.event.FeaturesChangedEvent;
import com.sinergise.gwt.gis.map.shapes.editor.event.FeaturesChangedEvent.FeaturesUpdatedEventHandler;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.attributes.TabbedAttributesPanel.FeatureAttrTabSettings;
import com.sinergise.gwt.ui.MessageBox;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTabLayoutPanel;

/**
 * @author tcerovski
 */
public class FeatureAttrSummaryTab extends SGFlowPanel implements MessageListener, StatusListener, HasFeatures, FeaturesUpdatedEventHandler {
	public static class FeatureAttrSummaryTabSettings extends FeatureAttrTabSettings {
	}
	
	protected final MapComponent map;
	protected final SGTabLayoutPanel parent;
	protected final Map<Identifier, List<CFeature>> featuresMap = new HashMap<Identifier, List<CFeature>>();

	protected CombinedFeatureActionsProvider actProvider;
	protected GenSummaryTableBuilder genTableBuilder;
	protected MessageBox messageBox = null;
	private SimplePanel resultsContainer;

	public FeatureAttrSummaryTab(MapComponent map, SGTabLayoutPanel parent, CombinedFeatureActionsProvider actProvider) {
		this.map = map;
		this.actProvider = actProvider;
		this.parent = parent;
		VerticalPanel vp = new VerticalPanel();
		vp.add(resultsContainer = new SimplePanel());
		vp.add(messageBox = new MessageBox());
		vp.setWidth("100%");

		add(vp);
	}
	
	public void close() {
		parent.closeTab(this);
	}

	public GenSummaryTableBuilder getTableBuilder() {
		if (genTableBuilder == null) {
			genTableBuilder = createTableBuilder();
		}
		return genTableBuilder;
	}

	protected GenSummaryTableBuilder createTableBuilder() {
		return new GenSummaryTableBuilder(actProvider);
	}

	public void addFeature(FeatureInfoItem feature) {
		if (feature != null) {
			getTableBuilder().addData(feature);

			Identifier type = feature.f.getIdentifier().getFeatureTypeID();
			List<CFeature> typeFeatures = featuresMap.get(type);
			if (typeFeatures == null) {
				featuresMap.put(type, typeFeatures = new ArrayList<CFeature>());
			}
			typeFeatures.add(feature.f);
		}
	}

	public void setFeatures(FeatureInfoCollection features) {
		map.getDefaultHighlightLayer().clearSelection();

		clearFeatures();
		if (features == null || features.getItemCount() == 0) {
			return;
		}

		for (int i = 0; i < features.getItemCount(); i++) {
			FeatureInfoItem feature = features.getItem(i);
			if (feature != null) {
				addFeature(feature);
			}
		}

		render();
		ensureVisible();
	}

	public void clearFeatures() {
		getTableBuilder().clearData();
		resultsContainer.clear();
		featuresMap.clear();
	}

	public void render() {
		Widget table = getTableBuilder().generateTables(map);
		table.setWidth("100%");
		resultsContainer.setWidget(table);
	}

	@Override
	public void onMessage(MessageType type, String msg) {
		messageBox.showMsg(type, msg);
		ensureVisible();
	}

	@Override
	public void clearStatus() {
		messageBox.hide();
	}

	@Override
	public void setErrorStatus(String error) {
		messageBox.showErrorMsg(error);
		ensureVisible();
	}

	@Override
	public void setInfoStatus(String status) {
		messageBox.showInfoMsg(status);
		ensureVisible();
	}

	@Override
	public Collection<CFeature> getFeatures() {
		List<CFeature> features = new ArrayList<CFeature>();
		for (List<CFeature> typeFeatures : featuresMap.values()) {
			features.addAll(typeFeatures);
		}
		return features;
	}

	public Collection<CFeature> getSortedFeatures() {
		return getTableBuilder().getFeatures();
	}

	public boolean isEmpty() {
		for (List<CFeature> f : featuresMap.values()) {
			if (!f.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	protected void applySettings(FeatureAttrSummaryTabSettings summarySettings) {
		if (summarySettings != null) {
			summarySettings.registerExtraActions(actProvider);
		}
	}
	
	private void removeFeature(CFeatureIdentifier featureId) {
		List<CFeature> typeFeatures = featuresMap.get(featureId.getFeatureTypeID());
		
		if (typeFeatures != null) {
			for (int i=0; i<typeFeatures.size(); i++) {
				if (typeFeatures.get(i).getQualifiedID().equals(featureId)) {
					typeFeatures.remove(i);
					render();
					break;
				}
			}
		}
		
		if (isEmpty()) {
			close();
		}
	}
	
	@Override
	public void onFeaturesUpdated(FeaturesChangedEvent event) {
		//TODO: handle updates
		if (event.hasDeletedFeatures()) {
			for (CFeature f : getFeatures()) {
				if (event.isDeleted(f)) {
					removeFeature(f.getQualifiedID());
				}
			}
		}
	}

}
