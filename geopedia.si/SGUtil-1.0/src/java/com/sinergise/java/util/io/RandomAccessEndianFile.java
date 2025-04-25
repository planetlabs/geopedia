package com.sinergise.java.util.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessEndianFile extends AbstractBinaryRandomAccessIO {
	private final File f;
	private final RandomAccessFile raFile;

	public RandomAccessEndianFile(final String filename) throws FileNotFoundException {
		this(new File(filename), "rw");
	}

	public RandomAccessEndianFile(final File file) throws FileNotFoundException {
		this(file, "rw");
	}

	public RandomAccessEndianFile(final String filename, final String mode) throws FileNotFoundException {
		this(new File(filename), mode);
	}

	public RandomAccessEndianFile(final File file, final String mode) throws FileNotFoundException {
		f = file;
		raFile = new RandomAccessFile(file, mode);
	}
	
	public File getFile() {
		return f;
	}

	@Override
	public void readFully(final byte[] b, final int off, final int len) throws IOException {
		raFile.readFully(b, off, len);
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		raFile.write(b, off, len);
	}

	@Override
	public void seek(final long pos) throws IOException {
		raFile.seek(pos);
	}
	
	@Override
	public long length() throws IOException {
		return raFile.length();
	}

	@Override
	public void close() throws IOException {
		raFile.close();
	}
	
	@Override
	public int skipBytes(int n) throws IOException {
		return raFile.skipBytes(n);
	}
	
	@Override
	public void flush() throws IOException {}

	public static RandomAccessEndianFile createReadOnly(File f) throws FileNotFoundException {
		if (!f.isFile()) {
			throw new FileNotFoundException(f.getAbsolutePath());
		}
		return new RandomAccessEndianFile(f, "r");
	}

	public static BinaryRandomAccessIO create(File f, boolean readOnly) throws FileNotFoundException {
		if (readOnly) {
			return createReadOnly(f);
		}
		return createReadWrite(f);
	}

	public static BinaryRandomAccessIO createReadWrite(File f) throws FileNotFoundException {
		return new RandomAccessEndianFile(f, "rw");
	}
}
