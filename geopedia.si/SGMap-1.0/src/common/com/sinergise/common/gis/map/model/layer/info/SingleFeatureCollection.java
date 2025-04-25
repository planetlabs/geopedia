package com.sinergise.common.gis.map.model.layer.info;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.sinergise.common.gis.feature.RepresentsFeature;
import com.sinergise.common.gis.map.model.MapViewContext;
import com.sinergise.common.gis.map.ui.info.FeatureInfoUtil;
import com.sinergise.common.util.collections.OneElIterator;

/**
 * Convenience class wrapping single FeatureItem as FeatureInfoCollection.
 *  
 * @author tcerovski
 */
public class SingleFeatureCollection implements FeatureInfoCollection {
	
	private static final long serialVersionUID = -6861187443622065258L;
	
	private FeatureInfoItem item;
	
	public SingleFeatureCollection(FeatureInfoItem item) {
		this.item = item;
	}
	
	@Override
	public Iterator<FeatureInfoItem> iterator() {
		return OneElIterator.createFor(item);
	}

	@Override
	public Collection<FeatureInfoItem> getAll() {
		return Collections.singletonList(item);
	}

	@Override
	public FeatureInfoItem getItem(int i) {
		if(i != 0) throw new IndexOutOfBoundsException(i+" > 0");
		return item;
	}

	@Override
	public int getItemCount() {
		return 1;
	}
	
	@Override
	public int getHitCount() {
		return 1;
	}
	
	@Override
	public void updateTransient(MapViewContext context) {
		FeatureInfoUtil.updateTransient(item, context);
	}
	
	@Override
	public Collection<? extends RepresentsFeature> getFeatures() {
		return Collections.singleton(item);
	}
}
