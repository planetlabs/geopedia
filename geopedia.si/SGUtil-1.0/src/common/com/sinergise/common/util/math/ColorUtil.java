/*
 *
 */
package com.sinergise.common.util.math;

import java.awt.Color;
import java.util.Arrays;

import com.sinergise.common.util.string.StringUtil;

public class ColorUtil {
	public static final double[][] RGBToYIQ   = {{0.299, 0.587, 0.114}, {0.595716, -0.274453, -0.321263}, {0.211456, -0.522591, 0.311145}};
	public static final double[][] YIQToRGB   = {{1, 0.9563, 0.6210}, {1, -0.2721, -0.6474}, {1, -1.1070, 1.7046}};
	
	public static final double[][] RGBToYCrCb = {{0.299, 0.587, 0.114, 0}, {-0.168736, -0.331264, 0.5, 128}, {0.5, -0.418688, -0.081312, 128}};
	
	public static final double[][] YCrCbToRGB = {{1., 0, 1.402, -179.456}, {1., -0.344136, -0.714136, 135.459}, {1., 1.772, 0, -226.816}};
	public static final int	TRANSPARENT	= 0;
	
	
	public static final Color TRANSPARENT_COLOR	= new Color(0, true);
	public static final Color TRANSPARENT_WHITE	= new Color(0x00FFFFFF, true);
	
	public static Color colorFromIntArray(final int[] src4Int) {
		return new Color(src4Int[0], src4Int[1], src4Int[2], src4Int[3]);
	}
	
	public static void toIntArray(final Color color, final int[] dest4Int) {
		dest4Int[0] = color.getRed();
		dest4Int[1] = color.getGreen();
		dest4Int[2] = color.getBlue();
		dest4Int[3] = color.getAlpha();
	}
	
	public static final int YIQtoRGB(final float[] YIQ) {
		int ret = (0xff & (int)(255 * dot(YIQToRGB[0], YIQ)));
		ret = ((ret << 8) | (0xff & (int)(255 * dot(YIQToRGB[1], YIQ))));
		ret = ((ret << 8) | (0xff & (int)(255 * dot(YIQToRGB[2], YIQ))));
		return ret;
	}
	
	public static final float[] RGBtoYIQ(final int r, final int g, final int b, final float[] yiq) {
		final float[] ret = ((yiq == null) ? new float[3] : yiq);
		final float[] rgb = new float[]{r / 255f, g / 255f, b / 255f};
		ret[0] = (float)dot(RGBToYIQ[0], rgb);
		ret[1] = (float)dot(RGBToYIQ[1], rgb);
		ret[2] = (float)dot(RGBToYIQ[2], rgb);
		return ret;
	}
	
	public static final int subtractWhite(int srcARGB, double amount, double gamma) {
		final int b = srcARGB & 0xFF;
		final int g = (srcARGB >>> 8) & 0xFF;
		final int r = (srcARGB >>> 16) & 0xFF;
		final int a = (srcARGB >>> 24) & 0xFF;

		int min = r < g ? (r < b ? r : b) : (g < b ? g : b);
		if (amount < 1) min = (int)Math.round(amount * min);

		if (gamma > 1) {
			if (gamma == 2) min = (int)((double)min * min / 255);
			else if (gamma == 3) min = (int)((double)min * min * min / 255 / 255);
			else min = (int)(255 * Math.pow((double)min / 255, gamma));
		}
		if (min == 255 || a == 0) return 0;
		
		final double invA = 255.0 / (255 - min);
		return ((int)((b - min) * invA)) | ((int)((g - min) * invA) << 8) | ((int)((r - min) * invA) << 16) | ((int)(a / invA) << 24);
	}
	
	public static final void subtractWhite(int[] rgba, double amount, double gamma) {
		final int r = rgba[0];
		final int g = rgba[1];
		final int b = rgba[2];
		final int a = rgba[3];

		int min = r < g ? (r < b ? r : b) : (g < b ? g : b);
		if (amount < 1) min = (int)Math.round(amount * min);

		if (gamma > 1) {
			if (gamma == 2) min = (int)((double)min * min / 255);
			else if (gamma == 3) min = (int)((double)min * min * min / 255 / 255);
			else min = (int)(255 * Math.pow((double)min / 255, gamma));
		}
		if (min == 255 || a == 0) {
			rgba[3] = 0;
			return;
		}
		final double invA = 1.0 / (1.0 - min / 255.0);
		rgba[3] = (int)(a / invA);
		rgba[0] = (int)((r - min) * invA);
		rgba[1] = (int)((g - min) * invA);
		rgba[2] = (int)((b - min) * invA);
	}
	
	public static final int YCrCbtoRGB(final int[] YCrCb) {
		final double[] ydbl = new double[]{YCrCb[0], YCrCb[1], YCrCb[2], 1};
		int ret = (0xff & clipInt(dot(YCrCbToRGB[0], ydbl), 0, 255));
		ret = ((ret << 8) | (0xff & clipInt(dot(YCrCbToRGB[1], ydbl), 0, 255)));
		ret = ((ret << 8) | (0xff & clipInt(dot(YCrCbToRGB[2], ydbl), 0, 255)));
		return ret;
	}
	
	public static final int[] RGBtoYCrCb(final int r, final int g, final int b, final int[] yCrCb) {
		final int[] ret = ((yCrCb == null) ? new int[3] : yCrCb);
		final double[] rgb = new double[]{r, g, b, 1};
		ret[0] = clipInt(dot(RGBToYCrCb[0], rgb), 0, 255);
		ret[1] = clipInt(dot(RGBToYCrCb[1], rgb), 0, 255);
		ret[2] = clipInt(dot(RGBToYCrCb[2], rgb), 0, 255);
		return ret;
	}
	
	public static final int RGBToGray(final int r, final int g, final int b) {
		return clipInt(dot(RGBToYCrCb[0], new float[]{r, g, b, 1}), 0, 255);
	}
	
	public static final int RGBToGray(final int rgb) {
		return clipInt(dot(RGBToYCrCb[0], new float[]{(rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff, 1}), 0, 255);
	}
	
	/**
	 * @param contrastValue [-1 .. 1]
	 * @return factor used to multiply values for contrast adjustment
	 */
	public static final double contrastFactor(final double contrastValue) {
		if (contrastValue > 0) {
			return 1.0001 / (1.0001 - contrastValue);
		}
		return 1 + contrastValue;
	}
	
	public static final int clipInt(final double a, final int min, final int max) {
		return (a < min) ? min : (a > (max + 1)) ? max : (int)a;
	}
	
	public static final double dot(final double[] a, final float[] b) {
		double ret = 0;
		for (int i = 0; i < a.length; i++) {
			ret += a[i] * b[i];
		}
		return ret;
	}
	
	public static final double dot(final double[] a, final double[] b) {
		double ret = 0;
		for (int i = 0; i < a.length; i++) {
			ret += a[i] * b[i];
		}
		return ret;
	}
	
	public static final int sqr(final int a) {
		return a * a;
	}
	
	public static float clip(final float f, final float min, final float max) {
		return f < min ? min : f > max ? max : f;
	}
	
	public static int clip(final int v, final int min, final int max) {
		return v < min ? min : v > max ? max : v;
	}
	
	public static double clip(final double v, final double min, final double max) {
		return v < min ? min : v > max ? max : v;
	}
	
	public static int rgbaFromARGB(final int argb) {
		return (argb << 8) | (argb >>> 24);
	}
	
	public static void toIntArray(final int src4Byte, final int[] dest4Int) {
		dest4Int[3] = (src4Byte) & 0xFF;
		dest4Int[2] = (src4Byte >>> 8) & 0xFF;
		dest4Int[1] = (src4Byte >>> 16) & 0xFF;
		dest4Int[0] = (src4Byte >>> 24) & 0xFF;
	}
	
	public static int fromIntArray(final int[] src4Int) {
		int ret = src4Int[0] & 0xff;
		ret = (ret << 8) | (src4Int[1] & 0xff);
		ret = (ret << 8) | (src4Int[2] & 0xff);
		ret = (ret << 8) | (src4Int[3] & 0xff);
		return ret;
	}
	
	public static int argbFromRGBA(final int rgba) {
		return (rgba << 24) | (rgba >>> 8);
	}
	
	public static int colorOver(final int baseColor, final int overColor) {
		final int sa = overColor >>> 24;
		final int ta = baseColor >>> 24;
		final int invSa = 255 - sa;
		final int outA = (ta == 255) ? 255 : (sa + ta - ((sa * ta) >>> 8));
		int ret = 0;
		if (outA > 0) {
			ret = outA;
			ret = (ret << 8) + (((overColor >>> 16)	& 0xff) * sa + ((((baseColor >>> 16)	& 0xff) * invSa * ta) >>> 8)) / outA;
			ret = (ret << 8) + (((overColor >>> 8) 	& 0xff) * sa + ((((baseColor >>> 8) 	& 0xff) * invSa * ta) >>> 8)) / outA;
			ret = (ret << 8) + ((overColor 			& 0xff) * sa + (((baseColor 			& 0xff) * invSa * ta) >>> 8)) / outA;
		}
		return ret;
	}
	
	public static void colorOver(final double[] baseC, final double[] overC, final double[] outC) {
		final double oa = overC[0];
		if (oa == 1) {
			copyIfDifferent(overC, outC);
			return;
		}
		if (oa == 0) {
			copyIfDifferent(baseC, outC);
			return;
		}
		final double ba = baseC[0];
		final double outA = (ba == 1) ? 1 : (oa + ba - oa * ba);
		final double invSaTa = (1.0 - oa) * ba;
		if (outA > 0) {
			outC[0] = outA;
			outC[1] = (overC[1] * oa + baseC[1] * invSaTa) / outA;
			outC[2] = (overC[2] * oa + baseC[2] * invSaTa) / outA;
			outC[3] = (overC[3] * oa + baseC[3] * invSaTa) / outA;
			for (int i = 0; i < 4; i++) {
				if (outC[i] < 0 || 1 < outC[i]) {
					System.out.println("ERR "+Arrays.toString(outC));
				}
			}
		} else {
			copyIfDifferent(baseC, outC);
			return;
		}
	}

	public static void copyIfDifferent(final Object src, final Object tgt) {
		if (tgt != src) {
			System.arraycopy(src, 0, tgt, 0, 4);
		}
	}
	
	/**
	 * @return double [0,1) for hue; red is 0; grey yields NaN;
	 */
	public static float hue(final float r, final float g, final float b) {
		if (r >= g) {
			if (g >= b) {
				if (r == b) {
					return Float.NaN;
				}
				return (g - b) / (r - b) / 6;
			} else if (r >= b) {
				return (6 - (b - g) / (r - g)) / 6;
			} else {
				return (4 + (r - g) / (b - g)) / 6;
			}
		} else if (r >= b) {
			return (2 - (r - b) / (g - b)) / 6;
		} else if (g >= b) {
			return (2 + (b - r) / (g - r)) / 6;
		} else {
			return (4 - (g - r) / (b - r)) / 6;
		}
	}
	
	/**
	 * @param h1
	 * @param h2
	 * @return hue delta (h1-h2) in the range [-0.5, 0.5)
	 */
	public static float hueDelta(final float h1, final float h2) {
		final float ret = h1 - h2;
		if (Double.isNaN(ret)) {
			return 0;
		}
		if (ret > 0.5) {
			return ret - 1;
		}
		if (ret < -0.5) {
			return 1 + ret;
		}
		return ret;
	}
	
	public static void main(final String[] args) {
		// System.out.println(Integer.toHexString(argbFromRGBA(0xffffff00)));
		// System.out.println(Integer.toHexString(argbFromRGBA(0xaabbccdd)));
		// System.out.println(Integer.toHexString(rgbaFromARGB(0x00ffffff)));
		// System.out.println(Integer.toHexString(rgbaFromARGB(0xddaabbcc)));
		// int[] ret = new int[4];
		// toIntArray(0xaabbccdd, ret);
		// for (int i = 0; i < ret.length; i++) {
		// System.out.println(Integer.toHexString(ret[i]));
		// }
		System.out.println(hue(1, 1, 1));
		System.out.println(hue(1, 0, 1));
		System.out.println(hue(240, 20, 240));
		System.out.println(hueDelta(0.1f, 0.9f));
	}

	public static int fromDouble4(double[] val0to1) {
		return (((int)(val0to1[0]*255.99))<<24) | (((int)(val0to1[1]*255.99))<<16) | (((int)(val0to1[2]*255.99))<<8) | ((int)(val0to1[3]*255.99));
	}
	public static double[] toDouble4(final int clr, final double[] ret) {
		ret[0] = (clr >>> 24 & 0xff)/255.0;
        ret[1] = (clr >>> 16 & 0xff)/255.0;
        ret[2] = (clr >>> 8  & 0xff)/255.0;
        ret[3] = (clr        & 0xff)/255.0;
        return ret;
	}

	public static String toHTMLColor(int rgb) {
		return "#" + StringUtil.padWith(Integer.toHexString(rgb & 0x00FFFFFF).toUpperCase(), '0', 6, true);
	}

	public static int mix(final int c1, final int c2, final double k) {
		final double k1 = 1.0 - k;
		final int ret0 = ((int)(k * ((c1 >>> 24) & 0xFF) + k1 * ((c2 >>> 24) & 0xFF) + 0.5) << 24) & 0xFF000000;
		final int ret1 = ((int)(k * ((c1 >>> 16) & 0xFF) + k1 * ((c2 >>> 16) & 0xFF) + 0.5) << 16) & 0xFF0000;
		final int ret2 = ((int)(k * ((c1 >>> 8) & 0xFF) + k1 * ((c2 >>> 8) & 0xFF) + 0.5) << 8) & 0xFF00;
		final int ret3 = ((int)(k * (c1 & 0xFF) + k1 * (c2 & 0xFF) + 0.5)) & 0xFF;
		return ret0 | ret1 | ret2 | ret3;
	}

	public static int unblend(int blendedARGB, int overARGB) {
		final int ba = (blendedARGB >>> 24) & 0xff;
		final int br = (blendedARGB >>> 16) & 0xff;
		final int bg = (blendedARGB >>>  8) & 0xff;
		final int bb = (blendedARGB       ) & 0xff;

		final int oa = (overARGB >>> 24) & 0xff;
		final int or = (overARGB >>> 16) & 0xff;
		final int og = (overARGB >>>  8) & 0xff;
		final int ob = (overARGB       ) & 0xff;
		
		
		final int fact = ba - oa;
		
		if (fact <= 0) return blendedARGB; //Can't unblend; blended is more transparent than over  
		
		final int sa = (((ba - oa) * 255) / (255 - oa));
		final int sr = MathUtil.clamp(0, ((ba * br - oa * or)/fact), 255);
		final int sg = MathUtil.clamp(0, ((ba * bg - oa * og)/fact), 255);
		final int sb = MathUtil.clamp(0, ((ba * bb - oa * ob)/fact), 255);
		
		return (sa << 24) | (sr << 16) | (sg << 8) | sb;
	}

	public static int unblend(int blendedRGB, int overRGB, double overAmount) {
		int ovrA = (int)(overAmount * 255.99);
		return unblend(0xff000000 | blendedRGB, (ovrA << 24) | overRGB);
	}
}
