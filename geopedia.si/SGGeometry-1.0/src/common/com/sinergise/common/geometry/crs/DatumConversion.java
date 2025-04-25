package com.sinergise.common.geometry.crs;

import static com.sinergise.common.util.math.MathUtil.AS_IN_RAD;
import static com.sinergise.common.util.math.MathUtil.MAS_IN_RAD;
import static com.sinergise.common.util.math.MathUtil.hypot;
import static java.lang.Double.NaN;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;

import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;
import com.sinergise.common.geometry.crs.transform.Transform.InvertibleTransform;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.geom.Envelope;

public abstract class DatumConversion extends LatLonToLatLon<Ellipsoidal, Ellipsoidal> implements InvertibleTransform<Ellipsoidal, Ellipsoidal> {
	
	// Parameters from http://www.ordnancesurvey.co.uk/oswebsite/gps/docs/A_Guide_to_Coordinate_Systems_in_Great_Britain.pdf
	// or http://www.ordnancesurvey.co.uk/oswebsite/gps/information/coordinatesystemsinfo/guidecontents/guide6.html#6.5
	private static final double[] _TDR_I = {5.6, 4.8, -3.7, 0, 0, 0, 0};
	
	private static final double[] _TDRdot_I = {0, 0, 0, 0, 0.054, 0.518, -0.781};
	
	public static final PositionVector7Params ITRF2005_TO_ETRS89(double year) {
		return fromTcmDppbRmas(CRS.WGS84_ELLIPSOIDAL, CRS.ETRS89_ELLIPSOIDAL, applyDeltaFactor(_TDR_I, _TDRdot_I, year - 1989.0));
	}
	
	private static double[] applyDeltaFactor(double[] offset, double[] deltaPerFactor, double factor) {
		double[] ret = new double[offset.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = offset[i] + deltaPerFactor[i]*factor;
		}
		return ret;
	}

	public static final class Identity extends DatumConversion implements EnvelopeTransform {
		public Identity(Ellipsoidal src, Ellipsoidal tgt) {
			super(src, tgt);
		}
		@Override
		public void convertXYZ(double[] arg0) {
		}
		@Override
		public Identity inverse() {
			return new Identity(target, source);
		}
		@Override
		public double lat(double arg0, double arg1) {
			return arg0;
		}
		@Override
		public double lon(double arg0, double arg1) {
			return arg1;
		}
		@Override
		public Envelope envelope(Envelope src) {
			return new Envelope(src);
		}
	}
	/**
	 * <pre>
	 * rs = M * (1 + Rx<sup>2</sup> + Ry<sup>2</sup> + Rz<sup>2</sup>) * dot(RR, rt - dr)
	 * 
	 * RR = {{   1 + Rx<sup>2</sup>, Rx Ry + Rz , Rx Rz - Ry },
	 *       {Rx Ry - Rz,     1 + Ry<sup>2</sup>, Ry Rz + Rx },
	 *       {Rx Rz + Ry,  Ry Rz - Rx,     1 + Rz<sup>2</sup>}}
	 *       
	 * rt = {xt, yt, zt}
	 * dr = {dx, dy, dz}
	 * </pre>
	 * @author Miha
	 */
	public static class InversePositionVector7P extends DatumConversion {
		public final PositionVector7Params inverse;
		public final double fact;
		public final double mxx;
		public final double mxy;
		public final double mxz;
		public final double myx;
		public final double myy;
		public final double myz;
		public final double mzx;
		public final double mzy;
		public final double mzz;
		public final double dx;
		public final double dy;
		public final double dz;
		
		public final boolean rotate;
		public final boolean scale;
		
		public InversePositionVector7P(PositionVector7Params inverse) {
			super(inverse.getTarget(), inverse.getSource());
			this.inverse=inverse;
			rotate = inverse.rotate;
			scale = inverse.scale;

			dx = inverse.dx;
			dy = inverse.dy;
			dz = inverse.dz;

			final double Rx=inverse.Rx;
			final double Ry=inverse.Ry;
			final double Rz=inverse.Rz;
			
			final double inM = scale ? 1 : inverse.M;
			
			fact= rotate ? (inM*(1+Rx*Rx+Ry*Ry+Rz*Rz)) : inM;
			if (rotate) {
				mxx=(1 + Rx*Rx);
				mxy=Rx*Ry + Rz;
				mxz=Rx*Rz - Ry;
				myx=Rx*Ry - Rz;
				myy=1 + Ry*Ry;
				myz=Ry*Rz + Rx;
				mzx=Rx*Rz + Ry;
				mzy=Ry*Rz - Rx;
				mzz=1 + Rz*Rz;
			} else {
				mxx=1;
				mxy=0;
				mxz=0;
				myx=0;
				myy=1;
				myz=0;
				mzx=0;
				mzy=0;
				mzz=1;
			}
		}
		
		@Override
		public void convertXYZ(double[] srcXYZ) {
			final double x = srcXYZ[0]-dx;
			final double y = srcXYZ[1]-dy;
			final double z = srcXYZ[2]-dz;
			
			if (rotate) {
				if (scale) {
					srcXYZ[0] = (mxx*x+mxy*y+mxz*z)/fact;
					srcXYZ[1] = (myx*x+myy*y+myz*z)/fact;
					srcXYZ[2] = (mzx*x+mzy*y+mzz*z)/fact;
				} else {
					srcXYZ[0] = (mxx*x+mxy*y+mxz*z);
					srcXYZ[1] = (myx*x+myy*y+myz*z);
					srcXYZ[2] = (mzx*x+mzy*y+mzz*z);
				}
			} else if (scale) {
				srcXYZ[0] = x/fact;
				srcXYZ[1] = y/fact;
				srcXYZ[2] = z/fact;
			}
		}
		
		@Override
		public PositionVector7Params inverse() {
			return inverse;
		}
	}
	
	/**
	 * EPSG:9603
	 * @author Miha
	 *
	 */
	public static class GeocentricTranslation extends PositionVector7Params {
		
		public GeocentricTranslation(Ellipsoidal src, Ellipsoidal tgt, double[] dXYZ) {
			super(src, tgt, dXYZ, null, Double.NaN);
		}

		public GeocentricTranslation(Ellipsoidal src, Ellipsoidal tgt, double dx, double dy, double dz) {
			super(src, tgt, new double[]{dx,dy,dz}, null, Double.NaN);
		}

		@Override
		public GeocentricTranslation inverse() {
			return new GeocentricTranslation(target, source, new double[]{-dx,-dy,-dz});
		}
		
		@Override
		public void convertXYZ(double[] xyz) {
			xyz[0]+=dx;
			xyz[1]+=dy;
			xyz[2]+=dz;
		}
	}
	
	/**
	 * @param TDR {dx [cm], dy [cm], dz [cm], D [ppb], Rx [mas], Ry [mas], Rz [mas]} 
	 * @return datum conversion
	 */
	public static final PositionVector7Params fromTcmDppbRmas(Ellipsoidal src, Ellipsoidal tgt, double[] TDR) {
		 return new PositionVector7Params(src, tgt, 
		TDR[0]/100, TDR[1]/100, TDR[2]/100, 
		TDR[4] * MAS_IN_RAD, TDR[5] * MAS_IN_RAD, TDR[6] * MAS_IN_RAD,
		TDR[3] / 1000);
	}
	
	/**
	 * @param TDR {dx [m], dy [m], dz [m], Rx [as], Ry [as], Rz [as], D [ppm]} 
	 * @return datum conversion
	 */
	public static final PositionVector7Params fromMetreArcSecondPpm(Ellipsoidal src, Ellipsoidal tgt, double[] TRD) {
		 return new PositionVector7Params(src, tgt, 
		TRD[0], TRD[1], TRD[2], 
		TRD[3] * AS_IN_RAD, TRD[4] * AS_IN_RAD, TRD[5] * AS_IN_RAD,
		TRD[6]);
	}
	
	public static final CoordinateFrameRotation cfrFromMetreArcSecondPpm(Ellipsoidal src, Ellipsoidal tgt, double[] masppm) {
		return new CoordinateFrameRotation(src, tgt, //
			new double[] {masppm[0], masppm[1], masppm[2]},
			new double[] {masppm[3] * AS_IN_RAD, masppm[4] * AS_IN_RAD, masppm[5] * AS_IN_RAD},
			masppm[6]);
	}
	
	/**
	 * EPSG:9606
	 * @author Miha
	 */
	public static class PositionVector7Params extends DatumConversion {
		public final double dx;
		public final double dy;
		public final double dz;
		protected final double Rx;
		protected final double Ry;
		protected final double Rz;
		public final double M;
		public final double dM;
		public final boolean scale;
		public final boolean rotate;
		
		protected InversePositionVector7P inverse=null;

		/**
		 * 
		 * @param src
		 * @param tgt
		 * @param dxyz
		 *            in m
		 * @param Rxyz
		 *            in rad
		 * @param dS
		 *            in ppm
		 */
		public PositionVector7Params(Ellipsoidal src, Ellipsoidal tgt,
				double[] dxyz, double[] Rxyz, double dS) {
			this(src, tgt, dxyz == null ? NaN : dxyz[0], dxyz == null ? NaN
					: dxyz[1], dxyz == null ? NaN : dxyz[2], Rxyz == null ? NaN
					: Rxyz[0], Rxyz == null ? NaN : Rxyz[1], Rxyz == null ? NaN
					: Rxyz[2], dS);
		}
		
		/**
		 * @param src
		 * @param tgt
		 */
		public PositionVector7Params(Ellipsoidal src, Ellipsoidal tgt, double[] dRs) {
			this(src, tgt, dRs[0], dRs[1], dRs[2], dRs[3], dRs[4], dRs[5], dRs[6]);
		}
		
		/**
		 * 
		 * @param src
		 * @param tgt
		 * @param dx in m
		 * @param dy in m
		 * @param dz in m
		 * @param Rx in rad
		 * @param Ry in rad
		 * @param Rz in rad
		 * @param dS in ppm
		 */
		public PositionVector7Params(Ellipsoidal src, Ellipsoidal tgt, double dx, double dy, double dz, double Rx, double Ry, double Rz, double dS) {
			super(src, tgt);
			if (Double.isNaN(dS)) dS = 0;
			if (Double.isNaN(Rx)) Rx = 0;
			if (Double.isNaN(Ry)) Ry = 0;
			if (Double.isNaN(Rz)) Rz = 0;

			this.dx=dx;
			this.dy=dy;
			this.dz=dz;
			this.Rx=Rx;
			this.Ry=Ry;
			this.Rz=Rz;
			dM = dS == 0 ? 0 : dS/1e6;
			M = dM == 0 ? 0 : 1.0 + dM;

			scale = (dM != 0);
			rotate = (Rx != 0 && Ry != 0 && Rz !=0);
		}
	
		@Override
		public void convertXYZ(double[] xyz) {
			if (rotate) {
				double x=xyz[0];
				double y=xyz[1];
				double z=xyz[2];
				if (scale) {
					xyz[0] = M * (    x - Rz*y + Ry*z) + dx;
					xyz[1] = M * ( Rz*x +    y - Rx*z) + dy;
					xyz[2] = M * (-Ry*x + Rx*y +    z) + dz;
				} else {
					xyz[0] = (    x - Rz*y + Ry*z) + dx;
					xyz[1] = ( Rz*x +    y - Rx*z) + dy;
					xyz[2] = (-Ry*x + Rx*y +    z) + dz;
				}
			} else {
				if (scale) {
					xyz[0]+=dM*xyz[0]+dx;
					xyz[1]+=dM*xyz[1]+dy;
					xyz[2]+=dM*xyz[2]+dz;
				} else {
					xyz[0]+=dx;
					xyz[1]+=dy;
					xyz[2]+=dz;
				}
			}
		}
		
		@Override
		public DatumConversion inverse() {
			return new InversePositionVector7P(this);
		}
	}


	public static class CoordinateFrameRotation extends PositionVector7Params {
		/**
		 * 
		 * @param src
		 * @param tgt
		 * @param dxyz
		 *            in m
		 * @param Rxyz
		 *            in rad
		 * @param dS
		 *            in ppm
		 */
		public CoordinateFrameRotation(Ellipsoidal src, Ellipsoidal tgt, double[] dxyz, double[] rxyz, double dS) {
			super(src, tgt, dxyz, multiplyArray(copy(rxyz), -1), dS);
		}

		private static double[] copy(double[] rxyz) {
			double[] c=new double[rxyz.length];
			for(int i=0; i<rxyz.length;i++){
				c[i]=rxyz[i];
			}
			return c;
		}
	}
	
	public static class GeneralMatrix extends DatumConversion {
		protected double[][] mtrx;
		protected DatumConversion inverse;
		public GeneralMatrix(Ellipsoidal src, Ellipsoidal tgt, double[][] mtrx4x3) {
			this(src,tgt,mtrx4x3, null);
		}
		public GeneralMatrix(Ellipsoidal src, Ellipsoidal tgt, double[][] mtrx4x3, DatumConversion inverse) {
			super(src, tgt);
			this.mtrx=mtrx4x3;
			if (inverse!=null) {
				this.inverse=inverse;
				if (inverse instanceof GeneralMatrix) {
					if (inverse.getSource()!=target || inverse.getTarget()!=source) throw new IllegalArgumentException("Inverse should have inverted source/target");
					((GeneralMatrix)inverse).inverse=this;
				}
			}
		}
		@Override
		public void convertXYZ(double[] srcVec) {
			double tgtX = multiplyVectorsAdd1(mtrx[0],srcVec);
			double tgtY = multiplyVectorsAdd1(mtrx[1],srcVec);
			double tgtZ = multiplyVectorsAdd1(mtrx[2],srcVec);
			srcVec[0]=tgtX;
			srcVec[1]=tgtY;
			srcVec[2]=tgtZ;
		}
		@Override
		public DatumConversion inverse() {
			return inverse;
		}
	}
	
	public static double multiplyVectorsAdd1(double[] vec1, double[] vec2) {
		double ret=vec1[3];
		for (int i = 0; i < 3; i++) {
			ret+=vec1[i]*vec2[i];
		}
		return ret;
	}
	
	protected static double[] multiplyArray(double[] arr, double factor) {
		for (int i = arr.length-1; i >= 0; i--) {
			arr[i]*=factor;
		}
		return arr;
	}
	
	public DatumConversion(Ellipsoidal elSrc, Ellipsoidal elTgt) {
		super(elSrc, elTgt);
	}
	
	@Override
	public Point point(Point src, Point ret) {
		return point(src.x, src.y, src.z, ret);
	}
	
	/**
	 * Converts in geocentric cartesian 3D space; result overwrites the parameter
	 * @param xyz
	 */
	public abstract void convertXYZ(double[] xyz);
	
	@Override
	public abstract DatumConversion inverse();
	
	@Override
	public double lat(double srcLat, double srcLon) {
		return point(srcLat, srcLon, 0, new Point()).x;
	}
	@Override
	public double lon(double srcLat, double srcLon) {
		return point(srcLat, srcLon, 0, new Point()).y;
	}
	
	public Point point(double srcLat, double srcLon, double srcH, Point ret) {
		double[] xyz = new double[3];
		xyz = xyzFromGeodetic(srcLat, srcLon, Double.isNaN(srcH)?0:srcH, (source).ellipsoid, xyz);
		convertXYZ(xyz);
		updateCrsReference(ret);
		ret = fromXYZ(xyz, (target).ellipsoid, ret);
		if (Double.isNaN(srcH)) {
			ret.z = srcH;
		}
		return ret;
	}
	
	@Override
	public final Point point(Point pt) {
		boolean hasH = !Double.isNaN(pt.z);
		Point ret=xyzFromGeodetic(pt, (source).ellipsoid, null);
		convertXYZ(ret,ret);
		updateCrsReference(ret);
		ret = geodeticFromXYZ(ret, (target).ellipsoid, ret);
		if (!hasH) ret.z = Double.NaN;
		return ret;
	}
	
	private static Point fromXYZ(double[] xyz, Ellipsoid ellipsoid, Point retLatLon) {
		if (retLatLon==null) retLatLon=new Point();
		double p = hypot(xyz[0], xyz[1]);
		double fi = fiRadFromXYZ(p, xyz[2],ellipsoid);
		retLatLon.x = Math.toDegrees(fi);
		retLatLon.y = Math.toDegrees(lamRadFromXYZ(xyz[0], xyz[1], ellipsoid));
		retLatLon.z = hElFromXYZ(fi, p, ellipsoid);
		return retLatLon;
	}
	
	public static double[] geodeticFromXYZ_noiter(double[] xyz, Ellipsoid el, double[] retLatLonH) {
		if (retLatLonH==null) retLatLonH=new double[3];
		double p = hypot(xyz[0], xyz[1]);
		double fi = fiRadFromXYZ_noiter(p, xyz[2], el);
		retLatLonH[0]=toDegrees(fi);
		retLatLonH[1]=toDegrees(lamRadFromXYZ(xyz[0], xyz[1], el));
		retLatLonH[2]=hElFromXYZ(fi, p, el);
		return retLatLonH;
	}

	public static double[] geodeticFromXYZ(double[] xyz, Ellipsoid el, double[] retLatLonH) {
		if (retLatLonH==null) retLatLonH=new double[3];
		double p = hypot(xyz[0], xyz[1]);
		double fi = fiRadFromXYZ(p, xyz[2], el);
		retLatLonH[0]=toDegrees(fi);
		retLatLonH[1]=toDegrees(lamRadFromXYZ(xyz[0], xyz[1], el));
		retLatLonH[2]=hElFromXYZ(fi, p, el);
		return retLatLonH;
	}
	
	public static Point geodeticFromXYZ(Point xyz, Ellipsoid el, Point ret) {
		if (ret==null) ret=new Point();
		double p = hypot(xyz.x, xyz.y);
		double fi = fiRadFromXYZ(p, xyz.z, el);
		ret.setLocation(toDegrees(fi),toDegrees(lamRadFromXYZ(xyz.x, xyz.y, el)));
		ret.setZ(hElFromXYZ(fi, p, el));
		return ret;
	}
	
	public static double lamRadFromXYZ(double x, double y, @SuppressWarnings("unused") Ellipsoid el) {
		return atan2(y,x);
	}
	
	public static double fiRadFromXYZ_noiter(double p, double z, Ellipsoid el) {
		double q = atan2(z*el.a, p*el.b);
		double sinQ = sin(q);
		double cosQ = cos(q);
		double sinQ_3 = sinQ*sinQ*sinQ;
		double cosQ_3 = cosQ*cosQ*cosQ;
		return atan2(z+el.eps[1]*el.b*sinQ_3, p-el.eSq[1]*el.a*cosQ_3);
	}

	public static double hElFromXYZ(double fiRad, double p, Ellipsoid el) {
			double sinFi = sin(fiRad);
			double nu = el.a/Math.sqrt(1-el.eSq[1]*sinFi*sinFi);
			return (p / cos(fiRad)) - nu;
	}
	
	public static final DatumConversion compose(final DatumConversion c1, final DatumConversion c2) {
		return new DatumConversion(c1.source, c2.target) {
			@Override
			public DatumConversion inverse() {
				return compose(c2.inverse(), c1.inverse());
			}
			
			@Override
			public void convertXYZ(double[] xyz) {
				c1.convertXYZ(xyz);
				c2.convertXYZ(xyz);
			}
		};
	}
	
	public static double fiRadFromXYZ(double p, double z, Ellipsoid el) {
		double oldFi=-1;
		double fi=0;
		double nu = el.a;
		while (oldFi!=fi) {
			oldFi=fi;
			double sinFi = sin(fi);
			fi = atan2(z+el.eSq[1]*nu*sinFi,p);
			nu = el.a/sqrt(1-el.eSq[1]*sinFi*sinFi);
			if (Double.isNaN(fi)) return fi;
		}
		return fi;
	}

	public static final double[] xyzFromGeodetic(double lat, double lon, double hEl, Ellipsoid el, double[] retXYZ) {
		if (retXYZ==null) retXYZ=new double[3];
		double fiRad=Math.toRadians(lat);
		double lamRad=Math.toRadians(lon);
		double sinFi=Math.sin(fiRad);
		double cosFi=Math.cos(fiRad);
		double sinLam=Math.sin(lamRad);
		double cosLam=Math.cos(lamRad);
		double nu = el.a/Math.sqrt(1-el.eSq[1]*sinFi*sinFi);
		retXYZ[0]= (nu+hEl)*cosFi*cosLam;
		retXYZ[1]= (nu+hEl)*cosFi*sinLam;
		retXYZ[2]= ((1-el.eSq[1])*nu+hEl)*sinFi;
		return retXYZ;
	}
	
	public static final Point xyzFromGeodetic(Point gPoint, Ellipsoid el, Point ret) {
		return xyzFromGeodetic(gPoint.x, gPoint.y, Double.isNaN(gPoint.z)?0:gPoint.z,el,ret);
	}
	
	public static final Point xyzFromGeodetic(double lat, double lon, double hEl, Ellipsoid el, Point ret) {
		if (ret==null) ret=new Point();
		double fiRad=Math.toRadians(lat);
		double lamRad=Math.toRadians(lon);
		double sinFi=Math.sin(fiRad);
		double cosFi=Math.cos(fiRad);
		double sinLam=Math.sin(lamRad);
		double cosLam=Math.cos(lamRad);
		double nu = el.a/Math.sqrt(1-el.eSq[1]*sinFi*sinFi);
		ret.setLocation((nu+hEl)*cosFi*cosLam,(nu+hEl)*cosFi*sinLam);
		ret.setZ(((1-el.eSq[1])*nu+hEl)*sinFi);
		return ret;
	}

	/**
	 * Converts in geocentric cartesian 3D space; result overwrites the parameter
	 * @param xyz
	 */
	public Point convertXYZ(Point pGeocentricXYZ, Point retGeocentricXYZ) {
		if (retGeocentricXYZ==null) retGeocentricXYZ=new Point();
		double[] vals=new double[]{pGeocentricXYZ.x, pGeocentricXYZ.y, pGeocentricXYZ.z};
		convertXYZ(vals);
		retGeocentricXYZ.setLocation(vals[0], vals[1]);
		retGeocentricXYZ.setZ(vals[2]);
		return retGeocentricXYZ;
	}
}
