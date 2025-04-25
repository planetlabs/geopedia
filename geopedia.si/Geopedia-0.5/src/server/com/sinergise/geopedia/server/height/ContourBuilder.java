/*
 *
 * Created on 5 August 2001, 15:03
 *
 * Copyright (c) 1996-1997 Nicholas Yue
 *
 * This software is copyrighted by Nicholas Yue. This code is base on the work of
 * Paul D. Bourke CONREC.F routine
 *
 * The authors hereby grant permission to use, copy, and distribute this
 * software and its documentation for any purpose, provided that existing
 * copyright notices are retained in all copies and that this notice is included
 * verbatim in any distributions. Additionally, the authors grant permission to
 * modify this software and its documentation for any purpose, provided that
 * such modifications are not distributed without the explicit consent of the
 * authors and that existing copyright notices are retained in all copies. Some
 * of the algorithms implemented by this software are patented, observe all
 * applicable patent law.
 *
 * IN NO EVENT SHALL THE AUTHORS OR DISTRIBUTORS BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE, ITS DOCUMENTATION, OR ANY DERIVATIVES THEREOF,
 * EVEN IF THE AUTHORS HAVE BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE AUTHORS AND DISTRIBUTORS SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, AND NON-INFRINGEMENT.  THIS SOFTWARE IS PROVIDED ON AN
 * "AS IS" BASIS, AND THE AUTHORS AND DISTRIBUTORS HAVE NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */

package com.sinergise.geopedia.server.height;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.util.collections.BitSet2D;
import com.sinergise.geopedia.geometry.height.DMVHolder;
/**
 * Conrec a straightforward method of contouring some surface represented a regular 
 * triangular mesh. 
 *
 * Ported from the C++ code by Nicholas Yue (see above copyright notice).
 * @see  http://paulbourke.net/papers/conrec/ for full description
 * of code and original C++ source.
 *
 * @author  Bradley White
 * @version 1.0 
 */

public class ContourBuilder{
	
	private final Logger logger = LoggerFactory.getLogger(ContourBuilder.class);

    private double  []  h   =  new double [5];
    private int     []  sh  =  new int    [5];
    private double  []  xh  =  new double [5];
    private double  []  yh  =  new double [5];
    
    private short [][] d;		// DMR data
    private double [] xCoords;	// array of x world coordinates
    private double [] yCoords;	// array of y world coordinates
    private int nc;				// number of contour levels
    private double [] z;		// contour levels in increasing order, in this case contains only one value
    private int ilb;			// i lower bound
    private int iub;			// i upper bound
    private int jlb;			// j lower bound
    private int jub;			// j upper bound
    
   	private Deque<DPoint> ring = new ArrayDeque<DPoint>(); 									// main ring
   	private ArrayList<Deque<DPoint>> orphan = new ArrayList<Deque<DPoint>>(); 				// orphan rings
   	private LinkedList<DPoint> ring2 = new LinkedList<DPoint>(); 							// copied main ring for performance
   	private LinkedList<LinkedList<DPoint>> orphan2 = new LinkedList<LinkedList<DPoint>>(); 	// copied orphan rings for performance
   	
   	private MultiLineString mls = null; // output MultiLineString
   	
   	private static BitSet2D hasContour; // for interior calculation
   	private static BitSet2D visited;	// visited matrix for iteration
   	private static BitSet2D down;		// four matrices for preventing duplicates
   	private static BitSet2D right;
   	private static BitSet2D up;
   	private static BitSet2D left;
   	
   	private static Queue<DMVPoint> queue = new LinkedList<DMVPoint>(); // queue for iteration
   	
   	private int envelopeMinX = Integer.MAX_VALUE; // envelope bounds, constrains interior for second pass
   	private int envelopeMaxX = 0;
   	private int envelopeMinY = Integer.MAX_VALUE;
   	private int envelopeMaxY = 0;
   	
   	private double resolution; // DMR properties
   	private static int dimensionsX;
   	private static int dimensionsY;
   	private int minX;
   	private int minY;
   	
   	private boolean interior = false; // if contouring interior or not
   	
	private int maxTolerance = 0; // simplification parameters
	private int nIterations = 0;
   	
    /** Creates new ContourBuilder
     * @param dmvHolder - hold the DMV data
     * @param x - start point x coordinate in pixels
     * @param y - start point y coordinate in pixels
     * @param iterations - number of iterations for simplification (10 recommended)
     * @param maxTolerance - tolerance level for simplification, higher simplifies more
     * */
	
	public ContourBuilder(DMVHolder dmvHolder, int x, int y, int iterations, int maxTolerance){
		this(dmvHolder, x, y);
		this.nIterations = iterations;
		this.maxTolerance = maxTolerance;
	}
   	
   	public ContourBuilder(DMVHolder dmvHolder, int x, int y){
   		resolution = dmvHolder.step;
		dimensionsX = (int) (dmvHolder.maxx - dmvHolder.minx + 1);
		dimensionsY = (int) (dmvHolder.maxy - dmvHolder.miny + 1);
		this.minX = (int) dmvHolder.minx;
		this.minY = (int) dmvHolder.miny;
		
		double[] contourLevel = {ContourBuilder.gauss(dmvHolder.data, x, y)};
		logger.info("gaussed Z = " + contourLevel[0]/10 + "m");
		
		this.xCoords = new double[dimensionsX];
		this.yCoords = new double[dimensionsY];
		
		for(int i = 0; i < dimensionsX; i++){
			xCoords[i] = (i + minX)*resolution;
		}
		for(int i = 0; i < dimensionsY; i++){
			yCoords[i] = (i + minY)*resolution;
		}
		
		this.d = dmvHolder.data;
		this.nc = 1;
		this.z = contourLevel;
		this.ilb = 0;
		this.iub = dimensionsX;
		this.jlb = 0;
		this.jub = dimensionsY;
		
		this.nIterations = 0;
		this.maxTolerance = 0;
		
		hasContour = new BitSet2D(dimensionsY, dimensionsX); // x and y are transposed
		visited = new BitSet2D(dimensionsY, dimensionsX);
		down = new BitSet2D(dimensionsY, dimensionsX);
		right = new BitSet2D(dimensionsY, dimensionsX);
		up = new BitSet2D(dimensionsY, dimensionsX);
		left = new BitSet2D(dimensionsY, dimensionsX);
		
    	queue.add(new DMVPoint(x,y));
    	queue.add(new DMVPoint(x - 1, y - 1)); //can go to -1, careful!
        queue.add(new DMVPoint(x - 1, y    ));
        queue.add(new DMVPoint(x    , y - 1));
   	}
    
    public void contour(){
    	if(z[0] <= 0) return;
    	logger.info("Starting first pass ...");
        contour(ilb, iub, jlb, jub);
        logger.info("... First pass done.");
        logger.info("Envelope: x = " + envelopeMinY + " - " + envelopeMaxY + ", y = " + envelopeMinX + " - " + envelopeMaxX);
        logger.info("Starting second pass - interior ... ");
        contourInterior();
        logger.info("... interior done.");
//        outputImage();
    	logger.info("Simplification started ...");
        simplify(nIterations);
        logger.info("... and complete.");
        createMultiLineStringResult();
        logger.info("Finished contour calculation.");
    }

	private void contour(int ilb, int iub, int jlb, int jub){
        int         m1;
        int         m2;
        int         m3;
        int         case_value;
        double      dmin;
        double      dmax;
        double      x1 = 0.0;
        double      x2 = 0.0;
        double      y1 = 0.0;
        double      y2 = 0.0;
        int         i,j,k,m;
        
        int     [] im   = {0,1,1,0};
        int     [] jm   = {0,0,1,1};
        
        int [][][] castab=
        {
            {
                {0,0,8},{0,2,5},{7,6,9}
            },
            {
                {0,3,4},{1,3,1},{4,3,0}
            },
            {
                {9,6,7},{5,2,0},{8,0,0}
            }
        };
        
        while(!queue.isEmpty()){
        	
        	DMVPoint p = queue.remove();
        	i = p.x;
        	j = p.y;
        	
        	if(i < 0 || j < 0 || i > dimensionsX - 2 || j > dimensionsY - 2){ // if outside data matrix
        		continue;
        	}
        	
        	if(visited.isSet(i,j)){ // if we've been here before
        		continue;
        	}
        	
            double temp1,temp2;
            double g1 = gauss(d, i, j);
            double g2 = gauss(d, i, j + 1);
            double g3 = gauss(d, i + 1, j);
            double g4 = gauss(d, i + 1, j + 1);
            
            temp1 = Math.min(g1, g2);
            temp2 = Math.min(g3, g4);
            dmin  = Math.min(temp1,temp2);
            temp1 = Math.max(g1, g2);
            temp2 = Math.max(g3, g4);
            dmax  = Math.max(temp1,temp2);
            
            visited.set(i,j);
            
            if (dmax >= z[0] && dmin <= z[nc-1]) {
                for (k = 0; k < nc; k++) {
                    if (z[k] >= dmin && z[k] <= dmax) {
                        for (m = 4; m >= 0; m--) {
                            if (m > 0) {
                            	h[m] = gauss(d, i + im[m-1], j + jm[m-1]) - z[k];
                                xh[m] = xCoords[i+im[m-1]];
                                yh[m] = yCoords[j+jm[m-1]];
                            } else {
                                h[0] = 0.25*(h[1]+h[2]+h[3]+h[4]);
                                xh[0] = 0.5*(xCoords[i]+xCoords[i+1]);
                                yh[0] = 0.5*(yCoords[j]+yCoords[j+1]);
                            }
                            if (h[m] > 0.0) {
                                sh[m] = 1;
                            } else if (h[m] < 0.0) {
                                sh[m] = -1;
                            } else
                                sh[m] = 0;
                        }
                        
                        if(h[1] == 0 && h[2] == 0 && h[3] == 0 && h[4] == 0){
//                        	logger.info("FLATNESS");
                        	continue;
                        }
                        ArrayList<LineString> lineStringBlock = new ArrayList<LineString>(); // for storing all LineStrings in the 2x2 block
                        boolean draw_contour = false;
                        
                        for (m = 1; m <= 4; m++) {
                            m1 = m;
                            m2 = 0;
                            if (m!=4) {
                                m3 = m+1;
                            } else {
                                m3 = 1;
                            }
                            boolean duplicate = false;
                            case_value = castab[sh[m1]+1][sh[m2]+1][sh[m3]+1];
                            if (case_value!=0) {
                                switch (case_value) {
                                    case 1: // Line between vertices 1 and 2
                                        x1=xh[m1];
                                        y1=yh[m1];
                                        x2=xh[m2];
                                        y2=yh[m2];
                                        break;
                                    case 2: // Line between vertices 2 and 3
                                        x1=xh[m2];
                                        y1=yh[m2];
                                        x2=xh[m3];
                                        y2=yh[m3];
                                        break;
                                    case 3: // Line between vertices 3 and 1
                                        x1=xh[m3];
                                        y1=yh[m3];
                                        x2=xh[m1];
                                        y2=yh[m1];
                                    	if(m == 1){
                                    		if(down.isSet(i,j)){
                                    			duplicate = true;
                                    			break;
                                    		}
                                    		up.set(i, j-1);
                                    	}
                                    	else if(m == 2){
                                    		if(right.isSet(i,j)){
                                    			duplicate = true;
                                    			break;
                                    		}
                                    		left.set(i+1,j);
                                    	}
                                    	else if(m == 3){
                                    		if(up.isSet(i,j)){
                                    			duplicate = true;
                                    			break;
                                    		}
                                    		down.set(i, j+1);
                                    	}
                                    	else if(m == 4){
                                    		if(left.isSet(i,j)){
                                    			duplicate = true;
                                    			break;
                                    		}
                                    		right.set(i-1,j);
                                    	}
                                        break;
                                    case 4: // Line between vertex 1 and side 2-3
                                        x1=xh[m1];
                                        y1=yh[m1];
                                        x2=xsect(m2,m3);
                                        y2=ysect(m2,m3);
                                        break;
                                    case 5: // Line between vertex 2 and side 3-1
                                        x1=xh[m2];
                                        y1=yh[m2];
                                        x2=xsect(m3,m1);
                                        y2=ysect(m3,m1);
                                        break;
                                    case 6: //  Line between vertex 3 and side 1-2
                                        x1=xh[m3];
                                        y1=yh[m3];
                                        x2=xsect(m1,m2);
                                        y2=ysect(m1,m2);
                                        break;
                                    case 7: // Line between sides 1-2 and 2-3
                                        x1=xsect(m1,m2);
                                        y1=ysect(m1,m2);
                                        x2=xsect(m2,m3);
                                        y2=ysect(m2,m3);
                                        break;
                                    case 8: // Line between sides 2-3 and 3-1
                                        x1=xsect(m2,m3);
                                        y1=ysect(m2,m3);
                                        x2=xsect(m3,m1);
                                        y2=ysect(m3,m1);
                                        break;
                                    case 9: // Line between sides 3-1 and 1-2
                                        x1=xsect(m3,m1);
                                        y1=ysect(m3,m1);
                                        x2=xsect(m1,m2);
                                        y2=ysect(m1,m2);
                                        break;
                                    default:
                                        break;
                                }
                                
                                if(i < envelopeMinX) envelopeMinX = i;
                                if(i > envelopeMaxX) envelopeMaxX = i;
                                if(j < envelopeMinY) envelopeMinY = j;
                                if(j > envelopeMaxY) envelopeMaxY = j;
                                
                                if(!duplicate){ // if this is not a duplicate, add it and draw it
	                                double[] coordinates = {x1,y1,x2,y2};
	                                lineStringBlock.add(new LineString(coordinates));
	                                draw_contour = true;
	                                hasContour.set((int)(x1/resolution - minX), (int)(y1/resolution - minY));
	                                hasContour.set((int)(x2/resolution - minX), (int)(y2/resolution - minY));
                                }
                            }
                        }
                        
                        if(draw_contour){
                        	glueLineStrings(lineStringBlock);
                        	
                        	queue.add(new DMVPoint(i + 1, j + 1));
                        	queue.add(new DMVPoint(  i  , j + 1));
                        	queue.add(new DMVPoint(i - 1, j + 1));
                        	queue.add(new DMVPoint(i + 1,   j  ));
                        	queue.add(new DMVPoint(i - 1,   j  ));
                        	queue.add(new DMVPoint(i + 1, j - 1));
                        	queue.add(new DMVPoint(  i  , j - 1));
	                        queue.add(new DMVPoint(i - 1, j - 1)); //can go to -1, careful!
                        }
                    }
                }
            }
        }
        queue.clear();
    }
    
	private void contourInterior(){
		interior = true;
    	double contourLevel = z[0];
    	for(int y = envelopeMinY; y < envelopeMaxY; y++){
    		int counter = 0;
    		boolean previouslyOnBoundary = false; // if we hit the boundary in the previous step
    		for(int x = envelopeMinX; x < envelopeMaxX; x++){
    			// this checks if we are in the interior; this makes sense because our goal is
    			// to show only land mass affected by "flooding"
    			if(!hasContour.isSet(x, y)){
            		if(previouslyOnBoundary){ // we exited the boundary pixels and were at the boundary in the previous step
            			// the following 'if' block prevents false positives (happens when the path is tangential to the boundary)
            			// note: the interior is defined by lower ground! degenerate cases such as extremely high flood levels
            			// (leaving most of the country under water) will not generate all island contours
            			short value = d[y][x];
            			if((counter % 2 == 0 && value < contourLevel) || (counter % 2 == 1 && value > contourLevel)){
        					counter++;
        				}
        				previouslyOnBoundary = false;
            		}
            		if(counter % 2 == 1){ // interior reached
            			if(d[y][x] >= contourLevel){
	            	    	queue.add(new DMVPoint(  x  ,   y  ));
	            	    	queue.add(new DMVPoint(x - 1, y - 1)); //can go to -1, careful!
	            	        queue.add(new DMVPoint(x - 1,   y  ));
	            	        queue.add(new DMVPoint(  x  , y - 1));
	            	        contour(envelopeMinX, envelopeMaxX, envelopeMinY, envelopeMaxY);
	            			previouslyOnBoundary = true; // this point is now on the boundary
	            		}
	            	}
            	} else previouslyOnBoundary = true;
            }
        }
		repackRingAndOrphan();
    }
    
	private void glueLineStrings(ArrayList<LineString> lls){ // connects LineStrings in the 2x2 block. attempts to connect them with first:
		nukeDuplicates(lls);								 // the ring, then orphans, then if that fails get added as a new orphan
		
		Deque<DPoint> points = new ArrayDeque<DPoint>();
		boolean[] wasConnected = new boolean[lls.size()];
		
		double startXLocal = lls.get(0).getX(0);
		double startYLocal = lls.get(0).getY(0);
		double endXLocal = lls.get(0).getX(1);
		double endYLocal = lls.get(0).getY(1);
		
		points.addLast(new DPoint(startXLocal, startYLocal));
		points.addLast(new DPoint(endXLocal, endYLocal));
		
		wasConnected[0] = true;
		
		for(int i = 1; i < lls.size(); i++){
    		for(int j = 1; j < lls.size(); j++){ // can start from 1 since wasConnected[0] == true by definition
    			
    			if(wasConnected[j]) continue;
    			
    			LineString lls1 = lls.get(j);
    			double startX1 = lls1.getX(0);
        		double startY1 = lls1.getY(0);
        		double endX1 = lls1.getX(1);
        		double endY1 = lls1.getY(1);
        		
        		if(startX1 == endXLocal && startY1 == endYLocal){ // start(this) = end(previous)
        			points.addLast(new DPoint(endX1, endY1));
        			endXLocal = endX1;
        			endYLocal = endY1;
        			wasConnected[j] = true;
        		}
        		else if(startX1 == startXLocal && startY1 == startYLocal){ // start(this) = start(previous)
        			points.addFirst(new DPoint(endX1, endY1));
        			startXLocal = endX1;
        			startYLocal = endY1;
        			wasConnected[j] = true;
        		}
        		else if(endX1 == endXLocal && endY1 == endYLocal){ // end(this) = end(previous)
        			points.addLast(new DPoint(startX1, startY1));
        			endXLocal = startX1;
        			endYLocal = startY1;
        			wasConnected[j] = true;
        		}
        		else if(endX1 == startXLocal && endY1 == startYLocal){// end(this) = start(previous)
        			points.addFirst(new DPoint(startX1, startY1));
        			startXLocal = startX1;
        			startYLocal = startY1;
        			wasConnected[j] = true;
        		}
    		}
		}
		
		ArrayList<LineString> points2 = new ArrayList<LineString>();
		for(int i = 0; i < wasConnected.length; i++){
			if(!wasConnected[i]){ //
				LineString lsTmp = lls.get(i);
				points2.add(lsTmp);
			}
		}
		if(!points2.isEmpty()){
			glueLineStrings(points2);
		}
		if(interior){ // if we are calculating the interior we cannot connect with the main ring, so skip it
			if(connectWithOrphanRings(points)){ // if we can connect with orphan rings, do so
				return;
			}
			else orphan.add(points); // otherwise this is a new orphan ring
			return;
		}
		if(connectWithMainRing(points)){ // if we can connect with the main ring, do so
			tryConnectingOrphanRingsWithMainRing(); // and see if the added point fixes something
			return;
		} // no connections = orphan rings
		else if(connectWithOrphanRings(points)){ // if we can connect with orphan rings, do so
			return;
		}
		orphan.add(points); // otherwise this is a new orphan ring
	}
	
	private void nukeDuplicates(ArrayList<LineString> lls) { // gets rid of duplicate LineStrings
		for(int i = 0; i < lls.size(); i++){
			for(int j = i + 1; j != i && j < lls.size(); j++){
				LineString lls0 = lls.get(i);
				LineString lls1 = lls.get(j);
				if(lls0.equals(lls1)){
					lls.remove(j);
					continue;
				}
				if(lls0.getX(0) == lls1.getX(1) && lls0.getY(0) == lls1.getY(1)
						&& lls0.getX(1) == lls1.getX(0) && lls0.getY(1) == lls1.getY(0)){ // in case lls1 is lls0 mirrored
					lls.remove(j);
					continue;
				}
			}
		}
	}

	private boolean connectWithMainRing(Deque<DPoint> points){ // tries to connect the LineString with the main ring
		if(ring.isEmpty()){
			while(!points.isEmpty()){
				ring.addLast(points.removeFirst());
			}
			return true;
		}
		
		DPoint startRing = ring.getFirst();
		DPoint endRing = ring.getLast();
		
		DPoint startPoints = points.getFirst();
		DPoint endPoints = points.getLast();
		
		if(startPoints.equals(endRing)){ // start(points) = end(ring)
			points.removeFirst(); // remove first to prevent duplicates
			while(!points.isEmpty()){
				ring.addLast(points.removeFirst());
			}
			return true;
		}
		else if(endPoints.equals(endRing)){ // end(points) = end(ring)
			points.removeLast();
			while(!points.isEmpty()){
				ring.addLast(points.removeLast());
			}
			return true;
		}
		else if(startPoints.equals(startRing)){ // start(points) = start(ring)3
			points.removeFirst();
			while(!points.isEmpty()){
				ring.addFirst(points.removeFirst());
			}
			return true;
		}
		else if(endPoints.equals(startRing)){ //end(points) = start(ring)
			points.removeLast();
			while(!points.isEmpty()){
				ring.addFirst(points.removeLast());
			}
			return true;
		}
		return false;
	}
	
	private boolean connectWithOrphanRings(Deque<DPoint> points){ // tries to connect the LineString with orphans
		return connectWithOrphanRings(points, 0, false);
	}
	
	private boolean connectWithOrphanRings(Deque<DPoint> points, int startIndex, boolean connected){
		int size = orphan.size();
		if(size == 0) return false;
		DPoint startPoints = points.getFirst();
		DPoint endPoints = points.getLast();
		for(int i = startIndex + 1; i <= size; i ++){
			Deque<DPoint> o = orphan.get(size - i);
			if(points.equals(o)){
				continue;
			}
			DPoint startOrphan = o.getFirst();
			DPoint endOrphan = o.getLast();
			if(startOrphan.equals(endOrphan)){ // if orphan is closed ...
				LinkedList<DPoint> tmp = new LinkedList<DPoint>();
				for(DPoint p : o){
					tmp.add(p);
				}
				orphan2.add(tmp); // ... then it is finished and can be moved to orphan2
				orphan.remove(size - i);
				continue;
			}
			if(startPoints.equals(endOrphan)){ // start(points) = end(ring)
				points.removeFirst(); // remove first to prevent duplicates
				while(!points.isEmpty()){
					o.addLast(points.removeFirst());
				}
				if(!connected){ // check if first or second iteration. in first iteration (connected = false) try connecting a second time
								// and if successful, remove the orphan (since it was added to another orphan)
					if(connectWithOrphanRings(o, i, true)){
						orphan.remove(size - i);
					}
				}
				return true; // return after second iteration
			}
			if(endPoints.equals(endOrphan)){ // end(points) = end(ring)
				points.removeLast();
				while(!points.isEmpty()){
					o.addLast(points.removeLast());
				}
				if(!connected){
					if(connectWithOrphanRings(o, i, true)){
						orphan.remove(size - i);
					}
				}
				return true;
			}
			if(startPoints.equals(startOrphan)){ // start(points) = start(ring)
				points.removeFirst();
				while(!points.isEmpty()){
					o.addFirst(points.removeFirst());
				}
				if(!connected){
					if(connectWithOrphanRings(o, i, true)){
						orphan.remove(size - i);
					}
				}
				return true;
			}
			if(endPoints.equals(startOrphan)){ //end(points) = start(ring)
				points.removeLast();
				while(!points.isEmpty()){
					o.addFirst(points.removeLast());
				}
				if(!connected){
					if(connectWithOrphanRings(o, i, true)){
						orphan.remove(size - i);
					}
				}
				return true;
			}
		}
		return false;
	}
	
	private void tryConnectingOrphanRingsWithMainRing(){ // if an orphan can be connected with the main ring, do so then delete it
		for (Iterator<Deque<DPoint>> iterator = orphan.iterator(); iterator.hasNext(); ) {
			Deque<DPoint> o = iterator.next();
			if(connectWithMainRing(o)){
				iterator.remove();
			}
		}
	}
	
	private void repackRingAndOrphan() { // move ring and orphan into different objects for much improved performance
		for(DPoint p : ring){			 // in simplifying (O(n) instead of O(n^2))
			ring2.add(p);
		}
		ring.clear();
		for(Deque<DPoint> o : orphan){
			LinkedList<DPoint> tmp = new LinkedList<DPoint>();
			for(DPoint p : o){
				tmp.add(p);
			}
			orphan2.add(tmp);
			o.clear();
		}
		orphan.clear();
	}
	
	private void calculateTriangleAreas(){ // calculates triangle areas for simplification and removes colinear points
		DPoint beforeA;
		DPoint a;
		DPoint b;
		DPoint c;
		if(ring2.size() >= 3){
			a = ring2.removeFirst(); 
			beforeA = new DPoint(a.x, a.y);
			b = ring2.removeFirst();
			c = ring2.getFirst();
			b.area = calculateTriangleArea(a, b, c);
			ring2.addFirst(b);
			ring2.addFirst(a);
			
			for (Iterator<DPoint> iterator = ring2.iterator(); iterator.hasNext(); ) {
				beforeA = a;
				a = b;
				b = c;
				c = iterator.next();
				b.area = calculateTriangleArea(a, b, c);
				if(b.area == 0){ // recalculate area of a (we will recalculate c later)
					while(b.area == 0 && ring2.size() >= 3){
						a.area = calculateTriangleArea(beforeA, a, c);
						b.x = c.x;
						b.y = c.y;
						b.area = c.area;
						iterator.remove();
						if(iterator.hasNext()){
							c = iterator.next();
							b.area = calculateTriangleArea(a, b, c);
						}
					}
				}
			}
		}
		for (Iterator<LinkedList<DPoint>> iterator = orphan2.iterator(); iterator.hasNext(); ) {
			LinkedList<DPoint> o = iterator.next();
			if(o.size() < 3) continue;
			a = o.removeFirst();
			beforeA = new DPoint(a.x, a.y);
			b = o.removeFirst();
			c = o.getFirst();
			b.area = calculateTriangleArea(a, b, c);
			o.addFirst(b);
			o.addFirst(a);
			for (Iterator<DPoint> innerIterator = o.iterator(); innerIterator.hasNext(); ){
				a = b;
				b = c;
				c = innerIterator.next();
				b.area = calculateTriangleArea(a, b, c);
				if(b.area == 0){
					while(b.area == 0 && o.size() >= 3){
						a.area = calculateTriangleArea(beforeA, a, c);
						b.x = c.x;
						b.y = c.y;
						b.area = c.area;
						innerIterator.remove();
						if(innerIterator.hasNext()){
							c = innerIterator.next();
							b.area = calculateTriangleArea(a, b, c);
						}
					}
				}
			}
		}
	}
	
	private float calculateTriangleArea(DPoint a, DPoint b, DPoint c){
		return (float)(Math.abs((a.x - c.x)*(b.y -a.y) - (a.x - b.x)*(c.y - a.y)));
	}
	
	private void simplify(int iterations){ // simplifies the geometry before output. simplification depends mostly on maxTolerance
		if(iterations == 0) return;
		int tolerance = maxTolerance - (iterations - 1)*(maxTolerance)/nIterations; // progressive tolerance levels, helps accuracy
    	calculateTriangleAreas();
		
		int counter = 0;
		float area;
		
		for (Iterator<DPoint> iterator = ring2.iterator(); iterator.hasNext(); ) {
			if(ring2.size() < 3) break;
    		if(counter++ %2 == 0){
    			iterator.next();
    			continue;
    		}
    		area = iterator.next().area;
    		if(area < tolerance){ // if area is small (hence point insignificant) remove the point
				iterator.remove();
				continue;
			}
		}
		for (Iterator<LinkedList<DPoint>> iterator = orphan2.iterator(); iterator.hasNext(); ) {
			LinkedList<DPoint> o = iterator.next();
			if(o.size() < 3) continue;
	    	counter = 0;
	    	for (Iterator<DPoint> iteratorInner = o.iterator(); iteratorInner.hasNext(); ) {
	    		if(o.size() < 3){
	    			break;
	    		}
	    		if(counter++ %2 == 0){
	    			iteratorInner.next();
	    			continue;
	    		}
	    		area = iteratorInner.next().area;
	    		if(area < tolerance){
					iteratorInner.remove();
					continue;
				}
	    	}
		}
		simplify(iterations - 1);
	}
	
    private void createMultiLineStringResult() { // creates LineString output rounded to two decimal places
       	LineString[] ls = new LineString[orphan2.size() + 1];
    	double[] coords = new double[2*ring2.size()];
    	int i = 0;
    	for(DPoint p : ring2){
			coords[i++] = Math.round(p.x * 100)/100.0;
			coords[i++] = Math.round(p.y * 100)/100.0;
    	}
    	ls[0] = new LineString(coords);
    	int n = 1;
    	for(Deque<DPoint> o : orphan2){
    		coords = new double[2*o.size()];
    		i = 0;
    		for(DPoint p : o){
    			coords[i++] = Math.round(p.x * 100)/100.0;
    			coords[i++] = Math.round(p.y * 100)/100.0;
    		}
    		ls[n++] = new LineString(coords);
    	}
    	mls = new MultiLineString(ls);
    }
    
    public MultiLineString getMultiLineStringContour() {
    	return mls;
	}

	private double xsect(int p1, int p2){
        return    (h[p2]*xh[p1]-h[p1]*xh[p2])/(h[p2]-h[p1]);
    }
    
    private double ysect(int p1, int p2){
        return (h[p2]*yh[p1]-h[p1]*yh[p2])/(h[p2]-h[p1]);
    }
    
    public static double gauss(short[][] d, int x, int y){ // 2D discrete Gaussian kernel
    	
    	if(x <= 0 || x >= dimensionsX - 1|| y <= 0 || y >= dimensionsY - 1){
    		return 65536;
    	}
    	return  (	(d[y-1][x-1]&0xFFFF) +  2*(d[y][x-1]&0xFFFF) +   (d[y+1][x-1]&0xFFFF)
    			+ 2*(d[y-1][ x ]&0xFFFF) +  4*(d[y][ x ]&0xFFFF) + 2*(d[y+1][ x ]&0xFFFF)
    			+   (d[y-1][x+1]&0xFFFF) +  2*(d[y][x+1]&0xFFFF) +   (d[y+1][x+1]&0xFFFF))/16.0;
    }
    
	@SuppressWarnings("unused")
	private void outputImage(){
    	if(envelopeMaxX == 0) return; // don't bother outputting if nothing gets drawn
    	BufferedImage output = new BufferedImage(dimensionsX, dimensionsY, BufferedImage.TYPE_USHORT_GRAY);
    	for(int y = 0; y < dimensionsY; y++){
    		for(int x = 0; x < dimensionsX; x++){
    			if(hasContour.isSet(x, y)){
            		output.getRaster().setSample(x, y, 0, -1);
            	}
            	else output.getRaster().setSample(x, y, 0, d[y][x]);
            }
        }
        try {
        	String filename = "contour_" + System.currentTimeMillis() + ".png";
        	OutputStream outputfile = new BufferedOutputStream(new FileOutputStream(new File("C:\\" + filename)), 32*1024*1024);
			logger.info("Outputting image " + filename + " ... ");
        	ImageIO.write(output, "png", outputfile);
        	logger.info(" ... done");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private class DMVPoint {
    	int x;
    	int y;
    	
    	public DMVPoint(int x, int y){
    		this.x = x;
    		this.y = y;
    	}
    }
    
    private class DPoint{
    	double x;
    	double y;
    	float area = Float.MAX_VALUE;
    	
    	DPoint(double x, double y){
    		this.x = x;
    		this.y = y;
    	}
    	boolean equals(DPoint p){
	    	if (p == null)
	            return false;
	        if (p == this)
	            return true;
	        if (!(p instanceof DPoint))
	            return false;
	        if (p.x == this.x && p.y == this.y)
	        	return true;
	        return false;
    	}
    	public String toString(){
    		return "(" + this.x + ", " + this.y + ")";
    	}
    	public int hashCode(){
    		return (int)(x + y);
    	}
    }
}