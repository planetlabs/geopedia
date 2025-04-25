package com.sinergise.geopedia.pro.client.ui.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.display.GraphicMeasure;
import com.sinergise.common.geometry.display.event.CoordinatesListener;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.format.NumberFormatUtil;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.RemoteServices;
import com.sinergise.geopedia.client.ui.map.MapWidget;
import com.sinergise.geopedia.client.ui.panels.ActivatableTabPanel;
import com.sinergise.geopedia.core.service.DMVServiceAsync;
import com.sinergise.geopedia.core.service.params.GetContour;
import com.sinergise.geopedia.core.service.result.GetContourResult;
import com.sinergise.geopedia.light.client.i18n.GeopediaTerms;
import com.sinergise.geopedia.light.client.i18n.LightMessages;
import com.sinergise.geopedia.light.theme.GeopediaLightStyle;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.client.i18n.ProMessages;
import com.sinergise.gwt.gis.map.ui.actions.MouseClickActionW;
import com.sinergise.gwt.gis.map.ui.vector.LineMarker;
import com.sinergise.gwt.gis.map.ui.vector.LineMarkerStyle;
import com.sinergise.gwt.gis.map.ui.vector.Marker;
import com.sinergise.gwt.gis.map.ui.vector.MarkerOverlay;
import com.sinergise.gwt.gis.map.ui.vector.VectorOverlay;
import com.sinergise.gwt.gis.map.ui.vector.signs.ImageSign;
import com.sinergise.gwt.ui.NotificationPanel;
import com.sinergise.gwt.ui.core.MouseHandler;
import com.sinergise.gwt.ui.maingui.Breaker;
import com.sinergise.gwt.ui.maingui.SimpleLoadingPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.util.html.CSS;

public class ShowContourSidebarTab extends ActivatableTabPanel {
	
	private static final Logger logger = LoggerFactory.getLogger(ShowContourSidebarTab.class);

	private static final int CONTOUR_MARKER_Z = 101;
	private static final int CONTOUR_CONTOURS_Z = 102;
	
	private static class ShowContourPanel extends FlowPanel {
		
		private final ImageSign MARKER_SIGN = new ImageSign(GWT.getModuleBaseURL() + "images/cursor/target.png", DimI.create(37, 40));
		private final Marker marker = new Marker(MARKER_SIGN, new Point(0, 0));
		
		private final MapWidget 				mapWidget;
		private final DisplayCoordinateAdapter	dca;
		
		private final Point coordinate = new Point();
		
		private VectorOverlay		contourOverlay;
		private MarkerOverlay 		markersOverlay;
		private MouseClickActionW 	pickCoordinateMouseAction;
		
		private NotificationPanel 	pnlPickPointNotificationHolder;
		private SimpleLoadingPanel 	loadingPanel;
		private Label 				pointHeight;
		
		
		public ShowContourPanel(){
			mapWidget = ClientGlobals.mainMapWidget;
			dca = mapWidget.getMapComponent().getCoordinateAdapter();
			
			pickCoordinateMouseAction = new MouseClickActionW(dca, "Pick coordinate action") {
				protected boolean mouseClickedW(double xWorld, double yWorld) {
					onCoordinatePicked(xWorld, yWorld);
					return true;
				}
			};
			
			contourOverlay = new VectorOverlay(dca);
			markersOverlay = new MarkerOverlay(mapWidget.getMapComponent().getCoordinateAdapter());
			markersOverlay.addPoint(marker);

			mapWidget.getMapComponent().addOverlay(contourOverlay, CONTOUR_CONTOURS_Z);
			mapWidget.getMapComponent().addOverlay(markersOverlay, CONTOUR_MARKER_Z);
			
			loadingPanel = new SimpleLoadingPanel(this);
			
			add(new Breaker(10));
			
			pnlPickPointNotificationHolder = new NotificationPanel(LightMessages.INSTANCE.coordinatePickPoint(), MessageType.INFO);
			
			SGPushButton btnPickLocation = new SGPushButton(GeopediaTerms.INSTANCE.pickPoint(), GeopediaLightStyle.INSTANCE.pickTarget(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					pickLocation();
				}

			});
			
			SimplePanel sp = new SimplePanel(btnPickLocation);
			CSS.textAlign(sp.getElement(), "center");
			sp.setWidth("100%");
			add(sp);
			
			pnlPickPointNotificationHolder.setVisible(false);
			add(pnlPickPointNotificationHolder);
			
			add(pointHeight = new Label(ProConstants.INSTANCE.contourLineHeight() + ":"));
			
			add(new NotificationPanel(ProMessages.INSTANCE.contourInFixedEnvelope(), MessageType.INFO));
			
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
				}
			});
			
			pickLocation();
		}
		
		private void pickLocation() {
			MouseHandler mHandler = mapWidget.getMouseHandler();
			if (!mHandler.isActionRegistered(pickCoordinateMouseAction)) {
				pickCoordinateMouseAction.setCursor("url('"+GWT.getModuleBaseURL()+"images/cursor/pick_target.cur'), default");
				mHandler.registerAction(pickCoordinateMouseAction, MouseHandler.BUTTON_LEFT, MouseHandler.MOD_NONE, 1);
				pnlPickPointNotificationHolder.setVisible(true);
				pnlPickPointNotificationHolder.add(new InlineLabel());
			}
		}
		
		private void onCoordinatePicked(double xWorld, double yWorld) {		
			mapWidget.getMouseHandler().deregisterAction(pickCoordinateMouseAction);
			pnlPickPointNotificationHolder.setVisible(false);
			applyPickedCoordinates(new Point(xWorld, yWorld));
		}
		
		
		private void applyPickedCoordinates(HasCoordinate pos) {
			coordinate.setLocation(pos);
			updateMarkerPosition();
			fetchHeight();
		}
		
		private void fetchHeight() {
			loadingPanel.showLoading();
			DMVServiceAsync dmvService = RemoteServices.getDMVServiceInstance();
			dmvService.getContour(new GetContour(coordinate, Envelope.withCenter(coordinate.x(), coordinate.y(), 7500, 7500)),  new AsyncCallback<GetContourResult>() {

				@Override
				public void onSuccess(GetContourResult result) {
					loadingPanel.hideLoading();
					
					pointHeight.setText(ProConstants.INSTANCE.contourLineHeight() + ": "+NumberFormatUtil.create("0.00").format(result.pointHeight));
					
					contourOverlay.clear();
					String color = "rgb(15,15,185)";
					int displayThickness = 2;

					try {
						drawMultiLineString(color, displayThickness, result.contour);
					} catch(Exception e){
						onFailure(e);
					}
					
					Envelope env = result.contour.getEnvelope();
					if(!env.isEmpty()){
						String colorEnv = "rgb(185,15,15)";
						int displayThicknessEnv = 1;
	
						LineString ls = new LineString(new double[] { env.getMinX(), env.getMaxY(), env.getMinX(), env.getMinY(), env.getMaxX(), env.getMinY(), env.getMaxX(), env.getMaxY(), env.getMinX(), env.getMaxY()});
						try {
							drawLineString(colorEnv, displayThicknessEnv, ls);
						} catch(Exception e){
							onFailure(e);
						}
						
						mapWidget.getMapComponent().ensureVisible(ls.getEnvelope(), true, false);
						mapWidget.getMapComponent().repaint(0);
					}
				}

				@Override
				public void onFailure(Throwable caught) {
					loadingPanel.hideLoading();
					logger.error("Got error from getContour:" + caught.getMessage());
					Window.alert("Could not retrieve contour for " + coordinate);	
				}

			});
			
		}
		
		private void updateMarkerPosition() {
			marker.setLocation(coordinate);
			mapWidget.repaint();
		}
		
		public void deactivate() {
			mapWidget.getMouseHandler().deregisterAction(pickCoordinateMouseAction);
			
			contourOverlay.clear();
			markersOverlay.clear();
			
			mapWidget.getMapComponent().removeOverlay(contourOverlay);
			mapWidget.getMapComponent().removeOverlay(markersOverlay);
		}
		
		private boolean isMarkerPlaced() {
			return marker.getWorldPosition().x() != 0 && marker.getWorldPosition().y() != 0;
		}
		
		private void applyMapCoordinates() {
			coordinate.setLocation(dca.worldCenterX, dca.worldCenterY);
		}

		
		protected void drawLineString(String color, double displayThickness, LineString lineString) throws Exception {
			if(lineString.isEmpty()){
				return;
			}
			LineMarkerStyle style = new LineMarkerStyle(color, GraphicMeasure.fixedDisplaySize(displayThickness));
			int nPoints = lineString.getNumCoords();
			LineMarker[] lines = new LineMarker[nPoints-1];
			for(int i=0;i<nPoints-1;i++){
				lines[i] = new LineMarker(
						new Point(lineString.getX(i), lineString.getY(i)),
						new Point(lineString.getX(i+1), lineString.getY(i+1)),
						style);
			}
			contourOverlay.addLines(lines);
		}
		
		protected void drawMultiLineString(String color, double displayThickness, MultiLineString multiLineString) throws Exception {
			if(multiLineString.isEmpty()){
				return;
			}
			for(LineString lineString:multiLineString){
				drawLineString(color, displayThickness, lineString);
			}
		}
		
	}
	
	ShowContourPanel contourPanel;
	
	public ShowContourSidebarTab() {
		setTabTitle(ProConstants.INSTANCE.Show_contours());
		addContent(contourPanel = new ShowContourPanel());
	}
	
	@Override
	protected void internalActivate() {
		
	}
	
	@Override
	protected boolean internalDeactivate() {
		contourPanel.deactivate();
		return true;
	}
}
