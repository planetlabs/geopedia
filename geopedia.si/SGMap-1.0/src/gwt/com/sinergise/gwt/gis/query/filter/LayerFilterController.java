package com.sinergise.gwt.gis.query.filter;

import static com.sinergise.common.gis.map.model.layer.LayerTreeElement.INHERITANCE_NONE;
import static com.sinergise.common.util.collections.CollectionUtil.isNullOrEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.gis.feature.CFeatureDataSource.FeatureDescriptorCallback;
import com.sinergise.common.gis.feature.FeatureAccessException;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.filter.InvalidFilterDescriptorException;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.util.collections.tree.TreeVisitor;
import com.sinergise.common.util.event.status.DummyStatusListener;
import com.sinergise.common.util.event.status.StatusListener;
import com.sinergise.common.util.lang.SGAsyncCallback;
import com.sinergise.gwt.gis.i18n.Messages;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.query.struct.cond.QueryConditionFactory;

public class LayerFilterController {
	
	public static final String PROP_FILTERABLE = "filterable";
	public static final String PROP_DEFAULT_FILTER = "defaultFilter";
	
	public static final FilterCapabilities MINIMAL_FILTER_CAPS 
	 		= new FilterCapabilities(FilterCapabilities.SCALAR_OP_COMP_EQUALTO);

	private final Logger logger = LoggerFactory.getLogger(LayerFilterController.class);
	
	private final MapComponent map;
	private QueryConditionFactory conditionFactory = new QueryConditionFactory();
	
	protected final Map<String, FeatureDataLayer> filterLayers = new LinkedHashMap<String, FeatureDataLayer>();
	private final Map<String, Map<String, String>> currentFilters = new HashMap<String, Map<String,String>>(); //filter model
	private final Map<String, LayerFilterBuilder> layerFilterBuilders = new HashMap<String, LayerFilterBuilder>();
	
	private List<LayerFilterListener> listeners = new ArrayList<LayerFilterListener>();
	private StatusListener statusListener = new DummyStatusListener();
	
	public LayerFilterController(MapComponent map) {
		this.map = map;
		
		final Collection<FeatureDataLayer> layers = new ArrayList<FeatureDataLayer>();
		map.getLayers().traverseDepthFirst(new TreeVisitor<LayerTreeElement>() {
			@Override
			public boolean visit(LayerTreeElement node) {
				if ("TRUE".equalsIgnoreCase(node.getGenericProperty(PROP_FILTERABLE, INHERITANCE_NONE))
					&& node instanceof FeatureDataLayer && node instanceof Layer
					&& ((FeatureDataLayer)node).getFeaturesSource() instanceof SupportsLayerFiltering
					&& ((FeatureDataLayer)node).isFeatureDataQueryEnabled(MINIMAL_FILTER_CAPS)) 
				{
					layers.add((FeatureDataLayer)node);
				}
				return true;
			}
		});
		
		setFilterableLayers(layers);
	}
	
	public LayerFilterController(MapComponent map, Iterable<? extends FeatureDataLayer> filterLayers) {
		this.map = map;
		setFilterableLayers(filterLayers);
	}
	
	protected void setFilterableLayers(Iterable<? extends FeatureDataLayer> layers) {
		filterLayers.clear();
		layerFilterBuilders.clear();
		for (FeatureDataLayer layer : layers) {
			String layerName = layer.getLocalID();
			filterLayers.put(layerName, layer);
			setLayerFilter(layerName, null); //force setting of default filter
		}
	}
	
	public void addLayerFilterListener(LayerFilterListener listener) {
		listeners.add(listener);
	}
	
	public void removeLayerFilterListener(LayerFilterListener listener) {
		listeners.remove(listener);
	}
	
	public void bindWithHistory() {
		LayerFilterHistoryHandler.bind(this);
	}
	
	public Collection<FeatureDataLayer> getFilterableLayers() {
		return filterLayers.values();
	}
	
	public Map<String, String> getDefaultLayerFilter(String layerName) {
		final Layer layer = (Layer)filterLayers.get(layerName);
		if (layer == null) return null;
		
		return LayerFilterUtil.toLayerFilterValuesMap(
				layer.getGenericProperty(PROP_DEFAULT_FILTER, INHERITANCE_NONE));
	}
	
	public void resetAllFilters() {
		for (String layerName : filterLayers.keySet()) {
			resetFilter(layerName);
		}
	}
	
	public void resetFilter(final String layerName) {
		doSetLayerFilter(layerName, getDefaultLayerFilter(layerName));
	}
	
	public void setLayerFilter(String layerName, Map<String, String> valuesMap) {
		if (valuesMap != null && valuesMap.equals(currentFilters.get(layerName))) return; //nothing changed
		if (isNullOrEmpty(valuesMap)) { //set to default in empty values
			resetFilter(layerName);
			return;
		}
		doSetLayerFilter(layerName, valuesMap);
	}
	
	private void doSetLayerFilter(final String layerName, final Map<String, String> valuesMap) {
		final FeatureDataLayer layer = filterLayers.get(layerName);
		if (layer == null) {
			return;
		}
		
		//set current filter (do this before it is actually set in case this method will be called before filter is set)
		currentFilters.put(layerName, valuesMap);
		
		getLayerFilterBuilder(layer, new AsyncCallback<LayerFilterBuilder>() {
			@Override
			public void onSuccess(LayerFilterBuilder builder) {
				//set values on builder and get proper FilterDescriptor
				builder.setFilterFieldValues(valuesMap, true);
				doSetLayerFilter(layer, builder);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				currentFilters.remove(layerName); //remove current filter as it was not set
				handleError(caught.getMessage(), caught);
			}
		});
	}
	
	private void doSetLayerFilter(final FeatureDataLayer layer, final LayerFilterBuilder filterBuilder) {
		
		statusListener.clearStatus();
		SupportsLayerFiltering source = (SupportsLayerFiltering)layer.getFeaturesSource();
		
		try {
			source.setLayerFilter(layer.getFeatureTypeName(), filterBuilder.buildFilter(), new SGAsyncCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					if (layer instanceof Layer) ((Layer)layer).setDirty();
//					map.getCoordinateAdapter().pixPan(1, 0); //force repaint, should be done more nicely
					map.repaint(200);
					
					//notify listeners
					for (LayerFilterListener listener : listeners) {
						listener.layerFilterSet(layer.getFeatureTypeName(), filterBuilder.getFilterFieldValues());
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					currentFilters.remove(layer.getFeatureTypeName());
					handleError("Failed to set layer filter: "+caught.getMessage(), caught);
				}
			});
		} catch(InvalidFilterDescriptorException e) {
			currentFilters.remove(layer.getFeatureTypeName());
			handleError("Failed to set layer filter: "+e.getMessage(), e);
		}
	}
	
	public void setFilterConditionFactory(QueryConditionFactory factory) {
		this.conditionFactory = factory;
	}
	
	public void getLayerFilterBuilder(final FeatureDataLayer layer, final AsyncCallback<LayerFilterBuilder> cb) {
		
		LayerFilterBuilder builder = layerFilterBuilders.get(layer.getFeatureTypeName());
		
		if (builder == null) {
			if (layer.getDescriptor() != null) {
				_getLayerFilterBuilder(layer, layer.getDescriptor(), cb);
			}
			
			try {
				layer.getFeaturesSource().getDescriptor(new String[] { layer.getFeatureTypeName() }, new FeatureDescriptorCallback() {
					
					@Override
					public void onSuccess(CFeatureDescriptor[] fDesc) {
						//check if already created in previous call
						LayerFilterBuilder b = layerFilterBuilders.get(layer.getFeatureTypeName());
						if (b != null) {
							cb.onSuccess(b);
							return;
						}
						
						if(fDesc != null &&  fDesc.length > 0) {
							_getLayerFilterBuilder(layer, fDesc[0], cb);
						} else {
							cb.onFailure(new RuntimeException(
								Messages.INSTANCE.structuredQueryBuilder_noDescriptor(), null));
						}
					}
					
					@Override
					public void onError(FeatureAccessException error) {
						cb.onFailure(new RuntimeException(
							Messages.INSTANCE.structuredQueryBuilder_errorLoadingDescriptor(error.getMessage()), error));
					}
					
				});
			} catch(FeatureAccessException e) {
				cb.onFailure(e);
			} 
		} else {
			cb.onSuccess(builder);
		}
	}
	
	private void _getLayerFilterBuilder(FeatureDataLayer layer, CFeatureDescriptor fDesc, final AsyncCallback<LayerFilterBuilder> cb) {
		LayerFilterBuilder b = createLayerFilterBuilder(layer, fDesc);
		layerFilterBuilders.put(layer.getFeatureTypeName(), b);
		cb.onSuccess(b);
	}
	
	protected LayerFilterBuilder createLayerFilterBuilder(FeatureDataLayer layer, CFeatureDescriptor fDesc) {
		return new LayerFilterBuilder(fDesc, layer.getFilterCapabilities(), conditionFactory);
	}
	
	public boolean hasStatusListener() {
		return !(statusListener instanceof DummyStatusListener || statusListener == null);
	}
	
	public void setStatusListener(StatusListener statusListener) {
		if (statusListener == null) statusListener = new DummyStatusListener(); //to avoid NPE checking
		this.statusListener = statusListener;
	}
	
	protected void handleError(String msg, Throwable e) {
		logger.error(msg, e);
		statusListener.setErrorStatus(msg);
	}

}
