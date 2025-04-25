package com.sinergise.java.raster.dataraster;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.sinergise.common.raster.dataraster.AbstractShortDataBank;
import com.sinergise.common.raster.dataraster.SGDataBank;
import com.sinergise.java.raster.dataraster.io.PGMFormat;
import com.sinergise.java.raster.dataraster.io.SDMFormat;

public class FileShortDataBank extends AbstractShortDataBank {
	private int headerLen;
	private final RandomAccessFile raf;
	public FileShortDataBank(RandomAccessFile raf) throws IOException {
		this(raf, false);
	}
	
	@Override
	public void checkDataStore() {
	}	
	
	public FileShortDataBank(RandomAccessFile raf, boolean pgm) throws IOException {
		super(1,1,0,1);
		this.raf = raf;
		if (raf.length() > 0) {
			readHeader(pgm);
		}
	}

	public void readHeader(boolean pgm) throws IOException {
		raf.seek(0L);
		if (pgm) {
			PGMFormat.readHeader(raf, this);
		} else {
			SDMFormat.readHeader(raf, this);
		}
		headerLen = (int)raf.getFilePointer();
	}
	
	public void writeHeader(boolean pgm) throws IOException {
		raf.seek(0);
		if (pgm) {
			PGMFormat.writePGMHeaderText(this, raf);
		} else {
			SDMFormat.writeHeader(this, raf);
		}
		headerLen = (int)raf.getFilePointer();
	}
	
	@Override
	public short getShortValue(long x, long y) {
		try {
			int w = (int)dataEnv.getWidth();
			raf.seek(2L * rowInData(y) * w + 2L * colInData(x) + headerLen);
			return raf.readShort();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public short[] getDataRow(long row, long minCol, int length, short[] out, int outStart) throws IOException {
		final byte[] buf = new byte[2 * length];
		final int w = (int)dataEnv.getWidth();
		synchronized(this) {
			raf.seek(2L * row * w + 2L * minCol + headerLen);
			raf.readFully(buf);
		}
		int idx = 0;
		int col = 0;
		for (int i = 0; i < length; i++) {
			out[col++] = (short)(((buf[idx++] & 0xFF) << 8) | (buf[idx++] & 0xFF));
		}
		return out;
	}
	
	@Override
	public void setDataRow(long row, long minCol, int length, short[] inData, int inStart) throws IOException {
		byte[] buf = new byte[2 * length];
		for (int i = 0; i < length; i++) {
			final short val = inData[inStart + i];
			buf[2*i] = (byte)((val & 0xFF00) >>> 8); 
			buf[2*i+1] = (byte)(val & 0xFF); 
		}
		final int w = (int)dataEnv.getWidth();
		raf.seek(2L * row * w + 2L * minCol + headerLen);
		raf.write(buf);
	}

	@Override
	protected void setShortValue(long x, long y, short value) {
		try {
			raf.seek(2L * rowInData(y) * dataEnv.getWidth() + 2L * colInData(x) + headerLen);
			raf.writeShort(value & 0xFFFF);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void expandDataStore(int rowsBottom, int rowsTop, int colsLeft, int colsRight) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SGDataBank cutBorders(int top, int left, int bot, int right) {
		throw new UnsupportedOperationException();
	}

	public void clear() throws IOException {
		raf.seek(headerLen);
		int w = (int)dataEnv.getWidth();
		int h = (int)dataEnv.getHeight();
		ByteBuffer bb = ByteBuffer.wrap(new byte[2*w]);
		bb.mark();
		for (int i = 0; i < w; i++) {
			bb.putShort(NO_DATA_SHORT);
		}
		FileChannel fch = raf.getChannel();
		for (int i = 0; i < h; i++) {
			bb.reset();
			bb.mark();
			fch.write(bb);
		}
	}

	@Override
	public void close() throws IOException {
		raf.close();
	}

}
