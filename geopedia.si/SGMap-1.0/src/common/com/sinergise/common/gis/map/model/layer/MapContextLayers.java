/*
 *
 */
package com.sinergise.common.gis.map.model.layer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sinergise.common.gis.feature.RepresentsFeature;
import com.sinergise.common.gis.map.model.layer.view.LayersView;
import com.sinergise.common.util.collections.tree.AbstractTree;
import com.sinergise.common.util.collections.tree.ITreeNode;
import com.sinergise.common.util.collections.tree.TreeVisitor;
import com.sinergise.common.util.collections.tree.TreeVisitor.SingleNodeFinder;
import com.sinergise.common.util.string.StringUtil;

public class MapContextLayers extends AbstractTree<LayerTreeElement> {
	public static final String ROOT_LAYER_ID = "{{LAYERS_ROOT}}";
    public static class RootLayersNode extends LayerGroup {
        public RootLayersNode() {
            super(ROOT_LAYER_ID);
        }
    }
    
    /** Depth first flat list of layers */
    List<LayerTreeElement> flatList = new ArrayList<LayerTreeElement>();
    
    public MapContextLayers() {
        super(new RootLayersNode());
    }
    
    public RootLayersNode getRootLayer() {
        return getRoot();
    }
    
    @Override
    public RootLayersNode getRoot() {
    	return (RootLayersNode)super.getRoot();
    }
    
    /**
     * @param lyr
     * @return the first element with id matching the "lyr" parameter (case-insensitive)
     */
    public LayerTreeElement findById(final String lyr) {
        SingleNodeFinder<LayerTreeElement> snf=new TreeVisitor.SingleNodeFinder<LayerTreeElement>() {
            @Override
			public boolean matches(LayerTreeElement l) {
                if (lyr.equalsIgnoreCase(l.getLocalID())) {
                	return true;
                }
                return false;
            }
        };
        traverseDepthFirst(snf);
        return snf.result;
    }
    
    public Layer findByName(final String lyr) {
    	if (StringUtil.isNullOrEmpty(lyr)) {
    		return null;
    	}
    	
        SingleNodeFinder<LayerTreeElement> snf=new TreeVisitor.SingleNodeFinder<LayerTreeElement>() {
            @Override
			public boolean matches(LayerTreeElement node) {
                if (node instanceof Layer) {
                	Layer l=(Layer)node;
                    if (lyr.equalsIgnoreCase(l.getSpec().getLocalID())) return true;
                }
                return false;
            }
        };
        traverseDepthFirst(snf);
        return (Layer)snf.result;
    }
    
    public FeatureDataLayer findByFeatureType(final String featureType) {
        SingleNodeFinder<LayerTreeElement> snf=new TreeVisitor.SingleNodeFinder<LayerTreeElement>() {
            @Override
			public boolean matches(LayerTreeElement node) {
                if (node instanceof FeatureDataLayer) {
                	FeatureDataLayer l=(FeatureDataLayer)node;
                    if (l.getFeatureTypeName().equals(featureType)) {
                    	return true;
                    }
                }
                return false;
            }
        };
        traverseDepthFirst(snf);
        return (FeatureDataLayer)snf.result;
    }
    
	public void afterRepaint() {
	}
	
	@Override
	public void childAdded(ITreeNode<LayerTreeElement> parent, LayerTreeElement child, int index) {
		flatList = null;
		super.childAdded(parent, child, index);
	}
	
	@Override
	public void childRemoved(ITreeNode<LayerTreeElement> parent, LayerTreeElement child, int oldIdx) {
		flatList = null;
		super.childRemoved(parent, child, oldIdx);
	}
	
	/**
	 * @return Unmodifiable depth first flat list of layers.
	 */
	public List<LayerTreeElement> getFlatLayerList() {
		if (flatList == null) {
			flatList = new ArrayList<LayerTreeElement>();
			traverseDepthFirst(new TreeVisitor<LayerTreeElement>() {
				@Override
				public boolean visit(LayerTreeElement node) {
					flatList.add(node);
					return true;
				}
			});
		}
		return Collections.unmodifiableList(flatList);
	}
	
	
	private Map<String, LayersView> layerViews = new HashMap<String, LayersView>();
	
	public void registerNamedLayersView(String name, LayersView view) {
		if (layerViews.containsKey(name)) {
			throw new IllegalArgumentException("A view is already registered under name: "+name);
		}
		layerViews.put(name, view);
	}
	
	public LayersView deregisterNamedLayersView(String name) {
		return layerViews.remove(name);
	}
	
	public LayersView getNamedLayersView(String name) {
		LayersView view = layerViews.get(name);
		
		if (view == null) {
			if (LAYERS_VIEW_DEEP_ON.equals(name)) {
				layerViews.put(LAYERS_VIEW_DEEP_ON, view = new LayersView.LayersDeepOnView(this));
			} else if (LAYERS_VIEW_ON.equals(name)) {
				layerViews.put(LAYERS_VIEW_ON, view = new LayersView.LayersOnView(this));
			} else if (LAYERS_VIEW_SHOW_LEGEND.equals(name)) {
				layerViews.put(LAYERS_VIEW_SHOW_LEGEND, view = new LayersView.LayersShowLegendView(this));
			} else if (LAYERS_VIEW_FEATURE_DATA.equals(name)) {
				layerViews.put(LAYERS_VIEW_FEATURE_DATA, view = new LayersView.FeatureDataLayersView(this));
			}
			
			//Add additional common views here
		}
		return view;
	}
	
	
	public static final String LAYERS_VIEW_DEEP_ON = "LAYERS_VIEW_DEEP_ON";
	public static final String LAYERS_VIEW_ON = "LAYERS_VIEW_ON";
	public static final String LAYERS_VIEW_HLT_ON = "LAYERS_VIEW_HLT_ON";
	public static final String LAYERS_VIEW_SHOW_LEGEND = "LAYERS_VIEW_SHOW_LEGEND";
	public static final String LAYERS_VIEW_FEATURE_DATA = "LAYERS_VIEW_FEATURE_DATA";
	
	
	public Collection<FeatureDataLayer> extractFeatureLayers(Collection<? extends RepresentsFeature> fReps) {
		Map<String, FeatureDataLayer> layersMap = new HashMap<String, FeatureDataLayer>();
		
		for (RepresentsFeature fRep : fReps) {
			String featureType = fRep.getQualifiedID().getFeatureTypeName();
			if (!layersMap.containsKey(featureType)) {
				layersMap.put(featureType, findByFeatureType(featureType));
			}
		}
		
		return layersMap.values();
	}
	
	
}
