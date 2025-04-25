package com.sinergise.gwt.gis.map.ui.controls;


import static com.sinergise.common.util.math.MathUtil.between;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.LoseCaptureEvent;
import com.google.gwt.event.dom.client.LoseCaptureHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.display.event.CoordinatesListener;
import com.sinergise.common.gis.map.ui.IMap;
import com.sinergise.common.util.math.AngleUtil;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.ui.CompositeExt;
import com.sinergise.gwt.ui.core.MouseHandler;
import com.sinergise.gwt.util.html.ExtDOM;


public class PanCtrl extends CompositeExt
{
	
	private static Widget createHidden(String style, AbsolutePanel pOuter)
	{
		SimplePanel w = new SimplePanel();
		w.setStyleName(style);
		w.setVisible(false);
		pOuter.add(w);
		return w;
	}
	private class MyEventHandler implements MouseDownHandler, MouseUpHandler, MouseMoveHandler, MouseOverHandler, MouseOutHandler, LoseCaptureHandler {
			private PanTimer timer;
			private ClickTimer curClick = null;
			private boolean panning = false;
			private int dx;
			private int dy;
			private long startT;
			private boolean inActiveArea = false;

			public MyEventHandler() {
			}
			
			@Override
			public void onMouseOver(MouseOverEvent event) {
				updatePosAndPan(event);
			}

			@Override
			public void onMouseMove(MouseMoveEvent event) {
				updatePosAndPan(event);
			}

			@Override
			public void onMouseOut(MouseOutEvent event) {
				updatePosAndPan(event);
			}

			@Override
			public void onMouseDown(MouseDownEvent event) {
				updateMousePos(event);
				if (!panning && inActiveArea) {
					handleDown(event);
				}
			}

			private boolean isLeftButton(MouseEvent<?> event) {
				return event.getNativeButton() == NativeEvent.BUTTON_LEFT;
			}

			private void updatePosAndPan(MouseEvent<?> event) {
				updateMousePos(event);
				if (panning) {
					panIt();
				}
			}

			private void handleDown(MouseDownEvent event) {
				if (isLeftButton(event)) {
					panning = true;
					startT = System.currentTimeMillis();
					DOM.setCapture(getElement());
					
					cancelTimers();
					timer = new PanTimer(map);
					timer.start();
					panIt();
				}
				suppressConsumedEvent(event);
			}

			private void cancelTimers() {
				if (curClick != null) {
					curClick.cancel();
					curClick = null;
				}
				if (timer != null) {
					timer.cancel();
					timer = null;
				}
			}

			@Override
			public void onMouseUp(MouseUpEvent event) {
				updatePosAndPan(event);
				if (isLeftButton(event)) {
					handleUp(event);
				}
			}

			@Override
			public void onLoseCapture(LoseCaptureEvent event) {
				handleUp(event);
			}

			private void updateMousePos(MouseEvent<?> event) {
				int curX = ExtDOM.eventGetElementX(event.getNativeEvent(), getElement());
				int curY = ExtDOM.eventGetElementY(event.getNativeEvent(), getElement());
				inActiveArea = isInActiveArea(curX, curY);
				dot.setVisible(inActiveArea);

				double dPxX = curX - compWidth2;
				double dPxY = compHeight2 - curY;
				double dPxLen = MathUtil.hypot(dPxX, dPxY);
				if (dPxLen > compWidth2) {
					double lenFact = compWidth2 / dPxLen;
					dPxX = dPxX * lenFact;
					dPxY = dPxY * lenFact;
				}
				dx = (int)Math.round(dPxX);
				dy = (int)Math.round(dPxY);
			}

			private boolean isInActiveArea(int x, int y) {
				return between(minActivePxIndex, x, maxActivePxIndex) && between(minActivePxIndex, y, maxActivePxIndex);
			}

			private void suppressConsumedEvent(DomEvent<?> event) {
				event.preventDefault();
				event.stopPropagation();
			}

			private void handleUp(DomEvent<?> event) {
				dot.setVisible(false);
				up.setVisible(false);
				down.setVisible(false);
				left.setVisible(false);
				right.setVisible(false);
				if (panning) {
					panning = false;
					DOM.releaseCapture(getElement());
					suppressConsumedEvent(event);
					if (interpretAsClick()) {
						cancelTimers();
						startClick();
					} else {
						timer.cancelAndRepaint();
					}
				} else {
					cancelTimers();
				}
			}

			private void startClick() {
				int absX = Math.abs(dx);
				int absY = Math.abs(dy);
				
				int totalDx = 0;
				int totalDy = 0;
				if (absX > absY /2) {
					totalDx = dca.getDisplayWidth() * dx/absX * 3/4;
				} 
				if (absY > absX /2) {
					totalDy = dca.getDisplayHeight() * dy/absY * 3/4;
				}				
				new ClickTimer(map, totalDx, totalDy).start();
			}

			
			private boolean interpretAsClick() {
				return (Math.abs(dx) > 3 || Math.abs(dy) > 3) && (System.currentTimeMillis() < startT + 250);
			}

			private void panIt()
			{
				timer.setSpeed(dx, dy);
				updateButtonsHighlight();
			}

			private void updateButtonsHighlight() {
				if (dx*dx + dy*dy < 16) {
					up.setVisible(false);
					down.setVisible(false);
					left.setVisible(false);
					right.setVisible(false);
				} else {
					double dang = AngleUtil.pseudoATan2(dy, dx);
					dang = AngleUtil.positiveNormalAngle(dang);
					
					int ang = (int)Math.toDegrees(dang);
					up.setVisible(ang > 22 && ang < 158);
					left.setVisible(ang > 112 && ang < 248);
					down.setVisible(ang > 202 && ang < 338);
					right.setVisible(ang > 292 || ang < 68);
				}
			}
		}
	//When mouse button is hold, the timer does the panning.
	//Timer is canceled on MouseUp event.
	private static class PanTimer implements Scheduler.RepeatingCommand
	{
		private int dx = 0;
		private int dy = 0;
		private boolean scheduled = false;
		private IMap map;
		private DisplayCoordinateAdapter dca;
		
		public PanTimer(IMap map) {
			this.map = map;
			this.dca = map.getCoordinateAdapter();
		}
		
		public void setSpeed(int dX, int dY) {
			this.dx = dX;
			this.dy = dY;
		}
		
		public void cancelAndRepaint() {
			cancel();
			map.repaint(500);
		}
		
		public void cancel() {
		    scheduled = false;
		}
		
		@Override
		public boolean execute() {
			if (scheduled) {
				dca.pixPan(dx, dy);
				return true;
			}
			return false;
		}
	
		public void start() {
			if (!scheduled) {
				scheduled = true;
				Scheduler.get().scheduleFixedPeriod(this, 30);
			}
		}
	}
	private static class ClickTimer implements Scheduler.RepeatingCommand {
		double tgtX;
		double tgtY;
		int stepsRemaining;
		boolean scheduled = false;
		
		private IMap map;
		private DisplayCoordinateAdapter dca;
		
		public ClickTimer(IMap map, int tgtDx, int tgtDy) {
			this.map = map;
			this.dca = map.getCoordinateAdapter();
			this.tgtX = dca.worldCenterX + tgtDx*dca.worldLengthPerPix;
			this.tgtY = dca.worldCenterY + tgtDy*dca.worldLengthPerPix;
			start();
		}
		
		@Override
		public boolean execute() {
			if (!scheduled) {
				return false;
			}
			stepsRemaining--;
			if (stepsRemaining == 0) {
				finish();
				return false;
			}
			double newX = (tgtX + stepsRemaining*dca.worldCenterX) / (stepsRemaining + 1);
			double newY = (tgtY + stepsRemaining*dca.worldCenterY) / (stepsRemaining + 1);
			dca.setWorldCenter(newX, newY);
			return true;
		}

		private void finish() {
			dca.setWorldCenter(tgtX, tgtY);
			map.repaint(100);
			scheduled = false;
		}
	
		public void start() {
			stepsRemaining = 10;
			if (!scheduled) {
				scheduled = true;
				Scheduler.get().scheduleFixedPeriod(this, 30);
			}
		}
		
		public void cancel() {
			scheduled = false;
		}
	}
	private final AbsolutePanel outer;
	private final Widget dot;
	
	private final Widget left;
	private final Widget right;
	private final Widget up;
	private final Widget down;
	
	private final DisplayCoordinateAdapter dca;
	private final IMap map;
	
	private int compWidth2 = 0;
	private int compHeight2 = 0;
	private int minActivePxIndex = 0;
	private int maxActivePxIndex = 0;

	private final MyEventHandler eventHandler = new MyEventHandler();
	
	public PanCtrl(IMap map) {
		this(map, StyleConsts.PAN_CONTROL);
	}
	
	public PanCtrl(IMap map, String styleBase)
    {
		this.dca = map.getCoordinateAdapter();
		this.map = map;
		
		outer = new AbsolutePanel();
		initWidget(outer);
		setStyleName(styleBase);
		
		SimplePanel main = new SimplePanel();
		main.setStyleName(styleBase+"-main");
		outer.add(main, 0, 0);

		left = createHidden(styleBase+"-left-on", outer);
		right = createHidden(styleBase+"-right-on", outer);
		up = createHidden(styleBase+"-up-on", outer);
		down = createHidden(styleBase+"-down-on", outer);
		dot = createHidden(styleBase+"-dot-on", outer);

        MouseHandler.preventContextMenu(getElement());
        addStyleName("mapNoPrint");
        
        map.getCoordinateAdapter().addCoordinatesListener(new CoordinatesListener() {
			@Override
			public void displaySizeChanged(int newWidthPx, int newHeightPx) {
				updateSize();
			}
			
			@Override
			public void coordinatesChanged(double newX, double newY, double newScale, boolean coordsChanged, boolean scaleChanged) {
			}
		});
    }
	
	@Override
	protected void onAttach()
	{
		super.onAttach();

		addDomHandler(eventHandler, MouseDownEvent.getType());
		addDomHandler(eventHandler, MouseUpEvent.getType());
		addDomHandler(eventHandler, MouseMoveEvent.getType());
		addDomHandler(eventHandler, MouseOverEvent.getType());
		addDomHandler(eventHandler, MouseOutEvent.getType());
		addDomHandler(eventHandler, LoseCaptureEvent.getType());
		
		updateSize();
	}
	
	private void updateSize() {
		int compWidth = outer.getOffsetWidth();
		compWidth2 = compWidth/2;
		compHeight2 = outer.getOffsetHeight()/2;
		//store offset positions (must be 2 pixels in component to register pan events)
		minActivePxIndex = 2;
		maxActivePxIndex = compWidth - 1 - minActivePxIndex;
	}
}
