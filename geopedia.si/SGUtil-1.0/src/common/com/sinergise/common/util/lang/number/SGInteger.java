package com.sinergise.common.util.lang.number;

import com.sinergise.common.util.lang.number.SGNumberApiMapping.RepresentableAsNumber;

public interface SGInteger extends RepresentableAsNumber {
	@Override
	public SGNatural abs();
	@Override
	public SGInteger opposite();
	public SGInteger plus(SGInteger other);
	public SGInteger times(SGInteger other);
}