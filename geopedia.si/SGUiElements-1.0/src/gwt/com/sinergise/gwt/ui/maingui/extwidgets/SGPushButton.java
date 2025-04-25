package com.sinergise.gwt.ui.maingui.extwidgets;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;

public class SGPushButton extends PushButton {
	
	private Image image = new Image();
	private Element lSide = DOM.createSpan();
	private Element rSide = DOM.createSpan();
	
	public enum ButtonColor {GREEN, RED, YELLOW, ORANGE, CYAN, MAGENTA}
	
	public SGPushButton() {
		lSide.setClassName("lSide");
		rSide.setClassName("rSide");
		getElement().insertFirst(lSide);
		getElement().appendChild(rSide);
		setStyleName("sgButton");
	}
	
	public SGPushButton(String text) {
		this();
		setText(text);
	}
	
	public SGPushButton(ImageResource imgRes) {
		this();
		addStyleName("iconOnly");
		setImage(imgRes);
	}
	public SGPushButton(String text,ImageResource imgRes) {
		this(text);
		setImage(imgRes);
	}
	
	public SGPushButton(String text,String imgURL) {
		this(text);
		setImageURL(imgURL);
	}
	
	public SGPushButton(String text, ClickHandler handler) {
		this(text);
		addClickHandler(handler);
	}
	public SGPushButton(ImageResource imgRes, ClickHandler handler) {
		this(imgRes);
		addClickHandler(handler);
	}
	
	public SGPushButton(String text,ImageResource imgRes, ClickHandler handler) {
		this(text, imgRes);
		addClickHandler(handler);
	}

	public void setImage(ImageResource imgRes) {
		image.setResource(imgRes);
		if(image!=null) {
			getElement().insertAfter(image.getElement(),rSide);
		}
	}
	
	public void setImageURL(String imgURL) {
		image.setUrl(imgURL);
		if(image!=null) {
			getElement().insertAfter(image.getElement(),rSide);
		}
	}

	public void setRightImage() {
		image.addStyleName("fl-right");
	}
	/**
	 * 
	 * @param String as "green", "yellow", "orange", "red"
	 */
    public void setButtonColor(ButtonColor color) {
    	addStyleName("customBtn");
		addStyleName(color.name().toLowerCase());
    }
}