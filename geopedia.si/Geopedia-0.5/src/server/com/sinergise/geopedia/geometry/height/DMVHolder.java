package com.sinergise.geopedia.geometry.height;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.util.io.BinaryInputStream;
import com.sinergise.util.io.BinaryOutputStream;

public class DMVHolder
{
	private static final Logger logger = LoggerFactory.getLogger(DMVHolder.class);
	
    /**
     * @param outFile
     * @param dmv
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void saveRaw(File outFile, DMVHolder dmv)
            throws FileNotFoundException, IOException {
        BinaryOutputStream bos = new BinaryOutputStream(new FileOutputStream(outFile));
        bos.writeLong(dmv.minx);
        bos.writeLong(dmv.miny);
        
        int w = (int) (dmv.maxx-dmv.minx+1);
        int h = (int) (dmv.maxy-dmv.miny+1);
        bos.writeInt(w);
        bos.writeInt(h);
        
        for (int y=0; y<h; y++)
            for (int x=0; x<w; x++)
                bos.writeShort(dmv.get(dmv.minx + x, dmv.miny + y));
        
        bos.close();
    }
    
	public double step;
	public long minx, maxx, miny, maxy;
	
	public short minz = Short.MAX_VALUE;
	public short maxz = Short.MIN_VALUE;
	
	public short[][] data;
	
	public DMVHolder(double step)
	{
		this.step = step;
	}
	
	public void add(long x, long y, short value)
	{
		if (data == null) {
			data = new short[][] { { value } };
			minx = maxx = x;
			miny = maxy = y;

			return;
		}
		
		if (x < minx || x > maxx) {
			long newminx = x < minx ? x : minx;
			long newmaxx = x > maxx ? x : maxx;
			int neww = (int) (newmaxx - newminx + 1);
			for (int a=0; a<data.length; a++) {
				short[] newrow = new short[neww];
				for (long tx = newminx; tx < minx; tx++)
					newrow[(int) (tx - newminx)] = Short.MIN_VALUE;
				for (long tx = maxx+1; tx <= newmaxx; tx++)
					newrow[(int) (tx - newminx)] = Short.MIN_VALUE;
				System.arraycopy(data[a], 0, newrow, (int)(minx - newminx), data[a].length);
				data[a] = newrow;
			}
			
			System.out.println("Realloc width: "+(maxx-minx+1)+" => "+(newmaxx-newminx+1));
			
			minx = newminx;
			maxx = newmaxx;
		}
		if (y < miny || y > maxy) {
			long newminy = y < miny ? y : miny;
			long newmaxy = y > maxy ? y : maxy;
			int newh = (int) (newmaxy - newminy + 1);
			short[][] newdata = new short[newh][];
			System.arraycopy(data, 0, newdata, (int)(miny - newminy), data.length);
			for (long ty = newminy; ty < miny; ty++) {
				short[] newrow = new short[(int) (maxx - minx + 1)];
				Arrays.fill(newrow, Short.MIN_VALUE);
				newdata[(int) (ty - newminy)] = newrow;
			}
			for (long ty = maxy+1; ty <= newmaxy; ty++) {
				short[] newrow = new short[(int) (maxx - minx + 1)];
				Arrays.fill(newrow, Short.MIN_VALUE);
				newdata[(int) (ty - newminy)] = newrow;
			}

			System.out.println("Realloc height: "+(maxy-miny+1)+" => "+(newmaxy-newminy+1));

			data = newdata;
			miny = newminy;
			maxy = newmaxy;
		}
		
		data[(int) (y-miny)][(int) (x-minx)] = value;
	}

	public short get(long x, long y)
    {
		return data[(int) (y-miny)][(int)(x-minx)];
    }

	public DMVHolder subset(long copyMinX, long copyMaxX, long copyMinY, long copyMaxY)
    {
		if (copyMaxY < miny) return null;
		if (copyMaxX < minx) return null;
		if (copyMinY > maxy) return null;
		if (copyMinX > maxx) return null;
		
		if (copyMinX < minx) copyMinX = minx;
		if (copyMaxX > maxx) copyMaxX = maxx;
		if (copyMinY < miny) copyMinY = miny;
		if (copyMaxY > maxy) copyMaxY = maxy;
		
		int nh = (int) (copyMaxY - copyMinY + 1);
		int nw = (int) (copyMaxX - copyMinX + 1);
		
		DMVHolder out = new DMVHolder(step);
		
		out.minx = copyMinX;
		out.maxx = copyMaxX;
		out.miny = copyMinY;
		out.maxy = copyMaxY;
		
		out.data = new short[nh][nw];
		for (int y=0; y<nh; y++) {
			for (int x=0; x<nw; x++) {
				out.data[y][x] = data[(int) (copyMinY + y - miny)][(int)(copyMinX + x - minx)];
				
				out.minz = out.data[y][x] < out.minz ? out.data[y][x] : out.minz;
				out.maxz = out.data[y][x] > out.maxz ? out.data[y][x] : out.maxz;
			}
		}
		
		return out;
    }

    /**
     * @param dataFile
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static DMVHolder loadRaw(String dataFile, double step) throws FileNotFoundException, IOException {
        File f = new File(dataFile);
        BinaryInputStream bis = new BinaryInputStream(new BufferedInputStream(new FileInputStream(f)));

        long lminx = bis.readLong();
        long lminy = bis.readLong();
        int w = bis.readInt();
        int h = bis.readInt();
        
        DMVHolder data = new DMVHolder(step);
        data.add(lminx, lminy, Short.MIN_VALUE);
        data.add(lminx+w-1, lminy+h-1, Short.MIN_VALUE);
        
    	byte[] tmp = new byte[w*2];
        for (int a=0; a<h; a++) {
        	bis.readFully(tmp);
        	short[] s = data.data[a];
        	int pos = 0;
        	for (int b=0; b<w; b++) {
        		s[b] = (short)(((tmp[pos++] & 255) << 8) | (tmp[pos++] & 255));
        		
        		data.minz = s[b] < data.minz ? (s[b] == Short.MIN_VALUE ? data.minz : s[b]) : data.minz;
        		data.maxz = s[b] > data.maxz ? s[b] : data.maxz;
        	}
        }
        return data;
	        
        
    }
	
    //this was taken from a file describing the coordinate system in geopedia (I dont know anymore where it is... so if someone knows where it is and put it here FIXME)
	public static final int minScale = 7;
	public static final double txPxSizeAtMinScale = 1024;
	public static final double dmrStepAtMinScale = 3200;
	
	public DMVHolder subsample(long copyMinX, long copyMinY, int scale, int outWidthSamples, int outHeightSamples)
    {
		logger.debug("copyMinX:"+copyMinX+" copyMinY:"+copyMinY+" scale:"+scale+" outWidthSamples:"+outWidthSamples+" outHeightSamples:"+outHeightSamples);
		
		if (copyMinY > maxy) return null;
		if (copyMinX > maxx) return null;
		
		if (copyMinX < minx) copyMinX = minx;
		if (copyMinY < miny) copyMinY = miny;
		
		double originalStep = step;//geopedia->12.5
		
		//adapting the steps to new scale
		//one sample is eq to dmrStep in meters in the new scale 
		double dmrStep = ( scale == minScale ? dmrStepAtMinScale : Math.max(originalStep, dmrStepAtMinScale / (1 << (scale - minScale))) );//in raster unit
		
		int nAvg = (int) Math.round(dmrStep / originalStep);
		
		DMVHolder out = new DMVHolder(dmrStep);
		
		
		out.data = new short[outHeightSamples][outWidthSamples];
		
		double currentHeight = 0;//average
		int outX = 0;
		int outY = 0;
		int x = 0;
		int y = 0;
		double maxHeight = Double.MIN_VALUE;
		double minHeight = Double.MAX_VALUE;
		int dmrwidth = (int) (maxx-minx+1);
		int dmrheight = (int) (maxy-miny+1);
		
		for (; outY < outHeightSamples && outX < outWidthSamples; y++) {
			
			if(y != 0 && ((y % nAvg == 0) || (copyMinY + y - miny) == dmrheight)){
				//getting here means I finished sampling one square
				//calculate average. I have to account that it might not be the same the same size as before (like when we get to the end of the map) 
				int numberofcellsX = (outX*nAvg)+nAvg-1 > dmrwidth ? (outX*nAvg)+nAvg-1-dmrwidth : nAvg;
				int numberofcellsY = (outY*nAvg)+nAvg-1 > dmrheight ? (outY*nAvg)+nAvg-1-dmrheight : nAvg;
				int numberofcells = (numberofcellsX)*(numberofcellsY);
				//int numberofcells = (nAvg)*(nAvg);
				
				currentHeight = Math.round(currentHeight / (numberofcells));
				maxHeight = (maxHeight < currentHeight ? currentHeight : maxHeight);
				minHeight = (minHeight > currentHeight ? currentHeight : minHeight);
				
				out.data[outY][outX] = (short) currentHeight;
				currentHeight = 0;
				
				outX++;
				if(outX == outWidthSamples){
					//here i finished sampling nAvg*nAvg squares on the original DMV
					if((copyMinY + y - miny) == dmrheight)
						break;
					outY++;//go up one line 
					outX = 0;//from the beginning
				}
				x = outX * nAvg;
				y = outY * nAvg;
			}
			
			
			for (; outX < outWidthSamples; x++) {
				short[] xarray = data[(int)(copyMinY + y - miny)];
				currentHeight += xarray[(int)(copyMinX + x - minx)];
				if((x+1) % nAvg == 0 || (copyMinX + x+1 - minx) == dmrwidth){
					x = outX * nAvg;//reset to the start again from the beginning
					break;
				}
			}
		}
		
		out.minx = copyMinX;
		out.maxx = out.minx+(outWidthSamples*nAvg);
		out.miny = copyMinY;
		out.maxy = out.miny+(outHeightSamples*nAvg);
		out.maxz = (short)Math.round(maxHeight);
		out.minz = (short)Math.round(minHeight);
		
		
		return out;
    }

	@Override
	public String toString() {
		return "DMVHolder [step=" + step + ", minx=" + minx + ", maxx=" + maxx
				+ ", miny=" + miny + ", maxy=" + maxy + ", minz=" + minz
				+ ", maxz=" + maxz + "]";
	}
	
	
	    
}
