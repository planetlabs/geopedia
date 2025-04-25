package com.sinergise.gwt.ui.controls;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sinergise.gwt.ui.StyleConsts;

public class ImageButton extends Button {
    
	private static final String BUTTON_STYLE_PRIMARY_NAME = StyleConsts.IMAGE_BUTTON;
	
	final String htmlEnableImage;
	final String htmlDisableImage;
	String labelString;

	public ImageButton(final String label, final String imgUrl, final String disImgUrl) {
		super(label);
		addStyleName(BUTTON_STYLE_PRIMARY_NAME);
		String stil = getStyle(label);
		htmlEnableImage = getImgHtml(imgUrl, stil);
		htmlDisableImage = getImgHtml(disImgUrl, stil);
		labelString = label;
		setText(labelString);
	}
	
	public ImageButton(final String label, final AbstractImagePrototype img) {
		this(label, img, img);
	}
	public ImageButton(final String label, final ImageResource img) {
		this(label, img, img);
	}

	private static String getStyle(final String label) {
		// TODO set in external css
		String stil = "{ margin-right: 2px;	vertical-align:middle;}";

		if (label == null || label.length() < 1) {
			stil = "margin: 0; padding: 0;";
		}
		return stil;
	}
	
	public ImageButton(final String label, final AbstractImagePrototype img, final AbstractImagePrototype disImg) {
		super(label);
		addStyleName(BUTTON_STYLE_PRIMARY_NAME);
		
		htmlEnableImage = img.getHTML();
		htmlDisableImage = disImg.getHTML();
		setText(label);
	}
	
	public ImageButton(final String label, final ImageResource img, final ImageResource disImg) {
		super(label);
		addStyleName(BUTTON_STYLE_PRIMARY_NAME);
		
		htmlEnableImage = getImgHtml(img);
		htmlDisableImage = getImgHtml(disImg);
		setText(label);
	}

	private static String getImgHtml(final ImageResource imgRes) {
	    SimplePanel sp = new SimplePanel();
	    sp.add(new Image(imgRes));
	    return sp.getElement().getInnerHTML();
	}

	
	private static String getImgHtml(final String imgUrl, String stil) {
	    return "<img border=\"0\" src=\""+imgUrl+"\" style=\""+stil+"\" />";

	}
	

	public ImageButton(final String label, final String imgUrl) {
		this(label, imgUrl, imgUrl);
	}

	@Override
	public void setText(String text) {
		if (text == null) {
			text = "";
		}
		
		labelString = text;
		text = (text.length() > 0 
				? "&nbsp;" 
				: "") + text;
		if (isEnabled()) {
			setHTML(htmlEnableImage + text);
		} else {
			setHTML(htmlDisableImage + text);
		}

	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		setText(labelString);
		if (enabled) {
			removeStyleDependentName("disabled");
		} else {
			addStyleDependentName("disabled");
		}
	}

}
