package com.sinergise.geopedia.pro.client.ui.feature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.topo.TopologyException;
import com.sinergise.geopedia.client.components.editor.GeometryEditor;
import com.sinergise.geopedia.client.ui.map.MapWidget;
import com.sinergise.geopedia.client.ui.panels.ActivatableTabPanel;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.GeomType;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.client.i18n.ProMessages;
import com.sinergise.geopedia.pro.theme.featureedit.FeatureEditStyle;
import com.sinergise.geopedia.themebundle.ui.GeopediaStandardIcons;
import com.sinergise.gwt.ui.Heading;
import com.sinergise.gwt.ui.ImageAnchor;
import com.sinergise.gwt.ui.maingui.extwidgets.SGHeaderPanel;

public class FeatureGeometryEditor extends ActivatableTabPanel {

	protected static final Logger logger = LoggerFactory.getLogger(FeatureGeometryEditor.class);
	private MapWidget mapWidget;
	
	private GeometryEditorFlowPanel geomEditPanel = null;
	protected GeometryValidationPanel gValidationPanel;
	protected GeometryEditor gEditor;
	
	public FeatureGeometryEditor (MapWidget mapWidget) {
		
		super(false);
		this.mapWidget=mapWidget;

		gEditor = new GeometryEditor(mapWidget);
		gValidationPanel = new GeometryValidationPanel(mapWidget);
		
		SGHeaderPanel contentPanel = new SGHeaderPanel();
		
		ImageAnchor btnValidate = new ImageAnchor(ProConstants.INSTANCE.codeEditorValidate(), GeopediaStandardIcons.INSTANCE.check(), new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				try {
					validate(); 
				} catch (TopologyException e) {
					showError(e.getMessage());
					logger.error(ProMessages.INSTANCE.geometryErrorTopology() + ": " + e.getMessage());
				}
			}
		});
		FlowPanel topBar = new FlowPanel();
		topBar.setStyleName(FeatureEditStyle.INSTANCE.featureGeometry().topBar());
		topBar.add(btnValidate);
		topBar.add(new Heading.H2(ProConstants.INSTANCE.topologyCheck()));
		contentPanel.setHeaderWidget(topBar);
		contentPanel.setContentWidget(gValidationPanel);
		addContent(contentPanel);
	}
	
	public void addGeometryChangedListener(ValueChangeHandler<Geometry> handler){
		gEditor.addGeometryChangedListener(handler);
	}
	
	public boolean openGeometryEditor(Feature editedFeature) throws TopologyException {
		if (editedFeature.geomType == GeomType.NONE) {
			return false;
		}
		if (gEditor.isRunning()) {
			return false;
		}
		
		showGeometryEditorPanel(editedFeature);
		gEditor.editFeature(editedFeature);
		
		return true;
	}

	private void showGeometryEditorPanel(Feature editedFeature) {
		if (geomEditPanel!=null && geomEditPanel.isAttached())
			return;
		geomEditPanel = new GeometryEditorFlowPanel(mapWidget, "geomEditPanel");
		geomEditPanel.setGeometryType(editedFeature.getGeometryType());
		geomEditPanel.addToMap();
	}
	
	public void closeEditor() {
		
		gEditor.closeEditor();
		
		if (geomEditPanel!=null) {
			geomEditPanel.removeFromParent();
			geomEditPanel=null;
		}
		
	}

	public boolean saveGeometry(Feature feature) {
		boolean saved = false;
		try{
			saved = gEditor.saveFeature(feature);
		} catch (Exception e){
			showError(e.getMessage());
			logger.error("Could not save feature: " + e.getMessage());
			saved = false;
		}
		
		return saved;
	}

	public boolean isRunning() {
		return geomEditPanel!=null;
	}
	
	protected void showError(String error) {
		geomEditPanel.showError(error);
	}


	protected Geometry validate() throws TopologyException {
		return gValidationPanel.validate(gEditor);
	}
	
}
