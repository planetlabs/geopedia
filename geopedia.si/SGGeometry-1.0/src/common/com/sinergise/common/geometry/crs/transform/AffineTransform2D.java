package com.sinergise.common.geometry.crs.transform;

import java.io.Serializable;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.transform.Transform.EnvelopeTransform;
import com.sinergise.common.geometry.crs.transform.Transform.InvertibleTransform;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.geom.Position2D;
import com.sinergise.common.util.math.MathUtil;

//TODO: Remove CRS-related stuff and move affine to SGUtil 
public class AffineTransform2D extends AbstractTransform<CRS, CRS> implements InvertibleTransform<CRS, CRS>, EnvelopeTransform, Serializable {

	private static final long serialVersionUID = 1L;

	public static AffineTransform2D fromFiducials(HasCoordinate[] src, HasCoordinate[] tgt) {
		double[][] srcArr = new double[src.length][2];
		double[][] tgtArr = new double[tgt.length][2];
		for (int i = 0; i < tgtArr.length; i++) {
			srcArr[i][0] = src[i].x();
			srcArr[i][1] = src[i].y();
			tgtArr[i][0] = tgt[i].x();
			tgtArr[i][1] = tgt[i].y();
		}
		return new AffineTransform2D(CRS.NONAME_WORLD_CRS, CRS.NONAME_WORLD_CRS, com.sinergise.common.util.math.fit.AffineFitUtil.fitGeneralAffine(srcArr, tgtArr));
	}
	
	public static AffineTransform2D createScale(double xScale, double yScale) {
		return createTrScale(xScale, yScale, 0, 0);
	}

	public static AffineTransform2D create(double trXX, double trXY, double trYX, double trYY, double trOffX, double trOffY) {
		return new AffineTransform2D(CRS.NONAME_WORLD_CRS, CRS.NONAME_WORLD_CRS, trXX, trXY, trYX, trYY, trOffX, trOffY);
	}
	
	public static AffineTransform2D create(double[] params) {
		return new AffineTransform2D(CRS.NONAME_WORLD_CRS, CRS.NONAME_WORLD_CRS, params);
	}

	public static AffineTransform2D createTrScale(double scaleX, double scaleY, double trOffX, double trOffY) {
		return create(scaleX, 0, 0, scaleY, trOffX, trOffY);
	}

	public static AffineTransform2D createTranslation(CRS src, CRS tgt, double trX, double trY) {
		return new AffineTransform2D(src, tgt, 1, 0, 0, 1, trX, trY);
	}
	
	public static final AffineTransform2D IDENTITY = createScale(1, 1);

	private double m0, m1, m2, m3, m4, m5;
    
    protected transient AffineTransform2D inverse = null;
    
    public AffineTransform2D() {
        super(null, null);
    }
    
    public AffineTransform2D(final CRS source, final CRS target, final double[] m) {
        super(source, target);
        m0 = m[0];
        m1 = m[1];
        m2 = m[2];
        m3 = m[3];
        m4 = m[4];
        m5 = m[5];
    }
    
    public AffineTransform2D(final CRS source, final CRS target, final double m0, final double m1, final double m2, final double m3, final double m4, final double m5) {
        super(source, target);
        this.m0 = m0;
        this.m1 = m1;
        this.m2 = m2;
        this.m3 = m3;
        this.m4 = m4;
        this.m5 = m5;
    }
    
    public AffineTransform2D(int w, int h, Envelope envTarget) {
    	 super(null, null);
    	this.m0 = envTarget.getWidth()/w;
    	this.m3 = envTarget.getHeight()/h;
    	this.m4 = w < 0 ? envTarget.getMaxX() : envTarget.getMinX();
    	this.m5 = h < 0 ? envTarget.getMaxY() : envTarget.getMinY();
	}

	public AffineTransform2D append(final AffineTransform2D o) {
        return new AffineTransform2D(source, o.getTarget(), new double[] {
                                                                          m0 * o.m0 + m1 * o.m2,
                                                                          m0 * o.m1 + m1 * o.m3,
                                                                          m2 * o.m0 + m3 * o.m2,
                                                                          m2 * o.m1 + m3 * o.m3,
                                                                          m4 * o.m0 + m5 * o.m2 + o.m4,
                                                                          m4 * o.m1 + m5 * o.m3 + o.m5 });
    }
    
    public AffineTransform2D concatenateWith(final AffineTransform2D o) {
    	final double newM0 = m0 * o.m0 + m1 * o.m2;
    	final double newM1 = m0 * o.m1 + m1 * o.m3;
    	final double newM2 = m2 * o.m0 + m3 * o.m2;
    	final double newM3 = m2 * o.m1 + m3 * o.m3;
    	final double newM4 = m4 * o.m0 + m5 * o.m2 + o.m4;
    	final double newM5 = m4 * o.m1 + m5 * o.m3 + o.m5;
    	return new AffineTransform2D(getSource(), o.getTarget(), newM0, newM1, newM2, newM3, newM4, newM5);
    }
    
	@Override
	public Envelope envelope(final Envelope src) {
		CrsIdentifier targetCrsId = target!=null ? target.getDefaultIdentifier() : null;
		EnvelopeBuilder eb = new EnvelopeBuilder(targetCrsId);
		if (!src.isEmpty()) {
			eb.expandToInclude(x(src.getMinX(), src.getMinY()), y(src.getMinX(), src.getMinY()));
			eb.expandToInclude(x(src.getMaxX(), src.getMinY()), y(src.getMaxX(), src.getMinY()));
			eb.expandToInclude(x(src.getMaxX(), src.getMaxY()), y(src.getMaxX(), src.getMaxY()));
			eb.expandToInclude(x(src.getMinX(), src.getMaxY()), y(src.getMinX(), src.getMaxY()));
		}
        return eb.getEnvelope();
    }
    
    public double getScaleX() {
        return m0;
    }
    
    public double getScaleY() {
        return m3;
    }
    
    public double getTranslateX() {
        return m4;
    }
    
    public double getTranslateY() {
        return m5;
    }
    
    @Override
	public AffineTransform2D inverse() {
        if (inverse == null) {
            final double det = 1.0 / (m0 * m3 - m1 * m2);
            final double[] params =
                                    new double[] { m3 * det, -m1 * det, -m2 * det, m0 * det,
                                                  det * (m2 * m5 - m3 * m4),
                                                  det * (m1 * m4 - m0 * m5) };
            inverse = new AffineTransform2D(target, source, params);
            inverse.inverse = this;
        }
        return inverse;
    }
    
    @Override
	public Point point(final Point src, Point ret) {
        if (ret == null) {
            ret = new Point();
        }
        ret.z = src.z;
        ret.x = x(src.x, src.y);
        ret.y = y(src.x, src.y);
        return ret;
    }
    
    public Position2D point(final Position2D src) {
    	return point(src.x, src.y);
    }
    
    public Position2D point(double x, double y) {
    	return new Position2D(x(x, y), y(x, y));
    }

	public AffineTransform2D scaledSource(double scX, double scY) {
        return new AffineTransform2D(source, target, new double[] { m0 * scX, m1 * scX, m2 * scY,
                                                                   m3 * scY, m4, m5 });
    }
	
	public AffineTransform2D translatedSource(double trSrcX, double trSrcY) {
		double[] m = paramsToArray();
		m[4] = x(trSrcX, trSrcY);
		m[5] = y(trSrcX, trSrcY);
		return AffineTransform2D.create(m);
	}
    
    @Override
    public String toString() {
        return "(" + m0 + "|" + m2 + "|" + m4 + ")(" + m1 + "|" + m3 + "|" + m5 + ")";
    }
    
    /**
     * Transforms an array of double precision coordinates by this transform. The two coordinate
     * array sections can be exactly the same or can be overlapping sections of the same array
     * without affecting the validity of the results. This method ensures that no source coordinates
     * are overwritten by a previous operation before they can be transformed. The coordinates are
     * stored in the arrays starting at the indicated offset in the order
     * <code>[x0, y0, x1, y1, ..., xn, yn]</code>.
     * 
     * @param srcPts
     *            the array containing the source point coordinates. Each point is stored as a pair
     *            of x,&nbsp;y coordinates.
     * @param dstPts
     *            the array into which the transformed point coordinates are returned. Each point is
     *            stored as a pair of x,&nbsp;y coordinates.
     * @param srcOff
     *            the offset to the first point to be transformed in the source array
     * @param dstOff
     *            the offset to the location of the first transformed point that is stored in the
     *            destination array
     * @param numPts
     *            the number of point objects to be transformed
     */
    public void transform(final double[] srcPts, int srcOff, final double[] dstPts, int dstOff,
                          int numPts) {
        double M0, M1, M2, M3, M4, M5; // For caching
        if ((dstPts == srcPts) && (dstOff > srcOff) && (dstOff < srcOff + numPts * 2)) {
            // If the arrays overlap partially with the destination higher
            // than the source and we transform the coordinates normally
            // we would overwrite some of the later source coordinates
            // with results of previous transformations.
            // To get around this we use arraycopy to copy the points
            // to their final destination with correct overwrite
            // handling and then transform them in place in the new
            // safer location.
            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2);
            srcOff = dstOff;
        }
        M0 = m0;
        M2 = m1;
        M4 = m4;
        M1 = m2;
        M3 = m3;
        M5 = m5;
        while (--numPts >= 0) {
            final double x = srcPts[srcOff++];
            final double y = srcPts[srcOff++];
            dstPts[dstOff++] = M0 * x + M2 * y + M4;
            dstPts[dstOff++] = M1 * x + M3 * y + M5;
        }
    }
    
    public AffineTransform2D translate(final double trX, final double trY) {
    	return new AffineTransform2D(source, target, m0, m1, m2, m3, m4 + trX, m5 + trY);
    }
    
    public double x(final double x, final double y) {
        return m0 * x + m2 * y + m4;
    }
    
    public double y(final double x, final double y) {
        return m1 * x + m3 * y + m5;
    }

	public double[] paramsToArray() {
		return new double[]{m0, m1, m2, m3, m4, m5};
	}

	public boolean isTranslateOnly() {
		return m0==1 && m1 == 0 && m2 == 0 && m3 == 1;
	}

	/**
	 * @return true whether this transform contains only scale (x and/or y) and translate parameters
	 */
	public boolean isSimpleRectangular() {
		return m1 == 0 && m2 == 0;
	}

	public double getAreaFactor() {
		return m0*m3 - m1*m2;
	}

	public double getShearX() {
		return m1;
	}

	public double getShearY() {
		return m2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + MathUtil.hashCode(m0);
		result = prime * result + MathUtil.hashCode(m1);
		result = prime * result + MathUtil.hashCode(m2);
		result = prime * result + MathUtil.hashCode(m3);
		result = prime * result + MathUtil.hashCode(m4);
		result = prime * result + MathUtil.hashCode(m5);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof AffineTransform2D)) {
			return false;
		}
		AffineTransform2D other = (AffineTransform2D)obj;
		return (m0 == other.m0) && (m1 == other.m1) && //
			(m2 == other.m2) && (m3 == other.m3) && //
			(m4 == other.m4) && (m5 == other.m5);
	}

	public AffineTransform2D scale(double scx, double scy) {
		return append(AffineTransform2D.createScale(scx, scy));
	}

	public static Position2D rotate(HasCoordinate point, double angRad) {
		final double x = point.x();
		final double y = point.y();
		final double sin = Math.sin(angRad);
		final double cos = Math.cos(angRad);
		return new Position2D(cos * x + sin * y, -sin * x + cos * y);
	}
	
	
}
