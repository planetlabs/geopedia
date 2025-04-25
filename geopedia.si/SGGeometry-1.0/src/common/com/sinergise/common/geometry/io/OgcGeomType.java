package com.sinergise.common.geometry.io;

public class OgcGeomType {
	public static final int COORD_BIT_HASZ = 1;
	public static final int COORD_BIT_HASM = 2;
	
	public static final OgcGeomType fromWkbHeaderInt32(int wkbInt) {
		final int coordBits = getCoordBits(wkbInt);
		return new OgcGeomType(OgcShapeType.fromWkbHeader(wkbInt), hasZ(coordBits), hasM(coordBits));
	}

	public static boolean hasZ(final int coordBits) {
		return isMaskSet(coordBits, COORD_BIT_HASZ);
	}

	public static boolean hasM(final int coordBits) {
		return isMaskSet(coordBits, COORD_BIT_HASM);
	}

	public static boolean isMaskSet(final int coordBits, int mask) {
		return (coordBits & mask) == mask;
	}
	
	public static final int getCoordBits(int wkbInt) {
		return wkbInt/1000;
	}

	/**
	 * @param tag; will be trimmed; any case is fine
	 * @return
	 */
	public static final OgcGeomType fromWktTag(String tag) {
		tag = tag.trim().toUpperCase();
		if (tag.indexOf(' ') >= 0) {
			final String[] parts = tag.split("\\s");
			return new OgcGeomType(OgcShapeType.fromWktTag(parts[0], true), parts[1].startsWith("Z"), parts[1].endsWith("M"));
		}
		return new OgcGeomType(OgcShapeType.fromWktTag(tag, true), false, false);
	}
	
	private final OgcShapeType shapeType;
	private final boolean hasZ;
	private final boolean hasM;


	public OgcGeomType(OgcShapeType shapeType, boolean z, boolean m) {
		this.shapeType = shapeType;
		this.hasZ = z;
		this.hasM = m;
	}
	
	public int getWkbHeaderInt32() {
		return toWkbHeaderInt32(shapeType, hasZ, hasM);
	}
	
	public static int toWkbHeaderInt32(OgcShapeType shapeType, boolean hasZ, boolean hasM) {
		return shapeType.getWkbValue() + (hasZ ? 1000 : 0) + (hasM ? 2000 : 0);
	}
	
	public OgcShapeType getShapeType() {
		return shapeType;
	}
	
	/**
	 * OGC's Is3D() method
	 * @return
	 */
	public boolean hasZ() {
		return hasZ;
	}

	/**
	 * OGC's IsMeasured() method
	 * @return
	 */
	public boolean hasM() {
		return hasM;
	}
	
	public boolean instanceOf(OgcShapeType otherShp, boolean otherZ, boolean otherM) {
		return otherShp.isSuperOf(this.shapeType) && hasZ == otherZ && hasM == otherM;
	}

	@Override
	public String toString() {
		return getWktTag();
	}
	
	public String getWktTag() {
		StringBuilder sb = new StringBuilder(getShapeType().getWktTag());
		if (hasZ) {
			if (hasM) {
				sb.append(" ZM");
			} else {
				sb.append(" Z");
			}
		} else if (hasM) {
			sb.append(" M");
		}
		return sb.toString();
	}
	
	public int getCoordinateDimension() {
		return getSpatialDimension() + (hasM ? 1 : 0);
	}
	
	public int getSpatialDimension() {
		return hasZ ? 3 : 2;
	}
}