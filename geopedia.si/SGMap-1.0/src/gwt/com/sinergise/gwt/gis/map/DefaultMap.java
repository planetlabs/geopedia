/*
 *
 */
package com.sinergise.gwt.gis.map;


import static com.sinergise.gwt.gis.map.ui.controls.RelativePosition.HorizontalAlignment.LEFT_RIGHT;
import static com.sinergise.gwt.gis.map.ui.controls.RelativePosition.VerticalAlignment.BOTTOM_DOWN;
import static com.sinergise.gwt.ui.core.MouseHandler.BUTTON_LEFT;
import static com.sinergise.gwt.ui.core.MouseHandler.BUTTON_RIGHT;
import static com.sinergise.gwt.ui.core.MouseHandler.MOD_ANY;
import static com.sinergise.gwt.ui.core.MouseHandler.MOD_CTRL;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.geometry.crs.CartesianCRS;
import com.sinergise.common.geometry.display.ScaleBounds;
import com.sinergise.common.geometry.display.ScaleLevelsSpec;
import com.sinergise.common.geometry.tiles.TileUtilGWT;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.geometry.tiles.TiledCRS.AxisSign;
import com.sinergise.common.geometry.tiles.WithBounds;
import com.sinergise.common.geometry.tiles.WithoutZooms;
import com.sinergise.common.gis.map.model.MapViewContext;
import com.sinergise.common.ui.action.ToggleAction;
import com.sinergise.common.util.event.selection.ExcludeContext;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.MapViewSpec;
import com.sinergise.gwt.gis.i18n.Tooltips;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.ViewUndoStack;
import com.sinergise.gwt.gis.map.ui.actions.PanAction;
import com.sinergise.gwt.gis.map.ui.actions.PanScaleMapTouchAction;
import com.sinergise.gwt.gis.map.ui.actions.ZoomBoxAction;
import com.sinergise.gwt.gis.map.ui.actions.ZoomWheelAction;
import com.sinergise.gwt.gis.map.ui.controls.EffectsOverlay;
import com.sinergise.gwt.gis.map.ui.controls.FixedOverlay;
import com.sinergise.gwt.gis.map.ui.controls.PanCtrl;
import com.sinergise.gwt.gis.map.ui.controls.RelativePosition;
import com.sinergise.gwt.gis.map.ui.controls.ScaleBox;
import com.sinergise.gwt.gis.map.ui.controls.ZoomSliderCtrl;
import com.sinergise.gwt.gis.resources.GisTheme;
import com.sinergise.gwt.ui.action.MouseActionBindingsToggleAction;
import com.sinergise.gwt.ui.core.MouseHandler;
import com.sinergise.gwt.ui.core.MouseHandler.MouseActionBinding;


public class DefaultMap extends MapComponent {
	
	public static class MapSettings {
		
		private TiledCRS defaultTiledCRS;
		private CartesianCRS mapCRS;

		private ScaleBounds scaleBounds = new ScaleBounds(500, 5e6, false);

		private int mapScalesPerDecade = 9;
		protected ScaleLevelsSpec scaleLevels = null;
		
		private DimI defaultTileSize = TileUtilGWT.TILE_SIZE_MEDIUM;

		private MapViewSpec initialView;

		private Envelope bounds;

		private boolean panLeft = false;
		
		public DimI getDefaultTileSize() {
			return defaultTileSize;
		}
		public void setDefaultTileSize(DimI defaultTileSize) {
			this.defaultTileSize = defaultTileSize;
		}

		public MapSettings() {
		}
		public MapSettings(CartesianCRS mapCRS) {
			this.mapCRS = mapCRS;
		}

		public MapSettings(CartesianCRS mapCRS, ScaleLevelsSpec scaleLevels) {
			this(mapCRS);
			this.scaleLevels = scaleLevels;
			scaleBounds = scaleLevels.getScaleBounds();
		}

		public MapSettings setMapCRS(CartesianCRS mapCRS) {
			this.mapCRS = mapCRS;
			return this;
		}

		public double getMinScale(double pixSizeMicro) {
			return scaleBounds.minScale(pixSizeMicro);
		}

		public double getMaxScale(double pixSizeMicro) {
			return scaleBounds.maxScale(pixSizeMicro);
		}
		
		public MapSettings setPanLeft(boolean panLeft) {
			this.panLeft = panLeft;
			return this;
		}
		
		public MapSettings setMaxScale(double maxScale) {
			if (scaleBounds instanceof ScaleBounds.InDisp) {
				scaleBounds = new ScaleBounds.InDisp(scaleBounds.minScale(0), maxScale);
			} else {
				throw new IllegalStateException("Cannot set max scale when minScale is set in pixel units");
			}
			return this;
		}
		public MapSettings setMinScale(double minScale) {
			if (scaleBounds instanceof ScaleBounds.InDisp) {
				scaleBounds = new ScaleBounds.InDisp(minScale, scaleBounds.maxScale(0));
			} else {
				throw new IllegalStateException("Cannot set min scale when maxScale is set in pixel units");
			}
			return this;
		}
		
		public void setBounds(Envelope bounds) {
			this.bounds = bounds;
		}
		
		public MapSettings setInitialView(double worldX, double worldY,
				double scale) {
			return setInitialView(new MapViewSpec(worldX, worldY, scale));
		}
		
		public MapSettings setInitialView(MapViewSpec spec) {
			this.initialView = spec;
			return this;
		}

		public ScaleLevelsSpec createMapZooms(double pixSizeMicro) {
			if (scaleLevels == null) {
				return ScaleLevelsSpec.createStandard(getMinScale(pixSizeMicro), getMaxScale(pixSizeMicro),
					mapScalesPerDecade);
			}
			return scaleLevels;
		}
		
		public TiledCRS getTiledCrs() {
			return defaultTiledCRS;
		}
		
		public MapSettings setTiledCrs(TiledCRS tiledCrs) {
			this.defaultTiledCRS = tiledCrs;
			return this;
		}
		
		public CartesianCRS getMapCRS() {
			return mapCRS;
		}
		
		public Envelope getBounds() {
			return bounds;
		}
		
		public MapViewSpec getInitialView() {
			return initialView;
		}
		
		public boolean isPanLeft() {
			return panLeft;
		}
		
	}

	protected final PanAction pan;
	protected final ZoomWheelAction zoom;
	protected final ZoomBoxAction zoomBox;
	
	protected final MapRibbonHolder mapRibbonHolder;
	
	protected transient ToggleAction zoomModeAction;
	protected transient ToggleAction panModeAction;
	protected transient ExcludeContext navigationModeExcludeContext;

	protected final ScaleBox scaleBox;
	protected final PanCtrl panCtrl;
	protected final ZoomSliderCtrl sliderCtrl;

	private final EffectsOverlay effectsOvr = new EffectsOverlay();
	protected final FixedOverlay controlsOvr = new FixedOverlay();
	private final ViewUndoStack undoStack;

	public final TiledCRS defaultTiles;

	public DefaultMap(CartesianCRS crs) {
		this(new MapViewContext(crs));
	}

	public DefaultMap(MapViewContext context) {
		this(context, new MapSettings(context.coords.worldCRS));
	}
	
	public DefaultMap(MapViewContext context, TiledCRS tiledCrs) {
		this(context, new MapSettings(context.coords.worldCRS), tiledCrs);
	}

	public DefaultMap(MapSettings settings) {
		this(createContext(settings), settings);
	}

	public ScaleLevelsSpec getMapZooms() {
		return context.coords.getPreferredZoomLevels();
	}
	
	public ViewUndoStack getUndoStack() {
		return undoStack;
	}

	protected DefaultMap(MapViewContext context, MapSettings settings) {
		this(context, settings, settings.getTiledCrs());
	}
	
	protected DefaultMap(MapViewContext context, MapSettings settings, TiledCRS tiledCrs) {
		super(context);
		
		DimI defaultTileSize = TileUtilGWT.TILE_SIZE_DEFAULT;
		if (settings != null) {
			defaultTileSize = settings.getDefaultTileSize();
			setUserZooms(settings.createMapZooms(coords.pixSizeInMicrons));
			ScaleLevelsSpec userZooms = getUserZooms();
			coords.bounds.setScaleBounds(userZooms
					.minScale(coords.pixSizeInMicrons), userZooms
					.maxScale(coords.pixSizeInMicrons));
			MapViewSpec spc = settings.getInitialView();
			if (spc != null) {
				context.setInitialView(spc.worldCenterX, spc.worldCenterY,
						spc.worldLenPerDisp);
			}
			Envelope bounds = settings.getBounds();
			if (bounds != null) {
				coords.bounds.setMBR(bounds);
			}
		} else if (tiledCrs != null) {
			coords.bounds.setScaleBounds(
					tiledCrs.zoomLevels.minScale(coords.pixSizeInMicrons), 
					tiledCrs.zoomLevels.maxScale(coords.pixSizeInMicrons));
		}

		ScaleLevelsSpec mapZooms = getMapZooms();
		if (tiledCrs != null) {
			defaultTiles = tiledCrs;
		} else if (mapZooms == null) {
			defaultTiles = new WithoutZooms(coords.worldCRS,
					"DefaultTiled", coords.pixSizeInMicrons, coords.bounds.mbr,
					defaultTileSize);
		} else {
			defaultTiles = new WithBounds(coords.worldCRS,
					"DefaultTiled", mapZooms.toPix(coords.pixSizeInMicrons),
					coords.bounds.mbr, defaultTileSize, AxisSign.POSITIVE, AxisSign.POSITIVE);
		}

		// components are organized in overlays which are displayed
		// given a depth priority (Z_TOP_EFFECTS, etc.).
		overlaysPane.insertOverlay(effectsOvr, Z_TOP_EFFECTS, true);
//		overlaysPane.insertOverlay(controlsOvr, Z_TOP_FIXEDCONTROLS, true);
		setControlsOverlayEnabled(true);

		scaleBox = new ScaleBox(coords);
		panCtrl = new PanCtrl(this);
		sliderCtrl = new ZoomSliderCtrl(this);
		sliderCtrl.setVisible(false);
		
		mapRibbonHolder = new MapRibbonHolder();
		mapRibbonHolder.setWidth("100%");
		controlsOvr.add(mapRibbonHolder, 0 ,0);
		
		addToControlsOverlay(panCtrl, 15, 10);
		
//		controlsOvr.add(scaleBox, new RelativePosition(panCtrl, 0, -10,
//				RelativePosition.VerticalAlignment.NONE,
//				RelativePosition.HorizontalAlignment.CENTER));
		
		addToControlsOverlay(scaleBox, 20, -40);
		addToControlsOverlay(sliderCtrl, 43, 100);

		zoom = new ZoomWheelAction(this, effectsOvr);
		mouser.registerAction(zoom, MouseHandler.MOD_NONE);
		
		pan = new PanAction(this);
		zoomBox = new ZoomBoxAction(this, effectsOvr);
		
		touchGestureHandler.registerAction(new PanScaleMapTouchAction(this));

		if (settings != null && settings.isPanLeft()) {
			getActionForPanMode().setSelected(true);
		} else {
			getActionForZoomWindowMode().setSelected(true);
		} 

		undoStack = new ViewUndoStack(this);
	}
	
	public ToggleAction getActionForZoomWindowMode() {
		if (zoomModeAction == null) {
			zoomModeAction = new MouseActionBindingsToggleAction(Tooltips.INSTANCE.toolbar_zoomWindowMode(), mouser, 
				new MouseActionBinding[] {
					MouseActionBinding.create(zoomBox, BUTTON_LEFT, MOD_ANY),
					MouseActionBinding.create(pan, BUTTON_RIGHT, MOD_ANY),
					MouseActionBinding.create(pan, BUTTON_LEFT, MOD_CTRL)
				}, false);
			zoomModeAction.setIcon(GisTheme.getGisTheme().gisStandardIcons().zoomTo());
			
			if (panModeAction != null) {
				getNavigationModeExcludeContext();
			}
		}
		zoomModeAction.setStyle("mapToolbarZoomModeAction");
		return zoomModeAction;
	}
	
	public ToggleAction getActionForPanMode() {
		if (panModeAction == null) {
			panModeAction = new MouseActionBindingsToggleAction(Tooltips.INSTANCE.toolbar_panMode(), getMouseHandler(), 
			new MouseActionBinding[] {
				MouseActionBinding.create(pan, BUTTON_LEFT, MOD_ANY),
				MouseActionBinding.create(zoomBox, BUTTON_RIGHT, MOD_ANY),
				MouseActionBinding.create(zoomBox, BUTTON_LEFT, MOD_CTRL)
			}, false);
			panModeAction.setIcon(GisTheme.getGisTheme().gisStandardIcons().pan());
			
			if (zoomModeAction != null) {
				getNavigationModeExcludeContext();
			}
		}
		panModeAction.setStyle("mapToolbarPanModeAction");
		return panModeAction;
	}
	
	public ExcludeContext getNavigationModeExcludeContext() {
		if (navigationModeExcludeContext == null) {
			navigationModeExcludeContext = new ExcludeContext();
			navigationModeExcludeContext.setForceSelection(true);
			navigationModeExcludeContext.register(zoomModeAction);
			navigationModeExcludeContext.register(panModeAction);
		}
		return navigationModeExcludeContext;
	}

	protected static MapViewContext createContext(MapSettings settings) {
		MapViewContext ret = new MapViewContext(settings.getMapCRS());
		ret.setPreferredZoomLevels(settings.createMapZooms(ret.coords.pixSizeInMicrons));
		return ret;
	}

	// Enable/disable the scalebox.
	public void setScaleBoxEnabled(boolean showScaleBox) {
		scaleBox.setVisible(showScaleBox);
	}
	public boolean isScaleBoxEnabled() {
		return scaleBox.isVisible();
	}

	// Enable/disable the panning control.
	public void setPanCtrlEnabled(boolean showPanCtrl) {
		panCtrl.setVisible(showPanCtrl);
	}
	public boolean isPanCtrlEnabled() {
		return panCtrl.isVisible();
	}

	// Enable/disable the zoom slider.
	public void setSliderCtrlEnabled(boolean showSliderCtrl) {
		sliderCtrl.setVisible(showSliderCtrl);
	}
	public boolean isSliderCtrlEnabled() {
		return sliderCtrl.isVisible();
	}

	// Enable/disable the effects overlay.
	public void setEffectsOverlayEnabled(boolean showOverlay) {
		effectsOvr.setVisible(showOverlay);
	}
	public boolean isEffectsOverlayEnabled() {
		return effectsOvr.isVisible();
	}

	// Enable/disable the controls overlay.
	public void setControlsOverlayEnabled(boolean showOverlay) {
		if (showOverlay && !isControlsOverlayEnabled()) {
			overlaysPane.insertOverlay(controlsOvr, Z_TOP_FIXEDCONTROLS, true);
		} else if (!showOverlay){
			overlaysPane.removeOverlay(controlsOvr);
		}
		controlsOvr.setVisible(showOverlay);
	}

	public EffectsOverlay getEffectsOverlay() {
		return effectsOvr;
	}

	public void addToControlsOverlay(Widget w, int posX, int posY) {
		if (mapRibbonHolder != null) {
			controlsOvr.add(w, new RelativePosition(mapRibbonHolder, posX, posY, BOTTOM_DOWN, LEFT_RIGHT));
		} else {
			controlsOvr.add(w, posX, posY);
		}
	}

	public boolean isControlsOverlayEnabled() {
		return controlsOvr.isVisible() && overlaysPane.hasOverlay(controlsOvr);
	}
	
	public void prepareForPrint() {
		overlaysPane.removeOverlay(controlsOvr);
		setEffectsOverlayEnabled(false);
	}
	
	public MapRibbonHolder getMapRibbonHolder() {
		return mapRibbonHolder;
	}
	
	
	public static class MapRibbonHolder extends SimplePanel {
		
		private Widget defaultRibbon;
		
		public void setDefaultRibbon(Widget defaultRibbon) {
			this.defaultRibbon = defaultRibbon;
			if (getWidget() == null) {
				super.setWidget(defaultRibbon);
			}
		}
		
		@Override
		public void setWidget(Widget w) {
			if (w == null) {
				w = defaultRibbon;
			}
			super.setWidget(w);
		}
		
		//Prevent ribbon events from propagating to the map.
		{
			sinkEvents(Event.MOUSEEVENTS);
		}
		
		@Override
		public void onBrowserEvent(Event event) {
             DOM.eventCancelBubble(event, true);  
             super.onBrowserEvent(event);
		}
	}
}
