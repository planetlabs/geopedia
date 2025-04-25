/*
 *
 */
package com.sinergise.common.geometry.crs.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CartesianCRS;
import com.sinergise.common.geometry.crs.CartesianCRS.ProjectedCRS;
import com.sinergise.common.geometry.crs.CartesianCRS.PseudoPlatteCarreeToLatLon;
import com.sinergise.common.geometry.crs.CartesianToCartesian;
import com.sinergise.common.geometry.crs.CartesianToLatLon;
import com.sinergise.common.geometry.crs.CrsRepository;
import com.sinergise.common.geometry.crs.DatumConversion;
import com.sinergise.common.geometry.crs.LatLonCRS;
import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;
import com.sinergise.common.geometry.crs.LatLonToCartesian;
import com.sinergise.common.geometry.crs.TransverseMercator;
import com.sinergise.common.geometry.crs.TransverseMercator.GeographicToTM;
import com.sinergise.common.geometry.crs.TransverseMercator.TMToGeographic;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.ToCartesian;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.ToLatLon;
import com.sinergise.common.geometry.crs.transform.Transform.EnvelopeTransform;
import com.sinergise.common.geometry.crs.transform.Transform.InvertibleTransform;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;


public class Transforms {
	public static final HashMap<CRS, HashMap<CRS, Transform<?, ?>>> registry = new HashMap<CRS, HashMap<CRS, Transform<?, ?>>>();

	public static final void register(Transform<?, ?> tr) {
		HashMap<CRS, Transform<?, ?>> srcMap = registry.get(tr.getSource());
		if (srcMap == null) {
			srcMap = new HashMap<CRS, Transform<?, ?>>();
			registry.put(tr.getSource(), srcMap);
		}
		Transform<?, ?> existing = srcMap.get(tr.getTarget());
		if (existing == tr)
			return;
		srcMap.put(tr.getTarget(), tr);
	}

	public static Envelope transformWithCorners(Transform<?, ?> tr, Envelope src) {
		EnvelopeBuilder bld = new EnvelopeBuilder(tr.getTarget().getDefaultIdentifier());
		bld.expandToInclude(tr.point(new Point(src.topLeft()), new Point()));
		bld.expandToInclude(tr.point(new Point(src.topRight()), new Point()));
		bld.expandToInclude(tr.point(new Point(src.bottomLeft()), new Point()));
		bld.expandToInclude(tr.point(new Point(src.bottomRight()), new Point()));
		return bld.getEnvelope();
	}

	public static final PseudoPlatteCarreeToLatLon<Ellipsoidal> PSEUDO_PLATE_CARREE_TO_WGS84 = new CartesianCRS.PseudoPlatteCarreeToLatLon<Ellipsoidal>(
		CRS.WGS84_GLOBAL_PSEUDO_PLATTE_CARRE);

	public static CartesianToLatLon<TransverseMercator, Ellipsoidal> D48_TO_WGS84 = new D48xyToWGS84LatLon();
	public static LatLonToCartesian<Ellipsoidal, TransverseMercator> WGS84_TO_D48 = new WGS84LatLonToD48xy();
	public static D48xyToD96xy D48_TO_D96 = new D48xyToD96xy();
	public static D96xyToD48xy D96_TO_D48 = new D96xyToD48xy();
	public static ToCartesian<Ellipsoidal, TransverseMercator> WGS84_TO_D96 = compose(WGS84_TO_D48, D48_TO_D96);
	public static ToLatLon<TransverseMercator, Ellipsoidal> D96_TO_WGS84 = compose(D96_TO_D48, D48_TO_WGS84);

	public static GeographicToTM MGI_TO_SI_D48 = new GeographicToTM(CRS.SI_D48);
	public static TMToGeographic SI_D48_TO_MGI = new TMToGeographic(CRS.SI_D48);

	public static GeographicToTM MGI_TO_D48_GK = new GeographicToTM(CRS.D48_GK);
	public static TMToGeographic D48_GK_TO_MGI = new TMToGeographic(CRS.D48_GK);
	public static CartesianToCartesian<TransverseMercator, TransverseMercator> D48_GK_TO_SI_D48 = new SimpleTransform.TranslationScale<TransverseMercator, TransverseMercator>(
		CRS.D48_GK, CRS.SI_D48, 0, 5e6);
	public static CartesianToCartesian<TransverseMercator, TransverseMercator> SI_D48_TO_D48_GK = new SimpleTransform.TranslationScale<TransverseMercator, TransverseMercator>(
		CRS.SI_D48, CRS.D48_GK, 0, -5e6);
	public static GeographicToTM ETRS89_TO_D96_TM = new GeographicToTM(CRS.D96_TM);
	public static TMToGeographic D96_TM_TO_ETRS89 = new TMToGeographic(CRS.D96_TM);

	public static CartesianToLatLon<TransverseMercator, Ellipsoidal> BNG_TO_OSGB1936 = new TMToGeographic(CRS.BNG);
	public static GeographicToTM OSGB1936_TO_BNG = new GeographicToTM(CRS.BNG);

	public static final double applyMatrixToXYPowers(double ax, double ay, double[] AP, final int maxPower) {
		double ret = 0;
		double powy = 1;
		int idx = 0;
		for (int iy = 0; iy <= maxPower; iy++) {
			double powXY = powy;
			for (int ix = 0; ix <= maxPower; ix++) {
				ret += AP[idx++] * powXY;
				powXY *= ax;
			}
			powy *= ay;
		}
		return ret;
	}

	/**
     */
	public static final <A extends CRS, B extends CRS, C extends CartesianCRS> ToCartesian<A, C> compose(
		final Transform<A, B> t1, final ToCartesian<B, C> t2) {
		ToCartesian<A, C> tr = new CompositeToCartesian<A, B, C>(t1, t2);
		register(tr);
		return tr;
	}

	/**
	 * this method creates transformation instance of type CartesianToLatLon WGS84_TO_D96 = WGS84_TO_D48 followed by
	 * D48_TO_D96
	 * 
	 * @param t1 - WGS84_TO_D48 transformation instance from this class
	 * @param t2 - D48_TO_D96 transformation instance from this class
	 * @return WGS84_TO_D96 transformation instance from this class
	 */
	public static final <A extends CRS, B extends CRS, C extends LatLonCRS> ToLatLon<A, C> compose(
		final Transform<A, B> t1, final ToLatLon<B, C> t2) {
		ToLatLon<A, C> tr = new CompositeToLatLon<A, B, C>(t1, t2);
		register(tr);
		return tr;
	}

	public static final <A extends CRS, B extends CRS, C extends CRS> Transform<A, C> compose(final Transform<A, B> t1,
		final Transform<B, C> t2) {
		AbstractTransform<A, C> tr = new CompositeTransform<A, B, C>(t1, t2);
		register(tr);
		return tr;
	}

	public static Transform<?, ?> find(CrsIdentifier sourceId, CrsIdentifier targetId) {
		CRS source = CrsRepository.INSTANCE.get(sourceId);
		CRS target = CrsRepository.INSTANCE.get(targetId);

		if (source == null || target == null) {
			return null;
		}

		return find(source, target);
	}

	@SuppressWarnings("unchecked")
	public static <S extends CRS, T extends CRS> Transform<S, T> find(S source, T target) {
		if (source.equals(target))
			return (Transform<S, T>)new AbstractTransform.Identity<S>(source);

		HashMap<CRS, Transform<?, ?>> transs = registry.get(source);
		if (transs != null) {
			Transform<?, ?> ret = transs.get(target);
			if (ret != null) {
				return (Transform<S, T>)ret;
			}
		}

		// TODO: Make registry into a graph with CRS as nodes and Transform edges
		// TODO: Find transform by looking for the shortest path in the graph

		// XXX: This is ugly

		//WGS 
		if (source.equals(CRS.WGS84)) {
			if (target.equals(CRS.D48_GK))
				return (Transform<S, T>)WGS84_TO_D48;
			else if (target.equals(CRS.D96_TM))
				return (Transform<S, T>)WGS84_TO_D96;
		}

		// UK
		else if (source.equals(CRS.BNG)) {
			if (target.equals(CRS.OSGB1936_ELLIPSOIDAL))
				return (Transform<S, T>)BNG_TO_OSGB1936;
		} else if (source.equals(CRS.OSGB1936_ELLIPSOIDAL)) {
			if (target.equals(CRS.BNG))
				return (Transform<S, T>)OSGB1936_TO_BNG;
		}

		// Slo D48 GK
		else if (source.equals(CRS.D48_GK)) {
			if (target.equals(CRS.MGI_BESSEL_ELLIPSOIDAL))
				return (Transform<S, T>)D48_GK_TO_MGI;
			else if (target.equals(CRS.WGS84_ELLIPSOIDAL))
				return (Transform<S, T>)D48_TO_WGS84;
			else if (target.equals(CRS.D96_TM))
				return (Transform<S, T>)D48_TO_D96;
			else if (target.equals(CRS.SI_D48))
				return (Transform<S, T>)D48_GK_TO_SI_D48;
		}
		// MGI
		else if (source.equals(CRS.MGI_BESSEL_ELLIPSOIDAL)) {
			if (target.equals(CRS.D48_GK))
				return (Transform<S, T>)MGI_TO_D48_GK;
			else if (target.equals(CRS.SI_D48))
				return (Transform<S, T>)MGI_TO_SI_D48;
		}
		// Slo D96 TM
		else if (source.equals(CRS.D96_TM)) {
			if (target.equals(CRS.ETRS89_ELLIPSOIDAL))
				return (Transform<S, T>)D96_TM_TO_ETRS89;
			else if (target.equals(CRS.WGS84_ELLIPSOIDAL))
				return (Transform<S, T>)D96_TO_WGS84;
			else if (target.equals(CRS.D48_GK))
				return (Transform<S, T>)D96_TO_D48;
		}
		// ETRS89
		else if (source.equals(CRS.ETRS89_ELLIPSOIDAL)) {
			if (target.equals(CRS.D96_TM))
				return (Transform<S, T>)ETRS89_TO_D96_TM;
		} else if (source.equals(CRS.SI_D48)) {
			if (target.equals(CRS.MGI_BESSEL_ELLIPSOIDAL))
				return (Transform<S, T>)SI_D48_TO_MGI;
			else if (target.equals(CRS.D48_GK))
				return (Transform<S, T>)SI_D48_TO_D48_GK;
		}

		return null;
	}

	public static Transform<?, ?>[] getDefaultTransforms(CRS sourceCRS) {
		CRS[] cs = getDefaultTargets(sourceCRS);
		if (cs == null)
			return null;
		ArrayList<Transform<?, ?>> ret = new ArrayList<Transform<?, ?>>(cs.length);
		for (int i = 0; i < cs.length; i++) {
			Transform<?, ?> tr = find(sourceCRS, cs[i]);
			if (tr != null)
				ret.add(tr);
		}
		return ArrayUtil.toArray(ret, new Transform<?, ?>[ret.size()]);
	}

	public static CRS[] getDefaultTargets(CRS sourceCRS) {
		HashMap<CRS, Transform<?, ?>> transs = registry.get(sourceCRS);
		if (transs != null) {
			Set<CRS> keySet = transs.keySet();
			CRS[] ret = new CRS[keySet.size() + 1];
			int i = 0;
			ret[i] = sourceCRS;
			for (CRS crs : keySet) {
				ret[++i] = crs;
			}
			return ret;
		}
		if (sourceCRS instanceof ProjectedCRS<?>) {
			return new CRS[]{sourceCRS, ((ProjectedCRS<?>)sourceCRS).sourceCRS};
		}
		return null;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static <S extends CRS, T extends CRS> Transform<S, T> swap() {
		return new AbstractTransform.Swap(CRS.NONAME_WORLD_CRS);
	}

	public static class D48xyToD96xy extends CartesianToCartesian<TransverseMercator, TransverseMercator> implements InvertibleTransform<TransverseMercator, TransverseMercator>, EnvelopeTransform {
		public static final double A = -3.780832235459238e2;
		public static final double B = 4.966289143937757e2;
		public static final double C = 1.000010067293144;
		public static final double D = -0.000024622864943;

		public D48xyToD96xy() {
			super(CRS.D48_GK, CRS.D96_TM);
		}

		/**
		 * E
		 */
		@Override
		public double x(double sourceX, double sourceY) {
			return A + C * sourceX - D * sourceY;
		}

		@Override
		public double y(double sourceX, double sourceY) {
			return B + D * sourceX + C * sourceY;
		}

		@Override
		public Point point(Point src, Point ret) {
			//in case src == ret
			double x = src.x;
			double y = src.y;

			ret.x = x(x, y);
			ret.y = y(x, y);
			ret.z = src.z;
			return ret;
		}

		@Override
		public InvertibleTransform<TransverseMercator, TransverseMercator> inverse() {
			return D96_TO_D48;
		}

		@Override
		public Envelope envelope(Envelope src) {
			return transformWithCorners(this, src);
		}
	}

	public static class D96xyToD48xy extends CartesianToCartesian<TransverseMercator, TransverseMercator> implements InvertibleTransform<TransverseMercator, TransverseMercator>, EnvelopeTransform {
		public static final double A = 3.780916661183001e2;
		public static final double B = -4.966146004652837e2;
		public static final double C = 0.999989932158991;
		public static final double D = 0.000024622369163;

		public D96xyToD48xy() {
			super(CRS.D96_TM, CRS.D48_GK);
		}

		/**
		 * E
		 */
		@Override
		public double x(double sourceX, double sourceY) {
			return A + C * sourceX - D * sourceY;
		}

		@Override
		public double y(double sourceX, double sourceY) {
			return B + D * sourceX + C * sourceY;
		}

		@Override
		public Point point(Point src, Point ret) {
			//in case src == ret
			double x = src.x;
			double y = src.y;

			ret.x = x(x, y);
			ret.y = y(x, y);
			ret.z = src.z;
			return ret;
		}

		@Override
		public InvertibleTransform<TransverseMercator, TransverseMercator> inverse() {
			return D48_TO_D96;
		}

		@Override
		public Envelope envelope(Envelope src) {
			return transformWithCorners(this, src);
		}
	}

	/**
	 * Derived by fitting 4th order 2D polynomial to http://www.e-prostor.gov.si/fileadmin/ogs/tran_param/virtualne.txt
	 * Conversion of ETRS89 to WGS84 was done using {@link DatumConversion#ITRF2005_TO_ETRS89(double)} at epoch 2013
	 * 
	 * @author Miha
	 */
	public static class D48xyToWGS84LatLon extends CartesianToLatLon<TransverseMercator, Ellipsoidal> {

		private static final double[] LAT_FROM_GKXY = new double[]{
			+4.496804017752853e+01,+7.011469468255591e-07,-6.862998795371809e-13,-2.851713738614811e-20,+1.386781563841756e-26,//
			+8.943895925991360e-06,+2.202135727022642e-13,-2.168234061899595e-19,-9.124267441942990e-27,+6.492849910081913e-33,//
			-1.663011859281158e-14,+4.780352097984618e-20,-6.996613190158683e-26,+2.472772994234826e-32,-7.459150031887058e-40,//
			-3.101284903615750e-21,+5.060662275851127e-27,+3.114708272669540e-32,-7.450853919449900e-38,+4.327089885533503e-44,//
			+1.325826320368695e-26,-1.187984752923146e-31,+2.677601568405021e-37,-1.806937392297547e-43,+1.296317287525460e-50//
		};

		private static final double[] LON_FROM_GKXY = new double[]{
			+8.656841254772570e+00,+1.259770330719655e-05,+2.391291299730220e-13,-1.636262354119054e-19,+3.953179570942186e-27,//
			-9.887772284020300e-07,+1.925500913720868e-12,+1.567293783807248e-19,-1.138196342977083e-25,+8.443298722622820e-33,//
			-2.288850625426966e-13,+4.136655925453757e-19,+1.643092240626411e-25,-1.846027032661649e-31,+6.746212414878610e-38,//
			-4.634521189461498e-20,+9.917477092156630e-26,+7.394806839820694e-32,-2.455549424718957e-37,+1.892418403385552e-43,//
			-2.050588911166923e-27,+1.027282334493059e-31,-7.483177435751549e-37,+1.502038283747368e-42,-9.155916581447070e-49//
		};

		public D48xyToWGS84LatLon() {
			super(CRS.D48_GK, CRS.WGS84);
		}

		@Override
		public double lat(double x, double y) {
			return applyMatrixToXYPowers(x, y, LAT_FROM_GKXY, 4);
		}

		@Override
		public double lon(double x, double y) {
			return applyMatrixToXYPowers(x, y, LON_FROM_GKXY, 4);
		}

		@Override
		public Point point(Point src, Point ret) {
			//in case src == ret
			double x = src.x;
			double y = src.y;

			ret.x = lat(x, y);
			ret.y = lon(x, y);
			ret.z = src.z;
			return ret;
		}
	}

	/**
	 * Derived by fitting 4th order 2D polynomial to http://www.e-prostor.gov.si/fileadmin/ogs/tran_param/virtualne.txt
	 * Conversion of ETRS89 to WGS84 was done using {@link DatumConversion#ITRF2005_TO_ETRS89(double)} at epoch 2013
	 * 
	 * @author Miha
	 */
	public static class WGS84LatLonToD48xy extends LatLonToCartesian<Ellipsoidal, TransverseMercator> {

		private static final double[] GKX_FROM_WGSLL = new double[]{
			+5.558574216825061e+07, -5.005558158835486e+06, +1.656594223164156e+05, -2.427291817270441e+03, +1.334145632798474e+01,//
			-1.742429763693941e+07, +1.544766131169000e+06, -5.100997357670790e+04, +7.476017839360712e+02, -4.106985709999918e+00,//
			+1.968651888504608e+06, -1.732300607179949e+05, +5.712502176044365e+03, -8.367046113099660e+01, +4.592808799901139e-01,//
			-9.545046587461870e+04, +8.390483131895190e+03, -2.764250762659601e+02, +4.045169971799449e+00, -2.218584941239261e-02,//
			+1.696034619198408e+03, -1.489475408565393e+02, +4.902753489680811e+00, -7.168640195297947e-02, +3.928520666041312e-04//
		};

		private static final double[] GKY_FROM_WGSLL = new double[]{
			-1.121631038354185e+07, +7.052305018255422e+05, -2.090631383137378e+04, +3.259728707743927e+02, -1.900685359039224e+00,//
			+6.092000050978104e+05, -6.606587392091894e+04, +2.549135138171954e+03, -4.280765712667299e+01, +2.652871954366784e-01,//
			+5.692036461553487e+04, -3.813608535887606e+03, +8.811688248327610e+01, -7.428477786162892e-01, +1.085683025181651e-03,//
			-7.806776823154260e+03, +6.366741410030054e+02, -1.936391463725479e+01, +2.600311531265934e-01, -1.299268089677535e-03,//
			+2.114050417241862e+02, -1.780942999353327e+01, +5.615488530114062e-01, -7.852125386021251e-03, +4.107240865611158e-05//
		};

		public WGS84LatLonToD48xy() {
			super(CRS.WGS84, CRS.D48_GK);
		}

		@Override
		public double x(double lat, double lon) {
			return applyMatrixToXYPowers(lat, lon, GKX_FROM_WGSLL, 4);
		}

		@Override
		public double y(double lat, double lon) {
			return applyMatrixToXYPowers(lat, lon, GKY_FROM_WGSLL, 4);
		}

		@Override
		public Point point(Point src, Point ret) {
			double x = src.x;
			double y = src.y;

			ret.x = x(x, y);
			ret.y = y(x, y);
			ret.z = src.z;
			return ret;
		}
	}

	private static class CompositeTransform<A extends CRS, B extends CRS, C extends CRS> extends AbstractTransform<A, C> implements EnvelopeTransform, InvertibleTransform<A, C> {
		protected final Transform<A, B> t1;
		protected final Transform<B, C> t2;

		private CompositeTransform(Transform<A, B> t1, Transform<B, C> t2) {
			super(t1.getSource(), t2.getTarget());
			this.t1 = t1;
			this.t2 = t2;
		}

		@Override
		public Point point(Point src, Point ret) {
			ret = t1.point(src, ret);
			ret = t2.point(ret, ret);
			ret.setCrsId(getTarget().getDefaultIdentifier());
			return ret;
		}


		@Override
		public InvertibleTransform<C, A> inverse() {
			return (InvertibleTransform<C, A>)compose(((InvertibleTransform<B, C>)t2).inverse(),
				((InvertibleTransform<A, B>)t1).inverse());
		}

		@Override
		public Envelope envelope(Envelope src) {
			return ((EnvelopeTransform)t2).envelope(((EnvelopeTransform)t1).envelope(src));
		}

		@Override
		public String getName() {
			return t1.getName() + " (+) " + t2.getName();
		}
	}

	private static final class CompositeToCartesian<A extends CRS, B extends CRS, C extends CartesianCRS> extends CompositeTransform<A, B, C> implements ToCartesian<A, C> {
		private CompositeToCartesian(Transform<A, B> t1, ToCartesian<B, C> t2) {
			super(t1, t2);
		}

		@Override
		public double x(double src0, double src1) {
			return point(new Point(src0, src1), new Point()).x;
		}

		@Override
		public double y(double src0, double src1) {
			return point(new Point(src0, src1), new Point()).y;
		}
	}

	private static final class CompositeToLatLon<A extends CRS, B extends CRS, C extends LatLonCRS> extends CompositeTransform<A, B, C> implements ToLatLon<A, C> {
		private CompositeToLatLon(Transform<A, B> t1, ToLatLon<B, C> t2) {
			super(t1, t2);
		}

		@Override
		public double lat(double x, double y) {
			return point(new Point(x, y), new Point()).x;
		}

		@Override
		public double lon(double x, double y) {
			return point(new Point(x, y), new Point()).y;
		}
	}
}
