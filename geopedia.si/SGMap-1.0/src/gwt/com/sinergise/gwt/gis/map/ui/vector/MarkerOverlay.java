/**
 * Copyright (c) 2008 by Cosylab d.d.
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the LICENSE file.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE ASSUMES _NO_
 * RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION, OR
 * REDISTRIBUTION OF THIS SOFTWARE.
 */
package com.sinergise.gwt.gis.map.ui.vector;

import static com.sinergise.common.util.CheckUtil.checkArgument;

import java.util.HashSet;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.sinergise.common.geometry.crs.CartesianCRS;
import com.sinergise.common.geometry.crs.transform.SimpleTransform;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.gis.map.render.RenderInfo;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.gwt.gis.map.ui.OverlayComponent;
import com.sinergise.gwt.util.html.CSS;


/**
 * @author smusevic
 */
public class MarkerOverlay extends OverlayComponent<RenderInfo> {

	protected AbsolutePanel pnl;
	protected RenderInfo myInfo = new RenderInfo();
	
	protected HashSet<AbstractMarker> points;
	protected final DisplayCoordinateAdapter dca;
	protected final SimpleTransform.ToInt<CartesianCRS, CartesianCRS> pxFromW;
	protected final SimpleTransform.FromInt<CartesianCRS, CartesianCRS> wFromPx;
	
	public MarkerOverlay(DisplayCoordinateAdapter dca)
	{
		super(new AbsolutePanel());
		//TODO: Handle panning by moving the container, not by repositioning each marker 
	    this.dca = dca;
	    this.wFromPx = dca.worldFromPix;
	    this.pxFromW = dca.pixFromWorld;
	    pnl = (AbsolutePanel)getWidget();
	    points = new HashSet<AbstractMarker>();
	    
	    
	}
	
	@Override
	public RenderInfo prepareToRender(DisplayCoordinateAdapter renderingDCA, boolean trans, boolean quick) {
	    myInfo.quick=quick;
	    myInfo.hasAnything=pnl.getWidgetCount()>0;
	    myInfo.isTransparent=true;
	    return myInfo;
	}

	@Override
	public void reposition(RenderInfo info) {
	    setPixelSize(info.dca.getDisplaySize());
	    updateDisplay();
	}

	public void addPoint(AbstractMarker markr) {
		if (containsPoint(markr)) {
			internalRemove(markr);
		}
	    internalAdd(markr);
	    updatePointLocation(markr);
	}

	protected void internalAdd(AbstractMarker markr) {
		points.add(markr);
	}

	public boolean containsPoint(AbstractMarker markr) {
		return points.contains(markr);
	}

	public void removePoint(AbstractMarker markr) {
		if (markr == null) {
			return;
		}
		checkArgument(points.contains(markr), "Can't remove marker that is not contained in the overlay");
	    internalRemove(markr);
	}

	protected void internalRemove(AbstractMarker markr) {
	    points.remove(markr);
	    removeFromPanel(markr);
	}

	public void clear() {
		for (AbstractMarker m : points) {
			pnl.remove(m);
		}
		points.clear();
	}
	
	public boolean isEmpty() {
		return points.isEmpty();
	}

	public void updatePointLocation(final AbstractMarker m) {
		HasCoordinate mPos = m.getWorldPosition();
		double x = mPos.x();
		double y = mPos.y();
		if (!m.isVisible() || !dca.worldRect.contains(x, y)) {
			removeFromPanel(m);
		    return;
		}
		if (!m.isDisplayed()) {
			m.prepareToRender();
		    CSS.position(m, CSS.POS_ABSOLUTE); // AbsolutePanel resets position when removing
			pnl.add(m);
		}
		final double l = pxFromW.x(x);
		final double t = pxFromW.y(y);
		m.positionPx(l,t);
	}

	private void removeFromPanel(AbstractMarker m) {
		if (m.isDisplayed()) {
		    pnl.remove(m);
		}
	}

	public void setPointLocationPix(Marker point, int x, int y) {
	    point.setLocation(wFromPx.point(x, y));
	    point.positionPx(x, y);
	}

	public void updateDisplay() {
	    for (AbstractMarker m : points) {
	        updatePointLocation(m);
	    }
	}

}

/* __oOo__ */