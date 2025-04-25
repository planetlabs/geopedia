package com.sinergise.gwt.ui.maingui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;


public class DecoratedAnchor extends Anchor {
//	private static ButtonResources RESOURCES = Theme.getTheme().buttonBundle();
//	private static ButtonCss STYLE = RESOURCES.buttonStyle();
//	static {
//		STYLE.ensureInjected();
//	}
	
	private InlineHTML textSpan;
	private InlineHTML rSide;
	private InlineHTML lSide;
	private Image pic;
	private InlineHTML spanPic = null;
	private String currentSpanIconStyle = null;

	public DecoratedAnchor() {
//		super(Document.get().createAnchorElement());
		
		//apparently setting href disables button to force click if dragging it. this was set to always show pointer cursor without CSS
//		setHref("javascript:;");
		setStyleName("btn");
		lSide = new InlineHTML();
		lSide.setStyleName("lSide");
		getElement().appendChild(lSide.getElement());
		
//		setStyleName(STYLE.btn());
		textSpan = new InlineHTML();
		textSpan.setStyleName("txt");
//		textSpan.setStyleName(STYLE.txt());
		getElement().appendChild(textSpan.getElement());
		
		rSide = new InlineHTML();
		rSide.setStyleName("rSide");
		getElement().appendChild(rSide.getElement());
		
		//BUG: if overriding mouse handler, there are problems with dialog boxes, that prevent certain events
		/*addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				addStyleName("down");
			}
		});
		addMouseUpHandler(new MouseUpHandler() {
			@Override
			public void onMouseUp(MouseUpEvent event) {
				removeStyleName("down");
			}
		});
		addMouseMoveHandler(new MouseMoveHandler() {
			@Override
			public void onMouseMove(MouseMoveEvent event) {
				removeStyleName("down");
			}
		});*/
	}

	public DecoratedAnchor(String text) {
		this();
		setText(text);
	}
	public DecoratedAnchor(String text, ImageResource imgRes) {
		this(text);
		setImage(imgRes);
	}
	
	public DecoratedAnchor(String text, ClickHandler handler) {
	    this(text);
	    addClickHandler(handler);
	}
	
	public DecoratedAnchor(String text, ImageResource imgRes, ClickHandler handler) {
	    this(text, imgRes);
	    addClickHandler(handler);
	}

	public DecoratedAnchor setIcon(String iconUrl) {
//		addStyleName(STYLE.icon());
		addStyleName("icon");
		if (pic == null) {
			pic = new Image();
			getElement().insertFirst(pic.getElement());
		}
		pic.setUrl(GWT.getModuleBaseURL() + iconUrl);
		return this;
	}

	public DecoratedAnchor setImage(ImageResource iconImage) {
		addStyleName("icon");
		if (pic == null) {
			pic = new Image(iconImage);
			getElement().insertFirst(pic.getElement());
		} else {
			pic.setResource(iconImage);
		}
		return this;
	}

	
	public DecoratedAnchor setIconRel(String iconUrl) {
		addStyleName("icon");
		if (pic == null) {
			pic = new Image();
			getElement().insertFirst(pic.getElement());
		}
		pic.setUrl(iconUrl);
		return this;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		if (!enabled) {
			addStyleName("disabled");
		} else {
			removeStyleName("disabled");
		}
	}
	
	@Override
	public boolean isEnabled() {
		String st = getStyleName();
		if (st == null || st.length()==0) {
			return true;
		}
		String[] nms = st.split(" ");
		for (String nm : nms) {
			if ("disabled".equals(nm)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void onBrowserEvent(Event event) {
		switch (DOM.eventGetType(event)) {
			case Event.ONDBLCLICK:
			case Event.ONFOCUS:
			case Event.ONCLICK:
				if (!isEnabled()) {
					return;
				}
				break;
		}
		super.onBrowserEvent(event);
	}

	
	public DecoratedAnchor setSpanIcon(String style) {
		if (currentSpanIconStyle!=null) {
			removeStyleName(currentSpanIconStyle);
		}
		currentSpanIconStyle = "icon "+style;
		addStyleName(currentSpanIconStyle);
		if (spanPic==null) {
			spanPic = new InlineHTML();
//			spanPic.setStyleName(STYLE.spanImg());
			spanPic.setStyleName("spanImg");
			getElement().insertFirst(spanPic.getElement());
		} 		
		return this;
	}

	@Override
	public String getText() {
		return textSpan.getText();
	}

	@Override
	public void setText(String text) {
		textSpan.setText(text);
	}

	@Override
	public String getHTML() {
		return textSpan.getHTML();
	}

	@Override
	public void setHTML(String html) {
		textSpan.setHTML(html);
	}

	public void setRightImage() {
		addStyleName("rightImage");
	}

}