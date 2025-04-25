package com.sinergise.common.geometry.tiles;

import static com.sinergise.common.geometry.tiles.TiledCRS.AxisSign.POSITIVE;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.display.ScaleLevelsSpec;
import com.sinergise.common.geometry.display.ScaleLevelsSpec.ZoomLevelsPix;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.Position2D;

public class WithBounds extends TiledCRS {
	public static final WithBounds createDefault(CRS cs, String name, int minLevel, int maxLevel, DimI tileSize) {
		ZoomLevelsPix zooms = ScaleLevelsSpec.createWithFactor2(1, 17, maxLevel, minLevel);
		Envelope bnds = new Envelope(0, 0, tileSize.w()*zooms.worldPerPix(minLevel), tileSize.h()*zooms.worldPerPix(minLevel));
		return new WithBounds(cs, name, zooms, bnds, tileSize);
	}
	
    private DimI tileSize;
    private Position2D origin;
    private Position2D opposite;
    
    public WithBounds() {
        super();
    }
    
    public WithBounds(CRS baseCRS, String name, ScaleLevelsSpec.ZoomLevelsPix zoomLevels,
                      Envelope bounds, DimI tileSizePx) {
        this(baseCRS, name, zoomLevels, bounds, tileSizePx, POSITIVE, POSITIVE);
    }
    
    public WithBounds(CRS baseCRS, String name, ScaleLevelsSpec.ZoomLevelsPix zoomLevels,
                      Envelope bounds, DimI tileSizePx, AxisSign columnSign, AxisSign rowSign) {
        super(baseCRS, name, zoomLevels, columnSign, rowSign);
        this.tileSize = tileSizePx;
        this.origin = originCorner(bounds);
        this.opposite = oppositeCorner(bounds);
    }
        
	private Position2D oppositeCorner(Envelope env) {
		return new Position2D(origin.x() + colSign.signedDelta(env.getWidth()), origin.y() + rowSign.signedDelta(env.getHeight()));
	}

	@SuppressWarnings("boxing")
	private Position2D originCorner(Envelope env) {
		return new Position2D(colSign.withMinIndex(env.getMinX(), env.getMaxX()), rowSign.withMinIndex(env.getMinY(), env.getMaxY()));
	}

	@Override
    public int tileMatrixHeight(int zoomLevel) {
		return matrixSize(zoomLevel, origin.y, opposite.y, tileSize.h());
    }

	@Override
    public int tileMatrixWidth(int zoomLevel) {
        return matrixSize(zoomLevel, origin.x, opposite.x, tileSize.w());
    }
	
    private int matrixSize(int zoomLevel, double originOrdinate, double oppositeOrdinate, int tileSpan) {
        double pxSize = zoomLevels.worldPerPix(zoomLevel);
        double worldSpan = Math.abs(oppositeOrdinate - originOrdinate);
        return (int)Math.ceil((worldSpan - pxSize/8) / (pxSize * tileSpan));
	}

    @Override
    public DimI tileSizeInPix(int zoomLevel) {
        return tileSize;
    }
    
    @Override
    public Position2D worldTilesOrigin(int arg0) {
        return origin;
    }    
    
    @Override
    public boolean isQuad() {
    	return zoomLevels.isFactorsOf2();
    }
    
    /* (non-Javadoc)
     * @see com.sinergise.common.geometry.tiles.TiledCRS#createWithSubsetLevels(int, int)
     */
    @Override
    public TiledCRS createWithMaxLevel(int maxLevel) {
    	if (maxLevel == this.zoomLevels.getMaxLevelId()) return this;
    	double[] levels = new double[maxLevel-zoomLevels.getMinLevelId()+1];
    	for (int i = 0; i < levels.length; i++) {
			levels[i]=zoomLevels.worldPerPix(zoomLevels.getMinLevelId()+i);
		}
    	ZoomLevelsPix newZL = new ZoomLevelsPix(zoomLevels.getMinLevelId(), levels);
    	return new WithBounds(baseCRS, name, newZL, getBounds().mbr, tileSize, colSign, rowSign)
    		.setTilePrefixChar(getTilePrefixChar())
    		.setInterlacedName(interlacedName);
    }
}