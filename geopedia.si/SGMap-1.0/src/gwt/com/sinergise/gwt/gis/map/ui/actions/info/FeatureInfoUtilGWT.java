package com.sinergise.gwt.gis.map.ui.actions.info;

import java.util.Collection;
import java.util.HashSet;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.geometry.display.ScaleLevelsSpec;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.CFeatureDataSource;
import com.sinergise.common.gis.feature.CFeatureDataSource.FeatureCollectionCallback;
import com.sinergise.common.gis.feature.CFeatureDataSource.FeatureDescriptorCallback;
import com.sinergise.common.gis.feature.CFeatureUtils;
import com.sinergise.common.gis.feature.FeatureAccessException;
import com.sinergise.common.gis.feature.RepresentsFeature;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.gis.query.Query;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.gwt.gis.map.ui.MapComponent;

public class FeatureInfoUtilGWT {
	private FeatureInfoUtilGWT() {}

	public static void zoomToFeatures(MapComponent map, Collection<CFeature> features) {
		Envelope mbr = CFeatureUtils.getMBR(features);
		if (!mbr.isEmpty()) {
			map.setDisplayedRect(mbr.getMinX(), mbr.getMinY(), mbr.getMaxX(), mbr.getMaxY());
			map.repaint(100);
		}
	}

	public static void zoomToFeature(MapComponent map, CFeature feature) {
		Envelope mbr = feature.getEnvelope();
		if (mbr != null && !mbr.isEmpty()) {
			map.setDisplayedRect(mbr.getMinX(), mbr.getMinY(), mbr.getMaxX(), mbr.getMaxY());
			map.repaint(100);
		}
	}

	public static Envelope ensureVisibleFeatures(MapComponent map, Collection<CFeature> features, double maxScaleFactor) {
		Envelope ret = CFeatureUtils.getMBR(features);
		ensureVisible(ret.expandedForSizeRatio(0.1), map, maxScaleFactor);
		return ret;
	}

	public static void ensureVisible(Envelope area, MapComponent map, double maxScaleFactor) {
		if (area == null || area.isEmpty()) {
			return;
		}
		ScaleLevelsSpec scaleSpec = map.getUserZooms();
		map.ensureDisplay(area, maxScaleFactor, scaleSpec.scale(scaleSpec.getMaxLevelId(), map.coords.pixSizeInMicrons), 0);
		map.repaint(100);
	}

	public static void ensureVisibleFeatures(final MapComponent map, Collection<? extends RepresentsFeature> featureIDs, final double maxScaleFactor, final AsyncCallback<? super Envelope> cb) {
		getMBR(map, featureIDs, new AsyncCallback<Envelope>() {
			@Override
			public void onSuccess(Envelope result) {
				try {
					if (result == null) result = Envelope.getEmpty();
					if (!result.isEmpty()) {
						ensureVisible(result.expandedForSizeRatio(0.1), map, maxScaleFactor);
					}
					cb.onSuccess(result);
				} catch(Exception e) {
					cb.onFailure(e);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				cb.onFailure(caught);
			}
		});
	}
	
	public static void fetchMissingDescriptors(final MapComponent map, Iterable<? extends RepresentsFeature> featureIds, final AsyncCallback<Void> cb) {
		HashSet<String> missing = new HashSet<String>();
		CFeatureDataSource dataSource = null;
		for (RepresentsFeature f : featureIds) {
			String fType = f.getQualifiedID().getFeatureTypeName();
			FeatureDataLayer lyr = map.getLayers().findByFeatureType(fType);
			if (lyr.getDescriptor() != null) {
				continue;
			}
			if (dataSource != lyr.getFeaturesSource()) {
				if (dataSource != null) {
					throw new UnsupportedOperationException("Fetching descriptors from different sources currently not supported");
				}
				dataSource = lyr.getFeaturesSource();
			}
			missing.add(fType);
		}
		if (missing.isEmpty()) {
			cb.onSuccess(null);
		} else {
			if (dataSource == null) {
				throw new RuntimeException("No data source found for provided feature IDs");
			}
			try {
				dataSource.getDescriptor(missing.toArray(new String[missing.size()]), 
					new FeatureDescriptorCallback() {
						@Override
						public void onSuccess(CFeatureDescriptor[] result) {
							cb.onSuccess(null);
						}
						@Override
						public void onError(FeatureAccessException error) {
							cb.onFailure(error);
						}
				});
			} catch(FeatureAccessException e) {
				cb.onFailure(e);
			}
		}
	}

	public static void getMBR(final MapComponent map, final Collection<? extends RepresentsFeature> featureIDs, final AsyncCallback<Envelope> asyncCallback) {
		if (featureIDs.isEmpty()) {
			asyncCallback.onSuccess(new Envelope());
		}
		fetchMissingDescriptors(map, featureIDs, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				getMBRNoCheck(map, featureIDs, asyncCallback);
			}
			@Override
			public void onFailure(Throwable caught) {
				asyncCallback.onFailure(caught);
			}
		});
	}
	
	protected static void getMBRNoCheck(final MapComponent map, final Collection<? extends RepresentsFeature> featureIDs, final AsyncCallback<Envelope> asyncCallback) {
		try {
			CFeatureDataSource dataSource = null;
			Query[] qrys = CFeatureUtils.createQueriesForFeatures(featureIDs);
			for (Query qry : qrys) {
				FeatureDataLayer lyr = map.getLayers().findByFeatureType(qry.getFeatureTypeId());
				CFeatureDescriptor desc = lyr.getDescriptor();
				if (dataSource != lyr.getFeaturesSource()) {
					if (dataSource != null) {
						throw new UnsupportedOperationException("Fetching MBRs from different sources currently not supported");
					}
					dataSource = lyr.getFeaturesSource();
				}
				qry.setProperties(new String[]{desc.getIdDescriptor().getSystemName(), desc.getGeomDescriptor().getSystemName()});
			}
			if (dataSource == null) {
				throw new RuntimeException("No data source found for provided feature IDs");
			}
			dataSource.queryFeatures(qrys, new FeatureCollectionCallback() {
				@Override
				public void onError(FeatureAccessException error) {
					asyncCallback.onFailure(error);
				}
				
				@Override
				public void onSuccess(FeatureInfoCollection features) {
					Envelope ret = null;
					try {
						ret = CFeatureUtils.getMBR(features);
					} catch (Exception e) {
						asyncCallback.onFailure(e);
					}
					asyncCallback.onSuccess(ret);
				}
			});
		} catch(Throwable t) {
			asyncCallback.onFailure(t);
		}
	}
}
