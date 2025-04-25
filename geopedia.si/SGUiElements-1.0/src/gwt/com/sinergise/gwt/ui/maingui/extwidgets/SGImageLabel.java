package com.sinergise.gwt.ui.maingui.extwidgets;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.gwt.ui.StyleConsts;

/**
 * @author tcerovski
 *
 */
public class SGImageLabel extends Composite implements HasText {

	public enum ImagePosition {LEFT, RIGHT} 
	
	private ImagePosition pos;
	
	protected FlowPanel outer;
	private Label label;
	private SimplePanel imagePanel;
	
	public SGImageLabel(String text, Image image) {
		this(text, image, ImagePosition.LEFT);
	}
	
	public SGImageLabel(String text, Image image, ImagePosition pos, boolean wordWrap) {
		this(text, image, pos);
		setWordWrap(wordWrap);
	}

	public SGImageLabel(String text, Image image, ImagePosition pos) {
		this.pos = pos;
		
		outer = new FlowPanel();
		label = new Label();
		imagePanel = new SimplePanel();
		imagePanel.setStyleName("title-image");
		label.setStyleName("title-label");
		
		if(pos == ImagePosition.LEFT) {
			outer.add(imagePanel);
			outer.add(label);
		} else {
			outer.add(label);
			outer.add(imagePanel);
		}
		
		setImage(image);
		setText(text);
		
		initWidget(outer);
		setStyleName(StyleConsts.SG_IMAGE_LABEL+" clearfix");
	}
	
	public void setImage(Image image) {
		imagePanel.setWidget(image);
	}
	
	@Override
	public void setText(String text) {
		if(text == null)
			text = "";
		
		label.setText(text);
	}
	
	@Override
	public String getText() {
		return label.getText();
	}
	
	public void setImagePosition(ImagePosition newPos) {
		if(newPos == pos)
			return;
		
		pos = newPos;
		Widget temp = outer.getWidget(1);
		outer.remove(1);
		outer.insert(temp, 0);
	}
	
	public void setWordWrap(boolean wrap) { 
		label.setWordWrap(wrap);
	}
}
