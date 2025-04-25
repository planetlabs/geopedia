package com.sinergise.common.geometry.tiles;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.display.ScaleLevelsSpec;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;

/**
 * To be used when tiles are used at any scale, not just at fixed zoom levels 
 */
public class WithoutZooms extends WithBounds {
    protected WithoutZooms() {
        super();
    }
    
    // TODO: Remove zooms
    public WithoutZooms(CRS baseCRS, String name, double pixSizeInMicrons, Envelope bounds, DimI tileSizePx) {
        super(baseCRS, name, ScaleLevelsSpec.createStandard(10, 10e6, 20).toPix(pixSizeInMicrons), bounds, tileSizePx);
    }
}