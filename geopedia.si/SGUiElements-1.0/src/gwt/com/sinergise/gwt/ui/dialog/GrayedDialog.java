/*
 *
 */
package com.sinergise.gwt.ui.dialog;


import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.sinergise.gwt.util.html.CSS;

/**
 * @deprecated  use DialogBox with setGlassEnabled(true)
 */
@Deprecated
public class GrayedDialog extends Dialog
{
    Element gray;
    public GrayedDialog()
    {
        super(true, true, false, false, null);
        addCloseHandler(new CloseHandler<PopupPanel>() {
			public void onClose(CloseEvent<PopupPanel> event) {
	            removeGray();
			}
		});
    }
    
    private void removeGray() {
    	if (gray==null) return;
        UIObject.setVisible(gray, false);
        if (DOM.getParent(gray)!=null) {
        	DOM.removeChild(RootPanel.get().getElement(), gray);
            gray=null;
        }
    }
    private void addGray() {
        if (gray!=null) return;
        gray=DOM.createDiv();
        CSS.position(gray, CSS.POS_ABSOLUTE);
        CSS.size(gray, "100%", "100%");
        CSS.leftTop(gray, 0, 0);
        CSS.background(gray, "#808080");
        CSS.opacity(gray, 75);
        DOM.appendChild(RootPanel.get().getElement(), gray);
    }
    
    @Override
	public void show() {
        addGray();
        super.show();
    }
    
    @Override
	protected void onHide() {
        removeGray();
        super.onHide();
    }
}
