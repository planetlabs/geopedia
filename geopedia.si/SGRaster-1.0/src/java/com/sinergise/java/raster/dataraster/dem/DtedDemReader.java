package com.sinergise.java.raster.dataraster.dem;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.java.raster.dataraster.dem.DEMReader.WithMatrix;
import com.sinergise.java.util.string.StringSerializer;

@SuppressWarnings("boxing")
public class DtedDemReader extends WithMatrix {
	private static final int HEADER_LENGTH = 3428;
	
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
		
		public HeaderField<String> UHL_LON = createString(5, 12);
		public HeaderField<String> UHL_LAT = createString(13, 20);
		public HeaderField<Integer> UHL_LON_DELTA_IN_TENTH_OF_S = createInt(21, 24);
		public HeaderField<Integer> UHL_LAT_DELTA_IN_TENTH_OF_S = createInt(25, 28);
		public HeaderField<Integer> UHL_CNT_LON_LINES = createInt(48, 51);
		public HeaderField<Integer> UHL_CNT_LAT_POINTS = createInt(52, 55);
		
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
		
		private final String header;
		
		public Header(String data) {
			this.header = data;
		}
		
		public int getNRows() {
			return UHL_CNT_LAT_POINTS.read();
		}

		public int getNCols() {
			return UHL_CNT_LON_LINES.read();
		}

		public double getLonResolution() {
			return degFromTenthOfSecond(UHL_LON_DELTA_IN_TENTH_OF_S.read());
		}

		private static double degFromTenthOfSecond(double tenthOfS) {
			return tenthOfS / (10 * 60 * 60);
		}

		public double getLatResolution() {
			return degFromTenthOfSecond(UHL_LAT_DELTA_IN_TENTH_OF_S.read());
		}

		public double getMinX() {
			return parseDDDMMSSH(UHL_LON.read());
		}

		private static double parseDDDMMSSH(String dddmmssh) {
			char last = dddmmssh.charAt(7);
			int sign = "NE".indexOf(Character.toUpperCase(last)) >= 0 ? 1 : -1;
			int d = Integer.parseInt(dddmmssh.substring(0, 3));
			int m = Integer.parseInt(dddmmssh.substring(3, 5));
			int s = Integer.parseInt(dddmmssh.substring(5, 7));
			return sign * (d + (m + s / 60.0) / 60.0);
		}

		public double getMinY() {
			return parseDDDMMSSH(UHL_LAT.read());
		}
	}
	
	private Header hdr;
	private double[] data;
	
	public DtedDemReader(InputStream is) throws IOException {
		try {
			readHeader(is);
			for (int i = 0; i < w; i++) {
				readDataRecord(is);
			}
		} finally {
			is.close();
		}
	}

	private void readDataRecord(InputStream is) throws UnsupportedEncodingException, IOException {
		is.read(); //Recognition Sentinel
		is.skip(3); // Data block count
		int lonCount = readFixedBinary(is);
		int latCount = readFixedBinary(is);

		for (int row = 0; row < h; row++) {
			data[h * lonCount + (h - 1 - row - latCount)] = readFixedBinary(is);	
		}
		is.skip(4); //Checksum
	}

	private static int readFixedBinary(InputStream is) throws IOException {
		int b1 = is.read() & 0xFF;
		int b2 = is.read() & 0xFF;
		if ((b1 & 0x80) != 0) {
			return - (((b1 & 0x7F) << 8) | (b2 & 0xFF)); 
		}
		return (b1 << 8) | b2;
	}

	private void readHeader(InputStream is) throws IOException {
		hdr = readHeader(is, HEADER_LENGTH);
		initialize(hdr.getNCols(), hdr.getNRows(), hdr.getLonResolution(), hdr.getLatResolution(), hdr.getMinX(), hdr.getMinY());
		data = new double[w*h];
	}

	private static Header readHeader(InputStream rdr, int headerLength) throws IOException, UnsupportedEncodingException {
		byte[] hdrChars = new byte[headerLength];
		rdr.read(hdrChars);
		return new Header(new String(hdrChars, "US-ASCII"));
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
