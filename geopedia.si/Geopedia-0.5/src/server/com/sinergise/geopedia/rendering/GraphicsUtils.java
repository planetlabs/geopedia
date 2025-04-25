package com.sinergise.geopedia.rendering;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import com.sinergise.common.util.geom.DimI;

public class GraphicsUtils {
	public static final DirectColorModel opaqueColorModel  = new DirectColorModel(24, 0x00FF0000, 0x0000FF00, 0x000000FF, 0);
	public static final int[] opaqueBandMasks = new int[] { 0x00FF0000, 0x0000FF00, 0x000000FF };
	public static final DirectColorModel transColorModel = new DirectColorModel(32, 0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000);
	public static final int[] transBandMasks = new int[] { 0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000 };
	
	
	public static int[] allocImage(int width, int height) {
		return new int[width * height];
	}
	
	public static BufferedImage wrap(int[] data, int width, int height, boolean opaque)
	{
		DirectColorModel cm;
		int[] bandmasks;
		if (opaque) {
			cm = opaqueColorModel;
			bandmasks = opaqueBandMasks;
		} else {
			cm = transColorModel;
			bandmasks = transBandMasks;
		}

		DataBufferInt dbi = new DataBufferInt(data, width * height);

		WritableRaster raster = Raster.createPackedRaster(dbi, width, height,
		                width, bandmasks, null);

		return new BufferedImage(cm, raster, false, null);
	}
	
	public static BufferedImage initImage(DimI imageSize, boolean opaque) {
		return initAndWrap(allocImage(imageSize.w(), imageSize.h()), imageSize.w(), imageSize.h(), opaque);
	}
	
	public static BufferedImage initAndWrap(int[] data, int width, int height, boolean opaque)
	{
		if (opaque)
			clear(data, true);
		
		return wrap(data, width, height, opaque);
	}

	public static int alphaBlend(int dst, int src)
	{
		int srcAlpha = src >>> 24;
		int srcRed = (src >>> 16) & 0xFF;
		int srcGreen = (src >>> 8) & 0xFF;
		int srcBlue = src & 0xFF;

		int dstAlpha = dst >>> 24;
		int dstRed = (dst >>> 16) & 0xFF;
		int dstGreen = (dst >>> 8) & 0xFF;
		int dstBlue = dst & 0xFF;
		
		int invSrcAlpha = 255 - srcAlpha;
		
		int outAlpha = (srcAlpha * srcAlpha + dstAlpha * invSrcAlpha + 127) / 255;
		int outRed = (srcRed * srcAlpha + dstRed * invSrcAlpha + 127) / 255;
		int outGreen = (srcGreen * srcAlpha + dstGreen * invSrcAlpha + 127) / 255;
		int outBlue = (srcBlue * srcAlpha + dstBlue * invSrcAlpha + 127) / 255;
		
		return (outAlpha << 24) | (outRed << 16) | (outGreen << 8) | outBlue;
	}
	
	public static void clear(int[] data, boolean opaque)
	{
        Arrays.fill(data, opaque?0xFFFFFFFF:0);
	}

	private static final int[] ones = new int[256];
	static {
		for (int a = 0; a < 256; a++)
			ones[a] = 0xFFFFFFFF;
	}
	public static byte[] jpegOf(BufferedImage img, int compLevel) throws IOException
    {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	
    	ImageWriter iw = ImageIO.getImageWritersByMIMEType("image/jpeg").next();
    	ImageWriteParam iwp = iw.getDefaultWriteParam();
    	iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    	iwp.setCompressionQuality(0.01f * compLevel);
    	
    	ImageOutputStream ios = new MemoryCacheImageOutputStream(baos);
    	IIOImage iio = new IIOImage(img, null, null);
    	
    	iw.setOutput(ios);
    	iw.write(null, iio, iwp);
    	
    	return baos.toByteArray();
    }
}
