package com.sinergise.common.util.lang.number;

public interface SGNatural extends SGInteger {
	@Override
	@Deprecated
	public boolean isNegative();
	@Override
	@Deprecated
	public SGNatural abs();
	public SGNatural plus(SGNatural other);
	public SGNatural times(SGNatural other);
}