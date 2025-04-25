package com.sinergise.util.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class BinaryInputStream extends InputStream implements BinaryInput
{
	byte[] mybuff = new byte[8];
	private InputStream is;
	
	public BinaryInputStream(InputStream is)
	{
		this.is = is;
	}
	
	public void close() throws IOException
	{
		if (is != null) {
			is.close();
			is = null;
		}
	}
	
	public int read() throws IOException
	{
		return is.read();
	}
	
	public int read(byte[] b) throws IOException
	{
		return is.read(b);
	}
	
	public int read(byte[] b, int off, int len) throws IOException
	{
		return is.read(b, off, len);
	}
	
	private void read(int n) throws IOException
	{
		int pos = 0;
		while (n > 0) {
			int read = is.read(mybuff, pos, n);
			if (read < 0)
				throw new EOFException();
			pos += read;
			n -= read;
		}
	}
	
	public short readShort() throws IOException
	{
		read(2);

		return (short) (
		       ((mybuff[0] & 255) << 8) |
		       ((mybuff[1] & 255)     )
		       );
	}
	
	public int readInt() throws IOException
	{
		read(4);

		return ((mybuff[0]      ) << 24) |
		       ((mybuff[1] & 255) << 16) |
		       ((mybuff[2] & 255) <<  8) |
		       ((mybuff[3] & 255)      )
		       ;
	}
	
	public long readLong() throws IOException
	{
		read(8);

		return (long)((
		        ((mybuff[0]      ) << 24) |
		        ((mybuff[1] & 255) << 16) |
		        ((mybuff[2] & 255) <<  8) |
		        ((mybuff[3] & 255)      )
		       )) << 32
		       |
		       (
		        ((mybuff[4]      ) << 24) |
		        ((mybuff[5] & 255) << 16) |
		        ((mybuff[6] & 255) <<  8) |
		        ((mybuff[7] & 255)      )
		       )
		       ;
	}

	public short[] readShortArr() throws IOException
	{
		int len = readInt();
		if (len < 0)
			return null;

		short[] out = new short[len];
		for (int a = 0; a < len; a++)
			out[a] = readShort();

		return out;
	}

	public short[][] writeShortArrArr() throws IOException
	{
		int len = readInt();
		if (len < 0)
			return null;

		short[][] out = new short[len][];
		for (int a = 0; a < len; a++)
			out[a] = readShortArr();

		return out;
	}

	public void readFully(byte[] tmp) throws IOException
    {
		int remain = tmp.length;
		int pos = 0;
		while (remain > 0) {
			int read = is.read(tmp, pos, remain);
			if (read < 0)
				throw new EOFException();
			pos += read;
			remain -= read;
		}
    }

	public float readFloat() throws IOException
    {
		return Float.intBitsToFloat(readInt());
    }

	public boolean readBool() throws IOException
    {
		return readByte() != 0;
    }

	public byte readByte() throws IOException
    {
		int read = read();
		if (read < 0)
			throw new EOFException();
		return (byte) read;
    }

	public char readChar() throws IOException
    {
		read(2);

		return (char) (
		       ((mybuff[0] & 255) << 8) |
		       ((mybuff[1] & 255)     )
		       );
    }

	public char readCharLE() throws IOException
    {
		read(2);

		return (char) (
		       ((mybuff[1] & 255) << 8) |
		       ((mybuff[0] & 255)     )
		       );
    }

	public double readDouble() throws IOException
    {
		return Double.longBitsToDouble(readLong());
    }

	public double readDoubleLE() throws IOException
    {
		return Double.longBitsToDouble(readLongLE());
    }

	public float readFloatLE() throws IOException
    {
		return Float.intBitsToFloat(readIntLE());
    }

	public void readFully(byte[] buff, int offset, int length) throws IOException
    {
		while (length > 0) {
			int read = read(buff, offset, length);
			if (read <= 0)
				throw new EOFException();
			offset += read;
			length -= read;
		}
    }

	public int readIntLE() throws IOException
    {
		read(4);

		return ((mybuff[3]      ) << 24) |
		       ((mybuff[2] & 255) << 16) |
		       ((mybuff[1] & 255) <<  8) |
		       ((mybuff[0] & 255)      )
		       ;
    }

	public long readLongLE() throws IOException
    {
		read(8);

		return (long)((
		        ((mybuff[7]      ) << 24) |
		        ((mybuff[6] & 255) << 16) |
		        ((mybuff[5] & 255) <<  8) |
		        ((mybuff[4] & 255)      )
		       )) << 32
		       |
		       (
		        ((mybuff[3]      ) << 24) |
		        ((mybuff[2] & 255) << 16) |
		        ((mybuff[1] & 255) <<  8) |
		        ((mybuff[0] & 255)      )
		       )
		       ;
    }

	public short readShortLE() throws IOException
    {
		read(2);

		return (short) (
		       ((mybuff[1] & 255) << 8) |
		       ((mybuff[0] & 255)     )
		       );
    }

	public String readStringLenUTF8() throws IOException
    {
		int len = readInt();
		if (len == -1)
			return null;
		if (len == 0)
			return "";
		byte[] data = new byte[len];
		readFully(data);
		return new String(data, 0, data.length, "UTF-8");
    }
}
