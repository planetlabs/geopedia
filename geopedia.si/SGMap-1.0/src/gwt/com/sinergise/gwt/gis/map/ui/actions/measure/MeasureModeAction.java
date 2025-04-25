/*
 *
 */
package com.sinergise.gwt.gis.map.ui.actions.measure;


import com.sinergise.common.ui.action.ToggleAction;
import com.sinergise.gwt.gis.i18n.Tooltips;
import com.sinergise.gwt.gis.map.ui.ControlPositioner;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.resources.GisTheme;


public class MeasureModeAction extends ToggleAction {
    
	private Measurer editr;
    
	public MeasureModeAction(MapComponent map, ControlPositioner resultsPositioner) {
        super(Tooltips.INSTANCE.toolbar_lengthAreaMeasurement());
		editr = new Measurer(map, resultsPositioner, getIcon(), this);
		setStyle("mapToolbarMeasureAction");
		init(map);
    }

	public MeasureModeAction(MapComponent map, Measurer editr) {
		super(Tooltips.INSTANCE.toolbar_lengthAreaMeasurement());
		this.editr = editr;
		setStyle("mapToolbarMeasureAction");
		init(map);
	}
	
	private void init(MapComponent map) {
		map.getToolsExcludeContext().register(this);
        setIcon(GisTheme.getGisTheme().gisStandardIcons().measure());
        setProperty(LARGE_ICON_RES, GisTheme.getGisTheme().gisStandardIcons().measure());
	}
    
	public MeasureModeAction(MapComponent map, IMeasureResultsPanel measureResultsPanel) {
        super("Measurement");
        setStyle("mapToolbarMeasureAction");
        map.getToolsExcludeContext().register(this);
        setIcon(GisTheme.getGisTheme().gisStandardIcons().measure());
        setProperty(LARGE_ICON_RES, GisTheme.getGisTheme().gisStandardIcons().measure());
        editr=new Measurer(map, measureResultsPanel);
    }

    @Override
	protected void selectionChanged(boolean selected) {
        if (selected) startMeasureMode();
        else finishMeasureMode();
    }

	public Measurer getMeasurer() {
		return editr;
	}

    private void finishMeasureMode() {
        editr.cleanup();
    }

    private void startMeasureMode() {
        editr.start();
    }
}
