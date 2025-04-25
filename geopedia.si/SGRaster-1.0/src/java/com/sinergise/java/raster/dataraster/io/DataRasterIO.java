package com.sinergise.java.raster.dataraster.io;

import static com.sinergise.java.raster.dataraster.io.PNGDataIO.loadPNG;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.imageio.stream.MemoryCacheImageInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.raster.dataraster.AbstractShortDataBank;
import com.sinergise.common.raster.dataraster.ShortDataBank;
import com.sinergise.common.util.geom.EnvelopeL;
import com.sinergise.java.util.io.StreamBinaryInput;
import com.sinergise.java.util.io.StreamBinaryOutput;

public class DataRasterIO {

	public static final String formatSuffix(final int outFormat) {
		switch (outFormat) {
			case FORMAT_RAW:
			case FORMAT_RAW_OLD:
				return ".raw";
			case FORMAT_SDM:
				return ".sdm";
			case FORMAT_PNG:
				return ".png";
			case FORMAT_RAWZIP:
				return ".rwz";
			case FORMAT_PGM:
				return ".pgm";
		}
		return null;
	}

	/**
	 * @param outFile
	 * @param dmv
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void saveRaw(File outFile, ShortDataBank dmv) throws FileNotFoundException, IOException {
		FileOutputStream fos = new FileOutputStream(outFile);
		BufferedOutputStream bos = new BufferedOutputStream(fos, 2 * dmv.getWidth());
		try {
			saveRaw(bos, dmv);
		} finally {
			try {
				bos.flush();
				bos.close();
				fos.close();
			} catch(Throwable t) {}
		}
	}

	public static void saveRaw(OutputStream outStream, AbstractShortDataBank dmv, int rawFormat) throws IOException {
		StreamBinaryOutput bos = new StreamBinaryOutput(outStream);
		writeHeader(dmv, rawFormat, bos);

		EnvelopeL dmvEn = dmv.getEnvelope();
		for (long y = dmvEn.getMinY(); y <= dmvEn.getMaxY(); y++) {
			for (long x = dmvEn.getMinX(); x < dmvEn.getMaxX(); x++) {
				bos.writeShort(dmv.getShortValue(x, y));
			}
		}
		bos.flush();
	}

	public static void saveRaw(OutputStream outStream, ShortDataBank dmv) throws IOException {
		saveRaw(outStream, dmv, FORMAT_RAW);
	}

	/**
	 * @param outFile
	 * @param dmv
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void saveRawZIP(File outFile, ShortDataBank dmv) throws FileNotFoundException, IOException {
		FileOutputStream fos = new FileOutputStream(outFile);
		try {
			saveRawZIP(fos, dmv);
		} finally {
			fos.close();
		}
	}

	public static void saveRawZIP(OutputStream os, ShortDataBank dmv) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(os);
		zos.setLevel(Deflater.BEST_COMPRESSION);
		ZipEntry ze = new ZipEntry("data.raw");
		zos.putNextEntry(ze);
		saveRaw(zos, dmv);
		zos.closeEntry();
	}

	static void writeHeader(AbstractShortDataBank dmv, int rawFormat, DataOutput bo) throws IOException {
		if (rawFormat == FORMAT_RAW_OLD) {
			EnvelopeL env = dmv.getEnvelope();
			bo.writeLong(env.getMinX()); //8
			bo.writeLong(env.getMinY()); //16
			bo.writeInt((int)env.getWidth()); //20
			bo.writeInt((int)env.getHeight()); //24
		} else {
			SDMFormat.writeHeader(dmv, bo);
		}
	}

	public static void save(OutputStream outStream, ShortDataBank dmv, int format) throws IOException {
		if (format == FORMAT_RAW_OLD || format == FORMAT_RAW) {
			saveRaw(outStream, dmv, format);
			return;
		} else if (format == FORMAT_RAWZIP) {
			saveRawZIP(outStream, dmv);
		} else if (format == FORMAT_SDM) {
			SDMFormat.save(outStream, dmv);
		} else if (format == FORMAT_PGM) {
			PGMFormat.savePGM(outStream, dmv);
		}
	}

	public static final int FORMAT_RAW = 1;
	public static final int FORMAT_RAW_OLD = 0;
	public static final int FORMAT_RAWZIP = 4;
	public static final int FORMAT_SDM = 2;
	public static final int FORMAT_PGM = 8;

	static final Logger logger = LoggerFactory.getLogger(DataRasterIO.class);

	@Deprecated
	public static ShortDataBank load(InputStream str) throws IOException {
		if (!str.markSupported()) {
			str = new BufferedInputStream(str, 1024);
		}
		str.mark(1024);
		try {
			PNGDataIO.checkPNGSupport();
			MemoryCacheImageInputStream iis = new MemoryCacheImageInputStream(str);
			if (PNGDataIO.PNG_READER_SPI.canDecodeInput(iis)) {
				return loadPNG(iis);
			}
		} catch(Exception e) {
			logger.warn("Failed when trying to read PNG", e);
		}
		str.reset();
		return (ShortDataBank)SDMFormat.load(str);
	}

	public static ShortDataBank load(InputStream str, int format) throws IOException {
		switch (format) {
			case FORMAT_RAW:
			case FORMAT_RAW_OLD:
				return loadRaw(str);
			case FORMAT_RAWZIP:
				return loadRawZip(str);
			case FORMAT_PNG:
				return loadPNG(str);
			case FORMAT_SDM:
				return (ShortDataBank)SDMFormat.load(str);
		}
		throw new UnsupportedOperationException("Unknown format (" + format + ")");
	}

	public static ShortDataBank load(URL url) throws IOException {
		int format = FORMAT_RAW;
		String fName = url.toExternalForm().toLowerCase();
		if (fName.endsWith(formatSuffix(FORMAT_RAW))) {
			format = FORMAT_RAW;
		} else if (fName.endsWith(formatSuffix(FORMAT_RAWZIP))) {
			format = FORMAT_RAWZIP;
		} else if (fName.endsWith(formatSuffix(FORMAT_PNG))) {
			format = FORMAT_PNG;
		} else if (fName.endsWith(formatSuffix(FORMAT_SDM))) {
			format = FORMAT_SDM;
		}

		ShortDataBank ret = null;
		InputStream is = url.openStream();
		try {
			ret = load(is, format);
		} finally {
			is.close();
		}
		return ret;
	}

	public static ShortDataBank loadRaw(InputStream inputStream) throws IOException {
		StreamBinaryInput bis = new StreamBinaryInput(new BufferedInputStream(inputStream));
		try {
			ShortDataBank data = SDMFormat.readHeader(bis).bank;
			data.checkDataStore();

			final int w = data.getWidth();
			final int h = data.getHeight();

			byte[] tmp = new byte[2 * w];
			short[][] allDta = data.getDataBuffer();
			for (int y = 0; y < h; y++) {
				bis.readFully(tmp);
				short[] s = allDta[y];
				int pos = 0;
				for (int x = 0; x < w; x++) {
					s[x] = (short)(((tmp[pos++] & 0xFF) << 8) | (tmp[pos++] & 0xFF));
				}
			}
			return data;
		} finally {
			bis.close();
		}
	}

	/**
	 * @param dataFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static ShortDataBank loadRaw(String dataFile) throws FileNotFoundException, IOException {
		FileInputStream fis = new FileInputStream(dataFile);
		try {
			return loadRaw(new FileInputStream(dataFile));
		} finally {
			try {
				fis.close();
			} catch(Throwable t) {}
		}
	}

	/**
	 * @param dataFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static ShortDataBank loadRawZip(File f) throws FileNotFoundException, IOException {
		ZipFile zf = new ZipFile(f);
		ZipEntry ze = zf.entries().nextElement();
		return loadRaw(zf.getInputStream(ze));
	}

	public static ShortDataBank loadRawZip(InputStream is) throws FileNotFoundException, IOException {
		ZipInputStream zis = new ZipInputStream(is);
		zis.getNextEntry();
		return loadRaw(zis);
	}

	public static void main(String[] args) {
		try {
			long t = 0;
			ShortDataBank dta;
			{
				t = System.currentTimeMillis();
				dta = loadRaw("D:\\Data\\GeoData\\mus\\dem\\Mauritius_DTM.raw");
				System.out.println("RAW loaded in " + (System.currentTimeMillis() - t) + " ms."); // 0.75 s

				dta.compact();

				//					t = System.currentTimeMillis();
				//					short zero = dta.calcShortVal(0);
				//					short[][] s = dta.getDataBuffer();
				//					for (int i = 0; i < s.length; i++) {
				//						for (int j = 0; j < s[0].length; j++) {
				//							if (s[i][j]==NO_DATA_SHORT) s[i][j] = zero;
				//						}
				//					}
				//					System.out.println("Filled in " + (System.currentTimeMillis() - t) + " ms."); // 0.75 s

				t = System.currentTimeMillis();
				FileOutputStream fos = new FileOutputStream("D:\\Data\\GeoData\\mus\\dem\\Mauritius_DTM.pgm");
				try {
					PGMFormat.savePGM(fos, dta);
				} finally {
					fos.close();
				}
				System.out.println("PGM saved in " + (System.currentTimeMillis() - t) + " ms."); // 0.75 s

				t = System.currentTimeMillis();
				SDMFormat.save(new FileOutputStream("D:\\Data\\GeoData\\mus\\dem\\Mauritius_DTM.sdm"), dta);
				System.out.println("Delta saved in " + (System.currentTimeMillis() - t) + " ms."); // 0.75 s
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static final int FORMAT_PNG = 3;

}
