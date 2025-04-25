package com.sinergise.common.util.lang.number;


import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.sinergise.common.util.lang.number.SGNumberApiMapping.RepresentableAsBigDecimal;
import com.sinergise.common.util.lang.number.SGNumberApiMapping.RepresentableAsBigInteger;
import com.sinergise.common.util.lang.number.SGNumberApiMapping.RepresentableAsDouble;
import com.sinergise.common.util.lang.number.SGNumberApiMapping.RepresentableAsInt;
import com.sinergise.common.util.lang.number.SGNumberApiMapping.RepresentableAsNumber;
import com.sinergise.common.util.lang.number.SGNumberImpl.Int;
import com.sinergise.common.util.lang.number.SGNumberImpl.IntNat;
import com.sinergise.common.util.lang.number.SGNumberImpl.NegativeInfinity;
import com.sinergise.common.util.lang.number.SGNumberImpl.NegativeZero;
import com.sinergise.common.util.lang.number.SGNumberImpl.PositiveInfinity;
import com.sinergise.common.util.lang.number.SGNumberImpl.Undefined;
import com.sinergise.common.util.lang.number.SGNumberImpl.Zero;
import com.sinergise.common.util.string.HasCanonicalStringRepresentation;

/**
 * Implementation notes: - Zero is used for +0 and -0 - PositiveInfinity and NegativeInfinity is used for inf and -inf,
 * respectively - Int is used if value is representable as int - BigInt is used if value is integral and not
 * representable as int - Dbl is used if value is not integral and representable as double - BigDec is used otherwise
 * 
 * @author Miha
 */
public interface SGNumber extends Comparable<SGNumber>, HasCanonicalStringRepresentation, Serializable {
	public static final class Util {
		private Util() {
		}

		public static BigInteger asBigInteger(SGNumber num) {
			if (num instanceof RepresentableAsBigInteger) {
				return ((RepresentableAsBigInteger)num).bigIntegerValue();
			}
			throw new IllegalArgumentException("Argument not representable as BigInteger: "+num);
		}
		
		public static BigDecimal asBigDecimal(SGNumber num) {
			if (num instanceof RepresentableAsBigDecimal) {
				return ((RepresentableAsBigDecimal)num).bigDecimalValue();
			}
			throw new IllegalArgumentException("Argument not representable as BigDecimal: "+num);
		}

		public static double asDouble(SGNumber num) {
			if (num instanceof RepresentableAsDouble) {
				return ((RepresentableAsDouble)num).doubleValue();
			}
			throw new IllegalArgumentException("Argument not representable as double: "+num);
		}

		public static int asInt(SGNumber num) {
			if (num instanceof RepresentableAsInt) {
				return ((RepresentableAsInt)num).intValue();
			}
			throw new IllegalArgumentException("Argument not representable as int: "+num);
		}
		
		public static Integer asJavaInteger(SGNumber num) {
			if (num == null) {
				return null;
			}
			return Integer.valueOf(asInt(num));
		}

		public static RepresentableAsBigDecimal valueOf(BigDecimal numVal) {
			return SGNumberImpl.BigDec.valueOf(numVal);
		}

		public static RepresentableAsBigInteger valueOf(BigInteger numVal) {
			return SGNumberImpl.BigInt.valueOf(numVal);
		}

		public static RepresentableAsDouble valueOf(double d) {
			return SGNumberImpl.Dbl.valueOf(d);
		}

		public static RepresentableAsInt valueOf(int i) {
			return SGNumberImpl.Int.valueOf(i);
		}

		public static RepresentableAsBigInteger valueOf(long l) {
			return SGNumberImpl.BigInt.valueOf(l);
		}

		public static RepresentableAsNumber valueOf(Number numVal) {
			if (numVal == null) {
				return null;
			}
			if (numVal instanceof Byte || 
				numVal instanceof Short || 
				numVal instanceof Integer || 
				numVal instanceof AtomicInteger) {
				return valueOf(numVal.intValue());
			}
			if (numVal instanceof Long || 
				numVal instanceof AtomicLong) {
				return valueOf(numVal.longValue());
			}
			if (numVal instanceof Double || 
				numVal instanceof Float) {
				return valueOf(numVal.doubleValue());
			}
			if (numVal instanceof BigInteger) {
				return valueOf((BigInteger)numVal);
			}
			if (numVal instanceof BigDecimal) {
				return valueOf((BigDecimal)numVal);
			}
			throw new IllegalArgumentException("Unsupported Number type: "+numVal.getClass());
		}

		public static SGNumber valueOf(String strVal) {
			strVal = strVal.trim();
			if (strVal.length() == 0 || strVal.equals("NaN")) {
				return UNDEFINED;
			}
			if (strVal.startsWith("-")) {
				return valueOf(strVal.substring(1)).opposite();
			}
			if (strVal.equals(SGNumber.POSITIVE_INFINITY.toCanonicalString())) {
				return SGNumber.POSITIVE_INFINITY;
			}
			return valueOf(new BigDecimal(strVal));
		}
		
		public static SGNatural naturalValueOf(String strVal) {
			return asNatural(valueOf(strVal));
		}

		public static SGNatural naturalValueOf(long lVal) {
			return asNatural(valueOf(lVal));
		}
		
		private static SGNatural asNatural(SGNumber val) {
			asInteger(val);
			if (val.isNegative() || val.equals(NEGATIVE_ZERO)) {
				throw new IllegalArgumentException("Natural number expected but the value was negative ("+val+")");
			}
			return (SGNatural)val;
		}

		private static SGInteger asInteger(SGNumber val) {
			if (!val.isInteger()) {
				throw new IllegalArgumentException("Integer expected but the value wasn't integral ("+val+")");
			}
			return (SGInteger)val;
		}

		public static long asLong(SGNumber val) {
			SGInteger intgr = asInteger(val);
			if (intgr instanceof RepresentableAsBigInteger) {
				BigInteger bi = ((RepresentableAsBigInteger)intgr).bigIntegerValue();
				if (BigInteger.valueOf(Long.MIN_VALUE).compareTo(bi) < 0 && //
					bi.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) < 0) {
					return bi.longValue();
				}
			}
			throw new IllegalArgumentException("Argument not representable as long ("+val+")");
		}
	}
	
	Zero ZERO = new Zero();
	NegativeZero NEGATIVE_ZERO = new NegativeZero();

	PositiveInfinity POSITIVE_INFINITY = new PositiveInfinity();
	NegativeInfinity NEGATIVE_INFINITY = new NegativeInfinity();

	Int ONE = new IntNat(1);
	Int NEGATIVE_ONE = new Int(-1);
	
	Undefined UNDEFINED = new Undefined();

	SGInteger signum();

	boolean isInfinite();
	boolean isFinite();
	boolean isPositive();
	boolean isZero();
	boolean isNegative();
	boolean isInteger();
	boolean isDefined();
	
	SGNumber abs();
	SGNumber opposite();
	SGNumber plus(SGNumber other);
	SGNumber times(SGNumber other);
	//	public abstract SGNumber inverse();


}
