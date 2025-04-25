package com.sinergise.common.raster.dataraster.interpolation;

import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.raster.dataraster.SGDataBank;
import com.sinergise.common.util.geom.Envelope;

public class ResampledDataBank extends SGDataBank {
	DataBankInterpolator interpolator;
	
	/** For serialization purposes only */
	@Deprecated
	protected ResampledDataBank() {
	}
	
	public ResampledDataBank(SGDataBank src, AffineTransform2D worldTrans) {
		super(worldTrans);
		interpolator = new BilinearDataBankInterpolator(src);
		Envelope srcWorldEnv = src.getWorldEnvelopeForCellCentres();
		Envelope srcWorldInMyPixels = worldTrans.inverse().envelope(srcWorldEnv);
		dataEnv = srcWorldInMyPixels.roundLongInside().expand(0, 0, 1, 1);
		Envelope myWorldEnv = getWorldEnvelopeForCellCentres();
		if (!srcWorldEnv.contains(myWorldEnv)) {
			System.err.println(srcWorldEnv +" "+myWorldEnv);
		}
	}
	
	@Override
	public void close() {
	}

	@Override
	protected void expandDataStore(int rowsBottom, int rowsTop, int colsLeft, int colsRight) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public double getValue(long x, long y) {
		return interpolator.getInterpolatedValue(this.worldTr.x(x, y), this.worldTr.y(x, y));
	}

	@Override
	public void setValue(long x, long y, double value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SGDataBank cutBorders(int top, int left, int bot, int right) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void checkDataStore() {
		throw new UnsupportedOperationException();
	}
	
}
