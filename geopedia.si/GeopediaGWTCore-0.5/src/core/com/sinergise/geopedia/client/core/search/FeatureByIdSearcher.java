package com.sinergise.geopedia.client.core.search;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.geopedia.client.core.search.SearchListener.SystemNotificationType;
import com.sinergise.geopedia.client.core.util.FeatUtil;
import com.sinergise.geopedia.client.ui.map.MapComponent;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.query.FeaturesQueryResults;

public class FeatureByIdSearcher implements Searcher{

	private int featureId;
	private int tableId;
	private long timestamp;
	
	
	private boolean setLocation = false;
	private boolean setScale = false;
	private MapComponent mapComponent = null;
	
	public FeatureByIdSearcher(int tableId, int featureId) {
		this.tableId=tableId;
		this.featureId=featureId;
		timestamp=0;
	}
	
	public void showFeature(boolean location, boolean scale, MapComponent mapComponent) {
		setLocation = location;
		setScale = scale;
		this.mapComponent = mapComponent;
	}
	
	@Override
	public void search(final SearchListener listener) {
		listener.systemNotification(SystemNotificationType.GLOBAL_SEARCH_START, null);
		FeatUtil.getFeatureById(tableId, timestamp, featureId, false, new AsyncCallback<FeaturesQueryResults>() {

			@Override
			public void onFailure(Throwable caught) {
				listener.systemNotification(SystemNotificationType.ERROR, caught.toString());				
			}

	
			@Override
			public void onSuccess(FeaturesQueryResults result) {
				if (result!=null) {
					List<Feature> featureList = result.getCollection();
					listener.searchResults(featureList.toArray(new Feature[featureList.size()]), result.table,false,false,null);
					if (featureList.size() > 0) {
						show(featureList.get(0), result.table);
					}
				}
				listener.systemNotification(SystemNotificationType.GLOBAL_SEARCH_DONE, null);
				
			}
		});
		
	}

	
	private void show(Feature f, Table t) {
		
		if (mapComponent==null)
			return;
		if (!setScale && !setLocation)
			return;
		DisplayCoordinateAdapter dca = mapComponent.getCoordinateAdapter();
		if (setScale && setLocation) {
			if(f.envelope != null && !f.envelope.isEmpty())
				mapComponent.ensureVisible(f.envelope, true, t.geomType.isPoint());
		} else if (setLocation) {
			HasCoordinate cent=null;
			if (f.centroid!=null) {
				cent = f.centroid;
			} else if (f.envelope != null) {
				cent = f.envelope.getCenter();
			}
			if (cent!=null) {
				dca.setWorldCenter(cent.x(), cent.y());
			}
		} else if (setScale) {
			if (!t.geomType.isPoint()){
              dca.setScale(dca.scaleForEnvelope(f.envelope));
			}
		}
		
		mapComponent.repaint(0);
	}
}
