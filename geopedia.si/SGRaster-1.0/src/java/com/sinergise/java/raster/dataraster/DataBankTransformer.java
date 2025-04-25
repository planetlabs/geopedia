package com.sinergise.java.raster.dataraster;

import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.crs.transform.Transform.EnvelopeTransform;
import com.sinergise.common.geometry.crs.transform.Transform.InvertibleTransform;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.raster.dataraster.ShortDataBank;
import com.sinergise.common.raster.dataraster.interpolation.BilinearDataBankInterpolator;
import com.sinergise.common.raster.dataraster.interpolation.DataBankInterpolator;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeL;

public class DataBankTransformer {
	private DataBankInterpolator src;
	private ShortDataBank srcBank;
	private Envelope srcWorldEnv;

	public DataBankTransformer(ShortDataBank src) {
		this(new BilinearDataBankInterpolator(src));
	}
	
	public DataBankTransformer(DataBankInterpolator src) {
		this.src = src;
		srcBank = (ShortDataBank)src.getBank();
		srcWorldEnv = srcBank.getWorldEnvelopeForCellCentres();
	}
	
	@SuppressWarnings("rawtypes")
	private static double pixelDistance(AffineTransform2D srcTr, Transform tr, long x0, long y0, long x1, long y1) {
		return GeomUtil.distance(trPixel(srcTr, tr, x0, y0), trPixel(srcTr, tr, x1, y1));
	}

	@SuppressWarnings("rawtypes")
	private static Point trPixel(AffineTransform2D srcTr, Transform tr, long x, long y) {
		return tr.point(new Point(srcTr.point(x, y)), new Point());
	}

	@SuppressWarnings("rawtypes")
	public ShortDataBank warp(Transform tr) {
		EnvelopeL srcDataEnv = srcBank.getEnvelope();
		long minX = srcDataEnv.getMinX();
		long minY = srcDataEnv.getMinY();
		
		AffineTransform2D srcPixTr = srcBank.getWorldTr();
		
		
		double pixSize = Math.min(pixelDistance(srcPixTr, tr, minX, minY, minX, minY+1), pixelDistance(srcPixTr, tr, minX, minY, minX+1, minY));
		return warp(tr, pixSize);
	}

	@SuppressWarnings("rawtypes")
	public ShortDataBank warp(Transform tr, double pixSize) {
		Envelope srcEnv = src.getBank().getWorldEnvelopeForCellCentres();
		Envelope tgtEnv = ((EnvelopeTransform)tr).envelope(srcEnv);
		return warp(tr, pixSize, roundForWarping(tgtEnv, pixSize));
	}

	public static Envelope roundForWarping(Envelope tgtEnv, double pixSize) {
		return tgtEnv.divide(pixSize).translate(0.5, 0.5).roundIntOutside().translate(-0.5, -0.5).times(pixSize);
	}

	@SuppressWarnings("rawtypes")
	public ShortDataBank warp(Transform tr, double pixSize, Envelope tgtEnv) {
		AffineTransform2D tgtTrans = AffineTransform2D.createTrScale(pixSize, pixSize, tgtEnv.getMinX(), tgtEnv.getMinY());
		ShortDataBank tgt = new ShortDataBank(tgtTrans, srcBank.zMin, srcBank.zScale);
		int tgtW = (int)Math.ceil(tgtEnv.getWidth() / pixSize + 1);
		int tgtH = (int)Math.ceil(tgtEnv.getHeight() / pixSize + 1);
		tgt.setSize(tgtW, tgtH, true);
		warp(tr, tgt);
		return tgt;
	}

	@SuppressWarnings("rawtypes")
	public void warp(Transform tr, ShortDataBank tgt) {
		final int tgtW = tgt.getWidth();
		final int tgtH = tgt.getHeight();
		final AffineTransform2D tgtTrans = tgt.getWorldTr();
		
		final Transform invTr = ((InvertibleTransform)tr).inverse();
		
		Point tmp = new Point();
		for (int y = 0; y < tgtH; y++) {
			for (int x = 0; x < tgtW; x++) {
				Point srcPt = invTr.point(new Point(tgtTrans.point(x, y)), tmp);
				double srcWorldX = srcPt.x();
				double srcWorldY = srcPt.y();
				if (srcWorldEnv.contains(srcWorldX, srcWorldY)) {
					tgt.setValue(x, y, src.getInterpolatedValue(srcWorldX, srcWorldY));
				} else {
					tgt.setValue(x, y, Double.NaN);
				}
			}
		}
	}

}
