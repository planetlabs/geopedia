/*
 *
 */
package com.sinergise.gwt.gis.map.ui.actions.info;


import com.google.gwt.core.client.GWT;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.gis.map.ui.info.IFeatureInfoWidget;
import com.sinergise.common.ui.action.ToggleAction;
import com.sinergise.gwt.gis.i18n.Tooltips;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.resources.GisTheme;
import com.sinergise.gwt.ui.core.MouseClickAction;
import com.sinergise.gwt.ui.core.MouseHandler;


public class FeatureInfoModeAction extends ToggleAction {
	
	protected class FeatureInfoClick extends MouseClickAction {
        public FeatureInfoClick() {
            super("FeatureInfo");
            setCursor("url('"+GWT.getModuleBaseURL()+"style/cur/info.cur'), default");
        }
        @Override
		protected boolean mouseClicked(int x, int y) {
            showFeatureInfo(x,y);
            return false;
        }
    }
    
    protected MapComponent map;
    protected FeatureInfoClick clickAct = new FeatureInfoClick();
    protected IFeatureInfoWidget infoWidget;
    
    public FeatureInfoModeAction(MapComponent map, IFeatureInfoWidget infoWidget) {
        super(Tooltips.INSTANCE.toolbar_featureInfo());
        map.getToolsExcludeContext().register(this);
        setIcon(GisTheme.getGisTheme().gisStandardIcons().featureInfo());
        setProperty(LARGE_ICON_RES, GisTheme.getGisTheme().gisStandardIcons().featureInfo());
        this.map=map;
        this.infoWidget = infoWidget;
        setStyle("actionButFeatureInfo");
    }
    
    @Override
	protected void selectionChanged(boolean newSelected) {
        if (newSelected) {
            startMode();
        } else {
            endMode();
        }
    }
    
    protected void endMode() {
    	setSelected(false);
        map.mouser.deregisterAction(clickAct);
        infoWidget.hideWidget();
    }

    protected void startMode() {
    	setSelected(true);
       	infoWidget.showWidget();
        map.mouser.registerAction(clickAct, MouseHandler.BUTTON_LEFT, MouseHandler.MOD_NONE, 1);
    }

    protected void showFeatureInfo(int x, int y) {
        DisplayCoordinateAdapter dca = map.getCoordinateAdapter();
		double wX=dca.worldFromPix.x(x);
        double wY=dca.worldFromPix.y(y);
        infoWidget.showInfo(dca.worldCRS, wX, wY);
    }
}
