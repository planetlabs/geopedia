/*
 *
 */
package com.sinergise.common.util.geom;

import java.io.Serializable;

import com.sinergise.common.util.string.HasCanonicalStringRepresentation;
import com.sinergise.common.util.string.StringUtil;

public class PointI implements Serializable, HasCanonicalStringRepresentation {
	private static final long serialVersionUID = 1L;
	public static final PointI valueOf(String posStr) {
		posStr = StringUtil.trimNullEmpty(posStr);
		if (posStr == null) return null;
		
		String[] posSplit = posStr.substring(1, posStr.length() - 1).split("\\w+");
		return new PointI(Integer.parseInt(posSplit[0]), Integer.parseInt(posSplit[1]));
	} 
	
	public int x = 0;
	public int y = 0;
	
	public PointI() {}
	
	public PointI(final int x, final int y) {
		setPos(x, y);
	}
	
	public PointI(final PointI source) {
		this(source.x, source.y);
	}
	
	public void setPos(final int x, final int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + x;
		result = PRIME * result + y;
		return result;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PointI)) {
			return false;
		}
		final PointI other = (PointI)obj;
		return equals(other.x, other.y);
	}
	
	public boolean equals(final int x2, final int y2) {
		return x == x2 && y == y2;
	}
	
	@Override
	public String toString() {
		return toCanonicalString();
	}
	
	public boolean isContainedIn(final int minX, final int minY, final int maxX, final int maxY) {
		if (x < minX) {
			return false;
		}
		if (x > maxX) {
			return false;
		}
		if (y < minY) {
			return false;
		}
		if (y > maxY) {
			return false;
		}
		return true;
	}
	
	public double distanceSq(final PointI p) {
		final double dx = p.x - x;
		final double dy = p.y - y;
		return dx * dx + dy * dy;
	}
	
	@Override
	public String toCanonicalString() {
		return "(" + x + " " + y + ")";
	}

	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}

	public static PointI create(int ptX, int ptY) {
		return new PointI(ptX, ptY);
	}
}
