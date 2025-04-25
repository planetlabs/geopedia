package com.sinergise.java.swing.map.raster;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.List;

import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.raster.core.RasterWorldInfo;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeI;
import com.sinergise.common.util.geom.PointI;
import com.sinergise.java.raster.core.RasterUtilJava;
import com.sinergise.java.raster.core.SGRenderedImage;
import com.sinergise.java.raster.core.WorldImageCollection;
import com.sinergise.java.raster.core.WorldRasterImage;
import com.sinergise.java.swing.map.PaintOperation;
import com.sinergise.java.swing.map.layer.LayerPerformanceInfo;
import com.sinergise.java.swing.map.layer.OrLayerImpl;

public class ImageCollectionLayer extends OrLayerImpl {
	WorldImageCollection<? extends RasterWorldInfo> images;
	LayerPerformanceInfo perfInfo = new LayerPerformanceInfo() {
		@Override
		public double updateFreq() {
			return 0;
		}
		
		@Override
		public long timeToRender(DisplayCoordinateAdapter dca) {
			return 0;
		}
		
		@Override
		public long maxTimeToRender() {
			return 0;
		}
	};
		
	public ImageCollectionLayer(WorldImageCollection<?> images) {
		this.images = images;
	}

	@Override
	public void paintLayer(final Graphics2D g, final DisplayCoordinateAdapter dca, final PaintOperation mgr) {
		if (images == null) return;

		List<? extends RasterWorldInfo> infos = images.search(dca.worldRect, null);
		for (RasterWorldInfo tif : infos) {
			if (mgr.isCancelled()) return;
			g.setColor(new Color(0x2000FF00, true));
			int[] xPts = new int[5];
			int[] yPts = new int[5];
			createPolygon(tif, dca, xPts, yPts);
			g.fillPolygon(xPts, yPts, 5);
		}
		
		for (RasterWorldInfo tif : infos) {
			if (mgr.isCancelled()) return;
			if (tif instanceof WorldRasterImage && scaleRatio(tif, dca) > 0.2) {
				renderImage((WorldRasterImage)tif, g, dca, mgr);
			}
		}
		for (RasterWorldInfo tif : infos) {
			if (mgr.isCancelled()) return;
			double scaleRatio = scaleRatio(tif, dca);

			int[] xPts = new int[5];
			int[] yPts = new int[5];
			createPolygon(tif, dca, xPts, yPts);
			int opacity = Math.max(32, Math.min((int)(1000*scaleRatio), 255));
			g.setColor(new Color(0xFF0000 | (opacity << 24), true));
			g.drawPolyline(xPts, yPts, 5);

			
			xPts[1] = dca.pixFromWorld.xInt(tif.transformXGridBased(1, 0));
			yPts[1] = dca.pixFromWorld.yInt(tif.transformYGridBased(1, 0));
			xPts[2] = dca.pixFromWorld.xInt(tif.transformXGridBased(1, 1));
			yPts[2] = dca.pixFromWorld.yInt(tif.transformYGridBased(1, 1));
			xPts[3] = dca.pixFromWorld.xInt(tif.transformXGridBased(0, 1));
			yPts[3] = dca.pixFromWorld.yInt(tif.transformYGridBased(0, 1));
			xPts[4] = xPts[0];
			yPts[4] = yPts[0];
			g.setColor(Color.BLUE);
			g.drawPolyline(xPts, yPts, 5);

			PointI textPos = dca.pixFromWorld.pointInt(tif.transformPointGridBased(new Point(10/scaleRatio, 10/scaleRatio)));
			if (scaleRatio > 0.005) {
				int w = dca.pixFromWorld.lengthInt(tif.worldEnvelope().getWidth());
				String text = tif.toString();
				if (text.length() * 8 > w) {
					text = text.replaceAll("(\\.\\S+)\\s.*", "$1");
				}
				if (text.length() * 8 > w) {
					text = text.replaceAll("\\.\\S+", "");
				}
				g.drawString(text, textPos.x - 5, textPos.y + 5);
			}
		}
	}

	private void createPolygon(RasterWorldInfo tif,
			final DisplayCoordinateAdapter dca, int[] xPts, int[] yPts) {
		xPts[0] = dca.pixFromWorld.xInt(tif.transformXGridBased(0, 0));
		yPts[0] = dca.pixFromWorld.yInt(tif.transformYGridBased(0, 0));
		xPts[1] = dca.pixFromWorld.xInt(tif.transformXGridBased(tif.w, 0));
		yPts[1] = dca.pixFromWorld.yInt(tif.transformYGridBased(tif.w, 0));
		xPts[2] = dca.pixFromWorld.xInt(tif.transformXGridBased(tif.w, tif.h));
		yPts[2] = dca.pixFromWorld.yInt(tif.transformYGridBased(tif.w, tif.h));
		xPts[3] = dca.pixFromWorld.xInt(tif.transformXGridBased(0, tif.h));
		yPts[3] = dca.pixFromWorld.yInt(tif.transformYGridBased(0, tif.h));
		xPts[4] = xPts[0];
		yPts[4] = yPts[0];
	}

	private double scaleRatio(RasterWorldInfo tif, final DisplayCoordinateAdapter dca) {
		return dca.pixFromWorld.length(Math.sqrt(tif.getWorldAreaPerPix()));
	}

	protected void renderImage(WorldRasterImage img, Graphics2D g, DisplayCoordinateAdapter dca, PaintOperation mgr) {
		if (!img.wEnv.intersects(dca.worldRect)) {
			return;
		}
		if (mgr.isCancelled()) {
			return;
		}
		AffineTransform2D trans = img.tr.append(dca.pixFromWorld.createAffine());
		AffineTransform2D transInv = trans.inverse();
		
		Envelope pixDispEnv = new Envelope(-0.5,-0.5,dca.pixDisplaySize.w()-0.5, dca.pixDisplaySize.h()-0.5);
		Envelope imEnv = transInv.envelope(pixDispEnv);
		EnvelopeI imEnvI = imEnv.roundOutside();
		imEnvI = imEnvI.intersection(0,0,img.w-1,img.h-1);
		if (imEnvI.isEmpty()) {
			return;
		}
		
		try {
			SGRenderedImage croppedImage = RasterUtilJava.crop(img.getImage(), imEnvI);
			// Translate transform to account for cropping
			trans = AffineTransform2D.createTranslation(trans.getSource(), trans.getSource(), imEnvI.minX(), imEnvI.minY()).append(trans);
			AffineTransform bothTr = com.sinergise.java.geometry.util.APIMapping.toJ2D(trans);
			
//			float amount = 0.6f;
//			ColorFilter filter = new BrightnessContrast(-amount*0.44f, amount);
			
//			croppedImage = ColorFilter.transform(RasterUtilJava.wrap(RasterUtilJava.toBufferedImage(croppedImage)), filter);
//			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			g.drawRenderedImage(croppedImage.unwrap(), bothTr);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Envelope getBounds() {
		return images.getBounds();
	}

	@Override
	public LayerPerformanceInfo getPerformanceInfo() {
		return new LayerPerformanceInfo() {
			
			@Override
			public double updateFreq() {
				return 0;
			}
			
			@Override
			public long timeToRender(DisplayCoordinateAdapter dca) {
				return 1000;
			}
			
			@Override
			public long maxTimeToRender() {
				return 1000;
			}
		};
	}

}
