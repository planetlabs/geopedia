package com.sinergise.gwt.ui.dialog;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.core.IResizable;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.gwt.ui.core.MouseClickAction;
import com.sinergise.gwt.ui.core.MouseDragAction;
import com.sinergise.gwt.ui.core.MouseHandler;
import com.sinergise.gwt.ui.core.ResizeHandler;
import com.sinergise.gwt.util.html.CSS;


/**
 * @see RoundedDialogBox
 * @author jsrebrnic
 */
@Deprecated
public class Dialog extends PopupPanel implements IResizable, CloseHandler<PopupPanel> {
	public static final String MINIMIZE_IMG = GWT.getModuleBaseURL()+"img/minimize.gif";
	public static final String RESTORE_IMG = GWT.getModuleBaseURL()+"img/restore.gif";
	public static final String CLOSE_IMG = GWT.getModuleBaseURL()+"img/close.gif";

	public static final String HILITE_TITLE_STYLE = "dialog-title-highlighted";

	protected FlowPanel outer, titleBar, statusBar, inner;

	Element tl, t, tr, l, r, bl, b, br;

	static final int LR_SIZE = 8;
	static final int TB_SIZE = 8;

	boolean bleft = true, bright = true, btop = true, bbottom = true;

	int width = 400, height = 300;

	DialogExcludeContext exclude;

	boolean autoHide;
	boolean autoSize = false;

	/**
	 * 
	 * @param autoHide
	 * @param modal
	 * @param closeButton
	 * @param minimizeButton
	 * @param exclude
	 *            two dialogs can not be show at the same time having the same
	 *            lock --> exclude
	 */
	public Dialog(boolean autoHide, boolean modal, boolean closeButton, boolean minimizeButton, DialogExcludeContext exclude) {
		super(autoHide, modal);
		if (this.autoHide == autoHide)
			addCloseHandler(this);

		this.exclude = exclude;

		makeDivs();
		super.setWidget(outer);

		if (closeButton) {
			Image close = makeTitleImg(CLOSE_IMG, new ClickHandler() {
				public void onClick(ClickEvent event) {
					if (mayHide()) {
						hide();
					}
				}
			});
			titleBar.add(close);
		}
		if (minimizeButton) {
			miniButton = makeTitleImg(MINIMIZE_IMG, new ClickHandler() {
				public void onClick(ClickEvent event) {
					toggleMinimized();
				}
			});
			titleBar.add(miniButton);

			dragMouser.registerAction(new MouseClickAction("Minimize/restore") {
				@Override
				protected boolean mouseClicked(int x, int y) {
					toggleMinimized();
					return false;
				}
			}, MouseHandler.BUTTON_LEFT, MouseHandler.MOD_NONE, 2);
		}

		updateUI();
	}

	boolean minimized = false;

	static Image makeTitleImg(String url, ClickHandler handler) {
		Image img = new Image(url);
		img.addClickHandler(handler);
		CSS.floating(img.getElement(), CSS.FLOAT_RIGHT);
		CSS.cursor(img.getElement(), CSS.CURSOR_HAND);
		DOM.setStyleAttribute(img.getElement(), "marginLeft", "3px");
		return img;
	}

	void toggleMinimized() {
		minimized = !minimized;

		if (miniButton != null)
			miniButton.setUrl(minimized ? RESTORE_IMG : MINIMIZE_IMG);

		updateResizeHandler();
		updateUI();
	}

	Image miniButton;

	protected boolean mayHide() {
		return true;
	}

	/**
	 * @deprecated use add
	 */
	@Deprecated
	@Override
	public void setWidget(Widget w) {
		super.setWidget(w);
	}

	@Override
	public boolean onEventPreview(Event event) {
		if (super.onEventPreview(event))
			return true;

		switch (DOM.eventGetType(event)) {
		case Event.ONMOUSEDOWN:
		case Event.ONKEYDOWN:
			blink();
		}
		return false;
	}

	boolean visible = false;

	@Override
	public void setVisible(boolean visible) {
		if (visible == this.visible)
			return;

		this.visible = visible;
		if (visible) {
			super.show();
			onShow();
			updateUILater();
			if (exclude != null)
				exclude.imVisible(this);
		} else {
			super.hide();
			if (exclude != null)
				exclude.imNotVisible(this);
			if (!autoHide) // this case gets caught by the listener
				onHide();
		}
	}

	@Override
	public void setPopupPosition(int left, int top) {
		// XXX: this exact same checks are performed in
		// super.setPopupPosition(...,...).Do we really need this?
		if (left < 0)
			left = 0;

		if (top < 0)
			top = 0;
		super.setPopupPosition(left, top);
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	public void showNextTo(Widget w) {
		showNextTo(w, w.getOffsetWidth(), 0);
	}

	public void showCentered() {
		int top = Window.getClientHeight() - height;
		int left = Window.getClientWidth() - width;
		top /= 2;
		left /= 2;
		if (top < 0)
			top = 0;
		if (left < 0)
			left = 0;
		setPopupPosition(left, top);
		show();
	}

	public void showNextTo(Widget w, int offX, int offY) {
		int top = w.getAbsoluteTop() + offY;
		int left = w.getAbsoluteLeft() + offX;

		if (left + width > Window.getClientWidth())
			left = Window.getClientWidth() - width;
		if (left < 0)
			left = 0;
		if (top + height > Window.getClientHeight())
			top = Window.getClientHeight() - height;
		if (top < 0)
			top = 0;

		setPopupPosition(left, top);
		show();
	}

	@Override
	public void show() {
		setVisible(true);
	}

	@Override
	public void hide() {
		setVisible(false);
	}

	@Override
	public void add(Widget w) {
		inner.add(w);
		updateUILater();
	}

	public void addToTitle(Widget w) {
		titleBar.add(w);
		updateUILater();
	}
	
	public void removeFromTitle(Widget w) {
		titleBar.remove(w);
		updateUILater();
	}
	
	public void clearTitle() {
		titleBar.clear();
		updateUILater();
	}
	
	public void addToStatus(Widget w) {
		statusBar.add(w);
		updateUILater();
	}

	@Override
	public boolean remove(Widget w) {
		updateUILater();
		if (statusBar.remove(w))
			return true;
		if (inner.remove(w))
			return true;
		return titleBar.remove(w);
	}

	boolean updateScheduled = false;

	void updateUILater() {
		if (updateScheduled)
			return;

		updateScheduled = true;

		new Timer() {
			@Override
			public void run() {
				updateScheduled = false;
				updateUI();
			}
		}.schedule(1);
	}

	void updateUIMinimized() {
		int innerLeft = bleft ? LR_SIZE : 0;
		int innerRight = bright ? width - LR_SIZE : width;
		int innerW = innerRight - innerLeft;

		int innerTop = btop ? TB_SIZE : 0;
		if (!titleBar.isVisible()) {
			titleBar.setVisible(true);
			updateUILater();
		}
		CSS.leftTop(titleBar, innerLeft, innerTop);
		CSS.width(titleBar, innerW);

		int titleH = getOffsetHeight(titleBar.getElement());

		int innerBottom = innerTop + titleH;
		int innerH = titleH;

		CSS.leftTop(tl, 0, 0);
		CSS.leftTop(t, innerLeft, 0);
		CSS.leftTop(tr, innerRight, 0);
		CSS.leftTop(l, 0, innerTop);
		CSS.leftTop(r, innerRight, innerTop);
		CSS.leftTop(bl, 0, innerBottom);
		CSS.leftTop(b, innerLeft, innerBottom);
		CSS.leftTop(br, innerRight, innerBottom);

		CSS.size(outer, width, innerBottom + (bbottom ? TB_SIZE : 0));
		_superSetPixelSize(width, innerBottom + (bbottom ? TB_SIZE : 0));
		CSS.size(tl, LR_SIZE, TB_SIZE);
		CSS.size(t, innerW, TB_SIZE);
		CSS.size(tr, LR_SIZE, TB_SIZE);
		CSS.size(l, LR_SIZE, innerH);
		CSS.size(r, LR_SIZE, innerH);
		CSS.size(bl, LR_SIZE, TB_SIZE);
		CSS.size(b, innerW, TB_SIZE);
		CSS.size(br, LR_SIZE, TB_SIZE);

		statusBar.setVisible(false);
		inner.setVisible(false);

		makeVisible(tl, btop && bleft);
		makeVisible(t, btop);
		makeVisible(tr, btop && bright);
		makeVisible(l, bleft);
		makeVisible(r, bright);
		makeVisible(bl, bbottom && bleft);
		makeVisible(b, bbottom);
		makeVisible(br, bbottom && bright);
	}

	void updateUI() {
		if (minimized) {
			updateUIMinimized();
			return;
		}

		int innerLeft = bleft ? LR_SIZE : 0;
		int innerRight = bright ? width - LR_SIZE : width;
		int innerW = innerRight - innerLeft;

		int innerTop = btop ? TB_SIZE : 0;
		int innerBottom = bbottom ? height - TB_SIZE : height;
		int innerH = innerBottom - innerTop;

		CSS.leftTop(tl, 0, 0);
		CSS.leftTop(t, innerLeft, 0);
		CSS.leftTop(tr, innerRight, 0);
		CSS.leftTop(l, 0, innerTop);
		CSS.leftTop(r, innerRight, innerTop);
		CSS.leftTop(bl, 0, innerBottom);
		CSS.leftTop(b, innerLeft, innerBottom);
		CSS.leftTop(br, innerRight, innerBottom);

		CSS.size(outer, width, height);
		_superSetPixelSize(width, height);
		CSS.size(tl, LR_SIZE, TB_SIZE);
		CSS.size(t, innerW, TB_SIZE);
		CSS.size(tr, LR_SIZE, TB_SIZE);
		CSS.size(l, LR_SIZE, innerH);
		CSS.size(r, LR_SIZE, innerH);
		CSS.size(bl, LR_SIZE, TB_SIZE);
		CSS.size(b, innerW, TB_SIZE);
		CSS.size(br, LR_SIZE, TB_SIZE);

		if (titleBar.getWidgetCount() < 1) {
			titleBar.setVisible(false);
		} else {
			if (!titleBar.isVisible()) {
				titleBar.setVisible(true);
				updateUILater();
			}
			CSS.leftTop(titleBar, innerLeft, innerTop);
			CSS.width(titleBar, innerW);

			int titleH = getOffsetHeight(titleBar.getElement());
			innerTop += titleH;
			innerH -= titleH;
			if (innerH < 1)
				innerH = 1;
		}

		if (statusBar.getWidgetCount() < 1) {
			statusBar.setVisible(false);
		} else {
			if (!statusBar.isVisible()) {
				statusBar.setVisible(true);
				updateUILater();
			}
			int statusHeight = getOffsetHeight(statusBar.getElement());
			int statusY = innerTop + innerH - statusHeight;
			innerH -= statusHeight;
			if (innerH < 1)
				innerH = 1;
			CSS.leftTop(statusBar, innerLeft, statusY);
			CSS.width(statusBar, innerW);
		}

		CSS.leftTop(inner, innerLeft, innerTop);
		inner.setVisible(true);
		inner.setPixelSize(innerW, innerH);

		makeVisible(tl, btop && bleft);
		makeVisible(t, btop);
		makeVisible(tr, btop && bright);
		makeVisible(l, bleft);
		makeVisible(r, bright);
		makeVisible(bl, bbottom && bleft);
		makeVisible(b, bbottom);
		makeVisible(br, bbottom && bright);
	}

	public static int getOffsetHeight(Element el) {
		return DOM.getElementPropertyInt(el, "offsetHeight");
	}

	public static int getOffsetWidth(Element el) {
		return DOM.getElementPropertyInt(el, "offsetWidth");
	}

	void makeVisible(Element el, boolean newVisible) {
		CSS.display(el, newVisible ? "block" : "none");
	}

	static final String IMG_PREFIX = "img/bp/";

	void makeDivs() {
		outer = makeFlow();
		CSS.fullSize(outer.getElement());
		CSS.overflow(outer, CSS.OVR_HIDDEN);

		tl = makeImgDiv(IMG_PREFIX + "tl.gif");
		t = makeImgDiv(IMG_PREFIX + "t.gif");
		tr = makeImgDiv(IMG_PREFIX + "tr.gif");
		l = makeImgDiv(IMG_PREFIX + "l.gif");
		r = makeImgDiv(IMG_PREFIX + "r.gif");
		bl = makeImgDiv(IMG_PREFIX + "bl.gif");
		b = makeImgDiv(IMG_PREFIX + "b.gif");
		br = makeImgDiv(IMG_PREFIX + "br.gif");

		inner = makeFlow();
		CSS.overflow(inner, CSS.OVR_AUTO);
		CSS.background(inner, "white");

		titleBar = makeFlow();
		titleBar.setStyleName("dialog-title");
		CSS.overflow(titleBar, CSS.OVR_HIDDEN);
		dragMouser = new MouseHandler(titleBar);
		dragMouser.registerAction(dialogDrag, MouseHandler.BUTTON_LEFT, MouseHandler.MOD_NONE);

		statusBar = makeFlow();
		statusBar.setStyleName("dialog-status");
		CSS.overflow(statusBar, CSS.OVR_HIDDEN);

		outer.add(inner);
		outer.add(titleBar);
		outer.add(statusBar);

		DOM.appendChild(outer.getElement(), tl);
		DOM.appendChild(outer.getElement(), t);
		DOM.appendChild(outer.getElement(), tr);
		DOM.appendChild(outer.getElement(), l);
		DOM.appendChild(outer.getElement(), r);
		DOM.appendChild(outer.getElement(), bl);
		DOM.appendChild(outer.getElement(), b);
		DOM.appendChild(outer.getElement(), br);
	}

	@Override
	protected void onAttach() {
		super.onAttach();
	}

	private static FlowPanel makeFlow() {
		FlowPanel panel = new FlowPanel();
		CSS.position(panel, CSS.POS_ABSOLUTE);
		return panel;
	}

	private static Element makeDiv() {
		Element div = DOM.createDiv();
		CSS.position(div, CSS.POS_ABSOLUTE);
		return div;
	}

	private static Element makeImgDiv(String imgUrl) {
		Element div = makeDiv();
		CSS.background(div, "url(" + imgUrl + ")");
		CSS.fontSize(div, "0");
		return div;
	}

	private void _superSetPixelSize(int w, int h) {
		super.setWidth(w + "px");
		super.setHeight(h + "px");
	}

	@Override
	public void setPixelSize(int width, int height) {
		super.setPixelSize(width, height);
		this.width = width;
		this.height = height;
		updateUI();
	}

	public void setInnerSize(int w, int h) {
		setPixelSize(w + 2 * LR_SIZE, h + 2 * TB_SIZE);
	}

	@Override
	public void setHeight(String height) {
		if (height.endsWith("px")) {
			this.height = Integer.parseInt(height.substring(0, height.length() - 2));
			super.setHeight(height);
			updateUI();
		} else
			throw new IllegalStateException();
	}

	public void setHeight(int height) {
		setHeight(height + "px");
	}

	@Override
	public void setWidth(String width) {
		if (width.endsWith("px")) {
			this.width = Integer.parseInt(width.substring(0, width.length() - 2));
			super.setWidth(width);
			updateUI();
		} else
			throw new IllegalStateException();
	}

	public void setWidth(int width) {
		setWidth(width + "px");
	}

	@Override
	public void setSize(String width, String height) {
		setWidth(width);
		setHeight(height);
	}

	boolean resizable = false;
	ResizeHandler resizeHandler;

	public void setResizable(boolean resizable) {
		this.resizable = resizable;

		updateResizeHandler();
	}

	void updateResizeHandler() {
		if (!resizable) {
			if (resizeHandler == null)
				return;

			resizeHandler.deregisterHandle(t);
			resizeHandler.deregisterHandle(l);
			resizeHandler.deregisterHandle(b);
			resizeHandler.deregisterHandle(r);
			resizeHandler.deregisterHandle(tl);
			resizeHandler.deregisterHandle(tr);
			resizeHandler.deregisterHandle(bl);
			resizeHandler.deregisterHandle(br);

			resizeHandler = null;
			return;
		}

		if (resizeHandler == null)
			resizeHandler = new ResizeHandler(this);

		if (minimized) {
			resizeHandler.deregisterHandle(t);
			resizeHandler.deregisterHandle(b);

			resizeHandler.registerHandle(l, ResizeHandler.WEST);
			resizeHandler.registerHandle(r, ResizeHandler.EAST);
			resizeHandler.registerHandle(tl, ResizeHandler.WEST);
			resizeHandler.registerHandle(tr, ResizeHandler.EAST);
			resizeHandler.registerHandle(bl, ResizeHandler.WEST);
			resizeHandler.registerHandle(br, ResizeHandler.EAST);
		} else {
			resizeHandler.registerHandle(t, ResizeHandler.NORTH);
			resizeHandler.registerHandle(l, ResizeHandler.WEST);
			resizeHandler.registerHandle(b, ResizeHandler.SOUTH);
			resizeHandler.registerHandle(r, ResizeHandler.EAST);
			resizeHandler.registerHandle(tl, ResizeHandler.NORTHWEST);
			resizeHandler.registerHandle(tr, ResizeHandler.NORTHEAST);
			resizeHandler.registerHandle(bl, ResizeHandler.SOUTHWEST);
			resizeHandler.registerHandle(br, ResizeHandler.SOUTHEAST);
		}
	}

	public int getHeight() {
		return height;
	}

	public int getLeft() {
		return getPopupLeft();
	}

	public int getMaximalHeight() {
		return 2000; // XXX
	}

	public int getMaximalWidth() {
		return 2000; // XXX
	}

	public int getMinimalHeight() {
		return 50;
	}

	public int getMinimalWidth() {
		return 80;
	}

	public int getTop() {
		return getPopupTop();
	}

	public int getWidth() {
		return width;
	}

	public boolean isResizable() {
		return resizable;
	}

	public void setLeft(int l) {
		if (l < 0)
			l = 0;

		setPopupPosition(l, getPopupTop());
	}

	public void setTop(int t) {
		if (t < 0)
			t = 0;

		setPopupPosition(getPopupLeft(), t);
	}

	DialogDragAction dialogDrag = new DialogDragAction();
	MouseHandler dragMouser;

	private class DialogDragAction extends MouseDragAction {
		private int xOff, yOff;
		
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
			xOff = x - getPopupLeft();
			yOff = y - getPopupTop();
			return true;
		}

		@Override
		protected void dragMove(int x, int y) {
			int posX = MathUtil.clamp(0, x-xOff, Math.max(0, Window.getClientWidth()-width));
			int posY = MathUtil.clamp(0, y-yOff, Math.max(0, Window.getClientHeight()-height));
			setPopupPosition(posX, posY);
		}

		@Override
		protected void dragEnd(int x, int y) {

		}
	}

	Label statusContentText;

	public void setStatusContentText(String txt) {
		if (statusContentText != null)
			remove(statusContentText);

		if (txt != null)
			addToStatus(statusContentText = new Label(txt));
	}

	public void setStatusContentHtml(String html) {
		if (statusContentText != null)
			remove(statusContentText);

		if (html != null)
			addToStatus(statusContentText = new HTML(html));
	}

	public void clearStatus() {
		statusBar.clear();
		updateUILater();
	}

	public boolean okToHide() {
		return true;
	}

	protected void onHide() {

	}

	protected void onShow() {

	}

	Blinker blinker;
	boolean titleHilited = false;

	public void highlightTitle(boolean hilite) {
		implHighlightTitle(titleHilited = hilite);
	}

	private void implHighlightTitle(boolean hilited) {
		if (hilited) {
			UIObject.setStyleName(titleBar.getElement(), HILITE_TITLE_STYLE, true);
		} else {
			UIObject.setStyleName(titleBar.getElement(), HILITE_TITLE_STYLE, false);
		}
	}

	public void blink() {
		if (blinker == null)
			blinker = new Blinker();

		blinker.blink();
	}

	public void onClose(CloseEvent<PopupPanel> event) {
		if (event.isAutoClosed()) {
			visible = false;

			onHide();
		}
		if (exclude != null) {
			exclude.imNotVisible(this);
		}
	}
	
	

	class Blinker extends Timer {
		boolean scheduled;
		int nBlinks;

		public void blink() {
			nBlinks = 6;
			if (!scheduled) {
				scheduled = true;
				schedule(1);
			}
		}

		@Override
		public void run() {
			nBlinks--;
			implHighlightTitle(titleHilited ^ (0 != (nBlinks & 1)));

			if (nBlinks > 0)
				schedule(300);
			else
				scheduled = false;
		}
	}
	
	
}
