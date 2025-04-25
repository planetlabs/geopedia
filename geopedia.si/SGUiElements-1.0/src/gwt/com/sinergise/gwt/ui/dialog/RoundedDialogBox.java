package com.sinergise.gwt.ui.dialog;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.gwt.ui.core.MouseClickAction;
import com.sinergise.gwt.ui.core.MouseDragAction;
import com.sinergise.gwt.ui.core.MouseHandler;
import com.sinergise.gwt.ui.maingui.RoundedPanel;
import com.sinergise.gwt.util.html.CSS;


/**
 * 
 * @author jplisson
 *
 * A class to build dialog boxes with rounded corners.
 * It is organized it three parts:
 * title,
 * content,
 * status (for buttons, status bars, etc.).
 * 
 * Use corresponding functions:
 * setTitle
 * setContent
 * setStatus
 * 
 * Use GWT's normal DialogBox
 */
@Deprecated
public class RoundedDialogBox extends PopupPanel {
	public static final String 	MINIMIZE_IMG = GWT.getModuleBaseURL()+"img/minimize.gif";
	public static final String 	RESTORE_IMG  = GWT.getModuleBaseURL()+"img/restore.gif";
	public static final String 	CLOSE_IMG    = GWT.getModuleBaseURL()+"img/close.gif";
	
	private Image 				minimizeBut;
	private Image 				closeBut;
	private boolean 			minimized = false;

	private VerticalPanel		vp;
	private RoundedPanel 		rp;
	private FlexTable			fp;
	private FlexTable			titleBar;
	protected Label				title;
	protected Widget			content;
	protected Widget			status;
	  
	private int					width;
	private int					height;
	private int					cornerHeight = 7;

	private DialogDragAction	dialogDrag;
	private MouseHandler 		dragMouser;
	private Blinker 			blinker;
	
	private int					left = 0;
	private int					top = 0;
	
	public RoundedDialogBox(boolean autoHide, boolean modal, boolean closeButton, boolean minimizeButton) {
		super(autoHide, modal);
		
		width = 0;
		height = 0;
		
		Window.addResizeHandler(new ResizeHandler() {
			public void onResize(ResizeEvent event) {
				checkAndPositionPopup(getAbsoluteTop(), getAbsoluteLeft());
			}
		});
		layout(closeButton, minimizeButton);
	}

	protected void layout(boolean closeButton, boolean minimizeButton) {
		vp = new VerticalPanel();
		fp = new FlexTable();
		
		{ //TitleBar.
			titleBar = new FlexTable();
			title = new Label(" ");
			title.setWidth("100%");
			titleBar.setWidget(0, 0, title);
			titleBar.getCellFormatter().setWidth(0, 0, "100%");
			titleBar.setHeight("1em");
			titleBar.setWidth("100%");
			CSS.cursor(titleBar.getElement(), CSS.CURSOR_HAND);
			dialogDrag = new DialogDragAction();
			dragMouser = new MouseHandler(titleBar);
			dragMouser.registerAction(dialogDrag, MouseHandler.BUTTON_LEFT, MouseHandler.MOD_NONE);
			titleBar.setCellPadding(0);
			titleBar.setCellSpacing(0);
			titleBar.setStyleName("roundedDialogBox-title");
			
			//Close/minimize buttons.
			titleButtons(closeButton, minimizeButton);
			
			fp.setWidget(0, 0, titleBar);
			fp.getCellFormatter().setWidth(0, 0, "100%");
		}
		
		{ //Content (empty at creation time).
			content = null;
			fp.getCellFormatter().setWidth(1, 0, "100%");
			fp.getCellFormatter().setHeight(1, 0, "100%");
			fp.getCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
		}
		
		{ //Status (empty at creation time).
			status = null;
			fp.getCellFormatter().setWidth(2, 0, "100%");
		}
		
		fp.setCellPadding(0);
		fp.setCellSpacing(0);
		fp.setStyleName("roundedDialogBox");
		
		//Make the rounded corners.
		vp.add(fp);
		rp = new RoundedPanel(vp, RoundedPanel.ALL, cornerHeight);
		super.setWidget(rp);
	}
	
	/**
	 * Sets the title of the dialog.
	 * @param title
	 */
	@Override
	public void setTitle(String title) {
		this.title.setText(title);
	}
	
	/**
	 * Sets the content of the dialog, displayed between title and status bars.
	 * @param content
	 */
	public void setContent(Widget content) {
		this.content = content;
		fp.setWidget(1, 0, this.content);
		CSS.marginTop(this.content, 5);
	}
	
	/**
	 * Sets the status of the dialog. (e.g. label for error msg or ok/cancel buttons).
	 * @param status
	 */
	public void setStatus(Widget status) {
		this.status = status;
		fp.setWidget(2, 0, this.status);
		CSS.marginTop(this.status, 5);
	}
	
	/**
	 * Sets the width of the dialog (px or em units can be used).
	 */
	@Override
	public void setWidth(String width) {
		fp.setWidth(width);
		
		//If we reduce the dialog, the fp (content) can be reduced to
		//a certain minimal size, depending on the size of the components inside.
		//But the rounded panel (corners) is in a separate div and thus can
		//be reduced even more. That's why we have to get the size of content first
		//and then set the size of the rounded panel so that it is the same
		//size.
		rp.setWidth(fp.getOffsetWidth()+"px");
	}
	
	/**
	 * Sets the height of the dialog (px or em units can be used).
	 */
	@Override
	public void setHeight(String height) {
		fp.setHeight(height);
	}
	
	public int getWidth() {
		return (width);
	}
	
	public int getHeight() {
		return (height);
	}
	
	/**
	 * Creates minimize/close buttons if necessary.
	 * @param closeButton		true if a close button has to be added.
	 * @param minimizeButton	true to enable minimizing the dialog.
	 */
	private void titleButtons(boolean closeButton, boolean minimizeButton) {
		HorizontalPanel titleButs = new HorizontalPanel();
		
		if (minimizeButton) {
			minimizeBut = buildMinimizeButton();
			titleButs.add(minimizeBut);
			dragMouser.registerAction(new MouseClickAction("Minimize/restore") {
				@Override
				protected boolean mouseClicked(int x, int y) {
					toggleMinimized();
					return false;
				}
			}, MouseHandler.BUTTON_LEFT, MouseHandler.MOD_NONE, 2);
		}
		if (closeButton) {
			closeBut = buildCloseButton();
			titleButs.add(closeBut);
		}
		titleBar.setWidget(0, 1, titleButs);
	}
	
	/**
	 * Builds a button meant to close the dialog.
	 * @return	a clickable Image.
	 */
	protected Image buildCloseButton() {
		return makeTitleImg(CLOSE_IMG, new ClickHandler() {
			public void onClick(ClickEvent event) {
				hide();
			}
		});
	}

	/**
	 * Builds a button meant to minimize/maximize the dialog.
	 * @return	a clickable Image.
	 */
	protected Image buildMinimizeButton() {
		return minimizeBut = makeTitleImg(MINIMIZE_IMG, new ClickHandler() {
			public void onClick(ClickEvent event) {
				toggleMinimized();
			}
		});
	}
	
	/**
	 * Create the Image for the Image buttons used to close/minimize the dialog.
	 * @param url			URL of the image to use.
	 * @param listener		Click listener for the Image button.
	 * @return				The clickable image.
	 */
	private static Image makeTitleImg(String url, ClickHandler listener) {
		Image img = new Image(url);
		img.addClickHandler(listener);
		CSS.floating(img.getElement(), CSS.FLOAT_RIGHT);
		CSS.cursor(img.getElement(), CSS.CURSOR_HAND);
		DOM.setStyleAttribute(img.getElement(), "marginLeft", "3px");
		return img;
	}

	@Override
	public void show() {
		super.show();
		dragMouser = new MouseHandler(titleBar);
		dragMouser.registerAction(dialogDrag, MouseHandler.BUTTON_LEFT, MouseHandler.MOD_NONE);

		// (The width must be set after the super.show() otherwise it will be 0).
		// NOTE: As of GWT 1.1 the .getOffsetWidth() returns 0 at this point.
		// This is different with earlier versions of GWT. Disable the line will make
		// the rounded corners work in at least Firefox 1.5, but not in IE.
		rp.setWidth(vp.getOffsetWidth() + "px");
		width = getOffsetWidth();
		height = getOffsetHeight();
	}
	
	/**
	 * Show the dialog on the right of another widget.
	 * @param w		The widget nearby to position the dialog.
	 */
	public void showNextTo(Widget w) {
		showNextTo(w, w.getOffsetWidth(), 0);
	}

	private void showNextTo(final Widget w, final int offX, final int offY) {
		setPopupPositionAndShow(new PositionCallback() {
			public void setPosition(int offsetWidth, int offsetHeight) {
				int posTop = w.getAbsoluteTop() + offY;
				int posLeft = w.getAbsoluteLeft() + offX;
				
				width = offsetWidth;
				height = offsetHeight;
				
				checkAndPositionPopup(posTop+Window.getScrollTop(), posLeft+Window.getScrollLeft());
			}
		});
	}
	
	/**
	 * Display the dialog in the center of the browser window.
	 */
	public void showCentered() {
		setPopupPositionAndShow(new PositionCallback() {
			public void setPosition(int offsetWidth, int offsetHeight) {
				int posTop = Window.getClientHeight() / 2;
				int posLeft = Window.getClientWidth() / 2;
				
				posTop -= (offsetHeight / 2);
				posLeft -= (offsetWidth / 2);
				
				width = offsetWidth;
				height = offsetHeight;
				
				checkAndPositionPopup(posTop+Window.getScrollTop(), posLeft+Window.getScrollLeft());
			}
		});
	}

	public void show(final int x, final int y) {
		setPopupPositionAndShow(new PositionCallback() {
			public void setPosition(int offsetWidth, int offsetHeight) {
				width = offsetWidth;
				height = offsetHeight;
				
				checkAndPositionPopup(y, x);
			}
		});
	}
	
	/**
	 * Checks that the new position of the dialog is contained within
	 * the browser area. If not, reposition.
	 * @param yTop		The new y coordinate of the dialog.
	 * @param xLeft		The new x coordinate of the dialog.
	 */
	private void checkAndPositionPopup(int yTop, int xLeft) {
		if (yTop + height > ((Window.getClientHeight()+Window.getScrollTop()) - this.top))
			yTop = Window.getClientHeight() - height - this.top;
		if (xLeft + width > ((Window.getClientWidth()+Window.getScrollLeft()) - this.left))
			xLeft = Window.getClientWidth() - width - this.left;
		if (xLeft < this.left)
			xLeft = this.left;
		if (yTop < this.top)
			yTop = this.top;
		if (yTop != getAbsoluteTop() || xLeft != getAbsoluteLeft())
			setPopupPosition(xLeft, yTop);
	}
	
	/**
	 * Set the left margin for the dialog. The dialog cannot be moved over that margin.
	 * @param l
	 */
	public void setLeft(int l) {
		if (l < 0)
			l = 0;

		width = getOffsetWidth();
		height = getOffsetHeight();
		if (this.top != getAbsoluteTop() || this.left != getAbsoluteLeft()) {
			left = l;
			checkAndPositionPopup(getPopupTop(), l);
		}
	}

	/**
	 * Set the top margin for the dialog. The dialog cannot be moved over that margin.
	 * @param t
	 */
	public void setTop(int t) {
		if (t < 0)
			t = 0;

		width = getOffsetWidth();
		height = getOffsetHeight();
		if (this.top != getAbsoluteTop() || this.left != getAbsoluteLeft()) {
			top = t;
			checkAndPositionPopup(t, getPopupLeft());
		}
	}

	/**
	 * Minimizes/maximizes the dialog.
	 */
	protected void toggleMinimized() {
		minimized = !minimized;

		if (minimizeBut != null) {
			minimizeBut.setUrl(minimized ? RESTORE_IMG : MINIMIZE_IMG);
			content.setVisible(!minimized);
			status.setVisible(!minimized);
		}
	}

	/**
	 * Detects clicks/typing outside the modal dialog and makes the title blink.
	 */
	@Override
	public boolean onEventPreview(Event event) {
		if ((DOM.eventGetType(event) & Event.ONMOUSEDOWN) != 0 ||
			(DOM.eventGetType(event) & Event.ONFOCUS) != 0) {
			/*
			 * Firefox: the client area stops at the scroll bar. If a mouse
			 * event is outside the client area, it is targeted to the
			 * scrollbar. Override default behavior to allow these events. 
			 */
			int clientX = DOM.eventGetClientX(event);
			int clientY = DOM.eventGetClientY(event);
			if ((clientX > Window.getClientWidth() || clientY > Window.getClientHeight()))
			{
				return true;
			}
		}
		if (super.onEventPreview(event)) {
			return true;
		}

		switch (DOM.eventGetType(event)) {
		case Event.ONMOUSEDOWN:
		case Event.ONKEYDOWN:
			titleBlink(6);
		}
		return false;
	}
		
	/**
	 * Makes the title blink.
	 * @param nBlinks	The number of blinks.
	 */
	public void titleBlink(int nBlinks) {
		if (blinker == null)
			blinker = new Blinker();

		blinker.blink(nBlinks);
	}

	private class Blinker extends Timer {
		boolean 	scheduled;
		int 		nBlinks;

		public void blink(int nRepeats) {
			this.nBlinks = nRepeats;
			if (!scheduled) {
				scheduled = true;
				schedule(1);
			}
		}

		@Override
		public void run() {
			nBlinks--;
			implHighlightTitle(0 != (nBlinks & 1));
			if (nBlinks > 0)
				schedule(300);
			else
				scheduled = false;
		}

		private void implHighlightTitle(boolean highlighted) {
			if (highlighted) {
				UIObject.setStyleName(titleBar.getElement(), "roundedDialogBox-title-highlighted", true);
			} else {
				UIObject.setStyleName(titleBar.getElement(), "roundedDialogBox-title-highlighted", false);
			}
		}

	}
	
	/**
	 * Class to handle dialog dragging.
	 * Checks that the window does not go out of the browser area.
	 */
	private class DialogDragAction extends MouseDragAction {
		private int dragStartX, dragStartY;
		
		DialogDragAction() {
			super("Move dialog");
			useDocumentCoords=true;
			volatileParentPosition=false;
			setProperty(DRAG_CURSOR, "move");
			setProperty(CURSOR, "move");
			setDragDelay(1);
		}

		@Override
		protected boolean dragStart(int x, int y) {
			dragStartX = x - getPopupLeft();
			dragStartY = y - getPopupTop();
			return true;
		}

		@Override
		protected void dragMove(int x, int y) {
			width = getOffsetWidth();
			height = getOffsetHeight();
			int posX = MathUtil.clamp(0, x-dragStartX, Math.max(0, Window.getClientWidth()-width));
			int posY = MathUtil.clamp(0, y-dragStartY, Math.max(0, Window.getClientHeight()-height));
			setPopupPosition(posX, posY);
		}

		@Override
		protected void dragEnd(int x, int y) {}
	}
}
