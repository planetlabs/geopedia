package com.sinergise.java.swing.map.layer.misc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.java.swing.map.PaintOperation;
import com.sinergise.java.swing.map.layer.LayerPerformanceInfo;
import com.sinergise.java.swing.map.layer.OrLayerImpl;
import com.sinergise.java.swing.map.style.CompositeStyle;
import com.sinergise.java.swing.map.style.DefaultFillStyle;
import com.sinergise.java.swing.map.style.DefaultLineStyle;
import com.sinergise.java.swing.map.style.FillType;
import com.sinergise.java.swing.map.style.LineType;
import com.sinergise.java.swing.map.style.VectorStyle;


public class RectangleLayer extends OrLayerImpl {
	public static final VectorStyle createStyle(Color outline, Color fill) {
		VectorStyle lineStyle = outline==null?null:new DefaultLineStyle(LineType.SOLID, outline, 1.0f);
		VectorStyle fillStyle = fill==null?null:new DefaultFillStyle(FillType.SOLID, fill, 1);
		if (fillStyle==null) {
			if (lineStyle==null) return null;
			return lineStyle;
		}
		if (lineStyle==null) return fillStyle;
		return new CompositeStyle(new VectorStyle[]{fillStyle, lineStyle});
	}
	LayerPerformanceInfo pInfo=new LayerPerformanceInfo() {
		public double updateFreq() {
			return 25;
		}
		
		public long timeToRender(DisplayCoordinateAdapter dca) {
			return 1;
		}
		
		public long maxTimeToRender() {
			return 1;
		}
	};
	private Envelope envelope;
	private VectorStyle style;
	private transient Envelope enPix=new Envelope();
	private transient Rectangle2D.Double enJ2D=new Rectangle2D.Double();
	
	public RectangleLayer(Color outline, Color fill) {
		this(createStyle(outline, fill));
	}
	public RectangleLayer(VectorStyle style) {
		this.style = style;
	}
	
	@Override
	public LayerPerformanceInfo getPerformanceInfo() {
		return pInfo;
	}
	@Override
	public Envelope getBounds() {
		return null;
	}
	@Override
	public void paintLayer(Graphics2D g, DisplayCoordinateAdapter dca, PaintOperation mgr) {
		if (envelope==null || style==null) return;
		enPix = dca.pixFromWorld.rect(envelope);
		enJ2D.setFrame(enPix.getMinX(), enPix.getMinY(), enPix.getWidth(), enPix.getHeight());
		style.draw(enJ2D, dca, g);
	}
	
	public void clear() {
		if (envelope==null) return;
		envelope=null;
		fireLayerChanged();
	}
	
	public void setRect(Point p1, Point p2) {
		envelope = Envelope.create(p1, p2);
		fireLayerChanged();
	}
}
