package com.sinergise.common.raster.core;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Comparator;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.math.MathUtil;


public class RasterWorldInfo {

	/**
	 * @param size
	 * @param cornerPoints {botLeft, botRight, topRight, topLeft} - corners of respective pixels (not centers)
	 * @return RasterWorldInfo with affine transform that is the best least-squares fit at the corners
	 */
	public static RasterWorldInfo withCorners(DimI size, HasCoordinate[] cornerPoints) {
		HasCoordinate bl = cornerPoints[0];
		HasCoordinate br = cornerPoints[1];
		HasCoordinate tr = cornerPoints[2];
		HasCoordinate tl = cornerPoints[3];

		double m0 = 0.5 * (br.x() - bl.x() + tr.x() - tl.x()) / size.w();
		double m1 = 0.5 * (br.y() - bl.y() + tr.y() - tl.y()) / size.w();
		double m2 = 0.5 * (bl.x() + br.x() - tl.x() - tr.x()) / size.h();
		double m3 = 0.5 * (bl.y() + br.y() - tl.y() - tr.y()) / size.h();
		double m4 = 0.25 * (bl.x() - br.x() + 3 * tl.x() + tr.x());
		double m5 = 0.25 * (bl.y() - br.y() + 3 * tl.y() + tr.y());

		return new RasterWorldInfo(size, AffineTransform2D.create(m0, m1, m2, m3, m4, m5));
	}

	public static double affineFromCornerPointsError(HasCoordinate[] cornerPoints) {
		HasCoordinate bl = cornerPoints[0];
		HasCoordinate br = cornerPoints[1];
		HasCoordinate tr = cornerPoints[2];
		HasCoordinate tl = cornerPoints[3];

		double dif = 0.25 * Math.sqrt(MathUtil.sqr(bl.x() - br.x() - tl.x() + tr.x())
			+ MathUtil.sqr(bl.y() - br.y() - tl.y() + tr.y()));
		return dif;
	}

	public static class ScaleComparator implements Comparator<RasterWorldInfo> {

		@Override
		public int compare(RasterWorldInfo o1, RasterWorldInfo o2) {
			// do scale first
			int cmp = Double.compare(o2.getWorldAreaPerPix(), o1.getWorldAreaPerPix());
			if (cmp != 0)
				return cmp;

			// smaller images before (above) larger ones
			double wh1 = o1.wEnv.getWidth() * o1.wEnv.getHeight();
			double wh2 = o2.wEnv.getWidth() * o2.wEnv.getHeight();
			cmp = Double.compare(wh2, wh1);
			if (cmp != 0)
				return cmp;

			cmp = Double.compare(o1.tr.getTranslateX(), o2.tr.getTranslateX());
			if (cmp != 0)
				return cmp;
			cmp = Double.compare(o1.tr.getTranslateY(), o2.tr.getTranslateY());
			if (cmp != 0)
				return cmp;

			return 0;
		}
	}

	public AffineTransform2D tr;
	public CRS targetCRS;
	public transient Envelope wEnv = new Envelope();
	public int w;
	public int h;

	public RasterWorldInfo(DimI size, AffineTransform2D tr) {
		this.h = size.h();
		this.w = size.w();
		this.tr = tr;
		updateImageSize();
		this.targetCRS = tr.getTarget();
	}
	

	public DimI getImageSize() {
		return new DimI(w, h);
	}

	protected void updateImageSize() {
		Envelope imgBnd = new Envelope(0, 0, w, h);
		wEnv = tr.envelope(imgBnd);
	}

	@Override
	public String toString() {
		return w + " " + h + " " + tr.toString();
	}

	public double transformXGridBased(double x, double y) {
		return tr.x(x, y);
	}

	public double transformYGridBased(double x, double y) {
		return tr.y(x, y);
	}

	public Envelope transformEnvelopeGridBased(Envelope e) {
		return tr.envelope(e);
	}

	public Envelope backTransformEnvelopeGridBased(Envelope e) {
		return tr.inverse().envelope(e);
	}

	public Point transformPointGridBased(Point p) {
		return new Point(transformXGridBased(p.x, p.y), transformYGridBased(p.x, p.y));
	}

	public Point backTransformPointGridBased(Point p) {
		return tr.inverse().point(p);
	}


	public boolean isSimpleSquare() {
		return isSimpleRectangular() && Math.abs(tr.getScaleX()) == Math.abs(tr.getScaleY());
	}

	public boolean isSimpleRectangular() {
		return tr.isSimpleRectangular();
	}

	public double getWorldAreaPerPix() {
		return Math.abs(tr.getAreaFactor());
	}

	public Envelope worldEnvelope() {
		return tr.envelope(new Envelope(0, 0, w, h));
	}
	
	public RasterWorldInfo cleanRoundoffs() {
		AffineTransform2D at3 = cleanRoundoffs(tr, w, h);
		return new RasterWorldInfo(getImageSize(), at3);
	}

	private static AffineTransform2D cleanRoundoffs(AffineTransform2D tr, int w, int h) {
		double epsArea = 0.25 * Math.abs(tr.getAreaFactor()) / w / h;
		double epsLen = Math.sqrt(epsArea);
		BigDecimal bdLen = rndOnePlace(BigDecimal.valueOf(epsLen));

		AffineTransform2D at2 = AffineTransform2D.create(
			rnd(tr.getScaleX(), bdLen),
			rnd(tr.getShearX(), bdLen),
			rnd(tr.getShearY(), bdLen),
			rnd(tr.getScaleY(), bdLen),
			rnd(tr.getTranslateX(), bdLen),
			rnd(tr.getTranslateY(), bdLen)
		);
		
		BigDecimal bdTr = rndOnePlace(BigDecimal.valueOf(0.125 * Math.sqrt(Math.abs(at2.getAreaFactor()))));
		double[] arr2 = at2.paramsToArray();
		arr2[4] = rnd(arr2[4], bdTr);
		arr2[5] = rnd(arr2[5], bdTr);
		AffineTransform2D at3 = AffineTransform2D.create(arr2);
		return at3;
	}

	private static BigDecimal rndOnePlace(BigDecimal a) {
		BigDecimal ret = a.setScale(a.scale() - a.precision() + 1, RoundingMode.HALF_UP);
		int sc = ret.scale();
		int val = ret.unscaledValue().intValue();
		if (val > 5) {
			val = 5;
		}
		if (val == 3) {
			val = 2;
		}
		return new BigDecimal(BigInteger.valueOf(val), sc);
	}

	private static double rnd(double a, BigDecimal rndLen) {
		return rnd(a, rndLen, RoundingMode.HALF_UP);
	}
	private static double rnd(double a, BigDecimal rndLen, RoundingMode mode) {
		return BigDecimal.valueOf(a).divide(rndLen, 0, mode).multiply(rndLen).doubleValue();
	}
	
	public RasterWorldInfo setPixelSize(double tgtPxSize) {
		if (!isSimpleSquare()) {
			throw new IllegalStateException("Can't set pixel size if not simple square");
		}
		if (tr.getScaleX() < 0 || tr.getScaleY() > 0) {
			throw new IllegalStateException("Can't set pixel size if not in the form {sc 0 0 -sc trX trY}");
		}
		BigDecimal bdPx = BigDecimal.valueOf(tgtPxSize);
		double[] retTr = new double[] {
			tgtPxSize,
			0,
			0,
			-tgtPxSize,
			rnd(tr.getTranslateX(), bdPx, RoundingMode.FLOOR),
			rnd(tr.getTranslateY(), bdPx, RoundingMode.CEILING)
		};
		AffineTransform2D newTr = AffineTransform2D.create(retTr);
		double newW = newTr.inverse().x(tr.x(w, h), tr.y(w, h));
		double newH = newTr.inverse().y(tr.x(w, h), tr.y(w, h));
		RasterWorldInfo ret = new RasterWorldInfo(new DimI((int)Math.ceil(newW), (int)Math.ceil(newH)), newTr);
		if (!ret.wEnv.contains(wEnv)) {
			throw new IllegalStateException("Envelope after setting px size should contain the envelope before...");
		}
		return ret;
	}
}
