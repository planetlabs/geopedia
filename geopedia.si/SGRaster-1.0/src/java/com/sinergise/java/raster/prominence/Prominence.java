package com.sinergise.java.raster.prominence;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

import com.sinergise.common.raster.dataraster.ShortDataBank;
import com.sinergise.common.util.collections.BitSet2D;
import com.sinergise.common.util.geom.EnvelopeL;
import com.sinergise.java.raster.dataraster.io.PGMFormat;

public class Prominence{
	
	static ShortDataBank map;
	static ShortDataBank mapRasterTemp;
	static EnvelopeL dataEnv;
	static ArrayList<Peak> peaks = new ArrayList<Peak>();
	static ArrayList<Peak> prominence = new ArrayList<Peak>();
	static Peak[] peaksArray;
	static BitSet2D visited;
	static BitSet2D thresholdMap;
	static BitSet2D peakPositions;
	static Queue<Point> queue = new LinkedList<Point>();
	static PrintWriter out;
	static PrintWriter prominent;
	static int[][] peakMap;
	
	private static int v0, v1, v2, v3, v4, v5, v6, v7, v8;
	private static int maxHeight;

	static long offsetX;
	static long offsetY;
	static long dimensionsX;
	static long dimensionsY;
	
	private static int counter = 0;
	
	private static int thresholdLevel;

	public static void main(String[] args) throws IOException {
		
		
		map = (ShortDataBank)PGMFormat.loadPGM(new FileInputStream(new File("C:\\prominence\\dmv0125.pgm")));
//		mapRasterTemp = new FileShortDataBank(rasterTemp, true);
		
		dataEnv = map.getEnvelope();
		
		offsetX = dataEnv.getMinX();
		offsetY = dataEnv.getMinY();
		dimensionsX = dataEnv.getMaxX() - offsetX + 1;
		dimensionsY = dataEnv.getMaxY() - offsetY + 1;
		
		peakMap = new int[(int)dimensionsX][(int)dimensionsY];
//		Arrays.fill(peakMap, new Integer(-1));
		
		int bitSetSize = (int)(Math.max(offsetX, offsetY) + Math.max(dimensionsX, dimensionsY));
		
		visited = new BitSet2D(bitSetSize, bitSetSize);
		thresholdMap = new BitSet2D(1*(int)dimensionsX, 1*(int)dimensionsX);
		peakPositions = new BitSet2D(bitSetSize, bitSetSize);
		
		out = new PrintWriter(new FileWriter("C:\\prominence\\prominence.txt"));
		prominent = new PrintWriter(new FileWriter("C:\\prominence\\prominent.txt"));
		
		findPeaks();
		
		peaks.trimToSize();
		
		// sort so highest peaks first, helps speed up prominence search
		Collections.sort(peaks, new Comparator<Peak>(){
			
			@Override
			public int compare(Peak p1, Peak p2){
				return (p1.getHeight() < p2.getHeight()) ? 1 : -1;
			}
			
		});
		
//		thresholdDraw(10000);
		
		System.out.println("Indexing peaks");
			
		for(Peak peak : peaks){
			long x = peak.getX();
			long y = peak.getY();
			int index = peaks.indexOf(peak);
			peakMap[(int)(x - offsetX)][(int)(y - offsetY)] = index;
		}
		
//		paintPeaks();
				
		System.out.println("Thresholding start.");
		
		for(thresholdLevel = maxHeight - 2000; thresholdLevel >= 2000; thresholdLevel = thresholdLevel - 20){
			System.out.println("Calculating next threshold " + thresholdLevel +" = " + Math.round(thresholdLevel*0.05 - 100) + "m");
//			threshold(thresholdLevel);
			findPaths();
			System.out.println("Peaks with calculated prominence: " +counter);
			visited.clearAll();
		}
		
		out.close();
		prominent.close();
		System.out.println("Finished.");
		
	}
	
	private static void findPaths(){
		
		int previousHeight = 0;
		
		for(Peak peak : peaks){
			
			
			if(peak.getProminence() != 0) continue;
			if(peak.getHeight() <= thresholdLevel) continue; //peak needs to be strictly higher than the threshold level
			queue.add(new Point(peak.getX(), peak.getY(), peak.getHeight()));
			if(peak.getHeight() == previousHeight){ // in case we have equal heights, works because peaks are sorted by height
				visited.clearAll();
			}
			findPathsSingle(peak);
			previousHeight = peak.getHeight();
		}
	}
	
	private static void findPathsSingle(Peak peak){
		
		long peakX = peak.getX();
		long peakY = peak.getY();
		int peakHeight = peak.getHeight();
		
		while(!queue.isEmpty()){
			
			Point p = queue.remove();
		
			long x = p.x;
			long y = p.y;
			
			if(x > dimensionsX + offsetX - 1 || x < offsetX || y > dimensionsY + offsetY - 1|| y < offsetY){
				continue;
			}
			
			if(visited.isSet((int)(x - offsetX), (int)(y - offsetY))){
				continue;
			}
			
			if((map.getShortValue(x, y) & 0xFFFF) < thresholdLevel){
				continue;
			}
			
			if(peakPositions.isSet((int)(x), (int)(y)) && (peakX != x || peakY != y)){ // some peak hit, but not itself
				
				int height = map.getShortValue(x, y) & 0xFFFF;
				
				if(height > peakHeight){
					System.out.println("ERROR !!!!! THIS SHOULD NOT HAPPEN! height : " + height + 
						" peak height: " + peakHeight + " threshold level: " + thresholdLevel);
					int index = peakMap[(int)(x - offsetX)][(int)(y - offsetY)];
					if(peaks.get(index).getProminence() == 0){ // lower peak has not yet been classified
						System.out.println("THIS REALLY SHOULD NOT HAPPEN " + peaks.get(index).getHeight());
						
					}
					else System.out.println("Prominence nonzero " + peaks.get(index).getHeight() + ", " + peaks.get(index).getProminence());
					
					peak.setProminence(peakHeight - thresholdLevel);
					peak.setParent(new Peak(x, y, height));
					counter++;
					if(peak.getProminence() > 1000){
						prominent.println("Peak x, y, height, prominence: " + peak.getX() + ", " + 
							peak.getY() + ", " + peak.getHeight() + " = " + Math.round(peak.getHeight()*0.05 - 100) + 
							"m, " + Math.round(peak.getProminence()*0.05) +"m | parent x, y, height " + peak.getParent().getX() 
							+ ", " + peak.getParent().getY() + ", " + Math.round(peak.getParent().getHeight()*0.05 - 100)+ "m");
					}
//					below comment prints every result with prominence >=5m to prominence.txt
					if (peak.getProminence() >= 100) out.println("Peak x, y, height, prominence: " + peak.getX() + ", " + 
						peak.getY() + ", " + peak.getHeight() + " = " + Math.round(peak.getHeight()*0.05 - 100) + 
						"m, " + Math.round(peak.getProminence()*0.05) +"m | parent x, y, height " + peak.getParent().getX() 
						+ ", " + peak.getParent().getY() + ", " + Math.round(peak.getParent().getHeight()*0.05 - 100)+ "m");
					break;
				}
				
				// if height == peakHeight do nothing (handled in findPaths())
						
				if(height < peakHeight){
					int index = peakMap[(int)(x - offsetX)][(int)(y - offsetY)];
					if(peaks.get(index).getProminence() == 0){ // lower peak has not yet been classified
						int lowerHeight = peaks.get(index).getHeight();
						peaks.get(index).setProminence(lowerHeight - thresholdLevel);
						peaks.get(index).setParent(new Peak(peakX, peakY, peakHeight));
						counter++;
						if(peaks.get(index).getProminence() > 1000){
							prominent.println("Peak x, y, height, prominence: " + x + ", " + 
								y + ", " + height + " = " + Math.round(height*0.05 - 100) + 
								"m, " + Math.round((lowerHeight - thresholdLevel)*0.05) +"m | parent x, y, height " + peakX 
								+ ", " + peakY + ", " + Math.round(peakHeight*0.05 - 100)+ "m");
						}
						if (peaks.get(index).getProminence() >= 100) out.println("Peak x, y, height, prominence: " + x + ", " + 
							y + ", " + height + " = " + Math.round(height*0.05 - 100) + 
							"m, " + Math.round((lowerHeight - thresholdLevel)*0.05) +"m | parent x, y, height " + peakX 
							+ ", " + peakY + ", " + Math.round(peakHeight*0.05 - 100)+ "m");
					}
				}
			}
			visited.set((int)(x - offsetX), (int)(y - offsetY));
			
			queue.add(new Point(x - 1, y - 1, 0));
			queue.add(new Point(x, y - 1, 0));
			queue.add(new Point(x + 1, y - 1, 0));
			queue.add(new Point(x - 1, y, 0));
			queue.add(new Point(x + 1, y, 0));
			queue.add(new Point(x - 1, y + 1, 0));
			queue.add(new Point(x, y + 1, 0));
			queue.add(new Point(x + 1, y + 1, 0));
		}
		queue.clear();
	}
	
	private static void floodFill(){
		
		System.out.println("Starting flood fill");
		

		for(Peak peak : peaks){
//			if(peaks.indexOf(peak) % 1000 != 0) continue;
			long x = peak.getX();
			long y = peak.getY();
			int h = peak.getHeight();
			
			System.out.println("Peak found: Coords x, Coords y; height[16bit] = height[m]: " + 
				(x - offsetX) + ", " + (y - offsetY) + "; "+ h + " = " + (peak.getHeight()*0.05 - 100));
			
			queue.add(new Point(x,y,h));
			floodFillPoint();
//			visited.clearAll();		
		}
		System.out.println("Flood finished.");
	}
	
	private static void floodFillSingle(Peak peak){
		
		long x = peak.getX();
		long y = peak.getY();
		int h = peak.getHeight();
		
		queue.add(new Point(x,y,h));
		floodFillPoint();
		
	}
	
	private static int findTriglav(){
		int triglav = 0;
		int triglavHeight = 0;
		for(Peak p : peaks){
			if(triglavHeight*0.05 - 100 > 2800) break;	//for dmv0125
//			if(triglavHeight*0.1 - 10 > 2800) break;	//for dmv005
			if(p.getHeight() > triglavHeight){
				triglavHeight = p.getHeight();
				triglav = peaks.indexOf(p);
			}
		}
		return triglav;
	}
	
	private static void findPeaks(){
		
		int n = 0;
		
		System.out.println("offsetX, offsetX; dimensionsX, dimensionsY: " + offsetX + ", " + offsetY + "; " + dimensionsX + ", " + dimensionsY);
		
		for(long y = offsetY + 1; y < offsetY + dimensionsY - 1; y++){

			long x = offsetX + 1;
			
			v0 = map.getShortValue(x,y) & 0xFFFF;
//			v1 = map.getShortValue(x - 1, y - 1) & 0xFFFF;
			v2 = map.getShortValue(x, y - 1) & 0xFFFF;
			v3 = map.getShortValue(x + 1, y - 1) & 0xFFFF;
//			v4 = map.getShortValue(x - 1, y) & 0xFFFF;
			v5 = map.getShortValue(x + 1, y) & 0xFFFF;
//			v6 = map.getShortValue(x - 1, y + 1) & 0xFFFF;
			v7 = map.getShortValue(x,  y + 1) & 0xFFFF;
			v8 = map.getShortValue(x + 1, y + 1) & 0xFFFF;
			
			for(x = offsetX + 1; x < offsetX + dimensionsX - 1; x++){
				
//				x -->
//			y	[v1][v2][v3]
//			|	[v4][v0][v5]
//			v	[v6][v7][v8]
			
				v1 = v2;
				v4 = v0;
				v6 = v7;
				v2 = v3;
				v0 = v5;
				v7 = v8;
				
				
				v3 = map.getShortValue(x + 1, y - 1) & 0xFFFF;
				v5 = map.getShortValue(x + 1, y) & 0xFFFF;
				v8 = map.getShortValue(x + 1, y + 1) & 0xFFFF;
				
				if(isPeak() && v0 >= 2500){
					peaks.add(new Peak(x, y, v0));
					peakPositions.set((int)(x), (int)(y));
					
//					for(Peak p : peaks){
//						if(p.getX() == x && p.getY() == y){
//							if(!peakPositions.isSet((int)x, (int)y)){
//								System.out.println("MISMATCH");	
//							}
//						}
//					}
					
					if(v0 > maxHeight){
						maxHeight = v0;
					}
					n++;
				}
			}
		}
		System.out.println("Total number of local maxima: " + n);
		System.out.println("Peaks size check: " + peaks.size());
	}
	
	private static boolean isPeak(){
		
//		top left most point gets defined as a peak
//		[<][<][=]
//		[<][0][=]
//		[<][=][=]
		
		if(v1 < v0 && v2 < v0 && v3 <= v0 && v4 < v0 && v5 <= v0 && v6 < v0 && v7 <= v0 && v8 <= v0 && v0 != 65535){
			return true;
		}
		return false;
	}
	
	private static boolean filterAddPeak(long x0, long y0, int h0){ //returns true if new peak (i.e. n++)
		for(Peak p : peaks){
			long x = p.getX();
			long y = p.getY();
			int h = p.getHeight();
			
			if(Math.abs(x - x0) + Math.abs(y - y0) < 15){ //if they are close remove the lower one
				if(h0 <= h){ // if candidate peak lower than existing peak
					return false;
				}
				peaks.set(peaks.indexOf(p), new Peak(x0, y0, h0)); //replace peak with new higher one
				return false;
			}
		}
		peaks.add(new Peak(x0, y0, h0));
		return true;
	}
	
	private static void paintPeaks() throws IOException{
		for(Peak p : peaks){
			long x = p.getX();
			long y = p.getY();
			mapRasterTemp = (ShortDataBank)PGMFormat.loadPGM(new FileInputStream(new File("C:\\prominence\\dmv0125.pgm")));
			mapRasterTemp.setShortValue(x, y, (short)-1);
		}
		OutputStream os = new BufferedOutputStream(new FileOutputStream(new File("C:\\prominence\\output.pgm")), 32*1024*1024);
		try {
			PGMFormat.savePGM(os, mapRasterTemp);
		} finally {
			os.close();
		}
	}

	
//	private static void floodFillPoint(long x, long y, int height){
	private static void floodFillPoint(){
		
		while(!queue.isEmpty()){
			Point p = queue.remove();
		
			long x = p.x;
			long y = p.y;
			int height = p.height;
			
			if(x > dimensionsX + offsetX - 1 || x < offsetX || y > dimensionsY + offsetY - 1|| y < offsetY){
//				System.out.println("Out of bounds");
				continue;
			}
			if(visited.isSet((int)(x - offsetX), (int)(y - offsetY))){
//				System.out.println("Visited " + (x - offsetX) + ", " + (y - offsetY));
				continue;
			}
			
			int pointHeight = map.getShortValue(x, y) & 0xFFFF;
			
			// if visited point altitude > previous point + tolerance, stop
			// (we need go go down or very slightly up)
			if(pointHeight > height + 0){
//				visited.set((int)(x - offsetX), (int)(y - offsetY));
//				System.out.println("Point higher");
				continue;
			}
			
			visited.set((int)(x - offsetX), (int)(y - offsetY));
			mapRasterTemp.setShortValue(x, y, (short)-1000);
//			mapRasterTemp.writeShortValue(x, y, (short)-100);
			
	//		[][][]
	//		[][][]
	//		[][][]
			
			queue.add(new Point(x - 1, y - 1, pointHeight));
			queue.add(new Point(x, y - 1, pointHeight));
			queue.add(new Point(x + 1, y - 1, pointHeight));
			queue.add(new Point(x - 1, y, pointHeight));
			queue.add(new Point(x + 1, y, pointHeight));
			queue.add(new Point(x - 1, y + 1, pointHeight));
			queue.add(new Point(x, y + 1, pointHeight));
			queue.add(new Point(x + 1, y + 1, pointHeight));
			
		}
	}
	
	private static void threshold(int value){
		for(long x = offsetX; x < offsetX + dimensionsX; x++){
			for(long y = offsetY; y < offsetY + dimensionsY; y++){
				
				int h = map.getShortValue(x, y) & 0xFFFF;
				
//				if(h <= value) mapRasterTemp.setShortValue(x, y, (short)0);
//				else mapRasterTemp.setShortValue(x, y, (short)-1);
				
				if(h >= value) thresholdMap.set((int)(x - offsetX), (int)(y - offsetY));
//				if(h >= value) thresholdMap.setShortValue(x, y, (short)-1);
//				else thresholdMap.setShortValue(x, y, (short)0);
				
			}
		}
	}
	
	private static void thresholdDraw(int value) throws FileNotFoundException, IOException{
		
		mapRasterTemp = (ShortDataBank)PGMFormat.loadPGM(new FileInputStream(new File("C:\\prominence\\dmv0125.pgm")));
		
		for(long x = offsetX; x < offsetX + dimensionsX; x++){
			for(long y = offsetY; y < offsetY + dimensionsY; y++){
				
				int h = map.getShortValue(x, y) & 0xFFFF;
				
				if(h <= value) mapRasterTemp.setShortValue(x, y, (short)0);
				else mapRasterTemp.setShortValue(x, y, (short)-1);
				
			}
		}
		
		System.out.println("Painting threshold level to output.pgm");
		
		OutputStream os = new BufferedOutputStream(new FileOutputStream(new File("C:\\prominence\\output.pgm")), 32*1024*1024);
		try {
			PGMFormat.savePGM(os, mapRasterTemp);
		} finally {
			os.close();
		}
	}
	
	private static void contour(int value){
		for(long x = offsetX; x < offsetX + dimensionsX; x++){
			for(long y = offsetY; y < offsetY + dimensionsY; y++){
				
				int h = mapRasterTemp.getShortValue(x, y) & 0xFFFF;
				
				if(h < value || h > value) mapRasterTemp.setShortValue(x, y, (short)0);
				else mapRasterTemp.setShortValue(x, y, (short)-1);
				
			}
		}
	}
	
	private static void contourRange(int low, int high){
		for(long x = offsetX; x < offsetX + dimensionsX; x++){
			for(long y = offsetY; y < offsetY + dimensionsY; y++){
				
				int h = mapRasterTemp.getShortValue(x, y) & 0xFFFF;
				
				if(h == 65535) continue;
				else if(h < low || h > high) mapRasterTemp.setShortValue(x, y, (short)0);
				else mapRasterTemp.setShortValue(x, y, (short)-1);
				
			}
		}
	}
}
