package com.sinergise.java.raster.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.java.raster.ui.ElevationColors.ColorSpec;

public class ClrFileInfo {
	
	private ColorSpec[] colorSpec;
	
	public ClrFileInfo (InputStream openStream) {
		try {
			addValues(openStream);
			openStream.close();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	
	}
	
	private void addValues(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader( new InputStreamReader(is) );
		List<ColorSpec> valueColor = new ArrayList<ColorSpec>();
	    for (String line; ((line = br.readLine()) != null) ;) {
	    	ColorSpec tempColorSpec = getTempColSpec(line);
	    	if(tempColorSpec != null) {
	    		valueColor.add(tempColorSpec);
	    	}
	    }
	    colorSpec = valueColor.toArray(new ColorSpec[valueColor.size()]);
	}
	
	private static ColorSpec getTempColSpec(String line) {
		String [] splitedLine = splitIfLineValid(line);
		if(splitedLine == null) {
			return null;
		}		
		Integer value = Integer.valueOf(Integer.parseInt(splitedLine[0]));
		return new ColorSpec(value, Integer.parseInt(splitedLine[1]), Integer.parseInt(splitedLine[2]), Integer.parseInt(splitedLine[3]));
	}
	
	private static String [] splitIfLineValid(String line) {
		if(StringUtil.isNullOrEmpty(line) || !(line.contains(" "))) {
			return null;
		}
		String [] splitedLine = line.split(" ");
		if (splitedLine.length != 4) {
			return null;
		}
		return splitedLine;
	}

	public ColorSpec[] getColorSpec() {
		return colorSpec;
	}
}
