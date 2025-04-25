package com.sinergise.java.raster.dataraster.io;

import static com.sinergise.common.raster.dataraster.AbstractShortDataBank.NO_DATA_SHORT;
import static com.sinergise.java.raster.dataraster.io.SDMFormat.BandType.SHORT;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.raster.dataraster.AbstractShortDataBank;
import com.sinergise.common.raster.dataraster.SGDataBank;
import com.sinergise.common.raster.dataraster.ShortDataBank;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.geom.EnvelopeL;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.java.util.io.BinaryInput;
import com.sinergise.java.util.io.BinaryUtilJava;
import com.sinergise.java.util.io.IOUtilJava;
import com.sinergise.java.util.io.StreamBinaryInput;
import com.sinergise.java.util.io.StreamBinaryOutput;

public class SDMFormat {
	public static abstract class SDMPredictor {
		public void readRow(short[] prevRow, short[] curRow, int[] data) {
			short prevVal = calculateHeightVal(NO_DATA_SHORT, prevRow[0], NO_DATA_SHORT, data[0]);
			curRow[0] = prevVal;
			for (int x = 1; x < curRow.length; x++) {
				short curVal = calculateHeightVal(prevVal, prevRow[x], prevRow[x-1], data[x]);
				curRow[x] = curVal;
				prevVal = curVal;
			}
		}
		
		public void writeRow(short[] prevRow, short[] curRow, int[] outData) {
			outData[0] = calculateDeltaVal(NO_DATA_SHORT, prevRow[0], NO_DATA_SHORT, curRow[0]);
			for (int x = 1; x < curRow.length; x++) {
				outData[x] = calculateDeltaVal(curRow[x-1], prevRow[x], prevRow[x-1], curRow[x]);
			}
		}		
		public abstract int predictShort(final short valx, final short valy, final short valxy);

		public short calculateHeightVal(short vx, short vy, short vxy, int delta) {
			return (short)(predictShort(vx, vy, vxy) + delta);
		}

		public int calculateDeltaVal(short vx, short vy, short vxy, short v) {
			return v - predictShort(vx, vy, vxy);
		}
	}
	public static SDMPredictor PREDICTOR_3_WITH_CHECKS = new SDMPredictor() {
		@Override
		public final int predictShort(short valx, short valy, short valxy) {
			return (valx == NO_DATA_SHORT) ? 
						(valy == NO_DATA_SHORT) ? valxy - 0x8000 : valy - 0x8000 
				: (valy == NO_DATA_SHORT) ? valx - 0x8000 
				: (valxy == NO_DATA_SHORT) ? MathUtil.ushortAverage(valx, valy) - 0x8000 
				: (short)(valx + valy - valxy - 0x8000);
		}
	};
	public static SDMPredictor PREDICTOR_3 = new SDMPredictor() {
		@Override
		public final int predictShort(short valx, short valy, short valxy) {
			return valx + valy - valxy;
		}
	};
	
	public static enum SDMVersion {
		V1(PREDICTOR_3_WITH_CHECKS), V2(PREDICTOR_3);
		public final SDMPredictor predictor;
		private SDMVersion(SDMPredictor predictor) {
			this.predictor = predictor;
		}
	}
	
	protected static final int SDM_VERSION = 2;
	
	public static final class SDMReader {
		private ShortDataBank dmv;
		private ZipInputStream zis;
		private byte[] byteBuff;
		private int[] deltaBuff;
		private int h;
		private int w;
		private short[] dummyRow;
		private SDMVersion version;
	
		public SDMReader(InputStream is) throws IOException {
			zis = new ZipInputStream(is);
			readHeaderEntry();
			initializeTemp();
			readDataEntry();
		}

		private void readDataEntry() throws IOException {
			ZipEntry ze = zis.getNextEntry();
			skipPgmHeader(ze);
			
			readData();
			
			zis.closeEntry();
		}

		private void skipPgmHeader(ZipEntry ze) throws IOException {
			if (ze.getName().endsWith(".pgm")) {
				PGMFormat.readPGMHeader(new StreamBinaryInput(zis));
			}
		}

		private void initializeTemp() {
			h = dmv.getHeight();
			w = dmv.getWidth();
			byteBuff = new byte[w * 2];
			deltaBuff = new int[w];
			dummyRow = new short[w];
			Arrays.fill(dummyRow, NO_DATA_SHORT);
		}

		private void readHeaderEntry() throws IOException {
			ZipEntry he = zis.getNextEntry();
			SDMFileInfo<ShortDataBank> header = he.getName().endsWith(".txt") ? readHeaderTxt() : readHeaderBinary();
			version = header.version;
			dmv = header.bank;
			zis.closeEntry();
		}

		private SDMFileInfo<ShortDataBank> readHeaderBinary() throws IOException {
			return readHeader(new StreamBinaryInput(zis));
		}

		private SDMFileInfo<ShortDataBank> readHeaderTxt() throws IOException {
			return PGMFormat.readHeaderWKT(zis);
		}

		private void readData() throws IOException {
			final short[][] dataStore = new short[h][w];
			for (int y = 0; y < h; y++) {
				IOUtilJava.readFully(zis, byteBuff, 0, 2*w);
				BinaryUtilJava.getShortArray(byteBuff, 0, deltaBuff, 0, w);
				
				short[] prevRow = y == 0 ? dummyRow : dataStore[y-1];
				short[] curRow = dataStore[y];
				version.predictor.readRow(prevRow, curRow, deltaBuff);
			}
			dmv.setDataBuffer(dataStore);
		}

		public ShortDataBank getResult() {
			return dmv;
		}
	}

	private SDMFormat() {
	}
	
	public static class SDMFileInfo<T extends SGDataBank> {
		public SDMVersion version;
		public T bank;
		public int w;
		public int h;

		public SDMFileInfo(int version, T bank, int w, int h) {
			this.version = SDMVersion.values()[version-1];
			this.bank = bank;
			this.w = w;
			this.h = h;
		}
	}

	public static final class SDMWriter {
		private final int w;
		private final int h;
		private final long minRow;
		private final long minCol;
		private final AbstractShortDataBank sdb;
		private final OutputStream os;
		private ZipOutputStream zos;
		private byte[] byteBuff;
		private int[] deltaBuff;
		private short[] prevRow;
		private short[] row;

		public SDMWriter(OutputStream outStream, AbstractShortDataBank sdb) {
			this.sdb = sdb;
			this.os = outStream;
			
			w = (int)sdb.getEnvelope().getWidth();
			h = (int)sdb.getEnvelope().getHeight(); 
			minRow = sdb.getEnvelope().getMinY();
			minCol = sdb.getEnvelope().getMinX();
			
			row = new short[w];
			ArrayUtil.fill(row, AbstractShortDataBank.NO_DATA_SHORT);
			
			deltaBuff = new int[w];
			byteBuff = new byte[2*w];

			zos = new ZipOutputStream(os);
			zos.setLevel(9);
		}
		
		public void setCompressionLevel(int level) {
			zos.setLevel(level);
		}
		
		public void write() throws IOException {
			writeHeader();
			writeDelta();
			finishStreams();
		}

		private void finishStreams() throws IOException {
			zos.finish();
			os.flush();
		}
		
		private void writeDelta() throws IOException {
			zos.putNextEntry(new ZipEntry("delta.pgm"));
			writePnmHeader();
			for (int y = 0; y < h; y++) {
				writeRow(y);
			}
			zos.closeEntry();
			zos.flush();
		}

		private void writeRow(int y) throws IOException {
			prevRow = row;
			row = sdb.getDataRow(minRow + y, minCol, w, new short[w], 0);
			
			SDMVersion.V2.predictor.writeRow(prevRow, row, deltaBuff);
			
			BinaryUtilJava.putShortArray(deltaBuff, byteBuff, 0);
			zos.write(byteBuff);
		}

		@SuppressWarnings("resource")
		private void writePnmHeader() throws IOException {
			StreamBinaryOutput bosH = new StreamBinaryOutput(zos);
			bosH.writeASCII(PGMFormat.buildPNMHeader(w, h, 65535, null));
			bosH.flush();
		}

		@SuppressWarnings("resource")
		private void writeHeader() throws IOException {
			String headerString = PGMFormat.buildWKTHeader(sdb, SDM_VERSION).toWKTString();

			ZipEntry he = new ZipEntry("header.txt");
			he.setSize(headerString.length());
			zos.putNextEntry(he);
			
			StreamBinaryOutput bosH = new StreamBinaryOutput(zos);
			bosH.writeASCII(headerString);
			bosH.flush();
			zos.closeEntry();
			zos.flush();
		}
	}
	
	public static enum BandType{ BYTE, SHORT }

	public static void writeHeader(AbstractShortDataBank dmv, DataOutput bo) throws IOException {
		EnvelopeL srcEnv = dmv.getEnvelope();
		bo.writeLong(srcEnv.getMinX()); //8
		bo.writeLong(srcEnv.getMinY()); //16
		bo.writeInt(-SDM_VERSION); //20
		bo.writeInt((int)srcEnv.getWidth()); //24
		bo.writeInt((int)srcEnv.getHeight()); //28
		
		AffineTransform2D worldTr = dmv.getWorldTr();
		bo.writeDouble(worldTr.getTranslateX()); //36
		bo.writeDouble(worldTr.getTranslateY()); //44
		bo.writeDouble(worldTr.getScaleX()); //52
		bo.writeDouble(worldTr.getScaleY()); //60
		
		bo.writeByte(1); //61 // numBands
		bo.writeByte(SHORT.ordinal()); //62 // bandType = SHORT
		bo.writeDouble(dmv.zMin); //70
		bo.writeDouble(dmv.zScale);//78
	}

	public static <T extends SGDataBank> SDMFileInfo<T> readHeader(DataInput bis, T bank) throws IOException {
		long lminx = bis.readLong(); //8
		long lminy = bis.readLong(); //16

		int w = 0;
		int h = 0;
		double xOff = 0;
		double yOff = 0;
		double xStep = 1;
		double yStep = 1;
		double zMin = 0;
		double zScale = 0.1;

		int version = -bis.readInt(); //20
		if (version < 0) {
			w = -version;
			h = bis.readInt(); //24
		} else {
			w = bis.readInt(); //24
			h = bis.readInt(); //28
		}


		if (version > 0) {
			xOff = bis.readDouble(); //36
			yOff = bis.readDouble(); //44
			xStep = bis.readDouble();//52
			yStep = bis.readDouble();//60
		}

		bank.setWorldTr(AffineTransform2D.createTrScale(xStep, yStep, xOff, yOff));

		// Bands
		AbstractShortDataBank sBank = (AbstractShortDataBank)bank;
		if (version == 1) {
			zMin = bis.readDouble(); //68
			zScale = bis.readDouble();//76
		} else {
			int numBands = (bis.readByte() & 0xFF); //61
			if (numBands > 1) throw new UnsupportedOperationException("Only 1-band rasters currently supported");

			int bandType = (bis.readByte() & 0xFF); //62
			if (bandType != BandType.SHORT.ordinal()) throw new UnsupportedOperationException("Only short (2-byte) rasters currently supported");

			zMin = bis.readDouble(); //70
			zScale = bis.readDouble();//78
		}
		sBank.zMin = zMin;
		sBank.zScale = zScale;
		bank.setSize(w, h, false);
		bank.forceOffset(lminx, lminy);
		bank.updateTransient();
		return new SDMFileInfo<T>(version, bank, w, h);
	}

	public static SGDataBank load(InputStream inStream) throws IOException {
		return new SDMReader(inStream).getResult();
	}
	
	public static void save(OutputStream outStream, AbstractShortDataBank sdb) throws IOException {
		new SDMWriter(outStream, sdb).write();
	}

	public static SDMFileInfo<ShortDataBank> readHeader(BinaryInput bis) throws IOException {
		return readHeader(bis, new ShortDataBank(1, 1, 0, 1));
	}
	
	public static void main(String[] args) {
		try {
			//5248 x 6520
			long t = 0;
			ShortDataBank dta;
			t = System.currentTimeMillis();
			dta = DataRasterIO.loadRaw("D:\\Data\\GeoData\\mus\\dem\\Mauritius_DTM.raw");
			System.out.println("RAW loaded in " + (System.currentTimeMillis() - t) + " ms."); // 0.75 s

			dta.compact();

			t = System.currentTimeMillis();
			SDMFormat.save(new FileOutputStream("D:\\Data\\GeoData\\mus\\dem\\Mauritius_DTM.v2.sdm"), dta);
			System.out.println("Delta saved in " + (System.currentTimeMillis() - t) + " ms. "+dta.getWidth()+"x"+dta.getHeight()); // 0.75 s

			if (Math.sqrt(1) == 1) return;
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
