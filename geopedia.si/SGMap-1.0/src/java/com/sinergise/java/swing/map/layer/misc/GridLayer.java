package com.sinergise.java.swing.map.layer.misc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.java.swing.map.PaintOperation;
import com.sinergise.java.swing.map.layer.LayerPerformanceInfo;
import com.sinergise.java.swing.map.layer.OrLayerImpl;
import com.sinergise.java.swing.map.style.DefaultLineStyle;
import com.sinergise.java.swing.map.style.LineType;



public class GridLayer extends OrLayerImpl {
	private DefaultLineStyle lineStyle=new DefaultLineStyle(LineType.SOLID,new Color(255,0,0,96), 1f);
	private double gridW;
	private double gridH;
	private double offX;
	private double offY;
	private Envelope bnds = new Envelope();
	
	LayerPerformanceInfo lpInfo=new LayerPerformanceInfo() {
		public double updateFreq() {
			return 0;
		}
		
		public long timeToRender(DisplayCoordinateAdapter dca) {
			return 10;
		}
		
		public long maxTimeToRender() {
			return 10;
		}
	};
	
	public GridLayer setOffset(double offX, double offY) {
		this.offX = offX;
		this.offY = offY;
		return this;
	}
	
	public GridLayer setGridSize(double gridW, double gridH) {
		this.gridW = gridW;
		this.gridH = gridH;
		return this;
	}
	
	@Override
	public LayerPerformanceInfo getPerformanceInfo() {
		return lpInfo;
	}
	@Override
	public Envelope getBounds() {
		return bnds.isEmpty()?null:bnds;
	}
	@Override
	public void paintLayer(Graphics2D g2d, DisplayCoordinateAdapter dca, PaintOperation mgr) {
		int startX = (int)((dca.worldRect.getMinX()-offX)/gridW);
		int startY = (int)((dca.worldRect.getMinY()-offY)/gridH);
		int endX = (int)Math.ceil((dca.worldRect.getMaxX()-offX)/gridW);
		int endY = (int)Math.ceil((dca.worldRect.getMaxY()-offY)/gridH);
		Line2D ln= new Line2D.Double();
		for (int gx = startX; gx <= endX; gx++) {
			int xPx = dca.pixFromWorld.xInt(offX+gridW*gx);
			ln.setLine(xPx, 0, xPx, dca.getDisplayHeight());
			lineStyle.draw(ln, dca, g2d);
		}
		for (int gy = startY; gy <= endY; gy++) {
			int yPx = dca.pixFromWorld.yInt(offY+gridH*gy);
			ln.setLine(0, yPx, dca.getDisplayWidth(), yPx);
			lineStyle.draw(ln, dca, g2d);
		}
	}
}
