/*
 *
 */
package com.sinergise.common.gis.geopedia;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.display.ScaleLevelsSpec;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.tiles.WithBounds;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;


public class GeopediaTiledCRS extends WithBounds {
  public static final DimI GP_TILE_SIZE = new DimI(256, 256);
  public static final double GP_MAX_WORLD_TILE_SIZE = 262144;
  public static final double GP_BOTTOM = -19000;
  public static final double GP_LEFT = 368000;
  public static final Point GP_TOP_LEFT = new Point(GP_LEFT, GP_BOTTOM + GP_MAX_WORLD_TILE_SIZE);
  public static final Envelope GP_BOUNDS = new Envelope(GP_LEFT, GP_BOTTOM, GP_LEFT+GP_MAX_WORLD_TILE_SIZE, GP_BOTTOM+GP_MAX_WORLD_TILE_SIZE);

  public GeopediaTiledCRS(String name, int maxLevel) {
      super(CRS.D48_GK, 
        		name, 
        		ScaleLevelsSpec.createWithPixSize(1, 2, Math.pow(2,17-maxLevel), maxLevel, 7),
        		GP_BOUNDS,
        		GP_TILE_SIZE, AxisSign.POSITIVE, AxisSign.POSITIVE);
      setTilePrefixChar('S');
  }
}
