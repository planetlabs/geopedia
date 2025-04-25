/*
 *
 */
package com.sinergise.gwt.gis.map.ui.controls;

import java.util.ArrayList;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.gis.map.render.RenderInfo;
import com.sinergise.gwt.gis.map.ui.OverlayComponent;
import com.sinergise.gwt.util.html.CSS;


public class FixedOverlay extends OverlayComponent<RenderInfo> {
	String CSS_CLASS_FIXED_OVERLAY = "sgwebgis-fixedOverlay";
	
    public class FixedMapWidget {
        Widget comp;
        int posX;
        int posY;
        //relative width and height of control in percentage of map size
        int relW = Integer.MIN_VALUE; 
        int relH = Integer.MIN_VALUE;
        RelativePosition relativeTo = null;
        
        FixedMapWidget(Widget wgt, RelativePosition relativeTo) { 
        	this.comp = wgt;
        	this.relativeTo = relativeTo;
        	this.posX = relativeTo.offsetX;
        	this.posY = relativeTo.offsetY;
        }
        
        FixedMapWidget(Widget wgt, int posX, int posY) {
            this.comp=wgt;
            this.posX=posX;
            this.posY=posY;
        }
        
        FixedMapWidget(Widget wgt, int posX, int posY, int relW, int relH) {
        	this(wgt, posX, posY);
        	setRelativeSize(relW, relH);
        }
        
        public void setPosition(int posX, int posY) {
        	if (relativeTo != null) {
        		relativeTo.offsetX = posX;
        		relativeTo.offsetY = posY;
        	}
        	this.posX = posX;
        	this.posY = posY;
        }
        
        public void setRelativeSize(int relW, int relH) {
        	 this.relW = relW;
             this.relH = relH;
        }
        
    }
    ArrayList<FixedMapWidget> controls;
    AbsolutePanel container;
    
    public FixedOverlay() {
        super(new AbsolutePanel());
        CSS.position(this, CSS.POS_RELATIVE);
        container=(AbsolutePanel)getWidget();
        container.setStylePrimaryName(CSS_CLASS_FIXED_OVERLAY);
        controls=new ArrayList<FixedMapWidget>();
    }
 
    public void add(Widget ctrlWgt, int posX, int posY) {
        insert(controls.size(), ctrlWgt, posX, posY);
    }
    
    public void add(Widget ctrlWgt, int posX, int posY, int relW, int relH) {
        insert(controls.size(), new FixedMapWidget(ctrlWgt, posX, posY, relW, relH));
    }
    
    public void add(Widget ctrlWgt, RelativePosition relativeTo) {
        insert(controls.size(), ctrlWgt, relativeTo);
    }
    
    public void insert(int index, Widget ctrlWgt, int posX, int posY) {
    	insert(index, new FixedMapWidget(ctrlWgt, posX, posY));
    }
    
    public void insert(int index, Widget ctrlWgt, RelativePosition relativeTo) {
        insert(index, new FixedMapWidget(ctrlWgt, relativeTo));
    }
    
    private void insert(int index, FixedMapWidget ctrl) {
    	controls.add(index, ctrl);
        container.add(ctrl.comp, 0, 0);
        positionWidget(ctrl, container.getOffsetWidth(), container.getOffsetHeight());
    }
    
    public FixedMapWidget getControlFor(Widget ctrlWgt) {
    	for (FixedMapWidget fmc : controls) {
    		if (fmc.comp==ctrlWgt) return fmc;
		}
    	return null;
    }
   
    public boolean remove(Widget ctrlWgt) {
        int idx=container.getWidgetIndex(ctrlWgt);
        if (idx<0) return false;
        controls.remove(idx);
        container.remove(idx);
        return true;
    }
    
    
    private RenderInfo myInfo=new RenderInfo();
    @Override
	public RenderInfo prepareToRender(DisplayCoordinateAdapter dca, boolean trans, boolean quick) {
        myInfo.hasAnything=false;
        for (FixedMapWidget fmc : controls) {
            if (fmc.comp.isVisible()) {
                myInfo.hasAnything=true;
                break;
            }
        }
    	myInfo.dca = dca;
        myInfo.isTransparent = true;
        myInfo.quick = quick;
        return myInfo;
    }
    @Override
	public void reposition(RenderInfo info) {
        //TODO: Remove this and replace with CSS positioning
    	for (FixedMapWidget fmc : controls) {
            positionWidget(fmc, myInfo.getDisplayWidth(), myInfo.getDisplayHeight());
        }
    }

    //TODO: Remove this and replace with CSS positioning
    protected boolean positionWidget(final FixedMapWidget ctrl, final int w,final int h) {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
                Widget ctrWgt=ctrl.comp;
                int wgtW=0;
                int wgtH=0;
                int x=0;
                int y=0;
                
                if(ctrl.relW > 0) {
                	wgtW = (int)Math.round((ctrl.relW/100d)*w);
                	ctrWgt.setWidth(wgtW+"px");
                }
                if(ctrl.relH > 0) {
                	wgtH = (int)Math.round((ctrl.relH/100d)*h);
                	ctrWgt.setHeight(wgtH+"px");
                }
                
                if(ctrl.relativeTo == null) {
                	
	                if (ctrl.posX<0) {
	                    wgtW=ctrWgt.getOffsetWidth();
	                    x=w-wgtW+ctrl.posX+1;
	                } else {
	                    x=ctrl.posX;
	                }
	                if (ctrl.posY<0) {
	                    wgtH=ctrWgt.getOffsetHeight();
	                    y = h-wgtH+ctrl.posY+1;
	                } else {
	                    y = ctrl.posY;
	                }
	                
                } else {
                	
                	RelativePosition rel = ctrl.relativeTo;
                	int relW = rel.relativeTo.getOffsetWidth();
                	int relH = rel.relativeTo.getOffsetHeight();
                	int relX = container.getWidgetLeft(rel.relativeTo);
                	int relY = container.getWidgetTop(rel.relativeTo);
                	
                	switch (rel.hAlign) {
                	case CENTER:
                		x = relX+(relW-ctrWgt.getOffsetWidth())/2 + rel.offsetX;
                		break;
                	case LEFT_RIGHT:
                		x = relX + rel.offsetX;
                		break;
                	case RIGHT_RIGHT:
                		x = relX+relW + rel.offsetX;
                		break;
                	default:
                		if (ctrl.posX<0) {
    	                    wgtW=ctrWgt.getOffsetWidth();
    	                    x=w-wgtW+ctrl.posX+1;
    	                } else {
    	                    x=ctrl.posX;
    	                }
                	}
                	
                	
                	switch (rel.vAlign) {
                	case MIDDLE:
                		y = relY+(relH-ctrWgt.getOffsetHeight())/2 + rel.offsetY;
                		break;
                	case TOP_UP:
                		y = relY + rel.offsetY;
                		break;
                	case BOTTOM_DOWN:
                		y = relY + relH + rel.offsetY;
                		break;
                	default:
                		if (ctrl.posY<0) {
    	                    wgtH=ctrWgt.getOffsetHeight();
    	                    y = h-wgtH+ctrl.posY+1;
    	                } else {
    	                    y = ctrl.posY;
    	                }
                	}
                	
                }
                container.setWidgetPosition(ctrl.comp, x, y);
            }
        });
        return getWidget().isVisible();
    }
}
