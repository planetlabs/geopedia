package com.sinergise.gwt.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DisclosurePanelImages;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

@SuppressWarnings("deprecation")
public class DefaultDisclosureHeader extends FlowPanel implements CloseHandler<DisclosurePanel>, OpenHandler<DisclosurePanel> {
	// TODO: DisclosurePanel's DEFAULT_IMAGES is protected, use DisclosurePanelImages for now 
    DisclosurePanelImages  images         = (DisclosurePanelImages) GWT.create(DisclosurePanelImages.class);
    AbstractImagePrototype openedImage    = images.disclosurePanelOpen();
    AbstractImagePrototype closedImage    = images.disclosurePanelClosed();
	Image                  imageWidget    = openedImage.createImage();

	public void setOpenedImage(ImageResource openedImage) {
		this.openedImage = AbstractImagePrototype.create(openedImage);
		setStyle();
	}

	public void setClosedImage(ImageResource closedImage) {
		this.closedImage = AbstractImagePrototype.create(closedImage);
		setStyle();
	}

	public DefaultDisclosureHeader() {
	    // set image TD to be same width as image.
		imageWidget.setStyleName("groupToggler");
		add(imageWidget);
	}
	
    public DefaultDisclosureHeader(Widget displayWidget) {
    	this();
		add(displayWidget);
	}
    
    public void setEnabled(boolean en) {
    	imageWidget.setVisible(en);
    }
    
    private HandlerRegistration closeHandler;
    private HandlerRegistration openHandler;
    
    @Override
	protected void onAttach() {
    	super.onAttach();
    	DisclosurePanel dp=getDisclosurePanelParent();
	    if (dp!=null) {
	    	closeHandler = dp.addCloseHandler(this);
	    	openHandler  = dp.addOpenHandler (this);
	    }
	    setStyle();
    }
    
    @Override
	protected void onDetach() {
    	DisclosurePanel dp=getDisclosurePanelParent();
	    if (dp!=null) {
	    	closeHandler.removeHandler();
	    	openHandler.removeHandler();
	    }
    	super.onDetach();
    }
    
    protected DisclosurePanel getDisclosurePanelParent() {
    	Widget parent=getParent();
    	while (parent!=null) {
    		if (parent instanceof DisclosurePanel) return (DisclosurePanel)parent;
    		parent=parent.getParent();
    	}
    	return null;
    }

    public void onClose(CloseEvent<DisclosurePanel> event) {
      setStyle();
    }

    public void onOpen(OpenEvent<DisclosurePanel> event) {
      setStyle();
    }

    public boolean isOpen() {
    	return getDisclosurePanelParent().isOpen();
    }
    
    private void setStyle() {
    	if (isOpen()) {
   			openedImage.applyTo(imageWidget);
    	} else {
   			closedImage.applyTo(imageWidget);
    	}
    }
}
