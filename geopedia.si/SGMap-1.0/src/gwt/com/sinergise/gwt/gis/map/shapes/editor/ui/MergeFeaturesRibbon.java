package com.sinergise.gwt.gis.map.shapes.editor.ui;

import com.google.gwt.user.client.ui.Label;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.util.collections.safe.DefaultTypeSafeKey;
import com.sinergise.common.util.state.gwt.PropertyChangeListenerAdapter;
import com.sinergise.gwt.gis.i18n.Messages;
import com.sinergise.gwt.gis.map.shapes.editor.MergeFeaturesController;

public class MergeFeaturesRibbon extends GeometryEditorRibbon {

	public MergeFeaturesRibbon(MergeFeaturesController controller) {
		super(controller, new GeometryEditorRibbonSettings());
		
		butConfirm.setEnabled(false);
		controller.addPropertyChangeListener(new PropertyChangeListenerAdapter<CFeature>(
			new DefaultTypeSafeKey<CFeature>(MergeFeaturesController.PROP_ADJACENT_FEATURE)) 
		{
			@Override
			public void propertyChange(Object sender, CFeature oldValue, CFeature newValue) {
				butConfirm.setEnabled(newValue != null);
			}
		});
	}
	
	private MergeFeaturesController controller() {
		return (MergeFeaturesController) controller;
	}
	
	@Override
	protected void addDefaultWidgets() {
		super.addDefaultWidgets();
		
		addWidget(new GeometryEditorRibbon.GroupSepparator());
		addWidget(new ZoomToTopologyButton(controller()));
		addWidget(new ResetGeometryEditorButton(controller()));
		
		addWidget(new GeometryEditorRibbon.GroupSepparator());
		addWidget(new AreaLabelWidget(controller()));
		
		addWidget(new GeometryEditorRibbon.GroupSepparator());
	}
	
	protected void addInfoWidget() {
		addWidget(new Label(Messages.INSTANCE.geometryEditor_infoMergeFeatures()));
	}

}
