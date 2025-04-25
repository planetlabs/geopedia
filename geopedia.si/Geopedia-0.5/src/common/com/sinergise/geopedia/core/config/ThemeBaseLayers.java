package com.sinergise.geopedia.core.config;

import java.io.Serializable;
import java.util.HashMap;

public class ThemeBaseLayers  implements Serializable {

	private static final long serialVersionUID = 2613977267708827576L;

	public static final String SETTING_DEFAULT="DEFAULT";
	public static final String SETTING_PRO="PRO";
	
	public Integer defaultDataset;
	public HashMap<String, Integer[][]> datasets = new  HashMap<String, Integer[][]>();
	
	public Integer[][] getBaseLayer(String baseLayerGroup) {
		return datasets.get(baseLayerGroup);
	}
	
	@SuppressWarnings("unchecked")
	public ThemeBaseLayers clone() {
		ThemeBaseLayers cloned = new ThemeBaseLayers();
		cloned.datasets = (HashMap<String, Integer[][]>) datasets.clone();
		cloned.defaultDataset=defaultDataset;
		return cloned;
	}
	
	public boolean hasBaseLayer(String baseLayerGroup, int baseLayerId) {
		Integer[][] dsIds = getBaseLayer(baseLayerGroup);
		if (dsIds==null || dsIds.length==0)
			return false;
		for (Integer[] dss:dsIds) {
			for (int dsId:dss) {
				if (dsId==baseLayerId)
					return true;
			}
		}
		return false;
	}
}
