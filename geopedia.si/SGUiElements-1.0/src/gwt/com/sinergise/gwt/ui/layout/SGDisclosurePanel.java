package com.sinergise.gwt.ui.layout;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.controls.CanEnsureChildVisibility;
import com.sinergise.gwt.ui.ImageAnchor;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.resources.Theme;
import com.sinergise.gwt.util.UtilGWT;

public class SGDisclosurePanel extends SGFlowPanel implements CanEnsureChildVisibility {

    private SGFlowPanel content;
    private FocusPanel headerWrap;
    private SGFlowPanel headerPanel;
    
    private ImageAnchor toggleAnchor;

    private ImageResource openImg;
    private ImageResource closedImg;
    
    public SGDisclosurePanel(boolean expanded) {
    	this(expanded, Theme.getTheme().layoutBundle().closePanel(), Theme.getTheme().layoutBundle().openPanel());
    }
    
    public SGDisclosurePanel(String headerTxt) {
		this(false, Theme.getTheme().layoutBundle().closePanel(), Theme.getTheme().layoutBundle().openPanel());
		setHeaderText(headerTxt);
	}

	public SGDisclosurePanel(String headerTxt, Widget contentWidget) {
		this(false, Theme.getTheme().layoutBundle().closePanel(), Theme.getTheme().layoutBundle().openPanel());
		setHeaderText(headerTxt);
		setContent(contentWidget);
	}

	public SGDisclosurePanel(boolean isExpanded, final ImageResource openImg, final ImageResource closeImg) {
    	this.openImg = openImg;
    	this.closedImg = closeImg;

    	addStyleName("sgTogglePanel");
    	
    	toggleAnchor = new ImageAnchor();
    	toggleAnchor.setStyleName("sgToggleAnchor");
    	
    	headerPanel = new SGFlowPanel("sgTogglePanelHeader");
    	headerPanel.add(toggleAnchor);

    	headerWrap = new FocusPanel(headerPanel);
    	add(headerWrap);
    	
    	content = new SGFlowPanel("sgTogglePanelContent");
        add(content);
    	
        toggleAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	internalToggle();
            }
        });
        
        setOpen(isExpanded);
    }
	
	public SGDisclosurePanel( final ImageResource openImg, final ImageResource closeImg) {
		this(false, openImg, closeImg);
	}

	public void addHeaderWidget(Widget w) {
    	headerPanel.insert(w, 0);
    }
    
    public SGFlowPanel getContent() {
    	return content;
    }
    public SGFlowPanel getHeader() {
    	return headerPanel;
    }
    
    public void internalToggle() {
    	setOpen(!isOpen());
		onResize();
	}

	public boolean isOpen() {
    	return content.isVisible();
	}
    
    public void setContent(Widget child) {
    	content.clear();
    	content.add(child);
    	onResize();
    }
    
    public void setContentHeight(int height) {
    	content.setHeight(height + "px");
    }
    
    public void setHeaderText(final String title) {
		toggleAnchor.setText(title);
    }
    
    public void setOpen(boolean isOpen) {
    	content.setVisible(isOpen);
    	updateImg();
    }
    
    public void setToggleImage(ImageResource openImg, ImageResource closeImg) {
    	this.openImg = openImg;
    	this.closedImg = closeImg;
    	updateImg();
      }
    
    private void updateImg() {
		toggleAnchor.setImageRes(isOpen() ? openImg : closedImg);
	}
    
    @Override
	public void ensureChildVisible(Object child) {
		if (UtilGWT.isOrHasDescendant(this, (Widget)child)) {
			setOpen(true);
		}
	}
	
	@Override
	public boolean isChildVisible(Object child) {
		return isOpen() 
			&& UtilGWT.isOrHasDescendant(this, (Widget)child);
	}
    
} 
