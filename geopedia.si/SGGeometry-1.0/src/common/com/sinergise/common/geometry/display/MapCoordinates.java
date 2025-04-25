package com.sinergise.common.geometry.display;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CartesianCRS;
import com.sinergise.common.geometry.crs.transform.SimpleTransform;
import com.sinergise.common.geometry.display.event.CoordinatesListener;
import com.sinergise.common.geometry.display.event.CoordinatesListenerCollection;
import com.sinergise.common.geometry.display.event.SourcesCoordinateEvents;
import com.sinergise.common.util.geom.MapViewSpec;

/**
 * Coordinate systems prefixes:
 * <ul>
 * <li><code>world</code> = meters in source (world) coordinate space
 * <li><code>display</code> = meters in target (display) coordinate space measured to the right and upwards from display centre
 * </ul>
 * 
 * Variables:
 * <ul>
 * <li><code>worldCenterX</code> = x position of the window centre</li>
 * <li><code>worldCenterY</code> = y position of the window centre</li>
 * <li><code>worldLenPerDisp == scale</code> = normalised ratio of world units to display units</li>
 * </ul>
 * 
 * @author mkadunc, mslenc
 */
public class MapCoordinates extends MapViewSpec implements SourcesCoordinateEvents {
	public class WorldFromDisp extends SimpleTransform.FromInt<CartesianCRS, CartesianCRS> {
		
		public WorldFromDisp() {
			super(CRS.MAP_CRS, worldCRS);
		}
		
		@Override
		public double length(double sourceLength) {
			return sourceLength * worldLenPerDisp;
		}
		
		@Override
		public double area(double sourceArea) {
			return sourceArea * worldLenPerDisp * worldLenPerDisp;
		}
		
		@Override
		public double getScale() {
			return worldLenPerDisp;
		}
		
		@Override
		public double x(double x) {
			return (x * worldLenPerDisp) - worldCenterX;
		}
		
		@Override
		public double y(double y) {
			return (y * worldLenPerDisp) - worldCenterY;
		}
	}
	
	public class DispFromWorld extends SimpleTransform<CartesianCRS, CartesianCRS> {
		
		public DispFromWorld() {
			super(worldCRS, CRS.MAP_CRS);
		}
		
		@Override
		public double x(double worldX) {
			return (worldX - worldCenterX) / worldLenPerDisp;
		}
		
		@Override
		public double y(double worldY) {
			return (worldY - worldCenterY) / worldLenPerDisp;
		}
		
		@Override
		public double length(double sourceLength) {
			return sourceLength / worldLenPerDisp;
		}
		
		@Override
		public double area(double sourceArea) {
			return sourceArea / (worldLenPerDisp * worldLenPerDisp);
		}
		
		@Override
		public double getInverseScale() {
			return worldLenPerDisp;
		}
	}
	
	CoordinatesListenerCollection listeners;
	
	@Override
	public void addCoordinatesListener(CoordinatesListener l) {
		if (listeners == null) listeners = new CoordinatesListenerCollection();
		listeners.add(l);
	}
	
	@Override
	public void removeCoordinatesListener(CoordinatesListener l) {
		if (listeners == null) return;
		listeners.remove(l);
	}
	
	public final CartesianCRS worldCRS;
	
	public final DisplayBounds.Disp bounds = new DisplayBounds.Disp();
	
	public transient final WorldFromDisp worldFromDisp = new WorldFromDisp();
	public transient final DispFromWorld dispFromWorld = new DispFromWorld();
	
	public MapCoordinates() {
		this(null, CRS.NONAME_WORLD_CRS);
	}
	
	/**
	 * @param bounds
	 *          the boundaries that will be used to limit the position of view's
	 *          center (<code>null</code> implies infinite bounds)
	 */
	public MapCoordinates(DisplayBounds bounds, CartesianCRS worldCRS) {
		if (bounds != null) {
			this.bounds.copyFrom(bounds);
		} else {
			this.bounds.setScaleBounds(0, Double.POSITIVE_INFINITY);
			if (worldCRS.bounds2D == null) {
				this.bounds.setMBR(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
					Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
			}
		}
		if (this.bounds.mbr.isEmpty() && worldCRS.bounds2D != null) {
			this.bounds.mbr = worldCRS.bounds2D;
		}
		this.worldCRS = worldCRS;
		updateTransient();
	}
	
	protected void updateTransient() {}
	
	public void setScale(double scale) {
		setWorldCenterAndScale(worldCenterX, worldCenterY, scale);
	}
	
	public void setWorldCenterAndScale(double wCenterX, double wCenterY, double scale) {
		internalSetWorldCenterAndScale(wCenterX, wCenterY, scale);
	}
	
	@Override
	protected void internalSetWorldCenterAndScale(double wCenterX, double wCenterY, double scale) {
		wCenterX = bounds.clampX(wCenterX);
		wCenterY = bounds.clampY(wCenterY);
		scale = bounds.clampScale(scale);
		
		boolean locChanged = worldCenterX != wCenterX || worldCenterY != wCenterY;
		boolean zoomChanged = worldLenPerDisp != scale;
		if (locChanged || zoomChanged) {
			super.internalSetWorldCenterAndScale(wCenterX, wCenterY, scale);
			updateTransient();
			fireNotification(locChanged, zoomChanged);
		}
	}
	
	protected void fireNotification(boolean locChanged, boolean zoomChanged) {
		if (listeners == null) return;
		listeners.fireChange(worldCenterX, worldCenterY, worldLenPerDisp, locChanged, zoomChanged);
	}
	
	public void setWorldCenter(double x, double y) {
		setWorldCenterAndScale(x, y, worldLenPerDisp);
	}
	
	public void setWorldCenterAndScale(MapViewSpec viewSpec) {
		setWorldCenterAndScale(viewSpec.worldCenterX, viewSpec.worldCenterY, viewSpec.worldLenPerDisp);
	}
}
