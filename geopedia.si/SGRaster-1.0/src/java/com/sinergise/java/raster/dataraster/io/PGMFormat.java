package com.sinergise.java.raster.dataraster.io;

import static com.sinergise.java.raster.dataraster.io.PGMFormat.WKTConsts.BAND;
import static com.sinergise.java.raster.dataraster.io.PGMFormat.WKTConsts.BANDS;
import static com.sinergise.java.raster.dataraster.io.PGMFormat.WKTConsts.MIN_INDEX;
import static com.sinergise.java.raster.dataraster.io.PGMFormat.WKTConsts.SDM;
import static com.sinergise.java.raster.dataraster.io.PGMFormat.WKTConsts.SIZE;
import static com.sinergise.java.raster.dataraster.io.PGMFormat.WKTConsts.TYPE;
import static com.sinergise.java.raster.dataraster.io.PGMFormat.WKTConsts.VAL_OFFSET;
import static com.sinergise.java.raster.dataraster.io.PGMFormat.WKTConsts.VAL_SCALE;
import static com.sinergise.java.raster.dataraster.io.PGMFormat.WKTConsts.WKT_VERSION;
import static com.sinergise.java.raster.dataraster.io.PGMFormat.WKTConsts.WORLD_OFFSET;
import static com.sinergise.java.raster.dataraster.io.PGMFormat.WKTConsts.WORLD_STEP;
import static com.sinergise.java.raster.dataraster.io.SDMFormat.BandType.SHORT;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.sinergise.common.geometry.crs.io.ReferencingWktEntity;
import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.raster.dataraster.AbstractShortDataBank;
import com.sinergise.common.raster.dataraster.SGDataBank;
import com.sinergise.common.raster.dataraster.ShortDataBank;
import com.sinergise.common.util.geom.EnvelopeL;
import com.sinergise.java.raster.dataraster.io.SDMFormat.SDMFileInfo;
import com.sinergise.java.util.io.BinaryOutput;
import com.sinergise.java.util.io.IOUtilJava;
import com.sinergise.java.util.io.StreamBinaryInput;
import com.sinergise.java.util.io.StreamBinaryOutput;

public class PGMFormat {
	public static final class WKTConsts {
		public static final String SDM = "SDM";
		public static final String WKT_VERSION = "VERSION";
		public static final String SIZE = "SIZE";
		public static final String WORLD_OFFSET = "WORLD_OFFSET";
		public static final String WORLD_STEP = "WORLD_STEP";
		public static final String MIN_INDEX = "MIN_INDEX";
		public static final String BANDS = "BANDS";
		public static final String BAND = "BAND";
		public static final String TYPE = "TYPE";
		public static final String VAL_OFFSET = "VAL_OFFSET";
		public static final String VAL_SCALE = "VAL_SCALE";   
	}
	
	public static final class PGMHeader {
		int w;
		int h;
		int maxVal;
		ArrayList<String> comments;
	}

	protected static final int PGM_VERSION = 1;

	private PGMFormat() {
	}

	protected static ReferencingWktEntity buildWKTHeader(AbstractShortDataBank sdb, int version) {
		AffineTransform2D tr = sdb.getWorldTr();
		EnvelopeL env = sdb.getEnvelope();
		
		ReferencingWktEntity root = new ReferencingWktEntity(SDM);
		root.addChild(new ReferencingWktEntity(WKT_VERSION, version));
		root.addChild(new ReferencingWktEntity(SIZE,new int[]{sdb.getWidth(), sdb.getHeight()}));
		root.addChild(new ReferencingWktEntity(WORLD_OFFSET,new double[]{tr.getTranslateX(), tr.getTranslateY()}));
		root.addChild(new ReferencingWktEntity(WORLD_STEP,new double[]{tr.getScaleX(), tr.getScaleY()}));
		root.addChild(new ReferencingWktEntity(MIN_INDEX,new long[]{env.getMinX(), env.getMinY()}));
		
		ReferencingWktEntity bands = new ReferencingWktEntity(BANDS);
		{
			ReferencingWktEntity curBand = new ReferencingWktEntity(BAND);
			curBand.addChild(new ReferencingWktEntity(TYPE, SHORT.name()));
			curBand.addChild(new ReferencingWktEntity(VAL_OFFSET, sdb.zMin));
			curBand.addChild(new ReferencingWktEntity(VAL_SCALE, sdb.zScale));
			bands.addChild(curBand);
		}
		root.addChild(bands);
		return root;
	}

	public static SDMFileInfo<ShortDataBank> readHeaderWKT(InputStream is) throws IOException {
		return readHeaderWKT(is, new ShortDataBank(1, 1, 0, 1));
	}

	public static SDMFileInfo<ShortDataBank> readHeaderWKT(String text) {
		return readHeaderWKT(text, new ShortDataBank(1, 1, 0, 1));
	}
	
	public static <T extends SGDataBank> SDMFileInfo<T> readHeader(DataInput bis, T bank) throws IOException {
		PGMHeader head = readPGMHeader(bis);
		for (String comment : head.comments) {
			String s = comment.trim();
			if (s.startsWith(WKTConsts.SDM+"[")) {
				return setupBankFromWKT(ReferencingWktEntity.parseEntity(s), bank);
			}
		}
		throw new IOException("Missing SDM header in PGM comments");
	}
	
	public static PGMHeader readPGMHeader(DataInput is) throws IOException {
		ArrayList<String> comments = new ArrayList<String>();
		String magic = parsePNMToken(is, comments, false);
		if ("!P5".equals(magic)) {
			throw new IOException("Not a PGM header - magic string was "+magic);
		}
		
		PGMHeader ret = new PGMHeader();
		ret.w = Integer.parseInt(parsePNMToken(is, comments, true));
		ret.h = Integer.parseInt(parsePNMToken(is, comments, true));
		ret.maxVal = Integer.parseInt(parsePNMToken(is, comments, true));
		ret.comments = comments;
		return ret;
	}

	private static String parsePNMToken(DataInput is, ArrayList<String> comments, boolean skipWhitespaceFirst) throws IOException {
		StringBuilder sb = new StringBuilder();
		char ch = (char)is.readUnsignedByte();
		while (skipWhitespaceFirst && (" \n\r\f\t".indexOf(ch) >=0)) {
			ch = (char)is.readUnsignedByte();
		}
		do {
			if (ch == '#') {
				comments.add(parsePNMComment(is));
				ch = (char)is.readUnsignedByte();
			}
			sb.append(ch);
			ch = (char)is.readUnsignedByte();
		} while(" \n\r\f\t".indexOf(ch) <0);
		return sb.toString();
	}

	private static String parsePNMComment(DataInput is) throws IOException {
		StringBuilder sb = new StringBuilder();
		char ch;
		while (true) {
			ch = (char)is.readUnsignedByte();
			if (ch == '\n' || ch =='\r') { //comment ends with carriage return or newline
				return sb.toString();
			}
			sb.append(ch);
		}
	}

	private static <T extends SGDataBank> SDMFileInfo<T> readHeaderWKT(String text, T bank) {
		return setupBankFromWKT(ReferencingWktEntity.parseEntity(text), bank);
	}
	
	private static <T extends SGDataBank> SDMFileInfo<T> readHeaderWKT(InputStream is, T bank) throws IOException {
		return setupBankFromWKT(ReferencingWktEntity.parseEntityASCII(is), bank);
	}
	private static <T extends SGDataBank> SDMFileInfo<T> setupBankFromWKT(ReferencingWktEntity wktHeader, T bank) {
		int ver = (int)wktHeader.getChild(WKT_VERSION).getLongParamAt(0);
		ReferencingWktEntity size = wktHeader.getChild(SIZE);
		ReferencingWktEntity offw = wktHeader.getChild(WORLD_OFFSET);
		ReferencingWktEntity stepw = wktHeader.getChild(WORLD_STEP);
		ReferencingWktEntity minIdx = wktHeader.getChild(MIN_INDEX);
		ReferencingWktEntity bands = wktHeader.getChild(BANDS);
		
		bank.setWorldTr(AffineTransform2D.createTrScale(stepw.getDoubleParamAt(0),stepw.getDoubleParamAt(1), offw.getDoubleParamAt(0), offw.getDoubleParamAt(1)));
		int matrixW = (int)size.getLongParamAt(0);
		int matrixH = (int)size.getLongParamAt(1);
	
		if (bands.getChildCount() > 1) throw new UnsupportedOperationException("Only 1-band rasters currently supported");
		ReferencingWktEntity curBand = bands.getFirstChild();
	
		// Bands
		if (!SHORT.name().equals(curBand.getChild(TYPE).getFirstParam())) {
			throw new UnsupportedOperationException("Only short (2-byte) rasters currently supported");
		}
		AbstractShortDataBank sBank = (AbstractShortDataBank)bank;
		sBank.zMin = curBand.getChild(VAL_OFFSET).getDoubleParamAt(0);
		sBank.zScale = curBand.getChild(VAL_SCALE).getDoubleParamAt(0);
		bank.setSize(matrixW, matrixH, false);
		bank.forceOffset(minIdx.getLongParamAt(0), minIdx.getLongParamAt(1));
		bank.updateTransient();
		return new SDMFileInfo<T>(ver, bank, matrixW, matrixH);
	}

	public static void writeExtHeaderText(AbstractShortDataBank sdb, BinaryOutput bos, int version) throws IOException {
		bos.writeASCII(buildWKTHeader(sdb, version).toWKTString());
	}

	public static void savePGM(OutputStream outStream, AbstractShortDataBank sdb) throws IOException {
		StreamBinaryOutput bos = new StreamBinaryOutput(outStream);
		final long minRow = sdb.getEnvelope().getMinY();
		final long minCol = sdb.getEnvelope().getMinX();
		int w = sdb.getWidth();
		int h = sdb.getHeight();
		writePGMHeaderText(sdb, bos);

		for (int y = 0; y < h; y++) {
			short[] row = sdb.getDataRow(minRow + y, minCol, w, null, 0);
			for (int x = 0; x < w; x++) {
				bos.writeShort(row[x]);
			}
		}
		bos.flush();
	}

	public static void writePGMHeaderText(AbstractShortDataBank sdb, DataOutput out) throws IOException, UnsupportedEncodingException {
		out.write(buildCustomPGMHeader(sdb).getBytes("US-ASCII"));
	}

	public static String buildCustomPGMHeader(AbstractShortDataBank sdb) {
		return buildPNMHeader(sdb.getWidth(), sdb.getHeight(), 65535, buildWKTHeader(sdb, PGM_VERSION).toWKTString());
	}
	
	public static SGDataBank loadPGM(InputStream is) throws IOException {
		long start = System.nanoTime();
		
		PGMHeader head = readPGMHeader(new StreamBinaryInput(is));
		ShortDataBank ret = null; 
		for (String comment : head.comments) {
			String s = comment.trim();
			if (s.startsWith(WKTConsts.SDM+"[")) {
				ret = new ShortDataBank(1, 1, 0, 1);
				setupBankFromWKT(ReferencingWktEntity.parseEntity(s), ret);
			}
		}
		if (ret == null) {
			throw new IOException("Missing SDM header in PGM comments");
		}
		System.out.println("Read header in "+(System.nanoTime() - start)/1000000.0+ " ms");
		start = System.nanoTime();

		ret.setDataBuffer(readShortRows(is, ret.getWidth(), ret.getHeight()));

		long time = System.nanoTime() - start;
		System.out.println("Loading PGM took "+time/1000000.0+ " ms");
		return ret;
	}

	private static short[][] readShortRows(InputStream is, final int w, final int h) throws IOException {
		final short[][] data = new short[h][w];
		final byte[] tmp = new byte[2 * w];
		for (int y = 0; y < h; y++) {
			IOUtilJava.readFully(is, tmp, 0, 2*w);
			convertToShort(tmp, data[y], w);
		}
		return data;
	}

	private static void convertToShort(final byte[] tmp, final short[] s, final int lengthShorts) {
		int pos = 0;
		for (int x = 0; x < lengthShorts; x++) {
			s[x] = (short)(((tmp[pos++] & 0xFF) << 8) | (tmp[pos++] & 0xFF));
		}
	}

	public static String buildPNMHeader(int rasterW, int matrixH, int maxVal, String comment) {
		return "P5\n"+(comment==null?"":("# "+comment+"\n"))+rasterW+" "+matrixH+"\n"+maxVal+"\n";
	}
}
