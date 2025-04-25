package com.sinergise.geopedia.pro.client.ui.feature;

import static com.sinergise.gwt.ui.maingui.Buttons.YES;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.geometry.topo.TopologyException;
import com.sinergise.common.geometry.util.CoordStringUtil;
import com.sinergise.common.ui.i18n.Buttons;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.RemoteServices;
import com.sinergise.geopedia.client.core.entities.Repo;
import com.sinergise.geopedia.client.core.events.FeatureInfoEvent;
import com.sinergise.geopedia.client.core.i18n.ExceptionI18N;
import com.sinergise.geopedia.client.core.search.SearchListener.SystemNotificationType;
import com.sinergise.geopedia.client.core.util.GMStats;
import com.sinergise.geopedia.client.resources.GeopediaCommonStyle;
import com.sinergise.geopedia.client.ui.LoadingIndicator;
import com.sinergise.geopedia.client.ui.map.MapWidget;
import com.sinergise.geopedia.client.ui.panels.ActivatableStackPanel;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.GeomType;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.query.FeaturesQueryResults;
import com.sinergise.geopedia.core.query.Query;
import com.sinergise.geopedia.core.query.filter.FilterFactory;
import com.sinergise.geopedia.light.client.SideBar;
import com.sinergise.geopedia.light.client.i18n.GeopediaTerms;
import com.sinergise.geopedia.light.client.ui.HelpPanel;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.client.i18n.ProMessages;
import com.sinergise.geopedia.pro.theme.dialogs.ProDialogsStyle;
import com.sinergise.geopedia.pro.theme.featureedit.FeatureEditStyle;
import com.sinergise.geopedia.themebundle.ui.GeopediaStandardIcons;
import com.sinergise.gwt.ui.Heading;
import com.sinergise.gwt.ui.ImageAnchor;
import com.sinergise.gwt.ui.NotificationPanel;
import com.sinergise.gwt.ui.SGParagraph;
import com.sinergise.gwt.ui.dialog.MessageDialog;
import com.sinergise.gwt.ui.dialog.OptionDialog.ButtonsListener;
import com.sinergise.gwt.ui.maingui.StandardUIConstants;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGHeaderPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTabLayoutPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextBox;
import com.sinergise.gwt.ui.resources.Theme;

public class FeatureEditor {
	private static final Logger logger = LoggerFactory.getLogger(FeatureEditor.class);

	private static FeatureEditor INSTANCE;

	private boolean editorRunning = false;
	
	private MapWidget mapWidget;
	private Feature editedFeature;
	private FeatureGeometryEditor geometryEditor;
	private FeatureMetadataEditor metadataEditor;
	private LoadingIndicator progressIndicator;
	private EditorSidebarPanel sidebarPanel;
	private SideBar sideBar;
	private NotificationPanel notificationPanel;
	
	private SGHeaderPanel content;
	private static final int MAX_POINTS_WARNING = 200;
	
	private HelpPanel helpPanel;
	private SGTabLayoutPanel tabPanel;
	
	private class EditorSidebarPanel extends ActivatableStackPanel {
		
		
		SGFlowPanel btnPanel;
		FlowPanel extraPanel;
		
		public EditorSidebarPanel(boolean addDeleteButton) {
			FeatureEditStyle.INSTANCE.featureEdit().ensureInjected();
			
			setStyleName("featureEditor");
			
			btnPanel= new SGFlowPanel("editingPanel clearfix");
			Anchor btnCancel = new Anchor(Buttons.INSTANCE.cancel());
			btnCancel.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					MessageDialog.createYesNo("", MessageType.QUESTION, ProConstants.INSTANCE.FeatureEditorCancel(), 
							new ButtonsListener() {
						@Override
						public boolean buttonClicked(int whichButton) {
							if (whichButton == YES) {
								closeFeatureEditor();
							}
							return true;
						}
					}, false).center();

					}
				});
				
			SGPushButton btnSave = new SGPushButton(Buttons.INSTANCE.save(), GeopediaStandardIcons.INSTANCE.saveWhite(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					saveCurrentFeature();
				}
			});
			btnSave.addStyleName("blue");
				
			
			if (addDeleteButton) {
				ImageAnchor btnDelete = new ImageAnchor(GeopediaStandardIcons.INSTANCE.deleteWhite(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						
						MessageDialog.createYesNo("", MessageType.QUESTION, ProConstants.INSTANCE.DeleteFeatureConfirmation(), 
								new ButtonsListener() {
									@Override
									public boolean buttonClicked(int whichButton) {
										if (whichButton == YES) {
											editedFeature.setDeleted(true);
											saveCurrentFeature();
										}
										return true;
									}
								}, false).center();
						
					}
		
				});
				btnDelete.setTitle(Buttons.INSTANCE.delete());
				btnDelete.addStyleName("fl-right");
				btnPanel.insert(btnDelete,0);
			}
			btnPanel.addWidgets(btnSave, btnCancel);
			
			
			notificationPanel = new NotificationPanel();
			notificationPanel.hide();
			
			content = new SGHeaderPanel();
			
			tabPanel = new SGTabLayoutPanel();
			tabPanel.setSubmenu();
			content.setContentWidget(tabPanel);
			
			SGHeaderPanel head = new SGHeaderPanel(extraPanel = new FlowPanel(), metadataEditor);
			tabPanel.add(head, GeopediaTerms.INSTANCE.general());
			
			add(content);
			
			ClickHandler removeHelpHandler = new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					helpPanel.removeFromParent();
					content.onResize();
				}
			};
			helpPanel = new HelpPanel(ProMessages.INSTANCE.featureHelp(), removeHelpHandler);
			content.setFooterWidget(helpPanel);
		}
		
		
		@Override
		public void onActivate() {
			container.getTitleHolder().add(btnPanel);
		}
		
		@Override
		public void onDeactivate() {
			btnPanel.removeFromParent();
		}
		public void close() {
			super.deactivate();
		}
	}
	
	
	
	public static FeatureEditor getInstance(MapWidget mapWidget, SideBar sideBar) {
		if (INSTANCE == null) {
			INSTANCE = new FeatureEditor(mapWidget, sideBar);
		}
		return INSTANCE;
	}
	private FeatureEditor(MapWidget mapWidget, SideBar sideBar) {
		this.mapWidget = mapWidget;
		this.sideBar = sideBar;
		progressIndicator = new LoadingIndicator(true, true);
		sideBar.add(progressIndicator);
		progressIndicator.setVisible(false);
		editorRunning = false;
	}

	public void openFeatureEditor(Feature feature, MapWidget mapWidget) {
		if (editorRunning) {
			// TODO open new, close old, warn, bla bla
//			if (!closeFeatureEditor())
				return;
		}
		// GM stats
        GMStats.stats(GMStats.DIALOG_FEATURE_EDIT, 
        		new String[] {GMStats.PARAM_TABLE, GMStats.PARAM_FEATURE},
        		new String[] {String.valueOf(feature.getTableId()), String.valueOf(feature.getId())
        	});
		openEditor(feature);
	}

	
	

	private void openEditor(Feature feature) {
		editorRunning = true;
		

		if (feature.hasValidId()) {
			progressIndicator.setVisible(true);
			
			Query query = new Query();
			query.startIdx=0;
			query.stopIdx=1;
			query.options.add(Query.Options.FLDUSER_ALL);
			query.options.add(Query.Options.FLDMETA_ENVLENCEN);
			query.options.add(Query.Options.FLDMETA_BASE);
			query.options.add(Query.Options.FLDMETA_GEOMETRY);	
			query.tableId = feature.tableId;
			query.scale = mapWidget.getMapComponent().getZoomLevel();
			query.dataTimestamp = 0; //TODO 
			query.filter = FilterFactory.createIdentifierDescriptor(query.tableId, feature.id);
			RemoteServices.getFeatureServiceInstance().executeQuery(query, 
					new AsyncCallback<FeaturesQueryResults>() {

						@Override
						public void onFailure(Throwable caught) {
							progressIndicator.setVisible(false);
							closeFeatureEditor();
						}

						@Override
						public void onSuccess(FeaturesQueryResults result) {
							progressIndicator.setVisible(false);
							if (result.getCollection() != null
									&& result.getCollection().size() > 0) {
								internalOpenEditor(result.getCollection().get(0));
							}

						}
			});			
		} else {
			internalOpenEditor(feature);
		}
	}

	
	private static class GeomPointsUtil {
		private static int getNumPoints(Geometry geom) {
			if (geom instanceof LineString) {
				return ((LineString)geom).getNumCoords()/2;
			} else if (geom instanceof MultiLineString) {
				int size = 0;
				MultiLineString mls = (MultiLineString)geom;
				for (int i=0;i<mls.size();i++) {
					size+=mls.get(i).getNumCoords()/2;
				}
				return size;
			} else if (geom instanceof Polygon) {
				return getNumPoints((Polygon)geom);
			} else if (geom instanceof MultiPolygon) {
				MultiPolygon mp = (MultiPolygon)geom;
				int size=0;
				for (int i=0;i<mp.size();i++) {
					size+=getNumPoints(mp.get(i));
				}
				return size;
			}else if (geom instanceof MultiPoint) {
				return ((MultiPoint)geom).size();
			}
			return 1;
		}

		private static int getNumPoints(Polygon polygon) {
			int size = polygon.getOuter().getNumCoords()/2;
			for (int i=0;i<polygon.getNumHoles();i++) {
				size+=polygon.getHole(i).getNumCoords()/2;
			}
			return size;
		}
	}
	
	
	SGTextBox inputCoordinates;
	
	private void internalOpenEditor(Feature feature) {
		editedFeature = feature;
		
		metadataEditor = new FeatureMetadataEditor();
		metadataEditor.setFeature(editedFeature);
		sidebarPanel = new EditorSidebarPanel(feature.hasValidId());
		sideBar.openResultsPanel().addActivatablePanel(sidebarPanel);
		
		if (feature.geomType.isGeom()) {
			int numPoints = GeomPointsUtil.getNumPoints(feature.featureGeometry);
			if (numPoints>MAX_POINTS_WARNING) {
				MessageDialog.createYesNo("", MessageType.QUESTION, ProMessages.INSTANCE.geometryIsBigWarning(numPoints), 
						new ButtonsListener() {
							@Override
							public boolean buttonClicked(int whichButton) {
								if (whichButton == YES) {
									openGeomEditor();
								}
								return true;
							}
						}, false).center();
			} else {
				openGeomEditor();
			}
			
		}
		
	}
	
	protected void openGeomEditor(){
		if (geometryEditor!=null) {
			tabPanel.remove(geometryEditor);
		}
		
		geometryEditor = new FeatureGeometryEditor(mapWidget);
		tabPanel.add(geometryEditor,GeopediaTerms.INSTANCE.geometry());
		
		if(editedFeature.geomType.isPoint()){
			sidebarPanel.extraPanel.setStyleName("featureCoordinatesHolder mandatory");
			sidebarPanel.extraPanel.add(new InlineLabel(GeopediaTerms.INSTANCE.coordinates()+":"){
				{
					addStyleName("coordinatesFeatureInput");
				}
			});
			sidebarPanel.extraPanel.add(inputCoordinates = new SGTextBox());
			inputCoordinates.setVisibleLength(16);
			if(editedFeature.hasValidId() && editedFeature.featureGeometry != null){
				inputCoordinates.setText(((Point)editedFeature.featureGeometry).x()+" "+((Point)editedFeature.featureGeometry).y());
			}
			geometryEditor.addGeometryChangedListener(new ValueChangeHandler<Geometry>() {

				@Override
				public void onValueChange(ValueChangeEvent<Geometry> event) {
					Geometry newGeom = event.getValue();
					if(newGeom instanceof Point){
						double x = ((Point)newGeom).x();
						double y = ((Point)newGeom).y();
						inputCoordinates.setText(x+" "+y);
					}
				}
				
			});
			inputCoordinates.addValueChangeHandler(new ValueChangeHandler<String>() {

				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					setPointCoordinates();
				}
			});
			sidebarPanel.extraPanel.add(new ImageAnchor(GeopediaCommonStyle.INSTANCE.confirm(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					setPointCoordinates();
				}
			}){
				{
					addStyleName("confirmPointCoords");
					setTitle(ProConstants.INSTANCE.confirmPointCoords());
				}
			});
			sidebarPanel.extraPanel.add(new ImageAnchor(Theme.getTheme().standardIcons().search(), new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					Point newPoint = getInputPoint();
					if(newPoint != null){
						mapWidget.getMapComponent().ensureVisible(newPoint.getEnvelope(), true, true);
						mapWidget.getMapComponent().repaint(0);
					}
				}
			}){
				{
					setTitle(ProConstants.INSTANCE.showPointMap());
				}
			});
		}
		
		try {
			geometryEditor.openGeometryEditor(editedFeature);
		} catch (TopologyException e) {
			handleError(e);
			logger.error("Geometry editor failed ", e);
		}		
	}
	
	private void setPointCoordinates() {
		Point newPoint = getInputPoint();
		if(newPoint != null){
			try {
				editedFeature.setGeometry(newPoint);
				geometryEditor.closeEditor();
				geometryEditor.openGeometryEditor(editedFeature);

			} catch (TopologyException e) {
				handleError(e);
				logger.error("Edit point failed ", e);
			}
		}
	}
	
	private Point getInputPoint(){
		Point newPoint = CoordStringUtil.parseGpsCoords(inputCoordinates.getText(), true);
		if(newPoint == null){
			newPoint = CoordStringUtil.parseCoords(inputCoordinates.getText());
		} else {
			newPoint = ClientGlobals.getCRSSettings().getTransform(CRS.WGS84.getDefaultIdentifier(), ClientGlobals.getCRSSettings().getMainCrsId(), false).point(newPoint, new Point());
		}
		return newPoint;
	}
	private boolean closeFeatureEditor() {
		if (geometryEditor !=null) {
			geometryEditor.closeEditor();
			geometryEditor=null;
		}
		if (metadataEditor !=null) {
			metadataEditor = null;
		}
		if (sidebarPanel!=null) {
			sidebarPanel.close();
			sidebarPanel = null;
		}
		editorRunning = false;
		return true;
	}


	private void saveCurrentFeature() {		
		notificationPanel.removeFromParent();
		content.onResize();
		if (!editedFeature.isDeleted()) {
			if (geometryEditor!=null && GeomType.isGeom(editedFeature.geomType)) {
				if (!editedFeature.getGeometryType().isPoint()) {
					try {
						if (geometryEditor.validate() == null) {
							tabPanel.selectTab(geometryEditor);
							return;
						}
					} catch (TopologyException e) {
						tabPanel.selectTab(geometryEditor);
						content.setHeaderWidget(notificationPanel);
						notificationPanel.showErrorMsg(ProConstants.INSTANCE.topologyError());
						notificationPanel.setIconBig(false);
						e.printStackTrace();
						return;
					}
				}
				if (!geometryEditor.saveGeometry(editedFeature)) {
					tabPanel.selectTab(geometryEditor);
				}
					
				if (editedFeature.featureGeometry == null) {
					content.setHeaderWidget(notificationPanel);
					notificationPanel.showErrorMsg(ProConstants.INSTANCE.missingGeometry());
					notificationPanel.setIconBig(false);
					return;
				}
			}
			if (metadataEditor!=null) {
				try {
					metadataEditor.saveEditorValues(editedFeature);
				} catch (Exception ex) {
					tabPanel.selectTab(metadataEditor);
					content.setHeaderWidget(notificationPanel);
					notificationPanel.showErrorMsg("Error!");
					notificationPanel.setIconBig(false);
				}
				if(!editedFeature.verifyMandatoryUserFields()) {
					tabPanel.selectTab(metadataEditor);
					content.setHeaderWidget(notificationPanel);
					notificationPanel.showErrorMsg(ProConstants.INSTANCE.missingMandatory());
					notificationPanel.setIconBig(false);
					return;
				}
			} 
		}

		progressIndicator.setVisible(true);
		final boolean isNewFeature = !editedFeature.hasValidId();
		RemoteServices.getFeatureServiceInstance().
			saveFeature(editedFeature, new AsyncCallback<Feature>() {

			@Override
			public void onFailure(Throwable caught) {
				progressIndicator.setVisible(false);
				handleError(caught);
				logger.error("Feature saving failed", caught);
			}

			@Override
			public void onSuccess(final Feature featureResult) {
				progressIndicator.setVisible(false);
				Repo.instance().updateTableDataTimestamp(featureResult.tableId,
						featureResult.tableDataTs);
				editedFeature = featureResult;
				closeFeatureEditor();
				ClientGlobals.mainMapWidget.repaint();
				if (isNewFeature) {
					Repo.instance().getTable(featureResult.getTableId(), 0, new AsyncCallback<Table>() {

						@Override
						public void onFailure(Throwable caught) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onSuccess(Table table) {
							ClientGlobals.defaultSearchExecutor.resetSearch();
							ClientGlobals.defaultSearchExecutor.systemNotification(SystemNotificationType.GLOBAL_SEARCH_START, null);
							ClientGlobals.defaultSearchExecutor.searchResults(new Feature[]{featureResult}, 
									table, false, false, null);
							ClientGlobals.defaultSearchExecutor.systemNotification(SystemNotificationType.GLOBAL_SEARCH_DONE, null);
							
						}
					});
				} else {
					FeatureInfoEvent fiEvent = new FeatureInfoEvent(featureResult);
					fiEvent.showFeatureDetails();
					fiEvent.highlight();
					fiEvent.updateFeature();
					ClientGlobals.eventBus.fireEvent(fiEvent);
					
				}
			}
		});

	}

	protected void handleError(Throwable caught) {
		ExceptionPanel ep = new ExceptionPanel(caught);
		ep.center();
	}


	private class ExceptionPanel extends PopupPanel {
		public ExceptionPanel(Throwable caught) {
			super(false);
			ProDialogsStyle.INSTANCE.exceptionPopup().ensureInjected();
			setStyleName(ProDialogsStyle.INSTANCE.exceptionPopup().exceptionPanel());
			FlowPanel contentPanel = new FlowPanel();
			Heading.H1 h1 = new Heading.H1(ProConstants.INSTANCE.error()+"!");
			SGParagraph p = new SGParagraph(ExceptionI18N.getLocalizedMessage(caught));
			contentPanel.add(h1);
			contentPanel.add(p);
			
			SGPushButton close = new SGPushButton(StandardUIConstants.STANDARD_CONSTANTS.buttonClose(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					ExceptionPanel.this.hide();
				}
			});
			contentPanel.add(close);
			
			setWidget(contentPanel);
		}
	}

	
}
