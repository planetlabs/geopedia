package com.sinergise.geopedia.client.components.routing;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.maps.client.geocode.DirectionQueryOptions;
import com.google.gwt.maps.client.geocode.DirectionQueryOptions.TravelMode;
import com.google.gwt.maps.client.geocode.DirectionResults;
import com.google.gwt.maps.client.geocode.Directions;
import com.google.gwt.maps.client.geocode.DirectionsCallback;
import com.google.gwt.maps.client.geocode.DirectionsPanel;
import com.google.gwt.maps.client.geocode.Distance;
import com.google.gwt.maps.client.geocode.Duration;
import com.google.gwt.maps.client.geocode.Waypoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.util.event.ActionPerformedListener;
import com.sinergise.common.util.event.ValueChangeListener;
import com.sinergise.geopedia.client.components.routing.entities.Destination;
import com.sinergise.geopedia.client.components.routing.entities.Direction;
import com.sinergise.geopedia.client.components.routing.util.LatLngUtils;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.resources.GeopediaCommonStyle;
import com.sinergise.geopedia.client.resources.routing.GeopediaRoutingStyle;
import com.sinergise.geopedia.client.ui.map.MapComponent;
import com.sinergise.geopedia.client.ui.map.MapWidget;
import com.sinergise.geopedia.client.ui.panels.ActivatableTabPanel;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.gwt.gis.map.ui.vector.MarkerOverlay;
import com.sinergise.gwt.ui.ImageAnchor;
import com.sinergise.gwt.ui.core.MouseClickAction;
import com.sinergise.gwt.ui.core.MouseHandler;
import com.sinergise.gwt.ui.maingui.DecoratedAnchor;
import com.sinergise.gwt.ui.maingui.StandardUIConstants;

public class RoutingPanel extends ActivatableTabPanel {
	
	private ArrayList<WaypointSelector> wpSelectors = new ArrayList<WaypointSelector>();

	private MapComponent mapComponent;
	private MapWidget mapWidget;
	private DirectionsOverlay overlay;
	private MarkerOverlay markersOverlay;
		
	private SelectFeatureAction selectFeatureMouseAction = null;
	private ActionPerformedListener<WaypointSelector> removeWPListener;
	
	private TravelModeSelectionPanel travelModePanel;
	private FlowPanel wpHolderPanel;
	private boolean doRouting = false;
	private FlowPanel distanceHolder = null;
	private SimplePanel textDirectionHolder;
	
	private ArrayList<Table> tablesToSearch = new ArrayList<Table>();
	
	private ValueChangeListener<Feature> waypointChangedListener;
	
	private ToggleButton btnAvoidHghw;
	
	private class SelectFeatureAction extends MouseClickAction {
		
		public SelectFeatureAction() {
			super ("RoutingSelectFeature");
		}
		
		@Override
		protected boolean mouseClicked(int x, int y) {
			return true;
		}
	}
	
	public RoutingPanel(MapWidget mapWidget) {
		GeopediaRoutingStyle.INSTANCE.geopediaRoutingStyles().ensureInjected();
		addStyleName("routing");
		setTabTitle(Messages.INSTANCE.RoutingPanel_Title());
		this.mapComponent = mapWidget.getMapComponent();
		this.mapWidget = mapWidget;
		overlay = new DirectionsOverlay(mapComponent.getCoordinateAdapter());
		markersOverlay = new MarkerOverlay(mapComponent.getCoordinateAdapter());
		
		removeWPListener = new ActionPerformedListener<WaypointSelector>() {
			
			@Override
			public void onActionPerformed(WaypointSelector value) {
				onRemoveWaypoint(value);
			}
		};
		
		
		waypointChangedListener = new ValueChangeListener<Feature>() {

			@Override
			public void valueChanged(Object sender, Feature oldValue,
					Feature newValue) {
				onWaypointsChanged();
			}
		};
		
		travelModePanel = new TravelModeSelectionPanel();
		travelModePanel.addTravelModeOption(TravelMode.DRIVING);
		travelModePanel.addTravelModeOption(TravelMode.WALKING);
		travelModePanel.addValueChangeListener(new ValueChangeListener<TravelMode>() {
			@Override
			public void valueChanged(Object sender, TravelMode oldValue, TravelMode newValue) {
				findRoute(); //refresh
			}
		});
		
		FlowPanel waypointsPanel = new FlowPanel();
		waypointsPanel.setStyleName("waypointsPanel");
		waypointsPanel.addStyleName("clearfix");
		addContent(waypointsPanel);
		
		waypointsPanel.add(travelModePanel);
		wpHolderPanel = new FlowPanel();
		wpHolderPanel.setStyleName("waypointsHolder");
		waypointsPanel.add(wpHolderPanel);
		FlowPanel btnPanel1 = new FlowPanel();
		btnPanel1.setStyleName("btnPanel");
		waypointsPanel.add(btnPanel1);
		for (int i=0;i<2;i++) {			
			addWaypointSelector(i);
		}
		
		ImageAnchor btnAddWaypoint = new ImageAnchor(Messages.INSTANCE.RoutingPanel_AddWaypoint(), com.sinergise.gwt.ui.resources.Theme.getTheme().standardIcons().plus(), new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				addWaypointSelector(wpSelectors.size());
			}
		});
		btnAddWaypoint.addStyleName("btnAddWaypoint");
		
		Anchor btnClearWaypoints = new Anchor(StandardUIConstants.STANDARD_CONSTANTS.buttonClear());
		btnClearWaypoints.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				cleanRoute();
				for (WaypointSelector wps:wpSelectors) {
					wps.reset();
				}
			}
		});
		btnClearWaypoints.addStyleName("btnClearWaypoints");
		
		btnPanel1.add(btnClearWaypoints);
		btnPanel1.add(btnAddWaypoint);
		
		
		FlowPanel btnPanel2 = new FlowPanel();
		btnPanel2.setStyleName("routeBtnPanel");
		addContent(btnPanel2);
		
		
		DecoratedAnchor btnSearch = new DecoratedAnchor(Messages.INSTANCE.RoutingPanel_Search(), GeopediaCommonStyle.INSTANCE.zoomto());
		btnSearch.addStyleName("search");
		
		btnSearch.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				findRoute();
			}
		});
		
		btnAvoidHghw = new ToggleButton(new Image(GeopediaRoutingStyle.INSTANCE.avoid()),new Image(GeopediaRoutingStyle.INSTANCE.avoidOn()));
		btnAvoidHghw.addStyleName("toggle avoidHighways");
		btnAvoidHghw.setTitle(Messages.INSTANCE.RoutingPanel_AvoidHighways());
		btnPanel2.add(btnAvoidHghw);
		btnPanel2.add(btnSearch);
		

		distanceHolder = new FlowPanel();		
		addContent(distanceHolder);

		SimplePanel googleLogoPanel = new SimplePanel();
		googleLogoPanel.setStyleName("googleLogoPanel");
		selectFeatureMouseAction = new SelectFeatureAction();
		addContent(googleLogoPanel);
		
		addContent(textDirectionHolder = new SimplePanel());
		textDirectionHolder.setStyleName("textDirections");
	}

	
	private void cleanRoute() {
		distanceHolder.clear();
		overlay.clear();
		textDirectionHolder.clear();
	}
	
	@Override
	protected void internalActivate() {
		
		mapComponent.addOverlay(overlay,100);
		mapComponent.addOverlay(markersOverlay,101);
		mapWidget.addStyleName("withRoute");
		//markersOverlay.reposition(mapComponent.getCoordinateAdapter(), true);
		markersOverlay.addStyleName("routingMarkers");
//		overlay.reposition(mapComponent.getCoordinateAdapter(), true);
		mapWidget.getMouseHandler().registerAction(selectFeatureMouseAction,MouseHandler.BUTTON_LEFT, MouseHandler.MOD_NONE, 1);
		
		/** build list of tables to search **/
		tablesToSearch.clear();
		mapWidget.getMapLayers().getTables(tablesToSearch,true);
	}
	
	@Override
	public boolean internalDeactivate() {
		mapComponent.removeOverlay(overlay);
		mapComponent.removeOverlay(markersOverlay);
		mapWidget.removeStyleName("withRoute");
		mapWidget.getMouseHandler().deregisterAction(selectFeatureMouseAction);
		return true;
	}
	
	
	private String generateSelectorID(int i) {
		char ch = 'A';
		ch+=i;
		return ""+ch;
	}
	private void onRemoveWaypoint(WaypointSelector wps) {
		wps.destroy();
		wpSelectors.remove(wps);
		wpHolderPanel.remove(wps);
		for (int i=0;i<wpSelectors.size();i++) {
			wpSelectors.get(i).updateSelectorID(generateSelectorID(i));
		}
		onWaypointsChanged();
	}
	
	private void addWaypointSelector(int id) {
		WaypointSelector wps = new WaypointSelector(generateSelectorID(id),tablesToSearch, markersOverlay);
		wps.addValueChangeListener(waypointChangedListener);
		wps.setRemoveListener(removeWPListener);
		wpHolderPanel.add(wps);
		wpSelectors.add(wps);		
	}
	
	
	
	
	private void findRoute() {
		if (wpSelectors.size()<2) { // at least two waypoints are needed
			cleanRoute();
			return;
		}
		doRouting = true;
		boolean pointsOK=true;
		int pointsCount=0;
		for (WaypointSelector wps:wpSelectors) {
			
			if (wps.updateNeeded()) {
				pointsOK=false;
				wps.doTextSearch();
			} else {
				Feature f = wps.getFeature();
				if (f!=null) pointsCount++;
			}
		}
		if (pointsOK && pointsCount>=2) {
			executeRouting();
		}
	}
	
	private void onWaypointsChanged() {
		boolean pointsOK=true;
		for (WaypointSelector wps:wpSelectors) {			
			if (wps.updateNeeded()) {
				pointsOK=false;
				break;
			}
		}
		if (pointsOK) executeRouting();
	}
	
	
	private void updateDistance(Distance distance, Duration duration) {
		distanceHolder.clear();
		FlowPanel pnlDistance = new FlowPanel();
		pnlDistance.setStyleName("distancePanel");
		distanceHolder.add(pnlDistance);
		
		Anchor btnZoomToRoute=new Anchor();
		btnZoomToRoute.setStyleName("btnZoomToRoute");
		btnZoomToRoute.setTitle(Messages.INSTANCE.RoutingPanel_ZoomToRoute());
		btnZoomToRoute.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				DisplayCoordinateAdapter dca = mapWidget.getMapComponent().getCoordinateAdapter();
				dca.setDisplayedRect(overlay.minX,overlay.minY, overlay.maxX, overlay.maxY);
				mapWidget.repaint();
			}
		});
		pnlDistance.add(btnZoomToRoute);
		
		SimplePanel distancePanel = new SimplePanel();
		distancePanel.getElement().setInnerHTML(new Image(GeopediaRoutingStyle.INSTANCE.distance())+Messages.INSTANCE.RoutingPanel_Distance()+" "+distance.inLocalizedUnits());
		
		pnlDistance.add(distancePanel);
		
		SimplePanel durationPanel = new SimplePanel();
		int durationMinutes = Math.round((duration.inSeconds()/60));
		int durationInDays = Math.round(durationMinutes/(60*24));
		durationMinutes-=(durationInDays*24*60);
		int durationInHours =Math.round(durationMinutes/60);
		durationMinutes-=(durationInHours*60);
		
		String text ="";
		if (durationInDays>0) {
			text+=durationInDays+" d ";
		}
		if (durationInHours>0) {
			text+=durationInHours+" h ";
		}
		text+=durationMinutes+" min ";
		durationPanel.getElement().setInnerHTML(new Image(GeopediaRoutingStyle.INSTANCE.time())+Messages.INSTANCE.RoutingPanel_Duration()+" "+text);
		pnlDistance.add(durationPanel);
	}
	
	private void executeRouting() {
		if (!doRouting) return;

		final ArrayList<Destination> destinations = new ArrayList<Destination>();
		for (WaypointSelector wps:wpSelectors) {
			Feature f = wps.getFeature();
			if (f!=null)
			destinations.add(Destination.newInstanceFrom(f));			
		}
		if (destinations.size()<2) {
			cleanRoute();
			doRouting=false;
			return;
		}
		
		Waypoint[] waypoints = new Waypoint[destinations.size()];
		int idx = 0;
		for(Destination dest : destinations) {
			waypoints[idx++] = new Waypoint(LatLngUtils.getLatLng(dest.getXY()));
		}
		
		DirectionsPanel dirPanel = new DirectionsPanel();
		textDirectionHolder.setWidget(dirPanel);
		final DirectionQueryOptions opts = new DirectionQueryOptions(null, dirPanel);
		opts.setLocale(LocaleInfo.getCurrentLocale().getLocaleName());
		opts.setRetrievePolyline(true);
		opts.setAvoidHighways(btnAvoidHghw.getValue().booleanValue());
		if (travelModePanel.getSelectedMode() != null) opts.setTravelMode(travelModePanel.getSelectedMode());
		
		final SimplePanel progressPanel = new SimplePanel();
		progressPanel.setStyleName("searchInProgress");
		wpHolderPanel.add(progressPanel); //we cannot add progressPanel to abstractTabPanel since it can only have 3 elements.
		
		Directions.loadFromWaypoints(
			waypoints, opts, 
			new DirectionsCallback() {
				@Override
				public void onSuccess(DirectionResults result) {
					overlay.setDirection(Direction.newInstanceFrom(result, destinations, opts));
					updateDistance(result.getDistance(),result.getDuration());
					wpHolderPanel.remove(progressPanel);
				}
				
				@Override
				public void onFailure(int statusCode) {
					wpHolderPanel.remove(progressPanel);
					Window.alert("Error: "+statusCode);
				}
		});

	}
	
}
