package com.sinergise.java.raster.core;

import com.sinergise.common.raster.core.RasterWorldInfo;

public interface RasterProcessingStep<T> {
	
	T processRaster(RasterWorldInfo ti);
}
