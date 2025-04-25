package com.sinergise.common.gis.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.CFeatureCollection;
import com.sinergise.common.gis.feature.CFeatureDataSource;
import com.sinergise.common.gis.feature.CFeatureDataSource.FeatureCollectionCallback;
import com.sinergise.common.gis.feature.CFeatureDataSource.FeatureDescriptorCallback;
import com.sinergise.common.gis.feature.CFeatureIdentifier;
import com.sinergise.common.gis.feature.CFeatureUtils;
import com.sinergise.common.gis.feature.FeatureAccessException;
import com.sinergise.common.gis.feature.HasFeatureRepresentations;
import com.sinergise.common.gis.feature.RepresentsFeature;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.gis.filter.IdentifierOperation;
import com.sinergise.common.gis.filter.IdentifierReference;
import com.sinergise.common.gis.filter.InSelectionOperation;
import com.sinergise.common.gis.filter.Literal;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.MapContextLayers;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.util.lang.SGAsyncCallback;

public final class QueryUtil {
	
	private QueryUtil() { 
		//hide constructor
	}
	
//	public static Map<CFeatureDataSource, List<Query>> groupByDataSources(Query[] queries) {
//		return groupByDataSources(queries, ApplicationContext.getInstance().getPrimaryMap().getLayers());
//	}
	
	public static Map<CFeatureDataSource, List<Query>> groupByDataSources(Query[] queries, MapContextLayers ctxLayers) {
		
		Map<CFeatureDataSource, List<Query>> map = new HashMap<CFeatureDataSource, List<Query>>();
		
		for (Query q : queries) {
			FeatureDataLayer qLayer = ctxLayers.findByFeatureType(q.getFeatureTypeId());
			CFeatureDataSource qLayerSrc = qLayer.getFeaturesSource();
			List<Query> srcQueries = map.get(qLayerSrc);
			if (srcQueries == null) {
				map.put(qLayerSrc, srcQueries = new ArrayList<Query>());
			}
			srcQueries.add(q);
		}
		
		return map;
	}
	
//	public static Map<CFeatureDataSource, List<RepresentsFeature>> groupByDataSources(HasFeatureRepresentations features) {
//		return groupByDataSources(features, ApplicationContext.getInstance().getPrimaryMap().getLayers());
//	}
	
	public static Map<CFeatureDataSource, List<RepresentsFeature>> groupByDataSources(HasFeatureRepresentations features, MapContextLayers ctxLayers) {
		
		Map<CFeatureDataSource, List<RepresentsFeature>> map = new HashMap<CFeatureDataSource, List<RepresentsFeature>>();
		
		for (RepresentsFeature f : features.getFeatures()) {
			FeatureDataLayer qLayer = ctxLayers.findByFeatureType(f.getQualifiedID().getFeatureTypeName());
			CFeatureDataSource qLayerSrc = qLayer.getFeaturesSource();
			List<RepresentsFeature> srcFeatures = map.get(qLayerSrc);
			if (srcFeatures == null) {
				map.put(qLayerSrc, srcFeatures = new ArrayList<RepresentsFeature>());
			}
			srcFeatures.add(f);
		}
		
		return map;
	}
	
	public static void queryFeatureGeometry(final FeatureDataLayer layer, final CFeatureIdentifier featureID, final FeatureCollectionCallback callback) {
		queryFeatureGeometries(layer, new IdentifierOperation(featureID.getLocalID()), callback);
	}
	
	public static void queryFeatureGeometries(final FeatureDataLayer layer, final FilterDescriptor queryFilter, final FeatureCollectionCallback callback) {
		try {
			CFeatureDescriptor fDesc = layer.getDescriptor();
			
			if (fDesc != null && fDesc.hasGeometry()) {
				queryFeatureGeometries(layer, fDesc, queryFilter, callback);
				
			} else {
				layer.getFeaturesSource().getDescriptor(new String[]{layer.getFeatureTypeName()}, new FeatureDescriptorCallback() {
					@Override
					public void onSuccess(CFeatureDescriptor[] result) {
						queryFeatureGeometries(layer, result[0], queryFilter, callback);
					}
					
					@Override
					public void onError(FeatureAccessException error) {
						callback.onError(error);
					}
				});
			}
		} catch (FeatureAccessException error) {
			callback.onError(error);
		}
	}
	
	private static void queryFeatureGeometries(FeatureDataLayer layer, CFeatureDescriptor featureDesc, FilterDescriptor queryFilter, FeatureCollectionCallback callback) {
		String geomField = featureDesc.getGeomDescriptor().getSystemName();
		queryFeatures(layer, new String[]{geomField}, queryFilter, callback);
	}
	
	public static void queryFeatures(FeatureDataLayer layer, FilterDescriptor queryFilter, FeatureCollectionCallback callback) {
		queryFeatures(layer, null, queryFilter, callback);
	}
	
	public static void queryFeatures(FeatureDataLayer layer, String[] properties, FilterDescriptor queryFilter, FeatureCollectionCallback callback) {
		try {
			layer.getFeaturesSource().queryFeatures(new Query[]{new Query(layer.getFeatureTypeName(),  properties, queryFilter)}, callback);
		} catch (FeatureAccessException e) {
			callback.onError(e);
		}
	}
	
	public static void featchFeature(FeatureDataLayer layer, final String featureLocalId, final SGAsyncCallback<CFeature> callback) {
		featchFeatureInfos(layer, new String[]{featureLocalId}, new SGAsyncCallback<FeatureInfoCollection>() {
			
			@Override
			public void onSuccess(FeatureInfoCollection result) {
				if (result != null && result.getItemCount() > 0) {
					callback.onSuccess(result.getItem(0).f);
				} else {
					onFailure(new FeatureAccessException("No feature for ID: "+featureLocalId));
				}
			}
			
			@Override
			public void onFailure(Throwable error) {
				callback.onFailure(error);
			}
		});
	}
	
	public static void featchFeatures(FeatureDataLayer layer, final String[] featureLocalIds, final SGAsyncCallback<CFeatureCollection> callback) {
		featchFeatureInfos(layer, featureLocalIds, new SGAsyncCallback<FeatureInfoCollection>() {
			@Override
			public void onSuccess(FeatureInfoCollection result) {
				callback.onSuccess(CFeatureUtils.toFeatureList(result));
			}
			
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public static void featchFeatureInfos(FeatureDataLayer layer, final String[] featureLocalIds, final SGAsyncCallback<FeatureInfoCollection> callback) {
		
		Literal<?>[] ids = new Literal[featureLocalIds.length];
		for (int i = 0; i < featureLocalIds.length; i++) {
			ids[i] = Literal.newInstance(featureLocalIds[0]);
		}
		
		try {
			layer.getFeaturesSource().queryFeatures(new Query[]{
				new Query(layer.getFeatureTypeName(),  
				new InSelectionOperation(new IdentifierReference(), ids))}, 
				new FeatureCollectionCallback() {
					
					@Override
					public void onSuccess(FeatureInfoCollection features) {
						callback.onSuccess(features);
					}
					
					@Override
					public void onError(FeatureAccessException error) {
						callback.onFailure(error);
					}
				});
		} catch (FeatureAccessException e) {
			callback.onFailure(e);
		}
	}

}
