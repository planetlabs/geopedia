package com.sinergise.geopedia.client.components.heightprofile;

import java.util.ArrayList;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.CanvasGradient;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.LineCap;
import com.google.gwt.canvas.dom.client.Context2d.TextAlign;
import com.google.gwt.canvas.dom.client.Context2d.TextBaseline;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sinergise.geopedia.core.service.result.FeatureHeightResult;

public class HeightProfilePanel extends FlowPanel{


	private Canvas canvas;
	private FeatureHeightResult featureHeightProfile = null;
	
	private static final int MARGIN_VERTICAL=10;
	private static final int MARGIN_HORIZONTAL=10;
	
	private static final int LABEL_PAD_HORIZONTAL=50;
	private static final int LABEL_PAD_VERTICAL=25;
	private static final int GRAPH_PADDING=2;
	private static final int HORIZONTAL_GUIDELINES_STEP = 40;
	private static final int VERTICAL_GUIDELINES_STEP = 80;


	
	public HeightProfilePanel() {
		addStyleName("heightProfile");
		canvas = Canvas.createIfSupported();
		add(canvas);
	}
	
	public void resize(int width, int height) {
		canvas.setCoordinateSpaceHeight(height);
		canvas.setCoordinateSpaceWidth(width);
		drawProfile();
	}
	public boolean preResize(int width, int height) {
		canvas.setCoordinateSpaceHeight((int)(height*0.95));
		canvas.setCoordinateSpaceWidth((int)(width*0.95));
		return true;
	}
	
	 private static String lenTextFor(double len, double totalLen) {
	        if (totalLen > 8000) {
	            long hundreds = Math.round(len / 100);
	            if (hundreds % 10 == 0) {
	                return (hundreds / 10) + " km";
	            } else {
	                return (hundreds / 10) + "," + (hundreds % 10) + " km";
	            }
	        } else if (totalLen > 8) {
	            long hundreds = Math.round(len * 10);
	            if (hundreds % 10 == 0) {
	                return (hundreds / 10) + " m";
	            } else {
	                return (hundreds / 10) + "," + (hundreds % 10) + "m";
	            }
	        } else if (totalLen > 0.08) {
	            long hundreds = Math.round(len * 1000);
	            if (hundreds % 10 == 0) {
	                return (hundreds / 10) + " cm";
	            } else {
	                return (hundreds / 10) + "," + (hundreds % 10) + " cm";
	            }
	        } else {
	            long hundreds = Math.round(len * 10000);
	            if (hundreds % 10 == 0) {
	                return (hundreds / 10) + " mm";
	            } else {
	                return (hundreds / 10) + "," + (hundreds % 10) + " mm";
	            }
	        }
	    }
	public void drawProfile() {
		Context2d ctx = canvas.getContext2d();
		int ctxWidth = canvas.getCoordinateSpaceWidth();
		int ctxHeight = canvas.getCoordinateSpaceHeight();
		ctx.clearRect(0,0,ctxWidth,ctxHeight);
		if (featureHeightProfile==null) {
			return;
		}
		
		
		ctx.setLineWidth(0.5);
		ctx.beginPath();
		ctx.moveTo(MARGIN_HORIZONTAL+LABEL_PAD_HORIZONTAL, MARGIN_VERTICAL);
		ctx.lineTo(MARGIN_HORIZONTAL+LABEL_PAD_HORIZONTAL, ctxHeight-MARGIN_VERTICAL-LABEL_PAD_VERTICAL);
		ctx.lineTo(ctxWidth-MARGIN_HORIZONTAL, ctxHeight-MARGIN_VERTICAL-LABEL_PAD_VERTICAL);
		ctx.stroke();
		
		
		ArrayList<Double> heights = featureHeightProfile.heights;
		int graphWidth = heights.size();
		int graphHeight = (int) Math.ceil(featureHeightProfile.maxHeight-featureHeightProfile.minHeight);
		
		double minZ = featureHeightProfile.minHeight;
		int graphCanvasWidth = ctxWidth-(MARGIN_HORIZONTAL*2+LABEL_PAD_HORIZONTAL+2*GRAPH_PADDING);
		int graphCanvasHeight = ctxHeight-(MARGIN_VERTICAL*2+LABEL_PAD_VERTICAL+2*GRAPH_PADDING);
		double sx = (double)(ctxWidth-(MARGIN_HORIZONTAL*2+LABEL_PAD_HORIZONTAL)) / (double) graphWidth;
		double sy = (double)graphCanvasHeight/ (double)graphHeight;
		
		ctx.setFont("11px Arial");
		ctx.setStrokeStyle(CssColor.make(170,170,170).value());
		ctx.setLineWidth(0.5); // draw horizontal guide lines

		double heightFactor = (featureHeightProfile.maxHeight-featureHeightProfile.minHeight)/graphCanvasHeight;
		ctx.save();
		ctx.setTextBaseline(TextBaseline.MIDDLE);
		for (int i=0;i<graphCanvasHeight;i+=40) {
			int y = (graphCanvasHeight+MARGIN_VERTICAL+GRAPH_PADDING)-i;
			int heightLabel =(int)(i*heightFactor+minZ); 
			ctx.setGlobalAlpha(.5);
			ctx.fillText(heightLabel+" m", MARGIN_HORIZONTAL, y);
			ctx.beginPath();
			ctx.setGlobalAlpha(1);
			ctx.moveTo(MARGIN_HORIZONTAL+LABEL_PAD_HORIZONTAL-4,y);
			ctx.lineTo(ctxWidth-MARGIN_HORIZONTAL, y);
			ctx.stroke();
		}
		ctx.restore();
		
		double totalLen = featureHeightProfile.projectedLength;
		double lengthStep = roundToDecimal(totalLen/(double)(graphCanvasWidth/VERTICAL_GUIDELINES_STEP));

		int vStep = (int) (graphCanvasWidth/totalLen*lengthStep);
		ctx.save();
		ctx.setTextAlign(TextAlign.CENTER);
		
		//risanje y-a
		double distance = 0;
		for (int i=0;i<graphCanvasWidth;i+=vStep) {
			int x=i+MARGIN_HORIZONTAL+LABEL_PAD_HORIZONTAL;
			ctx.setGlobalAlpha(.5);
			ctx.fillText(lenTextFor(distance, totalLen), x, ctxHeight-MARGIN_VERTICAL);
			ctx.setGlobalAlpha(1);
			distance+=lengthStep;
			if (i>0) {
				ctx.beginPath();
				ctx.moveTo(x,ctxHeight-MARGIN_VERTICAL-LABEL_PAD_VERTICAL+4);
				ctx.lineTo(x, MARGIN_VERTICAL);
				ctx.stroke();
			}

		}
		ctx.restore();
		ctx.save();
		ctx.translate(MARGIN_HORIZONTAL+LABEL_PAD_HORIZONTAL+GRAPH_PADDING, MARGIN_VERTICAL);
		ctx.setLineWidth(2);
		ctx.setStrokeStyle(CssColor.make(0, 146, 237).value());
		ctx.setLineCap(LineCap.ROUND);
		ctx.beginPath();
		
		int nPoints = heights.size();
		if (nPoints >= 3*graphCanvasWidth) {
			double pointRes = (double)nPoints/(double)(graphCanvasWidth+1);
			for (int i=0;i<graphCanvasWidth;i++) {
				int p1=(int)(i*pointRes);
				int p2=(int)((i+1)*pointRes);
				if (p1>nPoints) p1=nPoints;
				if (p2>nPoints) p2=nPoints;
				double zMin = Double.MAX_VALUE;
				double zMax = Double.MIN_VALUE;
				for (int j=p1;j<p2;j++) {
					double z = heights.get(j);
					if (z<zMin)zMin=z;
					if (z>zMax)zMax=z;
				}
				double z=(zMin+zMax)/2;
				double y = (graphHeight-(z - minZ))*sy;				
				if (i == 0) {
					ctx.moveTo(i, y);
				} else {
					ctx.lineTo(i, y);
				}

			}
			
		} else {
			for (int i = 0; i < heights.size(); i++) {
				double z = heights.get(i);
				double y = (graphHeight-(z - minZ))*sy;
				double x  = i*sx; 
				if (i == 0) {
					ctx.moveTo(x, y);
				} else {
					ctx.lineTo(x, y);
				}
			}
		}
		ctx.stroke();
		ctx.setLineWidth(1);
		ctx.lineTo(heights.size()*sx, (graphHeight)*sy);
		ctx.lineTo(0, graphHeight*sy);
		ctx.closePath();
		CanvasGradient grd = ctx.createLinearGradient(graphWidth*sx/2, 0, graphWidth*sx/2, graphHeight*sy);
		grd.addColorStop(0, CssColor.make(0,146,237).value());
		grd.addColorStop(1, CssColor.make(255,255,255).value());
		ctx.setGlobalAlpha(0.3);
		ctx.setFillStyle(grd);
		ctx.fill();
		ctx.setGlobalAlpha(1);
		ctx.restore();
	}
	

	   static double roundToDecimal(double approxStep) {
	        double dec = Math.pow(10, Math.floor(Math.log10(approxStep)));
	        return dec * Math.round(approxStep / dec);
	    }

	public void setProfileData(FeatureHeightResult profile) {
		featureHeightProfile = profile;
	}

}
