/*
 *
 */
package com.sinergise.gwt.gis.map.ui.actions;

import com.sinergise.gwt.gis.map.ui.ControlPositioner;

public interface MapActionsContext {
    public ControlPositioner positionerForMeasurement();

    public ControlPositioner positionerForFeatureInfo();
}
