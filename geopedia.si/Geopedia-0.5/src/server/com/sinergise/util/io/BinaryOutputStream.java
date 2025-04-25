package com.sinergise.util.io;

import java.io.IOException;
import java.io.OutputStream;

public class BinaryOutputStream extends OutputStream implements BinaryOutput
{
	static final int BUFFSIZE = 4096;
	byte[] mybuff = new byte[BUFFSIZE];
	int buffpos = 0;
	private OutputStream os;
	
	public BinaryOutputStream(OutputStream os)
	{
		this.os = os;
	}
	
	public void write(int b) throws IOException
	{
		if (buffpos >= BUFFSIZE)
			flush();
		
		mybuff[buffpos++] = (byte) b;
	}
	
	public void write(byte[] b) throws IOException
	{
		if (buffpos > 0)
			flush();
		
		os.write(b);
	}
	
	public void write(byte[] b, int off, int len) throws IOException
	{
		if (buffpos > 0)
			flush();
		
		os.write(b, off, len);
	}
	
	public void flush() throws IOException
	{
		if (buffpos > 0) {
			os.write(mybuff, 0, buffpos);
			buffpos = 0;
		}
	}
	
	public void close() throws IOException
	{
		if (os != null) {
			flush();
			os.close();
			os = null;
		}
	}
	
	public void writeShort(short s) throws IOException
	{
		if (BUFFSIZE - buffpos < 2)
			flush();
		
		mybuff[buffpos++] = (byte) (s >>> 8);
		mybuff[buffpos++] = (byte) s;
	}
	
	public void writeInt(int i) throws IOException
	{
		if (BUFFSIZE - buffpos < 4)
			flush();
		
		mybuff[buffpos++] = (byte) (i >>> 24);
		mybuff[buffpos++] = (byte) (i >>> 16);
		mybuff[buffpos++] = (byte) (i >>>  8);
		mybuff[buffpos++] = (byte) (i       );
	}
	
	public void writeLong(long l) throws IOException
	{
		if (BUFFSIZE - buffpos < 8)
			flush();
		
		mybuff[buffpos++] = (byte) (l >>> 56);
		mybuff[buffpos++] = (byte) (l >>> 48);
		mybuff[buffpos++] = (byte) (l >>> 40);
		mybuff[buffpos++] = (byte) (l >>> 32);
		mybuff[buffpos++] = (byte) (l >>> 24);
		mybuff[buffpos++] = (byte) (l >>> 16);
		mybuff[buffpos++] = (byte) (l >>>  8);
		mybuff[buffpos++] = (byte) (l       );
	}

	public void writeShortArr(short[] data) throws IOException
	{
		if (data == null) {
			writeInt(-1);
			return;
		}
		writeInt(data.length);
		for (short s: data)
			writeShort(s);
	}

	public void writeShortArr(short[][] data) throws IOException
	{
		if (data == null) {
			writeInt(-1);
			return;
		}
		writeInt(data.length);
		for (short[] s : data)
			writeShortArr(s);
	}

	public void writeFloat(float val) throws IOException
    {
		writeInt(Float.floatToRawIntBits(val));
    }

	public void writeBool(boolean val) throws IOException
    {
		write(val ? 1 : 0);
    }

	public void writeByte(byte val) throws IOException
    {
		write(val);
    }

	public void writeChar(char val) throws IOException
    {
		if (BUFFSIZE - buffpos < 2)
			flush();
		
		mybuff[buffpos++] = (byte) (val >>> 8);
		mybuff[buffpos++] = (byte) val;
    }

	public void writeCharLE(char val) throws IOException
    {
		if (BUFFSIZE - buffpos < 2)
			flush();
		
		mybuff[buffpos++] = (byte) val;
		mybuff[buffpos++] = (byte) (val >>> 8);
    }

	public void writeDouble(double val) throws IOException
    {
		writeLong(Double.doubleToRawLongBits(val));
    }

	public void writeDoubleLE(double val) throws IOException
    {
		writeLongLE(Double.doubleToRawLongBits(val));
    }

	public void writeFloatLE(float val) throws IOException
    {
		writeIntLE(Float.floatToRawIntBits(val));
    }

	public void writeIntLE(int i) throws IOException
    {
		if (BUFFSIZE - buffpos < 4)
			flush();
		
		mybuff[buffpos++] = (byte) (i       );
		mybuff[buffpos++] = (byte) (i >>>  8);
		mybuff[buffpos++] = (byte) (i >>> 16);
		mybuff[buffpos++] = (byte) (i >>> 24);
    }

	public void writeLongLE(long l) throws IOException
    {
		if (BUFFSIZE - buffpos < 8)
			flush();
		
		mybuff[buffpos++] = (byte) (l       );
		mybuff[buffpos++] = (byte) (l >>>  8);
		mybuff[buffpos++] = (byte) (l >>> 16);
		mybuff[buffpos++] = (byte) (l >>> 24);
		mybuff[buffpos++] = (byte) (l >>> 32);
		mybuff[buffpos++] = (byte) (l >>> 40);
		mybuff[buffpos++] = (byte) (l >>> 48);
		mybuff[buffpos++] = (byte) (l >>> 56);
    }

	public void writeShortLE(short s) throws IOException
    {
		if (BUFFSIZE - buffpos < 2)
			flush();
		
		mybuff[buffpos++] = (byte) s;
		mybuff[buffpos++] = (byte) (s >>> 8);
    }

	public void writeStringLenUTF8(String s) throws IOException
    {
		if (s == null) {
			writeInt(-1);
			return;
		}
		
		if (s.length() == 0) {
			writeInt(0);
			return;
		}
		
		byte[] data = s.getBytes("UTF-8");
		writeInt(data.length);
		write(data);
    }
}
