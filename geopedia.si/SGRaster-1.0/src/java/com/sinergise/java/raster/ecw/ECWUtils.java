package com.sinergise.java.raster.ecw;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CartesianCRS;
import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.raster.core.RasterWorldInfo;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.state.gwt.StateGWT;


public class ECWUtils {
	
	public static void main(String[] args) {
		try {
			RasterWorldInfo info = readERMapperERS(new File("/home/DREJMAR/MAKEDONIJA/2009_CIR/682-3-3.ecw"));
			System.out.println(info);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static RasterWorldInfo readERMapperERS(File ersFile) throws IOException {
		return readERMapperERS(new FileInputStream(ersFile));
	}
	public static RasterWorldInfo readERMapperERS(InputStream inputStream) throws IOException  {
		//XXX no rotation
		
		StateGWT rootSt=readStateERMapperERS(inputStream);
		StateGWT st = rootSt.getState("DatasetHeader");
		StateGWT rInfo = st.getState("RasterInfo");
		StateGWT cInfo = rInfo.getState("CellInfo");
		double resX = cInfo.getDouble("Xdimension", Double.NaN);
		double resY = cInfo.getDouble("Ydimension", Double.NaN);
		
		StateGWT rCoord = rInfo.getState("RegistrationCoord");
		double leftX = rCoord.getDouble("Eastings", Double.NaN);
		double topX = rCoord.getDouble("Northings", Double.NaN);

		int w = rInfo.getInt("NrOfCellsPerLine", Integer.MIN_VALUE);
		int h = rInfo.getInt("NrOfLines", Integer.MIN_VALUE);
		
		return new RasterWorldInfo(DimI.create(w, h), new AffineTransform2D(CartesianCRS.createImageCRS(w, h),CRS.NONAME_WORLD_CRS, new double[]{resX, 0, 0, -resY, leftX, topX}));
	}
	public static StateGWT readStateERMapperERS(InputStream inputStream) throws IOException {
		StateGWT ret = new StateGWT();
		InputStreamReader isr = new InputStreamReader(inputStream, "iso-8859-2");
		try {
			LineNumberReader lnr = new LineNumberReader(isr);
			try {
				String ln=lnr.readLine();
				ArrayList<StateGWT> curStack = new ArrayList<StateGWT>();
				curStack.add(ret);
				while (ln!=null) {
					StateGWT curSt = curStack.get(curStack.size()-1);
					String[] token = splitERSLine(ln);
					if (token[1]!=null) {
						curSt.putString(token[0], token[2]);
					} else {
						if ("begin".equalsIgnoreCase(token[2])) {
							StateGWT newSt=new StateGWT();
							curSt.putState(token[0], newSt);
							curStack.add(newSt);
						} else if ("end".equalsIgnoreCase(token[2])) {
							curStack.remove(curStack.size()-1);
						}
					}
					ln=lnr.readLine();
				}
			} finally {
				lnr.close();
			}
		} finally {
			isr.close();
		}
		return ret;		
	}
	private static String[] splitERSLine(String ln) {
		String[] ret=new String[3];
		ln=ln.trim();
		int idx=ln.indexOf(' ');
		String first = ln.substring(0, idx).trim();
		ret[0]=first;
		String second = ln.substring(idx+1).trim();
		if (second.charAt(0)=='=') {
			ret[1]="=";
			second = second.substring(1).trim();
		}
		if (second.charAt(0)=='"') {
			second = second.substring(1,second.length()-1);
		}
		ret[2]=second;
		return ret;
	}
	
}
