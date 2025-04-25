package com.sinergise.util.io;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class EasyRandomAccessFile extends RandomAccessFile
{
	public EasyRandomAccessFile(String name, String mode) throws FileNotFoundException
	{
		super(name, mode);
	}

	public EasyRandomAccessFile(File file, String mode) throws FileNotFoundException
	{
		super(file, mode);
	}
	
	byte[] buff=new byte[8];
	
	public int readIntLE() throws IOException
	{
		readFully(buff, 0, 4);
		
		int d=0xff & buff[0];
		int c=0xff & buff[1];
		int b=0xff & buff[2];
		int a=0xff & buff[3];
		
		return (a<<24) | (b<<16) | (c<<8) | d;
	}
	
	public int readIntBE() throws IOException
	{
		readFully(buff, 0, 4);
		
		int a=0xff & buff[0];
		int b=0xff & buff[1];
		int c=0xff & buff[2];
		int d=0xff & buff[3];
		
		return (a<<24) | (b<<16) | (c<<8) | d;
	}
	
	public short readShortLE() throws IOException
	{
		readFully(buff, 0, 2);
		int b=0xff & buff[0];
		int a=0xff & buff[1];
		
		return (short)((a<<8) | b);
	}
	
	public int readShortBE() throws IOException
	{
		readFully(buff, 0, 2);
		int a=0xff & buff[0];
		int b=0xff & buff[1];
		
		return ((a<<8) | b);
	}
	
	public long readLongBE() throws IOException
	{
		readFully(buff, 0, 8);
		
		int a=0xff & buff[0];
		int b=0xff & buff[1];
		int c=0xff & buff[2];
		int d=0xff & buff[3];
		int e=0xff & buff[4];
		int f=0xff & buff[5];
		int g=0xff & buff[6];
		int h=0xff & buff[7];
		
		return (long)((a<<24) | (b<<16) | (c<<8) | (d)) << 32 | (0xffffffffL & ((e<<24)) | (f<<16) | (g<<8) | h);
	}

	public long readLongLE() throws IOException
	{
		readFully(buff, 0, 8);
		
		int h=0xff & buff[0];
		int g=0xff & buff[1];
		int f=0xff & buff[2];
		int e=0xff & buff[3];
		int d=0xff & buff[4];
		int c=0xff & buff[5];
		int b=0xff & buff[6];
		int a=0xff & buff[7];
		
		return (long)((a<<24) | (b<<16) | (c<<8) | (d)) << 32 | (0xffffffffL & ((e<<24)) | (f<<16) | (g<<8) | h);
	}
	
	public double readDoubleBE() throws IOException
	{
		return Double.longBitsToDouble(readLongBE());
	}
	
	public double readDoubleLE() throws IOException
	{
		return Double.longBitsToDouble(readLongLE());
	}
	
	public void writeIntLE(int val) throws IOException
	{
		buff[0]=(byte)val;
		buff[1]=(byte)(val>>>8);
		buff[2]=(byte)(val>>>16);
		buff[3]=(byte)(val>>>24);
		
		write(buff, 0, 4);
	}
	
	public void writeIntBE(int val) throws IOException
	{
		buff[0]=(byte)(val>>>24);
		buff[1]=(byte)(val>>>16);
		buff[2]=(byte)(val>>>8);
		buff[3]=(byte)val;
		
		write(buff, 0, 4);
	}
	
	public void writeShortLE(short val) throws IOException
	{
		buff[0]=(byte)val;
		buff[1]=(byte)(val>>>8);

		write(buff, 0, 2);
	}
	
	public void writeShortBE(short val) throws IOException
	{
		buff[0]=(byte)(val>>>8);
		buff[1]=(byte)val;

		write(buff, 0, 2);
	}
	
	public void writeLongLE(long val) throws IOException
	{
		buff[0]=(byte)val;
		buff[1]=(byte)(val>>>8);
		buff[2]=(byte)(val>>>16);
		buff[3]=(byte)(val>>>24);
		buff[4]=(byte)(val>>>32);
		buff[5]=(byte)(val>>>40);
		buff[6]=(byte)(val>>>48);
		buff[7]=(byte)(val>>>56);

		write(buff, 0, 8);
	}

	public void writeLongBE(long val) throws IOException
	{
		buff[0]=(byte)(val>>>56);
		buff[1]=(byte)(val>>>48);
		buff[2]=(byte)(val>>>40);
		buff[3]=(byte)(val>>>32);
		buff[4]=(byte)(val>>>24);
		buff[5]=(byte)(val>>>16);
		buff[6]=(byte)(val>>>8);
		buff[7]=(byte)val;

		write(buff, 0, 8);
	}
	
	public void writeDoubleBE(double val) throws IOException
	{
		writeLongBE(Double.doubleToRawLongBits(val));
	}
	
	public void writeDoubleLE(double val) throws IOException
	{
		writeLongLE(Double.doubleToRawLongBits(val));
	}
	
	public void skipFully(int n) throws IOException
	{
		while (n > 0) {
			int skipped = skipBytes(n);
			if (skipped == 0)
				throw new EOFException();
			
			n -= skipped;
		}
	}
}
