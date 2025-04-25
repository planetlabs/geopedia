package com.sinergise.gwt.ui.maingui.extwidgets;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.sinergise.gwt.ui.StyleConsts;
import com.sinergise.gwt.ui.maingui.extwidgets.SGImageLabel.ImagePosition;

/**
 * @author tcerovski
 *
 */
public class SGTitledPanel extends Composite {
	
	private static final String STYLE_NO_IMG = "noImg";
	
	private FlowPanel outer;
	private SimplePanel inner;
	private TitleWidget titleLabel;

	public SGTitledPanel (String title) {
		this(title, null, Integer.MIN_VALUE);
	}
	
	public SGTitledPanel (String title, int height) {
		this(title, null, height);
	}
	
	public SGTitledPanel (String title, Image icon) {
		this(title, icon, Integer.MIN_VALUE);
	}
	
	public SGTitledPanel (String title, Image icon, int height) {
		
		titleLabel = new TitleWidget(title, icon);
		if(height > Integer.MIN_VALUE)
			titleLabel.setHeight(height+"px");
		
		inner = new SimplePanel();
		outer = new FlowPanel();
		outer.add(titleLabel);
		outer.add(inner);
		
		initWidget(outer);
		setStyleName(StyleConsts.SG_TITLED_PANEL);
		
		Element mainElem = DOM.getChild(outer.getElement(), 0);
		UIObject.setStyleName(mainElem, "title", true);
		
		if (icon == null) {
			addStyleName(STYLE_NO_IMG);
		}
	}
	
	public void setPanelTitle(String title) {
		titleLabel.setText(title);
	}
	
	@Override
	@Deprecated /** Use setPanelTitle to set panel title */
	public void setTitle(String title) {
		super.setTitle(title);
	}
	
	public void setIcon(Image icon) {
		titleLabel.setImage(icon);
		if (icon == null) {
			addStyleDependentName(STYLE_NO_IMG);
		} else {
			removeStyleDependentName(STYLE_NO_IMG);
		}
	}
	
	public void setIconPosition(ImagePosition pos) {
		titleLabel.setImagePosition(pos);
	}
	
	public void addTitleWidget(Widget w, HorizontalAlignmentConstant align) {
		titleLabel.insertWidget(w, align);
	}
	
	@Override
	public void setWidget(Widget w) {
		if (w != null) {
			inner.setWidget(w);
			inner.setStyleName("body");
		}
	}
	
	public void addTitleStyleName(String style) {
		titleLabel.addStyleName(style);
	}
	
	private static class TitleWidget extends SGImageLabel {
		public TitleWidget(String text, Image image) {
			super(text, image);
		}
		
		void insertWidget(Widget w, HorizontalAlignmentConstant align) {
			w.addStyleName(align.getTextAlignString());
			outer.add(w);
		}
	}
	
	public SimplePanel getContent() {
		return inner;
	}
	
}
