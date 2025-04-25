package com.sinergise.java.raster.dataraster.dem;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.java.raster.dataraster.dem.DEMReader.WithMatrix;
import com.sinergise.java.util.string.StringSerializer;

@SuppressWarnings("boxing")
public class UsgsDemReader extends WithMatrix {
	public static class Header {
		public <S> HeaderField<S> create(Class<S> s, int minByte, int maxByte) {
			return new HeaderField<S>(s, minByte, maxByte);
		}
		
		public HeaderField<String> createString(int minByte, int maxByte) {
			return create(String.class, minByte, maxByte);
		}
		public HeaderField<Integer> createInt(int minByte, int maxByte) {
			return create(Integer.class, minByte, maxByte);
		}
		public class HeaderField<T> {
			private final Class<T> valueType;
			private final int minByte;
			private final int maxByte;
			
			HeaderField(Class<T> valueType, int minByte, int maxByte) {
				this.valueType = valueType;
				this.minByte = minByte;
				this.maxByte = maxByte;
			}

			public T read() {
				return StringSerializer.valueOf(readString().trim(), valueType);
			}

			protected String readString() {
				String subStr = header.substring(Math.min(header.length()-1, minByte-1), Math.min(maxByte, header.length()));
				return subStr;
			}
			
			protected double[] readReals(int howMany) {
				double[] ret = new double[howMany];
				for (int i = 0; i < howMany; i++) {
					int start = Math.min(header.length()-1, minByte-1 + i*24);
					ret[i] = Double.parseDouble(header.substring(start, start+24));
				}
				return ret;
			}
		}
		
		public static enum Unit {
			RADIANS, FEET, METERS, ARC_SECONDS;
			public int getCode() {
				return ordinal();
			}
			
			public static Unit valueOf(int code) {
				return values()[code];
			}
		}

		public final class UnitHeader extends HeaderField<Unit> {
			public UnitHeader(int minByte, int maxByte) {
				super(Unit.class, minByte, maxByte);
			}
			@Override
			public Unit read() {
				return Unit.valueOf(Integer.parseInt(readString()));
			}
		}
		
		public HeaderField<String> FILE_NAME = createString(1, 40);
		public HeaderField<String> SE_CORNER_LON = createString(110, 122);
		public HeaderField<String> SE_CORNER_LAT = createString(123, 135);
		public HeaderField<Integer> REF_SYSTEM_TYPE = createInt(157, 162);
		public HeaderField<Integer> REF_SYSTEM_ZONE = createInt(153, 168);
		public HeaderField<String>  REF_SYSTEM_PARAMS = createString(169, 528);
		public HeaderField<Unit> REF_SYSTEM_UOM = new UnitHeader(529, 5534);
		public HeaderField<Unit> ELEVATION_UOM = new UnitHeader(535, 540);
		
		public HeaderField<Envelope> ENVELOPE = new HeaderField<Envelope>(Envelope.class, 547, 738) {
			@Override
			public Envelope read() {
				double[] ordinates = readReals(8);
				EnvelopeBuilder eb = new EnvelopeBuilder();
				for (int i = 0; i < ordinates.length; i+=2) {
					eb.expandToInclude(ordinates[i], ordinates[i+1]);
				}
				return eb.getEnvelope();
			}
		};
		
		public HeaderField<Double> MIN_Z = create(Double.class, 739, 762);
		public HeaderField<Double> MAX_Z = create(Double.class, 763, 786);

		public HeaderField<Double> RES_X = create(Double.class, 817, 828);
		public HeaderField<Double> RES_Y = create(Double.class, 829, 840);
		public HeaderField<Double> RES_Z = create(Double.class, 841, 852);

		public HeaderField<Integer> N_ROWS = create(Integer.class, 853, 858);
		public HeaderField<Integer> N_COLS = create(Integer.class, 859, 864);

		
		public HeaderField<Integer> PROFILE_ROW = create(Integer.class, 1, 6);
		public HeaderField<Integer> PROFILE_COL = create(Integer.class, 7, 12);

		public HeaderField<Integer> PROFILE_H = create(Integer.class, 13, 18);
		public HeaderField<Integer> PROFILE_W = create(Integer.class, 19, 24);

		public HeaderField<Double> PROFILE_X = create(Double.class, 25, 48);
		public HeaderField<Double> PROFILE_Y = create(Double.class, 49, 72);

		public HeaderField<Double> PROFILE_DATUM_Z = create(Double.class, 73, 96);
		public HeaderField<Double> PROFILE_MIN_Z = create(Double.class, 97, 120);
		public HeaderField<Double> PROFILE_MAX_Z = create(Double.class, 121, 144);
		
		private final String header;
		
		public Header(String data) {
			this.header = data;
		}
		
		public String getFileName() {
			return FILE_NAME.read();
		}

		public int getNRows() {
			int m = N_ROWS.read();
			if (m > 1) {
				return m;
			}
			Envelope env = ENVELOPE.read();
			return (int)(env.getHeight() / RES_Y.read() + 1);
		}
	}
	
	private Header hdr;
	private double[] data;
	private double zStepNom = 1;
	private double zStepDen = 1;
	
	public UsgsDemReader(InputStream is) throws IOException {
		try {
			InputStreamReader reader = new InputStreamReader(is, "US-ASCII");
			readHeader(reader);
			for (int i = 0; i < w; i++) {
				readProfile(reader);
			}
		} finally {
			is.close();
		}
	}

	private void readProfile(Reader rdr) throws UnsupportedEncodingException, IOException {
		Header profileHead = readHeader(rdr, 144);
		int numRead = 144;
		int col0 = profileHead.PROFILE_COL.read() - 1;
		int row0 = profileHead.PROFILE_ROW.read() - 1;
		int curW = profileHead.PROFILE_W.read();
		int curH = profileHead.PROFILE_H.read();
		double localZ = profileHead.PROFILE_DATUM_Z.read();
		char[] zValBuf = new char[6];
		for (int col = 0; col < curW; col++) {
			for (int row = 0; row < curH; row++) {
				numRead += rdr.read(zValBuf);
				double curZ = localZ + zStepNom * Integer.parseInt(String.valueOf(zValBuf).trim()) / zStepDen;
				data[h * (col0 + col) + (h - 1 - row - row0)] = curZ;	
				if (numRead + zValBuf.length > 1024) {
					rdr.read(new char[1024-numRead]);
					numRead = 0;
				}
			}
		}
		if (numRead < 1024) {
			rdr.read(new char[1024 - numRead]);
		}
	}

	private void readHeader(Reader is) throws IOException {
		hdr = readHeader(is, 1024);
		
		Envelope env = hdr.ENVELOPE.read();
		initialize(hdr.N_COLS.read(), hdr.getNRows(), hdr.RES_X.read(), hdr.RES_Y.read(), env.getMinX(), env.getMinY());
		
		double zStep = hdr.RES_Z.read();
		if (zStep < 1) {
			zStepDen = tryRound(1 / zStep);
		} else {
			zStepNom = tryRound(zStep);
		}
		
		data = new double[w*h];
	}

	private static double tryRound(double val) {
		if (Math.abs((Math.round(val) - val)/val) < 1e-3) {
			return Math.round(val);
		}
		return val;
	}

	private static Header readHeader(Reader rdr, int headerLength) throws IOException, UnsupportedEncodingException {
		char[] hdrChars = new char[headerLength];
		rdr.read(hdrChars);
		return new Header(new String(hdrChars));
	}
	
	@Override
	public void close() {
	}

	@Override
	public double getZ(int east, int south) {
		return data[h*east + south];
	}

	public Header getHeader() {
		return hdr;
	}

}
