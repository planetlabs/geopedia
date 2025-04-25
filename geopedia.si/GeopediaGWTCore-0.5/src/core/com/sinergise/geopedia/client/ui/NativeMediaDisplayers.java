package com.sinergise.geopedia.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.geopedia.client.resources.GeopediaCommonStyle;
import com.sinergise.geopedia.client.ui.base.CenteredWidget;
import com.sinergise.geopedia.client.ui.widgets.PanImageViewer;
import com.sinergise.gwt.ui.dialog.AbstractDialogBox;
import com.sinergise.gwt.ui.dialog.GrayedDialog;
import com.sinergise.gwt.ui.maingui.StandardUIConstants;


public class NativeMediaDisplayers
{
    public static boolean inited=false;
	public static final native void init() /*-{
        @com.sinergise.geopedia.client.ui.NativeMediaDisplayers::inited=false;
		$wnd.gp_showVideoFrame = function (s, w, h) {
			@com.sinergise.geopedia.client.ui.NativeMediaDisplayers::showVideoFrame(Ljava/lang/String;II)(s, w, h);
		};
        $wnd.gp_showPanoramicFrame = function (s, w, h) {
            @com.sinergise.geopedia.client.ui.NativeMediaDisplayers::showPanoramicFrame(Ljava/lang/String;II)(s, w, h);
        };
        
        $wnd.gp_showClosableHTMLWidget = 
		    @com.sinergise.geopedia.client.ui.NativeMediaDisplayers::showClosableHTMLWidget(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;);
	}-*/;
	
	public static final void showVideoFrame(String content, int w, int h) 
	{
		showVideoFrame(new HTML(content), w, h);
	}
	
	public static final void showVideoFrame(Widget content, int w, int h)
	{
        if (!inited) init();
        CenteredWidget diag=new CenteredWidget(true, false);
		diag.getContent().setSize(w+"px", h+"px");
	}
    
    public static final void showPanoramicFrame(String src ,int w, int h)
    {
        if (!inited) init();
        PanoramicImageDialog pid = new PanoramicImageDialog(src,w,h);
        pid.center();
    }
    
    public static void showClosableHTMLWidget(String html, String width, String height) {
    	HTMLDisplayerDialog dlg = new HTMLDisplayerDialog();
    	dlg.setHTMLContent(html);
    	dlg.setWidth(width);
    	dlg.setHeight(height);
    	dlg.center();
    }
    
    private static class  HTMLDisplayerDialog extends AbstractDialogBox {
    	private FlowPanel contentPanel;
    	
		public HTMLDisplayerDialog() {
			super(false, true, true);
			addStyleName ("htmlDialog");
			contentPanel = new FlowPanel();
    		contentPanel.setStyleName("contentPanel");
    		contentPanel.add(createCloseButton());    		
    		setWidget(contentPanel);
		}
		
		public void setHTMLContent(String html) {
			 HTML wgt=new HTML(html);
			 wgt.setStyleName("htmlContent");
			 contentPanel.add(wgt);			 
		}
		
		
    	
    }
    
    private static class PanoramicImageDialog extends AbstractDialogBox {
    	private FlowPanel contentPanel;
    	PanoramicImageDialog(String src, int w, int h) {
    		super (false,true, true);
    		addStyleName("panoramicImageDialog");

    		contentPanel = new FlowPanel();
    		contentPanel.setStyleName("contentPanel");
    		String base = GWT.getModuleBaseURL();
    		PanImageViewer viewer=new PanImageViewer(src, GeopediaCommonStyle.INSTANCE.navLeft(), GeopediaCommonStyle.INSTANCE.navRight(), 
    			GeopediaCommonStyle.INSTANCE.navPause(), GeopediaCommonStyle.INSTANCE.navPlus(), GeopediaCommonStyle.INSTANCE.navMinus());
            viewer.setPixelSize(w, h);
            contentPanel.add(viewer);
            Anchor actionCloseDialog = new Anchor();
    		actionCloseDialog.setStyleName("actionClose");
    		actionCloseDialog.setTitle(StandardUIConstants.STANDARD_CONSTANTS.buttonClose());
    		actionCloseDialog.addClickHandler(new ClickHandler() {
    			
    			@Override
    			public void onClick(ClickEvent event) {
    				PanoramicImageDialog.this.hide();
    			}
    		});
    		contentPanel.add(actionCloseDialog);

            setWidget(contentPanel);
            
            
            
    	}
    }
    
}
