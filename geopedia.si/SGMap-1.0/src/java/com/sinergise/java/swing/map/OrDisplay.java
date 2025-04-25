package com.sinergise.java.swing.map;

import java.awt.Graphics2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.sinergise.common.geometry.crs.CartesianCRS;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.geom.Position2D;
import com.sinergise.java.swing.map.layer.OrLayer;


/**
 * @author Miha Kadunc ( <a
 *         href="mailto:miha.kadunc@cosylab.com">miha.kadunc@cosylab.com </a>
 */
public class OrDisplay {
	private class DisplayPainter extends PaintOperation.Default {
		
		private int layerStart;
		private int layerEnd;
		
		private Graphics2D g;
		
		public DisplayPainter(Graphics2D g, DisplayCoordinateAdapter coords, int layerStart,
				int layerEnd) {
			super(coords);
			this.g = g;
			this.layerStart = layerStart;
			this.layerEnd = layerEnd;
		}
		
		public void startPainting() {
			DimI displayDim = coords.getDisplaySize();
			if (displayDim.isEmpty()) { 
				return;
			}
			int i = layerStart;
			while (!cancelled && i <= layerEnd) {
				OrLayer curLayer = layers.get(i++);
				if (shouldPaint(curLayer)) {
					curLayer.paintLayer(g, coords, this);
				}
			}
			super.finish();
		}
		
		public boolean shouldPaint(OrLayer layer) {
			if (!layer.getProperties().isOn()) return false;
			if (!layer.getProperties().checkScale(coords.getScale())) return false;
			return true;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.cosylab.ortelius.space.display.PaintOperation#getCoordinateAdapter()
		 */
		@Override
		public DisplayCoordinateAdapter getCoordinateAdapter() {
			return coords;
		}
	}
	
	public static final String PROP_DISPLAY_CENTER = "displayCenter";
	
	public static final String PROP_DISPLAY_SCALE = "displayScale";
	
	public static final String PROP_DISPLAY_GRAPHICS = "displayGraphics";
	
	public static final String PROP_LAYER = "layerData";
	
	public static final String PROP_LAYERS = "layers";
	
	private DisplayCoordinateAdapter displayedCoords;
	
	public LayersList layers;
	
	// private double maxScale;
	// private double minScale;
	
	private PropertyChangeSupport pcs;
	
	private Envelope worldBounds;
	
	public OrDisplay(CartesianCRS worldCRS) {
		super();
		displayedCoords = new DisplayCoordinateAdapter(worldCRS);
		layers = new LayersList();
		
		worldBounds = null;
		pcs = new PropertyChangeSupport(this);
		setDisplayCenter(new Point());
		setDisplayScale(1);
	}
	
	public void addLayer(OrLayer layer) {
		layers.add(layer);
		layer.addedToDisplay(this);
		pcs.fireIndexedPropertyChange(PROP_LAYERS, layers.size() - 1, null, layer);
	}
	
	public void addLayer(int index, OrLayer layer) {
		layers.add(index, layer);
		layer.addedToDisplay(this);
		pcs.fireIndexedPropertyChange(PROP_LAYERS, index, null, layer);
	}
	
	/**
	 * @param listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}
	
	/**
	 * @param propertyName
	 * @param listener
	 */
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(propertyName, listener);
	}
	
	private void checkBounds() {
		worldBounds = null;
		for (OrLayer layer : layers) {
			if (layer.getProperties().isBackground()) continue;
			Envelope lb = layer.getBounds();
			if (lb != null) {
				if (worldBounds == null) {
					worldBounds = lb;
				} else {
					worldBounds = worldBounds.union(lb);
				}
			}
		}
		
		if (worldBounds == null) { // All layers are empty
			worldBounds = Envelope.withSize(0, 0, 1, 1);
		}
		// double stretch = Math.max(worldBounds.getWidth(),
		// worldBounds.getHeight());
		// maxScale = 1E20 / stretch;
		// minScale = 0;
	}
	
	public PaintOperation createDisplayPainter(Graphics2D g, DisplayCoordinateAdapter dca) {
		return new DisplayPainter(g, dca, 0, layers.size() - 1);
	}
	
	public PaintOperation createDisplayPainter(Graphics2D g, DimI displaySize, int startLayer,
			int endLayer) {
		if (layers.size() == 0) return null;
		return new DisplayPainter(g, createCoordinateAdapter(displaySize), startLayer, endLayer);
	}
	
	public DisplayCoordinateAdapter createCoordinateAdapter(DimI displayDim) {
		DisplayCoordinateAdapter dca = new DisplayCoordinateAdapter(displayedCoords);
		dca.setDisplaySize(displayDim);
		return dca;
	}
	
	/**
	 * @return Returns the displayCenter.
	 */
	public Point getDisplayCenter() {
		return displayedCoords.getDisplayCenterWorld();
	}
	
	/**
	 * @return Returns the displayScale.
	 */
	public double getDisplayScale() {
		return displayedCoords.getScale();
	}
	
	/**
	 * @return
	 */
	public PropertyChangeListener[] getPropertyChangeListeners() {
		return pcs.getPropertyChangeListeners();
	}
	
	/**
	 * @param propertyName
	 * @return
	 */
	public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
		return pcs.getPropertyChangeListeners(propertyName);
	}
	
	/**
	 * @return Returns the worldBounds.
	 */
	public Envelope getWorldBounds() {
		checkBounds();
		return worldBounds == null ? new Envelope() : worldBounds;
	}
	
	/**
	 * @param listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}
	
	/**
	 * @param propertyName
	 * @param listener
	 */
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(propertyName, listener);
	}
	
	/**
	 * @param displayCenter
	 *          The displayCenter to set.
	 */
	public void setDisplayCenter(HasCoordinate displayCenter) {
		Position2D old = new Position2D(displayedCoords.worldCenterX, displayedCoords.worldCenterY);
		displayedCoords.setWorldCenter(displayCenter.x(), displayCenter.y());
		displayCenter = displayedCoords.getDisplayCenterWorld();
		pcs.firePropertyChange(PROP_DISPLAY_SCALE, old, displayCenter);
	}
	
	/**
	 * @param displayScale
	 *          The displayScale to set.
	 */
	public void setDisplayScale(double displayScale) {
		double old = getDisplayScale();
		displayedCoords.setScale(displayScale);
		displayScale = displayedCoords.getScale();
		pcs.firePropertyChange(PROP_DISPLAY_SCALE, new Double(old), new Double(displayScale));
	}
	
	/*
	 * private void setWorldBounds(Envelope worldBounds) { Envelope old =
	 * this.worldBounds; this.worldBounds = worldBounds;
	 * pcs.firePropertyChange("worldBounds", old, worldBounds); }
	 */

	public void updateDisplay(Envelope changedRect) {
		checkBounds();
		pcs.firePropertyChange(PROP_DISPLAY_GRAPHICS, null, changedRect);
	}
	
	public void updateDisplay() {
		updateDisplay(worldBounds);
	}
	
	public LayersList getLayers() {
		return layers;
	}
	
	public OrLayer getLayer(String name) {
		return layers.get(name);
	}
	
	public void removeLayer(OrLayer layer) {
		int idx = layers.indexOf(layer);
		layers.remove(idx);
		pcs.fireIndexedPropertyChange(PROP_LAYERS, idx, layer, null);
	}
	
	public void layerChanged(OrLayer layer) {
		pcs.firePropertyChange(PROP_LAYER, null, layer);
	}

	public DisplayCoordinateAdapter getDisplayedCoordinateAdapter() {
		return displayedCoords;
	}
}
