/*
 *
 */
package com.sinergise.common.util.geom;

import java.io.Serializable;

public class EnvelopeL implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private /*final*/ long minX;
	private /*final*/ long minY;
	private /*final*/ long maxX;
	private /*final*/ long maxY;
	
	public EnvelopeL() {
		this.minX = 1;
		this.minY = 1;
		this.maxX = 0;
		this.maxY = 0;
	}
	
	public EnvelopeL(final long xmin, final long ymin, final long xmax, final long ymax) {
		this.minX = Math.min(xmin, xmax);
		this.minY = Math.min(ymin, ymax);
		this.maxX = Math.max(xmin, xmax);
		this.maxY = Math.max(ymin, ymax);
	}
	
	public boolean equals(final long x1, final long y1, final long x2, final long y2) {
		return minX == x1 && minY == y1 && maxX == x2 && maxY == y2;
	}
	
	@Override
	public int hashCode() {
		final long PRIME = 31;
		long result = 1;
		result = PRIME * result + maxX;
		result = PRIME * result + maxY;
		result = PRIME * result + minX;
		result = PRIME * result + minY;
		return (int)result;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EnvelopeL)) {
			return false;
		}
		final EnvelopeL other = (EnvelopeL)obj;
		return equals(other.minX, other.minY, other.maxX, other.maxY);
	}
	
	public long getWidth() {
		return maxX - minX + 1;
	}
	
	public long getHeight() {
		return maxY - minY + 1;
	}
	
	public boolean isEmpty() {
		return (minX > maxX) || (minY > maxY);
	}
	
	public EnvelopeL intersectWithXY(final long x0, final long y0, final long x1, final long y1) {
		long outMinX = Math.max(minX, x0);
		long outMinY = Math.max(minY, y0);
		long outMaxX = Math.min(maxX, x1);
		long outMaxY = Math.min(maxY, y1);
		if (outMinX > outMaxX || outMinY > outMaxY) {
			return new EnvelopeL();
		}
		return new EnvelopeL(outMinX, outMinY, outMaxX, outMaxY);
	}
	
	@Override
	public String toString() {
		return "[" + minX + "," + minY + " | " + getWidth() + "x" + getHeight() + "]";
	}
	
	public boolean contains(final long x, final long y) {
		return x >= minX && x <= maxX && y >= minY && y <= maxY;
	}

	public EnvelopeL intersectWith(EnvelopeL b) {
		return intersectWithXY(b.minX, b.minY, b.maxX, b.maxY);
	}

	public EnvelopeL divide(int factor) {
		return new EnvelopeL(minX/factor, minY/factor, maxX/factor, maxY/factor);
	}

	public EnvelopeL translate(long deltaX, long deltaY) {
		return new EnvelopeL(minX + deltaX, minY + deltaY, maxX + deltaX, maxY + deltaY);
	}

	/**
	 * Expands the envelope non-uniformly in all four directions, using the provided offsets. 
	 */
	public EnvelopeL expand(long xLow, long yLow, long xHigh, long yHigh) {
		return new EnvelopeL(minX - xLow, minY - yLow, maxX + xHigh, maxY + yHigh);
	}

	public EnvelopeL setOrigin(long newMinx, long newMiny) {
		return translate(newMinx-minX, newMiny-minY);
	}

	public long getMinX() {
		return minX;
	}

	public long getMinY() {
		return minY;
	}

	public long getMaxX() {
		return maxX;
	}

	public long getMaxY() {
		return maxY;
	}

	public static EnvelopeL fromWH(long minX, long minY, int w, int h) {
		return new EnvelopeL(minX, minY, minX + w - 1, minY + h - 1);
	}

	public EnvelopeL expandToInclude(EnvelopeL other) {
		if (isEmpty()) {
			return other;
		}
		return new EnvelopeL(//
			Math.min(other.minX, minX), Math.min(other.minY, minY),//
			Math.max(other.maxX, maxX), Math.max(other.maxY, maxY));
	}

	public long xFromIndex(int indexInsideEnvelope) {
		return minX + indexInsideEnvelope;
	}

	public long yFromIndex(int indexInsideEnvelope) {
		return minY + indexInsideEnvelope;
	}

	public EnvelopeL expand(RectSideOffsetsI offsets) {
		return new EnvelopeL(minX - offsets.l(), minY - offsets.b(), maxX + offsets.r(), maxY+offsets.t());
	}
}
