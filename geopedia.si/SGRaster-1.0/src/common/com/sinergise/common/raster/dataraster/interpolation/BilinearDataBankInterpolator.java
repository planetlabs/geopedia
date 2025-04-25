package com.sinergise.common.raster.dataraster.interpolation;

import com.sinergise.common.raster.dataraster.SGDataBank;
import com.sinergise.common.util.math.interpolation.Interpolation;

public class BilinearDataBankInterpolator extends DataBankInterpolator {
	public BilinearDataBankInterpolator(SGDataBank bank) {
		super(bank);
	}
	@Override
	protected double getInterpolatedValue(long x, long y, double u, double v) {
		final double z00 = bank.getValue(x, y);
		final double z10 = bank.getValue(x + 1, y);
		final double z01 = bank.getValue(x, y + 1);
		final double z11 = bank.getValue(x + 1, y + 1);
		return Interpolation.bilinear(u, v, z00, z10, z01, z11);
	}
}
