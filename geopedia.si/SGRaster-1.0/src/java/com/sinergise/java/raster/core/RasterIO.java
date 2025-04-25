package com.sinergise.java.raster.core;

import static com.sinergise.java.raster.core.RasterUtilJava.getHintForceBuffered;
import static com.sinergise.java.raster.core.RasterUtilJava.getHintForceBufferedMem;
import static com.sinergise.java.raster.core.RasterUtilJava.isFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;

import com.sinergise.common.util.geom.DimI;
import com.sinergise.java.raster.core.SGRenderedImage.BufferedImageWrapper;
import com.sinergise.java.util.io.ByteArrayOutputStream;
import com.sinergise.java.util.io.FileUtilJava;
import com.sinergise.java.util.io.IOUtilJava;

public class RasterIO {
	public static final int DEFAULT_QUALITY = 90;
	public static void writePngWithBuffer(BufferedImage img, File outFile) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(img.getWidth() * img.getHeight());
		ImageIO.write(img, "PNG", baos);
		FileOutputStream fos = new FileOutputStream(outFile);
		try {
			fos.write(baos.getInternalBuffer(), 0, baos.size());
		} finally {
			fos.close();
		}
	}

	public static BufferedImage readBuffered(File in) throws IOException {
		return readBuffered(in.toURI().toURL());
	}

	public static IIOMetadata readImageMetadata(File in) throws IOException {
		return RasterIoJiio.readImageMetadata(in);
	}


	public static IIOMetadata readImageMetadata(URL url) throws IOException {
		return RasterIoJiio.readImageMetadata(url);
	}

	public static SGRenderedImage readRendered(URL imageURL) {
		if (getHintForceBufferedMem()) {
			try {
				return RasterUtilJava.wrap(readLoadingToMem(imageURL));
			} catch(Exception e) {
				System.err.println("Failed to read image by loading to mem first. Will try direct. (" + imageURL + ")");
				e.printStackTrace();
			}
		}
		SGRenderedImage ret = internalReadRendered(imageURL);
		if (getHintForceBuffered()) {
			try {
				BufferedImageWrapper bufImg = RasterUtilJava.wrap(RasterUtilJava.toBufferedImage(ret));
				ret.close();
				return bufImg;
			} catch (Exception e) {
				System.err.println("Failed to convert image to bufferedImage. Will return rendered. (" + imageURL + ")");
				e.printStackTrace();
			} finally {
			}
		}
		return ret;
	}
	private static SGRenderedImage internalReadRendered(URL imageURL) {
		if (!RasterUtilJava.getHintForceJAI()) {
			try {
				return RasterIoJiio.readRendered(imageURL);
			} catch(Exception e) {
				System.err.println("Failed to read with ImageIO. Will try with JAI. (" + imageURL+ ")");
				e.printStackTrace();
			}
		}
		return RasterIoJai.readRendered(imageURL);
	}

	private static void writeJpeg(BufferedImage img, OutputStream os, int quality) throws IOException {
		writeJpegImageIO(img, os, quality);
	}

	private static void writeJpegImageIO(BufferedImage img, File outFile, int quality) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
		writeJpegImageIO(img, baos, quality);
		RandomAccessFile raf = new RandomAccessFile(outFile, "rw");
		try {
			baos.writeTo(raf);
		} finally {
			raf.close();
		}
	}

	private static void writeJpegImageIO(BufferedImage img, OutputStream os, int quality) throws IOException {
		RasterIoJiio.writeJpeg(img, os, quality);
	}

	public static void write(BufferedImage ret, File f, String imageType, int quality) throws IOException {
		imageType = imageType.toLowerCase();
		if (imageType.endsWith("jpg") || imageType.endsWith("jpeg")) {
			//writeJpeg(ret, f);
			writeJpegImageIO(ret, f, quality);
			return;
		}
		//TODO: add quality for PNG
		if (imageType.endsWith("png")) {
			RasterIoJai.writePng(ret, f);
			return;
		}
		writeGeneric(ret, f, imageType);
	}

	public static void write(BufferedImage ret, OutputStream os, String imageType, int quality) throws IOException {
		imageType = imageType.toLowerCase();
		if (imageType.endsWith("jpg") || imageType.endsWith("jpeg")) {
			writeJpeg(ret, os, quality);
			return;
		}
		if (imageType.endsWith("png")) {
			RasterIoJai.writePng(ret, os);
			return;
		}
		RasterIoJai.writeGeneric(ret, os, imageType);
	}

	public static void writeGeneric(BufferedImage ret, File f, String imageType) throws IOException {
		if (imageType.endsWith("tif")) {
			imageType = "TIFF";
		}
		ImageIO.write(ret, imageType, f);
	}

	public static BufferedImage readBuffered(URL imageURL) {
		SGRenderedImage img = readRendered(imageURL);
		try {
			return RasterUtilJava.toBufferedImage(img);
		} finally {
			dispose(img);
		}
	}
	
	private static SGRenderedImage readRendered(byte[] data, int len) throws IOException {
		if (!RasterUtilJava.getHintForceJAI()) {
			try {
				return RasterIoJiio.readRendered(data, len);
			} catch(Exception e) {
				System.err.println("Failed to read with ImageIO. Will try with JAI. (byte[] len=" + len+ ")");
				e.printStackTrace();
			}
		}
		return RasterIoJai.readRendered(data, len);
	}

	private static BufferedImage readLoadingToMem(URL imageURL) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (isFile(imageURL)) {
			FileUtilJava.copyFile(RasterUtilJava.toFile(imageURL), baos);
		} else {
			InputStream is = imageURL.openStream();
			try {
				IOUtilJava.copyStream(is, baos);
			} finally {
				is.close();
			}
		}
		SGRenderedImage rndImg = readRendered(baos.getInternalBuffer(), baos.size());
		try {
			return RasterUtilJava.toBufferedImage(rndImg);
		} finally {
			dispose(rndImg);
		}
	}

	public static DimI readImageSize(URL imageURL) throws IOException {
		if (!RasterUtilJava.getHintForceJAI()) {
			try {
				return RasterIoJiio.readImageSize(imageURL);
			} catch (UnsupportedOperationException e) {
			}
		}
		return RasterIoJai.readImageSize(imageURL);
	}
	
	public static void writeRendered(SGRenderedImage image, File imageFile, String imageType) throws IOException{
		writeRendered(image, imageFile, imageType, DEFAULT_QUALITY);
	}
	
	

	public static void writeRendered(SGRenderedImage image, File imageFile, String imageType, int quality) throws IOException {
		if (image.isWrapperFor(BufferedImage.class)) {
			write(image.unwrap(BufferedImage.class), imageFile, imageType, quality);
			return;
		}
		ImageOutputStream os = ImageIO.createImageOutputStream(imageFile);
		Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName(imageType);
		if (imageWriters.hasNext()) {
			ImageWriter iw = imageWriters.next();
			iw.setOutput(os);
			iw.write(image);
			return;
		}
		RasterIoJai.writeGeneric(image, imageFile, imageType);
	}

	public static final void dispose(SGRenderedImage img) {
		try {
			img.close();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static ByteArrayOutputStream encodeInMemory(BufferedImage image, String imageType) throws IOException {
		return encodeInMemory(image, imageType, 90);
	}
	/**
	 * @param image
	 * @param imageType
	 * @param quality 0-100
	 * @return
	 * @throws IOException
	 */
	public static ByteArrayOutputStream encodeInMemory(BufferedImage image, String imageType, int quality) throws IOException {
		if (imageType.endsWith("jpg") || imageType.endsWith("jpeg")) {
			return RasterIoJiio.encodeJpeg(image, quality);
			
		} else if (imageType.endsWith("png")) {
			return RasterIoJiio.encodePng(image);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		write(image, baos, imageType, quality);
		return baos;
	}
}
