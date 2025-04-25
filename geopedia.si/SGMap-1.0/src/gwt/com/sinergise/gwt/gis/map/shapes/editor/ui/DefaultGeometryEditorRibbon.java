package com.sinergise.gwt.gis.map.shapes.editor.ui;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;
import com.sinergise.gwt.gis.map.shapes.editor.DefaultTopoEditorController;


public class DefaultGeometryEditorRibbon extends GeometryEditorRibbon {
	public static class DefaultGeometryEditorRibbonSettings extends GeometryEditorRibbonSettings {
		public boolean areaLabel = true;
		public boolean clearButton = true;
	}

	protected List<IsWidget> topoEditorWidgets;

	public DefaultGeometryEditorRibbon(DefaultTopoEditorController controller) {
		this(controller, new DefaultGeometryEditorRibbonSettings());
	}
	public DefaultGeometryEditorRibbon(DefaultTopoEditorController controller, DefaultGeometryEditorRibbonSettings settings) {
		super(controller, settings);
		this.settings = settings;
	}
	
	private DefaultTopoEditorController controller() {
		return (DefaultTopoEditorController) controller;
	}
	
	@Override
	protected void addDefaultWidgets() {
		super.addDefaultWidgets();
		
		addTopoWidget(new GeometryEditorRibbon.GroupSepparator());
		addTopoWidget(new ZoomToTopologyButton(controller()));
		if (((DefaultGeometryEditorRibbonSettings)settings).clearButton) {
			addTopoWidget(new ClearGeometryButton(controller()));
		}
		addTopoWidget(new ResetGeometryEditorButton(controller()));
		
		addTopoWidget(new GeometryEditorRibbon.GroupSepparator());
		addTopoWidget(new ToggleTopoLabelsButton(controller()));
		if (((DefaultGeometryEditorRibbonSettings)settings).areaLabel) {
			addTopoWidget(new AreaLabelWidget(controller()));
		}
		addTopoWidget(new ApplyGeometryBufferWidget(controller()));
		
		addTopoWidget(new GeometryEditorRibbon.GroupSepparator());
		addTopoWidget(new ToggleLayerSnapProviderButton(controller().getLayersSnapProvider()));
		addTopoWidget(new PolygonAreaSnapProviderWidget(controller().getAreaSnapProvider()));
	}
	
	protected void addTopoWidget(IsWidget w) {
		if (topoEditorWidgets == null) {
			topoEditorWidgets = new ArrayList<IsWidget>();
		}
		
		addWidget(w);
		topoEditorWidgets.add(w);
	}
	
}
