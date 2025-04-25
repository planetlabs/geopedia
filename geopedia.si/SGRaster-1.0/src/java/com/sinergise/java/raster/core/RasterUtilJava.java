/*
 *
 */
package com.sinergise.java.raster.core;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.media.jai.Interpolation;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.CropDescriptor;
import javax.media.jai.operator.FileLoadDescriptor;
import javax.media.jai.operator.FileStoreDescriptor;
import javax.media.jai.operator.InvertDescriptor;
import javax.media.jai.operator.TranslateDescriptor;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import sun.awt.image.IntegerInterleavedRaster;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CartesianCRS;
import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.raster.core.ImageFileFilter;
import com.sinergise.common.raster.core.RasterColorType;
import com.sinergise.common.raster.core.RasterFileInfo;
import com.sinergise.common.raster.core.RasterUtil;
import com.sinergise.common.util.ImageUtil;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.EnvelopeI;
import com.sinergise.common.util.io.FileUtil;
import com.sinergise.common.util.math.fit.AffineFitUtil;
import com.sinergise.java.raster.core.RasterIoJai.JaiImageWrapper;
import com.sinergise.java.raster.core.SGRenderedImage.BufferedImageWrapper;
import com.sinergise.java.raster.io.GeoTIFFUtilJava;
import com.sinergise.java.util.UtilJava;
import com.sinergise.java.util.url.JavaURLCoder;


public class RasterUtilJava extends RasterUtil {
	static {
		UtilJava.initStaticUtils();
	}
	
	public static enum CheckResult {FULLY_OPAQUE, FULLY_TRANSPARENT, ALPHA_MASK, ALPHA_CONTINUOUS}
	
	public static final String HINT_FAIL_ON_MISSING_WORLD_FILE = "com.sinergise.java.raster.core.RasterUtilJava.failOnMissingWorldFile";
	public static final String HINT_FORCE_LOAD_BYTES_TO_MEM = "com.sinergise.java.raster.core.RasterUtilJava.loadBufferedToMem";
	public static final String HINT_FORCE_BUFFERED = "com.sinergise.java.raster.core.RasterUtilJava.loadBuffered";
	public static final String HINT_FORCE_JAI = "com.sinergise.java.raster.core.RasterUtilJava.loadWithJAI";

	public static final int getBufImgType(final RasterColorType cType) {
		if (cType == RasterColorType.TYPE_BINARY) {
			return BufferedImage.TYPE_BYTE_BINARY;
		}
		if (cType == RasterColorType.TYPE_GRAYSCALE) {
			return BufferedImage.TYPE_BYTE_GRAY;
		}
		if (cType == RasterColorType.TYPE_RGB) {
			return BufferedImage.TYPE_INT_RGB;
		}
		return BufferedImage.TYPE_INT_ARGB;
	}

	public static File getFile(URL imageURL) {
		try {
			return new File(URLDecoder.decode(imageURL.getFile(), "utf-8"));
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isFile(URL imageURL) {
		return JavaURLCoder.isFile(imageURL);
	}

	/**
	 * @param wrappedObj
	 * @param cropRegion
	 * @return cropped image, translated so that its 0, 0 coordinate is parent's minX, minY
	 */
	public static SGRenderedImage crop(SGRenderedImage img, EnvelopeI cropRegion) {
		if (img.isWrapperFor(BufferedImage.class)) {
			return RasterUtilJava.wrap(img.unwrap(BufferedImage.class).getSubimage(cropRegion.minX(), cropRegion.minY(), cropRegion.getWidth(), cropRegion.getHeight()));
		}
//		return RasterUtilJava.wrap(RasterUtilJava.toBufferedImage(img).getSubimage(cropRegion.minX(), cropRegion.minY(), cropRegion.getWidth(), cropRegion.getHeight()));
		RenderedOp croppedImg = CropDescriptor.create(img.unwrap(), Float.valueOf(cropRegion.minX()), Float.valueOf(cropRegion.minY()), Float.valueOf(cropRegion.getWidth()), Float.valueOf(cropRegion.getHeight()), null);
		RenderedOp translated = TranslateDescriptor.create(croppedImg, Float.valueOf(-cropRegion.minX()), Float.valueOf(-cropRegion.minY()),Interpolation.getInstance(Interpolation.INTERP_NEAREST), null);
		return RasterIoJai.wrap(translated);
	}

	public static final BufferedImage createCompatible(final RenderedImage ri, final int w, final int h) {
		ColorModel cm = ri.getColorModel(); 
		return new BufferedImage(cm, cm.createCompatibleWritableRaster(w, h), cm.isAlphaPremultiplied(), null);
	}

	public static final BufferedImage shrink(final BufferedImage in, BufferedImage out) {
		if (in.getColorModel().hasAlpha()) {
			return shrinkAlpha(in, out);
		}

		final int w = in.getWidth();
		final int h = in.getHeight();

		if ((w & 1) != 0 || (h & 1) != 0) throw new IllegalArgumentException();

		final int halfW = w >>> 1;
		final int halfH = h >>> 1;

		if (out == null) out = RasterUtilJava.createCompatible(in, halfW, halfH);

		for (int y = 0; y < halfH; y++) {
			final int y2 = y << 1;
			for (int x = 0; x < halfW; x++) {
				final int x2 = x << 1;

				final int c0 = in.getRGB(x2, y2);
				final int c1 = in.getRGB(x2, y2 + 1);
				final int c2 = in.getRGB(x2 + 1, y2);
				final int c3 = in.getRGB(x2 + 1, y2 + 1);

				final int r0 = 255 & (c0 >>> 16);
				final int r1 = 255 & (c1 >>> 16);
				final int r2 = 255 & (c2 >>> 16);
				final int r3 = 255 & (c3 >>> 16);
				final int g0 = 255 & (c0 >>> 8);
				final int g1 = 255 & (c1 >>> 8);
				final int g2 = 255 & (c2 >>> 8);
				final int g3 = 255 & (c3 >>> 8);
				final int b0 = 255 & c0;
				final int b1 = 255 & c1;
				final int b2 = 255 & c2;
				final int b3 = 255 & c3;

				final int dither = ((x + y) << 1) & 2;

				final int r = (r0 + r1 + r2 + r3 + dither) >>> 2;
				final int g = (g0 + g1 + g2 + g3 + dither) >>> 2;
				final int b = (b0 + b1 + b2 + b3 + dither) >>> 2;

				out.setRGB(x, y, (r << 16) | (g << 8) | b);
			}
		}

		return out;
	}

	public static final BufferedImage shrinkAlpha(final BufferedImage in, final BufferedImage outImg) {
		final int w = in.getWidth();
		final int h = in.getHeight();

		if ((w & 1) != 0 || (h & 1) != 0) throw new IllegalArgumentException("width or height ("+w+", "+h+")");

		final int halfW = w >>> 1;
		final int halfH = h >>> 1;

		final BufferedImage out;
		if (outImg != null && (outImg.getType() == in.getType())
			&& (outImg.getType() != BufferedImage.TYPE_CUSTOM)
			&& (outImg.getWidth() == halfW && outImg.getHeight() == halfH)) {
			out = outImg;
		} else {
			out = new BufferedImage(halfW, halfH, in.getType());
		}
		
		if (in.getType() == BufferedImage.TYPE_INT_ARGB) {
			return shrinkAlpha_INT_ARGB(in, out);
		}

		for (int y = 0; y < halfH; y++) {
			final int y2 = y << 1;
			for (int x = 0; x < halfW; x++) {
				final int x2 = x << 1;

				final int c0 = in.getRGB(x2, y2);
				final int c1 = in.getRGB(x2, y2 + 1);
				final int c2 = in.getRGB(x2 + 1, y2);
				final int c3 = in.getRGB(x2 + 1, y2 + 1);

				final int a0 = 255 & (c0 >>> 24);
				final int a1 = 255 & (c1 >>> 24);
				final int a2 = 255 & (c2 >>> 24);
				final int a3 = 255 & (c3 >>> 24);

				if ((a0 | a1 | a2 | a3) == 0) continue;

				final int r0 = 255 & (c0 >>> 16);
				final int r1 = 255 & (c1 >>> 16);
				final int r2 = 255 & (c2 >>> 16);
				final int r3 = 255 & (c3 >>> 16);
				final int g0 = 255 & (c0 >>> 8);
				final int g1 = 255 & (c1 >>> 8);
				final int g2 = 255 & (c2 >>> 8);
				final int g3 = 255 & (c3 >>> 8);
				final int b0 = 255 & c0;
				final int b1 = 255 & c1;
				final int b2 = 255 & c2;
				final int b3 = 255 & c3;

				if ((a0 & a1 & a2 & a3) == 255) {
					final int dither = ((x + y) << 1) & 2;

					final int r = (r0 + r1 + r2 + r3 + dither) >>> 2;
					final int g = (g0 + g1 + g2 + g3 + dither) >>> 2;
					final int b = (b0 + b1 + b2 + b3 + dither) >>> 2;

					out.setRGB(x, y, 0xFF000000 | (r << 16) | (g << 8) | b);
				} else {
					final double d = 1.0 / (a0 + a1 + a2 + a3);
					final int r = (int)Math.round((a0 * r0 + a1 * r1 + a2 * r2 + a3 * r3) * d);
					final int g = (int)Math.round((a0 * g0 + a1 * g1 + a2 * g2 + a3 * g3) * d);
					final int b = (int)Math.round((a0 * b0 + a1 * b1 + a2 * b2 + a3 * b3) * d);
					final int a = (int)Math.round((a0 + a1 + a2 + a3) * 0.25);

					out.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
				}
			}
		}

		return out;
	}

	private static BufferedImage shrinkAlpha_INT_ARGB(BufferedImage in, BufferedImage out) {
		final int[] inArr = ((IntegerInterleavedRaster)in.getRaster()).getDataStorage();
		final int[] outArr = ((IntegerInterleavedRaster)out.getRaster()).getDataStorage();
		
		final int off = in.getWidth();
		final int outH = out.getHeight();
		final int outW = out.getWidth();
		int i = -1;
		for (int row = 0; row < outH; row++) {
			int j = 2*row*off;
			int offJ = (2*row+1)*off;
			for (int col = 0; col < outW; col++) {
				i++;
				final int c0 = inArr[j++];
				final int c1 = inArr[j++];
				final int c2 = inArr[offJ++];
				final int c3 = inArr[offJ++];
				
				final int a0 = 255 & (c0 >>> 24);
				final int a1 = 255 & (c1 >>> 24);
				final int a2 = 255 & (c2 >>> 24);
				final int a3 = 255 & (c3 >>> 24);
				
				if ((a0 | a1 | a2 | a3) == 0) continue;

				final int r0 = 255 & (c0 >>> 16);
				final int r1 = 255 & (c1 >>> 16);
				final int r2 = 255 & (c2 >>> 16);
				final int r3 = 255 & (c3 >>> 16);
				final int g0 = 255 & (c0 >>> 8);
				final int g1 = 255 & (c1 >>> 8);
				final int g2 = 255 & (c2 >>> 8);
				final int g3 = 255 & (c3 >>> 8);
				final int b0 = 255 & c0;
				final int b1 = 255 & c1;
				final int b2 = 255 & c2;
				final int b3 = 255 & c3;

				if ((a0 & a1 & a2 & a3) == 255) {
					final int dither = ((col + row) << 1) & 2;

					final int r = (r0 + r1 + r2 + r3 + dither) >>> 2;
					final int g = (g0 + g1 + g2 + g3 + dither) >>> 2;
					final int b = (b0 + b1 + b2 + b3 + dither) >>> 2;

					outArr[i] = 0xFF000000 | (r << 16) | (g << 8) | b;
				} else {
					final double d = 1.0 / (a0 + a1 + a2 + a3);
					final int r = (int)Math.round((a0 * r0 + a1 * r1 + a2 * r2 + a3 * r3) * d);
					final int g = (int)Math.round((a0 * g0 + a1 * g1 + a2 * g2 + a3 * g3) * d);
					final int b = (int)Math.round((a0 * b0 + a1 * b1 + a2 * b2 + a3 * b3) * d);
					final int a = (int)Math.round((a0 + a1 + a2 + a3) * 0.25);

					outArr[i] = (a << 24) | (r << 16) | (g << 8) | b;
				}
			}
		}
		return out;
	}

	public static final BufferedImage shrinkJ2D(final BufferedImage in, final BufferedImage ret) {
		final Graphics2D rg = ret.createGraphics();
		rg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		rg.drawImage(in, 0, 0, ret.getWidth(), ret.getHeight(), null);
		return ret;
	}

	// private static void doit(File output, File tl, File tr, File bl, File br)
	// throws IOException
	// {
	// if (output.exists()) {
	// long outTime = output.lastModified();
	//
	// do {
	// if (tl.exists() && tl.lastModified() > outTime) break;
	// if (tr.exists() && tr.lastModified() > outTime) break;
	// if (bl.exists() && bl.lastModified() > outTime) break;
	// if (br.exists() && br.lastModified() > outTime) break;
	//	
	// if (latestSkipped == null || outTime > latestTimestamp) {
	// latestSkipped = output;
	// latestTimestamp = outTime;
	// }
	// System.out.println("Skipping "+output);
	// return;
	// } while (false);
	// }
	//		
	//		
	// System.out.println("Making "+output);
	// BufferedImage out = new BufferedImage(512, 512,
	// BufferedImage.TYPE_INT_ARGB);
	//		
	// drawIfExists(out, 0, 0, tl);
	// drawIfExists(out, 256, 0, tr);
	// drawIfExists(out, 0, 256, bl);
	// drawIfExists(out, 256, 256, br);
	//	
	// out = shrink(out, new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB));
	//		
	// if (allOpaque(out)) {
	// BufferedImage tmp = new BufferedImage(256, 256,
	// BufferedImage.TYPE_INT_RGB);
	// tmp.createGraphics().drawImage(out, 0, 0, null);
	// out = tmp;
	// }
	//		
	// output.getParentFile().mkdirs();
	//		
	// RasterIO.write(out, output);
	//		
	// if(!silent)
	// sip.setImage(0, 0, out);
	// }

	/**
	 * @deprecated
	 */
	@Deprecated
	public static final BufferedImage shrinkJAI(final BufferedImage in) {
		final AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(0.5, 0.5), AffineTransformOp.TYPE_BILINEAR);
		// SubsampleAverage is very bad - offsets by 1 pixel down and right
		// return SubsampleAverageDescriptor.create(in, 0.5, 0.5,
		// null).getAsBufferedImage();
		// BufferedImage ret;
		// int w = in.getWidth();
		// int h = in.getHeight();
		//				
		// if ((w & 1) != 0 || (h & 1) != 0)
		// throw new IllegalArgumentException();
		//				
		// int halfW = w >>> 1;
		// int halfH = h >>> 1;
		//				
		// if (in.getColorModel().hasAlpha()) {
		// ret=new BufferedImage(halfW,halfH,in.getType());
		// } else {
		// ret=new BufferedImage(halfW,halfH,in.getType());
		// }

		return ato.filter(in, null);
	}

	public static void main(final String[] args) {
		final String[] testFiles =
				new String[]{"/sad.tr/test123.tif", "/sad.tr/test123.tiff", "/sad.tr/test123.png", "/sad.tr/test123.jpg", "/sad.tr/test123.jpeg",
						"/sad.tr/test123.gif", "/sad.tr/test123.bmp", "/sad.tr/test123.jp2", "/sad.tr/test123.ecw", "/sad.tr/test123.TIF"};

		String[] ret;
		for (final String testFile : testFiles) {
			System.out.println("----------- " + testFile);
			ret = getWorldSuffixes(testFile.substring(testFile.lastIndexOf('.') + 1));
			for (final String element : ret)
				System.out.println(element);
		}
	}

	public static final File getWorldFile(final File imageFile) {
		File ret = null;
		final String[] fNames = ImageUtil.getWorldFileNames(imageFile.getName());
		for (final String fName : fNames) {
			ret = new File(imageFile.getParentFile(), fName);
			if (ret.isFile()) {
				return ret;
			}
		}
		//try ignoring case
		String[] filesInDir = imageFile.getParentFile().list();
		if (filesInDir != null) {
			for (String fN : filesInDir) {
				for (String wName : fNames) {
					if (wName.equalsIgnoreCase(fN)) return new File(imageFile.getParentFile(), fN);
				}
			}
		}
		return new File(imageFile.getParentFile(), fNames[0]);
	}
	
	
	public static RasterFileInfo readImageInfo(final URL imageURL) throws IOException {
		URL tfwURL = getWorldURL(imageURL);
		double[] tfwVals = readWorldFile(CRS.NONAME_WORLD_CRS, tfwURL);
		DimI size = RasterIO.readImageSize(imageURL);
		AffineTransform2D tr = affineFromCellBasedArray(CartesianCRS.createImageCRS(size.w(), size.h()), CRS.NONAME_WORLD_CRS, tfwVals);
		return new RasterFileInfo(imageURL, tfwURL, size, tr);
	}
	

	public static final double[] readImageTransform(final URL imageURL, final CRS space) throws IOException {
		try {
			return readWorldFile(space, RasterUtilJava.getWorldURL(imageURL));
		} catch (FileNotFoundException e) {
			return GeoTIFFUtilJava.readTFW(imageURL);
		}
	}
	
	public static final double[] readWorldFile(final CRS space, final URL worldFileURL) throws IOException {
		InputStream is = null;
		try {
			if ("file".equals(worldFileURL.getProtocol())) {
				File f = new File(worldFileURL.toURI());
				is = new FileInputStream(f);
			} else {
				is = worldFileURL.openStream();
			}
			return readWorldFile(space, is);
		} catch(URISyntaxException e) {
			throw new RuntimeException(e);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	private static final double[] readWorldFile(final CRS space, final InputStream inputStream) throws IOException {
		final double[] fufu = new double[6];
		final InputStreamReader isr = new InputStreamReader(inputStream, "US-ASCII");
		try {
			final LineNumberReader lnr = new LineNumberReader(isr);
			try {
				for (int a = 0; a < 6; a++) {
					fufu[a] = Double.parseDouble(lnr.readLine().trim());
					if (space == CRS.D48_GK && a >= 4 && fufu[a] >= 5000000) fufu[a] -= 5000000;
				}
			} finally {
				lnr.close();
			}
		} finally {
			isr.close();
		}
		return fufu;
	}

	public static final AffineTransform2D readAffineGridBasedFromWorldFile(final URL worldFile, final CRS imageCRS, final CRS worldCRS) throws IOException {
		final double[] tfwdata = readWorldFile(worldCRS, worldFile);
		return affineFromCellBasedArray(imageCRS, worldCRS, tfwdata);
	}
	
	public static final AffineTransform2D approximateTransformWorldInfo(final AffineTransform2D originalTr, DimI imgSize, Transform<?, ?> worldCRSConversion) {
		double[][] ptsSrc = new double[][] {{0,0},{imgSize.w(),0},{imgSize.w(), imgSize.w()},{0,imgSize.h()}};
		double[][] ptsWorld = new double[4][2];
		for (int i = 0; i < 4; i++) {
			Point ptWOrig = originalTr.point(new Point(ptsSrc[i][0],ptsSrc[i][1]));
			Point ptWNew = worldCRSConversion.point(ptWOrig, new Point());
			ptsWorld[i][0] = ptWNew.x;
			ptsWorld[i][1] = ptWNew.y;
		}
		double[] newParams = AffineFitUtil.fitGeneralAffine(ptsSrc, ptsWorld);
		return new AffineTransform2D(originalTr.getSource(), worldCRSConversion.getTarget(), newParams);
	}

	public static final void writeWorldFile(final AffineTransform2D aff, final File worldFile) throws IOException {
		writeWorldFile(affineToCellBasedArray(aff), worldFile);
	}
	
	public static final void writeWorldFile(final double[] tfw, final File worldFile) throws IOException {

		final Writer wr = new FileWriter(worldFile);
		try {
			for (int i = 0; i < 6; i++) {
				wr.write(String.valueOf(tfw[i]) + "\n");
			}
		} finally {
			wr.close();
		}
	}

	public static final URL getWorldURL(final URL imageURL) throws IOException {
		if ("file".equals(imageURL.getProtocol())) {
			try {
				final File retF = getWorldFile(new File(imageURL.toURI()));
				if (retF != null) return retF.toURI().toURL();
			} catch(URISyntaxException e) {
				e.printStackTrace();
			}
		}
		final String path = imageURL.getPath();
		final int dotIdx = path.lastIndexOf('.');
		final String pref = path.substring(0, dotIdx + 1);
		final String tfwSuff = getWorldSuffix(path.substring(dotIdx + 1));
		return new URL(new URL(imageURL.toExternalForm()), pref + tfwSuff);
	}

	public static final BufferedImage toBufferedImage(final SGRenderedImage rImg) {
		return toBufferedImage(rImg, null);
	}

	public static final BufferedImage toBufferedImage(final SGRenderedImage rImg, final List<Throwable> errors) {
		if (rImg == null) {
			return null;
		}
		if (rImg.isWrapperFor(BufferedImage.class)) {
			return rImg.unwrap(BufferedImage.class);
		}
		// Robust tile copying instead of RenderedOp.getAsBufferedImage

		final int rMinX = rImg.getMinX();
		final int rMinY = rImg.getMinY();
		
		int rW = rImg.getWidth();
		int rH = rImg.getHeight();

		if (rW < 0) {
			rW = rImg.getTileWidth() * rImg.getNumXTiles();
		}
		if (rH < 0) {
			rH = rImg.getTileHeight() * rImg.getNumYTiles();
		}
		
		final WritableRaster wr = Raster.createWritableRaster(rImg.getSampleModel().createCompatibleSampleModel(rW, rH), new java.awt.Point(0, 0));
		final BufferedImage bi = new BufferedImage(rImg.getColorModel(), wr, rImg.getColorModel().isAlphaPremultiplied(), null);

		final int tMinX = rImg.getMinTileX();
		final int tMaxX = tMinX + rImg.getNumXTiles() - 1;
		final int tMinY = rImg.getMinTileY();
		final int tMaxY = tMinY + rImg.getNumYTiles() - 1;

		// System.out.println(tMinX+" "+tMinY+", "+tMaxX+","+tMaxY);
		boolean didany = false;
		Exception firstEx = null;
		for (int i = tMinX; i <= tMaxX; i++) {
			for (int j = tMinY; j <= tMaxY; j++) {
				try {
					Raster rst = rImg.getTile(i, j);
					if (rMinX != 0 || rMinY != 0) {
						rst = rst.createChild(rst.getMinX(), rst.getMinY(), rst.getWidth(), rst.getHeight(), rst.getMinX() - rMinX, rst.getMinY() - rMinY, null);
					}
					bi.setData(rst);
					didany = true;
				} catch(final Exception e) {
					if (errors != null) {
						errors.add(e);
					}
					e.printStackTrace();
					if (firstEx == null) {
						firstEx = e;
					}

					try {
						// Fill with white
						final Object samp = bi.getColorModel().getDataElements(0x00FFFFFF, null);

						final int minx = rImg.getTileGridXOffset() + i * rImg.getTileWidth();
						final int miny = rImg.getTileGridYOffset() + j * rImg.getTileHeight();
						for (int x = rImg.getTileWidth() - 1; x >= 0; x--) {
							for (int y = rImg.getTileHeight() - 1; y >= 0; y--) {
								wr.setDataElements(minx + x, miny + y, samp);
							}
						}
					} catch(final Exception ex) {
						// Leave it
					}
				}
			}
		}
		if (!didany) { 
			throw new RuntimeException(firstEx);
		}
		return bi;
	}

	public static final void scan(final File f, HashMap<String, File> index, final ArrayList<WorldRasterImage> tifs, final ImageFileFilter filter, final CRS cs) throws IOException {
		if (f.isDirectory()) {
			if (filter != null && !filter.acceptDirectory(f.getName())) return;

			index = new HashMap<String, File>();
			final File[] fls = f.listFiles();
			for (final File fl : fls) {
				index.put(fl.getName().toLowerCase(), fl);
			}
			for (final File ff : fls) {
				if (ff.getParentFile().equals(f)) {// skip . and .. on linux
					scan(ff, index, tifs, filter, cs);
				}
			}
			return;
		}
		if (filter != null && !filter.acceptFile(f.getName())) return;

		final String name = f.getName();
		final String namelc = name.toLowerCase();

		if (ImageUtil.isImageFile(namelc)) {
			final String[] tfws = ImageUtil.getWorldFileNames(name);
			File tfwFile = null;
			for (final String tfw : tfws) {
				tfwFile = index.get(tfw.toLowerCase());
				if (tfwFile != null && tfwFile.exists()) break;
			}
			if (tfwFile == null || !tfwFile.exists()) {
				System.err.println("Warning: skipping " + f + " because no .tfw found");
			} else {
				final WorldRasterImage info = new WorldRasterImage(f.toURI().toURL(), cs);

				if (filter != null && !filter.acceptImage(info)) return;

				tifs.add(info);
				System.out.println("Found " + f + " (w = " + info.w + ", h = " + info.h + ", envelope = " + info.wEnv + ")");
			}
		}
	}

	public static BufferedImage cloneBI(final BufferedImage orig) {
		final BufferedImage ret = createCompatible(orig, orig.getWidth(), orig.getHeight());
		orig.copyData(ret.getRaster());
		return ret;
	}
	

	public static BufferedImage cloneBI(SGRenderedImage image) {
		if (image.isWrapperFor(BufferedImage.class)) {
			return cloneBI(image.unwrap(BufferedImage.class));
		}
		return toBufferedImage(image);
	}
	
	public static OffsetBufferedImage cloneBI(final OffsetBufferedImage orig) {
		return new OffsetBufferedImage(cloneBI(orig.bi), orig.offX, orig.offY);
	}

	public static Icon createDisabledIcon(Icon icon) {
		if (icon == null) { return null; }

		Image img;
		if (icon instanceof ImageIcon) {
			img = ((ImageIcon)icon).getImage();
		} else {
			img = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
			icon.paintIcon(null, img.getGraphics(), 0, 0);
		}

		return new ImageIcon(GrayFilter.createDisabledImage(img));
	}

	public static final void clear(BufferedImage img, Color bgColor) {
		if (bgColor.getAlpha() != 255 && !img.getColorModel().hasAlpha()) bgColor = new Color(bgColor.getRGB(), false);

		Graphics2D g = img.createGraphics();
		g.setColor(bgColor);
		g.setComposite(AlphaComposite.Src);
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
	}

	public static final void copy(BufferedImage src, BufferedImage tgt) {
		tgt.createGraphics().drawImage(src, null, 0, 0);
	}
	
	public static final void negative(File src, File tgt) {
		RenderedOp image = FileLoadDescriptor.create(src.getAbsolutePath(), null, Boolean.TRUE, null);
		System.out.println("rendered");
		RenderedOp inverted = InvertDescriptor.create(image, null);
		System.out.println("inverted");
		FileStoreDescriptor.create(inverted, tgt.getAbsolutePath(), JAIType(FileUtil.getSuffix(src.getName())), null, Boolean.TRUE, null);
	}

	public static boolean getHintForceBufferedMem() {
		return Boolean.parseBoolean(System.getProperty(HINT_FORCE_LOAD_BYTES_TO_MEM));
	}

	public static boolean getHintForceBuffered() {
		if (getHintForceBufferedMem()) {
			return true;
		}
		return Boolean.parseBoolean(System.getProperty(HINT_FORCE_BUFFERED));
	}
	
	public static boolean getHintForceJAI() {
		return Boolean.parseBoolean(System.getProperty(HINT_FORCE_JAI));
	}

	public static boolean getHintFailOnMissingWorldFile() {
		return Boolean.parseBoolean(System.getProperty(HINT_FAIL_ON_MISSING_WORLD_FILE));
	}

	public static void setHint(String name, String value) {
		System.setProperty(name, value);
	}

	public static RenderedImage stretchRendered(RenderedImage src, int outW, int outH) {
		if (src instanceof BufferedImage) {
			final BufferedImage bi = (BufferedImage)src;
			return stretch(bi, createCompatible(bi, outW, outH));
		}
		return ImageLoaderNew.scale(src, new Dimension(outW, outH), ImageLoaderNew.INTERPOLATION_BILINEAR);
	}

	public static BufferedImage stretch(BufferedImage in, BufferedImage out) {
		final int inW = in.getWidth();
		final int inH = in.getHeight();
		final int outW = out.getWidth();
		final int outH = out.getHeight();
		final double sW = ((double)outW) / inW;
		final double sH = ((double)outH) / inH;
		
		int outY1 = 0; 
		for (int y = 0; y < inH; y++) {
			final int outY0 = outY1;
			outY1 = (int)Math.round((y+1) * sH);
			
			int outX1 = 0;
			for (int x = 0; x < inW; x++) {
				final int rgb = in.getRGB(x, y);
				final int outX0 = outX1;
				outX1 = (int)Math.round((x+1) * sW);
				
				for (int i = outY0; i < outY1; i++) {
					for (int j = outX0; j < outX1; j++) {
						out.setRGB(j, i, rgb);
					}
				}
			}
		}
		return out;
	}

	public static BufferedImage stretchBI(SGRenderedImage src, int outW, int outH) {
		return stretch(toBufferedImage(src), createCompatible(src, outW, outH));
	}

	public static AffineTransform2D gridBasedFromCellBased(AffineTransform2D cellBasedTr) {
		return AffineTransform2D.createTrScale(1, 1, 0.5, 0.5).concatenateWith(cellBasedTr);
	}

	public static final SGRenderedImage wrap(RenderedImage img) {
		if (img instanceof SGRenderedImage) {
			return (SGRenderedImage)img;
		}
		if (img instanceof BufferedImage) {
			return wrap((BufferedImage)img);
		}
		if (img instanceof PlanarImage) {
			return new JaiImageWrapper((PlanarImage)img);
		}
		throw new IllegalArgumentException("Cannot wrap a non-specific rendered image, because we won't know how to close it"); 
	}
	
	public static BufferedImageWrapper wrap(BufferedImage bufImg) {
		return new BufferedImageWrapper(bufImg);
	}

	public static File toFile(URL url) {
		try {
			return new File(url.toURI());
		} catch(URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isEmpty(BufferedImage bi, int backgroundColor) {
		final int h = bi.getHeight();
		final int w = bi.getWidth();
		final int bgRGB = backgroundColor & 0xFFFFFF;
		
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int c = bi.getRGB(x, y);
				if (((c & 0xFFFFFF) != bgRGB) && (c & 0xFF000000) != 0) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean isOpaque(BufferedImage bi) {
		if (!bi.getColorModel().hasAlpha()) {
			return true;
		}

		final int h = bi.getHeight();
		final int w = bi.getWidth();

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int a = (bi.getRGB(x, y) >>> 24);
				if (a != 255) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static CheckResult checkImage(BufferedImage bi) {
		if (!bi.getColorModel().hasAlpha()) {
			return CheckResult.FULLY_OPAQUE;
		}

		final int h = bi.getHeight();
		final int w = bi.getWidth();
		boolean fullyTransparent = true;
		boolean fullyOpaque = true;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int a = (bi.getRGB(x, y) >>> 24);
				if (a > 0) {
					fullyTransparent = false;
					if (a < 255) {
						return CheckResult.ALPHA_CONTINUOUS;	
					}
				} else if (a < 255) {
					fullyOpaque = false;
				}
			}
		}
		if (fullyTransparent) {
			return CheckResult.FULLY_TRANSPARENT;
		}
		if (fullyOpaque) {
			return CheckResult.FULLY_OPAQUE;
		}
		return CheckResult.ALPHA_MASK;
	}
}
