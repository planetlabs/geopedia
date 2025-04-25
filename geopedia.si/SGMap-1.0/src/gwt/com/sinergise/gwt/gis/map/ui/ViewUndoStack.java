/*
 *
 */
package com.sinergise.gwt.gis.map.ui;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.display.event.CoordinatesListener;
import com.sinergise.common.gis.map.render.RepaintListenerAdapter;
import com.sinergise.common.gis.map.ui.IMap;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.util.collections.UndoStack;
import com.sinergise.common.util.geom.MapViewSpec;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.gwt.gis.i18n.Tooltips;
import com.sinergise.gwt.gis.resources.GisTheme;

public class ViewUndoStack {

	public static final class ViewUndoToken extends MapViewSpec {
		public final long timestamp;

		public ViewUndoToken(double cX, double cY, double sc) {
			super(cX, cY, sc);
			timestamp = System.currentTimeMillis();
		}

		public boolean coordEquals(ViewUndoToken lastLoc) {
			return super.equals(lastLoc);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + MathUtil.hashCode(timestamp);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			ViewUndoToken other = (ViewUndoToken) obj;
			if (timestamp != other.timestamp)
				return false;
			return true;
		}
	}

	public static final class UndoRedoAction extends Action {
		private final ViewUndoStack stack;
		private final boolean undo;

		public UndoRedoAction(ViewUndoStack stack, String name, boolean undo) {
			super(name);
			this.stack = stack;
			this.undo = undo;
			stack.map.getCoordinateAdapter().addCoordinatesListener(new CoordinatesListener() {
				@Override
				public void coordinatesChanged(double newX, double newY, double newScale, boolean coordsChanged, boolean scaleChanged) {
					updateEnabled();
				}

				@Override
				public void displaySizeChanged(int newWidthPx, int newHeightPx) {
				}
			});
		}

		@Override
		protected void actionPerformed() {
			if (undo)
				stack.goBack();
			else
				stack.goForward();
			updateEnabled();
		}

		protected void updateEnabled() {
			if (undo) {
				setProperty(INTERNAL_ENABLED, Boolean.valueOf(stack.canGoBack()));
			} else {
				setProperty(INTERNAL_ENABLED, Boolean.valueOf(stack.canGoForward()));
			}
		}
	}

	private static final ViewUndoToken createView(DisplayCoordinateAdapter coords) {
		return new ViewUndoToken(coords.worldCenterX, coords.worldCenterY, coords.getScale());
	}

	private final UndoStack<ViewUndoToken> stack = new UndoStack<ViewUndoToken>();
	private UndoRedoAction undoAction;
	private UndoRedoAction redoAction;
	private final long timeToWait = 250;

	protected final IMap map;
	protected boolean ignoreEvents = false;

	public ViewUndoStack(IMap map) {
		this.map = map;
		internalStore();
		map.addRepaintListener(new RepaintListenerAdapter() {
			@Override
			public void onRepaint(boolean hard) {
				if (!hard || ignoreEvents)
					return;
				storeCurrentView();
			}
		});
	}

	public UndoRedoAction getUndoAction() {
		if (undoAction == null) {
			undoAction = createUndoRedoAction(true);
		}
		return undoAction;
	}

	protected UndoRedoAction createUndoRedoAction(boolean undo) {
		UndoRedoAction ret = new UndoRedoAction(this, undo 
			? Tooltips.INSTANCE.toolbar_previousView() 
			: Tooltips.INSTANCE.toolbar_nextView(), 
			undo);
		ret.setIcon(undo? GisTheme.getGisTheme().gisStandardIcons().viewBack() : GisTheme.getGisTheme().gisStandardIcons().viewFwd());
		ret.setStyle("actionButZoom"+(undo?"Previous":"Next"));
    	return ret;
	}

	public UndoRedoAction getRedoAction() {
		if (redoAction == null) {
			redoAction = createUndoRedoAction(false);
		}
		return redoAction;
	}

	public void reset() {
		stack.clear();
		lastLoc = null;
		internalStore();
	}

	private ViewUndoToken lastLoc = null;

	protected void internalStore() {
		ViewUndoToken toStore = createView(map.getCoordinateAdapter());
		if (!toStore.isValid()) {
			return;
		}
		
		if (toStore.coordEquals(lastLoc)) {
			lastLoc = toStore;
			stack.replaceCurrent(toStore);
			return;
		}
		while (lastLoc != null && toStore.timestamp < (lastLoc.timestamp + timeToWait)) {
			lastLoc = stack.goBack();
		}
		stack.store(toStore);
		lastLoc = toStore;
		updateActions();
	}

	public void storeCurrentView() {
		internalStore();
	}

	public void goBack() {
		apply(stack.goBack());
	}

	protected void apply(ViewUndoToken loc) {
		lastLoc = loc;
		ignoreEvents = true;
		map.getCoordinateAdapter().setWorldCenterAndScale(loc);
		map.repaint(100);
		ignoreEvents = false;
	}

	private void updateActions() {
		if (undoAction != null)
			undoAction.updateEnabled();
		if (redoAction != null)
			redoAction.updateEnabled();
	}

	public void goForward() {
		apply(stack.goForward());
	}

	public boolean canGoBack() {
		return stack.canGoBack();
	}

	public boolean canGoForward() {
		return stack.canGoForward();
	}
}
