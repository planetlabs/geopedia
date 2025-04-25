package com.sinergise.geopedia.core.common;

import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.ThemeTableLink;

public class TileUtil {
	public static void appendLayerSpec(Table layer, int on, StringBuffer buf) {
		buf.append('l');
		buf.append(layer.id);
		buf.append('@');		
		buf.append(layer.lastDataWrite);
		buf.append(':');
		appendLfst(buf, on);
	}

	private static void appendLfst(StringBuffer buf, int on) {
		// if ((on & ThemeTableLink.ON_LINE) > 0)
		// buf.append('l');
		// if ((on & ThemeTableLink.ON_FILL) > 0)
		// buf.append('f');
		// if ((on & ThemeTableLink.ON_SYMBOL) > 0)
		// buf.append('s');
		// if ((on & ThemeTableLink.ON_TEXT) > 0)
		// buf.append('t');
		buf.append("lsft");
	}

	public static void appendThemeLayers(Theme th, StringBuffer buf) {
		boolean comma = false;
		for (int i = th.tables.length - 1; i >= 0; i--) {
			ThemeTableLink ttl = th.tables[i];
			if (ttl.isOn()) {
				if (comma)
					buf.append(',');
				if (ttl.id == 0 && ttl.table != null) {
					appendLayerSpec(ttl.table, ttl.on, buf);
				} else {
					appendThemeLayerSpec(ttl, ttl.on, buf);
				}
				comma = true;
			}
		}
	}

	public static void appendThemeLayerSpec(ThemeTableLink link, int on,
			StringBuffer buf) {
		buf.append('t');
		buf.append(link.themeId);
		buf.append('@');
		if (link.theme != null) {			
			buf.append(link.theme.lastMetaChange);
		} else {
			buf.append(0);
		}
		buf.append('.');
		buf.append(link.id);
		buf.append('.');
		buf.append(link.tableId);
		buf.append('@');
		if (link.table == null) {
			buf.append(0);
		} else {
			buf.append(link.table.lastDataWrite);
		}
		buf.append(':');
		appendLfst(buf, on);
	}

	/**** tiled CRS related */
	static final char[] numChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
			.toCharArray();

	public static char tileLevelCharFromZoomLevel(TiledCRS tiledCRS,
			int zoomLevel) {
		if (zoomLevel >= tiledCRS.getMinLevelId()
				&& zoomLevel <= tiledCRS.getMaxLevelId()) {
			return numChars[zoomLevel];
		}
		throw new IllegalArgumentException("Zoom level " + zoomLevel
				+ " is not valid.");
	}

	public static int zoomLevelFromTileLevelChar(char c, TiledCRS tiledCRS) {
		int ret = -1;

		if (Character.isDigit(c))
			ret = c - '0';
		else
			ret = 10 + (c - 'A');

		if (ret >= tiledCRS.getMinLevelId() && ret <= tiledCRS.getMaxLevelId())
			return ret;
		throw new IllegalArgumentException("Zoom level '" + c
				+ "' is not valid.");
	}

	public static double scaleXWorldToScreen(double pixSize, TiledCRS tiledCRS) {
		if (tiledCRS.isColumnLeftToRight())
			return 1.0 / pixSize;
		else
			return -1.0 / pixSize;
	}

	public static double scaleYWorldToScreen(double pixSize, TiledCRS tiledCRS) {
		if (tiledCRS.isRowBottomToTop())
			return -1.0 / pixSize;
		else
			return 1.0 / pixSize;
	}

	public static double translateXWorldToScreen(double pixSize, double xmin,
			double xmax, TiledCRS tiledCRS) {
		if (tiledCRS.isColumnLeftToRight())
			return -xmin / pixSize;
		else 
			return xmax / pixSize; // ?
	}

	public static double translateYWorldToScreen(double pixSize, double ymin,
			double ymax, TiledCRS tiledCRS) {
		if (tiledCRS.isRowBottomToTop())
			return ymax / pixSize;
		else
	       return  -ymin / pixSize;

	}

}
