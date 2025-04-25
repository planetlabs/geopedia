package com.sinergise.common.util.geom;

import static com.sinergise.common.util.Util.safeEquals;

import com.sinergise.common.util.CheckUtil;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.crs.HasCrsIdentifier;
import com.sinergise.common.util.lang.Predicate;
import com.sinergise.common.util.math.Interval;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.common.util.state.gwt.StateGWTOriginator;
import com.sinergise.common.util.string.HasCanonicalStringRepresentation;
import com.sinergise.common.util.string.StringUtil;


public class Envelope implements StateGWTOriginator, HasCanonicalStringRepresentation, HasCrsIdentifier, HasEnvelope {
	private static final long serialVersionUID = 1L;
	
	public static final String PROP_MINX = "minX";
	public static final String PROP_MINY = "minY";
	public static final String PROP_MAXX = "maxX";
	public static final String PROP_MAXY = "maxY";
	public static final String PROP_CRS_CODE = "crs";
	
	/**
	 * @deprecated use getters instead
	 */
	@Deprecated
	public double minX;

	/**
	 * @deprecated use getters instead
	 */
	@Deprecated
	public double maxX;

	/**
	 * @deprecated use getters instead
	 */
	@Deprecated
	public double minY;
	
	/**
	 * @deprecated use getters instead
	 */
	@Deprecated
	public double maxY;
	
	CrsIdentifier crsId = null;
	
	public Envelope() {
		minX = minY = 1;
		maxX = maxY = 0;
	}
	
	public Envelope(StateGWT st) {
		//do not reorder, could be empty envelope
		minX = st.getDouble(PROP_MINX, Double.NaN);
		minY = st.getDouble(PROP_MINY, Double.NaN);
		maxX = st.getDouble(PROP_MAXX, Double.NaN);
		maxY = st.getDouble(PROP_MAXY, Double.NaN);
		
		String crsCode = st.getString(PROP_CRS_CODE, null);
		if (!StringUtil.isNullOrEmpty(crsCode)) {
			crsId = new CrsIdentifier(crsCode);
		}
	}
	
	/**
	 * @return true iff both width and height are greater than 0;
	 */
	public boolean isNonTrivial() {
		return minX < maxX && minY < maxY;
	}
	
	public boolean isLine() {
		return (minX == maxX && minY < maxY) || (minX < maxX && minY == maxY);
	}
	
	public boolean isEmpty() {
		return !(minX <= maxX && minY <= maxY);
	}
	
	public boolean isPoint() {
		return minX == maxX && (minY == maxY);
	}
	
	public Envelope(Envelope env) {
		crsId = env.crsId;
		minX = env.minX;
		minY = env.minY;
		maxX = env.maxX;
		maxY = env.maxY;
	}
	
	public Envelope(double x1, double y1, double x2, double y2) {
		this(x1, y1, x2, y2, null);
	}
	
	public Envelope(double x1, double y1, double x2, double y2, CrsIdentifier crs) {
		crsId = crs;
		
		if (x1 < x2) {
			minX = x1;
			maxX = x2;
		} else {
			minX = x2;
			maxX = x1;
		}
		if (y1 < y2) {
			minY = y1;
			maxY = y2;
		} else {
			minY = y2;
			maxY = y1;
		}
	}
	
	@SuppressWarnings("boxing")
	public Envelope(Interval<Double> xInt, Interval<Double> yInt) {
		minX = xInt.getMinValue();
		maxX = xInt.getMaxValue();
		minY = yInt.getMinValue();
		maxY = yInt.getMaxValue();
	}
	
	@Override
	public CrsIdentifier getCrsId() {
		return crsId;
	}

	public boolean contains(Envelope other) {
		CheckUtil.checkNotNull(other, "other");
		checkSrid(other);
		
		return other.minX >= minX && other.maxX <= maxX && other.minY >= minY && other.maxY <= maxY;
	}

	public void checkSrid(Envelope other) {
		//TODO: Remove ifs when SGMap is refactored to better handle crsIds of layers
		if (crsId != null && other.crsId != null) {
			CheckUtil.checkArgument(safeEquals(crsId, other.crsId), "Cannot compare envelopes with different CRSs.");
		}
	}
	
	public boolean contains(double eMinX, double eMinY, double eMaxX, double eMaxY) {
		return eMinX >= this.minX && eMaxX <= this.maxX && eMinY >= this.minY && eMaxY <= this.maxY;
	}

	public boolean contains(double x, double y) {
		return minX <= x && x <= maxX && minY <= y && y <= maxY;
	}
	
	public double getWidth() {
		return maxX - minX;
	}
	
	public double getHeight() {
		return maxY - minY;
	}
	
	public double[] asArray() {
		return new double[] { minX, minY, maxX, maxY };
	}
	
	public boolean intersects(Envelope mbr) {
		if (this == mbr) {
			return true;
		}
		if (isNullOrEmpty(mbr)) {
			return false;
		}
		checkSrid(mbr);
		return intersects(mbr.minX, mbr.minY, mbr.maxX, mbr.maxY);
	}
	

	public boolean intersects(double minX2, double minY2, double maxX2, double maxY2) {
		if (isEmpty()) { return false; }
		return (maxX2 > minX && maxY2 > minY && minX2 < maxX && minY2 < maxY);
	}
	
	public double getCenterX() {
		if (maxX < minX) return Double.NaN;
		return 0.5 * (maxX + minX);
	}
	
	public double getCenterY() {
		if (maxY < minY) return Double.NaN;
		return 0.5 * (maxY + minY);
	}
	
	public HasCoordinate topLeft() {
		return new Position2D(minX, maxY);
	}
	
	public HasCoordinate topRight() {
		return new Position2D(maxX, maxY);
	}
	
	public HasCoordinate bottomLeft() {
		return new Position2D(minX, minY);
	}
	
	public HasCoordinate bottomRight() {
		return new Position2D(maxX, minY);
	}
	
	public Envelope intersectWith(Envelope b) {
		checkSrid(b);
		
		double outMinX = Math.max(minX, b.minX);
		double outMinY = Math.max(minY, b.minY);
		double outMaxX = Math.min(maxX, b.maxX);
		double outMaxY = Math.min(maxY, b.maxY);
		if (outMinX > outMaxX || outMinY > outMaxY) {
			return Envelope.getEmpty();
		}
		return new Envelope(outMinX, outMinY, outMaxX, outMaxY);
	}
	
	@Override
	public StateGWT storeInternalState(StateGWT target) {
		if (target == null) target = new StateGWT();
		// else target.clear();
		target.putDouble(PROP_MINX, minX);
		target.putDouble(PROP_MINY, minY);
		target.putDouble(PROP_MAXX, maxX);
		target.putDouble(PROP_MAXY, maxY);
		if (crsId != null) {
			target.putString(PROP_CRS_CODE, crsId.getCode());
		}
		return target;
	}
	
	@Override
	public String toString() {
		return toCanonicalString();
	}
	
	/**
	 * WKT-like space-separated ordinates, bottom-left and top-right separated by comma:
	 * (minX minY, maxX maxY)
	 * <p>
	 * See <a href="http://postgis.refractions.net/documentation/manual-1.4/ST_Box2D.html">PostGIS ST_BOX2D</a>
	 * </p>
	 */
	@Override
	public String toCanonicalString() {
		return "(" + minX + " " + minY + "," + maxX + " " + maxY + ")";
	}
	
	public HasCoordinate getCenter() {
		return new Position2D(getCenterX(), getCenterY());
	}

	public double getAspectRatio() {
		return getWidth()/getHeight();
	}
	
	public double clampX(double x) {
		return minX <= maxX ? MathUtil.clamp(minX, x, maxX) : x;
	}

	public double clampY(double y) {
		return minY <= maxY ? MathUtil.clamp(minY, y, maxY) : y;
	}
	
	@Override
	public int hashCode() {
		if (isEmpty()) return 0;
		final int prime = 31;
		int result = 1;
		result = prime * result + MathUtil.hashCode(maxX);
		result = prime * result + MathUtil.hashCode(maxY);
		result = prime * result + MathUtil.hashCode(minX);
		result = prime * result + MathUtil.hashCode(minY);
		if (crsId != null) {
			result = prime * result + crsId.hashCode();
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Envelope)) return false;
		Envelope other = (Envelope) obj;
		if (isEmpty()) return other.isEmpty();
		if (minX != other.minX) return false;
		if (minY != other.minY) return false;
		if (maxX != other.maxX) return false;
		if (maxY != other.maxY) return false;
		if (!safeEquals(crsId, other.crsId)) return false;
		return true;
	}

	public double getArea() {
		return getHeight() * getWidth();
	}

	public boolean isPointOnEdge(HasCoordinate c) {
		final double x = c.x();
		final double y = c.y();
		if ((x == minX || x == maxX) && (minY <= y && y <= maxY)) return true;
		if ((y == minY || y == maxY) && (minX <= x && x <= maxX)) return true;
		return false;
	}

	public double getMinX() {
		return minX;
	}
	public double getMinY() {
		return minY;
	}
	public double getMaxX() {
		return maxX;
	}
	public double getMaxY() {
		return maxY;
	}
	
	/**
	 * Returns EnvelopeI containing the grid cells that intersect with this envelope (e.g. interval (1.8, 3.1) goes to [1, 4]).
	 * The returned envelope will always be larger than this envelope.
	 * Small threshold (1e-6) is applied to snap borders even if they are not precise (e.g. (1.9999999, 3.00000001) goes to [2, 3] instead of [1, 4]).
	 */
	public EnvelopeI roundOutside() {
		return new EnvelopeI((int)Math.floor(minX + 1e-6), (int)Math.floor(minY + 1e-6), (int)Math.ceil(maxX - 1e-6)-1, (int)Math.ceil(maxY - 1e-6)-1);
	}

	/**
	 * Returns Envelope containing the whole grid cells that intersect with this envelope (e.g. interval [1.8, 3.1] goes to [1, 4]).
	 * The returned envelope will always be larger than this envelope.
	 * Small threshold (1e-6) is applied to snap borders even if they are not precise (e.g. [1.9999999, 3.00000001] goes to [2, 3] instead of [1, 4]).
	 */
	public Envelope roundIntOutside() {
		return new Envelope(Math.floor(minX + 1e-6), Math.floor(minY + 1e-6), Math.ceil(maxX - 1e-6), Math.ceil(maxY - 1e-6));
	}

	
	/**
	 * Returns EnvelopeI containing the grid cells that are at least half covered by this envelope (e.g. interval (1.8, 3.1) goes to [2, 3]).
	 * This is consistent with snapping this envelope's border lines to grid cell boundaries.
	 */
	public EnvelopeI round() {
		return new EnvelopeI((int)Math.round(minX), (int)Math.round(minY), (int)Math.round(maxX)-1, (int)Math.round(maxY)-1);
	}

	public EnvelopeL roundLong() {
		return new EnvelopeL(Math.round(minX), Math.round(minY), Math.round(maxX)-1, Math.round(maxY)-1);
	}
	
	public EnvelopeL roundLongInside() {
		return new EnvelopeL((long)Math.ceil(minX - 1e-6), (long)Math.ceil(minY - 1e-6), (long)Math.floor(maxX + 1e-6)-1, (long)Math.floor(maxY + 1e-6)-1);
	}

	public Envelope translate(double dx, double dy) {
		return new Envelope(minX + dx, minY + dy, maxX + dx, maxY + dy);
	}

	/**
	 * Adjust bounding box for new aspect ratio.
	 * 
	 * Expands width or height of the bounding box so that the new envelope contains this envelope 
	 * and its aspect ratio is as desired.
	 * 
	 * @param aspectRatio new aspect ratio (dx/dy)
	 * @return a new Envelope object that contains this envelope and has the desired aspect ratio
	 */
	public Envelope expandedForAspectRatio(double aspectRatio) {
		final double dx = Math.abs(maxX - minX);
		final double dy = Math.abs(maxY - minY);
		final double currentRatio = dx/dy;
		
		if (currentRatio == aspectRatio) {
			return this;
		} else if (currentRatio < aspectRatio) {
			return Envelope.withCenter(getCenterX(), getCenterY(), dy * aspectRatio, dy);
		}
		return Envelope.withCenter(getCenterX(), getCenterY(), dy * aspectRatio, dy);
	}

	public Envelope expandedFor(double expX, double expY) {
		if (isEmpty() || (expX == 0 && expY == 0)) {
			return this;
		}
		return new Envelope(minX - expX, minY - expY, maxX + expX, maxY + expY);
	}

	/**
	 * @param ratio Relative amount to expand this envelope for (e.g. 0.1 will stretch the envelope by 10%, -0.1 will shrink the envelope by 10%)
	 */
	public Envelope expandedForSizeRatio(double ratio) {
		if (isEmpty() || ratio == 0) {
			return this;
		}
		double dw = 0.5*getWidth()*ratio;
		double dh = 0.5*getHeight()*ratio;
		return new Envelope(minX - dw, minY - dh, maxX + dw, maxY + dh);
	}

	public Envelope union(Envelope other) {
		if (isEmpty()) {
			return other;
		}
		if (isNullOrEmpty(other)) {
			return this;
		}
		checkSrid(other);
		return new Envelope(Math.min(minX, other.minX), Math.min(minY, other.minY), Math.max(maxX, other.maxX), Math.max(maxY, other.maxY));
	}

	public Envelope times(double d) {
		return new Envelope(minX * d, minY * d, maxX * d, maxY * d);
	}
	
	public Envelope divide(double d) {
		return new Envelope(minX / d, minY / d, maxX / d, maxY / d);
	}

	public Envelope setWidth(double w) {
		double newMinX = getCenterX() - 0.5*w;
		return new Envelope(newMinX, minY, newMinX + w, maxY);
	}

	public Envelope setHeight(double h) {
		double newMinY = getCenterY() - 0.5*h;
		return new Envelope(minX, newMinY, maxX, newMinY + h);
	}
	
	//-----------------------------//
	// Mutation methods start here //
	//-----------------------------//
	
	@Override
	@Deprecated
	public void loadInternalState(StateGWT st) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Envelope expanded by amount in each direction
	 * @param amount
	 * @return
	 */
	public Envelope expandedFor(double amount) {
		return expandedFor(amount, amount);
	}

	public boolean contains(HasCoordinate p) {
		return contains(p.x(), p.y());
	}

	@Override
	public Envelope getEnvelope() {
		return this;
	}

	public static Envelope valueOf(String envStr) {
		envStr = StringUtil.trimNullEmpty(envStr);
		if (envStr == null) {
			return null;
		}
		
		String[] posSplit = envStr.substring(1, envStr.length() - 1).split("[\\s,]+");
		
		Envelope env = new Envelope(
			Double.parseDouble(posSplit[0]),
			Double.parseDouble(posSplit[1]),
			Double.parseDouble(posSplit[2]),
			Double.parseDouble(posSplit[3])
		);
		return env;
	}

	public static Envelope create(HasCoordinate p1, HasCoordinate p2) {		
		return new Envelope(p1.x(), p1.y(), p2.x(), p2.y());
	}
	
	public static Envelope create(HasCoordinate p1, HasCoordinate p2, double expand) {
		double x1 = p1.x();
		double y1 = p1.y();
		double x2 = p2.x();
		double y2 = p2.y();
		if (x2 < x1) {
			double tmp = x2;
			x2 = x1;
			x1 = tmp;
		}
		if (y2 < y1) {
			double tmp = y2;
			y2 = y1;
			y1 = tmp;
		}
		return new Envelope(x1 - expand, y1 - expand, x2 + expand, y2 + expand, null);
	}

	public static Envelope forPoint(HasCoordinate pos) {
		return withCenter(pos.x(), pos.y(), 0, 0);
	}

	public static Envelope withCenter(double cX, double cY, double w, double h) {
		return withSize(cX - w/2, cY - h/2, w, h);
	}

	public static Envelope withSize(double minX, double minY, double w, double h) {
		return new Envelope(minX, minY, minX + w, minY + h);
	}

	public static boolean isNullOrEmpty(Envelope e) {
		return e == null || e.isEmpty();
	}

	public static Envelope getEmpty() {
		//TODO: Use constant when Envelope is made unmodifiable
		return new Envelope();
	}
	
	public static Envelope getEmpty(CrsIdentifier crsId) {
		Envelope ret = new Envelope();
		ret.crsId = crsId;
		return ret;
	}

	public static Envelope getInfinite() {
		return new Envelope(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
	
	public static Predicate<HasCoordinate> createPredicateForContainsPoint(final Envelope e) {
		return new Predicate<HasCoordinate>() {
			@Override
			public boolean eval(HasCoordinate value) {
				return e.contains(value);
			}
		};
	}

	public Envelope setCrsId(CrsIdentifier crsRef) {
		EnvelopeBuilder envelopeBuilder = new EnvelopeBuilder(crsRef);
		envelopeBuilder.expandToInclude(this);
		return envelopeBuilder.getEnvelope();
	}
}
