package com.sinergise.geopedia.core.entities.baselayers;

import com.sinergise.common.geometry.tiles.IsTileProvider;


public class TiledBaseLayer extends BaseLayer {
	private static final long serialVersionUID = -436638516381337522L;

	public IsTileProvider tileProvider;
	
	
	public static TiledBaseLayer create(int id, String name, String description, IsTileProvider tileProvider) {
		TiledBaseLayer tbl = new TiledBaseLayer(id, name, description, tileProvider);
		return tbl;
	}
	protected TiledBaseLayer(int id, String name, String description, IsTileProvider tileProvider) {
		super(Type.TILED);
		this.tileProvider = tileProvider;
		this.id = id;
		this.name = name;
		this.description = description;
	}
	
	@Deprecated
	protected TiledBaseLayer() {
		super(Type.TILED);
	}

	
	
	public IsTileProvider getTileProvider() {
		return tileProvider;
	}

	
	
	@Override
	public int getMaxScaleLevel() {
		return getTileProvider().getDatasetProperties().getMaxScale();
	}


}
