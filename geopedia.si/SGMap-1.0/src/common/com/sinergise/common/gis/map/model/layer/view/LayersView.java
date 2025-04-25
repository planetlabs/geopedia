package com.sinergise.common.gis.map.model.layer.view;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.gis.map.model.layer.MapContextLayers;
import com.sinergise.common.util.collections.tree.TreeListener;
import com.sinergise.common.util.collections.tree.TreeVisitor;
import com.sinergise.common.util.naming.Identifier;

/**
 * @author tcerovski
 *
 */
public abstract class LayersView implements TreeListener<LayerTreeElement>, Iterable<LayerTreeElement> {

	private final MapContextLayers tree;
	private final LayersViewListenerCollection listeners = new LayersViewListenerCollection();
	LinkedHashMap<Identifier, Boolean> view = new LinkedHashMap<Identifier, Boolean>();
	
	public LayersView(MapContextLayers layersTree) {
		this(layersTree, true);
	}
	
	protected LayersView(MapContextLayers layersTree, boolean doScan) {
		tree = layersTree;
		tree.addTreeListener(this);
		if (doScan) {
			rescan();
		}
	}
	
	protected abstract boolean match(LayerTreeElement node);
	
	protected abstract boolean ignorePropertyChange(LayerTreeElement node, String propertyName);
	
	protected void rescan() {
		LinkedHashMap<Identifier, Boolean> newView = new LinkedHashMap<Identifier, Boolean>();
		boolean changed = false;
		
		for (LayerTreeElement layer : tree.getFlatLayerList()) {
			Identifier key = layer.getQualifiedID();
			boolean matched = match(layer);
			changed |= !view.containsKey(key) || matched != view.get(key).booleanValue();
			newView.put(key, Boolean.valueOf(matched));
		}
		
		if (changed) {
			view = newView;
			listeners.fireLayerViewChanged();
		}
	}
	
	@Override
	public void nodeAdded(LayerTreeElement parent, LayerTreeElement added, int newIndex) {
		rescan();
	}
	
	@Override
	public void nodeRemoved(LayerTreeElement parent, LayerTreeElement removed, int oldIndex) {
		rescan();
	}
	
	@Override
	public void treeStructureChanged(LayerTreeElement changeRoot) {
		rescan();
	}
	
	@Override
	public void nodeChanged(LayerTreeElement node, String propertyName) {
		if (ignorePropertyChange(node, propertyName)) {
			return;
		}
		
		final BooleanHolder viewChanged = new BooleanHolder(false);
		
		//check subtree
		tree.traverseSubTree(node, true, new TreeVisitor<LayerTreeElement>() {
			@Override
			public boolean visit(LayerTreeElement layer) {
				boolean matched = match(layer);
				viewChanged.val |= matched != view.get(layer.getQualifiedID()).booleanValue();
				view.put(layer.getQualifiedID(), Boolean.valueOf(matched));
				return true;
			}
		});
		
		//check parents
		LayerTreeElement parent = node.getParent();
		while (parent != null) {
			boolean matched = match(parent);
			viewChanged.val |= matched != view.containsKey(parent.getQualifiedID());
			view.put(parent.getQualifiedID(), Boolean.valueOf(matched));
			parent = parent.getParent();
		}
		
		if (viewChanged.val) {
			listeners.fireLayerViewChanged();
		} else {
			listeners.fireLayerNodeChanged(node, propertyName);
		}
	}
	
	public final void addLayersViewListener(LayersViewListener listener) {
		listeners.add(listener);
	}
	
	public final boolean removeLayersViewListener(LayersViewListener listener) {
		return listeners.remove(listener);
	}
	
	private static class BooleanHolder {
		boolean val;
		BooleanHolder(boolean val) {
			this.val = val;
		}
	}
	
	@Override
	public final Iterator<LayerTreeElement> iterator() {
		return new LayersViewIterator(view, tree.getFlatLayerList());
	}
	
	private static class LayersViewIterator implements Iterator<LayerTreeElement> {
		private final Map<Identifier, Boolean> view;
		private final Iterator<LayerTreeElement> layersIter;
		private LayerTreeElement next;
		
		LayersViewIterator(Map<Identifier, Boolean> view, List<LayerTreeElement> layers) {
			this.view = view;
			this.layersIter = layers.iterator();
			peekNext();
		}
		
		private void peekNext() {
			next = null;
			boolean go = layersIter.hasNext();
			while (go) {
				LayerTreeElement layer = layersIter.next();
				if (view.get(layer.getQualifiedID()).booleanValue()) { //is in view
					next = layer;
					go = false;
				} else {
					go = layersIter.hasNext();
				}
			}
		}
		
		@Override
		public boolean hasNext() {
			return next != null;
		}
		
		@Override
		public LayerTreeElement next() {
			if (next == null) {
				throw new NoSuchElementException();
			}
			LayerTreeElement ret = next;
			peekNext();
			return ret;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	
	public static final class LayersDeepOnView extends LayersView {
		
		public LayersDeepOnView(MapContextLayers layersTree) {
			super(layersTree);
		}
		
		@Override
		protected boolean match(LayerTreeElement node) {
			return node.deepOn();
		}
		
		@Override
		protected boolean ignorePropertyChange(LayerTreeElement node, String propertyName) {
			return !LayerTreeElement.PROP_ON.equals(propertyName);
		}
	}
	
	public static final class LayersOnView extends LayersView {
		
		public LayersOnView(MapContextLayers layersTree) {
			super(layersTree);
		}
		
		@Override
		protected boolean match(LayerTreeElement node) {
			return node.isOn();
		}
		
		@Override
		protected boolean ignorePropertyChange(LayerTreeElement node, String propertyName) {
			return !LayerTreeElement.PROP_ON.equals(propertyName);
		}
	}
	
	public static final class FeatureDataLayersView extends LayersView {
		
		public FeatureDataLayersView(MapContextLayers layersTree) {
			super(layersTree);
		}
		
		@Override
		protected boolean match(LayerTreeElement node) {
			return node instanceof FeatureDataLayer;
		}
		
		@Override
		protected boolean ignorePropertyChange(LayerTreeElement node, String propertyName) {
			return true;
		}
		
	}
	
	public static final class QueryableLayersView extends LayersView {
		
		private final FilterCapabilities minimalQueryCaps;
		
		public QueryableLayersView(MapContextLayers layersTree, FilterCapabilities minimalQueryCaps) {
			super(layersTree, false);
			this.minimalQueryCaps = minimalQueryCaps;
			rescan();
		}
		
		@Override
		protected boolean match(LayerTreeElement node) {
			return node instanceof FeatureDataLayer
				&& ((FeatureDataLayer)node).isFeatureDataQueryEnabled(minimalQueryCaps); 
		}
		
		@Override
		protected boolean ignorePropertyChange(LayerTreeElement node, String propertyName) {
			return true;
		}
		
	}
	
	public static final class LayersShowLegendView extends LayersView {
		
		public LayersShowLegendView(MapContextLayers layersTree) {
			super(layersTree);
		}
		
		@Override
		protected boolean match(LayerTreeElement node) {
			return node.showLegend();
		}
		
		@Override
		protected boolean ignorePropertyChange(LayerTreeElement node, String propertyName) {
			return !LayerTreeElement.PROP_SHOW_LEGEND.equals(propertyName);
		}
	}
	
}
