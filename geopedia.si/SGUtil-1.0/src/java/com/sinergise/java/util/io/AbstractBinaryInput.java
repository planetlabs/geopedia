package com.sinergise.java.util.io;

import java.io.EOFException;
import java.io.IOException;

public abstract class AbstractBinaryInput implements BinaryInput {

	public static abstract class AbstractBinaryRandomAccessInput extends AbstractBinaryInput implements BinaryRandomAccessInput {
		
	}

	protected final byte[] buff = new byte[8];

	private final byte[] r(final int len) throws IOException {
		readFully(buff, 0, len);
		return buff;
	}

	@Override
	public byte readByte() throws IOException {
		return r(1)[0];
	}

	@Override
	public char readChar() throws IOException {
		return BinaryUtilJava.getChar(r(2));
	}

	@Override
	public char readCharLE() throws IOException {
		return BinaryUtilJava.getCharLE(r(2));
	}

	@Override
	public double readDouble() throws IOException {
		return BinaryUtilJava.getDouble(r(8));
	}

	@Override
	public double readDoubleLE() throws IOException {
		return BinaryUtilJava.getDoubleLE(r(8));
	}

	@Override
	public float readFloat() throws IOException {
		return BinaryUtilJava.getFloat(r(4));
	}

	@Override
	public float readFloatLE() throws IOException {
		return BinaryUtilJava.getFloatLE(r(4));
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		readFully(b, 0, b.length);
	}

	@Override
	public int readInt() throws IOException {
		return BinaryUtilJava.getInt(r(4));
	}

	@Override
	public int readIntLE() throws IOException {
		return BinaryUtilJava.getIntLE(r(4));
	}

	@Override
	public long readLong() throws IOException {
		return BinaryUtilJava.getLong(r(8));
	}

	@Override
	public long readLongLE() throws IOException {
		return BinaryUtilJava.getLongLE(r(8));
	}

	@Override
	public short readShort() throws IOException {
		return BinaryUtilJava.getShort(r(2));
	}

	@Override
	public short readShortLE() throws IOException {
		return BinaryUtilJava.getShortLE(r(2));
	}

	@Override
	public String readStringLenUTF8() throws IOException {
		return BinaryUtilJava.getStringLenUTF8(this);
	}

	@Override
	@Deprecated
	public String readUTF() throws IOException {
		return BinaryUtilJava.getJavaModifiedUTF(this);
	}

	@Override
	public boolean readBoolean() throws IOException {
		return readByte() != 0;
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return BinaryUtilJava.getUByte(r(1));
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return BinaryUtilJava.getUShort(r(2));
	}

	@Override
	public long readUnsignedInt() throws IOException {
		return BinaryUtilJava.getUInt(r(4));
	}

	@Override
	public int readUnsignedShortLE() throws IOException {
		return BinaryUtilJava.getUShortLE(r(2));
	}

	@Override
	public long readUnsignedIntLE() throws IOException {
		return BinaryUtilJava.getUIntLE(r(4));
	}
	
	@Override
	public String readASCII(final int len) throws IOException {
		return BinaryUtilJava.getASCII(this, len, len<=8 ? buff : new byte[len]);
	}

	@Override
	@Deprecated
	public String readLine() throws IOException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void skipFully(int n) throws IOException {
		while (n > 0) {
			final int skipped = skipBytes(n);
			if (skipped == 0) throw new EOFException();
			n -= skipped;
		}
	}
}
