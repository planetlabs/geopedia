/*
 *
 */
package com.sinergise.gwt.gis.map.ui;


import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.gis.map.render.RenderInfo;
import com.sinergise.gwt.ui.CompositeExt;
import com.sinergise.gwt.ui.DummyWidget;
import com.sinergise.gwt.util.html.CSS;


public abstract class OverlayComponent<T extends RenderInfo> extends CompositeExt {
    
    public int zIndex=MapComponent.Z_FEATURES_TOP;
    protected boolean visible = true;
    
    public OverlayComponent() {
        this(new DummyWidget());
    }
    
    public OverlayComponent(Widget container) {
        super();
        initWidget(container);
        CSS.background(getElement(), CSS.TRANSPARENT);
        CSS.overflow(getElement(), CSS.OVR_VISIBLE);
    }
    
    @Override
	public void setWidth(String width) {
        super.setWidth("0px");
    }
    @Override
	public void setHeight(String height) {
        super.setHeight("0px");
    }
    
    /**
     * Repositions the display according to the dca.
     * If <code>trans</code> is true, an attempt should be made to make the rendering transparent.
     * If it is false, the fastest rendering method (transparent or not) should be used.
     * <br><br>
     * If nothing is displayed, false should be returned. Otherwise, true :)
     * 
     * @param RenderInfo
     */
    public abstract void reposition(T info);

    public abstract T prepareToRender(DisplayCoordinateAdapter dca, boolean trans, boolean quick);

    @Override
	public boolean isVisible() {
        return visible;
    }

    @Override
	public void setVisible(boolean visible) {
        this.visible = visible;
        super.setVisible(visible);
    }
}
