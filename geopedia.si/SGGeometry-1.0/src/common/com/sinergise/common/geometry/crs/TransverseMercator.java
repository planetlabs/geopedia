/*
 *
 */
package com.sinergise.common.geometry.crs;

import static com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority.EPSG;
import static com.sinergise.common.util.math.MathUtil.DEGREE_IN_RAD;
import static com.sinergise.common.util.math.MathUtil.RAD_IN_DEGREES;

import com.sinergise.common.geometry.crs.CartesianCRS.ProjectedEllipsoidalCRS;
import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;
import com.sinergise.common.geometry.crs.transform.Transform.EnvelopeTransform;
import com.sinergise.common.geometry.crs.transform.Transform.InvertibleTransform;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.common.util.math.Units;
import com.sinergise.common.util.math.Units.Unit;


public class TransverseMercator extends ProjectedEllipsoidalCRS {
	public static class UTM extends TransverseMercator {
		public static final double UTM_SCALE_FACTOR = 0.9996;
		public static final double UTM_OFF_X = 500000;
		public static final double UTM_S_OFF_Y = 10000000;

		public UTM(CrsIdentifier id, double long0_deg, Envelope bnds) {
			this(id, long0_deg, bnds, true);
		}

		public UTM(CrsIdentifier id, double long0_deg, Envelope bnds, boolean north) {
			this(CRS.WGS84_ELLIPSOIDAL, id, long0_deg, bnds, north);
		}

		public UTM(Ellipsoidal source, CrsIdentifier id, double long0_deg, Envelope bnds, boolean north) {
			super(source, 0, long0_deg, UTM_SCALE_FACTOR, id, bnds);
			setNiceName("UTM " + zoneNumber(long0_deg) + " " + (north ? "North" : "South"));
			offX = UTM_OFF_X;
			offY = north ? 0 : UTM_S_OFF_Y;
		}

		public static int zoneNumber(double long0) {
			return (int)Math.round((long0 - 3) / 6) + 31;
		}

		public static int centralLongForZone(int zoneNumber) {
			return (zoneNumber - 31) * 6 + 3;
		}

		public static char latBandChar(double long0, double lat0) {
			if (lat0 < -80) {
				return long0 < 0 ? 'A' : 'B';
			}
			if (lat0 > 84) {
				return long0 < 0 ? 'Y' : 'Z';
			}
			if (lat0 > 72) {
				return 'X';
			}
			return (char)('C' + (((int)Math.floor(lat0) + 80) / 8));
		}

		public static Envelope getNorthEnvelope() {
			// When the French defined metre, they made a slight error so north pole is a bit more than 10 000 km from the equator :)
			return new Envelope(0, 0, 2 * UTM_OFF_X, 10002000);
		}
	}

	public static class GeographicToTM extends LatLonToCartesian<Ellipsoidal, TransverseMercator> implements EnvelopeTransform, InvertibleTransform<Ellipsoidal, TransverseMercator> {
		public static final double[] sin1sec = new double[]{
			1, 4.848136811076367820079090940916766269913530126893986867406305655416969e-6,
			2.350443053891373300040836348809403368598727425020523283437257740599377e-11,
			1.139526949190952190351625195432357536053751820581441757340160614794060e-16,
			5.524582549586205171215883184763584799593399952342146009161871273362703e-22,
			2.678393202447901443452916571894585406226172287838271710006279804681723e-27,
			1.298521667932438934788972131285209540250592239028510830275262982705165e-32};

		public GeographicToTM(TransverseMercator proj) {
			super(proj.sourceCRS, proj);
		}

		@Override
		public TMToGeographic inverse() {
			return new TMToGeographic(this.target);
		}

		@Override
		public double x(double lat, double lon) {
			return point(new Point(lat, lon), new Point()).x;
		}

		@Override
		public double y(double lat, double lon) {
			return point(new Point(lat, lon), new Point()).y;
		}

		/**
		 * Transformation from EPSG guidance note 7/2
		 */
		@Override
		public Point point(Point src, Point ret) {
			TransverseMercator proj = target;
			Ellipsoid el = (proj.sourceCRS).ellipsoid;

			double lam = src.y * DEGREE_IN_RAD;
			double lam0 = proj.lam0_deg * DEGREE_IN_RAD;

			double fi = src.x * DEGREE_IN_RAD;
			double sinFi = Math.sin(fi);
			double cosFi = Math.cos(fi);
			double tanFi = sinFi / cosFi;

			double nu = el.a / Math.sqrt(1 - el.eSq[1] * sinFi * sinFi);

			double[] A = MathUtil.powers((lam - lam0) * cosFi, 6);
			double C = cosFi * cosFi * el.ePrimeSq[1];
			double T = tanFi * tanFi;
			double Tsq = T * T;
			ret.x = proj.offX + proj.k0 * nu * (//
				A[1]//
					+ (1 - T + C) * A[3] / 6 //
				+ (5 - 18 * T + Tsq + 72 * C - 58 * el.ePrimeSq[1]) * A[5] / 120);

			double M = M(el, fi);
			double M0 = proj.M0;

			ret.y = proj.offY
				+ proj.k0
				* (M - M0 + nu
					* tanFi
					* (0.5 * A[2] + (5 - T + 9 * C + 4 * C * C) * A[4] / 24 + (61 - 58 * T + Tsq + 600 * C - 330 * el.ePrimeSq[1])
						* A[6] / 720));

			ret.z = src.z;
			updateCrsReference(ret);
			return ret;
		}

		@Override
		public Envelope envelope(Envelope src) {
			EnvelopeBuilder ret = new EnvelopeBuilder(target.getDefaultIdentifier());

			final TransverseMercator proj = target;
			final Point srcPoint = new Point();
			final Point tgtPoint = new Point();

			double minX = src.getMinX();
			double minY = src.getMinY();
			double maxY = src.getMaxY();
			double maxX = src.getMaxX();

			srcPoint.setLocation(minX, minY);
			ret.expandToInclude(point(srcPoint, tgtPoint));

			srcPoint.setLocation(minX, maxY);
			ret.expandToInclude(point(srcPoint, tgtPoint));

			srcPoint.setLocation(maxX, maxY);
			ret.expandToInclude(point(srcPoint, tgtPoint));

			srcPoint.setLocation(maxX, minY);
			ret.expandToInclude(point(srcPoint, tgtPoint));

			final double centX = src.getCenterX();
			final double centY = src.getCenterY();

			if (src.contains(proj.fi0_deg, centY)) {
				srcPoint.setLocation(proj.fi0_deg, minY);
				ret.expandToInclude(point(srcPoint, tgtPoint));

				srcPoint.setLocation(proj.fi0_deg, maxY);
				ret.expandToInclude(point(srcPoint, tgtPoint));
			}
			if (src.contains(centX, proj.lam0_deg)) {
				srcPoint.setLocation(minX, proj.lam0_deg);
				ret.expandToInclude(point(srcPoint, tgtPoint));

				srcPoint.setLocation(maxX, proj.lam0_deg);
				ret.expandToInclude(point(srcPoint, tgtPoint));
			}
			return ret.getEnvelope();
		}
	}

	public static class TMToGeographic extends CartesianToLatLon<TransverseMercator, Ellipsoidal> implements EnvelopeTransform, InvertibleTransform<TransverseMercator, Ellipsoidal> {
		public TMToGeographic(TransverseMercator projected) {
			super(projected, projected.sourceCRS);
		}

		@Override
		public GeographicToTM inverse() {
			return new GeographicToTM(source);
		}

		@Override
		public double lat(double x, double y) {
			return point(new Point(x, y), new Point()).x;
		}

		@Override
		public double lon(double x, double y) {
			return point(new Point(x, y), new Point()).y;
		}


		@Override
		public Point point(final Point src, final Point ret) {
			final TransverseMercator projTM = source;
			final Ellipsoid el = (projTM.sourceCRS).ellipsoid;

			final double lam0 = projTM.lam0_deg * DEGREE_IN_RAD;
			final double M1 = projTM.M0 + (src.y - projTM.offY) / projTM.k0;
			final double mu1 = M1 / el.muFact;


			final double fi1 = F(el, mu1);
			final double cosFi1 = Math.cos(fi1);
			final double sinSqFi1 = 1 - cosFi1 * cosFi1;
			final double tanFi1 = Math.tan(fi1);

			final double oneMinEsqSinSqFi1 = 1 - el.eSq[1] * sinSqFi1;
			final double nu1 = el.a / Math.sqrt(oneMinEsqSinSqFi1);
			final double ro1 = el.a * (1 - el.eSq[1]) / Math.pow(oneMinEsqSinSqFi1, 1.5);
			final double[] D = MathUtil.powers((src.x - projTM.offX) / (projTM.k0 * nu1), 6);


			final double T1 = tanFi1 * tanFi1;
			final double C1 = el.ePrimeSq[1] * cosFi1 * cosFi1;
			final double C1sq = C1 * C1;
			final double T1sq = T1 * T1;

			final double fi = fi1
				- (nu1 * Math.tan(fi1) / ro1)
				* (0.5 * D[2] - (5 + 3 * T1 + 10 * C1 - 4 * C1sq - 9 * el.ePrimeSq[1]) * D[4] / 24 + (61 + 90 * T1
					+ 298 * C1 + 45 * T1sq - 252 * el.ePrimeSq[1] - 3 * C1sq)
					* D[6] / 720);

			final double lam = lam0
				+ (D[1] - (1 + 2 * T1 + C1) * D[3] / 6 + (5 - 2 * C1 + 28 * T1 - 3 * C1sq + 8 * el.ePrimeSq[1] + 24 * T1sq)
					* D[5] / 120) / cosFi1;

			ret.x = fi * RAD_IN_DEGREES;
			ret.y = lam * RAD_IN_DEGREES;
			ret.z = src.z;
			updateCrsReference(ret);
			return ret;
		}

		@Override
		public Envelope envelope(Envelope src) {
			EnvelopeBuilder ret = new EnvelopeBuilder(target.getDefaultIdentifier());
			Point srcPoint = new Point();
			Point tgtPoint = new Point();

			double minX = src.getMinX();
			double minY = src.getMinY();
			double maxY = src.getMaxY();
			double maxX = src.getMaxX();

			srcPoint.setLocation(minX, minY);
			ret.expandToInclude(point(srcPoint, tgtPoint));

			srcPoint.setLocation(minX, maxY);
			ret.expandToInclude(point(srcPoint, tgtPoint));

			srcPoint.setLocation(maxX, maxY);
			ret.expandToInclude(point(srcPoint, tgtPoint));

			srcPoint.setLocation(maxX, minY);
			ret.expandToInclude(point(srcPoint, tgtPoint));

			double centY = 0.5 * (minY + maxY);
			double centX = 0.5 * (minX + maxX);

			if (src.contains(source.offX, centY)) {
				srcPoint.setLocation(source.offX, minY);
				ret.expandToInclude(point(srcPoint, tgtPoint));

				srcPoint.setLocation(source.offX, maxY);
				ret.expandToInclude(point(srcPoint, tgtPoint));
			}
			if (src.contains(centX, source.offY)) {
				srcPoint.setLocation(minX, source.offY);
				ret.expandToInclude(point(srcPoint, tgtPoint));

				srcPoint.setLocation(maxX, source.offY);
				ret.expandToInclude(point(srcPoint, tgtPoint));
			}
			return ret.getEnvelope();
		}
	}


	public static double M(Ellipsoid el, double fi) {
		double sin2Fi = Math.sin(2 * fi);
		double sin4Fi = Math.sin(4 * fi);
		double sin6Fi = Math.sin(6 * fi);
		return el.a * (el.MFacts[0] * fi + el.MFacts[1] * sin2Fi + el.MFacts[2] * sin4Fi + el.MFacts[3] * sin6Fi);
	}

	public static double F(Ellipsoid el, double mu) {
		return mu + el.FFacts[0] * Math.sin(2 * mu) + el.FFacts[1] * Math.sin(4 * mu) + el.FFacts[2] * Math.sin(6 * mu)
			+ el.FFacts[3] * Math.sin(8 * mu);
	}

	/**
	 * Scale factor at central meridian
	 */
	private final double k0;
	/**
	 * Central meridian
	 */
	public final double lam0_deg;
	/**
	 * Central latitude
	 */
	private final double fi0_deg;
	/**
	 * False origin of the projection (x of the central meridian x=offX)
	 */
	public double offX;
	/**
	 * False origin of the projection (y of the equator y=offY)
	 */
	public double offY;

	private transient double M0;

	@Deprecated
	//Serialization only
	protected TransverseMercator() {
		super();
		fi0_deg = Double.NaN;
		k0 = Double.NaN;
		lam0_deg = Double.NaN;
	}

	public TransverseMercator(Ellipsoidal baseCRS, double lat0_deg, double long0_deg, double k0, CrsIdentifier id,
		Envelope bnds) {
		this(baseCRS, lat0_deg, long0_deg, Units.METRE, k0, id, bnds);
	}

	public TransverseMercator(Ellipsoidal baseCRS, double lat0_deg, double long0_deg, Unit linearUnit, double k0,
		CrsIdentifier id, Envelope bnds) {
		super(baseCRS, id, bnds);
		this.fi0_deg = lat0_deg;
		this.lam0_deg = long0_deg;
		this.k0 = linearUnit.convertFromBase(k0);
		updateTransient();
	}

	public double getFalseEasting() {
		return offX;
	}

	public double getFalseNorthing() {
		return offY;
	}

	public double getOriginLongitude() {
		return lam0_deg;
	}

	public double getOriginLatitude() {
		return fi0_deg;
	}

	public TransverseMercator setOffset(double offX, double offY) {
		this.offX = offX;
		this.offY = offY;
		return this;
	}

	public TMToGeographic createTransformToGeographic() {
		return new TMToGeographic(this);
	}

	public GeographicToTM createTransformFromGeographic() {
		return new GeographicToTM(this);
	}

	private void updateTransient() {
		M0 = M((sourceCRS).ellipsoid, fi0_deg * DEGREE_IN_RAD);
	}

	public static UTM createWGS84N(int zoneNumber) {
		return createNorth(CRS.WGS84, 32600, zoneNumber);
	}

	public static UTM createNorth(Ellipsoidal baseCRS, int epsgCodeOffset, int zoneNumber) {
		return new UTM(baseCRS, new CrsIdentifier(EPSG, epsgCodeOffset + zoneNumber),
			UTM.centralLongForZone(zoneNumber), new Envelope(150000, 0, 850000, 10000000), true);
	}
}
