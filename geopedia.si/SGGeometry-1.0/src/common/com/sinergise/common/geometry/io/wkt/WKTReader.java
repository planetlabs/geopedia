package com.sinergise.common.geometry.io.wkt;


import static com.sinergise.common.geometry.io.wkt.WKTConstants.LIST_END;
import static com.sinergise.common.geometry.io.wkt.WKTConstants.LIST_SEPARATOR;
import static com.sinergise.common.geometry.io.wkt.WKTConstants.LIST_START;
import static com.sinergise.common.geometry.io.wkt.WKTConstants.WKT_EMPTY;
import static com.sinergise.common.util.string.StringUtil.isNullOrEmpty;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.io.GeometryReader.AbstractGeometryReaderImpl;
import com.sinergise.common.geometry.io.OgcShapeType;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.io.IOUtil;
import com.sinergise.common.util.string.StringUtil;

public class WKTReader extends AbstractGeometryReaderImpl {
	private static final Logger logger = LoggerFactory.getLogger(WKTReader.class);
	
	public static class StringParser {
		private static final int LEN_EMPTY = WKT_EMPTY.length();
		
		private static boolean isNumberPart(int ch) {
			switch (ch) {
				case '+':
				case '-':
				case '.':
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
				case 'e':
				case 'E':
					return true;
				default:
					return false;
			}
		}

		private static boolean isWord(int ch) {
			return ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z');
		}

		protected static boolean isWhitespace(int ch) {
			return ch >= 0 && ch <= 32;
		}

		private final Reader str;
		private final StringBuilder tmp = new StringBuilder(64);
		private final boolean caseSensitive = false;
		private int lastChar = 0;

		public StringParser(String value) {
			this(new StringReader(value));
		}

		public StringParser(Reader in) {
			this.str = in;
		}

		public int readNextChar() {
			try {
				lastChar = str.read();
				return lastChar;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public int skipWhitespace() {
			while (isWhitespace(lastChar)) {
				readNextChar();
			}
			return lastChar;
		}

		/**
		 * @return true iff WKTConstants.LIST_SEPARATOR was skipped
		 */
		public boolean readCommaOrEndList() {
			readNextChar();
			return consumeCommaOrEndList();
		}

		
		/**
		 * @return true iff WKTConstants.LIST_SEPARATOR was skipped
		 */
		public boolean consumeCommaOrEndList() {
			if (consumeSpecialChar(LIST_SEPARATOR)) {
				return true;
			}
			checkContentEnd();
			return false;
		}

		public double readNumber() {
			try {
				skipWhitespace();
				tmp.setLength(0);
				while (isNumberPart(lastChar)) {
					tmp.append((char)lastChar);
					readNextChar();
				}
				return Double.parseDouble(tmp.toString());
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Expected number "+errorLocStr());
			}
		}

		private int countAhead(char countWhat, char endChar) {
			try {
				str.mark(Integer.MAX_VALUE);
				int cnt = 0;
				while (true) {
					int ch = str.read();
					if (ch < 0 || ch == endChar) {
						break;
					}
					if (ch == countWhat) {
						cnt++;
					}
				}
				str.reset();
				return cnt;
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}

		private String errorLocStr() {
			StringBuilder err=new StringBuilder(tmp);
			err.append("...");
			for (int i = 0; i < 16; i++) {
				err.append((char)lastChar);
				if (readNextChar() < 0) {
					break;
				}
			}
			return " at >"+tmp+"<";
		}

		public void close() throws IOException {
			str.close();
		}

		public int countCoordinates() {
			return 1 + countAhead(LIST_SEPARATOR, LIST_END);
		}

		public void checkContentEnd() {
			if (!isAtSpecialChar(LIST_END)) {
				throw new IllegalArgumentException("Expected " + LIST_END + " " + errorLocStr());
			}
		}

		public boolean consumeContentStartOrEmpty() {
			if (consumeSpecialChar(LIST_START)) {
				return true;
			}
			if (readEmptyOrWord()) {
				return false;
			}
			throw new IllegalArgumentException("Expected " + LIST_START + " " + errorLocStr());
		}

		public boolean readEmptyOrWord() {
			int i = 0;
			tmp.setLength(0);
			while (isWord(lastChar)) {
				tmp.append((char)lastChar);
				if (++i == LEN_EMPTY && isEmpty()) {
					return true;
				}
				readNextChar();
			}
			return false;
		}
		
		public String lastWord() {
			return tmp.toString();
		}

		public boolean consumeSpecialChar(char delimiter) {
			if (isAtSpecialChar(delimiter)) {
				readNextChar();
				return true;
			}
			return false;
		}
		
		private boolean isAtSpecialChar(char special) {
			skipWhitespace();
			return (lastChar == special);
		}

		private boolean isEmpty() {
			return caseSensitive ? StringUtil.endsWith(tmp, WKT_EMPTY) : StringUtil.endsWithIgnoreCase(tmp, WKT_EMPTY);
		}

		public boolean isEOF() {
			return lastChar < 0;
		}
	}
	
	final StringParser parser;

	public WKTReader(String in) {
		parser = new StringParser(in);
	}
	
	public WKTReader(Reader in) {
		parser = new StringParser(in);
	}
	
	@Override
	protected Geometry internalReadNext() throws com.sinergise.common.util.io.ObjectReader.ObjectReadException {
		try {
			//Consume a single comma
			parser.readNextChar();
			parser.consumeSpecialChar(',');
			if (parser.isEOF()) {
				return null;
			}
			return readGeometry(null);
		} catch (IllegalArgumentException iae) {
			throw new ObjectReadException(iae.getMessage(), iae);
		}
	}

	private Geometry readGeometry(OgcShapeType automaticShapeType) {
		parser.skipWhitespace();
		if (parser.readEmptyOrWord()) {// EMPTY autoType
			return automaticShapeType.createEmpty();
		}
		String tag = parser.lastWord();
		if (tag.isEmpty()) { // no tag; assuming autoType, expecting '('
			return readContentForTag(automaticShapeType);
		}
		return readContentForTag(OgcShapeType.fromWktTag(tag, parser.caseSensitive));
	}

	
	private Geometry readContentForTag(OgcShapeType tag) {
		switch (tag) {
			case POINT:
				return readPointContent();
			case LINESTRING:
				return readLineStringContent();
			case POLYGON:
			case MULTIPOINT://TODO: Allow PostGIS compatible mode where MULTIPOINT(10 40) is valid WKT
			case MULTILINESTRING:
			case MULTIPOLYGON:
			case GEOMETRYCOLLECTION:
				return readCompositeContent(tag);
			default:
				throw new IllegalArgumentException("Unsupported tag " + tag);
		}
	}

	private Point readPointContent() {
		if (parser.consumeContentStartOrEmpty()) {
			Point ret = new Point(parser.readNumber(), parser.readNumber());
			parser.checkContentEnd();
			return ret;
		}
		return new Point();
	}

	private LineString readLineStringContent() {
		return new LineString(readCoords());
	}

	private double[] readCoords() {
		if (parser.consumeContentStartOrEmpty()) {
			double[] crds = new double[2 * parser.countCoordinates()];
			int i = 0;
			do {
				crds[i++] = parser.readNumber();
				crds[i++] = parser.readNumber();
			} while (parser.consumeCommaOrEndList());
			return crds;
		}
		return ArrayUtil.emptyDoubleArray;
	}
	
	private Geometry readCompositeContent(OgcShapeType parentType) {
		if (!parser.consumeContentStartOrEmpty()) {
			return parentType.createEmpty();
		}
		OgcShapeType autoChild = parentType.getAutomaticMemberType();
		
		List<Geometry> elements = new LinkedList<Geometry>();
		do {
			elements.add(readGeometry(autoChild));
		} while (parser.readCommaOrEndList());
	
		return parentType.createInstance(elements);
	}
	
	@Override
	public void close() throws IOException {
		parser.close();
	}

	@SuppressWarnings("all")
	public static Geometry read(String wktString) throws ObjectReadException {
		if (StringUtil.isNullOrEmpty(wktString)) {
			return null;
		}
		WKTReader wktReader = new WKTReader(wktString);
		try {
			return wktReader.readNext();
		} finally {
			IOUtil.closeSilent(wktReader);
		}
	}

	public static Geometry readSilent(String wktString) {
		if (isNullOrEmpty(wktString)) {
			return null;
		}
		try {
			return read(wktString);
		} catch (ObjectReadException e) {
			logger.warn("Exception while silently reading WKT: {}", new Object[] {wktString, e});
			return null; //be quiet!
		}
	}
}
