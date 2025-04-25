package com.sinergise.geopedia.rendering;

import java.awt.Color;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import com.sinergise.geopedia.core.symbology.FillSymbolizer.GPFillType;

public class TextureMaker
{
	public static int midColor(int c0, int c1)
	{
		int a0 = 255 & (c0 >>> 24);
		int a1 = 255 & (c1 >>> 24);
		
		int r0 = 255 & (c0 >>> 16);
		int r1 = 255 & (c1 >>> 16);
		int g0 = 255 & (c0 >>>  8);
		int g1 = 255 & (c1 >>>  8);
		int b0 = 255 & c0;
		int b1 = 255 & c1;

		if ((a0 | a1) == 0)
			return 0;
		
		double d = 1.0 / (a0 + a1);
		int r = (int) Math.round((a0*r0 + a1*r1) * d);
		int g = (int) Math.round((a0*g0 + a1*g1) * d);
		int b = (int) Math.round((a0*b0 + a1*b1) * d);
		int a = (int) Math.round((a0 + a1) * 0.5);

		return (a << 24) | (r << 16) | (g << 8) | b;
	}
	
	private static final char[][] midPixels = {
		null, // NONE = 0;
		null, // SOLID = 1;
		"\u0001\u000c\u0010\u001b*/9>HMW\\fkuz\u0084\u0089\u0093\u0098\u00a2\u00a7\u00b1\u00b6\u00c0\u00c5\u00d4\u00df\u00e3\u00ee\u00f2\u00fd".toCharArray(), // SLASHES = 2;
		"\u0002\r\u0013\u001e$/05AFRWchty\u0085\u008a\u0096\u009b\u00a7\u00ac\u00b8\u00bd\u00c9\u00ce\u00da\u00df\u00e0\u00eb\u00f1\u00fc".toCharArray(), // BACKSLASHES = 3;
		null, // HOR_LINES = 4;
		null, // VER_LINES = 5;
		"VYej\u0095\u009a\u00a6\u00a9".toCharArray(), // DOTS = 6;
		null, // GRID = 7;
		"\u0003\r\u0010\u0014\u001c!%+/26:>CGIMTX\\ekvz\u0085\u008b\u0094\u0098\u009c\u00a3\u00a7\u00a9\u00ad\u00b2\u00b6\u00ba\u00be\u00c1\u00c5\u00cb\u00cf\u00d0\u00d4\u00dc\u00e3\u00ed".toCharArray(), // DIAG_GRID = 8;
		"7VXuy\u0094\u009a".toCharArray(), // TRIANGLES = 9
	};
	private static final char[][] fgPixels = {
		null, // NONE = 0;
		null, // SOLID = 1;
		"\u0000\r\u000e\u000f\u001c\u001d\u001e\u001f+,-.:;<=IJKLXYZ[ghijvwxy\u0085\u0086\u0087\u0088\u0094\u0095\u0096\u0097\u00a3\u00a4\u00a5\u00a6\u00b2\u00b3\u00b4\u00b5\u00c1\u00c2\u00c3\u00c4\u00d0\u00d1\u00d2\u00d3\u00e0\u00e1\u00e2\u00ef\u00f0\u00f1\u00fe\u00ff".toCharArray(), // SLASHES = 2;
		"\u0000\u0001\u000e\u000f\u0010\u0011\u0012\u001f !\"#1234BCDESTUVdefguvwx\u0086\u0087\u0088\u0089\u0097\u0098\u0099\u009a\u00a8\u00a9\u00aa\u00ab\u00b9\u00ba\u00bb\u00bc\u00ca\u00cb\u00cc\u00cd\u00db\u00dc\u00dd\u00de\u00ec\u00ed\u00ee\u00ef\u00f0\u00fd\u00fe\u00ff".toCharArray(), // BACKSLASHES = 3;
		"`abcdefghijklmnopqrstuvwxyz{|}~\u0080\u0081\u0082\u0083\u0084\u0085\u0086\u0087\u0088\u0089\u008a\u008b\u008c\u008d\u008e\u008f".toCharArray(), // HOR_LINES = 4;
		"\u0006\u0007\u0008\u0016\u0017\u0018&'(678FGHVWXfghvwx\u0086\u0087\u0088\u0096\u0097\u0098\u00a6\u00a7\u00a8\u00b6\u00b7\u00b8\u00c6\u00c7\u00c8\u00d6\u00d7\u00d8\u00e6\u00e7\u00e8\u00f6\u00f7\u00f8".toCharArray(), // VER_LINES = 5;
		"WXfghiuvwxyz\u0085\u0086\u0087\u0088\u0089\u008a\u0096\u0097\u0098\u0099\u00a7\u00a8".toCharArray(), // DOTS = 6;
		"\u0006\u0007\u0008\u0016\u0017\u0018&'(678FGHVWXfghpqrstuvwxyz{|}~\u0080\u0081\u0082\u0083\u0084\u0085\u0086\u0087\u0088\u0089\u008a\u008b\u008c\u008d\u008e\u008f\u0090\u0091\u0092\u0093\u0094\u0095\u0096\u0097\u0098\u0099\u009a\u009b\u009c\u009d\u009e\u009f\u00a6\u00a7\u00a8\u00b6\u00b7\u00b8\u00c6\u00c7\u00c8\u00d6\u00d7\u00d8\u00e6\u00e7\u00e8\u00f6\u00f7\u00f8".toCharArray(), // GRID = 7;
		"\u0000\u0001\u0002\u000e\u000f\u0011\u0012\u0013\u001d\u001e\u001f\"#$,-.345;<=DEFJKLUVWYZ[fghijwxy\u0086\u0087\u0088\u0089\u008a\u0095\u0096\u0097\u0099\u009a\u009b\u00a4\u00a5\u00a6\u00aa\u00ab\u00ac\u00b3\u00b4\u00b5\u00bb\u00bc\u00bd\u00c2\u00c3\u00c4\u00cc\u00cd\u00ce\u00d1\u00d2\u00d3\u00dd\u00de\u00df\u00e0\u00e1\u00e2\u00ee\u00ef\u00f0\u00f1\u00f2\u00fe\u00ff".toCharArray(), // DIAG_GRID = 8;
		"GWfghvwx\u0085\u0086\u0087\u0088\u0089\u0095\u0096\u0097\u0098\u0099\u00a4\u00a5\u00a6\u00a7\u00a8\u00a9\u00aa".toCharArray(), // TRIANGLES = 9
	};
	
	private static final Rectangle2D.Double textureBounds = new Rectangle2D.Double(0, 0, 16, 16);
	
	public static Paint getPaint(int bgColor, int fgColor, GPFillType fillType)
	{
		if (fillType == GPFillType.SOLID)
			return new Color(bgColor, true);
		
		BufferedImage img = makeFillImage(bgColor, fgColor, fillType);
		
		return new TexturePaint(img, textureBounds);
	}
	
	public static BufferedImage makeFillImage(int bgColor, int fgColor, GPFillType fillType)
	{
		int[] data = GraphicsUtils.allocImage(16, 16);
		
		DirectColorModel cm;
        int[] bandmasks;
        if (((fgColor & bgColor) >>> 24) == 255) {
        	cm = GraphicsUtils.opaqueColorModel;
        	bandmasks = GraphicsUtils.opaqueBandMasks;
        } else {
        	cm = GraphicsUtils.transColorModel;
        	bandmasks = GraphicsUtils.transBandMasks;
        }
        
        DataBufferInt dbi = new DataBufferInt(data, 256);
        WritableRaster raster = Raster.createPackedRaster(dbi, 16, 16, 16, bandmasks, null);
		BufferedImage res = new BufferedImage(cm, raster, false, null);

		for (int a=0; a<16; a++)
			data[a] = bgColor;
		
		System.arraycopy(data, 0, data, 16, 16); // 32
		System.arraycopy(data, 0, data, 32, 32); // 64
		System.arraycopy(data, 0, data, 64, 64); // 128
		System.arraycopy(data, 0, data, 128, 128); // 256

		char[] mid = midPixels[fillType.ordinal()];
		if (mid != null) {
			int midColor = midColor(bgColor, fgColor);
			for (int i : mid)
				data[i] = midColor;
		}
		
		char[] fg = fgPixels[fillType.ordinal()];
		if (fg != null)
			for (int i : fg)
				data[i] = fgColor;
		
		return res;
	}
}
