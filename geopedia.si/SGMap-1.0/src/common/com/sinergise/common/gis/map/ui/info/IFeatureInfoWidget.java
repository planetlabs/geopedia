/**
 * 
 */
package com.sinergise.common.gis.map.ui.info;

import com.sinergise.common.geometry.crs.CRS;

/**
 * @author tcerovski
 */
public interface IFeatureInfoWidget {
	/** Called by the feature info action when user click somewhere on the map */
	public void showInfo(CRS crs, double wx, double wy);

	/** Should display the feature info widget. Called when the user initiates feature info mode */
	public void showWidget();

	/** Should hide the feature info control when user resets the */
	public void hideWidget();
}
