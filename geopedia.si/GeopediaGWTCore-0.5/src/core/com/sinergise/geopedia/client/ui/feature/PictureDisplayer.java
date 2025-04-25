package com.sinergise.geopedia.client.ui.feature;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.sinergise.geopedia.client.ui.base.CenteredWidget;

public class PictureDisplayer extends FlowPanel { 
		public interface PictureProvider {
			String getPictureUrl(int w, int h);
		}
		
	private Image image = null;	
	private Anchor anchor;
	private PictureProvider pictureProvider = null;
	private int thumbnailW, thumbnailH;
	public PictureDisplayer() {
		this(200,200);
	}
	public PictureDisplayer(int thumbnailW, int thumbnailH) {
		this.thumbnailH=thumbnailH;
		this.thumbnailW=thumbnailW;
		setStyleName("blobDisplay");
	}
	
	public void setPictureProvider(PictureProvider provider) {
		this.pictureProvider=provider;
		if (provider == null) {
			if (image !=null) {
				image.getElement().removeFromParent();
				anchor.removeFromParent();
				image=null;
				anchor=null;
			}
		} else {
			if (image== null) {
				image = new Image();
				anchor = new Anchor();
				anchor.getElement().appendChild(image.getElement());
				anchor.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						showPictureFullscreen();
					}
				});
				add(anchor);
			}
			image.setUrl(pictureProvider.getPictureUrl(thumbnailW, thumbnailH));
		}
	}

	private void showPictureFullscreen() {
		if (pictureProvider!=null) {
			new PreviewDialog().show();
		}
	}
	
	
	private class PreviewDialog extends CenteredWidget {
		
		public PreviewDialog() {
			super(true, true);
		    int w = Window.getClientWidth()-45;
		    int h = Window.getClientHeight()-65;
			Image img = new Image(pictureProvider.getPictureUrl(w, h));
			img.setStyleName("blobDisplay");
    		setContent(img);
		}
	}

}
