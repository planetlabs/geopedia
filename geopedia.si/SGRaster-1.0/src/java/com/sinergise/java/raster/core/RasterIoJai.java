package com.sinergise.java.raster.core;
import static com.sinergise.java.raster.core.RasterUtilJava.getFile;
import static com.sinergise.java.raster.core.RasterUtilJava.isFile;
import static java.lang.Boolean.TRUE;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.operator.EncodeDescriptor;
import javax.media.jai.operator.FileLoadDescriptor;
import javax.media.jai.operator.FileStoreDescriptor;
import javax.media.jai.operator.StreamDescriptor;
import javax.media.jai.operator.URLDescriptor;

import com.sinergise.common.util.geom.DimI;
import com.sun.media.jai.codec.ByteArraySeekableStream;
import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jai.codec.PNGEncodeParam;

public class RasterIoJai {
	static {
		JAI.disableDefaultTileCache();
	}
	public static class JaiImageWrapper extends SGRenderedImage.RenderedImageWrapper {
		
		public JaiImageWrapper(PlanarImage image) {
			super(image);
		}

		@Override
		public void close() {
			((PlanarImage)wrappedObj).dispose();
		}
	}	
	
	public static SGRenderedImage readRendered(byte[] data, int len) throws IOException {
		return wrap(StreamDescriptor.create(new ByteArraySeekableStream(data, 0, len), null, null));
	}
	
	public static JaiImageWrapper wrap(PlanarImage image) {
		return new JaiImageWrapper(image);
	}
	
	public static void writeGeneric(BufferedImage ret, OutputStream os, String imageType) {
		if (imageType.endsWith("tif")) {
			imageType = "TIFF";
		}
		EncodeDescriptor.create(ret, os, imageType, null, null).dispose();
	}

	public static SGRenderedImage readRendered(File in) {
		return wrap(FileLoadDescriptor.create(in.getAbsolutePath(), null, TRUE, null));
	}

	public static SGRenderedImage readRendered(URL imageURL) {
		if (isFile(imageURL)) {
			return readRendered(getFile(imageURL)); 
		}
		return wrap(URLDescriptor.create(imageURL, null, null));
	}
	
	public static void writeJpeg(BufferedImage img, File outFile, float quality) {
		JPEGEncodeParam jp = new JPEGEncodeParam();
		jp.setQuality(0.01f * quality);
		outFile.getParentFile().mkdirs();
		FileStoreDescriptor.create(img, outFile.getAbsolutePath(), "JPEG", jp, Boolean.TRUE, null);
	}
	
	public static void writeJpeg(BufferedImage img, OutputStream os, int quality) {
		JPEGEncodeParam jp = new JPEGEncodeParam();
		jp.setQuality(0.01f * quality);
		EncodeDescriptor.create(img, os, "JPEG", jp, null);
	}


	public static void writeGeneric(SGRenderedImage image, File imageFile, String imageType) {
		FileStoreDescriptor.create(image.unwrap(), imageFile.getAbsolutePath(), imageType, null, TRUE, null).dispose();
	}

	public static void writePng(BufferedImage img, OutputStream os) {
		EncodeDescriptor.create(img, os, "PNG", PNGEncodeParam.getDefaultEncodeParam(img), null).dispose();
	}

	public static void writePng(BufferedImage img, File outFile) {
		FileStoreDescriptor.create(img, outFile.getAbsolutePath(), "PNG",PNGEncodeParam.getDefaultEncodeParam(img), TRUE, null).dispose();
	}

	public static DimI readImageSize(URL imageURL) throws IOException {
		SGRenderedImage img = readRendered(imageURL);
		try {
			return new DimI(img.getWidth(), img.getHeight());
		} finally {
			img.close();
		}
	}
}
