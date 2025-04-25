/*
 *
 */
package com.sinergise.gwt.gis.map.ui.controls;

import java.util.ArrayList;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.gis.map.render.RenderInfo;
import com.sinergise.gwt.gis.map.ui.OverlayComponent;


public class EffectsOverlay extends OverlayComponent<RenderInfo> {
    ArrayList<EffectElement> controls;
    Element container;

    public EffectsOverlay() {
        super();
        container=getElement();
        controls=new ArrayList<EffectElement>();
    }

    public EffectElement add(Element elem) {
        return insert(controls.size(), elem);
    }
    
    public EffectElement insert(int index, Element elem) {
        EffectElement ret=new EffectElement(elem);
        controls.add(index, ret);
        DOM.insertChild(container, ret.el, index);
        if (!isVisible()) {
            setVisible(true);
        }
        return ret;
    }
   
    public boolean remove(Element elem) {
        EffectElement ctrl=effectFor(elem);
        boolean ret=controls.remove(ctrl);
        if (ret) DOM.removeChild(container, ctrl.el);
        return ret;
    }
    
    public EffectElement effectFor(Element elem) {
    	for (EffectElement el : controls) {
            if (el.el.equals(elem)) return el;
        }
        return null;
    }

    public void add(EffectElement elem) {
        insert(controls.size(), elem);
    }
    
    public void insert(int index, EffectElement elem) {
        controls.add(index, elem);
        DOM.insertChild(container, elem.el, index);
        if (!isVisible()) {
            setVisible(true);
        }
    }
   
    public boolean remove(EffectElement elem) {
        boolean ret=controls.remove(elem);
        if (ret) DOM.removeChild(container, elem.el);
        return ret;
    }

    RenderInfo myInfo=new RenderInfo();
    @Override
	public RenderInfo prepareToRender(DisplayCoordinateAdapter dca,
            boolean trans, boolean quick) {
        myInfo.quick=quick;
        myInfo.hasAnything=controls.size()>0;
        myInfo.isTransparent=true;
        return myInfo;
    }
    @Override
	public void reposition(RenderInfo info) {
    }
}
