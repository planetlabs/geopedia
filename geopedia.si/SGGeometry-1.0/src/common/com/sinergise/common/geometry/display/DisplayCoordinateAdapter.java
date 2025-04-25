package com.sinergise.common.geometry.display;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CartesianCRS;
import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.geometry.crs.transform.SimpleTransform;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.util.CoordUtil;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.common.util.geom.MapDisplaySpec;


/**
 * Coordinate systems prefixes:
 * <ul>
 * <li><code>world</code> = meters in source (world) coordinate space
 * <li><code>display</code> = meters in target (display) coordinate space measured from the display centre
 * <li><code>pix</code> = pixels in target (display) coordinate space where (0,0) is the <strong>centre</strong> of the top-left pixel
 * </ul>
 * 
 * Variables:
 * <ul>
 * <li><code>worldCenterX</code> = x position of the window centre</li>
 * <li><code>worldCenterY</code> = y position of the window centre</li>
 * <li><code>worldLenPerPix</code> = world length that is represented by a pixel (scale)</li>
 * <li><code>pixSize</code> = size of the window in pixels</li>
 * </ul>
 * 
 * @author mkadunc, mslenc
 */
public class DisplayCoordinateAdapter extends MapCoordinates {
	public class WorldFromPix extends SimpleTransform.FromInt<CartesianCRS, CartesianCRS> {
		
		public WorldFromPix() {
			super(CRS.MAP_PIXEL_CRS, worldCRS);
		}
		
		@Override
		public double length(double sourceLength) {
			return sourceLength * worldLengthPerPix;
		}
		
		@Override
		public double getScale() {
			return worldLengthPerPix;
		}
		
		@Override
		public double x(double pixX) {
			return worldCenterX + (pixX - 0.5 * (pixDisplaySize.w() - 1)) * worldLengthPerPix;
		}
		
		@Override
		public double y(double pixY) {
			return worldCenterY - (pixY - 0.5 * (pixDisplaySize.h() - 1)) * worldLengthPerPix;
		}
		
		public AffineTransform2D createAffine() {
			return new AffineTransform2D(source, target, 
				getScale(), 0, 0, -getScale(),
				x(0),
				y(0));
		}
	}
	
	public class PixFromWorld extends SimpleTransform.ToInt<CartesianCRS, CartesianCRS> {
		
		public PixFromWorld() {
			super(worldCRS, CRS.MAP_PIXEL_CRS);
		}
		
		@Override
		public double x(double worldX) {
			return 0.5*(pixDisplaySize.w() - 1) + (worldX - worldCenterX) / worldLengthPerPix;
		}
		
		@Override
		public double y(double worldY) {
			return 0.5*(pixDisplaySize.h() - 1) - (worldY - worldCenterY) / worldLengthPerPix;
		}
		
		@Override
		public double length(double sourceLength) {
			return sourceLength / worldLengthPerPix;
		}
		
		@Override
		public double area(double sourceArea) {
			return sourceArea / (worldLengthPerPix * worldLengthPerPix);
		}
		
		@Override
		public double getInverseScale() {
			return worldLengthPerPix;
		}
		
		public int centerX() {
			return xInt(worldCenterX);
		}
		
		public int centerY() {
			return yInt(worldCenterY);
		}
		
		public AffineTransform2D createAffine() {
			return new AffineTransform2D(source, target, 
				getScale(), 0, 0, -getScale(),
				x(0),
				y(0));
		}
	}
	
	public DimI pixDisplaySize = DimI.EMPTY;
	public double pixSizeInMicrons = CoordUtil.getDefaultPixSizeInMicrons();
	
	public transient final PixFromWorld pixFromWorld = new PixFromWorld();
	public transient final WorldFromPix worldFromPix = new WorldFromPix();
	
	public transient Envelope worldRect = new Envelope(0,0,0,0);
	public transient double worldLengthPerPix = Double.POSITIVE_INFINITY;
	private ScaleLevelsSpec zooms;
	private boolean allowNonPreferredZooms = true;
	
	public DisplayCoordinateAdapter(CartesianCRS worldCRS) {
		this(null, worldCRS);
	}
	
	/**
	 * @param worldBounds
	 *          the boundaries that will be used to limit the position of view's
	 *          center (<code>null</code> implies infinite bounds)
	 * @param minWorldPerPix
	 *          minimal scale for the adapter (all settings will be cropped to
	 *          that scale); <code>0</code> means no limits
	 * @param maxWorldPerPix
	 *          minimal scale for the adapter (all settings will be cropped to
	 *          that scale); <code>Double.POSITIVE_INFINITY</code> means no
	 *          limits.
	 */
	public DisplayCoordinateAdapter(DisplayBounds bounds, CartesianCRS worldCRS) {
		super(bounds, worldCRS);
		updateTransient();
	}
	
	public DisplayCoordinateAdapter(DisplayCoordinateAdapter other) {
		this(other.bounds, other.worldCRS);
		setFrom(other);
		updateTransient();
	}
	
	public void setFrom(DisplayCoordinateAdapter other) {
		bounds.scaleBounds.setFrom(other.bounds.scaleBounds);
		bounds.mbr = other.bounds.mbr;
		pixDisplaySize = other.pixDisplaySize;
		pixSizeInMicrons = other.pixSizeInMicrons;
		worldCenterX = other.worldCenterX;
		worldCenterY = other.worldCenterY;
		worldLenPerDisp = other.worldLenPerDisp;
		updateTransient();
	}

	public void setDisplaySize(DimI pixSize) {
		if (!pixDisplaySize.equals(pixSize)) {
			pixDisplaySize = pixSize;
			updateTransient();
			fireNotification(true, true);
			fireSizeChange();
		}
	}
	public void setDisplaySize(int pixW, int pixH) {
		setDisplaySize(DimI.create(pixW, pixH));
	}
	
	protected void fireSizeChange() {
		if (listeners == null) return;
		listeners.fireSizeChange(pixDisplaySize.w(), pixDisplaySize.h());
	}
	
	@Override
	protected void updateTransient() {
		worldLengthPerPix = CoordUtil.worldPerPix(worldLenPerDisp, pixSizeInMicrons);
		
		EnvelopeBuilder eBuilder = new EnvelopeBuilder(worldCRS.getDefaultIdentifier());
		
		if (pixDisplaySize != null) {
			double wrW = pixDisplaySize.w() * worldLengthPerPix;
			double wrH = pixDisplaySize.h() * worldLengthPerPix;
			eBuilder.setMBR(worldCenterX - wrW/2, worldCenterY - wrH/2, worldCenterX + wrW/2, worldCenterY + wrH/2);
		} else {
			eBuilder.setMBR(0, 0, 0, 0);
		}
		worldRect = eBuilder.getEnvelope();
		
	}
	
	public void setWorldLenPerPix(double worldPerPix) {
		setWorldCenterAndScale(worldCenterX, worldCenterY, CoordUtil.scale(worldPerPix,
			pixSizeInMicrons));
	}
	
	/**
	 * 
	 * @return size of the display area in pixels
	 */
	public DimI getDisplaySize() {
		return pixDisplaySize;
	}
	
	public Envelope worldGetClip() {
		return worldRect;
	}
	
	/**
	 * @return scale, inverse of what is used in geography (returns 5000 for scale
	 *         1:5000), using the provided pixel size (see <code>CoordUtils</code>
	 *         )
	 */
	public double getWorldPerPix(double pixSizeMicroParam) {
		return pixSizeMicroParam / (worldLengthPerPix * 1e6);
	}
	
	public double scaleForEnvelope(Envelope envelope) {
		return scaleForEnvelope(envelope.getMinX(), envelope.getMinY(),
			envelope.getMaxX(),envelope.getMaxX());
	}
	
	/**
	 * @param startX
	 *          left coordinate
	 * @param startY
	 *          top or bottom coordinate
	 * @param endX
	 *          right coordinate
	 * @param endY
	 *          bottom or top coordinate
	 * @return scale which should be used to fit the envelope tightly into the
	 *         displayed area
	 */
	public double scaleForEnvelope(double startX, double startY, double endX, double endY) {
		if (pixDisplaySize.isEmpty()) throw new IllegalStateException("Can't determine optimal scale when map size is not set");
		return CoordUtil.scale(
				Math.max(Math.abs(startX - endX) / pixDisplaySize.w(), Math.abs(startY- endY) / pixDisplaySize.h()),
				pixSizeInMicrons);
	}
	
	/**
	 * Sets the display area to the specified envelope
	 * 
	 * @see #scaleForEnvelope(double, double, double, double);
	 * 
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 */
	public void setDisplayedRect(double startX, double startY, double endX, double endY) {
		setDisplayedRect(startX, startY, endX, endY, true);
	}
	
	public void setDisplayedRect(double startX, double startY, double endX, double endY, boolean exact) {
		double cx = 0.5 * (startX + endX);
		double cy = 0.5 * (startY + endY);
		double sc = scaleForEnvelope(startX, startY, endX, endY);
		if (!exact || !allowNonPreferredZooms) {
			sc = getPreferredScaleUp(sc);
		}
		
		setWorldCenterAndScale(cx, cy, sc);
	}
	
	public void pixPan(int dx, int dy) {
		setWorldCenter(worldCenterX + worldFromPix.length(dx), worldCenterY + worldFromPix.length(dy));
	}
	
	public void pixPanWithReference(double wX, double wY, int xPx, int yPx) {
		setWorldCenter(wX + (0.5*pixDisplaySize.w() - xPx - 0.5) * worldLengthPerPix,
					   wY - (0.5*pixDisplaySize.h() - yPx - 0.5) * worldLengthPerPix);
	}

	/**
	 * Adjusts the map center and scale so that the complete provided envelope is visible
	 * at the largest scale (only preferred scales are taken into account). 
	 * 
	 * @see #setDisplayedRect(Envelope, boolean)
	 * @param mbr
	 */
	public void setDisplayedRect(Envelope mbr) {
		setDisplayedRect(mbr, true);
	}
	
	/**
	 * Adjusts the map center and scale so that the provided rectangle is completely visible
	 * at the largest scale possible. 
	 * 
	 * @param mbr
	 * @param exact when true, preferred scales are not taken into account and the rectangle fits tightly into the map view.
	 */
	public void setDisplayedRect(Envelope mbr, boolean exact) {
		setDisplayedRect(mbr.getMinX(), mbr.getMinY(), mbr.getMaxX(), mbr.getMaxY(), exact);
	}
	
	public void zoomAll() {
		setDisplayedRect(bounds.mbr);
	}
	
	public void zoomAll(boolean exact) {
		setDisplayedRect(bounds.mbr, exact);
	}
	
	@Override
	protected void internalSetWorldCenterAndScale(double centerX, double centerY, double scale) {
		if (allowNonPreferredZooms || zooms == null) {
			super.internalSetWorldCenterAndScale(centerX, centerY, scale);
		} else {
			super.internalSetWorldCenterAndScale(centerX, centerY, getPreferredScale(scale));
		}
	}
	
	public ScaleLevelsSpec getPreferredZoomLevels() {
		return zooms;
	}
	
	public double getPreferredScale(double scale) {
		if (zooms == null) return scale;
		return zooms.roundScale(scale, pixSizeInMicrons);
	}
	
	public double getPreferredScaleUp(double scale) {
		if (zooms == null) return scale;
		return zooms.roundScaleUp(scale, pixSizeInMicrons);
	}
	
	public double getPreferredScaleDown(double scale) {
		if (zooms == null) return scale;
		return zooms.roundScaleDown(scale, pixSizeInMicrons);
	}
	
	public void setPreferredZoomLevels(ScaleLevelsSpec zooms) {
		this.zooms = zooms;
	}
	
	public void setAllowNonPreferredZooms(boolean allow) {
		this.allowNonPreferredZooms = allow;
	}
	
	public Point getDisplayCenterWorld() {
		return new Point(worldCenterX, worldCenterY);
	}
	
	public MapDisplaySpec getMapDisplaySpec() {
		return new MapDisplaySpec(worldRect, getScale(), CoordUtil.dpi(pixSizeInMicrons));
	}

	public int getDisplayWidth() {
		return pixDisplaySize.w();
	}
	
	public int getDisplayHeight() {
		return pixDisplaySize.h();
	}

	public static DisplayCoordinateAdapter createCopy(DisplayCoordinateAdapter src) {
		return new DisplayCoordinateAdapter(src);
	}

	public static void main(String[] args) {
		DisplayCoordinateAdapter dca = new DisplayCoordinateAdapter(CRS.NONAME_WORLD_CRS);
		dca.setDisplaySize(100, 100);
		dca.setWorldCenterAndScale(0, 0, 1.0/0.00028);
		System.out.println(dca.pixFromWorld.x(5));
		System.out.println(dca.pixFromWorld.y(2));
		AffineTransform2D aff = dca.pixFromWorld.createAffine();
		System.out.println(aff.x(5, 2));
		System.out.println(aff.y(5, 2));
		AffineTransform2D aff2 = dca.worldFromPix.createAffine();
		System.out.println(aff2.point(new Point(-0.5, -0.5)));
	}
}
