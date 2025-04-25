/*
 *
 */
package com.sinergise.common.geometry.tiles;

import static com.sinergise.common.geometry.tiles.TiledCRS.AxisSign.NEGATIVE;
import static com.sinergise.common.geometry.tiles.TiledCRS.AxisSign.POSITIVE;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.display.DisplayBounds;
import com.sinergise.common.geometry.display.ScaleLevelsSpec;
import com.sinergise.common.geometry.display.ScaleLevelsSpec.ZoomLevelsPix;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.common.util.geom.EnvelopeI;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.geom.PointI;
import com.sinergise.common.util.geom.RectSideOffsetsI;
import com.sinergise.common.util.math.Interval;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.common.util.settings.Settings;
import com.sinergise.common.util.settings.Settings.NeedsUpdateAfterDeserialization;
import com.sinergise.common.util.settings.Settings.NeedsUpdateBeforeSerialization;
import com.sinergise.common.util.settings.Settings.TypeMap;


@TypeMap(names = {"", "NO_ZOOMS"}, types = {WithBounds.class, WithoutZooms.class})
public abstract class TiledCRS implements Settings, NeedsUpdateAfterDeserialization, NeedsUpdateBeforeSerialization {
	public enum AxisSign {
		POSITIVE {
			@Override
			public int nextRightOrUp(int index) {
				return index + 1;
			}

			@Override
			public double signedDelta(double worldDelta) {
				return worldDelta;
			}

			@Override
			public double tileMinWorld(int tileIndex, double worldOriginOrdinate, double worldTileSpan) {
				return worldOriginOrdinate + tileIndex * worldTileSpan;
			}

			@Override
			protected <T extends Comparable<? super T>> T leftOrBottomMost(Interval<T> worldInterval) {
				return worldInterval.getMinValue();
			}

			@Override
			@Deprecated
			public int asInt() {
				return 1;
			}

			@Override
			public <T> T withMinIndex(T worldMin, T worldMax) {
				return worldMin;
			}

			@Override
			public <T> T withMaxIndex(T worldMin, T worldMax) {
				return worldMax;
			}
			
			@Override
			public int tileEndPixels(int val, int size) {
				return size - val;
			}
		},
		NEGATIVE {
			@Override
			public int nextRightOrUp(int index) {
				return index - 1;
			}

			@Override
			public double signedDelta(double worldDelta) {
				return -worldDelta;
			}
			
			@Override
			public double tileMinWorld(int tileIndex, double worldOriginOrdinate, double worldTileSpan) {
				return worldOriginOrdinate - (tileIndex + 1) * worldTileSpan;
			}

			@Override
			protected <T extends Comparable<? super T>> T leftOrBottomMost(Interval<T> worldInterval) {
				return worldInterval.getMaxValue();
			}

			@Override
			public <T> T withMinIndex(T worldMin, T worldMax) {
				return worldMax;
			}

			@Override
			public <T> T withMaxIndex(T worldMin, T worldMax) {
				return worldMin;
			}

			@Override
			@Deprecated
			public int asInt() {
				return -1;
			}
			
			@Override
			public int tileStartPixels(int val, int size) {
				return size - val;
			}
		};

		public abstract int nextRightOrUp(int index);

		public abstract double signedDelta(double worldDelta);

		protected abstract <T extends Comparable<? super T>> T leftOrBottomMost(Interval<T> worldInterval);

		public abstract int asInt();

		public abstract <T> T withMaxIndex(T worldMin, T worldMax);

		public abstract <T> T withMinIndex(T worldMin, T worldMax);

		public abstract double tileMinWorld(int tileIndex, double worldOriginOrdinate, double worldTileSpan);

		public double dIndex(double worldOrdinate, double worldTileSpan, double worldOriginOrdinate) {
			return signedDelta((worldOrdinate - worldOriginOrdinate) / worldTileSpan);
		}

		@SuppressWarnings("boxing")
		public Interval<Double> tileWorldInterval(Interval<Integer> tilesSpan, double worldOriginOrdinate, double worldTileSize) {
			int nTiles = tilesSpan.getMaxValue() - tilesSpan.getMinValue() + 1;
			int leftIdx = leftOrBottomMost(tilesSpan);
			double worldMin = tileMinWorld(leftIdx, worldOriginOrdinate, worldTileSize);
			return Interval.closed(worldMin, worldMin + worldTileSize * nTiles);
		}

		public static final AxisSign valueOf(int sign) {
			return sign < 0 ? NEGATIVE : POSITIVE;
		}

		/**
		 * @param val tileIndex
		 * @param size tileSize
		 */
		public int tileStartPixels(int val, int size) {
			return val;
		}

		/**
		 * @param val tileIndex
		 * @param size tileSize 
		 */
		public int tileEndPixels(int val, int size) {
			return val;
		}
	}


	private static final AxisSign DEFAULT_COL_AXIS_SIGN = AxisSign.POSITIVE;
	private static final AxisSign DEFAULT_ROW_AXIS_SIGN = AxisSign.NEGATIVE;

	public static TiledCRS GP_SLO = GeopediaTiledCRS.create("SLO Tiles", 20);

	/**
	 * Pixel sizes are nice expressed in angular minutes 
	 */
	public static TiledCRS WGS84_PPC_MIN = new WithBounds(//
		CRS.WGS84_GLOBAL_PSEUDO_PLATTE_CARRE_SEC, "1 Minute-based global PPC", //
		ScaleLevelsSpec.createWithFactor2(60.0, 7, 20, 0), //level 7 has 60 seconds per pixel
		new Envelope(-180 * 3600, -90 * 3600, 180 * 3600, 270 * 3600), new DimI(256, 256), //
		POSITIVE, POSITIVE//
	).setTilePrefixChar('W');

	/**
	 * Pixel sizes are nice expressed in angular minutes 
	 */
	public static TiledCRS ETRS89_PPC_MIN = new WithBounds(//
		CRS.ETRS89_GLOBAL_PSEUDO_PLATTE_CARRE, "Global ETRS89 PPC, units are degrees, pixels sizes are multiple of minutes", //
		ScaleLevelsSpec.createWithFactor2(1.0/60.0, 7, 20, 1), //level 7 has 1 minute per pixel
		new Envelope(-180, -90, 180, 270), new DimI(512, 512), //
		POSITIVE, POSITIVE//
	);
	
	public static TiledCRS IMAGE_CRS_TILES = new WithBounds(//
		CRS.MAP_PIXEL_CRS, "Image-based tiled CRS", //
		ScaleLevelsSpec.createWithFactor2(1.0, 20, 0), //
		new Envelope(0, 0, 131072 * 256, 131072 * 256), new DimI(256, 256), //
		POSITIVE, NEGATIVE //
	).setTilePrefixChar('I');

	public final String name;
	public final CRS baseCRS;
	protected char tilePrefix = 'X';
	protected boolean interlacedName = false;
	public final ScaleLevelsSpec.ZoomLevelsPix zoomLevels;

	protected transient AxisSign colSign = DEFAULT_COL_AXIS_SIGN;
	protected transient AxisSign rowSign = DEFAULT_ROW_AXIS_SIGN;

	protected RectSideOffsetsI overlap = RectSideOffsetsI.EMPTY;

	protected transient DisplayBounds.Pix bounds;

	@Deprecated
	protected int rowDirection = DEFAULT_ROW_AXIS_SIGN.asInt(); //TODO: Remove when stored XMLs are replaced with new layout 
	@Deprecated
	protected int colDirection = DEFAULT_COL_AXIS_SIGN.asInt(); //TODO: Remove when stored XMLs are replaced with new layout 

	protected TiledCRS() {
		zoomLevels = null;
		name = null;
		baseCRS = null;
	}

	public TiledCRS(CRS baseCRS, String name, ScaleLevelsSpec.ZoomLevelsPix zoomLevels) {
		this(baseCRS, name, zoomLevels, POSITIVE, NEGATIVE);
	}

	public TiledCRS(CRS baseCRS, String name, ScaleLevelsSpec.ZoomLevelsPix zoomLevels, AxisSign colSign, AxisSign rowSign) {
		this.zoomLevels = zoomLevels;
		this.baseCRS = baseCRS;
		this.name = name;
		this.colSign = colSign;
		this.rowSign = rowSign;
	}

	public DisplayBounds getBounds() {
		if (bounds == null) {
			updateTransient();
		}
		return bounds;
	}

	public int maxColumn(int scaleId) {
		return tileMatrixWidth(scaleId) - 1;
	}

	public int maxRow(int scaleId) {
		return tileMatrixHeight(scaleId) - 1;
	}

	public boolean isValidTile(int scale, int column, int row) {
		return row >= 0 && column >= 0 && column <= maxColumn(scale) && row <= maxRow(scale);
	}

	public final int tileColumn(double worldX, int level) {
		return (int)dTileColumn(worldX, level);
	}

	public final double dTileColumn(double worldX, int level) {
		return colSign.dIndex(worldX, tileSizeInPix(level).w() * worldPerPix(level), worldTilesOrigin(level).x());
	}

	public final int tileRow(double worldY, int level) {
		return (int)dTileRow(worldY, level);
	}

	public final double dTileRow(double worldY, int level) {
		return rowSign.dIndex(worldY, tileSizeInPix(level).h() * worldPerPix(level), worldTilesOrigin(level).y());
	}

	private double worldPerPix(int level) {
		return zoomLevels.worldPerPix(level);
	}

	public double tileLeft(final int level, final int column) {
		return colSign.tileMinWorld(column, worldTilesOrigin(level).x(), worldPerPix(level) * tileSizeInPix(level).w());
	}

	public double tileRight(int zoomLevel, int column) {
		return tileLeft(zoomLevel, colSign.nextRightOrUp(column));
	}

	public double tileTop(int zoomLevel, int row) {
		return tileBottom(zoomLevel, rowSign.nextRightOrUp(row));
	}

	public double tileBottom(int level, int row) {
		return rowSign.tileMinWorld(row,  worldTilesOrigin(level).y(), worldPerPix(level) *tileSizeInPix(level).h());
	}

	public char getTilePrefixChar() {
		return tilePrefix;
	}

	public TiledCRS setTilePrefixChar(char ch) {
		this.tilePrefix = ch;
		return this;
	}

	/**
	 * @return ID of the least detailed level (the one with maximal worldLenPerPix)
	 */
	public int getMinLevelId() {
		return zoomLevels.getMinLevelId();
	}

	/**
	 * @return ID of the most detailed level (the one with minimal worldLenPerPix)
	 */
	public int getMaxLevelId() {
		return zoomLevels.getMaxLevelId();
	}

	/**
	 * @return x ordinate of the left edge of the leftmost pixel
	 */
	public double getMinX() {
		return getBounds().mbr.getMinX();
	}

	/**
	 * @return y ordinate of the bottom edge of the bottom-most pixel
	 */
	public double getMinY() {
		return getBounds().mbr.getMinY();
	}

	public boolean isColumnLeftToRight() {
		return colSign == POSITIVE;
	}

	public boolean isRowBottomToTop() {
		return rowSign == POSITIVE;
	}

	/**
	 * Naming of tiles using interlaced x,y hex representation: <tt>
	 * x = [x1][x2][x3]
	 * y = [y1][y2][y3]
	 * </tt>
	 * <ul>
	 * <li>tileName non-interlaced: <tt>[char][zoom][x1][x2][x3][y1][y2][y3].[suffix]</tt></li>
	 * <li>tileName interlaced: <tt>[char][zoom][x1][y1][x2][y2][x3][y3].[suffix]</tt></li>
	 * </ul>
	 * 
	 * @param interlaced
	 */
	public TiledCRS setInterlacedName(boolean interlaced) {
		this.interlacedName = interlaced;
		return this;
	}

	/**
	 * @see #setInterlacedName(boolean)
	 * @return
	 */
	public boolean isInterlacedName() {
		return interlacedName;
	}

	public TiledRegion regionForWorldEnvelope(Envelope wEnv, int level) {
		if (wEnv.isEmpty()) {
			return TiledRegion.createEmpty();
		}
		double inOff = insideOffsetToPreventRoundoffErrors(level);
		DimI tSize = tileSizeInPix(level);
		int w = tSize.w();
		int h = tSize.h();

		double leftCol = dTileColumn(wEnv.getMinX() + inOff, level);
		double rightCol = dTileColumn(wEnv.getMaxX() - inOff, level);
		double bottomRow = dTileRow(wEnv.getMinY() + inOff, level);
		double topRow = dTileRow(wEnv.getMaxY() - inOff, level);

		int topRemaining = rowSign.tileStartPixels((int)Math.ceil(MathUtil.mod(topRow, 1) * h), h);
		int leftRemaining = colSign.tileEndPixels((int)Math.floor(MathUtil.mod(leftCol, 1) * w), w);
		int botRemaining = rowSign.tileEndPixels((int)Math.floor(MathUtil.mod(bottomRow, 1) * h), h);
		int rightRemaining = colSign.tileStartPixels((int)Math.ceil(MathUtil.mod(rightCol, 1) * w), w);

		EnvelopeI env = EnvelopeI.withPoints((int)leftCol, (int)bottomRow, (int)rightCol, (int)topRow);
				
		return new TiledRegion(level, tSize, env, new RectSideOffsetsI(topRemaining, leftRemaining, botRemaining, rightRemaining));
	}

	public EnvelopeI tilesInEnvelope(Envelope wMBR, int level) {
		return tilesInEnvelope(wMBR, level, 0);
	}
	public EnvelopeI tilesInEnvelope(Envelope wMBR, int level, int numExtraTiles) {
		final int maxC = maxColumn(level);
		final int maxR = maxRow(level);

		EnvelopeI.Builder ret = new EnvelopeI.Builder();

		double inOff = insideOffsetToPreventRoundoffErrors(level);
		ret.expandToInclude(tileColumn(wMBR.getMinX() + inOff, level), tileRow(wMBR.getMinY() + inOff, level));
		ret.expandToInclude(tileColumn(wMBR.getMaxX() - inOff, level), tileRow(wMBR.getMaxY() - inOff, level));
		ret.expand(numExtraTiles);
		ret.intersectWith(0, 0, maxC, maxR);
		return ret.getEnvelope();
	}

	private double insideOffsetToPreventRoundoffErrors(int level) {
		return worldPerPix(level) / 1677216.0;
	}

	public double worldTileWidth(int zoomLevel) {
		return tileSizeInPix(zoomLevel).w() * worldPerPix(zoomLevel);
	}

	public double worldTileHeight(int zoomLevel) {
		return tileSizeInPix(zoomLevel).h() * worldPerPix(zoomLevel);
	}


	public Envelope tileWorldBounds(int zoomLevel, int col, int row) {
		return new Envelope(tileLeft(zoomLevel, col), tileBottom(zoomLevel, row), tileRight(zoomLevel, col), tileTop(
			zoomLevel, row));
	}

	public Envelope tileWorldBounds(int level, EnvelopeI tBounds) {
		HasCoordinate origin = worldTilesOrigin(level);
		DimI size = tileSizeInPix(level);
		double resolution = worldPerPix(level);

		Interval<Double> xInt = colSign.tileWorldInterval(tBounds.getIntervalX(), origin.x(), size.w() * resolution);
		Interval<Double> yInt = rowSign.tileWorldInterval(tBounds.getIntervalY(), origin.y(), size.h() * resolution);
		return new Envelope(xInt, yInt);
	}

	public PointI tileForPoint(int zoomLevel, HasCoordinate point) {
		return new PointI(tileColumn(point.x(), zoomLevel), tileRow(point.y(), zoomLevel));
	}

	public boolean isCompatibleWith(TiledCRS tiledCRS) {
		return equals(tiledCRS);
	}

	public abstract TiledCRS createWithMaxLevel(int maxLevel);

	public abstract HasCoordinate worldTilesOrigin(int zoomLevel);

	public abstract int tileMatrixWidth(int zoomLevel);

	public abstract int tileMatrixHeight(int zoomLevel);

	public abstract DimI tileSizeInPix(int zoomLevel);

	public abstract boolean isQuad();

	public String tileName(int level, int column, int row) {
		return TileUtilGWT.tileNameForColRow(this, level, column, row);
	}

	public TiledCRS setOverlap(int t, int l, int b, int r) {
		overlap = RectSideOffsetsI.create(t, l, b, r);
		return this;
	}

	public RectSideOffsetsI getOverlap() {
		return overlap;
	}

	@Override
	public void updateAfterDeserialization() {
		colSign = determineSignForLegacyXML(colDirection, colSign, DEFAULT_COL_AXIS_SIGN);
		rowSign = determineSignForLegacyXML(rowDirection, rowSign, DEFAULT_ROW_AXIS_SIGN);
	}

	private static AxisSign determineSignForLegacyXML(int legacyDirectionInt, AxisSign curSignSet, AxisSign defaultSign) {
		if (legacyDirectionInt == defaultSign.asInt()) { //legacy value is default; do nothing
			return curSignSet;
		}
		if (curSignSet != defaultSign) { //XML provided non-default sign; do nothing
			return curSignSet;
		}
		return AxisSign.valueOf(legacyDirectionInt);
	}

	@Override
	public void updateBeforeSerialization() {
		updateDirectionInts();
	}

	private void updateDirectionInts() {
		colDirection = colSign.asInt();
		rowDirection = rowSign.asInt();
	}

	protected void updateTransient() {
		if (bounds == null) {
			bounds = new DisplayBounds.Pix();
		} else {
			bounds.clear();
		}
		bounds.setScaleBoundsPix(zoomLevels.minWorldPerPix(), zoomLevels.maxWorldPerPix());
		EnvelopeBuilder eb = new EnvelopeBuilder(); //TODO: Add base CRS to envelope builder once it's clear whether to use null or "Default" as "whatever CRS the map is using" 
		for (int i = zoomLevels.getMinLevelId(); i < zoomLevels.getMaxLevelId(); i++) {
			eb.expandToInclude(TileUtilGWT.tileMatrixBounds(this, i));
		}
		bounds.mbr = eb.getEnvelope();
		updateDirectionInts();
	}

	public static TiledCRS createDefault(CRS baseCRS, String name, double minX, double minY, int tileSize, int minScaleIdx) {
		return createDefault(baseCRS, name, minX, minY, tileSize, 0.125, 20, minScaleIdx);
	}
	
	public static TiledCRS createDefault(CRS baseCRS, String name, double minX, double minY, int tileSize, double pixSizeAtMaxScale, int maxScaleIdx, int minScaleIdx) {
		return createDefault(baseCRS, name, minX, minY, tileSize, ScaleLevelsSpec.createWithFactor2(pixSizeAtMaxScale, maxScaleIdx, minScaleIdx));
	}
	public static TiledCRS createDefault(CRS baseCRS, String name, double minX, double minY, int tileSize, ZoomLevelsPix zooms) {
		double maxLevelTile = zooms.maxWorldPerPix() * tileSize;
		Envelope env = Envelope.withSize(minX, minY, maxLevelTile, maxLevelTile);
		return new WithBounds(baseCRS, name, zooms, env, new DimI(tileSize, tileSize));
	}

	public static TiledCRS createWithCenter(CRS baseCRS, String name, double centX, double centY, int tileSize, int minScaleIdx) {
		ZoomLevelsPix zooms = ScaleLevelsSpec.createWithFactor2(0.125, 20, minScaleIdx);		
		double maxLevelTile = zooms.maxWorldPerPix() * tileSize;
		Envelope env = Envelope.withCenter(centX, centY, maxLevelTile, maxLevelTile);
		return new WithBounds(baseCRS, name, zooms, env, new DimI(tileSize, tileSize));
	}

	public static TiledCRS createWithCenter(CRS baseCRS, String name, double centX, double centY, int tileSize, int minScaleIdx, double minScaleWorldSize) {
		ZoomLevelsPix zooms = ScaleLevelsSpec.createWithFactor2(minScaleWorldSize/tileSize, minScaleIdx, 20, minScaleIdx);		
		Envelope env = Envelope.withCenter(centX, centY, minScaleWorldSize, minScaleWorldSize);
		return new WithBounds(baseCRS, name, zooms, env, new DimI(tileSize, tileSize));
	}
}