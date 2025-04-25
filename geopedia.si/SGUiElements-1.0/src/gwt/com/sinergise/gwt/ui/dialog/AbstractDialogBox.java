package com.sinergise.gwt.ui.dialog;

import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.sinergise.common.ui.i18n.Tooltips;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.gwt.util.html.CSS;

public abstract class AbstractDialogBox extends DialogBox {

	private static final int RESIZE_ELEMENT_WIDTH = 24;
	private static final int RESIZE_ELEMENT_HEIGHT = 24;
	
	protected Element gray;
	protected boolean grayBackground = false;
	protected boolean isResizable = false;
	protected DragElement elBottomRight = new DragElement();
	
	
	
	private class DragElement extends SimplePanel {	
	}
	public AbstractDialogBox(boolean autoHide, boolean modal) {
		this(autoHide,modal, false);
	}

	public AbstractDialogBox(boolean autoHide, boolean modal, boolean grayBackground) {
		this(autoHide, modal, grayBackground, false); 
	}
	
	public AbstractDialogBox(boolean autoHide, boolean modal, boolean grayBackground, boolean isResizable) {
		super(autoHide, modal);				
		this.grayBackground = grayBackground;
		this.isResizable = isResizable;
		if (isResizable) {
		  Element td = getCellElement(2, 2);
		  DOM.appendChild(td, elBottomRight.getElement());
		  adopt(elBottomRight);
		  elBottomRight.setStyleName("bottomRight");
		  elBottomRight.setTitle(Tooltips.INSTANCE.resizWindow());
		  elBottomRight.setSize(RESIZE_ELEMENT_WIDTH+"px", RESIZE_ELEMENT_HEIGHT+"px");
		  addStyleName("resizable");
		}
		
		addCloseHandler(new CloseHandler<PopupPanel>() {
			public void onClose(CloseEvent<PopupPanel> event) {
				removeGray();
			}
		});

	}
	
	
	 @Override
	  public void onBrowserEvent(Event event) {
		 if (isResizable) {
		    switch (event.getTypeInt()) {
		      case Event.ONMOUSEDOWN:
		      case Event.ONMOUSEUP:
		      case Event.ONMOUSEMOVE:
		        if (isResizing || isElementEventTarget(getCellElement(2, 2), event)) {
		        	onBottomRightEvent(event);	        	
		          return;
		        }
		    }
		 }
	    super.onBrowserEvent(event);
	  }

	private boolean isResizing = false;
	private int resizeStartX, resizeStartY;
	private ResizeTask resizeTask = new ResizeTask();
	
	private void onBottomRightEvent(Event event) {
		int posX = DOM.eventGetClientX(event);
		int posY = DOM.eventGetClientY(event);
		switch (event.getTypeInt()) {
			case Event.ONMOUSEDOWN:
				if (!isResizing) {
					isResizing=true;
					resizeStartX = posX;
					resizeStartY = posY;
					DOM.setCapture(getElement());
					elBottomRight.addStyleName("down");
				}
				break;
			case Event.ONMOUSEUP:
				if (isResizing) {
					isResizing=false;
					elBottomRight.removeStyleName("down");
					DOM.releaseCapture(getElement());
					if (resizeStartX!=posX || resizeStartY!=posY) {
						int width = posX-getAbsoluteLeft();
						int height = posY-getAbsoluteTop();
						resizeTask.scheduleResize(width,height);
					}
				}
				break;
			case Event.ONMOUSEMOVE:
				if (isResizing) {
					int width = posX-getAbsoluteLeft();
					int height = posY - getAbsoluteTop();
					resizeTask.scheduleResize(width,height);
					elBottomRight.addStyleName("down");
				}
				break;
		}
	}
	
	
	public DimI getDialogSize() {
		int botXafter = elBottomRight.getAbsoluteLeft()+RESIZE_ELEMENT_WIDTH;
		int botYafter = elBottomRight.getAbsoluteTop()+RESIZE_ELEMENT_HEIGHT;
		int actualWidth = botXafter-getAbsoluteLeft();
		int actualHeight = botYafter-getAbsoluteTop();
		return new DimI(actualWidth, actualHeight);
	}
	
	
	private class ResizeTask extends Timer {
		private int newWidth = 0;
		private int newHeight = 0;
		private boolean working = false;
		
		private boolean isScheduled = false;
		public void scheduleResize(int width, int height) {
			newWidth=width;
			newHeight=height;
			if (!isScheduled) {
				isScheduled=true;
				schedule(50);
			}
		}
		@Override
		public void run() {
			if (working)
				return;
			while (newWidth!=0 && newHeight!=0) {
				int w=newWidth; int h=newHeight;
				newWidth=0;
				newHeight=0;
				resize(w,h);
			}
			working=false;
			isScheduled=false;
		}
		
		private void resize(int width, int height) {
			if (width>0 && height>0) {
				if (!dialogResizePending(width,height))
					return;
				setSize(width+"px", height+"px");			
				DimI actualSize = getDialogSize();
				dialogResizing(actualSize.w(),actualSize.h());
			}
		}

		
	}

	
	@SuppressWarnings("unused")
	protected boolean dialogResizePending(int width, int height) {
		return false;
	}
	@SuppressWarnings("unused")
	protected void dialogResizing(int width, int height) {
		
	}
	
	
	@Override
	  protected void onPreviewNativeEvent(NativePreviewEvent event) {
	    // prevent default event outside dialog when resizing
		NativeEvent nativeEvent = event.getNativeEvent();

		if (isResizable) {
		    if (!event.isCanceled()
		        && (event.getTypeInt() == Event.ONMOUSEDOWN)
		        && isElementEventTarget(getCellElement(2, 2), nativeEvent)) {
		      nativeEvent.preventDefault();
		    }
		}
		if (event.getTypeInt()==Event.ONKEYDOWN) {			
			  event.consume();
		}

	    super.onPreviewNativeEvent(event);
	    
	    //implementation of closing the dialog on ESCAPE
        switch (event.getTypeInt()) {
            case Event.ONKEYDOWN:
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
                    hide();
                }
                break;
        }
	  }
	
	@SuppressWarnings({"static-access"})
	protected static boolean isElementEventTarget(Element element, NativeEvent event) {
		
		EventTarget target = event.getEventTarget();
		if (Element.is(target)) { return element.getParentElement().isOrHasChild(Element.as(target)); }
		return false;
	}	 
	 
	 
	 
	/**
	 * Dialog box is without close button by default. This method creates the close button. It should be
	 * added somewhere within the dialog 
	 * 
	 * @return
	 */
	protected Anchor createCloseButton() {
		Anchor actionCloseDialog = new Anchor();
		actionCloseDialog.setStyleName("actionClose");
		actionCloseDialog.addClickHandler(new ClickHandler() {
			
			public void onClick(ClickEvent event) {
				AbstractDialogBox.this.hide();
			}
		});
		return actionCloseDialog;	
	}

	
	private void removeGray() {
		if (gray == null)
			return;
		UIObject.setVisible(gray, false);
		if (DOM.getParent(gray) != null) {
			DOM.removeChild(RootPanel.get().getElement(), gray);
			gray = null;
		}
	}

	private void addGray() {
		if (gray != null)
			return;
		gray = DOM.createDiv();
		CSS.className(gray, "grayBackground");
		DOM.appendChild(RootPanel.get().getElement(), gray);
	}
	
	
	
	@Override
	public void show() {
		if (grayBackground) addGray();
		super.show();
	}
	
	@Override
	public void hide() {
		if (onBeforeClose()) {
			super.hide();
		}
	}
	
	protected void hideNoCheck() {
		super.hide();
	}
	
	@Override
	public final void hide(boolean autoClosed) {
		removeGray();
		super.hide(autoClosed);
	}
	
	
	/**
	 * Override this method to prevent closing
	 * 
	 * @return true, if dialog can be closed
	 */
	protected boolean onBeforeClose() {
		return true;
	}
	
	public static void showCentered(final DialogBox dialogBox) {
		dialogBox.setPopupPositionAndShow(new PositionCallback() {
			public void setPosition(int offsetWidth, int offsetHeight) {
				int top = Window.getClientHeight()/2-offsetHeight/2;
				int left = Window.getClientWidth()/2-offsetWidth/2;
				if (top < 0) top = 0;
				if (left < 0) left = 0;
				dialogBox.setPopupPosition(left, top);
			}
		});
	}

}
