package com.sinergise.java.raster.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SplitSource {
	
	public static void main(String [] args) throws IOException{
		
		makeGDALTranslateBat(args);
		//makeGDALWarpBat(args);
	}
	
	private static void makeGDALWarpBat(String[] args) throws IOException {
		File folder = new File(args[0]);
		String outFolderName = args[1];
		List<String> files = readAllFilesinDir(folder, "tif");
		
		String batPath = args[0] + File.separator+"gwarp";
		FileWriter outFile = new FileWriter(batPath + ".bat");
		PrintWriter out = new PrintWriter(outFile);
		
		writeStart(out, folder.getAbsolutePath());
		
		for (String fName : files) {
			out.println(buildWarpString(fName, outFolderName));
		}
		
		out.close();
		
		
		
	}


	private static void makeGDALTranslateBat(String[] args) throws IOException {
		File folder = new File(args[0]);
		String outFolderName = args[1];
		String size = args[2];
		String [] sizes = size.split(",");
		
		List<String> files = readAllFilesinDir(folder, "sid");
		
		
		for (int w = 0; w < files.size(); w++) {
			
		String file = files.get(w);
			
		int period = file.lastIndexOf(".");	
		String batPath = file.substring(0, period);
		
		FileWriter outFile = new FileWriter(batPath + ".bat");
		PrintWriter out = new PrintWriter(outFile);
		
		writeStart(out, folder.getAbsolutePath());
		
//		int xSize = 75098;
//		int ySize = 113803;
		int delta = 10000;
		
		//int xSize = 67403;
		//int ySize = 44734;
		
		//int xSize = 53892;
		//int ySize = 38944;
		
		int xSize = Integer.parseInt(sizes[2*w]);
		int ySize = Integer.parseInt(sizes[2*w+1]);
		
		int xDim = xSize/delta+1;
		int yDim = ySize/delta+1;
		
		int x1, x2, y1, y2;
		
		x1 = 0;
		y1 = 0;
		
		
		for (int i = 1; i <= yDim; i++) {
			x2 = i*delta;
			if(i == yDim) x2 = ySize;
			for (int j = 1; j <= xDim; j++) {
				
				y2 = j*delta;
				
				if(j == xDim) y2 = xSize;
				//System.out.println(buildString( y1, x1,  y2, x2, i, j));
				out.println(buildString( y1, x1,  y2, x2, i, j, file, outFolderName));
				
				y1 = y2;
			}
			y1 = 0;
			x1 = x2;
			
		}
		out.close();
		}
	}
	

	 private static void writeStart(PrintWriter out, String srcFolderPath) {
		
		 out.println("SET GDAL_DATA=C:\\Program Files\\GDAL\\gdal-data");
		 out.println("E:");
		 out.println("cd " + srcFolderPath);
		 out.println();
		
	}


	public static List<String> readAllFilesinDir(File folder, String end) {
		 List<String> allFiles = new ArrayList<String>();
	     for (File fileEntry : folder.listFiles()) {
	         if (fileEntry.isDirectory()) {
	             readAllFilesinDir(fileEntry, end);
	         } else {
	        	 String path = fileEntry.getAbsolutePath();
	        	 if(path.endsWith(end)) allFiles.add(path);
	         }
	     }
	     return allFiles;
	 }
	
	private static String  buildWarpString(String srcFile, String out) {
		
	

		int pathSep = srcFile.lastIndexOf("\\");
		int period = srcFile.lastIndexOf(".");
		String fileName = out  + srcFile.substring(pathSep, period) + ".tif";
		
		String s = "\"C:\\Program Files\\GDAL\\gdalwarp\" -s_srs EPSG:32632 -t_srs EPSG:32631 -tr 14.25 14.25 -tap -multi -wm 4096 -co \"TFW=YES\" -co \"PROFILE=BASELINE\" -co \"TILED=YES\" " + srcFile + "  " + fileName;
		return s;
	}

	private static String  buildString(int x1, int y1, int x2, int y2, int i, int j) {
		
		int dX = x2 - x1;
		int dY = y2 - y1;
	//	String s = "\"C:\\Program Files\\GDAL\\gdal_translate\" -co \"TFW=YES\" -co \"PROFILE=BASELINE\" -co \"TILED=YES\" -srcwin "+x1+" "+y1+" "+dX+" "+dY+" E:\\tzn\\img\\mwanza.ecw E:\\tzn\\img\\mwanzaTest\\mwanza"+i+"_"+j+".tif";
		//String s = "\"C:\\Program Files\\GDAL\\gdal_translate\" -co \"TFW=YES\" -co \"PROFILE=BASELINE\" -co \"TILED=YES\" -srcwin "+x1+" "+y1+" "+dX+" "+dY+" E:\\nga\\src\\2012-09-03\\OLPIS_Data\\OLPIS_Mosaic_Bigger_Area.ecw E:\\nga\\prepared\\2012-09-03\\split\\OLPIS_Mosaic_Bigger_Area"+i+"_"+j+".tif";
		String s = "\"C:\\Program Files\\GDAL\\gdal_translate\" -co \"TFW=YES\" -co \"PROFILE=BASELINE\" -co \"TILED=YES\" -srcwin "+x1+" "+y1+" "+dX+" "+dY+" E:\\nga\\satellite\\etm\\src\\N-31-05.sid E:\\nga\\satellite\\etm\\prepared\\N-31-05_"+i+"_"+j+".tif";
		return s;
	}
	
	private static String  buildString(int x1, int y1, int x2, int y2, int i, int j, String srcFile, String outFolderPath) {
		
		int dX = x2 - x1;
		int dY = y2 - y1;
		
		int pathSep = srcFile.lastIndexOf("\\");
		int period = srcFile.lastIndexOf(".");
		String fileName = srcFile.substring(pathSep, period);
		
	//	String s = "\"C:\\Program Files\\GDAL\\gdal_translate\" -co \"TFW=YES\" -co \"PROFILE=BASELINE\" -co \"TILED=YES\" -srcwin "+x1+" "+y1+" "+dX+" "+dY+" E:\\tzn\\img\\mwanza.ecw E:\\tzn\\img\\mwanzaTest\\mwanza"+i+"_"+j+".tif";
		//String s = "\"C:\\Program Files\\GDAL\\gdal_translate\" -co \"TFW=YES\" -co \"PROFILE=BASELINE\" -co \"TILED=YES\" -srcwin "+x1+" "+y1+" "+dX+" "+dY+" E:\\nga\\src\\2012-09-03\\OLPIS_Data\\OLPIS_Mosaic_Bigger_Area.ecw E:\\nga\\prepared\\2012-09-03\\split\\OLPIS_Mosaic_Bigger_Area"+i+"_"+j+".tif";
		String s = "\"C:\\Program Files\\GDAL\\gdal_translate\" -co \"TFW=YES\" -co \"PROFILE=BASELINE\" -co \"TILED=YES\" -srcwin "+x1+" "+y1+" "+dX+" "+dY+" "+srcFile+" " + outFolderPath + fileName+  "_"+i+"_"+j+".tif";
		return s;
	}

}
