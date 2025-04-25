/*
 *
 */
package com.sinergise.common.geometry.tiles;

import static com.sinergise.common.geometry.tiles.TiledCRS.AxisSign.POSITIVE;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.display.ScaleLevelsSpec;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;


public class GeopediaTiledCRS {
	public static final DimI GP_TILE_SIZE = new DimI(256, 256);
	public static final double GP_MAX_WORLD_TILE_SIZE = 262144;
	public static final double GP_BOTTOM = -19000;
	public static final double GP_LEFT = 368000;
	public static final Point GP_TOP_LEFT = new Point(GP_LEFT, GP_BOTTOM + GP_MAX_WORLD_TILE_SIZE);
	public static final Envelope GP_BOUNDS = new Envelope(//
		GP_LEFT, GP_BOTTOM, //
		GP_LEFT + GP_MAX_WORLD_TILE_SIZE, GP_BOTTOM + GP_MAX_WORLD_TILE_SIZE);

	private GeopediaTiledCRS() {}
	
	public static WithBounds create(String name, int maxLevel) {
		WithBounds ret = new WithBounds(CRS.D48_GK, name,// 
			ScaleLevelsSpec.createWithFactor2(1.0, 17, maxLevel, 7), //
			GP_BOUNDS, GP_TILE_SIZE, //
			POSITIVE, POSITIVE);
		ret.setTilePrefixChar('S');
		return ret;
	}
}
