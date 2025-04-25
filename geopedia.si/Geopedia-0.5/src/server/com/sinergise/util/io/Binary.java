package com.sinergise.util.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Binary
{
	public static char getChar(byte[] mybuff, int offset)
	{
		return (char) (((mybuff[offset++] & 255) << 8) | ((mybuff[offset] & 255)));
	}

	public static char getCharLE(byte[] mybuff, int offset)
	{
		return (char) (((mybuff[offset++] & 255)) | ((mybuff[offset] & 255) << 8));
	}

	public static double getDouble(byte[] mybuff, int offset)
	{
		// inlined
		// return Double.longBitsToDouble(getLong(mybuff, offset));

		return Double
		    .longBitsToDouble(((((long) (((mybuff[offset++] & 255) << 24) | ((mybuff[offset++] & 255) << 16)
		        | ((mybuff[offset++] & 255) << 8) | (mybuff[offset++] & 255))) << 32) | 0xFFFFFFFFL & (((mybuff[offset++] & 255) << 24)
		        | ((mybuff[offset++] & 255) << 16) | ((mybuff[offset++] & 255) << 8) | (mybuff[offset] & 255))));
	}

	public static double getDoubleLE(byte[] mybuff, int offset)
	{
		// inlined
		// return Double.longBitsToDouble(getLongLE(mybuff, offset));

		return Double
		    .longBitsToDouble(((((mybuff[offset++] & 255)) | ((mybuff[offset++] & 255) << 8)
		        | ((mybuff[offset++] & 255) << 16) | ((mybuff[offset++] & 255) << 24)) & 0xFFFFFFFFL | ((long) (((mybuff[offset++] & 255))
		        | ((mybuff[offset++] & 255) << 8) | ((mybuff[offset++] & 255) << 16) | ((mybuff[offset] & 255) << 24)) << 32)));
	}

	public static float getFloat(byte[] mybuff, int offset)
	{
		// inlined
		// return Float.intBitsToFloat(getInt(mybuff, offset));

		return Float.intBitsToFloat((((mybuff[offset++] & 255) << 24) | ((mybuff[offset++] & 255) << 16)
		    | ((mybuff[offset++] & 255) << 8) | ((mybuff[offset] & 255))));
	}

	public static float getFloatLE(byte[] mybuff, int offset)
	{
		// inlined
		// return Float.intBitsToFloat(getIntLE(mybuff, offset));

		return Float.intBitsToFloat((((mybuff[offset++] & 255)) | ((mybuff[offset++] & 255) << 8)
		    | ((mybuff[offset++] & 255) << 16) | ((mybuff[offset] & 255) << 24)));
	}

	public static int getInt(byte[] mybuff, int offset)
	{
		return ((mybuff[offset++] & 255) << 24) | ((mybuff[offset++] & 255) << 16) | ((mybuff[offset++] & 255) << 8)
		    | ((mybuff[offset] & 255));
	}

	public static int getIntLE(byte[] mybuff, int offset)
	{
		return ((mybuff[offset++] & 255)) | ((mybuff[offset++] & 255) << 8) | ((mybuff[offset++] & 255) << 16)
		    | ((mybuff[offset] & 255) << 24);
	}

	public static long getLong(byte[] mybuff, int offset)
	{
		// this weirdo expression is about 40% faster than a straightforward implementation
		// where each byte is used as (mybuff[offset++] & 255L)<<shift
		return (((long) (((mybuff[offset++] & 255) << 24) | ((mybuff[offset++] & 255) << 16)
		    | ((mybuff[offset++] & 255) << 8) | (mybuff[offset++] & 255))) << 32)
		    | 0xFFFFFFFFL
		    & (((mybuff[offset++] & 255) << 24) | ((mybuff[offset++] & 255) << 16) | ((mybuff[offset++] & 255) << 8) | (mybuff[offset] & 255));
	}

	public static long getLongLE(byte[] mybuff, int offset)
	{
		return (((mybuff[offset++] & 255)) | ((mybuff[offset++] & 255) << 8) | ((mybuff[offset++] & 255) << 16) | ((mybuff[offset++] & 255) << 24))
		    & 0xFFFFFFFFL
		    | ((long) (((mybuff[offset++] & 255)) | ((mybuff[offset++] & 255) << 8) | ((mybuff[offset++] & 255) << 16) | ((mybuff[offset] & 255) << 24)) << 32);
	}

	public static short getShort(byte[] mybuff, int offset)
	{
		return (short) (((mybuff[offset++] & 255) << 8) | ((mybuff[offset] & 255)));
	}

	public static short getShortLE(byte[] mybuff, int offset)
	{
		return (short) (((mybuff[offset++] & 255)) | ((mybuff[offset] & 255) << 8));
	}

	public static short getUByte(byte[] mybuff, int offset)
	{
		return (short) (mybuff[offset] & 255);
	}

	public static long getUInt(byte[] mybuff, int offset)
	{
		return ((((mybuff[offset++]) << 24) & 0xff000000L) | (((mybuff[offset++] & 255) << 16)
		    | ((mybuff[offset++] & 255) << 8) | ((mybuff[offset] & 255))));
	}

	public static long getUIntLE(byte[] mybuff, int offset)
	{
		return ((((mybuff[offset++] & 255)) | ((mybuff[offset++] & 255) << 8) | ((mybuff[offset++] & 255) << 16)) | (((mybuff[offset]) << 24) & 0xff000000L));
	}

	public static int getUShort(byte[] mybuff, int offset)
	{
		return ((mybuff[offset++] & 255) << 8) | (mybuff[offset] & 255);
	}

	public static int getUShortLE(byte[] mybuff, int offset)
	{
		return ((mybuff[offset++] & 255)) | ((mybuff[offset] & 255) << 8);
	}

	public static void putByte(byte[] buff, int offset, byte value)
	{
		buff[offset] = value;
	}

	public static void putChar(byte[] buff, int offset, char value)
	{
		buff[offset++] = (byte) (value >>> 8);
		buff[offset] = (byte) (value);
	}

	public static void putShort(byte[] buff, int offset, short value)
	{
		buff[offset++] = (byte) (value >>> 8);
		buff[offset] = (byte) (value);
	}

	public static void putInt(byte[] buff, int offset, int value)
	{
		buff[offset++] = (byte) (value >>> 24);
		buff[offset++] = (byte) (value >>> 16);
		buff[offset++] = (byte) (value >>> 8);
		buff[offset] = (byte) (value);
	}

	public static void putFloat(byte[] buff, int offset, float fvalue)
	{
		int value = Float.floatToRawIntBits(fvalue);

		buff[offset++] = (byte) (value >>> 24);
		buff[offset++] = (byte) (value >>> 16);
		buff[offset++] = (byte) (value >>> 8);
		buff[offset] = (byte) (value);
	}

	public static void putLong(byte[] buff, int offset, long lvalue)
	{
		int upper = (int) (lvalue >>> 32);
		int lower = (int) (lvalue);

		buff[offset++] = (byte) (upper >>> 24);
		buff[offset++] = (byte) (upper >>> 16);
		buff[offset++] = (byte) (upper >>> 8);
		buff[offset++] = (byte) (upper);
		buff[offset++] = (byte) (lower >>> 24);
		buff[offset++] = (byte) (lower >>> 16);
		buff[offset++] = (byte) (lower >>> 8);
		buff[offset] = (byte) (lower);
	}

	public static void putDouble(byte[] buff, int offset, double value)
	{
		long lvalue = Double.doubleToRawLongBits(value);

		int upper = (int) (lvalue >>> 32);
		int lower = (int) (lvalue);

		buff[offset++] = (byte) (upper >>> 24);
		buff[offset++] = (byte) (upper >>> 16);
		buff[offset++] = (byte) (upper >>> 8);
		buff[offset++] = (byte) (upper);
		buff[offset++] = (byte) (lower >>> 24);
		buff[offset++] = (byte) (lower >>> 16);
		buff[offset++] = (byte) (lower >>> 8);
		buff[offset] = (byte) (lower);
	}

	public static void putCharLE(byte[] buff, int offset, char value)
	{
		buff[offset++] = (byte) (value);
		buff[offset] = (byte) (value >>> 8);
	}

	public static void putShortLE(byte[] buff, int offset, short value)
	{
		buff[offset++] = (byte) (value);
		buff[offset] = (byte) (value >>> 8);
	}

	public static void putIntLE(byte[] buff, int offset, int value)
	{
		buff[offset++] = (byte) (value);
		buff[offset++] = (byte) (value >>> 8);
		buff[offset++] = (byte) (value >>> 16);
		buff[offset] = (byte) (value >>> 24);
	}

	public static void putFloatLE(byte[] buff, int offset, float fvalue)
	{
		int value = Float.floatToRawIntBits(fvalue);

		buff[offset++] = (byte) (value);
		buff[offset++] = (byte) (value >>> 8);
		buff[offset++] = (byte) (value >>> 16);
		buff[offset] = (byte) (value >>> 24);
	}

	public static void putLongLE(byte[] buff, int offset, long lvalue)
	{
		int upper = (int) (lvalue >>> 32);
		int lower = (int) (lvalue);

		buff[offset++] = (byte) (lower);
		buff[offset++] = (byte) (lower >>> 8);
		buff[offset++] = (byte) (lower >>> 16);
		buff[offset++] = (byte) (lower >>> 24);
		buff[offset++] = (byte) (upper);
		buff[offset++] = (byte) (upper >>> 8);
		buff[offset++] = (byte) (upper >>> 16);
		buff[offset] = (byte) (upper >>> 24);
	}

	public static void putDoubleLE(byte[] buff, int offset, double value)
	{
		long lvalue = Double.doubleToRawLongBits(value);

		int upper = (int) (lvalue >>> 32);
		int lower = (int) (lvalue);

		buff[offset++] = (byte) (lower);
		buff[offset++] = (byte) (lower >>> 8);
		buff[offset++] = (byte) (lower >>> 16);
		buff[offset++] = (byte) (lower >>> 24);
		buff[offset++] = (byte) (upper);
		buff[offset++] = (byte) (upper >>> 8);
		buff[offset++] = (byte) (upper >>> 16);
		buff[offset] = (byte) (upper >>> 24);
	}

	public static void main(String[] args)
	{
		byte[] test = new byte[8];
		ByteBuffer bb = ByteBuffer.wrap(test);
		bb.order(ByteOrder.LITTLE_ENDIAN);

		double val = Math.random();

		System.out.println("Original: " + val);
		putDoubleLE(test, 0, val);
		System.out.println("After putDouble: " + bb.getDouble(0));
		System.out.println("After putDouble: " + getDoubleLE(test, 0) + " (read with getDouble)");

		bb.putDouble(0, val);
		System.out.println("After bb.put: " + bb.getDouble(0));
		System.out.println("After bb.put: " + getDoubleLE(test, 0) + " (read with getDouble)");
	}
}
