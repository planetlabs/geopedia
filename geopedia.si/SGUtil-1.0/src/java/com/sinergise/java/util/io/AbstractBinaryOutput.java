package com.sinergise.java.util.io;

import java.io.IOException;

public abstract class AbstractBinaryOutput implements BinaryOutput {
	public static abstract class AbstractBinaryRandomAccessOutput extends AbstractBinaryOutput implements BinaryRandomAccessOutput {
		
	} 
	
	protected final byte[] buff = new byte[8];

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
	public void writeBoolean(final boolean v) throws IOException {
		writeByte(v ? 0xFF : 0x00);
	}

	@Override
	@Deprecated
	public void writeBytes(String s) throws IOException {
		throw new UnsupportedOperationException();
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
	public void writeChars(final String s) throws IOException {
		BinaryUtilJava.putChars(s, this);
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
		writeByte(val >>> 8);
		writeByte(val);
	}

	@Override
	public void writeShortLE(final short val) throws IOException {
		writeByte(val >>> 8);
		writeByte(val);
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
	public void writeASCII(String str) throws IOException {
		byte[] tmpBuff = new byte[str.length()];
		for (int i = 0; i < tmpBuff.length; i++) {
			tmpBuff[i] = (byte)str.charAt(i);
		}
		write(tmpBuff);
	}
	
	@Override
	public void writeDoubles(double[] vals, int off, int len, boolean bigEndian) throws IOException {
		throw new UnsupportedOperationException();
	}
}
