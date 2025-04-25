package com.sinergise.gwt.ui.dialog;

import static com.google.gwt.event.dom.client.KeyCodes.KEY_ESCAPE;
import static com.google.gwt.user.client.Event.ONKEYDOWN;
import static com.google.gwt.user.client.Event.ONMOUSEDOWN;
import static com.google.gwt.user.client.Event.ONMOUSEUP;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.i18n.Tooltips;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.gwt.ui.ButtonFactory;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.util.html.CSS;

public class SGDialogBox extends DialogBox {

	private static final int RESIZE_ELEMENT_WIDTH = 13;
	private static final int RESIZE_ELEMENT_HEIGHT = 13;
	private String STYLE_BUTTON_PANEL = "dialogButtonPanel";

	protected boolean grayBackground = false;
	protected boolean isResizable = false;
	private boolean isResizing = false;

	private int resizeStartX, resizeStartY;
	private int maxHeight, maxWidth, wH, wW = 0;

	protected SimplePanel gray, buttonPanelHolder;
	protected SimplePanel elBottomRight = new SimplePanel();
	protected Anchor actionCloseDialog;
	private ResizeTask resizeTask = new ResizeTask();
	private ContentPanel innerWrap;
	private SGFlowPanel wrap, content;
	private FlowPanel buttonPanel;

	public interface OnDialogBoxValueChangedListener<T> {
		public void onDialogBoxValueChanged(T value, boolean isCanceled);
	}

	private class ResizeTask extends Timer {
		private int newWidth = 0;
		private int newHeight = 0;
		private boolean working = false;
	
		private boolean isScheduled = false;
	
		public void scheduleResize(int width, int height) {
			newWidth = width;
			newHeight = height;
			if (!isScheduled) {
				isScheduled = true;
				schedule(50);
			}
		}
	
		@Override
		public void run() {
			if (working) {
				return;
			}
			while (newWidth != 0 && newHeight != 0) {
				int w = newWidth;
				int h = newHeight;
				newWidth = 0;
				newHeight = 0;
				resize(w, h);
			}
			working = false;
			isScheduled = false;
		}
	
		private void resize(int width, int height) {
			if (width > 0 && height > 0) {
				if (!dialogResizePending(width, height)) {
					return;
				}
				setSize(width + "px", height + "px");
				DimI actualSize = getDialogSize();
				dialogResizing(actualSize.w(), actualSize.h());
				wrap.onResize();
			}
		}
	}
	
	private class ContentPanel extends SGFlowPanel {
		public ContentPanel() {
			ContentPanel.this.setHeight("100%");
			CSS.position(ContentPanel.this, "relative");
		}
		@Override
		public void onResize() {
			if (isResizable) {
				CSS.bottom(content, buttonPanelHolder.getOffsetHeight());
			}
			super.onResize();
		}
	}

	public SGDialogBox(String title, boolean autoHide, boolean modal, 
		boolean grayBackground, boolean isResizable, boolean hasCloseButton) {
		
		super(autoHide, modal);
//		getElement().getStyle().setZIndex(1000);

		this.grayBackground = grayBackground;
		setResizable(isResizable);

		wW = Window.getClientWidth() - 30;
		wH = Window.getClientHeight() - 30;
		setText(title);
		addStyleName("sgDialogBox");
		this.isResizable = isResizable;

		wrap = new SGFlowPanel();
		wrap.setHeight("100%");
		innerWrap = new ContentPanel();
		content = new SGFlowPanel("sgDialogContent");
		buttonPanel = new FlowPanel();
		buttonPanel.setStyleName(STYLE_BUTTON_PANEL);
		buttonPanel.setVisible(false);

		innerWrap.add(content);
		innerWrap.add(buttonPanelHolder = new SimplePanel(buttonPanel));
		buttonPanelHolder.setStyleName("dialogButtonPanelHolder");
		wrap.add(innerWrap);
		if (hasCloseButton) {
			wrap.add(createCloseButton());
		}

		addCloseHandler(new CloseHandler<PopupPanel>() {
			public void onClose(CloseEvent<PopupPanel> event) {
				hide();
			}
		});

		super.setWidget(wrap);
	}

	public SGDialogBox(String title, boolean autoHide, boolean modal, boolean grayBackground, boolean hasCloseButton) {
		this(title, autoHide, modal, grayBackground, false, hasCloseButton);
	}

	public SGDialogBox(String title, boolean autoHide, boolean modal, boolean hasCloseButton) {
		this(title, autoHide, modal, false, hasCloseButton);
	}

	public SGDialogBox(boolean autoHide, boolean modal, boolean grayBackground, boolean isResizable, boolean hasCloseButton) {
		this(null, autoHide, modal, grayBackground, isResizable, hasCloseButton);
	}

	public SGDialogBox(boolean autoHide, boolean modal, boolean grayBackground, boolean isResizable) {
		this(autoHide, modal, grayBackground, isResizable, true);
	}

	public SGDialogBox(boolean autoHide, boolean modal, boolean grayBackground) {
		this(autoHide, modal, grayBackground, false);
	}

	public SGDialogBox(String title, boolean isResizable, boolean hasCloseButton) {
		this(title, false, true, true, isResizable, hasCloseButton);
	}
	
	public SGDialogBox(String title, boolean isResizable) {
		this(title, false, true, true, isResizable, true);
	}

	public SGDialogBox(String title) {
		this(title, false, true, true, true, true);
	}

	@Override
	public void add(IsWidget child) {
		content.add(child);
	}

	@Override
	public void add(Widget child) {
		content.add(child);
	}
	
	/** @see addValueWidget */
	public static <T extends Widget & HasValue<U>, U> void showValue(String title, final T widget, U initialValue,
		boolean autoHide, boolean modal, boolean grayBackground, boolean isResizable, boolean hasCloseButton, boolean hasClearButton, 
		Integer width, Integer height, OnDialogBoxValueChangedListener<U> callback) {
		
		SGDialogBox dialog = new SGDialogBox(title, autoHide, modal, grayBackground, isResizable, hasCloseButton);
		dialog.addValueWidget(widget, initialValue, hasClearButton, callback);
		if (width != null) dialog.setWidth(width.intValue());
		if (height != null) dialog.setHeight(height.intValue());
		dialog.center();
	}
	
	/** @see addValueWidget */
	public static <T extends Widget & HasValue<U>, U> void showModalValue(String title, final T widget, U initialValue,
		boolean isResizable, boolean hasCloseButton, boolean hasClearButton, 
		Integer width, Integer height, OnDialogBoxValueChangedListener<U> callback) {
		
		showValue(title, widget, initialValue, false, true, true, isResizable, 
			hasCloseButton, hasClearButton, width, height, callback);
	}

	/**
	 * Adds a widget that provides a value, adds Ok and Cancel buttons, and calls "callback" when Ok or Cancel is
	 * pressed. When Ok pressed, the value is returned via callback. When Cancel pressed, null is returned via callback.
	 * If hasClearButton is true, Clear button is added, that sets null value to the widget.
	 */
	public <T extends Widget & HasValue<U>, U> SGDialogBox addValueWidget(final T widget, U initialValue,
		boolean hasClearButton, final OnDialogBoxValueChangedListener<U> callback) {
		widget.setValue(initialValue);
		setContent(widget);

		ButtonBase okButton = ButtonFactory.createOkButton();
		addButton(okButton);
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				callback.onDialogBoxValueChanged(widget.getValue(), false);
				SGDialogBox.this.onDialogClose();
			}
		});

		ButtonBase cancelButton = ButtonFactory.createCancelButton();
		addButton(cancelButton);
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				callback.onDialogBoxValueChanged(null, true);
				SGDialogBox.this.onDialogClose();
			}
		});

		if (hasClearButton) {
			ButtonBase clearButton = ButtonFactory.createClearButton();
			addButton(clearButton);
			clearButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					widget.setValue(null);
				}
			});
		}

		return this;
	}

	public void addButton(Widget w) {
		buttonPanel.setVisible(true);
		buttonPanel.add(w);
	}

	public SGFlowPanel getContent() {
		return content;
	}

	public DimI getDialogSize() {
		int botXafter = elBottomRight.getAbsoluteLeft() + RESIZE_ELEMENT_WIDTH;
		int botYafter = elBottomRight.getAbsoluteTop() + RESIZE_ELEMENT_HEIGHT;
		int actualWidth = botXafter - getAbsoluteLeft();
		int actualHeight = botYafter - getAbsoluteTop();
		return new DimI(actualWidth, actualHeight);
	}

	public FlowPanel getWrap() {
		return wrap;
	}

	@Override
	public final void hide(boolean autoClosed) {
		removeGray();
		super.hide(autoClosed);
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
					break;
				default:
					break;
			}
		}
		super.onBrowserEvent(event);
	}

	public void setButtonPanel(Widget btnPanel) {
		buttonPanelHolder.setWidget(btnPanel);
		btnPanel.addStyleName(STYLE_BUTTON_PANEL);
	}

	public void setHeight(int height) {
		this.maxHeight = height;
		if (maxHeight == 0 && isResizable) {
			maxHeight = 15;
			for (int i = 0; i < content.getWidgetCount(); i++) {
				maxHeight += content.getWidget(i).getOffsetHeight();
			}
			maxHeight += 20; // padding bottom and top;
			maxHeight += getCaption().asWidget().getOffsetHeight(); // caption height
			maxHeight += buttonPanelHolder.getOffsetHeight(); // caption height
		}
		wrap.onResize();
		super.setHeight(Math.min(Window.getClientHeight(), Math.min(maxHeight, wH)) + "px");
	}

	@Override
	public void setHeight(String height) {
		//if we set size in %, we probably want it to fill browser's procentage
		if (height.contains("%")) {
			this.maxHeight = Integer.parseInt(height.split("%")[0]);
			maxHeight = Window.getClientHeight() * maxHeight / 100;
			super.setHeight(maxHeight + "px");
			return;
		}
		setHeight(Integer.parseInt(height.split("px")[0]));
	}

	public void setContent(Widget w) {
		content.clear();
		add(w);
	}

	/**
	 * @param maxWidth max width of the dialog
	 * @param maxHeight max height of the dialog. use 0 to auto adjust on the content. not suitable if working with
	 *            sgTabLayoutpanel or sgHeaderPanel that need certain height
	 */
	public void setOptimalSize(int maxWidth, int maxHeight) {
		setWidth(maxWidth);
		setHeight(maxHeight);
	}

	@Override
	public void setSize(String width, String height) {
		setWidth(width);
		setHeight(height);
	}

	public void setSize(int width, int height) {
		setWidth(width);
		setHeight(height);
	}

	@Override
	public void setWidget(Widget w) {
		setContent(w);
	}

	@Override
	public void setWidth(String width) {
		//if we set size in %, we probably want it to fill browser's procentage
		if (width.contains("%")) {
			this.maxWidth = Integer.parseInt(width.split("%")[0]);
			maxWidth = Window.getClientWidth() * maxWidth / 100;
			super.setWidth(maxWidth + "px");
			return;
		}
		setWidth(Integer.parseInt(width.split("px")[0]));
	}

	public void setWidth(int width) {
		this.maxWidth = width;

		if (maxWidth == 0 && isResizable) {
			maxWidth = 100; //some small width
			for (int i = 0; i < content.getWidgetCount(); i++) {
				maxWidth = Math.max(maxWidth, content.getWidget(i).getOffsetWidth());
			}
		}
		wrap.onResize();
		super.setWidth(Math.min(Window.getClientWidth(), Math.min(maxWidth, wW)) + "px");
	}

	public void setResizable(boolean isResizable) {
		this.isResizable = isResizable;
		if (isResizable) {
			Element td = getCellElement(2, 2);
			DOM.appendChild(td, elBottomRight.getElement());
			adopt(elBottomRight);
			elBottomRight.setStyleName("bottomRight");
			elBottomRight.setTitle(Tooltips.INSTANCE.resizWindow());
			elBottomRight.setSize(RESIZE_ELEMENT_WIDTH + "px", RESIZE_ELEMENT_HEIGHT + "px");
			addStyleName("resizable");
		}
	}
	
	public void onResize() {
		wrap.onResize();
	}

	@Override
	public void show() {
		if (grayBackground) {
			addGray();
		}
		super.show();
		
		if (isResizable) {
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					setSize(maxWidth, maxHeight);
				}
			});
		}
	}

	public static void showCentered(final DialogBox dialogBox) {
		dialogBox.setPopupPositionAndShow(new PositionCallback() {
			public void setPosition(int offsetWidth, int offsetHeight) {
				int top = Math.max(0, Window.getClientHeight() / 2 - offsetHeight / 2);
				int left = Math.max(0, Window.getClientWidth() / 2 - offsetWidth / 2);
				dialogBox.setPopupPosition(left, top);
			}
		});
	}

	@SuppressWarnings("unused")
	protected void dialogResizing(int width, int height) {}

	@SuppressWarnings("unused")
	protected boolean dialogResizePending(int width, int height) {
		return isResizable;
	}

	@Override
	protected void onPreviewNativeEvent(NativePreviewEvent event) {
		// prevent default event outside dialog when resizing
		NativeEvent nativeEvent = event.getNativeEvent();

		if (isResizable) {
			if (!event.isCanceled() && (event.getTypeInt() == ONMOUSEDOWN)
				&& isElementEventTarget(getCellElement(2, 2), nativeEvent)) {
				nativeEvent.preventDefault();
			}
		}
		if (event.getTypeInt() == ONKEYDOWN) {
			event.consume();
		}

		super.onPreviewNativeEvent(event);

		//implementation of closing the dialog on ESCAPE
		if (event.getTypeInt() == ONKEYDOWN && event.getNativeEvent().getKeyCode() == KEY_ESCAPE) {
			onDialogClose();
		}
	}

	/**
	 * Dialog box is without close button by default. This method creates the close button. It should be added somewhere
	 * within the dialog
	 * 
	 * @return
	 */
	protected Anchor createCloseButton() {
		actionCloseDialog = new Anchor();
		actionCloseDialog.setStyleName("actionClose");
		actionCloseDialog.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onDialogClose();
			}
		});
		return actionCloseDialog;
	}
	
	protected void removeCloseButton() {
		if (actionCloseDialog != null) {
			actionCloseDialog.removeFromParent();
			actionCloseDialog = null;
		}
	}

	protected static boolean isElementEventTarget(Element element, NativeEvent event) {
		EventTarget target = event.getEventTarget();
		if (Element.is(target)) {
			return element.getParentElement().isOrHasChild(Element.as(target));
		}
		return false;
	}

	protected void onDialogClose() {
		SGDialogBox.this.hide();
	}
	
	@Override
	public void hide() {
		super.hide();
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				removeGray();
			}
		});
	}

	private void addGray() {
		if (gray != null) {
			return;
		}
		gray = new SimplePanel();
		gray.addDomHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onGrayClicked();
			}
		}, ClickEvent.getType());
		gray.setStyleName("grayBackground");
		RootPanel.get().add(gray);
	}
	
	protected void onGrayClicked() {
	}

	private void onBottomRightEvent(Event event) {
		int posX = event.getClientX();
		int posY = event.getClientY();
		switch (event.getTypeInt()) {
			case ONMOUSEDOWN:
				if (!isResizing) {
					isResizing = true;
					resizeStartX = posX;
					resizeStartY = posY;
					DOM.setCapture(getElement());
					elBottomRight.addStyleName("down");
				}
				break;
			case ONMOUSEUP:
				if (isResizing) {
					isResizing = false;
					elBottomRight.removeStyleName("down");
					DOM.releaseCapture(getElement());
					if (resizeStartX != posX || resizeStartY != posY) {
						int width = posX - getAbsoluteLeft();
						int height = posY - getAbsoluteTop();
						resizeTask.scheduleResize(width, height);
					}
				}
				break;
			case Event.ONMOUSEMOVE:
				if (isResizing) {
					int width = posX - getAbsoluteLeft();
					int height = posY - getAbsoluteTop();
					resizeTask.scheduleResize(width, height);
					elBottomRight.addStyleName("down");
				}
				break;
			default:
				break;
		}
	}

	private void removeGray() {
		if (gray == null) {
			return;
		}
		gray.removeFromParent();
		gray = null;
	}
}
