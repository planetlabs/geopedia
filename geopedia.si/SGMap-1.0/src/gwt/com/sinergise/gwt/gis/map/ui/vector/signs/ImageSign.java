package com.sinergise.gwt.gis.map.ui.vector.signs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.UIObject;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.gwt.gis.map.ui.vector.Marker;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.ui.ImageUtilGWT;
import com.sinergise.gwt.util.html.CSS;

public class ImageSign extends Sign {
	public static final String DEFAULT_IMAGE_CLASSNAME = StyleConsts.MAP_DEFAULT_IMAGE_SIGN;
	private static HashMap<String, ImageSign> defaultMarkers;
	private static final ImageSign createDefaultMarkerSign(String color) {
		return new ImageSign(GWT.getModuleBaseURL() + "img/marker/marker_" + color + ".png", true, DimI.create(38, 38), 10, 37);
	}
	public static final ImageSign getDefaultMarkerSign(String color) {
		if (defaultMarkers == null)
			defaultMarkers = new HashMap<String, ImageSign>();
		ImageSign sgn = defaultMarkers.get(color);
		if (sgn == null) {
			sgn = createDefaultMarkerSign(color);
			defaultMarkers.put(color, sgn);
		}
		return sgn;
	}

	protected String imageUrl;
	protected Element elementStore;
	protected boolean transPNG = false;
	protected boolean loaded = false;

	public ImageSign(String imageUrl) {
		this(imageUrl, DimI.EMPTY);
	}
	public ImageSign(String imageUrl, DimI size) {
		this(imageUrl, imageUrl.contains("png"), size, size.w()/2 , size.h()/2);
	}
	public ImageSign(String imageUrl, boolean transPNG, DimI imageSize, int aX, int aY) {
		super(DEFAULT_IMAGE_CLASSNAME, imageSize);
		this.transPNG = transPNG;
		setAnchor(aX, aY);
		this.imageUrl = imageUrl;
		ImageUtilGWT.fetchImage(imageUrl, 10,
				new ImageUtilGWT.ImageFetchCallback() {
					@Override
					public void onSuccess(Element imgEl) {
						loaded = true;
						elementStore = imgEl;
						if (size.isEmpty()) {
							size = ImageUtilGWT.getNaturalSize(imgEl);
						}
						if (pending != null) {
							for (Iterator<Element> it = pending.iterator(); it
									.hasNext();) {
								Element el = it.next();
								updateElement(el);
							}
							pending = null;
						}
					}
					@Override
					public void onFailure(Throwable caught) {
						caught.printStackTrace();
					}
					@Override
					public boolean shouldRefetch(int tries) {
						return tries < 2;
					}
				});
	}

	ArrayList<Element> pending;

	@Override
	public Element createContent(Marker marker) {
		Element div = DOM.createImg();
		if (!loaded) {
			if (pending == null)
				pending = new ArrayList<Element>();
			pending.add(div);
		} else {
			updateElement(div);
		}
		return div;
	}

	public void updateElement(Element imgEl) {
		CSS.className(imgEl, className);
		ImageUtilGWT.setSource(imgEl, imageUrl, transPNG, size.w() >= 0);
		if (!size.isEmpty()) {
			ImageUtilGWT.setSize(imgEl, size);
			UIObject.setVisible(imgEl, true);
		} else {
			UIObject.setVisible(imgEl, false);
		}
		if (background != null) {
			CSS.background(imgEl, background);
		}
	}
}
