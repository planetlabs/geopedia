package com.sinergise.java.util.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sinergise.common.util.io.IOUtil;

public class IOUtilJava extends IOUtil {
	public static final int DEFAULT_BUFFSER_SIZE = 4096;

	private IOUtilJava() {
		super();
	}

	public static final void copyStream(InputStream src, OutputStream tgt) throws IOException {
		copyStream(src, tgt, DEFAULT_BUFFSER_SIZE);
	}


	public static final void copyStream(InputStream src, OutputStream tgt, int buffSize) throws IOException {
		byte[] buff = new byte[buffSize];
		copyStream(src, tgt, buff);
	}

	
	public static final void copyStream(InputStream src, OutputStream tgt, byte[] buffer) throws IOException {
		int numRead = 0;
		while ((numRead = src.read(buffer)) >= 0) {
			tgt.write(buffer, 0, numRead);
		}
	}

	public static String readASCIIToString(InputStream str) throws IOException {
		return new String(readFully(str), "US-ASCII");
	}
	
	public static int readBlocking(InputStream s, byte[] out, int off, int len, int sleepMillis) throws IOException {
		int rem = len;
		int read = 0;
		while (rem > 0) {
			int cur = s.read(out, off+read, rem);
			if (cur < 0) { //EOF
				if (read == 0) return -1;
				break;
			}
			rem -= cur;
			read += cur;
			if (rem > 0 && sleepMillis > 0) {
				try {
					Thread.sleep(sleepMillis);
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
		return read;
	}

	public static byte[] readFully(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.max(DEFAULT_BUFFSER_SIZE, is.available()));
		try {
			copyStream(is, baos);
			byte[] origBuff = baos.getInternalBuffer();
			if (baos.size() == origBuff.length) {
				return origBuff;
			}
			return baos.toByteArray();
		} finally {
			IOUtil.close(baos);
		}
	}

	public static void readFully(DataInput is, byte[] b, int off, int len) throws IOException {
		is.readFully(b, off, len);
	}
	

	public static void readFully(DataInput in, short[] s, int off, int len) throws IOException {
		for (int i = 0; i < len; i++) {
			s[off+i] = in.readShort();			
		}
	}

	public static void readFully(DataInput in, int[] s, int off, int len) throws IOException {
		for (int i = 0; i < len; i++) {
			s[off+i] = in.readInt();			
		}
	}

	public static void readFully(DataInput in, float[] s, int off, int len) throws IOException {
		for (int i = 0; i < len; i++) {
			s[off+i] = in.readFloat();			
		}
	}

	public static void readFully(DataInput in, double[] s, int off, int len) throws IOException {
		for (int i = 0; i < len; i++) {
			s[off+i] = in.readDouble();			
		}
	}
	
	public static void readFully(InputStream stream, byte[] b, int off, int len) throws IOException {
        int n = 0;
        do {
        	int count = stream.read(b, off + n, len - n);
        	if (count < 0) {
        		throw new EOFException();
        	}
        	n += count;
        } while (n < len);
	}

	public static String readAsciiToString(InputStream is, int size) throws IOException {
		byte[] buf = new byte[size];
		readFully(is, buf, 0, size);
		return new String(buf, "US-ASCII");
	}

	public static void write(DataOutput out, byte[] data, int off, int size) throws IOException {
		out.write(data, off, size);
	}
	public static void write(DataOutput out, short[] data, int off, int size) throws IOException {
		for (int i = 0; i < size; i++) {
			out.writeShort(data[off + i]);
		}
	}
	public static void write(DataOutput out, int[] data, int off, int size) throws IOException {
		for (int i = 0; i < size; i++) {
			out.writeInt(data[off + i]);
		}
	}
	public static void write(DataOutput out, float[] data, int off, int size) throws IOException {
		for (int i = 0; i < size; i++) {
			out.writeFloat(data[off + i]);
		}
	}
	public static void write(DataOutput out, double[] data, int off, int size) throws IOException {
		for (int i = 0; i < size; i++) {
			out.writeDouble(data[off + i]);
		}
	}

	public static byte[][] readBytes2D(DataInput in, int sizeI, int sizeJ) throws IOException {
		byte[][] ret = new byte[sizeI][sizeJ];
		for (int i = 0; i < sizeI; i++) {
			readFully(in, ret[i], 0, sizeJ);
		}
		return ret;
	}
	
	public static short[][] readShorts2D(DataInput in, int sizeI, int sizeJ) throws IOException {
		short[][] ret = new short[sizeI][sizeJ];
		for (int i = 0; i < sizeI; i++) {
			readFully(in, ret[i], 0, sizeJ);
		}
		return ret;
	}

	public static int[][] readInts2D(DataInput in, int sizeI, int sizeJ) throws IOException {
		int[][] ret = new int[sizeI][sizeJ];
		for (int i = 0; i < sizeI; i++) {
			readFully(in, ret[i], 0, sizeJ);
		}
		return ret;
	}

	public static float[][] readFloats2D(DataInput in, int sizeI, int sizeJ) throws IOException {
		float[][] ret = new float[sizeI][sizeJ];
		for (int i = 0; i < sizeI; i++) {
			readFully(in, ret[i], 0, sizeJ);
		}
		return ret;
	}

	public static double[][] readDoubles2D(DataInput in, int sizeI, int sizeJ) throws IOException {
		double[][] ret = new double[sizeI][sizeJ];
		for (int i = 0; i < sizeI; i++) {
			readFully(in, ret[i], 0, sizeJ);
		}
		return ret;
	}

	/** Writes double value to the output stream in little-endian way */
	public static void writeDoubleLE(OutputStream stream, double value) throws IOException {
		writeLongLE(stream, Double.doubleToLongBits(value));
	}
	
	/** Writes float value to the output stream in little-endian way */
	public static void writeFloatLE(OutputStream stream, float value) throws IOException {
		writeIntLE(stream, Float.floatToIntBits(value));
	}
	
	/** Writes long value to the output stream in little-endian way */
	public static void writeLongLE(OutputStream stream, long value) throws IOException {
		writeIntLE(stream, (int) ((value >> 0) & 0xFFFFFFFF));
		writeIntLE(stream, (int) ((value >> 32) & 0xFFFFFFFF));
	}
	
	/** Writes int value to the output stream in little-endian way */
	public static void writeIntLE(OutputStream stream, int value) throws IOException {
		writeShortLE(stream, (value >> 0) & 0xFFFF);
		writeShortLE(stream, (value >> 16) & 0xFFFF);
	}
	
	/** Writes short value to the output stream in little-endian way */
	public static void writeShortLE(OutputStream stream, int value) throws IOException {
		stream.write((value >> 0) & 0xFF);
		stream.write((value >> 8) & 0xFF);
	}
}
