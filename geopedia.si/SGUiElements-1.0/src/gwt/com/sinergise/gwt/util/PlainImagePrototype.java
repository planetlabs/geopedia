/*
 *
 */
package com.sinergise.gwt.util;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;

public class PlainImagePrototype extends AbstractImagePrototype {
	private final String src;
	private int          w = -1;
	private int          h = -1;
	
	public PlainImagePrototype(final String source) {
		src = source;
	}
	
	public PlainImagePrototype(final String source, final int w, final int h) {
		this(source);
		this.w = w;
		this.h = h;
	}
	
	@Override
	public void applyTo(final Image image) {
		if (w < 0) {
			image.setUrl(src);
		} else {
			image.setUrlAndVisibleRect(src, 0, 0, w, h);
		}
	}
	
	@Override
	public Image createImage() {
		if (w < 0) {
			return new Image(src);
		}
		return new Image(src, 0, 0, w, h);
	}
	
	@Override
	public String getHTML() {
		throw new UnsupportedOperationException();
	}
}
