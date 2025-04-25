package com.sinergise.geopedia.pro.client.ui.tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.LatLonCRS;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.display.ScaleLevelsSpec.ZoomLevelsDisp;
import com.sinergise.common.geometry.display.event.CoordinatesListener;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.util.CoordinateFormat;
import com.sinergise.common.ui.controls.object.ObjectStringRenderer;
import com.sinergise.common.util.Util;
import com.sinergise.common.util.event.selection.ObjectSelectionListener;
import com.sinergise.common.util.event.selection.ObjectSelector;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.ui.map.MapWidget;
import com.sinergise.geopedia.client.ui.panels.ActivatableTabPanel;
import com.sinergise.geopedia.core.crs.CRSSettings;
import com.sinergise.geopedia.light.client.i18n.GeopediaTerms;
import com.sinergise.geopedia.light.client.i18n.LightMessages;
import com.sinergise.geopedia.light.theme.GeopediaLightStyle;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.themebundle.ui.GeopediaStandardIcons;
import com.sinergise.gwt.gis.map.ui.actions.MouseClickActionW;
import com.sinergise.gwt.gis.map.ui.controls.coords.ZoomSelectWidget;
import com.sinergise.gwt.gis.map.ui.controls.coords.ZoomSelectWidget.ZoomSelectWidgetType;
import com.sinergise.gwt.gis.map.ui.vector.Marker;
import com.sinergise.gwt.gis.map.ui.vector.MarkerOverlay;
import com.sinergise.gwt.gis.map.ui.vector.signs.ImageSign;
import com.sinergise.gwt.ui.BoldText;
import com.sinergise.gwt.ui.NotificationPanel;
import com.sinergise.gwt.ui.controls.object.ObjectListBox;
import com.sinergise.gwt.ui.core.MouseHandler;
import com.sinergise.gwt.ui.handler.EnterKeyDownHandler;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.ui.resources.Theme;

public class CoordinateConverterSidebarTab extends ActivatableTabPanel{

	CoordinateConverterPanel convPanel;
	
	private static final int COORD_CONV_MARKER_Z = 101;
	
	private static class CoordinateConverterPanel extends FlowPanel  {
		
		private final ImageSign MARKER_SIGN = new ImageSign(GWT.getModuleBaseURL() + "images/cursor/target.png", DimI.create(37, 40));
		private final Marker marker = new Marker(MARKER_SIGN, new Point(0, 0));
		
		private ObjectListBox<CRS> lbFromCRS = new ObjectListBox<CRS>(true);
	    private ObjectListBox<CRS> lbToCRS = new ObjectListBox<CRS>(true);
	    private ObjectStringRenderer<CRS> crsRenderer = new ObjectStringRenderer<CRS>() {
	    	@Override
	    	public String getStringRepresentation(CRS object) {
	    		return object.getNiceName();
	    	}
		};
		
		private final DisplayCoordinateAdapter dca;
		private final Point coordinate = new Point();
	    
	    private CoordinatesEditor fromCRSEditor;
	    private ZoomSelectWidget zoomSelectWidget;
	    private Label toCRSLabel;
	    private SGPushButton btnShowPosition;
	    private CRSSettings crsSettings;
	    private MapWidget mapWidget;
	    private NotificationPanel pnlPickPointNotificationHolder;
	    private MouseClickActionW pickCoordinateMouseAction;
	    private MarkerOverlay markersOverlay;
	    private CoordinateFormat formatXY = new CoordinateFormat("XA YA: XC{0.###} YC{0.###}");
	    private CoordinateFormat formatLATLON=new CoordinateFormat("Xd°m''S{0.##}\" w Yd°m''S{0.##}\" w");
	    
		public CoordinateConverterPanel(CRSSettings crsSettings) {
			this.mapWidget = ClientGlobals.mainMapWidget;
			this.dca = mapWidget.getMapComponent().getCoordinateAdapter();
			this.crsSettings = crsSettings;
			
			pickCoordinateMouseAction = new MouseClickActionW(dca, "Pick coordinate action") {
				protected boolean mouseClickedW(double xWorld, double yWorld) {
					onCoordinatePicked(xWorld, yWorld);
					return true;
				}
			};
			
			btnShowPosition = new SGPushButton(GeopediaTerms.INSTANCE.show(), GeopediaStandardIcons.INSTANCE.search(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					showPosition();
				}
			});
			
			markersOverlay = new MarkerOverlay(mapWidget.getMapComponent().getCoordinateAdapter());
			markersOverlay.addPoint(marker);
			mapWidget.getMapComponent().addOverlay(markersOverlay, COORD_CONV_MARKER_Z);
			
			fromCRSEditor = new CoordinatesEditor();
			toCRSLabel = new Label();
			toCRSLabel.setStyleName("convertLabel");
			
			lbFromCRS.setItemStringRenderer(crsRenderer);
			lbToCRS.setItemStringRenderer(crsRenderer);

			lbFromCRS.setItems(crsSettings.getCoordinateSystems());
			fromCRSEditor.setCRS(lbFromCRS.getSelected());
			
			lbFromCRS.addSelectionListener(new ObjectSelectionListener<CRS>() {
				@Override
				public void objectSelected(CRS object, ObjectSelector<CRS> sender) {
					updateToCRSListbox(object);
					updateEditorCRS();
				}
			});
			
			lbToCRS.addSelectionListener(new ObjectSelectionListener<CRS>() {
				@Override
				public void objectSelected(CRS object, ObjectSelector<CRS> sender) {
					updateFromCRSListbox(object);					
					updateToCoordUI();
				}
			});
			
			fromCRSEditor.addEnterKeyDownHandler(new EnterKeyDownHandler() {
				@Override
				public void onEnterDown(KeyDownEvent event) {
					applySourceCoordinates();
				}
			});
			
			updateToCRSListbox(lbFromCRS.getSelected());
			lbToCRS.setSelected(CRS.WGS84);
			
			SGPushButton btnConvert = new SGPushButton(GeopediaTerms.INSTANCE.convert(), Theme.getTheme().standardIcons().refresh());
			btnConvert.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					applySourceCoordinates();
				}
			});
			
			pnlPickPointNotificationHolder = new NotificationPanel(LightMessages.INSTANCE.coordinatePickPoint(), MessageType.INFO);
			pnlPickPointNotificationHolder.setVisible(false);
			add(pnlPickPointNotificationHolder);
			
			Grid layout = new Grid(5,5);
			layout.setWidget(0, 0, lbFromCRS);
			layout.setWidget(0, 1, fromCRSEditor);
			
			
			layout.setWidget(1, 0, lbToCRS);
			layout.setWidget(1, 1, toCRSLabel);
			
			ZoomLevelsDisp zoomSpec = mapWidget.getMapComponent().getUserZooms().toDisplay(mapWidget.getMapComponent().getCoordinateAdapter().pixSizeInMicrons);
			layout.setWidget(2, 0, new BoldText(GeopediaTerms.INSTANCE.scale()+":"));
			layout.setWidget(2, 1, zoomSelectWidget = new ZoomSelectWidget(ZoomSelectWidgetType.MANUAL_ONLY, zoomSpec));
			
			zoomSelectWidget.addKeyDownHandler(new EnterKeyDownHandler() {
				@Override
				public void onEnterDown(KeyDownEvent event) {
					showPosition();
				}
			});
			
			SGPushButton btnPickLocation = new SGPushButton(GeopediaTerms.INSTANCE.pickPoint(), GeopediaLightStyle.INSTANCE.pickTarget(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					MouseHandler mHandler = mapWidget.getMouseHandler();
					if (!mHandler.isActionRegistered(pickCoordinateMouseAction)) {
						pickCoordinateMouseAction.setCursor("url('"+GWT.getModuleBaseURL()+"images/cursor/pick_target.cur'), default");
						mHandler.registerAction(pickCoordinateMouseAction, MouseHandler.BUTTON_LEFT, MouseHandler.MOD_NONE, 1);
						pnlPickPointNotificationHolder.setVisible(true);
						pnlPickPointNotificationHolder.add(new InlineLabel());
					}
				}
			});
			
			layout.setWidget(3, 1,new SGFlowPanel(btnConvert, btnShowPosition));
			
			layout.setWidget(4, 0, new BoldText(GeopediaTerms.INSTANCE.additional()+":"));
			layout.setWidget(4, 1, btnPickLocation);
			add(layout);
			
			dca.addCoordinatesListener(new CoordinatesListener() {
				@Override
				public void displaySizeChanged(int newWidthPx, int newHeightPx) { }
				
				@Override
				public void coordinatesChanged(double newX, double newY, double newScale, 
												boolean coordsChanged, boolean scaleChanged) 
				{
					if (!isMarkerPlaced()) {
						applyMapCoordinates();
					} else {
						coordsChanged = false;
					}
					
					if (coordsChanged || scaleChanged) {
						updateUI();
					}
				}
			});
			
			addStyleName("coordinatesConverter");
			applyMapCoordinates();
	    }
		
		private void showPosition() {
			double newScale = zoomSelectWidget.getScale();
			if(Double.isNaN(newScale)){
				newScale = dca.getScale();
			}
			
			dca.setWorldCenterAndScale(coordinate.x, coordinate.y, newScale);
		}
		
		private void updateUI() {
			updateFromCoordUI();
			updateToCoordUI();
			
			zoomSelectWidget.setScale(dca.getScale());
		}
		
		private void updateFromCoordUI() {
			Transform<?,?> tr = Transforms.find(dca.worldCRS, lbFromCRS.getSelected());
			Point pt = tr.point(coordinate, new Point());
			
			fromCRSEditor.setCoordinate1(pt.x);
			fromCRSEditor.setCoordinate2(pt.y);
		}
		
		private void updateToCoordUI() {
			Transform<?,?> tr = Transforms.find(dca.worldCRS, lbToCRS.getSelected());
			Point pt = tr.point(coordinate, new Point());
			
			if (lbToCRS.getSelected() instanceof LatLonCRS) {
				toCRSLabel.setText(formatLATLON.format(pt));
			} else {
				toCRSLabel.setText(formatXY.format(pt));
			}
		}
		
		private void updateEditorCRS() {
			fromCRSEditor.setCRS(lbFromCRS.getSelected());
		}
		
		private void onCoordinatePicked(double xWorld, double yWorld) {		
			mapWidget.getMouseHandler().deregisterAction(pickCoordinateMouseAction);
			pnlPickPointNotificationHolder.setVisible(false);
			applyPickedCoordinates(new Point(xWorld, yWorld));
		}
		
		private void updateFromCRSListbox(CRS toCRS) {
			CRS currentFromCrs = lbFromCRS.getSelected();
			lbFromCRS.setItems(crsSettings.getToCRSTransformCapabilities(toCRS.getDefaultIdentifier()));			
			lbFromCRS.setSelected(currentFromCrs);
			if (!Util.safeEquals(currentFromCrs, lbFromCRS.getSelected())) {
				updateEditorCRS();
			}
		}
		
		private void updateToCRSListbox(CRS fromCrs) {
			CRS currentToCrs = lbToCRS.getSelected();
			lbToCRS.setItems(crsSettings.getFromCRSTransformCapabilities(fromCrs.getDefaultIdentifier()));
			if (lbToCRS.hasItem(currentToCrs)) {
				lbToCRS.setSelected(currentToCrs);
			}
		}

		public void deactivate() {
			mapWidget.getMouseHandler().deregisterAction(pickCoordinateMouseAction);
			markersOverlay.clear();
			mapWidget.getMapComponent().removeOverlay(markersOverlay);
		}
		
		private boolean isMarkerPlaced() {
			return marker.getWorldPosition().x() != 0 && marker.getWorldPosition().y() != 0;
		}
		
		private void applySourceCoordinates() {
			if (fromCRSEditor.getCoordinate() == null) {
				return;
			}
			
			Transform<?,?> tr = Transforms.find(lbFromCRS.getSelected(), dca.worldCRS);
			tr.point(fromCRSEditor.getCoordinate(), coordinate);
			updateMarkerPosition();
			updateUI();
		}
		
		private void applyMapCoordinates() {
			coordinate.setLocation(dca.worldCenterX, dca.worldCenterY);
			updateUI();
		}
		
		private void applyPickedCoordinates(HasCoordinate pos) {
			coordinate.setLocation(pos);
			updateMarkerPosition();
			updateUI();
		}
		
		private void updateMarkerPosition() {
			marker.setLocation(coordinate);
			mapWidget.repaint();
		}

	}
	
	public CoordinateConverterSidebarTab() {
		setTabTitle(ProConstants.INSTANCE.converter());
		convPanel = new CoordinateConverterPanel(ClientGlobals.getCRSSettings());
		addContent(convPanel);
	}
	
	@Override
	protected void internalActivate() {
	}
	
	@Override
	protected boolean internalDeactivate() {
		convPanel.deactivate();
		return true;
	}
	
}
