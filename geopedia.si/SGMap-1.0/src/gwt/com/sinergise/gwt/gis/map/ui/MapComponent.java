package com.sinergise.gwt.gis.map.ui;



import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.display.ScaleLevelsSpec;
import com.sinergise.common.geometry.display.event.CoordinatesListener;
import com.sinergise.common.gis.app.ApplicationContext;
import com.sinergise.common.gis.map.model.MapViewContext;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.gis.map.model.layer.LayersSource;
import com.sinergise.common.gis.map.model.layer.MapContextLayers;
import com.sinergise.common.gis.map.model.layer.system.SelectionSetLayer;
import com.sinergise.common.gis.map.render.RenderInfo;
import com.sinergise.common.gis.map.render.RepaintListener;
import com.sinergise.common.gis.map.render.RepaintListenerCollection;
import com.sinergise.common.util.collections.tree.TreeListenerAdapter;
import com.sinergise.common.util.collections.tree.TreeVisitor;
import com.sinergise.common.util.event.selection.ExcludeContext;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.MapViewSpec;
import com.sinergise.gwt.gis.map.util.MapCoordinatesHistoryHandler;
import com.sinergise.gwt.gis.map.util.MapLayersHistoryHandler;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.ui.PixSizedComposite;
import com.sinergise.gwt.ui.core.MouseHandler;
import com.sinergise.gwt.ui.gesture.TouchGestureHandler;
import com.sinergise.gwt.util.html.CSS;


public class MapComponent extends PixSizedComposite implements IMapComponent {
	public static final int Z_BACKGROUND = 0;
	public static final int Z_BASE_LAYERS = 10;
	public static final int Z_FEATURES_AREA = 20;
	public static final int Z_FEATURES_LINE = 30;
	public static final int Z_FEATURES_POINT = 40;
	public static final int Z_FEATURES_TEXT = 50;
	public static final int Z_FEATURES_TOP = 60;
	public static final int Z_SELECTION = 70;
	public static final int Z_HIGHLIGHT_LAYERS = 75;
	public static final int Z_TOP_CONSTRUCTION = 80;
	public static final int Z_TOP_EFFECTS = 90;
	public static final int Z_TOP_FIXEDCONTROLS = 100;
	
	public final OverlaysPanel overlaysPane;
	public final MouseHandler mouser;
	public final TouchGestureHandler touchGestureHandler;
	public final MapViewContext context;
	public final DisplayCoordinateAdapter coords;
	private ScaleLevelsSpec userZoomLevels;
	private Set<LayersSource> registeredSources = new HashSet<LayersSource>();
	List<MapInitializedListener> initCompleteListeners = new ArrayList<MapComponent.MapInitializedListener>();
	
	private boolean primary;
	
	public class OverlaysPanel extends AbsoluteDeckPanel.FocusableDeckPanel implements IOverlaysHolder{
		private RenderInfo[] specs;
		
		public OverlaysPanel() {
			CSS.position(getElement(), CSS.POS_RELATIVE);
			CSS.overflow(getElement(), CSS.OVR_HIDDEN);
			setStyleName(StyleConsts.MAPOVERLAYS);
		}
		
		public void positionTiles(DisplayCoordinateAdapter dca, boolean quick) {
			prepareToRender(dca, quick);
			hideNonShowingLayers();
			reposition();
			context.layers.afterRepaint();
		}

		@SuppressWarnings({"rawtypes", "unchecked"})
		private void reposition() {
			for (int i = 0; i < getWidgetCount(); i++) {
				reposition((OverlayComponent)getChildWidget(i), specs[i]);
			}
		}

		private void prepareToRender(DisplayCoordinateAdapter dca, boolean quick) {
			int size = getWidgetCount();
			if (specs == null || specs.length < size) {
				specs = new RenderInfo[size];
			}
			boolean trans = false;
			for (int i = 0; i < size; i++) {
				specs[i] = prepareToRender((OverlayComponent<?>)getChildWidget(i), dca, quick, trans);
				trans = trans || specs[i].hasAnything;
			}
		}

		private <T extends RenderInfo> T prepareToRender(OverlayComponent<T> ovr, DisplayCoordinateAdapter dca, boolean quick, boolean trans) {
			T spec = ovr.prepareToRender(dca, trans, quick);
			spec.dca = dca;
			return spec;
		}

		private void hideNonShowingLayers() {
			boolean hasOpaq = false;
			for (int i = getWidgetCount() - 1; i >= 0; i--) {
				OverlayComponent<?> ovr = (OverlayComponent<?>) getChildWidget(i);
				if (hasOpaq || !specs[i].hasAnything) {
					ovr.setVisible(false);
				} else {
					ovr.setVisible(true);
					if (!specs[i].isTransparent) {
						hasOpaq = true;
					}
				}
			}
		}

		private <T extends RenderInfo> void reposition(OverlayComponent<T> ovr, T info) {
			if (ovr.isVisible()) {
				ovr.reposition(info);
			}
		}
		
		@Override
		public void insertOverlay(OverlayComponent<?> overlay, int zIndex, boolean end) {
			
			overlay.zIndex = zIndex;
			int cCount = getWidgetCount();
			if (cCount < 1) {
				// Empty, just do the adding
				insert(overlay, 0);
				return;
			}
			
			for (int i = 0; i < cCount; i++) {
				OverlayComponent<?> cmp = (OverlayComponent<?>) getChildWidget(i);
				if ((end && cmp.zIndex > zIndex) || (!end && cmp.zIndex >= zIndex)) {
					insert(overlay, i);
					return;
				}
			}
			
			// Not found, add to the end
			insert(overlay, cCount);
		}
		
		public void insert(Widget w, int beforeIndex) {
			super.insertChildWidget(w, beforeIndex);
			refresh();
			repaint(1000);
		}
		
		public void insertOverlay(OverlayComponent<?> overlay, int zIndex) {
			insertOverlay(overlay, zIndex, true);
		}
		
		public boolean hasOverlay(OverlayComponent<?> overlay) {
			return panel.contains(overlay);
		}
		
		// Enable/disable an group of overlays by zIndex.
		public void setOverlayGroupEnabled(int zIndex, boolean showGroup) {
			int cnt = getWidgetCount();
			for (int i = 0; i < cnt; i++) {
				OverlayComponent<?> cmp = (OverlayComponent<?>) getChildWidget(i);
				if (cmp.zIndex == zIndex) cmp.setVisible(showGroup);
			}
		}
		@Override
		public void removeOverlay(OverlayComponent<?> ovr) {
			removeChildWidget(ovr);
			refresh();
			repaint(1000);
		}
		
	}
	
	private class InitialTimer extends Timer {
		public boolean finished = false;
		
		public InitialTimer() {}

		@Override
		public void run() {
			if (finished) return;
			if (context != null && !coords.pixDisplaySize.isEmpty() && isAttached()) {
				finished = true;
				internalInitialize();
			} else {
				if (coords.pixDisplaySize.isEmpty()) {
					updateSize();
				}
				schedule();
			}
		}

		public boolean schedule() {
			if (finished) {
				return false;
			}
			schedule(1000);
			return true;
		}
		
		@Override
		public void schedule(int delayMillis) {
			if (finished) {
				return;
			}
			super.schedule(delayMillis);
		}
	}
	
	private class UpdateTimer extends Timer {
		private boolean updateScheduled = false;
		private long lastUpd;
		
		private boolean hardScheduled = false;
		
		public UpdateTimer() {}

		@Override
		public void run() {
			run(true);
		}
		
		public void run(boolean checkScheduled) {
			if (checkScheduled && (!updateScheduled && !hardScheduled)) return;
			boolean doHard = hardScheduled;
			updateScheduled = false;
			hardScheduled = false;
			if (!initialized() || overlaysPane == null) return;
			if (rlc != null) {
				if (!rlc.fireBeforeRepaint(doHard)) return;
				rlc.fireRepaint(doHard);
			}
			lastUpd = System.currentTimeMillis();
			if (doHard) {
				overlaysPane.positionTiles(coords, false);
			} else {
				overlaysPane.positionTiles(coords, true);
			}
		}
		
		public void doUpdate(int millis, boolean cont, boolean hard) {
			if (rlc != null) {
				if (!rlc.fireBeforeScheduled(millis, cont, hard)) return;
				rlc.fireScheduled(millis, cont, hard);
			}
			if (cont) hardScheduled = false;
			if (millis < 5) {
				long cur = System.currentTimeMillis();
				if (cur - lastUpd > 5) {
					cancel();
					updateScheduled = true;
					if (!hardScheduled) hardScheduled = hard;
					run(false);
				} else {
					updateScheduled = true;
					if (!hardScheduled) hardScheduled = hard;
					schedule(10);
				}
				return;
			}
			if (cont) {
				hardScheduled = false;
				if (updateScheduled) return;
				updateScheduled = true;
				schedule(millis);
			} else {
				cancel();
				updateScheduled = true;
				if (!hardScheduled) hardScheduled = hard;
				schedule(millis);
			}
		}
	}
	
	UpdateTimer updTimer = new UpdateTimer();
	private InitialTimer initialTimer = new InitialTimer();
	
	private ExcludeContext ec = new ExcludeContext();
	
	public MapComponent(MapViewContext context) {
		this(context, true);
	}
	
	public MapComponent(MapViewContext context, boolean primaryMap) {
		this.context = context;
		this.coords = context.coords;
		this.primary = primaryMap;
		initWidget(overlaysPane = new OverlaysPanel());
		setStylePrimaryName(StyleConsts.MAPCOMPONENT);
		
		mouser = new MouseHandler(overlaysPane);
		mouser.setContextMenuAllowed(false);
		mouser.setSelectionAllowed(false);
		MouseHandler.preventContextMenu(getElement());
		
		touchGestureHandler = new TouchGestureHandler(this);
		
		coords.addCoordinatesListener(new CoordinatesListener() {
			@Override
			public void coordinatesChanged(double newX, double newY, double newScale, boolean coordsChanged, boolean scaleChanged) {
				if (scaleChanged) {
					updatePos(200, true, false);
				} else {
					updatePos(0, true, false);
				}
			}
			
			@Override
			public void displaySizeChanged(int newWidthPx, int newHeightPx) {}
		});
		
		context.addTreeListener(new TreeListenerAdapter<LayerTreeElement>() {
			@Override
			public void nodeChanged(LayerTreeElement node, String propertyName) {
				if (node == null || LayerTreeElement.PROP_ON.equals(propertyName)) {
					refresh();
					repaint(1000);
				} else {
					LayerTreeElement el = node;
					if (el.hasAnythingToRender(coords)) {
						refresh();
						repaint(el.getRenderDelayOnChange());
					}
				}
			}
		});
	}

	public boolean initialized() {
		return isAttached() && (initialTimer == null || initialTimer.finished);
	}
	
	@Override
	public ScaleLevelsSpec getUserZooms() {
		if (userZoomLevels != null) return userZoomLevels;
		return coords.getPreferredZoomLevels();
	}
	
	public void setUserZooms(ScaleLevelsSpec spec) {
		userZoomLevels = spec;
	}
	
	public void setCenter(double x, double y) {
		coords.setWorldCenter(x, y);
	}
	
	public double getScale() {
		return coords.worldLenPerDisp;
	}

	@Override
	public MouseHandler getMouseHandler() {
		return mouser;
	}
	
	@Override
	public void repaint(int delayMillis) {
		updatePos(delayMillis, false, true);
	}
	
	@Override
	public void refresh() {
		refresh(100, false);
	}
	
	@Override
	public void refresh(int millis, boolean continuous) {
		updatePos(millis, continuous, false);
	}
	
	public void updatePos(int millis, boolean continuous, boolean hard) {
		updTimer.doUpdate(millis, continuous, hard);
	}
	
	public void setDisplayedRect(double startX, double startY, double endX, double endY) {
		if (initialized()) {
			coords.setDisplayedRect(startX, startY, endX, endY);
		} else {
			coords.setWorldCenter(0.5 * (startX + endX), 0.5 * (startY + endY));
			context.setInitialView(new Envelope(startX, startY, endX, endY));
		}
	}
	
	public void setViewCenterScale(double cx, double cy, double scale) {
		coords.setWorldCenterAndScale(cx, cy, scale);
		updateInitView();
	}
	
	public void setViewCenterScale(MapViewSpec view) {
		setViewCenterScale(view.worldCenterX, view.worldCenterY, view.worldLenPerDisp);
	}
	
	private void updateInitView() {
		if (initialized()) {
			return;
		}
		context.setInitialView(
			coords.worldCenterX,
			coords.worldCenterY, 
			coords.getScale()
		);
	}
	
	/**
	 * @param envelope
	 *          rectangle to be displayed
	 * @param maxScaleFactor
	 *          max factor between optimal and current scale
	 * @param minScale
	 *          min possible scale (e.g. for point envelopes)
	 * @param paddingRatio
	 *          ratio of padding on all sides of the rectangle (0 means tight fit)
	 */
	public void ensureDisplay(Envelope envelope, double maxScaleFactor, double minScale,
			double paddingRatio) {
		double optScale = Double.NaN;
		double paddingW = (envelope.getMaxX() - envelope.getMinX()) * 2.0 * paddingRatio;
		double paddingH = (envelope.getMaxY() - envelope.getMinY()) * 2.0 * paddingRatio;
		
		// Add padding
		double minX = envelope.getMinX() - paddingW;
		double minY = envelope.getMinY() - paddingH;
		double maxX = envelope.getMaxX() + paddingW;
		double maxY = envelope.getMaxY() + paddingH;
		
		if (!initialized()) {
			setDisplayedRect(minX, minY, maxX, maxY);
			return;
		}
		
		// Determine optimum scale
		if (envelope.isPoint()) { // point
			optScale = minScale;
		} else {
			optScale = coords.scaleForEnvelope(minX, minY, maxX, maxY);
		}
		
		// See if we can live with the difference in scale
		double scaleRatio = optScale / coords.worldLenPerDisp;
		if (scaleRatio > maxScaleFactor) {
			optScale = optScale / maxScaleFactor;
		} else if (1.0 / scaleRatio > maxScaleFactor) {
			optScale = optScale * maxScaleFactor;
		} else {
			optScale = coords.worldLenPerDisp;
		}
		if (optScale < minScale) optScale = minScale;
		
		// set the scale
		if (optScale != coords.worldLenPerDisp) {
			coords.setScale(optScale);
		}
		
		// now we can add padding for points (before we had the wrong map size)
		if (paddingW == 0) {
			paddingW = coords.worldRect.getWidth() * paddingRatio;
			minX -= paddingW;
			maxX += paddingW;
		}
		
		if (paddingH == 0) {
			paddingH = coords.worldRect.getHeight() * paddingRatio;
			minY -= paddingH;
			maxY += paddingH;
		}
		
		// do we have to move the center?
		if (!coords.worldGetClip().contains(minX, minY, maxX, maxY)) {
			coords.setWorldCenter(0.5 * (minX + maxX), 0.5 * (minY + maxY));
		}
		
		// if moving the center wasn't enough, we just set the whole thing
		if (!coords.worldGetClip().contains(minX, minY, maxX, maxY)) {
			setDisplayedRect(minX, minY, maxX, maxY);
		}
	}
	
	@Override
	protected void componentResized(DimI newSize) {
		if (!newSize.isEmpty()) {
			coords.setDisplaySize(newSize);
			if (initialized()) {
				updatePos(0, true, false);
				updatePos(1000, false, true);
			}
		}
	}
	
	public void setScale(double scale) {
		coords.setScale(scale);
	}
	
	public MapContextLayers getLayers() {
		return context.layers;
	}
	
	public MapViewContext getMapViewContext() {
		return context;
	}
	
	@Override
	public DisplayCoordinateAdapter getCoordinateAdapter() {
		return coords;
	}
	
	public void registerSource(LayersSource source) {
		registeredSources.add(source);
	}
	
	private void initSources() {
		//find sources from layers
		context.layers.traverseDepthFirst(new TreeVisitor<LayerTreeElement>() {
			@Override
			public boolean visit(LayerTreeElement node) {
				if (node instanceof Layer) {
					registerSource(((Layer) node).getSource());
				}
				return true;
			}
		});
		
		final HashSet<LayersSource> sourcesToInit = new HashSet<LayersSource>(registeredSources);

		if (!sourcesToInit.isEmpty()) {
			LayersSource[] srcArr = sourcesToInit.toArray(new LayersSource[sourcesToInit.size()]);
			for (LayersSource ls : srcArr) {
				final LayersSource curSrc = ls;
				try {
					curSrc.asyncInitialize(new AsyncCallback<LayersSource>() {
						@Override
						public void onFailure(Throwable caught) {
							throw new RuntimeException(caught);
						}
						@Override
						public void onSuccess(LayersSource result) {
							srcCameBack(curSrc);
						}
						private void srcCameBack(LayersSource src) {
							sourcesToInit.remove(src);
							if (sourcesToInit.isEmpty()) {
								onSourcesInitComplete();
							}
						}
					});
				} catch (Throwable t) {
					t.printStackTrace();
					sourcesToInit.remove(curSrc);
					if (sourcesToInit.isEmpty()) {
						onSourcesInitComplete();
					}
				}
			}
		} else {
			onSourcesInitComplete();
		}
	}
	
	void onSourcesInitComplete() {
		
		try {
			//register layers and overlays
			context.layers.traverseDepthFirst(new TreeVisitor<LayerTreeElement>() {
				OverlayComponent<?> current;
				@Override
				public boolean visit(LayerTreeElement node) {
					if (node instanceof Layer) {
						Layer lyr = (Layer) node;
						if (lyr.isRenderable()) {
							OverlayComponent<?> cmp = OverlaysFactory.INSTANCE.addLayer(coords.worldCRS, current, lyr);
							if (cmp != current) {
								overlaysPane.insertOverlay(cmp, Z_BASE_LAYERS, true);
							}
							current = cmp;
						}
					}
					return true;
				}
			});
		} finally {
			onInitializationComplete();
		}
	}

	public void initContextOverlays() {
		//init sources first layers and overlays will be inited after that
		initSources();
	}
	
	public ExcludeContext getToolsExcludeContext() {
		return ec;
	}
	
	RepaintListenerCollection rlc;
	
	@Override
	public void addRepaintListener(RepaintListener l) {
		if (l == null) return;
		if (rlc == null) rlc = new RepaintListenerCollection();
		rlc.add(l);
	}
	
	@Override
	public void removeRepaintListener(RepaintListener l) {
		if (rlc == null || l == null) return;
		rlc.remove(l);
	}
	
	public void addInitCompleteListener(MapInitializedListener listener) {
		if (initialized()) { // just fire if already initilized
			listener.mapComponentInited(this);
		} else {
			initCompleteListeners.add(listener);
		}
	}

	protected void onInitializationComplete() {
		ApplicationContext.getInstance().mapInitialized(MapComponent.this, context);
		initialTimer.schedule();
	}
	
	/**
	 * Binds map component coordinates with history handler.
	 */
	public void bindWithHistory() {
		if (initialized()) {
			MapCoordinatesHistoryHandler.bind(this);
			MapLayersHistoryHandler.bind(this);
		} else {
			addInitCompleteListener(new MapInitializedListener() {
				@Override
				public void mapComponentInited(MapComponent map) {
					bindWithHistory();
				}
			});
		}
	}
	
	protected void setPrimary(boolean primary) {
		if (initialized()) {
			throw new RuntimeException("Map primary property must be set before map is initialized.");
		}
		this.primary = primary;
	}
	
	@Override
	public boolean isPrimary() {
		return primary;
	}
	
	/**
	 * @param featureType - can be null if not known
	 * @return Default highlight layer for the provided featureType.
	 */
	public SelectionSetLayer getDefaultHighlightLayer() {
		if (context.defaultHighlightLayer == null) {
			 
			context.defaultHighlightLayer = new SelectionSetLayer("highlight", "Highlighted Features");
			context.defaultHighlightLayer.setVisible(false);
			context.defaultHighlightLayer.setOn(true);
	        getLayers().getRootLayer().addLayer(context.defaultHighlightLayer);
	        
	        OverlayComponent<?> cmp = OverlaysFactory.INSTANCE.addLayer(coords.worldCRS, null, context.defaultHighlightLayer);
			if (cmp != null) {
				overlaysPane.insertOverlay(cmp, Z_HIGHLIGHT_LAYERS, true);
			}
			
		}
		return context.defaultHighlightLayer;
	}
	
	protected void internalInitialize() {
		context.applyInitialView();
		repaint(100);
		for (MapInitializedListener listener : initCompleteListeners) {
			listener.mapComponentInited(MapComponent.this);
		}
	}
	
	public static interface MapInitializedListener {
		void mapComponentInited(MapComponent map);
	}

	@Override
	public IOverlaysHolder getOverlaysHolder() {
		return overlaysPane;
	}


	
}
