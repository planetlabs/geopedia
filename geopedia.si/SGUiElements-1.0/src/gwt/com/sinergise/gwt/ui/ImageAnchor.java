package com.sinergise.gwt.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.sinergise.common.util.web.AnchorData;
import com.sinergise.gwt.ui.maingui.extwidgets.SGAnchor;

public class ImageAnchor extends SGAnchor {
	private Image anchorImage = null;
	private InlineHTML txtLabel = null;
	
	public ImageAnchor() {
		this(new Image());
	}
	
	public ImageAnchor(AnchorData data, ImageResource imgRes) {
		this(data.getLabel(), imgRes, data.getHref(), data.getTarget());
	}
	
	public ImageAnchor(String imageUrl) {
		this(new Image());
		setAnchorImageUrl(imageUrl);
	}
	public ImageAnchor(ImageResource imgRes) {
		this(new Image());
		setImageRes(imgRes);
	}
	public ImageAnchor(String text, ImageResource imgRes) {
		this(new Image());
		setText(text);
		setImageRes(imgRes);
	}
	public ImageAnchor(String text, ImageResource imgRes, String url) {
		this(text, imgRes);
		setHref(url);
	}
	
	public ImageAnchor(ImageResource imgRes, String url, String target) {
		this(imgRes);
		setHref(url);
		setTarget(target);
	}
	public ImageAnchor(String text, ImageResource imgRes, String url, String target) {
		this(imgRes, url, target);
		setText(text);
	}
	
	public ImageAnchor(ImageResource imgRes, ClickHandler handler) {
		this(new Image(imgRes));
		addClickHandler(handler);
	}
	
	public ImageAnchor(String text, ImageResource imgRes, ClickHandler handler) {
		this(text, imgRes);
		addClickHandler(handler);
	}
	
	public ImageAnchor(Image image){
		setStyleName("imageAnchor");
		setImage(image);
	}	
	
	private void setImage(Image image) {
		this.anchorImage = image;
		this.getElement().appendChild(anchorImage.getElement());
	}
	
	@Override
	public void setText(String text) {
		if (txtLabel==null) {
			txtLabel = new InlineHTML(text);
			this.getElement().appendChild(txtLabel.getElement());
		} else {
			txtLabel.setText(text);
		}
	}
	
	public Image getAnchorImage() {
		return anchorImage;
	}
	
	public void setAnchorImageUrl(String imageUrl) {
		anchorImage.setUrl(GWT.getModuleName()+"/"+imageUrl);
	}

	public void setAnchorImageAbsoluteUrl(String imageUrl) {
		anchorImage.setUrl(imageUrl);
	}
	
	public void setImageRes(ImageResource imageRes) {
		if (anchorImage == null) {
			setImage(new Image());
		}
		anchorImage.setResource(imageRes);
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
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (anchorImage!=null)
			anchorImage.setVisible(visible);
		if (txtLabel!=null)
			txtLabel.setVisible(visible);
	}
}