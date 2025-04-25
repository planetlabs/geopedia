package com.sinergise.java.raster;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CartesianCRS;
import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.lang.SGCallable;
import com.sinergise.java.geometry.util.APIMapping;
import com.sinergise.java.raster.core.RasterUtilJava;
import com.sinergise.java.raster.core.WorldRasterImage;
import com.sinergise.java.util.MemoryUtil;


public class RasterDeskew {
	public static void main(String[] args)
	{
		try
		{
			File inDir = new File(args[0]);
			File outDir = new File(args[1]);
			
			boolean autoPix = true;
			double pxW = Double.NaN;
			double pxH = Double.NaN;
			if (args.length>2) {
				try { 
					pxW = Double.parseDouble(args[2]);
					pxH = pxW;
					autoPix = false;
				} catch (Exception e) {
				}
			}
			if (args.length>3) {
				try {
					pxH = Double.parseDouble(args[3]);
				} catch (Exception e) {
				}
			}
			process(inDir, outDir, autoPix, pxW, pxH, CRS.MAP_CRS);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void process(File inDir, File outDir, boolean autoPixSize, double pxW, double pxH, CRS cs)
			throws IOException
	{
		process(inDir, outDir, autoPixSize, pxW, pxH, cs, false);
	}
	public static void process(File inDir, File outDir, boolean autoPixSize, double pxW, double pxH, CRS cs, boolean skipIfExists)
	throws IOException
	{
		if (!inDir.isDirectory() || !inDir.exists())
		{
			throw new IllegalArgumentException("Input directory '"
					+ inDir.getAbsolutePath() + "' does not exist");
		}
		if (!outDir.exists())
			outDir.mkdirs();
		if (!outDir.isDirectory())
		{
			throw new IllegalArgumentException("Output '"
					+ outDir.getAbsolutePath() + "' is not a directory");
		}
	
		ArrayList<WorldRasterImage> tifs = new ArrayList<WorldRasterImage>();
		RasterUtilJava.scan(inDir, null, tifs, null, cs);
		int cnt=0;
		for (WorldRasterImage t : tifs)
		{
			File newF=new File(outDir, t.getImageFileName());
			if (skipIfExists && newF.exists()) {
				System.out.println("Skipping ("+t.getImageFileName()+") "+(++cnt)+"/"+tifs.size());
				continue;
			}
			System.out.println("File ("+t.getImageFileName()+") "+(++cnt)+"/"+tifs.size());
			if (autoPixSize) {
				deskewAuto(t, outDir);
			} else {
				deskew(t, newF, pxW, pxH);
			}
		}
	}

	public static void deskewAuto(WorldRasterImage t, File newF) throws IOException {
		WorldRasterImage newImg = deskewAuto(t);
		newImg.setURL(newF.toURI().toURL());
		newImg.save();
	}
	
	public static WorldRasterImage deskewAuto(WorldRasterImage t) {
		Envelope trPx = t.backTransformEnvelopeGridBased(new Envelope(0,0,1,1));
		double pxW = 1.0/trPx.getWidth();
		double pxH = 1.0/trPx.getHeight();
		pxW = Math.min(pxW, pxH);
		pxH = pxW;
		return deskew(t, pxW, pxH);
	}
	
	public static void deskew(WorldRasterImage t, File newF, double pxW, double pxH) throws IOException {
		WorldRasterImage newImg = deskew(t, pxW, pxH);
		newImg.setURL(newF.toURI().toURL());
		newImg.save();
	}
	public static WorldRasterImage deskew(final WorldRasterImage t, final double pxW, final double pxH) {
		return deskew(t, pxW, pxH, false, new Color(0x00000000,true), true);
	}
	
	public static WorldRasterImage deskew(final WorldRasterImage t, final double pxW, final double pxH, boolean interpolate, final Color bgColor, final boolean trans) {
	    final Envelope trBase = t.transformEnvelopeGridBased(new Envelope(0, 0, t.w, t.h));

	    // This matches exactly what the AffineTransformOp does
		final Envelope tr = new Envelope(pxW * Math.round(trBase.getMinX() / pxW - 1),
		pxH * Math.round(trBase.getMinY() / pxH - 1),
		pxW * Math.round(trBase.getMaxX() / pxW + 1),
		pxH * Math.round(trBase.getMaxY() / pxH + 1));
		
		final int imgW = (int)Math.round(tr.getWidth()/pxW);
		final int imgH = (int)Math.round(tr.getHeight()/pxH);
		
		final AffineTransform2D targetTr = new AffineTransform2D(CartesianCRS.createImageCRS(imgW, imgH), t.targetCRS, new double[]{pxW, 0, 0, -pxH, tr.getMinX(), tr.getMaxY()}); 
		final AffineTransform2D aMid = t.tr.append(targetTr.inverse());
		
		final RenderingHints rh=new RenderingHints(null);
		rh.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		rh.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
		if (interpolate) {
			rh.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			rh.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			rh.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			rh.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		} else {
			rh.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			rh.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			rh.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
			rh.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
		}
		final AffineTransformOp ato=new AffineTransformOp(APIMapping.toJ2D(aMid), rh);
		
		return MemoryUtil.executeWithOutOfMemoryRetry(2, 1000, new SGCallable<WorldRasterImage>() {
		    @Override
			public WorldRasterImage call() {
		    	try {
		    		final BufferedImage src = t.getAsBufferedImage();
		    		BufferedImage ret = ato.createCompatibleDestImage(src, (trans && !src.getColorModel().hasAlpha()) ? ColorModel.getRGBdefault() : null);
		    		RasterUtilJava.clear(ret, bgColor);
		    		ret = ato.filter(src, ret);
		    		final WorldRasterImage wri=new WorldRasterImage(null, RasterUtilJava.wrap(ret), targetTr);
		    		wri.setHardCache(true);
		    		return wri;
		    	} catch (Exception e) {
		    		throw new RuntimeException(e);
				}
		    }
		});
	}
}
