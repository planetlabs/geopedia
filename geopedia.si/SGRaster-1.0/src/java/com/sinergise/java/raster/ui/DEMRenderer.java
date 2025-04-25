package com.sinergise.java.raster.ui;

import static com.sinergise.java.raster.ui.DataRenderer.getRow;
import static java.lang.Double.isNaN;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.net.URL;

import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.raster.dataraster.SGDataBank;
import com.sinergise.common.raster.dataraster.ShortDataBank;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.Position2D;
import com.sinergise.common.util.geom.RectSideOffsetsI;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.java.raster.core.OffsetBufferedImage;
import com.sinergise.java.raster.core.RasterUtilJava;
import com.sinergise.java.raster.core.WorldRasterImage;
import com.sinergise.java.raster.dataraster.io.DataRasterIO;
import com.sinergise.java.raster.ui.ColorInterpolator.ColorVal;

public class DEMRenderer {
	private static final double SHADE_DARKEST = 0.05;
	private static final double SHADE_SPAN = 1 - SHADE_DARKEST;
	public static DEMRenderSettings DEFAULT_SHADING = new DEMRenderSettings(ElevationColors.DEFAULT);

	public static WorldRasterImage renderDEM(URL f) throws IOException {
		ShortDataBank sdb = DataRasterIO.load(f);
		return renderDEM(sdb);
	}
	
	public static WorldRasterImage createResult(SGDataBank sdb, final RectSideOffsetsI insets, BufferedImage bi) {
		AffineTransform2D srcTr = sdb.getWorldTr();
		Position2D topLeft = srcTr.point(sdb.getEnvelope().getMinX()+insets.l()-0.5, sdb.getEnvelope().getMaxY()-insets.t()+0.5);
		AffineTransform2D tr = AffineTransform2D.createTrScale(srcTr.getScaleX(), -srcTr.getScaleY(), topLeft.x, topLeft.y);
		return new WorldRasterImage(null, new DimI(bi.getWidth(), bi.getHeight()), RasterUtilJava.wrap(bi), tr);
	}

	private static double getMinDotProduct(double[] sunNormal) {
		return -Math.sqrt(1 - sunNormal[2] * sunNormal[2]);
	}
	
	private static double getPlaneShade(double sunNormal[]) {
		final double minS = getMinDotProduct(sunNormal);
		return SHADE_DARKEST + SHADE_SPAN * (sunNormal[2] - minS) / (1 - minS);
	}
	
	public static OffsetBufferedImage renderTile(SGDataBank sdb, Envelope tileWorldEnv, DEMRenderSettings settings) {
		RectSideOffsetsI xtraSpaceInSdb = DataRenderer.getXtraSpaceInSdb(sdb, tileWorldEnv);
		WorldRasterImage img;
		try {
			img = DEMRenderer.renderDEM(sdb, xtraSpaceInSdb, settings);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return DataRenderer.convertToTile(img, tileWorldEnv.getMinX(), tileWorldEnv.getMaxY());
	}
	
	
	
	public static int applyShadeFactor(final ColorVal in, final double fct) {
		return in.toIntColor(fct);
	}
	
	public static WorldRasterImage renderDEM(SGDataBank sdb) {
		return renderDEM(sdb, RectSideOffsetsI.EMPTY, DEFAULT_SHADING);
	}

	public static WorldRasterImage renderDEM(SGDataBank data, RectSideOffsetsI insets, DEMRenderSettings settings) {
		return renderDEM(data, insets, settings.prepare(data.getWorldTr().getScaleX(), data.getWorldTr().getScaleY()));
	}

	private static WorldRasterImage renderDEM(SGDataBank sdb, RectSideOffsetsI insets, DemShader shader) {
			int x0 = insets.l();
			int maxX = sdb.getWidth() - insets.r() - 1;
			long y0 = sdb.getEnvelope().getMinY() + insets.b();
			long maxY = sdb.getEnvelope().getMaxY() - insets.t();
			
			BufferedImage bi = new BufferedImage(maxX - x0 + 1, (int)(maxY - y0 + 1), BufferedImage.TYPE_INT_ARGB);
			int[] bufImgBank = ((DataBufferInt)bi.getRaster().getDataBuffer()).getData();

			int off = 0;
			double[] row0 = new double[sdb.getWidth()];
			double[] row1 = getRow(sdb, maxY, new double[sdb.getWidth()]);
			double[] row2 = getRow(sdb, maxY - 1, new double[sdb.getWidth()]);
			off += renderBoundaryRow(shader, x0, maxX, row2, row1, insets.t() == 0 ? null : getRow(sdb, maxY + 1, row0), bufImgBank, off);
			// top to bottom
			for (long botY = maxY - 2; botY >= y0; botY--) {
				double[] temp = row0;
				row0 = row1;
				row1 = row2;
				row2 = getRow(sdb, botY, temp);
				off = renderBulkRow(shader, x0, maxX, row2, row1, row0, bufImgBank, off);
			}
			off += renderBoundaryRow(shader, x0, maxX, insets.b() == 0 ? null : getRow(sdb, y0 - 1, row0), row2, row1, bufImgBank, off);
			return createResult(sdb, insets, bi);
		}

	private static int renderBoundaryRow(DemShader shader, int x0, int maxX, double[] datayBot, double[] datayMid, double[] datayTop, int[] bufImgBank, int off) {
		if (datayTop != null && datayBot != null) {
			return renderBulkRow(shader, x0, maxX, datayBot, datayMid, datayTop, bufImgBank, off);
		}
		for (int x = x0; x <= maxX; x++) {
			bufImgBank[off++] = renderBoundaryPixel(shader, x, datayBot, datayMid, datayTop);
		}
		return off;
	}

	private static int renderBulkRow(DemShader shader, int x0, int maxX, double[] datayBot, double[] datayMid, double[] datayTop, int[] bufImgBank, int off) {
		bufImgBank[off++] = renderBoundaryPixel(shader, x0, datayBot, datayMid, datayTop);
		off = renderBulkPixels(shader, x0, maxX, datayBot, datayMid, datayTop, bufImgBank, off);
		bufImgBank[off++] = renderBoundaryPixel(shader, maxX, datayBot, datayMid, datayTop);
		return off;
	}

	private static int renderBulkPixels(DemShader shader, int x0, int maxX, double[] datayBot, double[] datayMid, double[] datayTop, int[] bufImgBank, int off) {
		for (int x = x0 + 1; x < maxX; x++) {
			bufImgBank[off++] = shader.getColor(datayMid[x], datayMid[x-1], datayMid[x+1], datayBot[x], datayTop[x]);
		}
		return off;
	}

	private static int renderBoundaryPixel(DemShader shader, int x, double[] datayBot,  double[] datayMid, double[] datayTop) {
		double b = datayBot == null ? Double.NaN : datayBot[x];
		double t = datayTop == null ? Double.NaN : datayTop[x];
		if (x - 1 < 0) {
			return shader.getColor(datayMid[x], Double.NaN, datayMid[x+1], b, t);
		}
		if (x + 1 >= datayMid.length) {
			return shader.getColor(datayMid[x], datayMid[x-1], Double.NaN, b, t);
		}
		return shader.getColor(datayMid[x], datayMid[x - 1], datayMid[x + 1], b, t);
	}

	public static class DEMRenderSettings {
		public static double[] DEFAULT_SUN_NORMAL = new double[]{-1, 1, MathUtil.SQRT2};
		public final ElevationColors hypsometry;
		public final double[] sunNormal;
	
		public DEMRenderSettings(ElevationColors clrs) {
			this(clrs, DEFAULT_SUN_NORMAL);
		}
		
		public DEMRenderSettings(ElevationColors clrs, double[] sunNormal) {
			this.hypsometry = clrs;
			this.sunNormal = new double[3];
			MathUtil.normalize(sunNormal, this.sunNormal);
		}
	
		public DemShader prepare(double stepX, double stepY) {
			return new DemShader(sunNormal, stepX, stepY, hypsometry);
		}
		
		public int getPlaneNaNColor() {
			return applyShadeFactor(new ColorVal(hypsometry.colorWhenEmpty), getPlaneShade(sunNormal));
		}
	
		public void appendSignature(StringBuilder ret) {
			ret.append('S').append(sunNormal[0]).append(',').append(sunNormal[1]).append(',').append(sunNormal[2]);
			hypsometry.appendSignature(ret);
		}
	}
	
	public static final class DemShader {
		private final double shadeOff;
		
		private final ColorVal nanColor;
		private final ColorVal tempHeightClr = new ColorVal();
		private final ColorInterpolator hypso;

		private final double sunFactorX;
		private final double sunFactorY;
		private final double sunFactorZ;
	
		private final double botFactorZ;
		private final double botFactorY;

		public DemShader(double[] sunNormal, double stepX, double stepY, ElevationColors hypso) {
			double shadeFact = SHADE_SPAN / (1.0 - getMinDotProduct(sunNormal));
			this.shadeOff = SHADE_DARKEST - getMinDotProduct(sunNormal)*shadeFact;
			
			this.hypso = hypso.createInterpolator();
			this.nanColor = new ColorVal(hypso.colorWhenEmpty);
			
			// Cache factors for diffuse shading
			final double facRatio = stepX / stepY;
	
			this.sunFactorZ = 2 * stepX * sunNormal[2] * shadeFact;
			this.sunFactorY = -sunNormal[0] * shadeFact;
			this.sunFactorX = facRatio * sunNormal[1] * shadeFact;
	
			this.botFactorZ = 4.0 * stepX * stepX;
			this.botFactorY = facRatio * facRatio;
		}
		
		private final double calculateShadeFactor(double dzx, double dzy) {
			// top/sqrt(bot) = dot(sun_vec, normal_vec)
			double top = sunFactorZ + sunFactorY*dzx - sunFactorX*dzy;
			double bot = botFactorZ + dzx*dzx + botFactorY*dzy*dzy;
			return top / Math.sqrt(bot) + shadeOff;
		}

		private final int getNaNColor(double difX, double difY) {
			return applyShadeFactor(nanColor, calculateShadeFactor(difX, difY));
		}
		
		private final int getArgb(double z, double difX, double difY) {
			return applyShadeFactor(getHeightColor(z), calculateShadeFactor(difX, difY));
		}
		
		private final ColorVal getHeightColor(double z) {
			return hypso.getInterpolatedValue(z, tempHeightClr);
		}

		public final int getColor(double z, double zleft, double zright, double zbot, double ztop) {
			return isNaN(z)
				? getNaNColor(calcDifNoZ(zleft, zright), calcDifNoZ(zbot, ztop))
				: getArgb(z, calcDifWithZ(z, zleft, zright), calcDifWithZ(z, zbot, ztop));
		}

		private static double calcDifNoZ(double zleft, double zright) {
			return (Double.isNaN(zleft) || Double.isNaN(zright)) ? 0 : zright - zleft;
		}

		private static double calcDifWithZ(double z, double zleft, double zright) {
			return Double.isNaN(zleft) ? 
							 (Double.isNaN(zright) ? 0 : 2*(zright - z)) :
							 (Double.isNaN(zright) ? 2*(z - zleft) : zright - zleft);
		}
	}
}
