package com.sinergise.java.swing.map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.Timer;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeI;
import com.sinergise.java.swing.map.layer.OrLayer;
import com.sinergise.java.util.collections.ListenerList;


/**
 * @author Miha Kadunc (<a
 *         href="mailto:miha.kadunc@cosylab.com">miha.kadunc@cosylab.com</a>
 */
public class OrDisplayBuffer {
	private static class BufferedRaster {
		private static final GraphicsConfiguration gc = GraphicsEnvironment
			.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
		private static final Color TRANSP_COL = new Color(0, 0, 0, 0);
		
		public static final int OPAQUE = Transparency.OPAQUE;
		public static final int TRANSLUCENT = Transparency.TRANSLUCENT;
		
		public BufferedImage image;
		public Envelope worldRect;
		int type = TRANSLUCENT;
		
		public BufferedRaster() {
		}

		public void resize(DisplayCoordinateAdapter newDCA) {
			if (newDCA==null) return;
			DimI dispDim = newDCA.getDisplaySize();
			if (image == null) {
				image = create(dispDim, type);
				
			} else if (!dispDim.equals(image.getWidth(), image.getHeight())) {
				BufferedImage tempBuf = create(dispDim, type);
				Graphics2D gr = tempBuf.createGraphics();
				paint(gr, newDCA);
				image = tempBuf;
				
			} else {
				paint(image.createGraphics(), newDCA);
			}
			worldRect = newDCA.worldRect;
		}
		
		public void paint(Graphics2D g, DisplayCoordinateAdapter dca) {
			if (image == null) return;
			if (dca.worldRect.equals(worldRect)) {
				DimI size = dca.getDisplaySize();
				g.drawImage(image, 0, 0, size.w(), size.h(), null);
			} else {
				EnvelopeI bufRect = dca.pixFromWorld.rectInt(worldRect);
				g.drawImage(image, bufRect.minX(), bufRect.minY(), bufRect.getWidth()-1, bufRect.getHeight()-1,
					null);
			}
		}
		
		public void clear(Graphics2D g) {
			if (g == null) return;
			if (type == OPAQUE) {
				g.setBackground(Color.WHITE);
				g.clearRect(0, 0, image.getWidth(), image.getHeight());
			} else {
				g.setBackground(TRANSP_COL);
				g.clearRect(0, 0, image.getWidth(), image.getHeight());
			}
		}
		
		private static final BufferedImage create(DimI d, int transp) {
			return gc.createCompatibleImage(d.w(), d.h(), transp);
		}
	}
	
	private static final PaintOperation dummyOp = new PaintOperation() {
		public void startPainting() {
			System.err.println("START");
			throw new UnsupportedOperationException();
		}
		
		public boolean isFinished() {
			System.err.println("FINISH");
			return false;
		}
		
		public boolean isCancelled() {
			return false;
		}
		
		public DisplayCoordinateAdapter getCoordinateAdapter() {
			System.err.println("DCA");
			throw new UnsupportedOperationException();
		}
		
		public void cancelPainting() {
			System.err.println("CANCEL");
		}
	};
	
	private class LayerBuffer implements Runnable {
		
		public OrLayer layer;
		public int lyrIdx;
		private boolean running = true;
		
		private Object lock = new Object();
		BufferedRaster buffer = null;
		private DisplayCoordinateAdapter scheduledCoords = null;
		private PaintOperation currentPainter = null;
		private boolean dirty = false;
		boolean painting = false;
		
		public LayerBuffer(OrLayer layer, int idx) {
			this.layer = layer;
			this.lyrIdx = idx;
			
			// long t=layer.getPerformanceInfo().maxTimeToRender();
			// double freq=layer.getPerformanceInfo().updateFreq();
			this.buffer = new BufferedRaster();
			if (idx == 0) buffer.type = BufferedRaster.OPAQUE;
			else buffer.type = BufferedRaster.TRANSLUCENT;
		}
		
		public void run() {
			while (running) {
				try {
					if (dirty) {
						dirty = false;
						repaint();
					} else {
						synchronized (lock) {
							lock.wait();
						}
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
			painting = false;
			buffer = null;
			currentPainter = null;
			dirty = false;
			layer = null;
			lock = null;
			scheduledCoords = null;
		}
		
		public void schedule(DisplayCoordinateAdapter coords) {
			if (currentPainter != null) {
				currentPainter.cancelPainting();
			}
			synchronized (lock) {
				this.scheduledCoords = coords;
				dirty = true;
				painting = true;
				lock.notify();
			}
		}
		
		public void repaint() {
			DimI dim = scheduledCoords.getDisplaySize();
			if (dim.w() < 1 || dim.h() < 1) { return; }
			synchronized (lock) {
				buffer.resize(scheduledCoords);
				Graphics2D g = buffer.image.createGraphics();
				buffer.clear(g);
				paintLayer(g);
				if (!dirty) {
					painting = false;
					updateDirectBuffer();
				}
			}
		}
		
		private void paintLayer(Graphics2D g) {
			if (scheduledCoords == null) return;
			synchronized (lock) {
				currentPainter = disp.createDisplayPainter(g, scheduledCoords.getDisplaySize(), lyrIdx, lyrIdx);
				if (currentPainter != null) {
					currentPainter.startPainting();
					currentPainter = null;
				}
			}
		}
		
		@Override
		public String toString() {
			return "Buffer [" + lyrIdx + "]";
		}

		public void destroy() {
			if (running) {
				synchronized(lock) {
					running = false;
					lock.notify();
				}
			}
		}
	}
	
	public static final String DISPLAY_CHANGE_ACTION = "displayChange";
	
	private LayerBuffer[] painters;
	private BufferedRaster directBuffer;
	private ListenerList<ActionListener> actionListeners = new ListenerList<ActionListener>(ActionListener.class);
	
	OrDisplay disp = null;
	private DisplayCoordinateAdapter paintingCoords = null;
	private Timer timer = new Timer(250, new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			scheduleRepaint();
		}
	});
	{
		timer.setInitialDelay(250);
		timer.setRepeats(false);
	}
	
	/**
	 * 
	 */
	public OrDisplayBuffer(OrDisplay disp) {
		super();
		this.disp = disp;
		
		initializePainters();
		directBuffer = new BufferedRaster();
		
		disp.addPropertyChangeListener(OrDisplay.PROP_DISPLAY_GRAPHICS, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				repaintAll(false);
			}
		});
		disp.addPropertyChangeListener(OrDisplay.PROP_LAYERS, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				initializePainters();
			}
		});
		disp.addPropertyChangeListener(OrDisplay.PROP_LAYER, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				OrLayer changed = (OrLayer) evt.getNewValue();
				repaintLayer(changed);
			}
		});
	}
	
	void initializePainters() {
		HashMap<OrLayer, LayerBuffer> pMap = new HashMap<OrLayer, LayerBuffer>();
		HashSet<LayerBuffer> bufset = new HashSet<OrDisplayBuffer.LayerBuffer>();
		if (painters != null) {
			for (LayerBuffer buf : painters) {
				if (buf != null) {
					pMap.put(buf.layer, buf);
					bufset.add(buf);
				}
			}
		}
		LayersList layers = disp.getLayers();
		painters = new LayerBuffer[layers.size()];
		if (layers.isEmpty()) return;
		for (int i = 0; i < layers.size(); i++) {
			OrLayer ly = layers.get(i);
			long t = ly.getPerformanceInfo().maxTimeToRender();
			double freq = ly.getPerformanceInfo().updateFreq();
			
			LayerBuffer buf = pMap.get(ly);
			bufset.remove(buf);
			if (t > 5 || freq < 5) {
				if (buf == null) {
					painters[i] = createPainter(ly, i);
					painters[i].schedule(getCoordinateAdapter());
				} else {
					buf.lyrIdx = i;
					painters[i] = buf;
				}
			} else {
				painters[i] = null;
			}
		}
		for (LayerBuffer b : bufset) {
			b.destroy();
		}
		updateDirectBuffer();
	}
	
	private LayerBuffer createPainter(OrLayer layer, int idx) {
		LayerBuffer gp = new LayerBuffer(layer, idx);
		
		Thread upT = new Thread(gp, "OrDisplay update [" + layer + "]");
		upT.setDaemon(true);
		upT.setPriority(Thread.NORM_PRIORITY - 1);
		upT.start();
		
		return gp;
	}
	
	/**
	 * @param listener
	 */
	public void addActionListener(ActionListener listener) {
		actionListeners.add(listener);
	}
	
	protected void fireDisplayChanged() {
		ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, DISPLAY_CHANGE_ACTION);
		ActionListener[] lls = (ActionListener[]) actionListeners.toArray();
		for (int i = 0; i < lls.length; i++) {
			lls[i].actionPerformed(evt);
		}
	}
	
	void updateDirectBuffer() {
		for (LayerBuffer buf : painters) {
			if (buf != null && buf.painting) { return; }
		}
		directBuffer.resize(paintingCoords);
		DimI dim = paintingCoords.getDisplaySize();
		
		Graphics2D dG = directBuffer.image.createGraphics();
		dG.setBackground(Color.WHITE);
		dG.clearRect(0, 0, dim.w(), dim.h());
		
		int lastP = painters.length - 1;
		while (lastP >= 0 && painters[lastP] == null)
			lastP--;
		for (int i = 0; i <= lastP; i++) {
			LayerBuffer painter = painters[i];
			if (painter == null) {
				disp.getLayers().get(i).paintLayer(dG, paintingCoords, dummyOp);
			} else {
				painter.buffer.paint(dG, paintingCoords);
			}
		}
		fireDisplayChanged();
	}
	
	/**
	 * @return
	 */
	public DisplayCoordinateAdapter getCoordinateAdapter() {
		if (paintingCoords == null) {
			paintingCoords = disp.getDisplayedCoordinateAdapter();
		}
		return paintingCoords;
	}
	
	/**
	 * @return
	 */
	public Point getDisplayCenter() {
		return disp.getDisplayCenter();
	}
	
	/**
	 * @return
	 */
	public double getDisplayScale() {
		return disp.getDisplayScale();
	}
	
	/**
	 * @return Returns the displaySize.
	 */
	public DimI getDisplaySize() {
		return paintingCoords.getDisplaySize();
	}
	
	/**
	 * @return
	 */
	public OrDisplay getOrDisplay() {
		return disp;
	}
	
	public DisplayCoordinateAdapter paintContents(Graphics2D g2D) {
		DimI dim = paintingCoords.getDisplaySize();
		g2D.setBackground(Color.WHITE);
		g2D.clearRect(0, 0, dim.w(), dim.h());
		directBuffer.paint(g2D, paintingCoords);
		
		int lastP = painters.length - 1;
		while (lastP >= 0 && painters[lastP] == null)
			lastP--;
		for (int i = lastP + 1; i < painters.length; i++) {
			disp.getLayers().get(i).paintLayer(g2D, paintingCoords, dummyOp);
		}
		return new DisplayCoordinateAdapter(paintingCoords);
	}
	
	/**
	 * @param listener
	 */
	public void removeActionListener(PropertyChangeListener listener) {
		actionListeners.remove(listener);
	}
	
	public void repaintAll(boolean waitForNext) {
		if (waitForNext) {
			timer.restart();
		} else {
			timer.stop();
			scheduleRepaint();
		}
	}
	
	void scheduleRepaint() {
		if (paintingCoords != null) {
			updateDirectBuffer();
			DisplayCoordinateAdapter pDca = new DisplayCoordinateAdapter(paintingCoords);
			synchronized (painters) {
				for (LayerBuffer painter : painters) {
					if (painter != null) painter.schedule(pDca);
				}
			}
		}
	}
	
	public void repaintLayer(OrLayer layer) {
		if (paintingCoords != null) {
			int idx = disp.getLayers().indexOf(layer);
			
			int lastP = painters.length - 1;
			while (lastP >= 0 && painters[lastP] == null)
				lastP--;
			
			if (idx > lastP) {
				fireDisplayChanged();
			} else {
				LayerBuffer buf = painters[idx];
				if (buf == null) {
					updateDirectBuffer();
				} else {
					DisplayCoordinateAdapter pDca = new DisplayCoordinateAdapter(paintingCoords);
					buf.schedule(pDca);
				}
			}
		}
	}
	
	/**
	 * @param displayCenter
	 */
	public void setDisplayCenter(Point displayCenter) {
		displayCenter.x = Math.round(displayCenter.x/disp.getDisplayedCoordinateAdapter().worldLengthPerPix)*disp.getDisplayedCoordinateAdapter().worldLengthPerPix;
		displayCenter.y = Math.round(displayCenter.y/disp.getDisplayedCoordinateAdapter().worldLengthPerPix)*disp.getDisplayedCoordinateAdapter().worldLengthPerPix;
		Point oldC = disp.getDisplayCenter();
		if (oldC == null || !oldC.equals(displayCenter)) {
			disp.setDisplayCenter(displayCenter);
			getCoordinateAdapter().setWorldCenter(displayCenter.x, displayCenter.y);
			repaintAll(true);
		}
	}
	
	/**
	 * @param displayScale
	 */
	public void setDisplayScale(double displayScale) {
		double oldSc = disp.getDisplayScale();
		if (oldSc != displayScale) {
			disp.setDisplayScale(displayScale);
			getCoordinateAdapter().setScale(displayScale);
			repaintAll(true);
		}
	}
	
	/**
	 * @param displaySize
	 *          The displaySize to set.
	 */
	public void setDisplaySize(DimI displaySize) {
		DisplayCoordinateAdapter dca = getCoordinateAdapter();
		DimI old = dca.getDisplaySize();
		dca.setDisplaySize(displaySize.w(), displaySize.h());
		if (old == null || old.w() < displaySize.w() || old.h() < displaySize.h()) {
			repaintAll(true);
		}
	}
	
	/**
	 * @param rect
	 */
	public void setDisplayedWindow(Envelope rect) {
		paintingCoords.setDisplayedRect(rect);
		Point c = paintingCoords.getDisplayCenterWorld();
		long numX = (long)(c.x/paintingCoords.worldLengthPerPix);
		long numY = (long)(c.y/paintingCoords.worldLengthPerPix);
		c.x = (numX+0.5) * paintingCoords.worldLengthPerPix;
		c.y = (numY+0.5) * paintingCoords.worldLengthPerPix;
		setDisplayCenter(c);
		setDisplayScale(paintingCoords.getScale());
	}
}
