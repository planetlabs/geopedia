package com.sinergise.common.raster.core;

import java.math.BigDecimal;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CartesianCRS;
import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.util.ImageUtil;


public class RasterUtil extends ImageUtil {
	public static final AffineTransform2D affineFromCellBasedArrayImage(int imgW, int imgH, CRS worldCRS, double[] tfwdata) {
		return affineFromCellBasedArray(CartesianCRS.createImageCRS(imgW, imgH), worldCRS, tfwdata);
	}

	public static final double[] affineToCellBasedArray(AffineTransform2D tr) {
		double[] affArr = tr.paramsToArray();
		//concatenate with translation by (-0.5, -0.5)
		return new double[]{ //
			affArr[0], affArr[1], //
			affArr[2], affArr[3], //
			niceAdd(affArr[4], affArr[2], affArr[0]), 
			niceAdd(affArr[5], affArr[3], affArr[1])
		};
	}
	

	private static double niceAdd(double whole, double half1, double half2) {
		BigDecimal two = BigDecimal.valueOf(2);
		return BigDecimal.valueOf(whole).add(BigDecimal.valueOf(half1).divide(two)).add(BigDecimal.valueOf(half2).divide(two)).doubleValue();
	}

	public static AffineTransform2D affineFromCellBasedArray(CRS imageCRS, CRS worldCRS, double[] tfwdata) {
		final double[] mGrid =
			new double[]{
				tfwdata[0], 
				tfwdata[1], 
				tfwdata[2], 
				tfwdata[3], 
				tfwdata[0] * (-0.5) + tfwdata[2] * (-0.5) + tfwdata[4],
				tfwdata[1] * (-0.5) + tfwdata[3] * (-0.5) + tfwdata[5]
			};
		return new AffineTransform2D(imageCRS, worldCRS, mGrid);
	}
	
	public static final int numTilesPerDir(TiledCRS space, int level) {
		return space.maxColumn(level) + 1;
	}

	public static final String JAIType(String imageSuffix) {
		if ("tif".equalsIgnoreCase(imageSuffix)) {
			return "TIFF";
		} else if ("jpg".equalsIgnoreCase(imageSuffix)) { return "JPEG"; }
		return imageSuffix.toUpperCase();
	}

	public static String imageTypeFromSuffix(String imageSuffix) {
		return imageSuffix.toLowerCase();
	}
}
