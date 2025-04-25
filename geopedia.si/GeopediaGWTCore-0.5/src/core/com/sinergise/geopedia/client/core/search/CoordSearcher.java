package com.sinergise.geopedia.client.core.search;

import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;
import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.util.CoordStringUtil;
import com.sinergise.common.util.property.Property;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.events.FeatureInfoEvent;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.core.search.SearchListener.SystemNotificationType;
import com.sinergise.geopedia.client.ui.map.MapComponent;
import com.sinergise.geopedia.client.ui.map.MapWidget;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.GeomType;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.gwt.gis.map.ui.vector.Marker;
import com.sinergise.gwt.gis.map.ui.vector.MarkerOverlay;
import com.sinergise.gwt.gis.map.ui.vector.signs.ImageSign;

public class CoordSearcher implements Searcher {
	
	private static final RegExp isXY = RegExp.compile("(\\d+[,\\.]?\\d*)([ \\t]+)(\\d+[,\\.]?\\d*)");

	public static final int COORD_Z_OVERLAY = 102;
	private static MarkerOverlay markerOverlay;

	private static final ImageSign MARKER_SIGN = new ImageSign(GWT.getModuleBaseURL() + "images/cursor/target.png", DimI.create(37, 40));
	private static final Marker marker = new Marker(MARKER_SIGN, new Point(0, 0));

	public static final int COORD_SEARCH_FEATURE_ID = 1;
	
	static {
		FeatureInfoEvent.register(ClientGlobals.eventBus, new FeatureInfoEvent.Handler() {
			
			@Override
			public void onFeatureInfo(FeatureInfoEvent event) {
				MapWidget map = ClientGlobals.mainMapWidget;
				Feature feat = event.getFeature();
				if(isFeatureFromSearcher(feat)){
					if(event.hasHighlight()){
						if(markerOverlay == null){
							map.getMapComponent().addOverlay(
									markerOverlay = new MarkerOverlay(map.getMapComponent().getCoordinateAdapter()), 
									COORD_Z_OVERLAY);
						}
						Point coordP = (Point) feat.featureGeometry;
						
						marker.setLocation(coordP);
						markerOverlay.addPoint(marker);
					} else {
						marker.removeFromParent();
					}
					if(event.hasZoomTo()){
						map.getMapComponent().ensureVisible(feat.envelope, true, feat.getGeometryType().isPoint());
					}
					map.repaint();
				}
			}
			
		});
	}
	
	Point point;
	MapComponent map;
	
	private CoordSearcher(Point point, MapComponent map) {
		super();
		this.point = point;
		this.map = map;
	}


	@Override
	public void search(SearchListener listener) {
		listener.systemNotification(SystemNotificationType.GLOBAL_SEARCH_START, null);

		if(!map.getCoordinateAdapter().bounds.mbr.contains(point.getEnvelope())){
			listener.systemNotification(SystemNotificationType.GLOBAL_SEARCH_DONE, null);
			return;
		}
		
		Feature f = new Feature();
		f.geomType = GeomType.POINTS;
		f.setGeometry(point);
		f.setId(COORD_SEARCH_FEATURE_ID);
		f.tableId = COORD_SEARCH_FEATURE_ID;
		f.fields = new Field[]{};
		f.properties = new Property[]{};
		f.repText = Messages.INSTANCE.coordSearched();
		
		
		listener.searchResults(new Feature[] { f }, new Table(), false, false, null);
		map.ensureVisible(new Envelope(point.x, point.y(), point.x(), point.y()), false, true);
		
		listener.systemNotification(SystemNotificationType.GLOBAL_SEARCH_DONE, null);
	}

	public static CoordSearcher createSearcher(String query, MapComponent map){
		
		Point p = CoordStringUtil.parseGpsCoords(query);
		if(p != null && isXY.exec(query) == null){//if its not xy then it was given with WGS84
			p = ClientGlobals.crsSettings.getTransform(
					CRS.WGS84.getDefaultIdentifier(), 
					ClientGlobals.crsSettings.getMainCrsId(), false).point(p, p);
		}
		
		return p == null ? null : new CoordSearcher(p, map);
	}
	
	public static boolean isFeatureFromSearcher(Feature f){
		return f != null && f.getId() == COORD_SEARCH_FEATURE_ID && f.getTableId() == COORD_SEARCH_FEATURE_ID;
	}
}
