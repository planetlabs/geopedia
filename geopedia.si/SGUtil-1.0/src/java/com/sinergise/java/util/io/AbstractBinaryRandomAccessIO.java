package com.sinergise.java.util.io;

import java.io.IOException;


public abstract class AbstractBinaryRandomAccessIO extends AbstractBinaryInput implements BinaryRandomAccessIO {
	@Override
	public abstract void seek(long pos) throws IOException;

	private final byte[] w(final int len) throws IOException {
		write(buff, 0, len);
		return buff;
	}

	@Override
	public void write(final byte[] bytes) throws IOException {
		write(bytes, 0, bytes.length);
	}

	@Override
	public void write(int b) throws IOException {
		writeByte(b);
	}

	@Override
	public void writeByte(final int val) throws IOException {
		buff[0] = (byte)val;
		w(1);
	}

	@Override
	public void writeChar(final int val) throws IOException {
		BinaryUtilJava.putChar(buff, 0, (char)val);
		w(2);
	}

	@Override
	public void writeCharLE(final char val) throws IOException {
		BinaryUtilJava.putCharLE(buff, 0, val);
		w(2);
	}

	@Override
	public void writeDouble(final double val) throws IOException {
		BinaryUtilJava.putDouble(buff, val);
		w(8);
	}

	@Override
	public void writeDoubleLE(final double val) throws IOException {
		BinaryUtilJava.putDoubleLE(buff, val);
		w(8);
	}

	@Override
	public void writeFloat(final float val) throws IOException {
		BinaryUtilJava.putFloat(buff, val);
		w(4);
	}

	@Override
	public void writeFloatLE(final float val) throws IOException {
		BinaryUtilJava.putFloatLE(buff, val);
		w(4);
	}

	@Override
	public void writeInt(final int val) throws IOException {
		BinaryUtilJava.putInt(buff, val);
		w(4);
	}

	@Override
	public void writeIntLE(final int val) throws IOException {
		BinaryUtilJava.putIntLE(buff, val);
		w(4);
	}

	@Override
	public void writeLong(final long val) throws IOException {
		BinaryUtilJava.putLong(buff, val);
		w(8);
	}

	@Override
	public void writeLongLE(final long val) throws IOException {
		BinaryUtilJava.putLongLE(buff, val);
		w(8);
	}

	@Override
	public void writeShort(final int val) throws IOException {
		BinaryUtilJava.putShort(buff, (short)val);
		w(2);
	}

	@Override
	public void writeShortLE(final short val) throws IOException {
		BinaryUtilJava.putShortLE(buff, val);
		w(2);
	}

	@Override
	public void writeStringLenUTF8(final String s) throws IOException {
		BinaryUtilJava.putStringLenUTF8(s, this);
	}

	@Override
	@Deprecated
	public void writeUTF(String str) throws IOException {
		BinaryUtilJava.putJavaModifiedUTF(str, this);
	}
	

	@Override
	public void writeBoolean(boolean v) throws IOException {
		BinaryUtilJava.putByte(buff, v ? (byte)0xFF : (byte)0x00);
		w(1);
	}

	@Override
	@Deprecated
	public void writeBytes(String s) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeChars(String s) throws IOException {
		final int len = s.length(); 
		for (int i = 0; i < len; i++) {
			writeChar(s.charAt(i));
		}
	}
	
	@Override
	public void writeASCII(String str) throws IOException {
		write(str.getBytes("US-ASCII"));
	}
	
	@Override
	public void writeDoubles(double[] vals, int off, int len, boolean bigEndian) throws IOException {
		throw new UnsupportedOperationException();
	}
}
