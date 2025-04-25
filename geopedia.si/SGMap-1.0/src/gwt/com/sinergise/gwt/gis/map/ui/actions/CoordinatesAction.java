/*
 *
 */
package com.sinergise.gwt.gis.map.ui.actions;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CrsDescriptor;
import com.sinergise.common.ui.action.Action;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.controls.coords.CoordinateDialog;
import com.sinergise.gwt.gis.resources.GisTheme;


public class CoordinatesAction extends Action {
    private MapComponent map;
    private CoordinateDialog dialog;
    protected CrsDescriptor[] sysDescs;
    public CoordinatesAction(MapComponent map) {
        super("X,Y");
        setDescription("Set map centre");
        setIcon(GisTheme.getGisTheme().gisStandardIcons().centerXY());
        setStyle("actionButCoordinates");
        
        this.map=map;
    }
    
    public void setCoordinateSystems(CrsDescriptor[] systems) {
    	this.sysDescs=systems;
    }
    
    public void setCoordinateSystems(CRS[] systems, String[] names) {
    	sysDescs=new CrsDescriptor[systems.length];
    	for (int i = 0; i < systems.length; i++) {
    		CRS crs=systems[i];
			String name=names==null?null:names[i];
			if (name==null) {
				name=crs.getNiceName();
			}
			sysDescs[i]=new CrsDescriptor(name, crs);
		}
    }
    
    @Override
	protected void actionPerformed() {
        if (dialog != null && dialog.isAttached()) {
            return;
        }
        if (sysDescs==null) {
        	dialog = new CoordinateDialog(map);
        } else {
        	dialog = new CoordinateDialog(map, sysDescs);
        }
        dialog.showCentered();
    }
}
