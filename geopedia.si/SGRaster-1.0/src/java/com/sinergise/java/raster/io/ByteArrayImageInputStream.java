package com.sinergise.java.raster.io;

import javax.imageio.stream.ImageInputStreamImpl;

public class ByteArrayImageInputStream extends ImageInputStreamImpl {
	byte[] arr;
	final int arrLen;
	
	public ByteArrayImageInputStream(byte[] input, int len) {
		super();
		this.arr=input;
		this.arrLen=len;
	}
	
	@Override
	public int read() {
        if (streamPos < arrLen) {
        	return arr[(int)(streamPos++)] & 0xff;
        }
        return -1;
	}

	@Override
	public int read(byte[] b, int off, int len) {
        int toCpy = Math.min(len,(int)(arrLen-streamPos));
        if (toCpy <= 0) {
        	return 0;
        }
        System.arraycopy(arr, (int)(streamPos), b, off, toCpy);
        streamPos += toCpy;
        return toCpy;
	}
	
	@Override
	public long length() {
		return arrLen;
	}
	
	@Override
	public void close() {
		if (arr != null) {
			try {
				super.close();
			} catch (Throwable t) {
				// ignore
			}
		}
		arr = null;
	}
}
