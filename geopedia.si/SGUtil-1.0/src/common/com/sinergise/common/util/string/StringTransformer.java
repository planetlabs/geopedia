/*
 *
 */
package com.sinergise.common.util.string;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import com.sinergise.common.util.format.DateFormatUtil;
import com.sinergise.common.util.format.DateFormatter;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.PointI;
import com.sinergise.common.util.geom.Position2D;
import com.sinergise.common.util.lang.TimeSpec;
import com.sinergise.common.util.lang.number.SGNumber;

public interface StringTransformer<T> extends Serializable {
	public class SGNum extends StoresCanonicalString<SGNumber> {
		private static final long serialVersionUID = 1L;

		@Override
		public SGNumber valueOf(String str) {
			return SGNumber.Util.valueOf(str);
		}
	}

	public static abstract class StoresWithToString<T> implements StringTransformer<T> {

		private static final long serialVersionUID = 1L;

		@Override
		public final String store(T obj) {
			return obj.toString();
		}
	}
	
	public static abstract class StoresCanonicalString<T extends HasCanonicalStringRepresentation> implements StringTransformer<T> {

		private static final long serialVersionUID = 1L;

		@Override
		public final String store(T obj) {
			return obj.toCanonicalString();
		}
	}
	
	public static class Str extends StoresWithToString<String> {

		private static final long serialVersionUID = 1L;

		@Override
		public String valueOf(final String str) {
			return str;
		}
	}
	
	public static class TrURL implements StringTransformer<URL> {

		private static final long serialVersionUID = 1L;

		@Override
		public String store(URL url) {
			return url.toExternalForm();
		}
		
		@Override
		public URL valueOf(String str) {
			try {
				return new URL(str);
			} catch(MalformedURLException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}

	public static class TrURI extends StoresWithToString<URI> {

		private static final long serialVersionUID = 1L;

		@Override
		public URI valueOf(String str) {
			try {
				return new URI(str);
			} catch(URISyntaxException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}
	
	public static class Dbl implements StringTransformer<Double> {

		private static final long serialVersionUID = 1L;

		@Override
		public String store(final Double obj) {
			double val = obj.doubleValue();
			//Do integer first to improve performance in javascript where long is emulated
			if (val == (int)val) return Integer.toString((int)val); 
			if (val == (long)val) return Long.toString((long)val);
			return Double.toString(val);
		}
		
		@Override
		public Double valueOf(final String str) {
			return Double.valueOf(str);
		}
	}
	
	public static class Sht extends StoresWithToString<Short> {

		private static final long serialVersionUID = 1L;

		@Override
		public Short valueOf(final String str) {
			return Short.valueOf(str);
		}
	}
	
	public static class Int extends StoresWithToString<Integer> {

		private static final long serialVersionUID = 1L;

		@Override
		public Integer valueOf(final String str) {
			return Integer.valueOf(str);
		}
	}
	
	public static class BigInt extends StoresWithToString<BigInteger> {

		private static final long serialVersionUID = 1L;

		@Override
		public BigInteger valueOf(final String str) {
			return new BigInteger(str, 10);
		}
	}
	
	public static class BigDec extends StoresWithToString<BigDecimal> {

		private static final long serialVersionUID = 1L;

		@Override
		public BigDecimal valueOf(final String str) {
			return new BigDecimal(str);
		}
	}
	
	public static class Lng extends StoresWithToString<Long> {

		private static final long serialVersionUID = 1L;

		@Override
		public Long valueOf(final String str) {
			return Long.valueOf(str);
		}
	}
	
	public static class Flt implements StringTransformer<Float> {

		private static final long serialVersionUID = 1L;

		@Override
		public String store(final Float obj) {
			float val = obj.floatValue();
			if (val == (int)val) return Integer.toString((int)val);
			return Float.toString(val);
		}
		
		@Override
		public Float valueOf(final String str) {
			return Float.valueOf(str);
		}
	}
	
	public static class Byt extends StoresWithToString<Byte> {

		private static final long serialVersionUID = 1L;

		@Override
		public Byte valueOf(final String str) {
			return Byte.valueOf(str);
		}
	}
	
	public static class Chr extends StoresWithToString<Character> {

		private static final long serialVersionUID = 1L;

		@Override
		public Character valueOf(final String str) {
			return StringUtil.firstChar(str);
		}
	}
	
	public static class Bool extends StoresWithToString<Boolean> {

		private static final long serialVersionUID = 1L;

		@Override
		public Boolean valueOf(final String str) {
			return Boolean.valueOf(str);
		}
	}
	
	public static class Dt implements StringTransformer<Date> {

		private static final long serialVersionUID = 1L;

		protected static final DateFormatter oldDf = DateFormatUtil.create("yyyy/MM/dd HH:mm:ss.SSS");
		protected static final DateFormatter oldDf1 = DateFormatUtil.create("yyyy/MM/dd HH:mm:ss");
		protected static final DateFormatter df = DateFormatter.FORMATTER_ISO_DATETIME;
		protected static final DateFormatter df1 = DateFormatter.FORMATTER_ISO_DATE;
		
		@Override
		public String store(final Date obj) {
			return df.formatDate(obj);
		}
		
		@Override
		public Date valueOf(final String str) {
			try {
				return df.parse(str);
			} catch(Exception e) {
			}
			try {
				return oldDf.parse(str);
			} catch(Exception e) {
			}
			try {
				return df1.parse(str);
			} catch(Exception e) {
			}
			try {
				return oldDf1.parse(str);
			} catch(Exception e1) {
				throw new IllegalArgumentException(str);
			}
		}
	}
	
	public static class TimeSpecStringTransformer extends StoresCanonicalString<TimeSpec> {

		private static final long serialVersionUID = 1L;

		@Override
		public TimeSpec valueOf(String str) {
			return new TimeSpec(str);
		}
	}
	
	public static class DimensionI extends StoresCanonicalString<DimI> {

		private static final long serialVersionUID = 1L;

		@Override
		public DimI valueOf(final String str) {
			return DimI.valueOf(str);
		}
	}
	
	public static class Env extends StoresCanonicalString<Envelope> {

		private static final long serialVersionUID = 1L;

		@Override
		public Envelope valueOf(final String str) {
			return Envelope.valueOf(str);
		}
	}
	
	public static class Pos2D extends StoresCanonicalString<Position2D> {

		private static final long serialVersionUID = 1L;

		@Override
		public Position2D valueOf(final String str) {
			return Position2D.valueOf(str);
		}
	}
	
	public static class PtI extends StoresCanonicalString<PointI> {

		private static final long serialVersionUID = 1L;

		@Override
		public PointI valueOf(final String str) {
			return PointI.valueOf(str);
		}
	}
	
	public static class EnumStringTransformer<E extends Enum<E>> implements StringTransformer<E> {

		private static final long serialVersionUID = 1L;

		private Enum<E> type;

		@Deprecated
		protected EnumStringTransformer() {
		}
		
		public EnumStringTransformer(Enum<E> prototype) {
			this.type = prototype;
		}
		
		public Class<E> getType() {
			return type.getDeclaringClass();
		}
		
		@Override
		public String store(E obj) {
			return obj.name();
		}

		@Override
		public E valueOf(String str) {
			return Enum.valueOf(type.getDeclaringClass(), str);
		}
	}
	
	public String store(T obj);
	
	public T valueOf(String str);
}
