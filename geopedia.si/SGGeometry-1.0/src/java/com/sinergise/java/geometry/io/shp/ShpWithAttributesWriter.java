package com.sinergise.java.geometry.io.shp;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.util.CheckUtil;
import com.sinergise.common.util.io.IOUtil;
import com.sinergise.java.geometry.io.shp.SHPHeader.ShapefileType;
import com.sinergise.java.util.io.BinaryOutput.BinaryRandomAccessOutput;
import com.sinergise.java.util.io.BinaryRandomAccessIO;
import com.sinergise.java.util.io.FileUtilJava;
import com.sinergise.java.util.io.RandomAccessEndianFile;

public class ShpWithAttributesWriter implements Closeable {
	private final SHPSHXWriter sxWriter;
	private DBFFile dbf = null;
	private final BinaryRandomAccessIO dbfOut;

	public ShpWithAttributesWriter(File shpFile) throws FileNotFoundException {
		this(new RandomAccessEndianFile(checkNotExists(shpFile)),
			new RandomAccessEndianFile(checkNotExists(FileUtilJava.replaceSuffix(shpFile,"shx"))),
			new RandomAccessEndianFile(checkNotExists(FileUtilJava.replaceSuffix(shpFile,"dbf"))));
	}

	private static File checkNotExists(File file) {
		CheckUtil.checkArgument(!file.exists(), "File "+file+" exists");
		return file;
	}

	public ShpWithAttributesWriter(BinaryRandomAccessOutput shpFile, BinaryRandomAccessOutput shxFile, BinaryRandomAccessIO dbfFile) {
		sxWriter = new SHPSHXWriter(shpFile, shxFile);
		this.dbfOut = dbfFile;
	}
	
	public void setTransform(Transform<?, ?> tr) {
		sxWriter.setTransform(tr);
	}

	public void initAttributes(DBFField[] fields) {
		DBFFile.create(dbfOut, fields, (byte)0);
		dbf = new DBFFile();
		dbf.open(dbfOut);
	}
	
	public boolean isAttributesInited() {
		return dbf != null;
	}
	
	public void initShapeType(Geometry g) throws IOException {
		initShapeType(ShapefileType.forGeometry(g));
	}
	
	/**
	 * @param shapeType SHPHeader.SHAPETYPE_*
	 * @throws IOException
	 */
	public void initShapeType(ShapefileType shapeType) throws IOException {
		assert sxWriter != null;
		sxWriter.initialize(shapeType);
	}

	@Override
	public void close() throws IOException {
			try {
				if (dbf != null) {
					dbf.updateHeader();
					dbf.flush();
				}
				if (sxWriter != null) {
					sxWriter.done();
				}	
			} finally {
				IOUtil.close(dbf, dbfOut, sxWriter.shpTarget, sxWriter.shxTarget);
			}
		}

	public boolean isShapeTypeInited() {
		return sxWriter.getShapeType() != null;
	}

	public BinaryRandomAccessOutput getShpFile() {
		return sxWriter.shpTarget;
	}
	public BinaryRandomAccessOutput getShxFile() {
		return sxWriter.shxTarget;
	}
	public BinaryRandomAccessIO getDbfFile() {
		return dbfOut;
	}

	public void appendRecord(Geometry g, Object[] vals) throws IOException {
		sxWriter.appendGeometry(g);
		if (dbf != null) {
			dbf.prepareEmptyRecord();
			for (int i = 0; i < vals.length; i++) {
				Object v = vals[i];
				if (v instanceof Date) {
					dbf.setValue(i, (Date)v);
				} else if (v instanceof Boolean) {
					dbf.setValue(i, (Boolean)v);
				} else {
					dbf.setValue(i, String.valueOf(v));
				}
			}
			dbf.appendCurrentRecord();
		}
	}
}
