package com.sinergise.geopedia.client.ui.base;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.util.UtilGWT;

public class CenteredWidget extends FocusPanel {
	protected SimplePanel gray;
	private boolean grayBackground = false;
	private boolean shown = false;
	protected SGFlowPanel innerContent;
	private ClickHandler close;
	private boolean showing;

	public CenteredWidget(boolean grayBackground) {
		this.grayBackground = grayBackground;
		setStyleName("centeredWidget");
		innerContent = new SGFlowPanel();
		innerContent.setStyleName("innerContent");
		close = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		};

		if (UtilGWT.isIE8()) {
			createDiv("tl");
			createDiv("tm");
			createDiv("tr");
			createDiv("lm");
			createDiv("rm");
			createDiv("bl");
			createDiv("bm");
			createDiv("br");
		}
	}

	private void createDiv(String className) {
		SimplePanel div = new SimplePanel();
		div.setStyleName("borders " + className);
		innerContent.add(div);
	}

	public CenteredWidget(boolean grayBackground, boolean autoHide) {
		this(grayBackground);
		isCloseable(autoHide);
	}

	public void setContent(Widget w) {
		int h = Window.getClientHeight();
		SimplePanel outerContent = new SimplePanel(innerContent);
		outerContent.setStyleName("outerContent");
		innerContent.add(w);
		w.addStyleName("centeredContent");
		w.getElement().setAttribute("style", "max-height: " + (0.9*h-100) +"px");
		setWidget(new SimplePanel(outerContent));
	}
	
	//function that is used when centeredWidget has SGTabLayoutPanel inside as main container
	public void setContent(Widget w, int heightProcent) {
		int h = Window.getClientHeight();
		SimplePanel outerContent = new SimplePanel(innerContent);
		outerContent.setStyleName("outerContent");
		innerContent.add(w);
		w.addStyleName("centeredContent");
		w.setHeight((0.9*h-100) +"px");
		setWidget(new SimplePanel(outerContent));
	}
	
	public SGFlowPanel getContent() {
		return innerContent;
	}

	public void show() {
		if (shown)
			return;
		shown = true;
		if (grayBackground)
			addGray();
		RootPanel.get().add(this);
	}

	protected void isCloseable(boolean closeable) {
		if (closeable) {
			addClickHandler(close);
		} else {
			Anchor closeAnchor = new Anchor();
			closeAnchor.setStyleName("actionClose");
			closeAnchor.addClickHandler(close);
			innerContent.add(closeAnchor);
		}
	}
	protected void autoClose() {
		addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if(event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
					hide();
				}
			}
		});
	}
	
	public void addInnerContent(Widget w) {
		innerContent.add(w);
	}
	public void removeInnerContent(Widget w) {
		innerContent.remove(w);
	}
	
	public void hide() {
		if (!shown)
			return;
		shown = false;
		if (grayBackground)
			removeGray();
		RootPanel.get().remove(this);
	}

	private void removeGray() {
		if (gray == null)
			return;
		gray.setVisible(false);
		if (gray != null) {
			gray.removeFromParent();
			gray = null;
		}
	}

	private void addGray() {
		if (gray != null)
			return;
		gray = new SimplePanel();
		gray.setStyleName("grayBackground");
		RootPanel.get().add(gray);
	}

	public boolean isShowing() {
		return showing;
	}
}
