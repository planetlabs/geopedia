package com.sinergise.gwt.util;

import static com.sinergise.common.util.math.MathUtil.ceilPow2Int;
import static com.sinergise.common.util.math.MathUtil.floorPow2Int;

import com.sinergise.common.util.Util.UtilImpl;
import com.sinergise.common.util.math.MathUtil;

public class UtilGwtImplOld implements UtilImpl {
	private static final double MAX_INT_POW2 = MathUtil.TWO_POW_30;
	private static final double INV_MAX_INT_POW2 = 1.0 / MAX_INT_POW2;

	public UtilGwtImplOld() {
	}
	
	protected void init() {
	}

	@Override
	public double ceilPow2(double val) {
		return INV_MAX_INT_POW2 <= val && val <= MAX_INT_POW2
			? (val >= 1 
				? ceilPow2Int((int)Math.ceil(val)) 
				: 1.0/floorPow2Int((int)(1.0/val))
				)
			: MathUtil.powerOf2((int)Math.ceil(MathUtil.log2(val)));
	}

	@Override
	public double floorPow2(double val) {
		return INV_MAX_INT_POW2 <= val && val <= MAX_INT_POW2
			? (val >= 1 
				? floorPow2Int((int)Math.floor(val)) 
				: 1.0/ceilPow2Int((int)Math.ceil(1.0/val))
				)
			: MathUtil.powerOf2((int)Math.floor(MathUtil.log2(val)));
	}

	@Override
	public int hashCode(double val) {
		if (Double.isNaN(val)) {
			return Integer.MAX_VALUE;
		}
		int s = 0;
		if (val < 0) {
			val = -val;
			s = 0x80000000;
		}
		int e = MathUtil.floorLog2(val);
		double m = MathUtil.scalb(val, 52 - e) - 4503599627370496.0;
		int u = (int)(m / 268435456.0); //2^28
		int d = (int)(m % 268435456.0) | (u << 28);//2^28
		return (s | ((e+1023) << 20) | (u >>> 4)) ^ d;
	}

}
