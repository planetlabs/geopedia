package com.sinergise.geopedia.client.ui.map.controls;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.display.event.CoordinatesListener;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.core.common.util.MathUtils;
import com.sinergise.gwt.ui.core.MouseHandler;
import com.sinergise.gwt.util.html.CSS;
import com.sinergise.gwt.util.html.ExtDOM;


// IMPORTANT: Do not use UIObject.setVisible() to hide and show images.
// CSS.visibility must be used for the elements to provide their offset sizes on attaching the component.

public class ZoomSliderCtrl extends Composite
{
	AbsolutePanel outer;
	Widget main;
	Widget ball, plus, minus;
	MyEventHandler eventHandler = new MyEventHandler();
	DisplayCoordinateAdapter dca;
	final int minLevel;
	final int maxLevel;
	double scaleFactor=2;
	
	private Widget createHidden(String style, AbsolutePanel outer)
	{
		SimplePanel wgt = new SimplePanel();
		wgt.setStyleName(style);
		//CSS.visibility(wgt, false);
		outer.add(wgt);
		return wgt;
	}
	
	private static int getMinLevelId() {
		return ClientGlobals.getMainCRS().getMinLevelId();
	}

	private static int getMaxLevelId() {
		return ClientGlobals.getMainCRS().getMaxLevelId();
	}

	public ZoomSliderCtrl(DisplayCoordinateAdapter dca) {
		this(dca,getMinLevelId(), getMaxLevelId());
	}
	
	public ZoomSliderCtrl(DisplayCoordinateAdapter dca, int minLevel, int maxLevel)
    {
		this.dca = dca;
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
		
		outer = new AbsolutePanel();
		outer.setStyleName("geopedia-zoomCtrl");
		
		main = new SimplePanel();
		main.setStyleName("geopedia-zoomCtrl-main");
		outer.add(main, 0, 0);
		
		
		ball = createHidden("geopedia-zoomCtrl-ball", outer);
		plus = createHidden("geopedia-zoomCtrl-in", outer);
		minus = createHidden("geopedia-zoomCtrl-out", outer);		
		
		initWidget(outer);
		
        MouseHandler.preventContextMenu(getElement());
        dca.addCoordinatesListener(new CoordinatesListener() {
			
			@Override
			public void displaySizeChanged(int newWidthPx, int newHeightPx) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void coordinatesChanged(double newX, double newY, double newScale,
					boolean coordsChanged, boolean scaleChanged) {
				if (scaleChanged) {
					//eventHandler.zoomChangedFromOutside(zoomLevel);
				}
			}
		});
        
        addStyleName("mapNoPrint");
    }
	
	void setIndPos(int zoomLevel)
	{
		zoomLevel = MathUtils.clamp(minLevel, zoomLevel, maxLevel);
		
		outer.setWidgetPosition(ball, outerIndDiffWd2, getIndPixTop(zoomLevel));
	}
	
	private int getIndPixTop(int zoomLevel) {
		return plusButH + (int)Math.round((maxLevel-zoomLevel)*pixelsPerLevel);
	}
	
	private int getIndZoomLevel(int pixTop) {
		return (int)Math.round((plusButH - pixTop + maxLevel*pixelsPerLevel)/pixelsPerLevel);
	}
	
	@Override
	protected void onAttach()
	{
		super.onAttach();
		
		//get image sizes
		outerW = outer.getOffsetWidth();
		outerH = outer.getOffsetHeight();
		indW = ball.getOffsetWidth();
		indH = ball.getOffsetHeight();
		plusButW = plus.getOffsetWidth();
		plusButH = plus.getOffsetHeight();
		minusButW = minus.getOffsetWidth();
		minusButH = minus.getOffsetHeight();
		
		
		//calculate size related constants
		outerIndDiffWd2 = (outerW-indW)/2;
		sliderLen = outerH-plusButH-minusButH;
		pixelsPerLevel = (double)(sliderLen-indH)/(double)(maxLevel-minLevel);
		
		plusButL = outer.getWidgetLeft(plus);
		plusButR = plusButL + plusButW;
		plusButT = outer.getWidgetTop(plus);
		plusButB = plusButT + plusButH;
		
		minusButL = outer.getWidgetLeft(minus);
		minusButR = minusButL + minusButW;
		minusButT = outer.getWidgetTop(minus);
		minusButB = minusButT + minusButH;
		
		scaleL = outer.getWidgetLeft(ball);
		scaleR = scaleL+indW;
		scaleT = plusButB;
		scaleB = getIndPixTop(minLevel)+indH;
		
		//TODO: fixDCA
		//setIndPos(dca.getZoomLevel());
		CSS.setVisible(ball, true);
		
		DOM.setEventListener(getElement(), eventHandler);
		sinkEvents(Event.MOUSEEVENTS | Event.ONLOSECAPTURE);
	}
	
	//components sizes
	private int outerW;
	private int outerH;
	private int indW;
	private int indH;
	private int plusButW;
	private int plusButH;
	private int minusButW;
	private int minusButH;
	
	//store calculated constants for reusing
	private int sliderLen;
	private double pixelsPerLevel;
	private int outerIndDiffWd2;
	
	private int plusButL, plusButR, plusButT, plusButB;
	private int minusButL, minusButR, minusButT, minusButB;
	private int scaleL, scaleR, scaleT, scaleB;
	
	
	class MyEventHandler implements EventListener
	{
		int state = 0;
		// 0 = nothing
		// 1 = setting zoom
		// 2 = panning  NOT USED ANYMORE! See PanCtrl for panning.
		// 3 = upper button
		// 4 = lower button

		int lastx, lasty;
		
		public void onBrowserEvent(Event event)
        {
			int t = DOM.eventGetType(event);
			
			int x, y;
			if (t == Event.ONLOSECAPTURE) {
				x = lastx;
				y = lasty;
				t = Event.ONMOUSEUP;
			} else {
				lastx = x = ExtDOM.eventGetElementX(event, getElement());
				lasty = y = ExtDOM.eventGetElementY(event, getElement());
			}

			if (state == 0) {
				if (t == Event.ONMOUSEDOWN) {
					if (x >= plusButL && y >= plusButT && x < plusButR && y < plusButB) {
						state = 3;
						DOM.eventPreventDefault(event);
						DOM.eventCancelBubble(event, true);
						DOM.setCapture(getElement());
						CSS.setVisible(plus, true);
						plus.addStyleName("down");
						
					} else
					if (x >= minusButL && y >= minusButT && x < minusButR && y < minusButB) {
						state = 4;
						DOM.eventPreventDefault(event);
						DOM.eventCancelBubble(event, true);
						DOM.setCapture(getElement());
						CSS.setVisible(minus, true);
						minus.addStyleName("down");
					} else
					if (x >= scaleL && y >= scaleT && x <= scaleR && y < scaleB) {
						state = 1;
						DOM.eventPreventDefault(event);
						DOM.eventCancelBubble(event, true);
						DOM.setCapture(getElement());
						//CSS.visibility(ball, false);
						setIndPos(x, y-(indH/2));
						ball.addStyleName("down");
					}
				}
			} else
			if (state == 1) {
				// down up move over out
				y = y-indH/2;
				setIndPos(x, y);
				if (t == Event.ONMOUSEUP) {
					//CSS.visibility(ball, true);
					state = 0;
					DOM.releaseCapture(getElement());
					int zoomLevel = MathUtils.clamp(minLevel, getIndZoomLevel(y), maxLevel) ;
					//TODO: fixDCA
					//dca.setZoomLevel(zoomLevel);
					plus.removeStyleName("down");
					minus.removeStyleName("down");
					ball.removeStyleName("down");
				}
				DOM.eventPreventDefault(event);
				DOM.eventCancelBubble(event, true);
			} else
			if (state == 3 || state == 4) {
				boolean inside = 
					state == 3 ?
						x >= plusButL && y >= plusButT && x < plusButR && y < plusButB
					:
						x >= minusButL && y >= minusButT && x < minusButR && y < minusButB;

				//Widget theButton = state == 3 ? plus : minus;

				if (t == Event.ONMOUSEUP) {
					//CSS.visibility(theButton, false);
					if (inside) {
						//TODO: fixDCA
						//int curr = dca.getZoomLevel();
						int curr=0;
						int znew = curr + (state == 3 ? 1 : -1);
						
						// these two must happen here and in this order !
						state = 0;
						DOM.releaseCapture(getElement());
						plus.removeStyleName("down");
						minus.removeStyleName("down");
						ball.removeStyleName("down");

						if (znew >= getMinLevelId() && znew <= getMaxLevelId()) {
							//TODO: fixDCA
							//dca.setZoomLevel(znew);
						}
					}
				} else {
					if (!inside) {
						//CSS.visibility(theButton, false);
					} else {
						//CSS.visibility(theButton, true);
					}
				}
				DOM.eventPreventDefault(event);
				DOM.eventCancelBubble(event, true);
			} 
        }
		
		void setIndPos(int x, int y)
		{
			if (y > scaleB-indH) y = scaleB-indH;
			if (y < scaleT) y = scaleT;			
			outer.setWidgetPosition(ball, scaleL, y);
		}
		
		public void zoomChangedFromOutside(int zoomLevel)
        {
			ZoomSliderCtrl.this.setIndPos(zoomLevel);
        }
	}
}
