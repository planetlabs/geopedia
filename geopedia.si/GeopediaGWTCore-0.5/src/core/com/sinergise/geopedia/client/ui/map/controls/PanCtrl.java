package com.sinergise.geopedia.client.ui.map.controls;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.geopedia.core.common.util.MathUtils;
import com.sinergise.gwt.ui.CompositeExt;
import com.sinergise.gwt.ui.core.MouseHandler;
import com.sinergise.gwt.util.html.ExtDOM;


public class PanCtrl extends CompositeExt
{
	
	FlowPanel outer;
	Widget main;
	Widget left, right, up, down, dot;
	MyEventHandler eventHandler = new MyEventHandler();
	DisplayCoordinateAdapter dca;
	
	private Widget createHidden(String style, FlowPanel outer)
	{
		SimplePanel wgt = new SimplePanel();
		wgt.setStyleName(style);
		outer.add(wgt);
		return wgt;
	}
	
	public PanCtrl(DisplayCoordinateAdapter dca)
    {
		this.dca = dca;
		
		outer = new FlowPanel();
		initWidget(outer);
//		DOM.setAttribute(outer.getElement(), "id", "geopedia-panCtrl");
		setStyleName("geopedia-panCtrl");
		/*
		main = new SimplePanel();
		main.setStyleName("geopedia-panCtrl-main");
		outer.add(main, 0, 0);
*/
		left = createHidden("geopedia-panCtrl-left", outer);
		right = createHidden("geopedia-panCtrl-right", outer);
		up = createHidden("geopedia-panCtrl-up", outer);
		down = createHidden("geopedia-panCtrl-down", outer);
		dot = createHidden("geopedia-panCtrl-center", outer);

        MouseHandler.preventContextMenu(getElement());
        addStyleName("mapNoPrint");
    }
	
	@Override
	protected void onAttach()
	{
		super.onAttach();
		
		compWidth = outer.getOffsetWidth();
		compHeight = outer.getOffsetHeight();
		eventHandler.setArrowMargins(compWidth/2, compHeight/2, compWidth/2, compHeight/2);
		//store offset positions (must be 2 pixels in component to register pan events)
		ltActiveOffset = 2;
		rbActiveOffset = compWidth-2;
		
		DOM.setEventListener(getElement(), eventHandler);
		sinkEvents(Event.MOUSEEVENTS | Event.ONLOSECAPTURE);
	}
	
	private int compWidth = 0;
	private int compHeight = 0;
	private int ltActiveOffset = 0;
	private int rbActiveOffset = 0;
	
	class MyEventHandler implements EventListener
	{
		//When mouse button is hold, the timer does the panning.
		//Timer is canceled on MouseUp event.
		class MyTimer extends Timer
		{
			int iter = 0;
			
			int dx = 0, dy = 0;
			
			boolean scheduled = false;
			
			public void go(int dx, int dy)
			{
				this.dx = dx;
				this.dy = dy;
				iter = 0;
				
				if (!scheduled) {
					scheduleRepeating(30);
					scheduled = true;
				}
			}
			
			@Override
			public void cancel()
			{
			    if (System.currentTimeMillis() > startT+250) {
			    	//System.out.println("Drag cancelled");
//                    map.repaint(500);
			    	super.cancel();
			    } else {
			    	//System.out.println("Click");
			    	if (Math.abs(dx)>3) dx = (dx*40)/Math.abs(dx);
			    	else dx=0;
			    	if (Math.abs(dy)>3) dy = (dy*40)/Math.abs(dy);
			    	else dy=0;
			    	if (dx==0 && dy==0) super.cancel();
			    }
			    scheduled = false;
			}
			
			@Override
			public void run()
			{
//				System.out.println(iter++);
			    dca.pixPan(dx, dy);
			    int timeToTraverseHeight=dca.getDisplayHeight()/40*30;
			    if (!scheduled && System.currentTimeMillis()>startT+timeToTraverseHeight) {
			    	//System.out.println("Click cancelled");
			    	super.cancel();
//		            map.repaint(500);
			    }
			}
		}
		
		MyTimer timer = new MyTimer();
		
		boolean panning = false;

		int lastx, lasty;
		long startT;
		
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
			
			if (!panning) {
				if (t != Event.ONMOUSEOUT && x > ltActiveOffset && y > ltActiveOffset 
						&& x < rbActiveOffset && y < rbActiveOffset) 
				{
					updateVisibilityStyle(dot, true);
					if (t == Event.ONMOUSEDOWN) {
						panning=true;
						startT=System.currentTimeMillis();
						DOM.setCapture(getElement());
						DOM.eventPreventDefault(event);
						DOM.eventCancelBubble(event, true);
						panIt(x, y);
					}
				} else {
					updateVisibilityStyle(dot, false);
				}
			} else if (panning) {
				if (t == Event.ONMOUSEUP) {
					timer.cancel();
					startT = -250;
					panning = false;
					updateVisibilityStyle(dot, false);
					updateVisibilityStyle(up,false);
					updateVisibilityStyle(down,false);
					updateVisibilityStyle(left,false);
					updateVisibilityStyle(right,false);
					
					DOM.releaseCapture(getElement());
					DOM.eventPreventDefault(event);
					DOM.eventCancelBubble(event, true);
				} else {
					panIt(x, y);
				}
			}
        }
		
		private int centerX = 31;
		private int centerY = 31;
		private int minmaxX = 30;
		private int minmaxY = 30;
		private int deadZone = 20;
		
		
		public void setArrowMargins(int centerX, int centerY, int minmaxX, int minmaxY) {
			this.centerX = centerX;
			this.centerY = centerY;
			this.minmaxX = minmaxX;
			this.minmaxY = minmaxY;
			double dead = centerX*0.18;
			this.deadZone =(int) (dead*dead)*2;
		}
		
		void panIt(int x, int y)
		{
			int dx = MathUtils.clamp(-minmaxX, x - centerX, minmaxX);
			int dy = MathUtils.clamp(-minmaxY, centerY - y, minmaxY);
			
			if (dx*dx + dy*dy < deadZone) {
				timer.go(0,0);
				updateVisibilityStyle(up,false);
				updateVisibilityStyle(down,false);
				updateVisibilityStyle(left,false);
				updateVisibilityStyle(right,false);
			} else {
				timer.go(dx, dy);

				double dang = MathUtils.atan2(dy, dx);
				int ang = (int)Math.round(dang * (180 / Math.PI));
				if (ang < 0)
					ang += 360;
				
				updateVisibilityStyle(up,(ang > 22 && ang < 158));
				updateVisibilityStyle(left,(ang > 112 && ang < 248));
				updateVisibilityStyle(down,(ang > 202 && ang < 338));
				updateVisibilityStyle(right,(ang > 292 || ang < 68));
			}
		}
		
		private void updateVisibilityStyle(Widget w, boolean visible) {
			if (visible) {
				w.addStyleDependentName("clicked");
			}else {
				w.removeStyleDependentName("clicked");
			}
		}
	}
}
