package com.sinergise.geopedia.client.ui.map;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Panel;
import com.sinergise.common.geometry.crs.CartesianCRS;
import com.sinergise.common.geometry.display.DisplayBounds;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.display.ScaleBounds;
import com.sinergise.common.geometry.display.ScaleLevelsSpec;
import com.sinergise.common.geometry.display.ScaleLevelsSpec.ZoomLevelsPix;
import com.sinergise.common.geometry.display.event.CoordinatesListener;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.gis.map.render.RepaintListener;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.map.layers.MapLayers;
import com.sinergise.gwt.gis.map.ui.IMapComponent;
import com.sinergise.gwt.gis.map.ui.IOverlaysHolder;
import com.sinergise.gwt.gis.map.ui.OverlayComponent;
import com.sinergise.gwt.gis.map.ui.actions.PanAction;
import com.sinergise.gwt.gis.map.ui.actions.PanScaleMapTouchAction;
import com.sinergise.gwt.gis.map.ui.actions.ZoomBoxAction;
import com.sinergise.gwt.gis.map.ui.actions.ZoomClickAction;
import com.sinergise.gwt.gis.map.ui.actions.ZoomWheelAction;
import com.sinergise.gwt.gis.map.ui.controls.EffectsOverlay;
import com.sinergise.gwt.ui.PixSizedComposite;
import com.sinergise.gwt.ui.core.MouseHandler;
import com.sinergise.gwt.ui.gesture.TouchGestureHandler;
import com.sinergise.gwt.util.html.CSS;

public class MapComponent extends PixSizedComposite implements IMapComponent {

	protected static final int Z_EFFECTS=200;
	
	private class UpdateTimer extends Timer
	{
		private boolean updateScheduled = false;

		public void run()
		{
           
			updateScheduled = false;

			if (mapOverlays != null) {
				mapOverlays.positionTiles(dca, false);
			}
		}

		public void doUpdate(int millis, boolean cont)
		{
			if (millis == 0) {
				cancel();
				run();
				return;
			}
			if (cont) {
				if (updateScheduled)
					return;
				else {
					updateScheduled = true;
					schedule(millis);
				}
			} else {
				cancel();
				updateScheduled = true;
				schedule(millis);
			}
		}
	}

	private UpdateTimer updTimer = new UpdateTimer();
	protected DisplayCoordinateAdapter dca;
	private boolean attached=false;
	
	protected MapOverlaysContainer mapOverlays;
	protected MapLayers layers=new MapLayers();
	
	protected ZoomLevelsPix userZoomLevels = null;

	protected MouseHandler mouser;
	protected TouchGestureHandler touchHandler;
	private EffectsOverlay effectsOvr = new EffectsOverlay();
	
	
	
	protected PanAction mouseActionPan;
	protected ZoomWheelAction mouseActionZoom;
	protected ZoomBoxAction mouseActionZoomBox;
	protected ZoomClickAction mouseActionZoomIn;
	protected PanScaleMapTouchAction touchActionPanScale;
	
	public MapComponent() {
		TiledCRS crs = ClientGlobals.getMainCRS();		
		dca = new DisplayCoordinateAdapter(
				new DisplayBounds(crs.getBounds().mbr,  new ScaleBounds.InDisp(crs.zoomLevels.minWorldPerPix(),crs.zoomLevels.maxWorldPerPix())), 
				(CartesianCRS)crs.baseCRS);//new CartesianCRS(0, GeopediaTiledCRS.GP_BOUNDS));		
		ZoomLevelsPix zlp = crs.zoomLevels;
		dca.setPreferredZoomLevels(zlp);
		dca.bounds.setScaleBounds(zlp.scale(zlp.getMaxLevelId(), dca.pixSizeInMicrons), zlp.scale(zlp.getMinLevelId(), dca.pixSizeInMicrons));
		dca.setAllowNonPreferredZooms(false);
		

	}
	
	
	public MapComponent(Panel mapPanel) {
		this();
		initializeGUI(mapPanel);
	}
	
	
	private void setInitialPositionAndScale() {
		double sc=0;
		sc = getUserZooms().scale(9, dca.pixSizeInMicrons);
		double cX=0, cY=0;
		Envelope mbr=null;
		if (dca.bounds != null) {
			mbr=dca.bounds.mbr;
		}
		if (mbr==null) {
			mbr=dca.worldCRS.bounds2D;
		}
		if (Double.isInfinite(sc) || !(sc>0)) {
			sc=dca.scaleForEnvelope(mbr.minX, mbr.minY, mbr.maxX, mbr.maxY);
		}
		if (mbr!=null && !mbr.isEmpty()) {
			cX=mbr.getCenterX();
			cY=mbr.getCenterY();
		}
	
		dca.setWorldCenterAndScale(cX, cY, dca.getPreferredScale(sc));
	}
	public void addOverlay(OverlayComponent overlay, int zIndex) {
		mapOverlays.insertOverlay(overlay, zIndex);
	}
	
	public void removeOverlay(OverlayComponent overlay) {
		mapOverlays.removeOverlay(overlay);
	}
	
	@Override
	protected void componentResized(DimI newSize) {
		dca.setDisplaySize(newSize);
		if (!attached) {
			setInitialPositionAndScale();
			attached=true;
		}
		updatePos(5,false);
		
	}
	
	protected void updatePos(int millis, boolean continuous)
	{
		updTimer.doUpdate(millis, continuous);
	}
	
	
	protected void initializeGUI(Panel container)
	{
		initWidget(container);
		mapOverlays = new MapOverlaysContainer();
		CSS.fullSize(mapOverlays.getElement());
		container.add(mapOverlays);
		
		mouser = new MouseHandler(this);
		mouser.setContextMenuAllowed(false);
		mouser.setSelectionAllowed(false);
		
		touchHandler = new TouchGestureHandler(this);

		dca.addCoordinatesListener(new CoordinatesListener() {
			
			@Override
			public void displaySizeChanged(int newWidthPx, int newHeightPx) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void coordinatesChanged(double newX, double newY, double newScale,
					boolean coordsChanged, boolean scaleChanged) {
				updTimer.doUpdate(0, true);
				
			}
		});
          
		
		addOverlay(effectsOvr, Z_EFFECTS);

		mouseActionPan = new PanAction(this);
		mouseActionZoom = new ZoomWheelAction(this, effectsOvr);
		mouseActionZoomBox = new ZoomBoxAction(this, effectsOvr);
		mouseActionZoomIn = new ZoomClickAction(this,1);
		registerMouseActions();
		
		touchActionPanScale = new PanScaleMapTouchAction(this);
		registerTouchActions();
	}

	

	public void  registerMapMousePan() {
		registerMapMousePan(MouseHandler.MOD_NONE);
	}

	public void  registerMapMousePan(int mod) {
		mouser.registerAction(mouseActionPan, Event.BUTTON_LEFT, mod);
	}
	
	public void registerMapMouseZoomBox() {
		mouser.registerAction(mouseActionZoomBox, Event.BUTTON_RIGHT, MouseHandler.MOD_NONE);
	}
	
	protected void registerMouseActions() {
		registerMapMousePan();
		mouser.registerAction(mouseActionZoomBox, Event.BUTTON_RIGHT, MouseHandler.MOD_NONE);
		mouser.registerAction(mouseActionZoom, MouseHandler.MOD_NONE);
		mouser.registerAction(mouseActionZoomIn,Event.BUTTON_LEFT,MouseHandler.MOD_NONE,2);
	}

	protected void registerTouchActions() {
		touchHandler.registerAction(touchActionPanScale);
	}
	
	
	public MapLayers getMapLayers() {
		return layers;
	}

	
	public int getZoomLevel() {
		return getUserZooms().nearestZoomLevel(dca.getScale(), dca.pixSizeInMicrons);
	}

	public void ensureVisible (Envelope envelope, boolean force, boolean isPoints) {
		if (envelope.isPoint()) {
			if (getZoomLevel() < 14 && force)
				dca.setScale(getUserZooms().scale(17, dca.pixSizeInMicrons));

			double cx = dca.pixFromWorld.x(envelope.getCenterX());
			double cy = dca.pixFromWorld.y(envelope.getCenterY());
			if (cx < 32 || cy < 32 || cx > dca.getDisplayWidth() - 32 || cy > dca.getDisplayHeight() - 32 || force)
				dca.setWorldCenter(envelope.getCenterX(), envelope.getCenterY());
		} else {
			if (!dca.worldRect.contains(envelope)) {
				dca.setDisplayedRect(envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY());
			} else {
				if (isPoints) {
					double currW = dca.pixFromWorld.length(envelope.getWidth());
					double currH = dca.pixFromWorld.length(envelope.getHeight());
					if (currW < 32 && currH < 32 || (force && currW<128 && currH<128))
						dca.setDisplayedRect(envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY());
				} else {
					double sc = dca.scaleForEnvelope(envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY());
					int currentZoomLevel = getZoomLevel();
					int wantedZoomLevel = getUserZooms().nearestZoomLevel(sc, dca.pixSizeInMicrons);
					if (Math.abs(currentZoomLevel-wantedZoomLevel)>2 || force) {
						dca.setDisplayedRect(envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY());
					}
			}
			}
		}

		}
	
	@Override
	public void addRepaintListener(RepaintListener l) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void removeRepaintListener(RepaintListener l) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void repaint(int delay) {
		refresh(delay, false);
	}

	@Override
	public void refresh() {
		refresh(100, false);
	}
	
	@Override
	public void refresh(int millis, boolean continuous) {
		updatePos(millis, continuous);
	}
	
	@Override
	public DisplayCoordinateAdapter getCoordinateAdapter() {
		return dca;
	}

	@Override
	public ScaleLevelsSpec getUserZooms() {
		if (userZoomLevels==null)
			return dca.getPreferredZoomLevels();
		else
			return userZoomLevels;
	}
	
	@Override
	public boolean isPrimary() {
		return true;
	}


	@Override
	public MouseHandler getMouseHandler() {
		return mouser;
	}


	@Override
	public IOverlaysHolder getOverlaysHolder() {
		return mapOverlays;
	}



}
