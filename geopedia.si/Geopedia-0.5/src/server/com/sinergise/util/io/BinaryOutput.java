package com.sinergise.util.io;

import java.io.IOException;

public interface BinaryOutput
{
	public void write(byte[] buff) throws IOException;
	public void write(byte[] buff, int offset, int length) throws IOException;
	
	public void writeBool(boolean val) throws IOException;
	public void writeByte(byte val) throws IOException;
	
	public void writeShort(short val) throws IOException;
	public void writeChar(char val) throws IOException;
	public void writeInt(int val) throws IOException;
	public void writeLong(long val) throws IOException;
	public void writeFloat(float val) throws IOException;
	public void writeDouble(double val) throws IOException;
	
	public void writeShortLE(short val) throws IOException;
	public void writeCharLE(char val) throws IOException;
	public void writeIntLE(int val) throws IOException;
	public void writeLongLE(long val) throws IOException;
	public void writeFloatLE(float val) throws IOException;
	public void writeDoubleLE(double val) throws IOException;
	
	public void writeStringLenUTF8(String s) throws IOException;
	
	public void flush() throws IOException;
	public void close() throws IOException;
}
