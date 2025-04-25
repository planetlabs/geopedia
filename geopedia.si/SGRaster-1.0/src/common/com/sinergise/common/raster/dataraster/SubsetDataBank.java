package com.sinergise.common.raster.dataraster;

import com.sinergise.common.util.geom.EnvelopeL;

public class SubsetDataBank extends SGDataBank {
	SGDataBank source;

	public SubsetDataBank(SGDataBank source, EnvelopeL env) {
		super(source.getWorldTr());
		this.source = source;
		dataEnv = env;
	}
	
	@Override
	public double getValue(long x, long y) {
		checkEnv(x, y);
		return source.getValue(x, y);
	}
	
	@Override
	public void close() {
		source = null;
	}

	@Override
	protected void expandDataStore(int rowsBottom, int rowsTop, int colsLeft, int colsRight) {
		throw new UnsupportedOperationException("SubsedDataBank is unmodifiable");
	}

	@Override
	public void setValue(long x, long y, double value) {
		throw new UnsupportedOperationException("SubsedDataBank is unmodifiable");
	}

	@Override
	public SGDataBank cutBorders(int top, int left, int bot, int right) {
		throw new UnsupportedOperationException("SubsedDataBank is unmodifiable");
	}

	@Override
	public void checkDataStore() {
		throw new UnsupportedOperationException("SubsedDataBank is unmodifiable");
	}
}
