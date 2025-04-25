package com.sinergise.common.gis.map.model.layer.info;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import com.sinergise.common.gis.feature.CFeatureIdentifier;
import com.sinergise.common.gis.feature.RepresentsFeature;
import com.sinergise.common.gis.map.model.MapViewContext;
import com.sinergise.common.gis.map.ui.info.FeatureInfoUtil;

/**
 * Convenience class wrapping multiple FeatureItem as FeatureInfoCollection.
 * 
 * @author mperse
 */
// TODO: Merge all implementations of FeatureInfoCollection into one class
public class MultipleFeatureCollection implements FeatureInfoCollection {
	
	private static final long serialVersionUID = -5759757137395851535L;
	private ArrayList<FeatureInfoItem> items;
	
	public MultipleFeatureCollection() {
		this.items = new ArrayList<FeatureInfoItem>();
	}
	
	@Override
	public Iterator<FeatureInfoItem> iterator() {
		return items.iterator();
	}

	@Override
	public Collection<FeatureInfoItem> getAll() {
		return Collections.unmodifiableList(items);
	}
	
	@Override
	public Collection<? extends RepresentsFeature> getFeatures() {
		return getAll();
	}

	public void addAll(Collection<FeatureInfoItem> itms) {
		items.addAll(itms);
	}

	public void addAll(FeatureInfoItem[] itms) {
		for (int i = 0; i < itms.length; i++) {
			items.add(itms[i]);
		}
	}

	public void add(FeatureInfoItem item) {
		items.add(item);
	}

	@Override
	public FeatureInfoItem getItem(int i) {
		if (i >= items.size()) {
			throw new IndexOutOfBoundsException("Collection size is " + items.size());
		}
		return items.get(i);
	}

	@Override
	public int getItemCount() {
		return items.size();
	}
	
	@Override
	public int getHitCount() {
		return items.size();
	}
	
	@Override
	public void updateTransient(MapViewContext context) {
		FeatureInfoUtil.updateTransient(items, context);
	}

	public void clearDuplicates() {
		HashSet<CFeatureIdentifier> set = new HashSet<CFeatureIdentifier>();
		for (Iterator<FeatureInfoItem> it = items.iterator(); it.hasNext();) {
			FeatureInfoItem itm = it.next();
			if (itm == null || itm.f == null || itm.f.getIdentifier() == null) continue;
			if (!set.add(itm.f.getIdentifier())) {
				it.remove();
			}
		}
	}
	
	@Override
	public String toString() {
		return String.valueOf(items);
	}
}
