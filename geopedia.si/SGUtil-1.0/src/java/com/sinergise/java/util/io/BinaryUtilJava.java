package com.sinergise.java.util.io;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class BinaryUtilJava {
	private static final ThreadLocal<CharsetDecoder> utfDecoder = new ThreadLocal<CharsetDecoder>() {
		@Override
		protected CharsetDecoder initialValue() {
			return Charset.forName("UTF-8").newDecoder();
		}
	};
	
	private static final ThreadLocal<CharsetEncoder> utfEncoder = new ThreadLocal<CharsetEncoder>() {
		@Override
		protected CharsetEncoder initialValue() {
			return Charset.forName("UTF-8").newEncoder();
		}
	};
	
	public static char getChar(final byte[] mybuff) {
		return (char)(((mybuff[0] & 255) << 8) | ((mybuff[1] & 255)));
	}

	public static char getChar(final byte[] mybuff, int offset) {
		return (char)(((mybuff[offset++] & 255) << 8) | ((mybuff[offset] & 255)));
	}

	public static char getCharLE(final byte[] mybuff) {
		return (char)(((mybuff[0] & 255)) | ((mybuff[1] & 255) << 8));
	}

	public static char getCharLE(final byte[] mybuff, int offset) {
		return (char)(((mybuff[offset++] & 255)) | ((mybuff[offset] & 255) << 8));
	}

	public static double getDouble(final byte[] mybuff) {
		// inlined
		// return Double.longBitsToDouble(getLong(mybuff, offset));

		return Double.longBitsToDouble(((((long)(((mybuff[0] & 255) << 24) | ((mybuff[1] & 255) << 16)
			| ((mybuff[2] & 255) << 8) | (mybuff[3] & 255))) << 32) | 0xFFFFFFFFL & (((mybuff[4] & 255) << 24)
			| ((mybuff[5] & 255) << 16) | ((mybuff[6] & 255) << 8) | (mybuff[7] & 255))));
	}

	public static double getDouble(final byte[] mybuff, int offset) {
		// inlined
		// return Double.longBitsToDouble(getLong(mybuff, offset));

		return Double.longBitsToDouble(((((long)(((mybuff[offset++] & 255) << 24) | ((mybuff[offset++] & 255) << 16)
			| ((mybuff[offset++] & 255) << 8) | (mybuff[offset++] & 255))) << 32) | 0xFFFFFFFFL & (((mybuff[offset++] & 255) << 24)
			| ((mybuff[offset++] & 255) << 16) | ((mybuff[offset++] & 255) << 8) | (mybuff[offset] & 255))));
	}

	public static double getDoubleLE(final byte[] mybuff) {
		// inlined return Double.longBitsToDouble(getLongLE(mybuff, offset));

		return Double.longBitsToDouble(((((mybuff[0] & 255)) | ((mybuff[1] & 255) << 8) | ((mybuff[2] & 255) << 16) | ((mybuff[3] & 255) << 24)) & 0xFFFFFFFFL | ((long)(((mybuff[4] & 255))
			| ((mybuff[5] & 255) << 8) | ((mybuff[6] & 255) << 16) | ((mybuff[7] & 255) << 24)) << 32)));
	}

	public static double getDoubleLE(final byte[] mybuff, int offset) {
		// inlined
		// return Double.longBitsToDouble(getLongLE(mybuff, offset));

		return Double.longBitsToDouble(((((mybuff[offset++] & 255)) | ((mybuff[offset++] & 255) << 8)
			| ((mybuff[offset++] & 255) << 16) | ((mybuff[offset++] & 255) << 24)) & 0xFFFFFFFFL | ((long)(((mybuff[offset++] & 255))
			| ((mybuff[offset++] & 255) << 8) | ((mybuff[offset++] & 255) << 16) | ((mybuff[offset] & 255) << 24)) << 32)));
	}

	public static float getFloat(final byte[] mybuff) {
		// inlined
		// return Float.intBitsToFloat(getInt(mybuff, offset));

		return Float.intBitsToFloat((((mybuff[0] & 255) << 24) | ((mybuff[1] & 255) << 16) | ((mybuff[2] & 255) << 8) | ((mybuff[3] & 255))));
	}

	public static float getFloat(final byte[] mybuff, int offset) {
		// inlined
		// return Float.intBitsToFloat(getInt(mybuff, offset));

		return Float.intBitsToFloat((((mybuff[offset++] & 255) << 24) | ((mybuff[offset++] & 255) << 16)
			| ((mybuff[offset++] & 255) << 8) | ((mybuff[offset] & 255))));
	}

	public static float getFloatLE(final byte[] mybuff) {
		// inlined
		// return Float.intBitsToFloat(getIntLE(mybuff, offset));

		return Float.intBitsToFloat((((mybuff[0] & 255)) | ((mybuff[1] & 255) << 8) | ((mybuff[2] & 255) << 16) | ((mybuff[3] & 255) << 24)));
	}

	public static float getFloatLE(final byte[] mybuff, int offset) {
		// inlined
		// return Float.intBitsToFloat(getIntLE(mybuff, offset));

		return Float.intBitsToFloat((((mybuff[offset++] & 255)) | ((mybuff[offset++] & 255) << 8)
			| ((mybuff[offset++] & 255) << 16) | ((mybuff[offset] & 255) << 24)));
	}

	public static int getInt(final byte[] mybuff) {
		return ((mybuff[0] & 255) << 24) | ((mybuff[1] & 255) << 16) | ((mybuff[2] & 255) << 8) | ((mybuff[3] & 255));
	}

	public static int getInt(final byte[] mybuff, int offset) {
		return ((mybuff[offset++] & 255) << 24) | ((mybuff[offset++] & 255) << 16) | ((mybuff[offset++] & 255) << 8)
			| ((mybuff[offset] & 255));
	}

	public static int getIntLE(final byte[] mybuff) {
		return ((mybuff[0] & 255)) | ((mybuff[1] & 255) << 8) | ((mybuff[2] & 255) << 16) | ((mybuff[3] & 255) << 24);
	}

	public static int getIntLE(final byte[] mybuff, int offset) {
		return ((mybuff[offset++] & 255)) | ((mybuff[offset++] & 255) << 8) | ((mybuff[offset++] & 255) << 16)
			| ((mybuff[offset] & 255) << 24);
	}

	public static long getLong(final byte[] mybuff) {
		// this weirdo expression is about 40% faster than a straightforward implementation
		// where each byte is used as (mybuff[offset] & 255L)<<shift
		return (((long)(((mybuff[0] & 255) << 24) | ((mybuff[1] & 255) << 16) | ((mybuff[2] & 255) << 8) | (mybuff[3] & 255))) << 32)
			| 0xFFFFFFFFL
			& (((mybuff[4] & 255) << 24) | ((mybuff[5] & 255) << 16) | ((mybuff[6] & 255) << 8) | (mybuff[7] & 255));
	}

	public static long getLong(final byte[] mybuff, int offset) {
		// this weirdo expression is about 40% faster than a straightforward implementation
		// where each byte is used as (mybuff[offset++] & 255L)<<shift
		return (((long)(((mybuff[offset++] & 255) << 24) | ((mybuff[offset++] & 255) << 16)
			| ((mybuff[offset++] & 255) << 8) | (mybuff[offset++] & 255))) << 32)
			| 0xFFFFFFFFL
			& (((mybuff[offset++] & 255) << 24) | ((mybuff[offset++] & 255) << 16) | ((mybuff[offset++] & 255) << 8) | (mybuff[offset] & 255));
	}

	public static long getLongLE(final byte[] mybuff) {
		return (((mybuff[0] & 255)) | ((mybuff[1] & 255) << 8) | ((mybuff[2] & 255) << 16) | ((mybuff[3] & 255) << 24))
			& 0xFFFFFFFFL
			| ((long)(((mybuff[4] & 255)) | ((mybuff[5] & 255) << 8) | ((mybuff[6] & 255) << 16) | ((mybuff[7] & 255) << 24)) << 32);
	}

	public static long getLongLE(final byte[] mybuff, int offset) {
		return (((mybuff[offset++] & 255)) | ((mybuff[offset++] & 255) << 8) | ((mybuff[offset++] & 255) << 16) | ((mybuff[offset++] & 255) << 24))
			& 0xFFFFFFFFL
			| ((long)(((mybuff[offset++] & 255)) | ((mybuff[offset++] & 255) << 8) | ((mybuff[offset++] & 255) << 16) | ((mybuff[offset] & 255) << 24)) << 32);
	}

	public static short getShort(final byte[] mybuff) {
		return (short)(((mybuff[0] & 255) << 8) | ((mybuff[1] & 255)));
	}

	public static short getShort(final byte[] mybuff, int offset) {
		return (short)(((mybuff[offset++] & 255) << 8) | ((mybuff[offset] & 255)));
	}

	public static short getShortLE(final byte[] mybuff) {
		return (short)(((mybuff[0] & 255)) | ((mybuff[1] & 255) << 8));
	}

	public static short getShortLE(final byte[] mybuff, int offset) {
		return (short)(((mybuff[offset++] & 255)) | ((mybuff[offset] & 255) << 8));
	}

	public static short getUByte(final byte[] mybuff) {
		return (short)(mybuff[0] & 255);
	}

	public static short getUByte(final byte[] mybuff, final int offset) {
		return (short)(mybuff[offset] & 255);
	}

	public static long getUInt(final byte[] mybuff) {
		return ((((mybuff[0]) << 24) & 0xff000000L) | (((mybuff[1] & 255) << 16) | ((mybuff[2] & 255) << 8) | ((mybuff[3] & 255))));
	}

	public static long getUInt(final byte[] mybuff, int offset) {
		return ((((mybuff[offset++]) << 24) & 0xff000000L) | (((mybuff[offset++] & 255) << 16)
			| ((mybuff[offset++] & 255) << 8) | ((mybuff[offset] & 255))));
	}

	public static long getUIntLE(final byte[] mybuff) {
		return ((((mybuff[0] & 255)) | ((mybuff[1] & 255) << 8) | ((mybuff[2] & 255) << 16)) | (((mybuff[3]) << 24) & 0xff000000L));
	}

	public static long getUIntLE(final byte[] mybuff, int offset) {
		return ((((mybuff[offset++] & 255)) | ((mybuff[offset++] & 255) << 8) | ((mybuff[offset++] & 255) << 16)) | (((mybuff[offset]) << 24) & 0xff000000L));
	}

	public static int getUShort(final byte[] mybuff) {
		return ((mybuff[0] & 255) << 8) | (mybuff[1] & 255);
	}

	public static int getUShort(final byte[] mybuff, int offset) {
		return ((mybuff[offset++] & 255) << 8) | (mybuff[offset] & 255);
	}

	public static int getUShortLE(final byte[] mybuff) {
		return ((mybuff[0] & 255)) | ((mybuff[1] & 255) << 8);
	}

	public static int getUShortLE(final byte[] mybuff, int offset) {
		return ((mybuff[offset++] & 255)) | ((mybuff[offset] & 255) << 8);
	}

	public static void putByte(final byte[] buff, final int value) {
		buff[0] = (byte)value;
	}

	public static void putByte(final byte[] buff, final int offset, final int value) {
		buff[offset] = (byte)value;
	}

	public static void putChar(final byte[] buff, final int value) {
		buff[0] = (byte)(value >>> 8);
		buff[1] = (byte)(value);
	}

	public static void putChar(final byte[] buff, int offset, final int value) {
		buff[offset++] = (byte)(value >>> 8);
		buff[offset] = (byte)(value);
	}

	public static void putCharLE(final byte[] buff, final int value) {
		buff[0] = (byte)(value);
		buff[1] = (byte)(value >>> 8);
	}

	public static void putCharLE(final byte[] buff, int offset, final int value) {
		buff[offset++] = (byte)(value);
		buff[offset] = (byte)(value >>> 8);
	}

	public static void putDouble(final byte[] buff, final double value) {
		final long lvalue = Double.doubleToRawLongBits(value);

		final int upper = (int)(lvalue >>> 32);
		final int lower = (int)(lvalue);

		buff[0] = (byte)(upper >>> 24);
		buff[1] = (byte)(upper >>> 16);
		buff[2] = (byte)(upper >>> 8);
		buff[3] = (byte)(upper);
		buff[4] = (byte)(lower >>> 24);
		buff[5] = (byte)(lower >>> 16);
		buff[6] = (byte)(lower >>> 8);
		buff[7] = (byte)(lower);
	}

	public static void putDouble(final byte[] buff, int offset, final double value) {
		final long lvalue = Double.doubleToRawLongBits(value);

		final int upper = (int)(lvalue >>> 32);
		final int lower = (int)(lvalue);

		buff[offset++] = (byte)(upper >>> 24);
		buff[offset++] = (byte)(upper >>> 16);
		buff[offset++] = (byte)(upper >>> 8);
		buff[offset++] = (byte)(upper);
		buff[offset++] = (byte)(lower >>> 24);
		buff[offset++] = (byte)(lower >>> 16);
		buff[offset++] = (byte)(lower >>> 8);
		buff[offset] = (byte)(lower);
	}

	public static void putDoubleLE(final byte[] buff, final double value) {
		final long lvalue = Double.doubleToRawLongBits(value);

		final int upper = (int)(lvalue >>> 32);
		final int lower = (int)(lvalue);

		buff[0] = (byte)(lower);
		buff[1] = (byte)(lower >>> 8);
		buff[2] = (byte)(lower >>> 16);
		buff[3] = (byte)(lower >>> 24);
		buff[4] = (byte)(upper);
		buff[5] = (byte)(upper >>> 8);
		buff[6] = (byte)(upper >>> 16);
		buff[7] = (byte)(upper >>> 24);
	}

	public static void putDoubleLE(final byte[] buff, int offset, final double value) {
		final long lvalue = Double.doubleToRawLongBits(value);

		final int upper = (int)(lvalue >>> 32);
		final int lower = (int)(lvalue);

		buff[offset++] = (byte)(lower);
		buff[offset++] = (byte)(lower >>> 8);
		buff[offset++] = (byte)(lower >>> 16);
		buff[offset++] = (byte)(lower >>> 24);
		buff[offset++] = (byte)(upper);
		buff[offset++] = (byte)(upper >>> 8);
		buff[offset++] = (byte)(upper >>> 16);
		buff[offset] = (byte)(upper >>> 24);
	}

	public static void putFloat(final byte[] buff, final float fvalue) {
		final int value = Float.floatToRawIntBits(fvalue);

		buff[0] = (byte)(value >>> 24);
		buff[1] = (byte)(value >>> 16);
		buff[2] = (byte)(value >>> 8);
		buff[3] = (byte)(value);
	}

	public static void putFloat(final byte[] buff, int offset, final float fvalue) {
		final int value = Float.floatToRawIntBits(fvalue);

		buff[offset++] = (byte)(value >>> 24);
		buff[offset++] = (byte)(value >>> 16);
		buff[offset++] = (byte)(value >>> 8);
		buff[offset] = (byte)(value);
	}

	public static void putFloatLE(final byte[] buff, final float fvalue) {
		final int value = Float.floatToRawIntBits(fvalue);

		buff[0] = (byte)(value);
		buff[1] = (byte)(value >>> 8);
		buff[2] = (byte)(value >>> 16);
		buff[3] = (byte)(value >>> 24);
	}

	public static void putFloatLE(final byte[] buff, int offset, final float fvalue) {
		final int value = Float.floatToRawIntBits(fvalue);

		buff[offset++] = (byte)(value);
		buff[offset++] = (byte)(value >>> 8);
		buff[offset++] = (byte)(value >>> 16);
		buff[offset] = (byte)(value >>> 24);
	}

	public static void putInt(final byte[] buff, final int value) {
		buff[0] = (byte)(value >>> 24);
		buff[1] = (byte)(value >>> 16);
		buff[2] = (byte)(value >>> 8);
		buff[3] = (byte)(value);
	}

	public static void putInt(final byte[] buff, int offset, final int value) {
		buff[offset++] = (byte)(value >>> 24);
		buff[offset++] = (byte)(value >>> 16);
		buff[offset++] = (byte)(value >>> 8);
		buff[offset] = (byte)(value);
	}

	public static void putIntLE(final byte[] buff, final int value) {
		buff[0] = (byte)(value);
		buff[1] = (byte)(value >>> 8);
		buff[2] = (byte)(value >>> 16);
		buff[3] = (byte)(value >>> 24);
	}

	public static void putIntLE(final byte[] buff, int offset, final int value) {
		buff[offset++] = (byte)(value);
		buff[offset++] = (byte)(value >>> 8);
		buff[offset++] = (byte)(value >>> 16);
		buff[offset] = (byte)(value >>> 24);
	}

	public static void putLong(final byte[] buff, int offset, final long lvalue) {
		final int upper = (int)(lvalue >>> 32);
		final int lower = (int)(lvalue);

		buff[offset++] = (byte)(upper >>> 24);
		buff[offset++] = (byte)(upper >>> 16);
		buff[offset++] = (byte)(upper >>> 8);
		buff[offset++] = (byte)(upper);
		buff[offset++] = (byte)(lower >>> 24);
		buff[offset++] = (byte)(lower >>> 16);
		buff[offset++] = (byte)(lower >>> 8);
		buff[offset] = (byte)(lower);
	}

	public static void putLong(final byte[] buff, final long lvalue) {
		final int upper = (int)(lvalue >>> 32);
		final int lower = (int)(lvalue);

		buff[0] = (byte)(upper >>> 24);
		buff[1] = (byte)(upper >>> 16);
		buff[2] = (byte)(upper >>> 8);
		buff[3] = (byte)(upper);
		buff[4] = (byte)(lower >>> 24);
		buff[5] = (byte)(lower >>> 16);
		buff[6] = (byte)(lower >>> 8);
		buff[7] = (byte)(lower);
	}

	public static void putLongLE(final byte[] buff, int offset, final long lvalue) {
		final int upper = (int)(lvalue >>> 32);
		final int lower = (int)(lvalue);

		buff[offset++] = (byte)(lower);
		buff[offset++] = (byte)(lower >>> 8);
		buff[offset++] = (byte)(lower >>> 16);
		buff[offset++] = (byte)(lower >>> 24);
		buff[offset++] = (byte)(upper);
		buff[offset++] = (byte)(upper >>> 8);
		buff[offset++] = (byte)(upper >>> 16);
		buff[offset] = (byte)(upper >>> 24);
	}

	public static void putLongLE(final byte[] buff, final long lvalue) {
		final int upper = (int)(lvalue >>> 32);
		final int lower = (int)(lvalue);

		buff[0] = (byte)(lower);
		buff[1] = (byte)(lower >>> 8);
		buff[2] = (byte)(lower >>> 16);
		buff[3] = (byte)(lower >>> 24);
		buff[4] = (byte)(upper);
		buff[5] = (byte)(upper >>> 8);
		buff[6] = (byte)(upper >>> 16);
		buff[7] = (byte)(upper >>> 24);
	}

	public static void putShort(final byte[] buff, int offset, final int value) {
		buff[offset++] = (byte)(value >>> 8);
		buff[offset] = (byte)(value);
	}

	public static void putShort(final byte[] buff, final int value) {
		buff[0] = (byte)(value >>> 8);
		buff[1] = (byte)(value);
	}

	public static void putShortLE(final byte[] buff, int offset, final int value) {
		buff[offset++] = (byte)(value);
		buff[offset] = (byte)(value >>> 8);
	}

	public static void putShortLE(final byte[] buff, final int value) {
		buff[0] = (byte)(value);
		buff[1] = (byte)(value >>> 8);
	}

	public static String getJavaModifiedUTF(DataInput in) throws IOException {
		return DataInputStream.readUTF(in);
	}
	
	public static void putJavaModifiedUTF(String s, DataOutput out) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeUTF(s);
			out.write(baos.getInternalBuffer(), 0, baos.size());
		} finally {
			dos.close();
		}
	}

	public static void putStringLenUTF8(String s, DataOutput out) throws IOException {
		if (s == null) {
			out.writeInt(-1);
			return;
		}

		if (s.length() == 0) {
			out.writeInt(0);
			return;
		}
		final ByteBuffer data = utfEncoder.get().encode(CharBuffer.wrap(s));
		out.writeInt(data.limit());
		out.write(data.array(), data.position(), data.limit());
	}

	public static String getStringLenUTF8(DataInput in) throws IOException {
		final int len = in.readInt();
		if (len == -1) return null;
		if (len == 0) return "";

		final byte[] data = new byte[len];
		in.readFully(data, 0, len);
		return utfDecoder.get().decode(ByteBuffer.wrap(data)).toString();
	}

	public static void putChars(String s, BinaryOutput out) throws IOException {
		final int len = s.length();
		for (int i = 0; i < len; i++) {
			out.writeChar(s.charAt(i));
		}
	}
	
	public static String getASCII(final DataInput in, final int len) throws IOException {
		return getASCII(in, len, new byte[len]);
	}
	public static String getASCII(final DataInput in, final int len, byte[] tempBuff) throws IOException {
		in.readFully(tempBuff, 0, len);
		return new String(tempBuff, 0, len, "US-ASCII");
	}
	
	public static void putASCII(final DataOutput out, final String str) throws IOException {
		out.write(str.getBytes("US-ASCII"));
	}

	public static void putShortArray(final short[] src, final byte[] out, int outOff) {
		for (short s : src) {
			out[outOff++] = (byte)(s >>> 8);
			out[outOff++] = (byte)(s);
		}
	}

	public static void putShortArray(final int[] src, final byte[] out, int outOff) {
		for (int s : src) {
			out[outOff++] = (byte)(s >>> 8);
			out[outOff++] = (byte)(s);
		}
	}

	
	public static void getShortArray(byte[] src, int srcOff, short[] tgt, int tgtOff, int numShorts) {
		for (int i = 0; i < numShorts; i++) {
			tgt[tgtOff++] = (short)(((src[srcOff] & 255) << 8) | (src[srcOff + 1] & 255));
			srcOff += 2;
		}
	}

	public static void getShortArray(byte[] src, int srcOff, int[] tgt, int tgtOff, int numShorts) {
		for (int i = 0; i < numShorts; i++) {
			tgt[tgtOff++] = (((src[srcOff] & 255) << 8) | (src[srcOff + 1] & 255));
			srcOff += 2;
		}
	}
}
