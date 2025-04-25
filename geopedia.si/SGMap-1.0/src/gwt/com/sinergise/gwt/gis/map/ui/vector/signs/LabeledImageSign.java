package com.sinergise.gwt.gis.map.ui.vector.signs;

import java.util.ArrayList;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.gwt.gis.map.ui.vector.Marker;

public class LabeledImageSign extends ImageSign {
	private String label;

	public LabeledImageSign(String imageUrl, String label) {
		super(imageUrl);
		this.label = label;
	}
	
	public LabeledImageSign(String imageUrl, DimI size, String label) {
		super(imageUrl, size);
		this.label = label;
	}
	
	public LabeledImageSign(String imageUrl, boolean transPNG, DimI size, int aX, int aY, String label) {
		super(imageUrl, transPNG, size, aX, aY);
		this.label = label;
	}
	
	
	@Override
	public Element createContent(Marker marker) {
		Element div = DOM.createImg();
		Element outerDiv = DOM.createDiv();
		if (!loaded) {
			if (pending == null)
				pending = new ArrayList<Element>();
			pending.add(div);
		} else {
			updateElement(div);
		}
		Element labelDiv = DOM.createLabel();
		labelDiv.setInnerText(label);
		outerDiv.setClassName("LabeledImageSign");
		outerDiv.appendChild(div);
		outerDiv.appendChild(labelDiv);
		return outerDiv;
	}
	
}
