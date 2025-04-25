package com.sinergise.gwt.ui.tooltip;

import com.google.gwt.event.dom.client.HasMouseMoveHandlers;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.Widget;



/**
 * GWT Tooltip Popup.
 * 
 * Displays tooltip popup for given widget.
 * 
 * Use static methods:
 * <UL>
 * <LI>{@link #addHtmlTooltipPopup} to add tooltip with HTML content</LI>
 * <LI>{@link #addTextTooltipPopup} to add tooltip with plain text content</LI> 
 * <LI>{@link #addWidgetTooltipPopup} to add tooltip with custom widget as content</LI>
 * </UL>
 * 
 * @author Jani Kleindienst
 *
 */
public class SGRichTooltipPopup {
	
	/**
	 * The default delay [milliseconds], after which tooltip is shown
	 */
	private static final int DEFAULT_SHOW_DELAY = 777;

	/**
	 * 
	 */
	private static final int DEFAULT_HIDE_DELAY = 300;

	
	/**
	 * The default css class name for the tool tip
	 */
	private static final String DEFAULT_TOOLTIP_STYLE = "sgwebui-richtooltip";
	
	
	/**
	 * Internal class to hold tooltip data
	 * 
	 * @author jani
	 *
	 */
	private static class TooltipData {
		public Widget widget;
		public TooltipPosition leftPosition;
		public TooltipPosition topPosition;
		public int showDelay;
		public int hideDelay;
		
		public TooltipData(Widget widget, TooltipPosition leftPosition, TooltipPosition topPosition, int showDelay, int hideDelay) {
			this.widget = widget; 
			this.leftPosition = leftPosition;
			this.topPosition = topPosition;
			this.showDelay = showDelay;
			this.hideDelay = hideDelay;
		}
	}
	
	/**
	 * Tooltip position type 
	 * (for single axis)
	 * 
	 * @author jani
	 *
	 */
	public enum PositionType {
		/**
		 * Tooltip is shown relative to source widget
		 */
		RELATIVE_TO_WIDGET,
		/**
		 * Tooltip is shown relative to mouse pointer 
		 */
		RELATIVE_TO_MOUSE_STATIC,
		/**
		 * Tooltip is following mouse pointer at the relative distance
		 */
		RELATIVE_TO_MOUSE_MOVING,
		/**
		 * Tooltip is shown at absolute screen position
		 */
		ABSOLUTE
	}
	
	
	public static class TooltipPosition {
		public PositionType positionType;
		public int position;
		
		public TooltipPosition(PositionType positionType, int position) {
			super();
			this.positionType = positionType;
			this.position = position;
		}
	}
	
	
	/**
	 * PopupPanel onstance
	 */
	private PopupPanel popupPanel;
	
	/**
	 * set to true when mouse has entered tooltip area
	 */
	private boolean mouseOnTooltip = false;
	

	
	
	/**
	 * 	TooltipPopup static instance  
	 */
	private final static SGRichTooltipPopup INSTANCE = new SGRichTooltipPopup();
	
	
	
	/**
	 * Creates a new tooltip with HTML content for specified widget with the default show delay 
	 * 
	 * @param sender
	 *            The widget to create the tooltip for
	 * @param html
	 * 			  The HTML to show in tooltip	
	 * @param leftPosition
	 *            The left {@link TooltipPosition} (type and offset)
	 * @param topPosition
	 *            The top {@link TooltipPosition} (type and offset)
	 *           
	 */
	public static final void addHtmlTooltipPopup(Widget sender, SafeHtml html, TooltipPosition leftPosition, TooltipPosition topPosition) {
		addHtmlTooltipPopup(sender, html, leftPosition, topPosition, DEFAULT_SHOW_DELAY);
	}
	

	/**
	 * Creates a new tooltip with HTML content for specified widget 
	 * 
	 * @param sender
	 *            The widget to create the tooltip for
	 * @param html
	 * 			  The HTML to show in tooltip	
	 * @param leftPosition
	 *            The left {@link TooltipPosition} (type and offset) 
	 * @param topPosition
	 *            The top {@link TooltipPosition} (type and offset)
	 * @param showDelay
	 * 			  The delay [milliseconds] to show the tooltip after mouse pointer enters sender widget 	
	 *           
	 */
	public static final void addHtmlTooltipPopup(Widget sender, SafeHtml html, TooltipPosition leftPosition, TooltipPosition topPosition, int showDelay) {
		HTML htmlWidget = new HTML(html);
		addWidgetTooltipPopup(sender, htmlWidget, leftPosition, topPosition, showDelay);
	}
	
	
	/**
	 * Creates a new tooltip with plain text content for specified widget with the default show delay 
	 * 
	 * @param sender
	 *            The widget to create the tooltip for
	 * @param html
	 * 			  The HTML to show in tooltip	
	 * @param leftPosition
	 *            The left {@link TooltipPosition} (type and offset)
	 * @param topPosition
	 *            The top {@link TooltipPosition} (type and offset)
	 *           
	 */
	public static final void addTextTooltipPopup(Widget sender, String text, TooltipPosition leftPosition, TooltipPosition topPosition) {
		SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
		safeHtmlBuilder.appendEscaped(text);
		addHtmlTooltipPopup(sender, safeHtmlBuilder.toSafeHtml(), leftPosition, topPosition, DEFAULT_SHOW_DELAY);
	}
		
		
	
	/**
	 * Creates a new tooltip with custom content for specified widget 
	 * 
	 * @param sender
	 *            The widget to create the tooltip for
	 * @param html
	 * 			  The HTML to show in tooltip	
	 * @param leftPosition
	 *            The left {@link TooltipPosition} (type and offset)
	 * @param topPosition
	 *            The top {@link TooltipPosition} (type and offset)
	 * @param showDelay
	 * 			  The delay [milliseconds] to show the tooltip after mouse pointer enters sender widget 
	 *           
	 */
	public static final void addWidgetTooltipPopup(Widget sender, Widget tooltipWidget, TooltipPosition leftPosition, TooltipPosition topPosition, int showDelay) {
		TooltipData data = new TooltipData(tooltipWidget, leftPosition, topPosition, showDelay, DEFAULT_HIDE_DELAY);
	
		SourceMouseHandlers sourceMouseHandlers = INSTANCE.new SourceMouseHandlers(data);
		TooltipMouseHandlers tooltipMouseHandlers = INSTANCE.new TooltipMouseHandlers();
		
		// the following did not trigger any events !?!
//		sender.addHandler(sourceMouseMovehandler, MouseOverEvent.getType());
//		sender.addHandler(sourceMouseMovehandler, MouseOutEvent.getType());
//		sender.addHandler(sourceMouseMovehandler, MouseMoveEvent.getType());
//		
//		tooltipWidget.addHandler(tooltipMouseHandlers, MouseOverEvent.getType());
//		tooltipWidget.addHandler(tooltipMouseHandlers, MouseOutEvent.getType());
//		tooltipWidget.addHandler(tooltipMouseHandlers, MouseMoveEvent.getType());
		
		
		if ((sender instanceof HasMouseOverHandlers) && (sender instanceof HasMouseOutHandlers)) {
			((HasMouseOverHandlers) sender).addMouseOverHandler(sourceMouseHandlers);
			((HasMouseOutHandlers) sender).addMouseOutHandler(sourceMouseHandlers);
			((HasMouseMoveHandlers) sender).addMouseMoveHandler(sourceMouseHandlers);
		}
		
		if ((tooltipWidget instanceof HasMouseOverHandlers) && (tooltipWidget instanceof HasMouseOutHandlers)) {
			((HasMouseOverHandlers) tooltipWidget).addMouseOverHandler(tooltipMouseHandlers);
			((HasMouseOutHandlers) tooltipWidget).addMouseOutHandler(tooltipMouseHandlers);
		}

		
	}
	

	/**
	 * Mouse handlers for tooltip source widget
	 * 
	 * @author jani
	 *
	 */
	private class SourceMouseHandlers implements MouseOutHandler, MouseMoveHandler, MouseOverHandler {
		private TooltipData tooltipData;

		public SourceMouseHandlers(TooltipData tooltipData) {
			super();
			this.tooltipData = tooltipData;
		}
		
		public void onMouseOver(MouseOverEvent event) {
			INSTANCE.show(tooltipData, event);
		}

		public void onMouseMove(MouseMoveEvent event) {
			int left = DOM.eventGetClientX((Event) event.getNativeEvent());
			int top = DOM.eventGetClientY((Event) event.getNativeEvent());
			INSTANCE.movePopup(left, top);	
		}

		public void onMouseOut(MouseOutEvent event) {
			INSTANCE.hide();
		}
		
	}
	
	
	/**
	 * Mouse handlers for tooltip content widget
	 * 
	 * @author jani
	 *
	 */
	private class TooltipMouseHandlers implements MouseOutHandler, MouseMoveHandler, MouseOverHandler {

		public void onMouseOver(MouseOverEvent event) {
			INSTANCE.mouseOverTooltip();
		}

		public void onMouseMove(MouseMoveEvent event) {
			// nothing to do here for now
		}

		public void onMouseOut(MouseOutEvent event) {
			INSTANCE.mouseOnTooltip = false;
			INSTANCE.delayedHide();
		}
		
	}
	

	

	

	private Timer showTimer;
	private Timer hideTimer;
	
	/**
	 * Holds information about currently displayed tooltip
	 */
	private TooltipData currentData;



	protected SGRichTooltipPopup() {
		super();
		
		popupPanel = new PopupPanel();
		popupPanel.setStyleName(DEFAULT_TOOLTIP_STYLE);
		

		showTimer = new Timer() {
			@Override
			public void run() {
				delayedShow();
			}
		};
		
		hideTimer = new Timer() {
			@Override
			public void run() {
				delayedHide();
			}
		};
	}


	
	private void positionPopup(int additionalLeft, int additionalTop) {
		popupPanel.setPopupPosition(additionalLeft, additionalTop);
	}
	

	private void movePopup(int toLeft, int toTop) {
		int newLeft;
		int newTop;
		if (currentData.leftPosition.positionType==PositionType.RELATIVE_TO_MOUSE_MOVING) {
			newLeft = currentData.leftPosition.position + toLeft;
		} else {
			newLeft = popupPanel.getPopupLeft();
		}
		
		if (currentData.topPosition.positionType==PositionType.RELATIVE_TO_MOUSE_MOVING) {
			newTop = currentData.topPosition.position +  toTop;
		} else {
			newTop = popupPanel.getPopupTop();
		}		

		if (popupPanel.isShowing()) {
			popupPanel.setPopupPosition(newLeft, newTop);
		} else {
			showTimer.schedule(currentData.showDelay);
		}
	}

	private void show(TooltipData tooltipData, MouseOverEvent event) {
		currentData = tooltipData;
		Widget source = (Widget) event.getSource();
		int left;
		int top;
		
		if (tooltipData.leftPosition.positionType==PositionType.RELATIVE_TO_WIDGET) {
			left = tooltipData.leftPosition.position + source.getAbsoluteLeft();
		} else if (tooltipData.leftPosition.positionType==PositionType.RELATIVE_TO_MOUSE_MOVING) {
			left = tooltipData.leftPosition.position + DOM.eventGetClientX((Event) event.getNativeEvent());
		} else if (tooltipData.leftPosition.positionType==PositionType.RELATIVE_TO_MOUSE_STATIC) {
			left = tooltipData.leftPosition.position + DOM.eventGetClientX((Event) event.getNativeEvent());
		} else {
			left = tooltipData.leftPosition.position;
		}
		
		
		if (tooltipData.topPosition.positionType==PositionType.RELATIVE_TO_WIDGET) {
			top = tooltipData.topPosition.position + source.getAbsoluteTop();
		} else if (tooltipData.topPosition.positionType==PositionType.RELATIVE_TO_MOUSE_MOVING) {
			top = tooltipData.topPosition.position + DOM.eventGetClientY((Event) event.getNativeEvent());
		} else if (tooltipData.topPosition.positionType==PositionType.RELATIVE_TO_MOUSE_STATIC) {
			top = tooltipData.topPosition.position + DOM.eventGetClientY((Event) event.getNativeEvent());
		} else {
			top = tooltipData.topPosition.position;
		}
		
		popupPanel.setWidget(tooltipData.widget);
		positionPopup(left, top);
		
		
		showTimer.cancel();
		if (tooltipData.showDelay > 0) {
			showTimer.schedule(tooltipData.showDelay);
		} else {
			delayedShow();
		}

	}


	private void hide() {
		hideTimer.cancel();
		if ((currentData!=null) && (currentData.hideDelay > 0)) {
			hideTimer.schedule(currentData.hideDelay);
		} else {
			delayedHide();
		}
	}

	
	private void mouseOverTooltip() {
		mouseOnTooltip = true;
	}
	
	
	/**
	 * Show tooltip
	 */
	private void delayedShow() {
		mouseOnTooltip = false;
		
		popupPanel.setPopupPositionAndShow(new PositionCallback() {
			
			public void setPosition(int offsetWidth, int offsetHeight) {
				int height = popupPanel.getOffsetHeight();
				int top = popupPanel.getAbsoluteTop();
		
				int scroll = Window.getScrollTop();
		
				int windowHeight = Window.getClientHeight();
		
				if ((height + top) > windowHeight + scroll) {
					popupPanel.setPopupPosition(popupPanel.getAbsoluteLeft(), windowHeight - height + scroll);
				}
			}
		});
	}


	private void delayedHide() {
		if (!mouseOnTooltip) {
			popupPanel.hide();
			showTimer.cancel();
			hideTimer.cancel();
		}
	}
	

}

