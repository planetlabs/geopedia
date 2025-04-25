package com.sinergise.java.raster.dataraster.io;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import com.sinergise.common.raster.dataraster.ShortDataBank;
import com.sinergise.java.raster.core.RasterIoJiio;

public class TIFDataIO {
	protected static ImageReaderSpi IMAGE_READER_SPI;
	protected static void checkTIFSupport() {
		ImageWriter iw = ImageIO.getImageWritersByFormatName("tif").next();
		IMAGE_READER_SPI = ImageIO.getImageReader(iw).getOriginatingProvider();
	}
	
	public static ShortDataBank loadTIF(InputStream is) throws IOException {
		checkTIFSupport();
		ImageInputStream iis = ImageIO.createImageInputStream(is);
		return loadTIF(iis);
	}
	
	public static ShortDataBank loadTIF(ImageInputStream iis) throws IOException {
		BufferedImage bi;
		ImageReader ir = null;
		try {
			ir = IMAGE_READER_SPI.createReaderInstance();
			ir.setInput(iis);
			IIOImage iimg = ir.readAll(0, null);
			bi = (BufferedImage)iimg.getRenderedImage();
			iis.close();
		} finally {
			RasterIoJiio.closeSilent(ir);
			RasterIoJiio.closeSilent(iis);
		}
	
		final int w = bi.getRaster().getWidth();
		final int h = bi.getRaster().getHeight();
	
		ShortDataBank dmv = null;
	
		final int minx = bi.getRaster().getMinX();
		final int miny = bi.getRaster().getMinY();
		dmv = new ShortDataBank(1, 1, 0, 1);
		dmv.expandToInclude(minx, miny, minx + w - 1, miny + h - 1);
	
		DataBuffer buf = bi.getRaster().getDataBuffer();
		short[][] dmvData = dmv.getDataBuffer();
		if (buf instanceof DataBufferUShort) {
			short[] data = ((DataBufferUShort)buf).getData();
			for (int y = 0; y < h; y++) {
				System.arraycopy(data, y * w, dmvData[h - y - 1], 0, w);
			}
		} else if (buf instanceof DataBufferByte) {
			byte[] data = ((DataBufferByte)buf).getData();
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					dmvData[h - y - 1][x] = (short)(0xFF & data[y * w + x]);
				}
			}
		}
		return dmv;
	}

}
