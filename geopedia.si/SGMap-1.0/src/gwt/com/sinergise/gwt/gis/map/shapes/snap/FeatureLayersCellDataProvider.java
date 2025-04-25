package com.sinergise.gwt.gis.map.shapes.snap;

import static com.sinergise.common.gis.map.model.layer.LayerTreeElement.INHERITANCE_CHILD_OVERRIDES;
import static com.sinergise.common.gis.map.model.layer.LayerTreeElement.PROP_SNAPON;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.gis.feature.CFeatureDataSource;
import com.sinergise.common.gis.feature.CFeatureDataSource.FeatureCollectionCallback;
import com.sinergise.common.gis.feature.CFeatureDataSource.FeatureDescriptorCallback;
import com.sinergise.common.gis.feature.FeatureAccessException;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.filter.BBoxOperation;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.gis.filter.InvalidFilterDescriptorException;
import com.sinergise.common.gis.filter.NoOperation;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.FeaturesLayer;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.gis.map.model.layer.MapContextLayers;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoItem;
import com.sinergise.common.gis.map.model.layer.view.LayersView;
import com.sinergise.common.gis.map.model.layer.view.LayersViewListener;
import com.sinergise.common.gis.query.Query;
import com.sinergise.common.util.collections.CollectionUtil;
import com.sinergise.common.util.collections.tree.TreeListenerAdapter;
import com.sinergise.common.util.geom.CoordinatePair;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.gwt.gis.map.shapes.snap.GridIndexedSnapProvider.CellDataFetchedCallback;
import com.sinergise.gwt.gis.map.shapes.snap.GridIndexedSnapProvider.CellDataProvider;
import com.sinergise.gwt.gis.map.shapes.snap.GridIndexedSnapProvider.GridIndexCell;

public class FeatureLayersCellDataProvider implements CellDataProvider {
	
	private static final Logger logger = LoggerFactory.getLogger(FeatureLayersCellDataProvider.class);
	
	private final LayersView snaponLayersView;
	private final Map<CFeatureDataSource, List<FeatureDataLayer>> snaponLayersMap = new HashMap<CFeatureDataSource, List<FeatureDataLayer>>();
	private long snaponFeatureTypesHash = 0;
	
	public FeatureLayersCellDataProvider(MapContextLayers layers) {
		
		snaponLayersView = new LayersSnapOnView(layers);
		snaponLayersView.addLayersViewListener(new LayersViewListener() {
			@Override
			public void layerViewChanged() {
				updateSnaponLayers();
			}
			
			@Override
			public void layerNodeChanged(LayerTreeElement node, String propertyName) { }
		});
		
		//listen for data modification
		layers.addTreeListener(new TreeListenerAdapter<LayerTreeElement>() {
			@Override
			public void nodeChanged(LayerTreeElement node, String propertyName) {
				if (LayerTreeElement.PROP_LAST_MODIFIED.equals(propertyName)) {
					updateSnaponLayers();
				}
			}
		});
		
		updateSnaponLayers();
	}
	
	@Override
	public void fetchCellData(final Collection<GridIndexCell> cells, final CellDataFetchedCallback callback) {
		if (CollectionUtil.isNullOrEmpty(cells)) {
			callback.onCellDataFetched(new ArrayList<GridIndexCell>());
			return;
		}
		
		if (!checkSnaponLayerDescriptorsBeforeFetching(cells, callback)) {
			return; //will fetch descriptors and call back
		}
		
		FilterDescriptor filter = constructCellDataQueryFilter(cells);
		if (filter != null) {
			
			final long dataHash = snaponFeatureTypesHash;
			final List<Geometry> geomsFetched = new ArrayList<Geometry>();
			final int[] waiting = new int[]{snaponLayersMap.size()};
			
			for (CFeatureDataSource source : snaponLayersMap.keySet()) {
				try {
					
					List<FeatureDataLayer> layers = snaponLayersMap.get(source);
					Query[] queries = new Query[layers.size()];
					for (int i=0; i<layers.size(); i++) {
						FeatureDataLayer layer = layers.get(i); 
						CFeatureDescriptor desc = layer.getDescriptor();
						String geomFieldName = desc.getGeomDescriptor().getSystemName();
						
						queries[i] = new Query(layer.getFeatureTypeName(), new String[]{geomFieldName}, filter);
					}
					
					source.queryFeatures(queries, new FeatureCollectionCallback() {
						@Override
						public void onSuccess(FeatureInfoCollection features) {
							for (FeatureInfoItem item : features) {
								geomsFetched.add(item.f.getGeometry());
							}
							returned();
						}
						
						@Override
						public void onError(FeatureAccessException e) {
							logger.error(e.getMessage(), e);
							returned();
						}
						
						void returned() {
							waiting[0] = --waiting[0];
							if (waiting[0] == 0) {
								cellDataFetched(geomsFetched, cells, dataHash, callback);
							}
						}
					});
				} catch (FeatureAccessException e) {
					logger.error(e.getMessage(), e);
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	@Override
	public boolean isCellDataValid(GridIndexCell cell) {
		return cell.getDataHash() == snaponFeatureTypesHash;
	}
	
	private static void cellDataFetched(List<Geometry> cellData, Collection<GridIndexCell> cells, long dataHash, CellDataFetchedCallback callback) {
		List<HasCoordinate> nodes = new ArrayList<HasCoordinate>();
		List<CoordinatePair> edges = new ArrayList<CoordinatePair>();
		for (Geometry g : cellData) {
			GeomUtil.extractCoordinatePairs(g, edges);
			GeomUtil.extractCoordinates(g, nodes);
		}
		
		for (GridIndexCell cell : cells) {
			cell.setData(nodes, edges, dataHash);
		}
		
		callback.onCellDataFetched(cells);
	}
	
	private static FilterDescriptor constructCellDataQueryFilter(Collection<GridIndexCell> cells) {
		//TODO: optimize MBR query construction
		try {
			//don't aggregate with OR as spatial index performance will be too slow!
			EnvelopeBuilder envBld = new EnvelopeBuilder();
			for (GridIndexCell cell : cells) {
				envBld.expandToInclude(cell.getEnvelope());
			}
			
			if (!envBld.isEmpty()) {
				return new BBoxOperation(envBld.getEnvelope());
			} 
			return new NoOperation();
			
		} catch (InvalidFilterDescriptorException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void updateSnaponLayers() {
		
		//update feature types hash
		final int prime = 31;
		snaponFeatureTypesHash = 1;
		for (LayerTreeElement layer : snaponLayersView) {
			snaponFeatureTypesHash = prime * snaponFeatureTypesHash + layer.getLocalID().hashCode();
			snaponFeatureTypesHash = prime * snaponFeatureTypesHash + layer.getLastModified();
		}
		
		//update layers map
		snaponLayersMap.clear();
		for (LayerTreeElement layer : snaponLayersView) {
			FeatureDataLayer fLayer = (FeatureDataLayer)layer;
			CFeatureDataSource source = fLayer.getFeaturesSource();
			List<FeatureDataLayer> srcLayers = snaponLayersMap.get(source);
			if (srcLayers == null) {
				snaponLayersMap.put(source, srcLayers = new ArrayList<FeatureDataLayer>());
			}
			srcLayers.add(fLayer);
		}
	}
	
	private boolean checkSnaponLayerDescriptorsBeforeFetching(final Collection<GridIndexCell> cells, final CellDataFetchedCallback callback) {
		try {
			for (CFeatureDataSource source : snaponLayersMap.keySet()) {
				List<FeatureDataLayer> toFetch = new ArrayList<FeatureDataLayer>();
				for (FeatureDataLayer fLayer : snaponLayersMap.get(source)) {
					if (fLayer.getDescriptor() == null || !fLayer.getDescriptor().hasGeometry()) {
						toFetch.add(fLayer);
					}
				}
				
				final int[] waiting = new int[]{snaponLayersMap.size()};
				
				if (!toFetch.isEmpty()) {
					String[] featureTypes = new String[toFetch.size()];
					for (int i=0; i<toFetch.size(); i++) {
						featureTypes[i] = toFetch.get(i).getFeatureTypeName();
					}
					
					source.getDescriptor(featureTypes, new FeatureDescriptorCallback() {
						@Override
						public void onSuccess(CFeatureDescriptor[] result) {
							waiting[0] = --waiting[0];
							if (waiting[0] == 0) {
								fetchCellData(cells, callback);
							}
						}
						
						@Override
						public void onError(FeatureAccessException e) {
							logger.error(e.getMessage(), e);
							throw new RuntimeException(e);
						}
					});
					return false;
				} 
			}
		} catch (FeatureAccessException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return true;
	}
	
	private static class LayersSnapOnView extends LayersView {
		
		public LayersSnapOnView(MapContextLayers layersTree) {
			super(layersTree);
		}
		
		@Override
		protected boolean match(LayerTreeElement node) {
			return node instanceof FeaturesLayer
				&& node.deepOn() 
				&& StringUtil.isTruthy(node.getGenericProperty(PROP_SNAPON, INHERITANCE_CHILD_OVERRIDES), false);
		}
		
		@Override
		protected boolean ignorePropertyChange(LayerTreeElement node, String propertyName) {
			return !LayerTreeElement.PROP_ON.equals(propertyName);
		}
	}
	
}
