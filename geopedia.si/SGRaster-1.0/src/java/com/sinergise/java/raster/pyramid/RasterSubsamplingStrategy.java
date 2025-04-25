package com.sinergise.java.raster.pyramid;

import java.awt.image.BufferedImage;

import com.sinergise.java.raster.core.RasterUtilJava;

public interface RasterSubsamplingStrategy {

	public class Default implements RasterSubsamplingStrategy {
		@Override
		public BufferedImage shrinkBy2(BufferedImage largeImg, BufferedImage smallImgOut) {
			return RasterUtilJava.shrink(largeImg, smallImgOut);
		}
	}

	BufferedImage shrinkBy2(BufferedImage largeImg, BufferedImage smallImg);

}
