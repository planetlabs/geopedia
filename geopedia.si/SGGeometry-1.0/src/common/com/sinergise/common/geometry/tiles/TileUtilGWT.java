/*
 *
 */
package com.sinergise.common.geometry.tiles;

import static com.sinergise.common.util.math.MathUtil.hexDigitValue;

import java.util.HashSet;

import com.sinergise.common.geometry.display.DisplayBounds;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeI;
import com.sinergise.common.util.geom.PointI;
import com.sinergise.common.util.math.MathUtil;


public class TileUtilGWT {
	public static final DimI TILE_SIZE_SMALL = new DimI(256, 256);
	public static final DimI TILE_SIZE_MEDIUM = new DimI(512, 512);
	public static final DimI TILE_SIZE_LARGE = new DimI(1024, 1024);
	public static final DimI TILE_SIZE_DEFAULT = TILE_SIZE_SMALL;

	public static final String FILENAME_TILEDCRS = "TiledCRS.xml";

	public static Envelope tileMatrixBounds(TiledCRS ts, int zoomLevel) {
		return ts.tileWorldBounds(zoomLevel, new EnvelopeI(0,0, ts.maxColumn(zoomLevel), ts.maxRow(zoomLevel)));
	}

	public static boolean intersects(DisplayBounds.Pix bounds, TiledCRS crs, int zoomLevel, int row, int column) {
		double wPerPix = crs.zoomLevels.worldPerPix(zoomLevel);
		if (!bounds.scaleIntersectsPix(wPerPix)) {
			return false;
		}
		if (bounds.mbr.isEmpty()) {
			return false;
		}
		double minX = crs.tileLeft(zoomLevel, column);
		if (bounds.mbr.getMaxX() < minX) {
			return false;
		}
		double minY = crs.tileBottom(zoomLevel, row);
		if (bounds.mbr.getMaxY() < minY) {
			return false;
		}
		double maxX = crs.tileRight(zoomLevel, column);
		if (bounds.mbr.getMinX() > maxX) {
			return false;
		}
		double maxY = crs.tileTop(zoomLevel, row);
		if (bounds.mbr.getMinY() > maxY) {
			return false;
		}
		return true;
	}

	static char[] numChars = MathUtil.NUMERAL_CHARS;
	public static final int MAX_POSSIBLE_LEVEL_ID = numChars.length-1; 
	
	static int[] numCharsPerOrd = new int[40];
	static {
		numCharsPerOrd[0] = 1;
		for (int i = 1; i < numCharsPerOrd.length; i++) {
			numCharsPerOrd[i] = Long.toHexString((1 << i) - 1).length();
		}
	}
	
	public static int numCharsPerOrdinateForLevel(int level, TiledCRS tiledCRS) {
		return numCharsPerOrdinateForLevel(level, tiledCRS.getMinLevelId());
	}
	public static int numCharsPerOrdinateForLevel(int level, int minLevel) {
		return numCharsPerOrd[level - minLevel];
	}
	public static String tileNameForColRow(TiledCRS crs, int zoomLevel, int col, int row) {
		if (crs.isInterlacedName()) return createTileNameInt(crs, zoomLevel, col, row);
		return createTileNameNonInt(crs, zoomLevel, col, row);
	}

	public static void tileFilesInEnvelope(TiledCRS crs, Envelope wMBR, int level, boolean lowerLevels, HashSet<String> ret) {
		EnvelopeI ei = crs.tilesInEnvelope(wMBR, level);
		for (PointI p : ei) {
			ret.add(TileUtilGWT.tileInDirColRow(crs, level, p.x, p.y));
		}
		if (lowerLevels && level > crs.getMinLevelId()) {
			tileFilesInEnvelope(crs, wMBR, level - 1, true, ret);
		}
	}


	private static String createTileNameInt(TiledCRS crs, int zoomLevel, int col, int row) {
		int nCharsPerOrdinate = numCharsPerOrdinateForLevel(zoomLevel, crs.zoomLevels.getMinLevelId());
		StringBuffer sb = new StringBuffer(2 + nCharsPerOrdinate * 2);
		sb.append(crs.getTilePrefixChar());
		sb.append(numChars[zoomLevel]);
		for (int xx = nCharsPerOrdinate - 1; xx >= 0; xx--) {
			sb.append(numChars[(col >>> (xx << 2)) & 15]);
			sb.append(numChars[(row >>> (xx << 2)) & 15]);
		}
		return sb.toString();
	}

	public static String tileDir(String tileName) {
		StringBuffer sb = new StringBuffer();
		int pos = 0;
		int len = tileName.lastIndexOf('.');
		if (len < 0) len = tileName.length();
		len -= 2;
		while (pos < len) {
			sb.append(tileName.charAt(pos++));
			sb.append(tileName.charAt(pos++));
			sb.append('/');
		}
		return sb.toString();
	}

	private static String createTileNameNonInt(TiledCRS crs, int level, int col, int row) {
		int nCharsPerOrdinate = numCharsPerOrdinateForLevel(level, crs.zoomLevels.getMinLevelId());
		StringBuffer sb = new StringBuffer(2 + nCharsPerOrdinate * 2);
		sb.append(crs.getTilePrefixChar());
		sb.append(numChars[level]);
		for (int xx = nCharsPerOrdinate - 1; xx >= 0; xx--) {
			sb.append(numChars[(col >>> (xx << 2)) & 15]);
		}
		for (int xx = nCharsPerOrdinate - 1; xx >= 0; xx--) {
			sb.append(numChars[(row >>> (xx << 2)) & 15]);
		}
		return sb.toString();
	}


	public static int zoomLevelIntFromChar(char c) {
		int ret = -1;
		if (Character.isDigit(c)) ret = c - '0';
		else ret = 10 + (c - 'A');
		return ret;
	}

	public static int zoomLevelFromTileLevelChar(TiledCRS space, char c) throws IllegalArgumentException {
		int ret = zoomLevelIntFromChar(c);
		if (ret >= space.zoomLevels.getMinLevelId() && ret <= space.zoomLevels.getMaxLevelId()) return ret;
		throw new IllegalArgumentException("Zoom level '" + c + "' is not valid (resolved to " + ret + ", min/max = " + space.zoomLevels.getMinLevelId() + "/"
				+ space.zoomLevels.getMaxLevelId() + ")");
	}

	public static char tileLevelCharFromZoomLevel(int scale) {
		return numChars[scale];
	}

	public static String tileInDirColRow(TiledCRS crs, int zoomLevel, int col, int row) {
		String tileId = tileNameForColRow(crs, zoomLevel, col, row);
		return tileInDir(tileId);
	}

	public static String tileInDir(String tileId) {
		StringBuffer sb = new StringBuffer();
		int pos = 0;
		int len = tileId.lastIndexOf('.');
		if (len < 0) len = tileId.length();
		len -= 2;
		while (pos < len) {
			sb.append(tileId.charAt(pos++));
			sb.append(tileId.charAt(pos++));
			sb.append('/');
		}
		sb.append(tileId);
		return sb.toString();
	}

	public static int parseTileSpec(TiledCRS space, String tileID, PointI posOut) {
		int tileLevel = zoomLevelFromTileLevelChar(space, tileID.charAt(1));
		parseIndex2D(tileID.substring(2), space.isInterlacedName(), posOut);
		return tileLevel;
	}
	
	public static PointI parseIndex2D(String hexIndex, boolean interlaced, PointI ret) {
		int x = 0;
		int y = 0;
		final int charCnt = hexIndex.length()/2;
		if (interlaced) {
			for (int i = 0; i < charCnt; i++) {
				x = (x<<4) + hexDigitValue(hexIndex.charAt(2*i));
				y = (y<<4) + hexDigitValue(hexIndex.charAt(2*i+1));
			}
		} else {
			for (int i = 0; i < charCnt; i++) {
				x = (x<<4) + hexDigitValue(hexIndex.charAt(i));
				y = (y<<4) + hexDigitValue(hexIndex.charAt(charCnt+i));
			}
		}
		if (ret == null) return new PointI(x,y);
		ret.setPos(x, y);
		return ret;
	}
}
