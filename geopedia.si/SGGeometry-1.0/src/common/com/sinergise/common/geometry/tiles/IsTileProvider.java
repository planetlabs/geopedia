package com.sinergise.common.geometry.tiles;

import com.sinergise.common.util.server.IsObjectURLProvider;

public interface IsTileProvider extends IsObjectURLProvider {
	public String getTileURL(TiledCRS crs,  int scaleLevel, int column, int row);
	public TiledDatasetProperties getDatasetProperties();
	public String getDatasetPropertiesConfigurationURL();
}
