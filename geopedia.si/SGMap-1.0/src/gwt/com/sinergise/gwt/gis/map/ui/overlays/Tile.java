/*
 *
 */
package com.sinergise.gwt.gis.map.ui.overlays;


import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.sinergise.gwt.ui.ImageUtilGWT;
import com.sinergise.gwt.ui.core.MouseHandler;
import com.sinergise.gwt.util.html.CSS;
import com.sinergise.gwt.util.html.ExtDOM;


public class Tile
{
	public class TileCallback implements ImageUtilGWT.ImageFetchCallback
	{
		private Element fetchEl;
		private Element imageElement;
		private boolean cancelled = false;
		private boolean transparent;
		boolean error = false;

		public TileCallback(Element imgEl)
		{
			this.imageElement = imgEl;
		}

		public void doFetch(String src, boolean isTransparent)
		{
			this.transparent = isTransparent;
			cancelled = false;
			error = false;
			
			// Attach to DOM to prevent event cancelling when showing map in a modal dialog.
			fetchEl = DOM.createImg();
			ExtDOM.setVisible(fetchEl, false);			
			DOM.appendChild(DOM.getParent(imageElement), fetchEl);			
			ImageUtilGWT.fetchImage(fetchEl, src, 1, 1, this);
		}
		
		@Override
		public boolean shouldRefetch(int tries) {
			if (tries<=maxRetries && !cancelled)
				return true;
			return false;
		}

		@Override
		public void onSuccess(Element result)
		{
			
			error=false;
			if (cancelled)
				return;
			
			// Don't use this: IE makes a white border when interpolating :)
//			ImageUtilGWT.ensureInterpolation(imgEl);
			detachFetchElement();
			ExtDOM.setVisible(imageElement, true);
			ImageUtilGWT.setSource(imageElement, ImageUtilGWT.getSrc(result), transparent);
		}

		@Override
		public void onFailure(Throwable caught)
		{
			if (cancelled)
				return;
			error=true;
			detachFetchElement();
			ImageUtilGWT.clearSource(imageElement);
		}

		public void cancel()
		{
			cancelled = true;
			if (fetchEl != null) {
				ImageUtilGWT.cancelFetch(fetchEl);
				detachFetchElement();
			}
		}
		
		/**
		 * Fetch element should be detached when no longer needed (on success, error or when cancelled).
		 */
		private void detachFetchElement() {
			if(fetchEl != null) {
				fetchEl.removeFromParent();
			}
		}
	}

	private Element imgEl;
	private String lastUrl;
	private boolean trans = false;
	private int maxRetries;
	private TileCallback cb;

	
	public Tile(int maxRetries) {
		this.maxRetries = maxRetries;
		this.imgEl = DOM.createImg();
        MouseHandler.preventContextMenu(imgEl);
		CSS.position(imgEl, CSS.POS_ABSOLUTE);
	}

	public void position(int pixLeft, int pixTop)
	{
		CSS.leftTop(imgEl, pixLeft, pixTop);
	}

	public Tile setSrc(final String src)
	{
		if (cb != null) {
			cb.cancel();
		}
        ExtDOM.setVisible(imgEl, false);
		if (src != null) {
			if (cb == null) {
				cb = new TileCallback(imgEl);
			}
			lastUrl=src;
			cb.doFetch(src, trans);
		}
		return this;
	}

	public Tile setSize(int w, int h)
	{
		CSS.sizePx(imgEl, w, h);
		return this;
	}

	public void release()
	{
		try {
			removeFromParent();
			ExtDOM.setVisible(imgEl, false);
			ImageUtilGWT.clearSource(imgEl);
		} finally {
			cb=null;
			lastUrl=null;
			imgEl=null;
		}
	}

	public Element getElement()
	{
		return imgEl;
	}

	public void removeFromParent()
	{
		Element p = DOM.getParent(imgEl);
		if (p != null) {
			DOM.removeChild(p, imgEl);
		}
	}

	public void setTransPNG(boolean transPNG)
	{
		trans = transPNG;
	}

	public void checkErrorAndRefetch() {
		if (cb!=null && cb.error && lastUrl!=null) {
			setSrc(lastUrl);
		}
	}
}
