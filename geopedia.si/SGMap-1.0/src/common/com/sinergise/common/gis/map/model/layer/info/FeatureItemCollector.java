/**
 * 
 */
package com.sinergise.common.gis.map.model.layer.info;


/**
 * Collector of feature items intended to collect
 * feature results returned from RPC callbacks.
 * <br><br>
 * Implementators of this interface may be GUI panels rendering collections
 * of features or just a simple holders for features.
 * 
 * @author tcerovski
 */
public interface FeatureItemCollector {
	
	public void addAll(FeatureInfoCollection features);
	
	public void add(FeatureInfoItem feature);	
	
	public void clearFeatures();
	
}
