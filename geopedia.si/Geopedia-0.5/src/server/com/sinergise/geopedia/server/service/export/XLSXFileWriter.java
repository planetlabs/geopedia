package com.sinergise.geopedia.server.service.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.sinergise.java.gis.io.XLSXFeatureWriter;

public class XLSXFileWriter extends XLSXFeatureWriter {
	public XLSXFileWriter (File destFile) throws IOException {
		super(new FileOutputStream(getFile(destFile)));
	}
	
	private static File getFile(File destFile) {
		String filebase = destFile.getAbsolutePath();
		int nameLen = filebase.length();
		int lastExtSeparator = filebase.lastIndexOf(".");
		if (lastExtSeparator>0 && (nameLen-lastExtSeparator)<=4) {			
			filebase = filebase.substring(0, lastExtSeparator);
		}
		return new File(filebase+".xlsx");
	}

}
