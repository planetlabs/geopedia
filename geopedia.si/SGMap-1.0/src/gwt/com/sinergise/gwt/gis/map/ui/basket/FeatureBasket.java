package com.sinergise.gwt.gis.map.ui.basket;

import static com.sinergise.common.gis.map.model.layer.LayerTreeElement.INHERITANCE_CHILD_OVERRIDES;
import static com.sinergise.common.util.string.StringUtil.isTruthy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.view.client.MultiSelectionModel;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.CFeatureCollection;
import com.sinergise.common.gis.feature.CFeatureUtils;
import com.sinergise.common.gis.feature.HasFeatures;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasEnvelope;
import com.sinergise.gwt.gis.map.ui.basket.res.FeatureBasketResources;

public class FeatureBasket implements HasFeatures, HasEnvelope {
	
	public static final String	LAYER_PROP_BASKET = "basket";
	
	public static boolean isBasketEnabledOnLayer(LayerTreeElement layer) {
		return layer instanceof FeatureDataLayer 
			&& isBasketEnabledOnLayer((FeatureDataLayer)layer);
	}
	
	public static boolean isBasketEnabledOnLayer(FeatureDataLayer layer) {
		return isTruthy(((LayerTreeElement)layer).getGenericProperty(LAYER_PROP_BASKET, INHERITANCE_CHILD_OVERRIDES), false);
	}
	
	
	public interface BasketContentChangeListener {
		void onContentChanged();
	}
	
	private final FeatureDataLayer layer;
	private final Map<String, CFeature> features = new LinkedHashMap<String, CFeature>();
	private final MultiSelectionModel<CFeature> selection = new MultiSelectionModel<CFeature>();
	
	private final List<BasketContentChangeListener> changeListeners = new ArrayList<BasketContentChangeListener>();
	
	private boolean autoSelectOnAdd = true;
	
	public FeatureBasket(FeatureDataLayer layer) {
		this.layer = layer;
		FeatureBasketResources.BASKET_RESOURCES.basketStyle().ensureInjected();
	}
	
	public String getFeatureType() {
		return layer.getFeatureTypeName();
	}
	
	public FeatureDataLayer getFeatureDataLayer() {
		return layer;
	}
	
	public Layer getLayer() {
		return (Layer) layer;
	}
	
	@Override
	public CFeatureCollection getFeatures() {
		return new CFeatureCollection(features.values());
	}
	
	public CFeatureCollection getSelectedFeatures() {
		return new CFeatureCollection(selection.getSelectedSet());
	}
	
	@Override
	public Envelope getEnvelope() {
		if (isEmpty()) {
			return Envelope.getEmpty();
		}
		
		return CFeatureUtils.getMBR(features.values());
	}
	
	public MultiSelectionModel<CFeature> getSelectionModel() {
		return selection;
	}
	
	public void addContentChangeListener(BasketContentChangeListener listener) {
		changeListeners.add(listener);
	}
	
	public void removeContentChangeListener(BasketContentChangeListener listener) {
		changeListeners.remove(listener);
	}
	
	public boolean contains(CFeature f) {
		return f.getFeatureTypeName().equals(layer.getFeatureTypeName())
			&& features.containsKey(f.getLocalID());
	}
	
	public void add(CFeature f) {
		addInternally(f);
		fireContentChanged();
	}
	
	public void addAll(Collection<CFeature> toAdd) {
		for (CFeature f : toAdd) {
			addInternally(f);
		}
		fireContentChanged();
	}
	
	public boolean remove(CFeature f) {
		if (removeInternally(f)) {
			fireContentChanged();
			return true;
		}
		return false;
	}
	
	public void removeAll(Collection<CFeature> toRemove) {
		boolean changed = false;
		for (CFeature f : toRemove) {
			changed |= removeInternally(f);
		}
		
		if (changed) {
			fireContentChanged();
		}
	}
	
	public void retainAll(Collection<CFeature> toRetain) {
		
		Set<CFeature> set = new HashSet<CFeature>(toRetain);
		List<CFeature> toRemove = new ArrayList<CFeature>();
		for (CFeature f : features.values()) {
			if (!set.contains(f)) {
				toRemove.add(f);
			}
		}
		
		removeAll(toRemove);
	}
	
	private void addInternally(CFeature f) {
		checkFeatureType(f);
		features.put(f.getLocalID(), f);
		if (autoSelectOnAdd) {
			selection.setSelected(f, true);
		}
	}
	
	private boolean removeInternally(CFeature f) {
		boolean removed = features.remove(f.getLocalID()) != null;
		selection.setSelected(f, false);
		return removed; 
	}
	
	private void fireContentChanged() {
		for (BasketContentChangeListener l : changeListeners) {
			l.onContentChanged();
		}
	}

	private void checkFeatureType(CFeature f) {
		if (!layer.getFeatureTypeName().equals(f.getFeatureTypeName())) {
			throw new IllegalArgumentException("Invalid feature type: "+f.getFeatureTypeName()+", expected:"+layer);
		}
	}
	
	public boolean getAutoSelectOnAdd() {
		return autoSelectOnAdd;
	}
	
	public void setAutoSelectOnAdd(boolean b) {
		autoSelectOnAdd = b;
	}

	public int size() {
		return features.size();
	}
	
	public int selectionSize() {
		return selection.getSelectedSet().size();
	}
	
	public double totalArea() {
		double area = 0;
		for (CFeature f : getFeatures()) {
			if (f.hasGeometry()) {
				area += f.getGeometry().getArea();
			}
		}
		return area;
	}
	
	public boolean isEmpty() {
		return features.isEmpty();
	}
	
	public boolean isSelectionEmpty() {
		return selection.getSelectedSet().isEmpty();
	}
	
	public void clear() {
		features.clear();
		selection.clear();
		fireContentChanged();
	}
	
}
