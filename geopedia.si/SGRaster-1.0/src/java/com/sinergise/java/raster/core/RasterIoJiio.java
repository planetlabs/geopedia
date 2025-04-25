package com.sinergise.java.raster.core;

import static com.sinergise.java.raster.core.RasterUtilJava.isFile;
import static com.sinergise.java.raster.core.RasterUtilJava.toFile;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.io.IOUtil;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.java.raster.core.SGRenderedImage.RenderedImageWrapper;
import com.sinergise.java.raster.io.ByteArrayImageInputStream;
import com.sinergise.java.raster.io.ByteArrayImageOutputStream;
import com.sinergise.java.util.io.ByteArrayOutputStream;
import com.sinergise.java.util.io.FileUtilJava;
import com.sinergise.java.util.io.IOUtilJava;

public class RasterIoJiio {
	{
		ImageIO.setUseCache(false);
	}
	private static final long MAX_BUFFERED_LEN = 512L*1024*1024;

	private static class WrappedJiioImage extends RenderedImageWrapper {
		ImageReader reader;
		public WrappedJiioImage(ImageReader reader, RenderedImage image) {
			super(image);
			this.reader = reader;
		}
		@Override
		public void close() {
			closeSilent(reader);
		}
	}

	private static ImageInputStream toStream(URL url, boolean buffer) throws IOException {
		if (isFile(url)) {
			return toStream(toFile(url), buffer);
		}
		URLConnection urlConn = url.openConnection();
		InputStream is = urlConn.getInputStream();
		try {
			if (buffer) {
				String lenStr = urlConn.getHeaderField("content-length");
				if (!StringUtil.isNullOrEmpty(lenStr)) {
					long len = Long.parseLong(lenStr);
					if (len < MAX_BUFFERED_LEN) {
						byte[] data = IOUtilJava.readFully(is);
						IOUtil.closeSilent(is);
						return toStream(data, data.length);
					}
				}
			}
		} catch (Throwable t) {
		}
		return ImageIO.createImageInputStream(is);
	}

	private static ImageInputStream toStream(File f, boolean buffer) throws IOException {
		// long start = System.currentTimeMillis();
		if (f.getName().toUpperCase().endsWith("ECW")) {
			//TODO: read ECW file as RenderedImage
			throw new UnsupportedOperationException("ECW files not supported");
		}
		if (buffer && (f.length() < MAX_BUFFERED_LEN)) {
			byte[] data = FileUtilJava.copyFileToMem(f);
			return toStream(data, data.length);
		}
		return ImageIO.createImageInputStream(f);
	}

	private static ImageInputStream toStream(byte[] data, int len) {
		return new ByteArrayImageInputStream(data, len);
	}

	private static RenderedImage readFirst(ImageReader ir) throws IOException {
		return ir.readAsRenderedImage(ir.getMinIndex(), ir.getDefaultReadParam());
	}

	private static SGRenderedImage wrap(ImageReader ir, RenderedImage image) {
		if (image instanceof SGRenderedImage) {
			return (SGRenderedImage)image;
		}
		if (image instanceof BufferedImage) {
			closeSilent(ir);
			return RasterUtilJava.wrap((BufferedImage)image);
		}
		return new WrappedJiioImage(ir, image);
	}

	private static ImageReader findAndPrepareImageReader(ImageInputStream is, boolean skipMetadata) {
		Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(is);
		if (imageReaders.hasNext()) {
			ImageReader ir = imageReaders.next();
			ir.setInput(is, true, skipMetadata);
			return ir;
		}
		throw new UnsupportedOperationException("Image type not supported.");
	}

	public static SGRenderedImage readRendered(URL imageURL) throws IOException {
		return readRendered(toStream(imageURL, false));
	}

	public static SGRenderedImage readRendered(File in) throws IOException {
		return readRendered(toStream(in, false));
	}

	public static SGRenderedImage readRendered(byte[] data, int len) throws IOException {
		return readRendered(toStream(data, len));
	}

	private static SGRenderedImage readRendered(ImageInputStream is) throws IOException {
		ImageReader ir = findAndPrepareImageReader(is, true);
		if (ir != null) {
			return wrap(ir, readFirst(ir));
		}
		try {
			return RasterUtilJava.wrap(ImageIO.read(is));
		} finally {
			closeSilent(is);
		}
	}

	public static DimI readImageSize(URL imageURL) throws IOException {
		ImageInputStream stream = toStream(imageURL, false);
		try {
			return readImageSize(stream);
		} finally {
			closeSilent(stream);
		}
	}

	private static DimI readImageSize(ImageInputStream is) throws IOException {
		ImageReader ir = findAndPrepareImageReader(is, true);
		if (ir == null) {
			throw new UnsupportedOperationException("Image format not supported by Java ImageIO");
		}
		try {
			return new DimI(ir.getWidth(ir.getMinIndex()), ir.getHeight(ir.getMinIndex()));
		} finally {
			closeSilent(ir);
		}
	}

	public static BufferedImage readBuffered(File f) throws IOException {
		return readBuffered(toStream(f, true));
	}

	private static BufferedImage readBuffered(ImageInputStream is) throws IOException {
		try {
			return ImageIO.read(is);
		} finally {
			closeSilent(is);
		}
	}

	public static IIOMetadata readImageMetadata(URL url) throws IOException {
		ImageInputStream stream = toStream(url, false);
		try {
			return readImageMetadata(stream);
		} finally {
			closeSilent(stream);
		}
	}

	public static void closeSilent(ImageInputStream stream) {
		if (stream == null) {
			return;
		}
		try {
			stream.close();
		} catch (Throwable t) {
			// ignore
		}
	}

	public static IIOMetadata readImageMetadata(File in) throws IOException {
		ImageInputStream stream = toStream(in, false);
		try {
			return readImageMetadata(stream);
		} finally {
			closeSilent(stream);
		}
	}

	private static IIOMetadata readImageMetadata(ImageInputStream stream) throws IOException {
		ImageReader rdr = findAndPrepareImageReader(stream, false);
		try {
			return rdr.getImageMetadata(rdr.getMinIndex());
		} finally {
			closeSilent(rdr);
		}
	}

	public static void closeSilent(ImageReader rdr) {
		if (rdr == null) {
			return;
		}
		try {
			rdr.dispose();
		} catch (Throwable t) {
			//ignore
		}
	}

	public static void writeJpeg(BufferedImage img, OutputStream os, int quality) throws IOException {
		ImageWriter iw = ImageIO.getImageWritersByMIMEType("image/jpeg").next();
		ImageWriteParam iwp = iw.getDefaultWriteParam();
		iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		iwp.setCompressionQuality(0.01f * quality);

		MemoryCacheImageOutputStream ios = new MemoryCacheImageOutputStream(os);
		try {
			iw.setOutput(ios);
			iw.write(null, new IIOImage(img, null, null), iwp);
		} finally {
			closeSilent(iw);
			closeSilent(ios);
			IOUtil.closeSilent(os);
		}
	}

	public static ByteArrayOutputStream encodeJpeg(BufferedImage img, float quality) throws IOException {
		ImageWriter iw = ImageIO.getImageWritersByMIMEType("image/jpeg").next();
		ImageWriteParam iwp = iw.getDefaultWriteParam();
		iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		iwp.setCompressionQuality(0.01f * quality);
		return encodeInMemory(img, iw, iwp);
	}

	private static ByteArrayOutputStream encodeInMemory(BufferedImage img, ImageWriter iw, ImageWriteParam iwp) throws IOException {
		ByteArrayImageOutputStream os = new ByteArrayImageOutputStream((int)(0.6 * img.getHeight()*img.getWidth()));
		try {
			iw.setOutput(os);
			iw.write(null, new IIOImage(img, null, null), iwp);
		} finally {
			closeSilent(iw);
			closeSilent(os);
		}
		return os.getStream();
	}

	private static void closeSilent(ImageWriter iw) {
		if (iw == null) {
			return;
		}
		try {
			iw.dispose();
		} catch (Throwable t) {
			// ignore
		}
	}

	public static ByteArrayOutputStream encodePng(BufferedImage img) throws IOException {
		ImageWriter iw = ImageIO.getImageWritersByMIMEType("image/png").next();
		return encodeInMemory(img, iw, null);
	}

}
