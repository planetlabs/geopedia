package com.sinergise.util.io;

import java.io.IOException;

public interface BinaryInput
{
	public int read(byte[] buff) throws IOException;
	public void readFully(byte[] buff) throws IOException;
	public int read(byte[] buff, int offset, int length) throws IOException;
	public void readFully(byte[] buff, int offset, int length) throws IOException;
	
	public byte readByte() throws IOException;
	public boolean readBool() throws IOException;

	public short readShort() throws IOException;
	public char readChar() throws IOException;
	public int readInt() throws IOException;
	public long readLong() throws IOException;
	public float readFloat() throws IOException;
	public double readDouble() throws IOException;
	
	public short readShortLE() throws IOException;
	public char readCharLE() throws IOException;
	public int readIntLE() throws IOException;
	public long readLongLE() throws IOException;
	public float readFloatLE() throws IOException;
	public double readDoubleLE() throws IOException;
	
	public String readStringLenUTF8() throws IOException;
}
