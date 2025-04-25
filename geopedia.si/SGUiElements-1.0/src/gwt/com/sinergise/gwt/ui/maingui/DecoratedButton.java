package com.sinergise.gwt.ui.maingui;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.PushButton;
import com.sinergise.gwt.ui.StyleConsts;
import com.sinergise.gwt.ui.maingui.extwidgets.SGDecoratorPanel;


public class DecoratedButton extends PushButton {
	
	public static final String DECORATOR_SIMPLE = SGDecoratorPanel.DEFAULT_STYLENAME+"-simple";
	
    public static final String TABLE_STYLENAME = StyleConsts.DECORATED_BUTTON;
    public static final String ICON_BUT_STYLENAME = TABLE_STYLENAME+"-withIcon";
	public static final String[] DEFAULT_ROW_STYLES = new String[]{"butTop","butBottom"}; 
    
	protected SGDecoratorPanel decorator;
	
	public DecoratedButton(String text, boolean asHTML, ClickHandler handler) {
		this(text, asHTML);
		addClickHandler(handler);
	}
	
	public DecoratedButton() {
		this(null, false);
	}
	
	public DecoratedButton(String text, boolean asHTML) {
		this(text, asHTML, DEFAULT_ROW_STYLES, 0);
	}
	
	public DecoratedButton(String text, boolean asHTML, String[] rowStyles, int contentRow) {
		decorator = new SGDecoratorPanel(rowStyles, SGDecoratorPanel.DEFAULT_COL_STYLESUFFIXES, contentRow, true);
		if (asHTML) {
		    setHTML(text);
		} else {
		    setText(text);
		}
		getUpFace().setHTML(decorator.getTableHTML());
		addStyleName(StyleConsts.DECORATED_BUTTON);
	}

	@Override
	public void setText(String text) {
	    decorator.setContentText(text);
	    getUpFace().setHTML(decorator.getTableHTML());
	}
	
    @Override
	public void setHTML(String html) {
	    decorator.setContentHTML(html);
        getUpFace().setHTML(decorator.getTableHTML());
	}
    
    public DecoratedButton setIconStyle(String iconStyle) {
    	addStyleName(ICON_BUT_STYLENAME);
    	addStyleName(iconStyle);
    	return this;
    }
    
    public void isSmall(boolean small) {
    	if(small) {
    		addStyleDependentName("small");
    	} else {
    		removeStyleDependentName("small");
    	}
    }
    
    public void setDecoratorStyle(String style) {
    	decorator.setStyleName(style);
    	getUpFace().setHTML(decorator.getTableHTML());
    }

    public static String createTableHTML(String contentHTML) {
        SGDecoratorPanel sdp = new SGDecoratorPanel(DEFAULT_ROW_STYLES, SGDecoratorPanel.DEFAULT_COL_STYLESUFFIXES, 0, true);
        sdp.setContentHTML(contentHTML);
        return sdp.getTableHTML();
    }    
    
}