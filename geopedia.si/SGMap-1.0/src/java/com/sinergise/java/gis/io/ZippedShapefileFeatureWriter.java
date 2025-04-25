package com.sinergise.java.gis.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.sinergise.common.util.io.FileUtil;

public class ZippedShapefileFeatureWriter extends ShapefileFeatureWriter {
	
	private final File zipFile;
	
	public ZippedShapefileFeatureWriter(File destZipFile) throws FileNotFoundException {
		super(destZipFile.getAbsolutePath().replace(FileUtil.getSuffix(destZipFile.getName()), "shp"));
		
		this.zipFile = destZipFile;
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		super.zipFiles(zipFile);
	}

}
