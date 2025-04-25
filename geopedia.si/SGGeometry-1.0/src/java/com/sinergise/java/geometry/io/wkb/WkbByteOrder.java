package com.sinergise.java.geometry.io.wkb;

public enum WkbByteOrder{BIG_ENDIAN, LITTLE_ENDIAN;
	public byte getWkbValue() {
		return (byte)ordinal();
	}

	public static WkbByteOrder valueOf(byte byteVal) {
		return byteVal > 0 ? LITTLE_ENDIAN : BIG_ENDIAN;
	}
}