/*
 *
 */
package com.sinergise.common.geometry.util;

import java.util.Collection;

import com.sinergise.common.util.format.NumberFormatUtil;
import com.sinergise.common.util.format.NumberFormatter;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.HasCoordinate;

public class CoordUtil {
	public static enum GeogDirection {N(1),E(1),S(-1),W(-1);
		private final int sign;
		private GeogDirection(int sign) {
			this.sign = sign;
		}
		public int getSign() {
			return sign;
		}
	}
	
	public static final double PIX_SIZE_MICRO_SMALL = 200;
	public static final double PIX_SIZE_MICRO_MEDIUM = 250;
	public static final double PIX_SIZE_MICRO_96DPI = 264.5833333333333333333333333333333333333;
	public static final double PIX_SIZE_MICRO_OGC = 280;

	public static final double MICRONS_PER_INCH = 25400;
	
	public static final NumberFormatter scaleNumberFormat = NumberFormatUtil.create("0");

	public static double getDefaultPixSizeInMicrons() {
		return PIX_SIZE_MICRO_OGC;
	}

	public static double scale(double worldPerPix, double pixSizeInMicrons) {
		return worldPerPix * 1e6 / pixSizeInMicrons;
	}

	public static double worldPerPix(double scale, double pixSizeInMicrons) {
		return scale * pixSizeInMicrons / 1e6;
	}

	public static double worldPerDisp(double worldPerPix, double pixSizeInMicrons) {
		return worldPerPix * 1e6 / pixSizeInMicrons;
	}

	public static double dpi(double pixSizeInMicrons) {
		return MICRONS_PER_INCH / pixSizeInMicrons;// D/in= (D/um)*um/in
	}

	public static double[] toDoubleArray(Collection<? extends HasCoordinate> coords) {
		double[] dCoords = new double[coords.size() * 2];
		int i = 0;
		for (HasCoordinate c : coords) {
			dCoords[i++] = c.x();
			dCoords[i++] = c.y();
		}
		return dCoords;
	}

	public static double degFromDms(double d, GeogDirection dir) {
		return degFromDms(d, 0, 0, dir);
	}
	
	public static double degFromDms(double d, double m, GeogDirection dir) {
		return degFromDms(d, m, 0, dir);
	}
	
	public static double degFromDms(double d, double m, double s, GeogDirection dir) {
		return dir.getSign() * (s / 3600 + m / 60 + d);
	}
	
	public static double pixelsFromMMDPI(double mm, double dpi) {
		return mm * 1000 * dpi / MICRONS_PER_INCH;
	}
	
	public static DimI pixelSizeFromMMDPI(double widthMM, double heightMM, double dpi) {
		return DimI.round(pixelsFromMMDPI(widthMM, dpi), pixelsFromMMDPI(heightMM, dpi));
	}

	public static String toWKTString(double[] coords) {
		if (coords == null) {
			return "EMPTY";
		}
		StringBuilder sb = new StringBuilder("(");
		if (coords.length > 0) {
			sb.append(coords[0]).append(' ').append(coords[1]);
			for (int i = 2; i < coords.length; i++) {
				sb.append(", ").append(coords[i++]).append(' ').append(coords[i]);
			}
		}
		return sb.append(')').toString();
	}
	
	public static String formatScale(double scale, String scaleTitle) {
		if (scaleTitle == null){
			scaleTitle = "";
		}
		if (Double.isNaN(scale) || Double.isInfinite(scale)) {
			 return "";
		} else if (scale<1) {
			return scaleTitle + scaleNumberFormat.format(1/scale)+":1";
		} else {
			return scaleTitle + "1:"+scaleNumberFormat.format(scale);
		}
	}
}
