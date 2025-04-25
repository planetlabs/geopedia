package com.sinergise.common.cluster.swift;

import com.sinergise.common.geometry.tiles.IsTileProvider;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.geometry.tiles.TiledDatasetProperties;

public class SwiftTileURLProvider extends SwiftObjectURLProvider implements IsTileProvider{
	private static final long serialVersionUID = 836012738264792067L;	
	private TiledDatasetProperties dsPropeties;
	
	public SwiftTileURLProvider(SwiftAccount account, TiledDatasetProperties dsProperties) {
		super(account);
		this.dsPropeties = dsProperties;
	}
	
	/** Serialization only **/
	@Deprecated
	protected SwiftTileURLProvider() {
	}

	public TiledDatasetProperties getDatasetProperties() {
		return dsPropeties;
	}
	
	@Override
	public String getTileURL(TiledCRS crs, int scaleLevel, int column, int row) {
		return getObjectURL(dsPropeties.getTileLocation(crs, scaleLevel, column, row));
	}

	@Override
	public String getDatasetPropertiesConfigurationURL() {
		return getObjectURL(dsPropeties.getPropertiesFilePath());
	}

}
