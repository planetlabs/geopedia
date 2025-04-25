/*
 *
 */
package com.sinergise.geopedia.client.core.map.markers;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.sinergise.common.util.geom.EnvelopeI;
import com.sinergise.common.util.geom.PointI;
import com.sinergise.gwt.ui.PNGImage;

public class ImageSign extends Sign
{
	private String imageSrc;
	public ImageSign(String imageSrc, PointI anchor, EnvelopeI activeRect)
    {
		super();
		this.imageSrc=imageSrc;
		this.anchor=anchor;
		this.linkMBR=activeRect;
    }
	@Override
	protected Element initContent(MarkerOld marker)
	{
		PNGImage img = new PNGImage(imageSrc);
		img.setTitle(marker.getText());
		DOM.appendChild(marker.getElement(), img.getElement());
	    return super.initContent(marker);
	}
}
