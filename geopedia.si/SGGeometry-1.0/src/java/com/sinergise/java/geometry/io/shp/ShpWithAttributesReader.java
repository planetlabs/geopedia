package com.sinergise.java.geometry.io.shp;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.io.IOUtil;
import com.sinergise.common.util.io.ObjectReader.ObjectReadException;
import com.sinergise.java.util.io.FileUtilJava;

public class ShpWithAttributesReader implements Closeable {
	private SHPReader shp;
	private DBFFile dbf;
	private int curIndex = 0;
	private Geometry curGeom;
	private String[] curAttributes;
	
	public ShpWithAttributesReader(File shpFile) throws ObjectReadException, IOException {
		try {
			shp = new SHPReader(shpFile);
			dbf = new DBFFile();
			dbf.open(FileUtilJava.setSuffix(shpFile, "dbf"), true);
			curAttributes = new String[dbf.getColumnCount()];
		} catch (Throwable t) {
			close();
			throw (ObjectReadException)(new ObjectReadException(t.getMessage()).initCause(t));
		}
	}
	
	public DBFFile getDbf() {
		return dbf;
	}
	
	public boolean hasNext() throws ObjectReadException {
		return shp.hasNext();
	}
	
	public void next() throws ObjectReadException, UnsupportedEncodingException {
		curGeom = shp.readNext();
		dbf.seek(curIndex++);
		curAttributes = new String[dbf.getColumnCount()];
		for (int i = 0; i < dbf.getColumnCount(); i++) {
			curAttributes[i] = dbf.getValue(i);
		}
	}
	
	public Geometry getGeometry() {
		return curGeom;
	}
	
	public Envelope getFileMBR() {
		return shp.getFileMBR();
	}
	
	public String[] getAttributes() {
		return curAttributes;
	}
	
	@Override
	public void close() throws IOException {
		IOUtil.close(shp, dbf);
	}
}
