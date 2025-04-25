package com.sinergise.gwt.gis.query;

import static com.sinergise.common.gis.filter.FilterCapabilities.SCALAR_OP_COMP_LIKE;
import static com.sinergise.common.gis.filter.FilterCapabilities.SCALAR_OP_LOGICAL_OR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sinergise.common.gis.feature.CFeatureDataSource;
import com.sinergise.common.gis.feature.CFeatureDataSource.FeatureCollectionCallback;
import com.sinergise.common.gis.feature.CFeatureDataSource.FeatureDescriptorCallback;
import com.sinergise.common.gis.feature.CFeatureUtils;
import com.sinergise.common.gis.feature.FeatureAccessException;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.filter.ComparisonOperation;
import com.sinergise.common.gis.filter.ExpressionDescriptor;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.gis.filter.IdentifierOperation;
import com.sinergise.common.gis.filter.InvalidFilterDescriptorException;
import com.sinergise.common.gis.filter.Literal;
import com.sinergise.common.gis.filter.LogicalOperation;
import com.sinergise.common.gis.filter.PropertyName;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.MapContextLayers;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoItem;
import com.sinergise.common.gis.map.model.layer.info.FeatureItemCollector;
import com.sinergise.common.gis.query.Query;
import com.sinergise.common.util.property.TextProperty;
import com.sinergise.gwt.ui.controls.QuickSearchWidget;

/**
 * @author tcerovski
 *
 */
public class GazetteerQueryWidget extends QuickSearchWidget {

	public static final FilterCapabilities MINIMAL_REQUIRED_FILTER_CAPABILIITES = new FilterCapabilities(SCALAR_OP_COMP_LIKE);

	private MapContextLayers mapLayers;
	private FeatureDataLayer gazetteerLayer;
	FeatureItemCollector collector;
	
	/** Fetch specific features for gazetteer results */
	boolean fetchFeatures = true;
	private int maxResultsLimit = Integer.MIN_VALUE;
	
	public GazetteerQueryWidget(MapContextLayers mapLayers, FeatureDataLayer gazetteerLayer, FeatureItemCollector collector) {
		this("", mapLayers, gazetteerLayer, collector);
	}
	
	public GazetteerQueryWidget(String emptyText, MapContextLayers mapLayers, 
			FeatureDataLayer gazetteerLayer, FeatureItemCollector collector) 
	{
		super(emptyText);
		
		this.gazetteerLayer = gazetteerLayer;
		this.collector = collector;
		this.mapLayers = mapLayers;
		
		if(!gazetteerLayer.isFeatureDataQueryEnabled(MINIMAL_REQUIRED_FILTER_CAPABILIITES)) {
			throw new IllegalArgumentException("Provided gazetteerLayer does not support minimal required feature query capabilities");
		}
	}
	
	public void setFetchFeatures(boolean fetch) {
		this.fetchFeatures = fetch;
	}
	
	protected String handleWildCard(String query) {
		query = query.replaceAll("\\?", "%");
		query = query.replaceAll("\\*", "%");
		return query;
	}
	
	@Override
	public void doSearch(String query) {
		if(query == null || query.trim().length() == 0) {
			return;
		}
		query =  handleWildCard(query);
		
		CFeatureDataSource src = gazetteerLayer.getFeaturesSource();
		try {
			Query q = buildQuery(query);
			q.setMaxResults(maxResultsLimit);
			src.queryFeatures(new Query[]{q}, new FeatureCollectionCallback() {
				
				@Override
				public void onSuccess(FeatureInfoCollection features) {
					if(fetchFeatures && features.getItemCount() > 0) {
						fetchFeatures(features);
					} else {
						collector.addAll(features);
					}
				}
				
				@Override
				public void onError(FeatureAccessException error) {
					handleException(error);
				}
			});
		} catch (Exception e) {
			handleException(e);
		}
		
	}
	
	@Override
	protected void handleException(Exception e) {
		super.handleException(e);
	}
	
	protected Query buildQuery(String queryString) throws InvalidFilterDescriptorException {
		FilterDescriptor filter = new ComparisonOperation(
				new PropertyName("geographicIdentifier"), 
				FilterCapabilities.SCALAR_OP_COMP_LIKE, 
				Literal.newInstance(new TextProperty(queryString))
		);
		
		return new Query(gazetteerLayer.getFeatureTypeName(), filter);
	}
	
	protected void fetchFeatures(FeatureInfoCollection gazResults) {
		
		try {
			//extract feature types and IDs
			Map<String, List<IdentifierOperation>> idOpsMap = new HashMap<String, List<IdentifierOperation>>();
			for(int i=0; i<gazResults.getItemCount(); i++) {
				FeatureInfoItem item = gazResults.getItem(i);
				String type = String.valueOf(item.getValue("locationType/name"));
				List<IdentifierOperation> idList = idOpsMap.get(type);
				if(idList == null) {
					idOpsMap.put(type, idList = new ArrayList<IdentifierOperation>());
				}
				idList.add(new IdentifierOperation(item.getLocalID()));
			}
			
			//check descriptors
			if(!descriptorsFetched) {
				checkDescriptorsAndFetchFeatures(idOpsMap);
			} else {
				doFetchFeaturesForIds(idOpsMap);
			}
			
			
		} catch (InvalidFilterDescriptorException e) {
			handleException(e);
		}
	}
	
	void doFetchFeaturesForIds(Map<String, List<IdentifierOperation>> idOpsMap) {
		try {
			//construct queries
			Query[] queries = new Query[idOpsMap.keySet().size()];
			int i=0;
			for(String type : idOpsMap.keySet()) {
				CFeatureDescriptor fd = mapLayers.findByFeatureType(type).getDescriptor();
				List<IdentifierOperation> ids = idOpsMap.get(type);
				if(ids.isEmpty()) {
					continue;
				}
				
				FilterDescriptor filter = ids.size() == 1 ? ids.get(0) :
					new LogicalOperation(ids.toArray(new ExpressionDescriptor[ids.size()]), SCALAR_OP_LOGICAL_OR);
				
				queries[i++] = new Query(type, fd != null ? CFeatureUtils.getPropertyNamesForQuery(fd) : null, filter);
			}
			
			//get features
			CFeatureDataSource src = gazetteerLayer.getFeaturesSource();
			src.queryFeatures(queries, new FeatureCollectionCallback() {
				
				@Override
				public void onSuccess(FeatureInfoCollection features) {
					collector.addAll(features);
				}
				
				@Override
				public void onError(FeatureAccessException error) {
					handleException(error);
				}
			});
		} catch (Exception e) {
			handleException(e);
		}
	}
	
	boolean descriptorsFetched = false;
	
	private void checkDescriptorsAndFetchFeatures(final Map<String, List<IdentifierOperation>> idOpsMap) {
		List<String> missingDescriptors = new ArrayList<String>();
		try {
			for(String type : idOpsMap.keySet()) {
				FeatureDataLayer layer = mapLayers.findByFeatureType(type);
				if(layer == null || layer.getDescriptor() == null) {
					missingDescriptors.add(type);
				}
			}
			
			if(missingDescriptors.isEmpty()) {
				descriptorsFetched = true;
				doFetchFeaturesForIds(idOpsMap);
			} else {
				
				gazetteerLayer.getFeaturesSource().getDescriptor(missingDescriptors.toArray(
					new String[missingDescriptors.size()]), 
					new FeatureDescriptorCallback() {
						@Override
						public void onSuccess(CFeatureDescriptor[] result) {
							descriptorsFetched = true;
							//descriptors stored not get features
							doFetchFeaturesForIds(idOpsMap);
						}
						
						@Override
						public void onError(FeatureAccessException error) {
							handleException(error);
						}
				});
				
			}
			
		} catch (FeatureAccessException e) {
			handleException(e);
		}
	}
	
	public void setMaxResultsLimit(int limit) {
		maxResultsLimit = limit;
	}
	
}
