package com.sinergise.common.gis.map.ui.info;

import com.sinergise.common.gis.map.model.MapViewContext;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoItem;
import com.sinergise.common.gis.map.model.layer.info.MultipleFeatureCollection;
import com.sinergise.common.util.lang.Predicate;

public class FeatureInfoUtil {
	private FeatureInfoUtil() {}

	public static FeatureInfoCollection remove(FeatureInfoCollection source, Predicate<FeatureInfoItem> filter) {
		MultipleFeatureCollection mf = null;
		int srcCount = source.getItemCount();
		for (int i = 0; i < srcCount; i++) {
			FeatureInfoItem fItm = source.getItem(i);
			if (filter.eval(fItm)) {
				if (mf == null) {
					mf = new MultipleFeatureCollection();
					for (int j = 0; j < i; j++) {
						mf.add(source.getItem(j));
					}
				}
			} else if (mf != null) {
				mf.add(fItm);
			}
		}
		return mf == null ? source : mf;
	}
	
	public static void updateTransient(FeatureInfoItem[] items, MapViewContext context) {
		if (items == null) {
			return;
		}
		for (FeatureInfoItem item : items) {
			updateTransient(item, context);
		}
	}

	public static void updateTransient(Iterable<FeatureInfoItem> items, MapViewContext context) {
		if (items == null) {
			return;
		}
		for (FeatureInfoItem item : items) {
			updateTransient(item, context);
		}
	}

	public static void updateTransient(FeatureInfoItem item, MapViewContext context) {
		if (item == null) {
			return;
		}
		item.updateTransient(context);
	}
}
