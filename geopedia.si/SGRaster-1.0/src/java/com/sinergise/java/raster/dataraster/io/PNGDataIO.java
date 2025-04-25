package com.sinergise.java.raster.dataraster.io;

import static com.sinergise.java.raster.dataraster.io.DataRasterIO.FORMAT_RAW;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import com.sinergise.common.raster.dataraster.ShortDataBank;
import com.sinergise.java.raster.core.RasterIoJiio;
import com.sinergise.java.util.io.ByteArrayInputStream;
import com.sinergise.java.util.io.StreamBinaryInput;
import com.sinergise.java.util.io.StreamBinaryOutput;
import com.sun.imageio.plugins.png.PNGMetadata;
import com.sun.media.imageioimpl.plugins.png.CLibPNGMetadata;

public class PNGDataIO {
	protected static ImageWriterSpi PNG_WRITER_SPI;
	protected static ImageReaderSpi PNG_READER_SPI;
	protected static void checkPNGSupport() {
		if (PNG_WRITER_SPI == null) { // Lazy because ImageIO doesn't really help getting Spi
			ImageWriter iw = ImageIO.getImageWritersByFormatName("png").next();
			PNG_WRITER_SPI = iw.getOriginatingProvider();
			PNG_READER_SPI = ImageIO.getImageReader(iw).getOriginatingProvider();
		}
	}
	@SuppressWarnings("unchecked")
	public static void savePNG(OutputStream outStream, ShortDataBank dmv) throws IOException {
		checkPNGSupport();
		final int w = dmv.getWidth();
		final int h = dmv.getHeight();
	
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_USHORT_GRAY);
		DataBufferUShort dbs = (DataBufferUShort)img.getRaster().getDataBuffer();
		
		short[] tgtArr = dbs.getData();
		short[][] data = dmv.getDataBuffer();
		for (int y = 0; y < h; y++) {
			System.arraycopy(data[y], 0, tgtArr, y*w, w);
		}
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		StreamBinaryOutput bos = new StreamBinaryOutput(baos);
		DataRasterIO.writeHeader(dmv, FORMAT_RAW, bos);
		bos.close();
		baos.close();
		
		ImageOutputStream ios = ImageIO.createImageOutputStream(outStream);
		ImageWriter iw = PNG_WRITER_SPI.createWriterInstance();
		iw.setOutput(ios);
		
		ImageWriteParam iwp = iw.getDefaultWriteParam();
		iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		iwp.setCompressionQuality(0.0f);
		
		IIOMetadata md = iw.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(img),iwp);
		if (md instanceof CLibPNGMetadata) {
			CLibPNGMetadata pngm = (CLibPNGMetadata)md;
			pngm.unknownChunkType.add("SiNd");
			pngm.unknownChunkData.add(baos.toByteArray());
		} else if (md instanceof PNGMetadata) {
			PNGMetadata pngm = (PNGMetadata)md;
			pngm.unknownChunkType.add("SiNd");
			pngm.unknownChunkData.add(baos.toByteArray());
		}
		iw.write(null, new IIOImage(img, null, md), iwp);
		ios.flush();
	}
	public static ShortDataBank loadPNG(InputStream is) throws IOException {
		checkPNGSupport();
		ImageInputStream iis = ImageIO.createImageInputStream(is);
		return loadPNG(iis);
	}
	@SuppressWarnings("unchecked")
	public static ShortDataBank loadPNG(ImageInputStream iis) throws IOException {
		IIOMetadata md;
		BufferedImage bi;
		ImageReader ir = null;
		try {
			ir = PNG_READER_SPI.createReaderInstance();
			ir.setInput(iis);
			IIOImage iimg = ir.readAll(0, null);
			md = iimg.getMetadata();
			bi = (BufferedImage)iimg.getRenderedImage();
			iis.close();
		} finally {
			RasterIoJiio.closeSilent(ir);
			RasterIoJiio.closeSilent(iis);
		}
	
		final int w = bi.getRaster().getWidth();
		final int h = bi.getRaster().getHeight();
	
		ArrayList<String> chunkTypes = null;
		ArrayList<byte[]> chunkData = null;
		if (md instanceof CLibPNGMetadata) {
			CLibPNGMetadata pngm = (CLibPNGMetadata)md;
			chunkTypes = pngm.unknownChunkType;
			chunkData = pngm.unknownChunkData;
		} else if (md instanceof PNGMetadata) {
			PNGMetadata pngm = (PNGMetadata)md;
			chunkTypes = pngm.unknownChunkType;
			chunkData = pngm.unknownChunkData;
		}
	
		ShortDataBank dmv = null;
	
		if (chunkTypes != null && chunkData != null) {
			StreamBinaryInput headerStream = null;
			for (int i = 0; i < chunkTypes.size(); i++) {
				if ("SiNd".equals(chunkTypes.get(i))) {
					headerStream = new StreamBinaryInput(new ByteArrayInputStream(chunkData.get(i)));
					break;
				}
			}
			if (headerStream != null) {
				dmv = SDMFormat.readHeader(headerStream).bank;
			}
		}
	
		if (dmv == null) {
			final int minx = bi.getRaster().getMinX();
			final int miny = bi.getRaster().getMinY();
			dmv = new ShortDataBank(1, 1, 0, 1);
			dmv.expandToInclude(minx, miny, minx + w - 1, miny + h - 1);
		} else {
			dmv.checkDataStore();
		}
	
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
