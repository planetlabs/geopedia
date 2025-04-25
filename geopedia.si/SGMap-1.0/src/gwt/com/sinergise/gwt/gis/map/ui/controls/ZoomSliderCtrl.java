package com.sinergise.gwt.gis.map.ui.controls;


import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.display.event.CoordinatesListener;
import com.sinergise.common.gis.map.ui.IMap;
import com.sinergise.common.util.geom.EnvelopeI;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.ui.CompositeExt;
import com.sinergise.gwt.ui.core.MouseHandler;
import com.sinergise.gwt.util.html.CSS;
import com.sinergise.gwt.util.html.ExtDOM;


public class ZoomSliderCtrl extends CompositeExt {
	private static Widget createHidden(String style, AbsolutePanel pOuter) {
		SimplePanel hwgt = new SimplePanel();
		hwgt.setStyleName(style);
		CSS.setVisible(hwgt, false);
		pOuter.add(hwgt);
		return hwgt;
	}
	
	private enum MouseActionState{
		IDLE, SLIDING,
		
		PLUS_BUTTON {
			@Override
			public boolean isButton() {
				return true;
			}
		}, 
		MINUS_BUTTON {
			@Override
			public boolean isButton() {
				return true;
			}
		};
		
		public boolean isButton() {
			return false;
		}
	}

	private class MyEventHandler implements EventListener {
		private static final String STYLE_DOWN = "down";
		MouseActionState state = MouseActionState.IDLE;
		int x, y;
		
		@Override
		public void onBrowserEvent(Event event) {
			int t = DOM.eventGetType(event);
			
			if (t == Event.ONLOSECAPTURE) {
				t = Event.ONMOUSEUP;
			} else {
				x = ExtDOM.eventGetElementX(event, getElement());
				y = ExtDOM.eventGetElementY(event, getElement());
			}
			
			if (state == MouseActionState.IDLE) {
				if (t == Event.ONMOUSEDOWN) {
					handleIdleDown(event);
				}
			} else if (state == MouseActionState.SLIDING) {
				setIndPos(y - sliderStart);
				DOM.eventPreventDefault(event);
				DOM.eventCancelBubble(event, true);
				if (t == Event.ONMOUSEUP) {
					handleSlidingUp();
				}
				
			} else if (state.isButton()) {
				boolean inside = state == MouseActionState.PLUS_BUTTON ? plusButEnv.contains(x, y) : minusButEnv.contains(x, y);
				Widget theButton = state == MouseActionState.PLUS_BUTTON ? plus : minus;
				theButton.setVisible(inside);
				
				if (t == Event.ONMOUSEUP) {
					handleButtonUp(inside);
				}
				DOM.eventPreventDefault(event);
				DOM.eventCancelBubble(event, true);
			}
		}

		private void handleButtonUp(boolean inside) {
			plus.removeStyleName(STYLE_DOWN);
			minus.removeStyleName(STYLE_DOWN);
			if (inside) {
				double curr = dca.worldLenPerDisp;
				double znew = curr * (state == MouseActionState.PLUS_BUTTON ? 1 / scaleFactor : scaleFactor);
				
				// these two must happen here and in this order !
				state = MouseActionState.IDLE;
				DOM.releaseCapture(getElement());
				
				dca.setScale(znew);
				map.repaint(100);
			}
		}

		private void handleSlidingUp() {
			CSS.setVisible(ind_off, true);
			CSS.setVisible(ind_on, false);
			ind_on.removeStyleName(STYLE_DOWN);
			state = MouseActionState.IDLE;
			DOM.releaseCapture(getElement());
			double scaleRatio = MathUtil.clamp(0, (double)(y - sliderStart) / (double)sliderLen, 1);
			double scale = MathUtil.fromLogRatio(scaleRatio, dca.bounds.minScale(), dca.bounds.maxScale());
			dca.setScale(scale);
		}

		private void handleIdleDown(Event event) {
			if (plusButEnv.contains(x, y)) {
				state = MouseActionState.PLUS_BUTTON;
				processHandledEvent(event);
				plus.addStyleName(STYLE_DOWN);
			} else if (minusButEnv.contains(x, y)) {
				state = MouseActionState.MINUS_BUTTON;
				processHandledEvent(event);
				minus.addStyleName(STYLE_DOWN);
			} else if (scaleEnv.contains(x, y)) {
				state = MouseActionState.SLIDING;
				processHandledEvent(event);
				ind_on.addStyleName(STYLE_DOWN);
				CSS.setVisible(ind_off, false);
				setIndPos(y - plusButEnv.getHeight());
				CSS.setVisible(ind_on, true);
			}
		}

		private void processHandledEvent(Event event) {
			DOM.eventPreventDefault(event);
			DOM.eventCancelBubble(event, true);
			DOM.setCapture(getElement());
		}
		
		public void zoomChangedFromOutside(double newScale) {
			updateScale(newScale);
		}
	}

	private final AbsolutePanel outer;
	private final Widget main;
	private final Widget ind_on;
	private final Widget ind_off;
	private final Widget plus;
	private final Widget minus;
	private final MyEventHandler eventHandler = new MyEventHandler();
	private final DisplayCoordinateAdapter dca;
	private final IMap map;
	private final double scaleFactor = 2;
	
	// components sizes
	private int indH;
	
	// store calculated constants for reusing
	private int sliderStart;
	private int sliderLen;
	
	private EnvelopeI plusButEnv;
	private EnvelopeI minusButEnv;
	private EnvelopeI scaleEnv;
	
	
	public ZoomSliderCtrl(IMap map) {
		this(map,StyleConsts.ZOOM_CONTROL);
	}

	public ZoomSliderCtrl(IMap map, String styleBase) {
		this.map = map;
		this.dca = map.getCoordinateAdapter();
		
		outer = new AbsolutePanel();
		outer.setStyleName(styleBase);
		
		main = new SimplePanel();
		main.setStyleName(styleBase + "-main");
		outer.add(main, 0, 0);
		
		ind_on = createHidden(styleBase + "-ind-on", outer);
		ind_off = createHidden(styleBase + "-ind-off", outer);
		plus = createHidden(styleBase + "-plus", outer);
		minus = createHidden(styleBase + "-minus", outer);
		CSS.setVisible(plus, true);
		CSS.setVisible(minus, true);
		
		initWidget(outer);
		
		MouseHandler.preventContextMenu(getElement());
		dca.addCoordinatesListener(new CoordinatesListener() {
			@Override
			public void coordinatesChanged(double newX, double newY, double newScale, boolean coordsChanged, boolean scaleChanged) {
				if (scaleChanged) {
					eventHandler.zoomChangedFromOutside(newScale);
				}
			}
			@Override
			public void displaySizeChanged(int newWidthPx, int newHeightPx) {
				updateSize();
			}
		});
		
		addStyleName("mapNoPrint");
	}

	void updateScale(double scale) {
		// ratio in the range 0..1
		if (dca.bounds.minScale() < dca.bounds.maxScale()) {
			double ratio = dca.bounds.scaleRatio(scale);
			if (ratio >= 0 && ratio <= 1) {
				setIndPos((int)Math.round(sliderLen * ratio));
			}
		}
	}
	
	void setIndPos(int sliderPos) {
		if (sliderPos > sliderLen) sliderPos = sliderLen;
		if (sliderPos < 0) sliderPos = 0;
		int indTop = sliderStart + sliderPos - indH/2;
		outer.setWidgetPosition(ind_on, scaleEnv.minX(), indTop);
		outer.setWidgetPosition(ind_off, scaleEnv.minX(), indTop);
	}
	
	@Override
	protected void onAttach() {
		super.onAttach();
		updateSize();
		DOM.setEventListener(getElement(), eventHandler);
		sinkEvents(Event.MOUSEEVENTS | Event.ONLOSECAPTURE);
	}

	private void updateSize() {
		// get image sizes
		int outerH = outer.getOffsetHeight();
		int indW = ind_off.getOffsetWidth();
		plusButEnv = getWidgetEnvelope(plus);
		minusButEnv = getWidgetEnvelope(minus);
		indH = ind_off.getOffsetHeight();
		
		// calculate size related constants
		sliderStart = plusButEnv.getHeight() + indH / 2;
		sliderLen = outerH - plusButEnv.getHeight() - minusButEnv.getHeight() - indH;
		
		int scaleL = outer.getWidgetLeft(ind_off);
		scaleEnv = EnvelopeI.create(scaleL, plusButEnv.minY()+1, scaleL + indW - 1, minusButEnv.maxY()-1);
		if (scaleEnv.isEmpty()) {
			scaleEnv = EnvelopeI.create(scaleL, plusButEnv.minY()+1, scaleL, minusButEnv.maxY()+1);
		}
		
		updateScale(dca.getScale());
		CSS.setVisible(ind_off, true);
	}
	
	private EnvelopeI getWidgetEnvelope(Widget w) {
		return EnvelopeI.withSize(outer.getWidgetLeft(w), outer.getWidgetTop(w), w.getOffsetWidth(), w.getOffsetHeight());
	}
}
