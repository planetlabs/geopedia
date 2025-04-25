package com.sinergise.common.util.lang.number;

import java.math.BigDecimal;
import java.math.BigInteger;


public class SGNumberApiMapping {
	private SGNumberApiMapping() {}
	
	public static interface RepresentableAsDouble extends RepresentableAsNumber {
		double doubleValue();
	}

	public static interface RepresentableAsBigDecimal extends RepresentableAsNumber {
		BigDecimal bigDecimalValue();
	}

	public static interface RepresentableAsBigInteger extends RepresentableAsBigDecimal, SGInteger {
		BigInteger bigIntegerValue();
	}

	public static interface RepresentableAsInt extends RepresentableAsBigInteger, RepresentableAsDouble {
		int intValue();
	}

	public static interface RepresentableAsNumber extends SGRational {
		Number numberValue();
	}
}
