package com.sinergise.common.util.string;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.PointI;
import com.sinergise.common.util.geom.Position2D;
import com.sinergise.common.util.lang.TimeSpec;
import com.sinergise.common.util.lang.number.SGNumber;
import com.sinergise.common.util.string.StringTransformer.EnumStringTransformer;

public class StringTransformUtil {
	private static final HashMap<Class<?>, StringTransformer<?>> trs             = new HashMap<Class<?>, StringTransformer<?>>(20);
	static {
		trs.put(String.class, new StringTransformer.Str());
		trs.put(Byte.class, new StringTransformer.Byt());
		trs.put(Short.class, new StringTransformer.Sht());
		trs.put(Integer.class, new StringTransformer.Int());
		trs.put(Long.class, new StringTransformer.Lng());
		trs.put(Float.class, new StringTransformer.Flt());
		trs.put(Double.class, new StringTransformer.Dbl());
		trs.put(Boolean.class, new StringTransformer.Bool());
		trs.put(Character.class, new StringTransformer.Chr());

		trs.put(BigDecimal.class, new StringTransformer.BigDec());
		trs.put(BigInteger.class, new StringTransformer.BigInt());
		trs.put(SGNumber.class, new StringTransformer.SGNum());
		
		trs.put(Date.class, new StringTransformer.Dt());
		trs.put(TimeSpec.class, new StringTransformer.TimeSpecStringTransformer());
		
		trs.put(DimI.class, new StringTransformer.DimensionI());
		trs.put(PointI.class, new StringTransformer.PtI());
		trs.put(Envelope.class, new StringTransformer.Env());
		trs.put(Position2D.class, new StringTransformer.Pos2D());

		trs.put(URI.class, new StringTransformer.TrURI());
		trs.put(URL.class, new StringTransformer.TrURL());
	}
	
	public static final <T> void addTransformer(final Class<? extends T> key, final StringTransformer<T> transformer) {
		trs.put(key, transformer);
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static final <T> StringTransformer<T> getTransformer(final Class<? extends T> key) {
		if (key != null && key.isEnum()) {
			return new EnumStringTransformer((Enum)key.getEnumConstants()[0]);
		}
		return (StringTransformer<T>)trs.get(key);
	}
	
	public static final <T> T valFromString(Class<? extends T> klass, String value) {
		if (value == null) {
			return null;
		}
		return forceGetTransformer(klass).valueOf(value);
	}
	
	public static <T> StringTransformer<T> forceGetTransformer(Class<? extends T> klass) {
		StringTransformer<T> tr = getTransformer(klass);
		if (tr == null) {
			throw new IllegalArgumentException("Transformer not found for "+klass);
		}
		return tr;
	}

	public static final <T> String stringFromVal(Class<? extends T> klass, T value) {
		if (value == null) {
			return null;
		}
		return StringTransformUtil.<T>forceGetTransformer(klass).store(value);
	}
	
}
