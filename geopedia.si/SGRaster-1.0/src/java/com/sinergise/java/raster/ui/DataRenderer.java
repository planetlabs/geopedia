package com.sinergise.java.raster.ui;

import static java.lang.Math.max;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.raster.dataraster.SGDataBank;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeL;
import com.sinergise.common.util.geom.RectSideOffsetsI;
import com.sinergise.java.raster.core.OffsetBufferedImage;
import com.sinergise.java.raster.core.WorldRasterImage;
import com.sinergise.java.raster.ui.ColorInterpolator.ColorVal;

public class DataRenderer {

	public static OffsetBufferedImage renderTile(SGDataBank sdb, Envelope tileWorldEnv, DataRenderSettings settings) {
		RectSideOffsetsI xtraSpaceInSdb = getXtraSpaceInSdb(sdb, tileWorldEnv);
		WorldRasterImage img;
		try {
			img = renderData(sdb, xtraSpaceInSdb, settings);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return convertToTile(img, tileWorldEnv.getMinX(), tileWorldEnv.getMaxY());
	}
	
	public static RectSideOffsetsI getXtraSpaceInSdb(SGDataBank sdb, Envelope tileWorldEnv) {
		AffineTransform2D tr = sdb.getWorldTr();
		Envelope tBnds = tileWorldEnv.translate(-tr.getTranslateX(), -tr.getTranslateY());
		double scaleX = tr.getScaleX();
		double scaleY = tr.getScaleY();
		return new RectSideOffsetsI(
				(int)max(0, sdb.getEnvelope().getMaxY() - (tBnds.getMaxY()/scaleY - 0.5)), 
				(int)max(0, (tBnds.getMinX()/scaleX + 0.5) - sdb.getEnvelope().getMinX()),
				(int)max(0, (tBnds.getMinY()/scaleY + 0.5) - sdb.getEnvelope().getMinY()),
				(int)max(0, sdb.getEnvelope().getMaxX() - (tBnds.getMaxX()/scaleX - 0.5))
		);
	}
	
	public static OffsetBufferedImage convertToTile(WorldRasterImage img, double leftX, double topY) {
		int offX = (int)Math.round((img.wEnv.getMinX() - leftX) / img.wEnv.getWidth() * img.w);
		int offY = (int)Math.round((topY - img.wEnv.getMaxY()) / img.wEnv.getHeight() * img.h);
		return new OffsetBufferedImage(img.getAsBufferedImage(), offX, offY);
	}
	
	public static WorldRasterImage renderData(SGDataBank data, RectSideOffsetsI insets, DataRenderSettings settings) {
		return renderData(data, insets, settings.prepare());
	}
	
	private static WorldRasterImage renderData(SGDataBank sdb, RectSideOffsetsI insets, DataShader shader) {
		int x0 = insets.l();
		int maxX = sdb.getWidth() - insets.r() - 1;
		long y0 = sdb.getEnvelope().getMinY() + insets.b();
		long maxY = sdb.getEnvelope().getMaxY() - insets.t();
		
		BufferedImage bi = new BufferedImage(maxX - x0 + 1, (int)(maxY - y0 + 1), BufferedImage.TYPE_INT_ARGB);
		int[] bufImgBank = ((DataBufferInt)bi.getRaster().getDataBuffer()).getData();

		int off = 0;
		
		// top to bottom
		for (long botY = maxY; botY >= y0; botY--) {
			double[] row = getRow(sdb, botY, new double[sdb.getWidth()]);
			off = renderPixels(shader, x0, maxX, row, bufImgBank, off);
		}
		
		WorldRasterImage ret = DEMRenderer.createResult(sdb, insets, bi);
		return ret;
	}
	
	public static double[] getRow(SGDataBank sdb, long row, double[] out) {
		EnvelopeL env = sdb.getEnvelope();
		long off = env.getMinX();
		for (int i = 0; i < out.length; i++) {
			out[i] = sdb.getValue(off + i, row);
		}
		return out;
	}
	
	private static int renderPixels(DataShader shader, int x0, int maxX, double[] data, int[] bufImgBank, int off) {
		for (int x = x0; x <= maxX; x++) {
			bufImgBank[off++] = shader.getColor(data[x]).toIntColor();
		}
		return off;
	}
	
	public static class DataRenderSettings {
		public final ElevationColors hypsometry;
	
		public DataRenderSettings(ElevationColors clrs) {
			this.hypsometry = clrs;
		}
	
		public DataShader prepare() {
			return new DataShader(hypsometry);
		}
		
		public int getPlaneNaNColor() {
			return hypsometry.colorWhenEmpty;
		}
	}
	
	public static final class DataShader {
		private final ColorInterpolator hypso;
		private final ColorVal nanColor;

		public DataShader(ElevationColors hypso) {
			this.hypso = hypso.createInterpolator();
			this.nanColor = new ColorVal(hypso.colorWhenEmpty);
		}		

		private final ColorVal getColor(double value) {
			if(Double.isNaN(value)) {
				return nanColor;
			}
			return hypso.getInterpolatedValue(value,  new ColorVal());
		}
	}
}
