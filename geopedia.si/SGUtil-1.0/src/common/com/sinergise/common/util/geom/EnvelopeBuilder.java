package com.sinergise.common.util.geom;

import static com.sinergise.common.util.Util.safeEquals;

import java.util.Arrays;

import com.sinergise.common.util.crs.CrsIdentifier;




@SuppressWarnings("deprecation")
public class EnvelopeBuilder {
	boolean copyOnMod = false;
	Envelope curEnv = new Envelope();
	
	public EnvelopeBuilder() {
		this((CrsIdentifier)null);
	}
	
	public EnvelopeBuilder(CrsIdentifier crsId) {
		clear();
		setCrsId(crsId);
	}
	
	public EnvelopeBuilder(Envelope initial) {
		setMBR(initial);
		setCrsId(initial.getCrsId());
	}

	public boolean isEmpty() {
		return curEnv.isEmpty();
	}
	
	public double getWidth() {
		return curEnv.getWidth();
	}
	
	public double getHeight() {
		return curEnv.getHeight();
	}
	
	public void expandToInclude(HasCoordinate p) {
		if (p == null) {
			return;
		}
		expandToInclude(p.x(), p.y());
	}
	
	public void setDiagonalPoints(HasCoordinate p1, HasCoordinate p2) {
		setPoints(p1.x(), p1.y(), p2.x(), p2.y());
	}
	

	public void setWidth(double w) {
		beforeModification(true);
		
		final double newMinX = curEnv.getCenterY() - w/2;
		curEnv.minX = newMinX;
		curEnv.maxX = newMinX + w;
	}
	
	public void setHeight(double h) {
		beforeModification(true);

		final double newMinY = curEnv.getCenterY() - h/2;
		curEnv.minY = newMinY;
		curEnv.maxY = newMinY + h;
	}
	
	public void translate(double dx, double dy) {
		if (isEmpty() || (dx == 0 && dy == 0)) {
			return;
		}
		beforeModification(true);

		curEnv.minX += dx;
		curEnv.maxX += dx;
		curEnv.minY += dy;
		curEnv.maxY += dy;
	}
	
	public void expandToInclude(Envelope other) {
		if (other == null || other.isEmpty()) {
			return;
		}
		
		if (isEmpty()) {
			setMBR(other);
		} else {
			beforeModification(true);
			if (other.minX < curEnv.minX) curEnv.minX = other.minX;
			if (other.minY < curEnv.minY) curEnv.minY = other.minY;
			if (other.maxX > curEnv.maxX) curEnv.maxX = other.maxX;
			if (other.maxY > curEnv.maxY) curEnv.maxY = other.maxY;
		}
	}
	
	public void expandToInclude(double x, double y) {
		beforeModification(true);

		if (isEmpty()) {
			curEnv.minX = x;
			curEnv.maxX = x;
			curEnv.minY = y;
			curEnv.maxY = y;
		} else {
			if (x < curEnv.minX) {
				curEnv.minX = x;
			} else if (x > curEnv.maxX) {
				curEnv.maxX = x;
			} if (y < curEnv.minY) {
				curEnv.minY = y;
			} else if (y > curEnv.maxY) {
				curEnv.maxY = y;
			}
		}
	}
	
	private void beforeModification(boolean preserveData) {
		if (copyOnMod) {
			copyOnMod = false;
			curEnv = preserveData ? new Envelope(curEnv) : new Envelope();
		}
	}

	public void expandFor(double value) {
		expandFor(value, value);
	}
	
	public void expandFor(double dw, double dh) {
		if (isEmpty()) {
			return;
		}
		beforeModification(true);
		curEnv.minX -= dw;
		curEnv.maxX += dw;

		curEnv.minY -= dh;
		curEnv.maxY += dh;
	}
	
	public void expandForSizeRatio(double ratio) {
		double dw = 0.5*getWidth()*ratio;
		double dh = 0.5*getHeight()*ratio;
		expandFor(dw, dh);
	}
	
	/**
	 * Adjustst bounding box for new aspect ratio.
	 * 
	 * Expands width or height of the bounding box so that the new envelope holds 
	 * the previous extent and its aspect ratio is as desired.
	 * 
	 * @param aspectRatio new aspect ratio (dx/dy)
	 * @return
	 */
	public void expandForAspectRatio(double aspectRatio) {
		double dx = getWidth();
		double dy = getHeight();
		double currentRatio = dx/dy;
		
		if (currentRatio < aspectRatio) {
			setWidth(dy * aspectRatio);
		} else {
			setHeight(dx / aspectRatio);
		}
	}
	
	public void shrinkForAspectRatio(double aspectRatio) {
		double dx = getWidth();
		double dy = getHeight();
		double currentRatio = dx/dy;
		
		if (currentRatio < aspectRatio) {
			setHeight(dx * aspectRatio);
		} else {
			setWidth(dy / aspectRatio);
		}
	}
	
	public void clear() {
		beforeModification(false);
		curEnv.minX = 1;
		curEnv.minY = 1;
		curEnv.maxX = 0;
		curEnv.maxY = 0;
	}
	
	public void setMBR(double minX, double minY, double maxX, double maxY) {
		beforeModification(false);
		curEnv.minX = minX;
		curEnv.minY = minY;
		curEnv.maxX = maxX;
		curEnv.maxY = maxY;
	}
	
	public void setMBR(Envelope other) {
		setMBR(other.minX, other.minY, other.maxX, other.maxY);
	}
	
	private void setCrsId(CrsIdentifier crs) {
		curEnv.crsId = crs;
	}
	
	public void setPoints(double x1, double y1, double x2, double y2) {
		beforeModification(false);
		if (x1 < x2) {
			curEnv.minX = x1;
			curEnv.maxX = x2;
		} else {
			curEnv.minX = x2;
			curEnv.maxX = x1;
		}
		if (y1 < y2) {
			curEnv.minY = y1;
			curEnv.maxY = y2;
		} else {
			curEnv.minY = y2;
			curEnv.maxY = y1;
		}
	}

	public Envelope getEnvelope() {
		copyOnMod = true;
		return curEnv;
	}
	
	@Override
	public String toString() {
		return curEnv.toString();
	}

	public void expandToInclude(Iterable<? extends HasCoordinate> coordinates) {
		for (HasCoordinate c : coordinates) {
			expandToInclude(c);
		}
	}
	
	public void expandToIncludeEnvelopes(Iterable<? extends HasEnvelope> envelopes) {
		for (HasEnvelope e : envelopes) {
			if (e == null) {
				continue;
			}
			Envelope mbr = e.getEnvelope();
			if (mbr == null || mbr.isEmpty()) {
				continue;
			}
			if (!safeEquals(curEnv.crsId, mbr.crsId)) {
				throw new IllegalArgumentException("Cannot join envelopes with different CRSs.");
			}
			expandToInclude(mbr);
		}
	}

	public double getMinX() {
		return curEnv.getMinX();
	}

	public double getMinY() {
		return curEnv.getMinY();
	}

	public double getMaxX() {
		return curEnv.getMaxX();
	}

	public double getMaxY() {
		return curEnv.getMaxY();
	}

	public boolean isPointOnEdge(HasCoordinate pt) {
		return curEnv.isPointOnEdge(pt);
	}

	public void intersectWith(Envelope e) {
		if (!safeEquals(curEnv.crsId, e.crsId)) {
			throw new IllegalArgumentException("Cannot intersect envelopes with different CRSs.");
		}
		double outMinX = Math.max(curEnv.minX, e.minX);
		double outMinY = Math.max(curEnv.minY, e.minY);
		double outMaxX = Math.min(curEnv.maxX, e.maxX);
		double outMaxY = Math.min(curEnv.maxY, e.maxY);
		if (outMinX > outMaxX || outMinY > outMaxY) {
			clear();
		} else {
			curEnv.minX = outMinX;
			curEnv.minY = outMinY;
			curEnv.maxX = outMaxX;
			curEnv.maxY = outMaxY;
		}
	}

	public boolean isNonTrivial() {
		return curEnv.isNonTrivial();
	}

	public static Envelope createUnionForEnvelopes(Iterable<? extends HasEnvelope> envelopes) {
		EnvelopeBuilder b = new EnvelopeBuilder();
		b.expandToIncludeEnvelopes(envelopes);
		return b.getEnvelope();
	}
	
	public static Envelope createUnionForEnvelopes(HasEnvelope ... envelopes) {
		return createUnionForEnvelopes(Arrays.asList(envelopes));
	}

	public static Envelope createUnionForCoordinates(Iterable<? extends HasCoordinate> coords) {
		EnvelopeBuilder b = new EnvelopeBuilder();
		b.expandToInclude(coords);
		return b.getEnvelope();
	}

	public static Envelope createUnionForCoordinates(HasCoordinate ... coords) {
		return createUnionForCoordinates(Arrays.asList(coords));
	}
	
	public boolean contains(Envelope itemEnv) {
		return curEnv.contains(itemEnv);
	}

	public boolean contains(double x, double y) {
		return curEnv.contains(x, y);
	}
}
