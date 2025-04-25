package com.sinergise.geopedia.pro.client.ui;

import java.util.Collection;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.util.collections.CollectionUtil;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.ClientSession;
import com.sinergise.geopedia.client.core.events.ClientSessionEvent;
import com.sinergise.geopedia.client.core.events.OpenSidebarPanelEvent;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.ui.map.MapWidget;
import com.sinergise.geopedia.client.ui.panels.ActivatableTabPanel;
import com.sinergise.geopedia.core.crs.CRSSettings;
import com.sinergise.geopedia.core.entities.Permissions;
import com.sinergise.geopedia.light.client.i18n.GeopediaTerms;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.client.ui.importexport.ImportNewLayerDialog;
import com.sinergise.geopedia.pro.client.ui.tools.CoordinateConverterSidebarTab;
import com.sinergise.geopedia.pro.client.ui.tools.ShowContourSidebarTab;
import com.sinergise.geopedia.pro.theme.tools.ToolsStyle;

public class ToolsSidebarPanel extends ActivatableTabPanel {

	
	private MapWidget mapWidget;
	private FlowPanel actionsPanel ;
	MeasurerSidebarTab measurerTab = null;
	CoordinateConverterSidebarTab coordConverterTab = null;
	ShowContourSidebarTab showContourTab = null;
	
	
	public ToolsSidebarPanel(final MapWidget mapWidget) {
		ToolsStyle.INSTANCE.toolsStyle().ensureInjected();
		this.mapWidget=mapWidget;
		setTabTitle(Messages.INSTANCE.ToolsPanel_Title());
		addStyleName("tools");
		actionsPanel = new FlowPanel();
		addContent(actionsPanel);
		updateUI();
		

		ClientSessionEvent.register(ClientGlobals.eventBus, new ClientSessionEvent.Handler() {

			@Override
			public void onAutoLoginEvent(ClientSessionEvent event) {
			}

			@Override
			public void onLogin(ClientSessionEvent event) {
				updateUI();
			}

			@Override
			public void onLogout(ClientSessionEvent event) {
				updateUI();
			}
			@Override
			public void onSessionChanged(ClientSessionEvent event) {
			}
			
		});
	}
	
	
	private void updateUI() {
		actionsPanel.clear();
		Anchor btnMeasurer = createNewAction(ToolsStyle.INSTANCE.measureIcon(), ProConstants.INSTANCE.measureDistanceSurface(), ProConstants.INSTANCE.measureDistanceSurface());
		btnMeasurer.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				measurerTab = new MeasurerSidebarTab();
				ClientGlobals.eventBus.fireEvent(OpenSidebarPanelEvent.openCustomPanel(measurerTab));
			}
			
		});
		
		CRSSettings crsSettings = ClientGlobals.getCRSSettings();
		Collection<CRS> transforms = ClientGlobals.crsSettings.getFromCRSTransformCapabilities(crsSettings.getMainCrsId());
		
		Anchor btnConverter = null;
		if(!CollectionUtil.isNullOrEmpty(transforms)){
			btnConverter = createNewAction(ToolsStyle.INSTANCE.converterIcon(),ProConstants.INSTANCE.converter(), ProConstants.INSTANCE.converterDescription());
			btnConverter.addClickHandler(new ClickHandler() {
	
				@Override
				public void onClick(ClickEvent event) {
					coordConverterTab = new CoordinateConverterSidebarTab();
					ClientGlobals.eventBus.fireEvent(OpenSidebarPanelEvent.openCustomPanel(coordConverterTab));
				}
				
			});
		}
		
		if (ClientSession.canPerformOperation(Permissions.PERM_PEDIAPRO)) {
			Anchor importLink = createNewAction(ToolsStyle.INSTANCE.importIcon(),GeopediaTerms.INSTANCE.Import(),ProConstants.INSTANCE.Import_Description());
			importLink.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					onImportAction();
				}
			});
			actionsPanel.add(importLink);
			if(ClientGlobals.configuration.hasDMV){
				Anchor contoursLink = createNewAction(ToolsStyle.INSTANCE.contourIcon(),ProConstants.INSTANCE.Show_contours(),ProConstants.INSTANCE.Show_contour_Description());
				contoursLink.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						showContourTab = new ShowContourSidebarTab();	
						ClientGlobals.eventBus.fireEvent(OpenSidebarPanelEvent.openCustomPanel(showContourTab));
					}
	
				});
				actionsPanel.add(contoursLink);
			}
		}
		
		actionsPanel.add(btnMeasurer);
		if(btnConverter != null) actionsPanel.add(btnConverter);
	}
	
	private Anchor createNewAction(ImageResource actionImgRes, String actionText, String descriptionText) {
		Anchor importLink = new Anchor();		
		importLink.setStyleName("tool");
		Image actionImg = new Image();
		actionImg.setResource(actionImgRes);
		Label title = new Label(actionText);
		title.setStyleName("title");
		Label details = new Label(descriptionText);
		details.setStyleName("details");
		importLink.getElement().appendChild(actionImg.getElement());
		importLink.getElement().appendChild(title.getElement());
		importLink.getElement().appendChild(details.getElement());
		return importLink;
		
	}

	private void onImportAction() {
		ImportNewLayerDialog inld = new ImportNewLayerDialog(mapWidget);
		inld.show();
	}
}
