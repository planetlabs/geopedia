/*
 *
 */
package com.sinergise.common.util.geom;

import java.util.Iterator;

import com.sinergise.common.util.lang.Predicate;
import com.sinergise.common.util.math.Interval;

public class EnvelopeI implements Iterable<PointI> {
	public static class EnvelopeIterator implements Iterator<PointI> {
		private final int len;
		private final int w;
		private final EnvelopeI env;
		private int nextI=0;
		
		public EnvelopeIterator(EnvelopeI e) {
			env = e;
			len = e.getArea();
			w = e.getWidth();
		}
		
		@Override
		public boolean hasNext() {
			return nextI < len;
		}
		
		@Override
		public PointI next() {
			int x = env.minX + nextI % w; 
			int y = env.minY + nextI / w;
			nextI++;
			return PointI.create(x, y);
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public static final EnvelopeI EMPTY = new EnvelopeI();	
	
	public static EnvelopeI withSize(int minX, int minY, int width, int height) {
		if (width == 0 || height == 0) {
			return EMPTY;
		}
		return new EnvelopeI(minX, minY, minX + width - 1, minY + height -1);
	}

	public static EnvelopeI withPoints(int x1, int y1, int x2, int y2) {
		if (x1 <= x2) {
			if (y1 <= y2) {
				return new EnvelopeI(x1, y1, x2, y2);
			}
			return new EnvelopeI(x1, y2, x2, y1);
		}
		if (y1 <= y2) {
			return new EnvelopeI(x2, y1, x1, y2);
		}
		return new EnvelopeI(x2, y2, x1, y1);
	}

	public static EnvelopeI create(int minx, int miny, int maxx, int maxy) {
		if (minx > maxx || miny > maxy) {
			return EMPTY;
		}
		return new EnvelopeI(minx, miny, maxx, maxy);
	}

	public static final class Builder {
		private boolean copyOnMod = false;
		private EnvelopeI curEnv;

		public Builder() {
			curEnv = new EnvelopeI();
		}
		
		public Builder(EnvelopeI start) {
			curEnv = start;
			copyOnMod = true;
		}
		
		private void beforeModification(boolean preserveData) {
			if (copyOnMod) {
				copyOnMod = false;
				curEnv = preserveData ? new EnvelopeI(curEnv) : new EnvelopeI();
			}
		}

		public void addToX(int deltaMinX, int deltaMaxX) {
			beforeModification(true);
			curEnv.minX += deltaMinX;
			curEnv.maxX += deltaMaxX;
		}

		public void addToY(int deltaMinY, int deltaMaxY) {
			beforeModification(true);
			curEnv.minY += deltaMinY;
			curEnv.maxY += deltaMaxY;
		}
		
		public int minX() {
			return curEnv.minX;
		}
		public int maxX() {
			return curEnv.maxX;
		}
		public int minY() {
			return curEnv.minY;
		}
		public int maxY() {
			return curEnv.maxY;
		}

		public EnvelopeI getEnvelope() {
			if (isEmpty()) {
				return EnvelopeI.EMPTY;
			}
			copyOnMod = true;
			return curEnv;
		}
		
		public boolean isEmpty() {
			return curEnv.isEmpty();
		}

		public void expandToInclude(int x, int y) {
			if (isEmpty()) {
				setMBR(x,y,x,y);
			} else {
				setMBR(//
					Math.min(curEnv.minX, x),
					Math.min(curEnv.minY, y),
					Math.max(curEnv.maxX, x),
					Math.max(curEnv.maxY, y)
				);
			}
		}
		
		public void setMBR(int minX, int minY, int maxX, int maxY) {
			beforeModification(false);
			curEnv.minX = minX;
			curEnv.minY = minY;
			curEnv.maxX = maxX;
			curEnv.maxY = maxY;
		}

		public void intersectWith(int minX, int minY, int maxX, int maxY) {
			beforeModification(true);
			if (curEnv.minX < minX) curEnv.minX = minX;
			if (curEnv.minY < minY) curEnv.minY = minY;
			if (curEnv.maxX > maxX) curEnv.maxX = maxX;
			if (curEnv.maxY > maxY) curEnv.maxY = maxY;
		}

		public void expand(int expand) {
			addToX(-expand, expand);
			addToY(-expand, expand);
		}
	}

	int minX;
	int minY;
	int maxX;
	int maxY;
	
	protected EnvelopeI() {
		// Serialization
		minX = Integer.MAX_VALUE;
		maxX = Integer.MIN_VALUE;
		minY = Integer.MAX_VALUE;
		maxY = Integer.MIN_VALUE;
	}
	
	public EnvelopeI(final int xmin, final int ymin) {
		this(xmin, ymin, xmin, ymin);
	}
	
	public EnvelopeI(final int xmin, final int ymin, final int xmax, final int ymax) {
		assert xmin <= xmax : "xmin should be smaller or equal xmax";
		assert ymin <= ymax : "ymin should be smaller or equal ymax";
		
		this.minX = xmin;
		this.minY = ymin;
		this.maxX = xmax;
		this.maxY = ymax;
	}
	
	public EnvelopeI(final EnvelopeI e) {
		this(e.minX, e.minY, e.maxX, e.maxY);
	}
	
	public boolean isEmpty() {
		return (minX > maxX) || (minY > maxY);
	}

	public int minX() {
		return minX;
	}

	public int minY() {
		return minY;
	}

	public int maxX() {
		return maxX;
	}

	public int maxY() {
		return maxY;
	}

	public int getWidth() {
		return maxX - minX + 1;
	}

	public int getHeight() {
		return maxY - minY + 1;
	}

	public boolean equals(final int x1, final int y1, final int x2, final int y2) {
		return minX == x1 && minY == y1 && maxX == x2 && maxY == y2;
	}
	
	public boolean sizeEquals(final int w, final int h) {
		return (getWidth() == w) && (getHeight() == h);
	}

	public boolean contains(final int x, final int y) {
		return x >= minX && x <= maxX && y >= minY && y <= maxY;
	}
	
	public boolean containsDouble(double x, double y) {
		return x >= minX && x <= maxX + 1 && y >= minY && y <= maxY + 1;
	}

	public boolean contains(EnvelopeI b) {
		return contains(b.minX, b.minY) && contains(b.maxX, b.maxY);
	}

	public boolean intersects(EnvelopeI b) {
		return intersects(b.minX, b.minY, b.maxX, b.maxY);
	}
	
	public boolean intersects(int x0, int y0, int x1, int y1) {
		if (isEmpty()) {
			return false;
		}
		if (x0 > x1) {
			int tmp = x1;
			x1 = x0;
			x0 = tmp;
		}
		if (y0 > y1) {
			int tmp = y1;
			y1 = y0;
			y0 = tmp;
		}
		return (x1 >= minX && y1 >= minY && x0 <= maxX && y0 <= maxY);
	}
	
	public boolean intersectsDouble(double x0, double y0, double x1, double y1) {
		if (isEmpty()) {
			return false;
		}
		if (x0 > x1) {
			double tmp = x1;
			x1 = x0;
			x0 = tmp;
		}
		if (y0 > y1) {
			double tmp = y1;
			y1 = y0;
			y0 = tmp;
		}
		return (x1 >= minX && y1 >= minY && x0 <= maxX+1 && y0 <= maxY+1);
	}

	public Interval<Integer> getIntervalX() {
		return Interval.closed(minX, maxX);
	}
	
	public Interval<Integer> getIntervalY() {
		return Interval.closed(minY, maxY);
	}

	public int getArea() {
		return getWidth() * getHeight();
	}

	public EnvelopeI addToBounds(int dMinX, int dMinY, int dMaxX, int dMaxY) {
		if (isEmpty()) {
			return EMPTY;
		}
		return new EnvelopeI(minX + dMinX, minY + dMinY, maxX + dMaxX, maxY + dMaxY);
	}

	public EnvelopeI intersection(int xmin, int ymin, int xmax, int ymax) {
		if (!intersects(xmin, ymin, xmax, ymax)) {
			return EMPTY;
		}
		return new EnvelopeI(//
			Math.max(this.minX, xmin),//
			Math.max(this.minY, ymin),//
			Math.min(this.maxX, xmax),//
			Math.min(this.maxY, ymax));
	}

	public EnvelopeI intersection(EnvelopeI b) {
		if (!intersects(b)) {
			return EMPTY;
		}
		if (contains(b)) {
			return b;
		}
		if (b.contains(this)) {
			return this;
		}
		return intersection(b.minX, b.minY, b.maxX, b.maxY);
	}

	/**
	 * @return Envelope of this object in real space, aligned with corners of the integral cells (e.g. interval [1, 2] is mapped to [1.0, 3.0])  
	 */
	public Envelope toDoubleEnvelope() {
		return new Envelope(minX, minY, maxX+1, maxY+1);
	}
	
	@Override
	public String toString() {
		return "[" + minX + "," + minY + " | " + getWidth() + "x" + getHeight() + "]";
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + maxX;
		result = PRIME * result + maxY;
		result = PRIME * result + minX;
		result = PRIME * result + minY;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EnvelopeI)) {
			return false;
		}
		final EnvelopeI other = (EnvelopeI)obj;
		return equals(other.minX, other.minY, other.maxX, other.maxY);
	}

	public boolean isTrueForAll(Predicate<PointI> predicate) {
		for (int i = minX; i <= maxX; i++) {
			for (int j = minY; j <= maxY; j++) {
				if (!predicate.eval(new PointI(i, j))) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public Iterator<PointI> iterator() {
		return new EnvelopeIterator(this);
	}

	public boolean contains(PointI pos) {
		return contains(pos.x, pos.y);
	}

	public DimI getSize() {
		return DimI.create(getWidth(), getHeight());
	}

	@SuppressWarnings("boxing")
	public static EnvelopeI createClosed(Interval<Integer> xInterval, Interval<Integer> yInterval) {
		if (xInterval.isEmpty() || yInterval.isEmpty()) {
			return EnvelopeI.EMPTY;
		}
		return create(xInterval.getMinValue(), yInterval.getMinValue(), xInterval.getMaxValue(), yInterval.getMaxValue());
	}
}
