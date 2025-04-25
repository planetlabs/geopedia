package com.sinergise.common.raster.dataraster.interpolation;

import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.raster.dataraster.SGDataBank;

public abstract class DataBankInterpolator {
	protected final SGDataBank bank;
	private final AffineTransform2D idxFromWorld;

	public DataBankInterpolator(SGDataBank bank) {
		this.bank = bank;
		idxFromWorld = bank.getWorldTr().inverse();
	}
	
	public SGDataBank getBank() {
		return bank;
	}
	
	public final double getInterpolatedValue(double worldX, double worldY) {
		double offX = idxFromWorld.x(worldX, worldY);
		double offY = idxFromWorld.y(worldX, worldY);
		long idxX = (long)offX;
		long idxY = (long)offY;
		return getInterpolatedValue(idxX, idxY, offX - idxX, offY - idxY);
	}

	protected abstract double getInterpolatedValue(long x, long y, double u, double v);
}
