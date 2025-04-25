package com.sinergise.common.util.lang.number;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.sinergise.common.util.lang.number.SGNumberApiMapping.RepresentableAsBigDecimal;
import com.sinergise.common.util.lang.number.SGNumberApiMapping.RepresentableAsBigInteger;
import com.sinergise.common.util.lang.number.SGNumberApiMapping.RepresentableAsDouble;
import com.sinergise.common.util.lang.number.SGNumberApiMapping.RepresentableAsInt;
import com.sinergise.common.util.math.MathUtil;

public abstract class SGNumberImpl implements SGNumber {
	private static final long serialVersionUID = 1L;

	abstract static class AbstractRational extends SGNumberImpl implements SGRational {

		private static final long serialVersionUID = 1L;

		@Override
		public SGRational plus(SGRational other) {
			return (SGRational)plus((SGNumber)other);
		}

		@Override
		public SGRational times(SGRational other) {
			return (SGRational)times((SGNumber)other);
		}

		@Override
		public SGRational abs() {
			return (SGRational)super.abs();
		}
	}

	abstract static class AbstractInteger extends AbstractRational implements SGInteger {

		private static final long serialVersionUID = 1L;

		@Override
		public SGInteger plus(SGInteger other) {
			return (SGInteger)plus((SGNumber)other);
		}

		@Override
		public SGInteger times(SGInteger other) {
			return (SGInteger)times((SGNumber)other);
		}

		@Override
		public boolean isInteger() {
			return true;
		}

		@Override
		public SGNatural abs() {
			return (SGNatural)super.abs();
		}
	}

	static class Int extends AbstractInteger implements RepresentableAsInt {

		private static final long serialVersionUID = 1L;
		
		private int value;

		@Deprecated
		Int() {}

		Int(int value) {
			assert value != 0;
			this.value = value;
		}

		@Override
		public int intValue() {
			return value;
		}

		@Override
		public double doubleValue() {
			return value;
		}

		@Override
		public Number numberValue() {
			return Integer.valueOf(value);
		}

		@Override
		public BigDecimal bigDecimalValue() {
			return BigDecimal.valueOf(value);
		}

		@Override
		public BigInteger bigIntegerValue() {
			return BigInteger.valueOf(value);
		}

		@Override
		public int compareTo(SGNumber o) {
			if (o instanceof RepresentableAsInt) {
				int oVal = ((RepresentableAsInt)o).intValue();
				return (value < oVal) ? -1 : ((value == oVal) ? 0 : 1);
			}
			return -o.compareTo(this);
		}

		@Override
		public SGNumber plus(SGNumber o) {
			if (o instanceof RepresentableAsInt) {
				return plus(value, ((RepresentableAsInt)o).intValue());
			}
			return o.plus(this);
		}

		@Override
		public SGNumber times(SGNumber o) {
			if (o instanceof RepresentableAsInt) {
				return times(value, ((RepresentableAsInt)o).intValue());
			}
			return o.times(this);
		}
		
		@Override
		public String toCanonicalString() {
			return String.valueOf(value);
		}

		@Override
		public SGInteger opposite() {
			return Dbl.valueOfInt(-(double)value);
		}

		@Override
		public SGInteger signum() {
			return value > 0 ? ONE : NEGATIVE_ONE;
		}

		@Override
		public int hashCode() {
			return value;
		}

		public static SGInteger plus(int a, int b) {
			return Dbl.valueOfInt((double)a + (double)b);
		}

		public static SGInteger times(int a, int b) {
			return (SGInteger)Dbl.times(a, b);
		}

		public static RepresentableAsInt valueOf(int val) {
			switch (val) {
				case 0:
					return ZERO;
				case 1:
					return ONE;
				case -1:
					return NEGATIVE_ONE;
				default:
					return val > 0 ? new IntNat(val) : new Int(val);
			}
		}
	}

	static final class IntNat extends Int implements SGNatural {

		private static final long serialVersionUID = 1L;

		@Deprecated
		IntNat() {}

		IntNat(int value) {
			super(value);
			assert value > 0;
		}

		@Override
		public SGNatural plus(SGNatural other) {
			return (SGNatural)plus((SGNumber)other);
		}

		@Override
		public SGNatural times(SGNatural other) {
			return (SGNatural)times((SGNumber)other);
		}
		
		//Need to override to prevent javac deprecation warnings
		@Override
		@Deprecated
		public IntNat abs() {
			return this;
		}
		
		//Need to override to prevent javac deprecation warnings
		@Override
		@Deprecated
		public final boolean isNegative() {
			return false;
		}
	}

	static class Dbl extends AbstractRational implements RepresentableAsDouble, RepresentableAsBigDecimal {

		private static final long serialVersionUID = 1L;
		
		private double value;

		@Deprecated
		Dbl() {}

		Dbl(double value) {
			this.value = value;
		}

		@Override
		public BigDecimal bigDecimalValue() {
			return new BigDecimal(doubleValue());
		}

		@Override
		public Number numberValue() {
			return Double.valueOf(value);
		}

		@Override
		public String toCanonicalString() {
			return bigDecimalValue().toString();
		}

		@Override
		public double doubleValue() {
			return value;
		}

		@Override
		public int compareTo(SGNumber o) {
			if (o.isFinite() && (o instanceof RepresentableAsDouble)) {
				return MathUtil.fastCompare(value, ((RepresentableAsDouble)o).doubleValue());
			}
			return -o.compareTo(this);
		}

		@Override
		public SGNumber plus(SGNumber o) {
			if (o instanceof RepresentableAsDouble) {
				return plus(value, ((RepresentableAsDouble)o).doubleValue());
			}
			return o.plus(this);
		}

		@Override
		public SGNumber times(SGNumber o) {
			if (o instanceof RepresentableAsDouble) {
				return times(value, ((RepresentableAsDouble)o).doubleValue());
			}
			return o.times(this);
		}

		@Override
		public SGInteger signum() {
			if (value > 0)
				return ONE;
			if (value < 0)
				return NEGATIVE_ONE;
			throw new IllegalStateException("Invalid value: " + value);
		}

		@Override
		public SGRational opposite() {
			return valueOf(-doubleValue());
		}

		@Override
		public int hashCode() {
			return MathUtil.hashCode(value);
		}

		public strictfp static SGNumber plus(double a, double b) {
			if (isSpecial(a) || isSpecial(b)) {
				return valueOf(a + b);
			}
			if (Math.abs(a) < Math.abs(b)) {
				double tmp = a;
				a = b;
				b = tmp;
			}
			double sum = a + b;
			if (sum - a == b) {
				return valueOf(sum);
			}
			return BigDec.valueOf(new BigDecimal(a).add(new BigDecimal(b)));
		}

		public static SGNumber times(double a, double b) {
			if (isSpecial(a) || isSpecial(b)) {
				return valueOfInt(a * b);
			}
			if (a == (float)a && b == (float)b) {
				return valueOf(a * b);
			}
			return BigDec.valueOf(new BigDecimal(a).multiply(new BigDecimal(b)));
		}

		public static RepresentableAsDouble valueOf(double dblVal) {
			if (isIntOrNaN(dblVal)) {
				return (RepresentableAsDouble)valueOfInt(dblVal);
			}
			return new Dbl(dblVal);
		}

		public static SGInteger valueOfInt(double d) {
			assert isIntOrNaN(d);
			if (isSpecial(d)) {
				return SpecialDouble.valueOf(d);
			}
			if (d < Integer.MIN_VALUE) {
				return new DblInt(d);
			}
			if (d > Integer.MAX_VALUE) {
				return new DblNat(d);
			}
			return Int.valueOf((int)d);
		}

		static boolean isSpecial(double a) {
			return a == 0 || Double.isInfinite(a) || Double.isNaN(a);
		}

		private static boolean isIntOrNaN(double dblVal) {
			return Double.isNaN(dblVal) || (Math.rint(dblVal) == dblVal);
		}
	}

	static class DblInt extends Dbl implements RepresentableAsBigInteger {

		private static final long serialVersionUID = 1L;

		@Deprecated
		DblInt() {}

		DblInt(double value) {
			super(value);
			assert value == Math.rint(value);
		}

		@Override
		public SGNatural abs() {
			return (SGNatural)super.abs();
		}

		@Override
		public DblInt opposite() {
			return (DblInt)super.opposite();
		}

		@Override
		public boolean isInteger() {
			return true;
		}

		@Override
		public SGInteger plus(SGInteger other) {
			return (SGInteger)plus((SGNumber)other);
		}

		@Override
		public SGInteger times(SGInteger other) {
			return (SGInteger)times((SGNumber)other);
		}

		@Override
		public BigInteger bigIntegerValue() {
			return bigDecimalValue().toBigIntegerExact();
		}
	}

	static final class DblNat extends DblInt implements SGNatural {

		private static final long serialVersionUID = 1L;

		@Deprecated
		DblNat() {}

		DblNat(double value) {
			super(value);
			assert value > 0;
		}

		@Override
		public SGNatural plus(SGNatural other) {
			return (SGNatural)plus((SGNumber)other);
		}

		@Override
		public SGNatural times(SGNatural other) {
			return (SGNatural)times((SGNumber)other);
		}
		
		//Need to override to prevent javac deprecation warnings
		@Override
		@Deprecated
		public DblNat abs() {
			return this;
		}
		
		//Need to override to prevent javac deprecation warnings
		@Override
		@Deprecated
		public final boolean isNegative() {
			return false;
		}
	}

	static class BigDec extends AbstractRational implements RepresentableAsBigDecimal {
		
		private static final long serialVersionUID = 1L;
		
		private BigDecimal value;

		@Deprecated
		BigDec() {}

		BigDec(BigDecimal value) {
			this.value = value.stripTrailingZeros();
		}

		@Override
		public BigDecimal bigDecimalValue() {
			return value;
		}

		@Override
		public Number numberValue() {
			return value;
		}

		@Override
		public String toCanonicalString() {
			return value.toPlainString();
		}

		@Override
		public int compareTo(SGNumber o) {
			if (o instanceof RepresentableAsBigDecimal) {
				return value.compareTo(((RepresentableAsBigDecimal)o).bigDecimalValue());
			}
			return -o.compareTo(this);
		}

		@Override
		public SGNumber plus(SGNumber o) {
			if (o instanceof RepresentableAsBigDecimal) {
				return plus(value, ((RepresentableAsBigDecimal)o).bigDecimalValue());
			}
			return o.plus(this);
		}

		@Override
		public SGNumber times(SGNumber o) {
			if (o instanceof RepresentableAsBigDecimal) {
				return times(value, ((RepresentableAsBigDecimal)o).bigDecimalValue());
			}
			return o.times(this);
		}

		@Override
		public SGInteger signum() {
			switch (value.signum()) {
				case 1:
					return ONE;
				case -1:
					return NEGATIVE_ONE;
			}
			throw new IllegalStateException("Invalid signum for BigDec: " + value.signum());
		}

		@Override
		public RepresentableAsBigDecimal opposite() {
			return valueOf(bigDecimalValue().negate());
		}

		@Override
		public int hashCode() {
			return value.hashCode();
		}

		public static SGNumber plus(BigDecimal a, BigDecimal b) {
			return valueOf(a.add(b));
		}

		public static SGNumber times(BigDecimal a, BigDecimal b) {
			int cmpA = a.signum();
			int cmpB = b.signum();
			if (cmpA == 0) {
				return cmpB >= 0 ? ZERO : NEGATIVE_ZERO;
			}
			if (cmpB == 0) {
				return cmpA >= 0 ? ZERO : NEGATIVE_ZERO;
			}
			return valueOf(a.multiply(b));
		}

		public static boolean isWhole(BigDecimal val) {
			return val.stripTrailingZeros().scale() <= 0;
		}

		public static RepresentableAsBigDecimal valueOf(BigDecimal val) {
			double dblVal = val.doubleValue();
			if (equalsExact(val, dblVal)) {
				return (RepresentableAsBigDecimal)Dbl.valueOf(dblVal);
			}
			val = val.stripTrailingZeros();
			if (val.scale() <= 0) {
				return valueOfInt(val);
			}
			return new BigDec(val);
		}


		static RepresentableAsBigInteger valueOfInt(BigDecimal val) {
			assert isWhole(val);
			double dVal = val.doubleValue();
			if (equalsExact(val, dVal)) {
				return (RepresentableAsBigInteger)Dbl.valueOfInt(dVal);
			}
			return val.compareTo(BigDecimal.ZERO) > 0 ? new BigNat(val) : new BigInt(val);
		}

		public static boolean equalsExact(BigDecimal val, double dblVal) {
			return new BigDecimal(dblVal).compareTo(val) == 0;
		}

		public static boolean isRepresentableAsDouble(BigDecimal val) {
			return equalsExact(val, val.doubleValue());
		}
	}

	static class BigInt extends BigDec implements RepresentableAsBigInteger {

		private static final long serialVersionUID = 1L;

		@Deprecated
		BigInt() {}

		BigInt(BigDecimal value) {
			super(value);
			assert value.scale() <= 0;
			assert new BigDecimal(value.doubleValue()).compareTo(value) != 0;
		}

		@Override
		public SGNatural abs() {
			return (SGNatural)super.abs();
		}

		@Override
		public boolean isInteger() {
			return true;
		}

		@Override
		public BigInteger bigIntegerValue() {
			return bigDecimalValue().toBigIntegerExact();
		}

		@Override
		public RepresentableAsBigInteger opposite() {
			return (RepresentableAsBigInteger)super.opposite();
		}

		@Override
		public Number numberValue() {
			return bigIntegerValue();
		}

		@Override
		public SGInteger plus(SGInteger other) {
			return (SGInteger)plus((SGNumber)other);
		}

		@Override
		public SGInteger times(SGInteger other) {
			return (SGInteger)times((SGNumber)other);
		}

		public static RepresentableAsBigInteger valueOf(BigInteger val) {
			return BigDec.valueOfInt(new BigDecimal(val).stripTrailingZeros());
		}

		public static RepresentableAsBigInteger valueOf(long l) {
			if (Integer.MIN_VALUE <= l && l <= Integer.MAX_VALUE) {
				return Int.valueOf((int)l);
			}
			return valueOf(BigInteger.valueOf(l));
		}
	}

	static final class BigNat extends BigInt implements SGNatural {

		private static final long serialVersionUID = 1L;

		@Deprecated
		BigNat() {}

		BigNat(BigDecimal value) {
			super(value);
			assert (value.compareTo(BigDecimal.ZERO) > 0);
		}

		@Override
		public SGNatural plus(SGNatural other) {
			return (SGNatural)super.plus(other);
		}

		@Override
		public SGNatural times(SGNatural other) {
			return (SGNatural)super.times(other);
		}
		
		//Need to override to prevent javac deprecation warnings
		@Override
		@Deprecated
		public BigNat abs() {
			return this;
		}
		
		//Need to override to prevent javac deprecation warnings
		@Override
		@Deprecated
		public final boolean isNegative() {
			return false;
		}
	}

	abstract static class SpecialDouble extends AbstractInteger implements RepresentableAsDouble {


		private static final long serialVersionUID = 1L;

		@Override
		public final boolean isDefined() {
			return !Double.isNaN(doubleValue());
		}

		@Override
		public final Number numberValue() {
			return Double.valueOf(doubleValue());
		}

		@Override
		public final SGInteger opposite() {
			return valueOf(-doubleValue());
		}

		@Override
		public final SGInteger signum() {
			return valueOf(Math.signum(doubleValue()));
		}

		@Override
		public final int compareTo(SGNumber o) {
			if (o.isFinite()) {
				return compareTo((RepresentableAsDouble)o.signum());
			}
			return compareTo((RepresentableAsDouble)o);
		}

		public final int compareTo(RepresentableAsDouble o) {
			return Double.compare(doubleValue(), o.doubleValue());
		}

		public static SGInteger valueOf(double val) {
			assert Dbl.isSpecial(val) || val == 1 || val == -1;
			if (val == 0.0) {
				return 1.0 / val > 0 ? ZERO : NEGATIVE_ZERO;
			}
			if (Double.isNaN(val)) {
				return UNDEFINED;
			}
			if (val == 1.0) {
				return ONE;
			}
			if (val == -1.0) {
				return NEGATIVE_ONE;
			}
			return val > 0 ? POSITIVE_INFINITY : NEGATIVE_INFINITY;
		}
	}

	public static final class Zero extends SpecialDouble implements SGNatural, RepresentableAsInt {

		private static final long serialVersionUID = 1L;

		Zero() {}

		@Override
		public SGNumber plus(SGNumber other) {
			if (other.equals(NEGATIVE_ZERO)) {
				return ZERO;
			}
			return other;
		}

		@Override
		public SGNumber times(SGNumber other) {
			if (!other.isFinite()) {
				return UNDEFINED;
			}
			if (isPositiveOrZero(other)) {
				return ZERO;
			}
			return NEGATIVE_ZERO;
		}

		@Override
		public String toCanonicalString() {
			return "0";
		}

		@Override
		public BigInteger bigIntegerValue() {
			return BigInteger.ZERO;
		}

		@Override
		public BigDecimal bigDecimalValue() {
			return BigDecimal.ZERO;
		}

		@Override
		public double doubleValue() {
			return 0;
		}

		@Override
		public int intValue() {
			return 0;
		}

		@Override
		public SGNatural plus(SGNatural other) {
			return (SGNatural)plus((SGNumber)other);
		}

		@Override
		public SGNatural times(SGNatural other) {
			return (SGNatural)times((SGNumber)other);
		}
		
		//Need to override to prevent javac deprecation warnings
		@Override
		@Deprecated
		public Zero abs() {
			return this;
		}
		
		//Need to override to prevent javac deprecation warnings
		@Override
		@Deprecated
		public final boolean isNegative() {
			return false;
		}
	}

	public static final class NegativeZero extends SpecialDouble {

		private static final long serialVersionUID = 1L;

		NegativeZero() {}

		@Override
		public SGNumber plus(SGNumber other) {
			return other;
		}

		@Override
		public SGNumber times(SGNumber other) {
			if (!other.isFinite()) {
				return UNDEFINED;
			}
			if (isNegativeOrNegativeZero(other)) {
				return ZERO;
			}
			return NEGATIVE_ZERO;
		}

		@Override
		public String toCanonicalString() {
			return "-0";
		}

		@Override
		public double doubleValue() {
			return -0.0;
		}
	}

	static abstract class Infinity extends SpecialDouble {

		private static final long serialVersionUID = 1L;

		@Override
		public SpecialDouble plus(SGNumber other) {
			if (other.isFinite() || other.signum().equals(signum())) {
				return this;
			}
			return UNDEFINED;
		}

		@Override
		public SGInteger times(SGNumber other) {
			if (other.isPositive()) {
				return this;
			}
			if (other.isNegative()) {
				return opposite();
			}
			return UNDEFINED;
		}

		@Override
		public boolean isInfinite() {
			return true;
		}
	}

	public static final class PositiveInfinity extends Infinity implements SGNatural {

		private static final long serialVersionUID = 1L;

		PositiveInfinity() {}

		@Override
		public String toCanonicalString() {
			return "∞";
		}

		@Override
		public double doubleValue() {
			return Double.POSITIVE_INFINITY;
		}

		@Override
		public SGNatural plus(SGNatural other) {
			return (SGNatural)plus((SGNumber)other);
		}

		@Override
		public SGNatural times(SGNatural other) {
			return (SGNatural)times((SGNumber)other);
		}
		
		//Need to override to prevent javac deprecation warnings
		@Override
		@Deprecated
		public PositiveInfinity abs() {
			return this;
		}
		
		//Need to override to prevent javac deprecation warnings
		@Override
		@Deprecated
		public final boolean isNegative() {
			return false;
		}
	}

	public static final class NegativeInfinity extends Infinity {

		private static final long serialVersionUID = 1L;

		NegativeInfinity() {}

		@Override
		public String toCanonicalString() {
			return "-∞";
		}

		@Override
		public double doubleValue() {
			return Double.NEGATIVE_INFINITY;
		}
	}

	public static final class Undefined extends SpecialDouble {

		private static final long serialVersionUID = 1L;

		Undefined() {}

		@Override
		public Undefined plus(SGNumber other) {
			return UNDEFINED;
		}

		@Override
		public Undefined times(SGNumber other) {
			return UNDEFINED;
		}

		@Override
		public String toCanonicalString() {
			return "NaN";
		}

		@Override
		public double doubleValue() {
			return Double.NaN;
		}
	}

	@Override
	public String toString() {
		return toCanonicalString();
	}

	@Override
	public SGNumber abs() {
		return isPositiveOrZero(this) ? this : opposite();
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		return compareTo((SGNumber)obj) == 0;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean isInteger() {
		return false;
	}

	@Override
	public boolean isInfinite() {
		return false;
	}

	@Override
	public final boolean isZero() {
		final SGInteger sig = signum();
		return sig.equals(ZERO) || sig.equals(NEGATIVE_ZERO);
	}

	@Override
	public final boolean isFinite() {
		return isDefined() && !isInfinite();
	}

	@Override
	public boolean isDefined() {
		return true;
	}

	@Override
	public final boolean isPositive() {
		return signum().equals(ONE);
	}

	@Override
	public boolean isNegative() {
		return signum().equals(NEGATIVE_ONE);
	}

	public static boolean isPositiveOrZero(SGNumber a) {
		return a.isDefined() && a.compareTo(ZERO) >= 0;
	}

	public static boolean isNegativeOrNegativeZero(SGNumber a) {
		return a.isDefined() && a.compareTo(ZERO) < 0;
	}

}