package com.sinergise.common.raster.core;

import java.net.URL;

import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.util.geom.DimI;

public class RasterFileInfo {
	private final URL imageURL;
	private final URL tfwURL;
	private RasterWorldInfo worldInfo;
	
	public RasterFileInfo(URL imageURL, DimI size, AffineTransform2D tr) {
		this(imageURL, null, size, tr);
	}
	
	public RasterFileInfo(URL imageURL, URL tfwURL, DimI imageSize, AffineTransform2D tr) {
		this(imageURL, tfwURL, new RasterWorldInfo(imageSize, tr));
	}
	
	public RasterFileInfo(URL imageURL, URL tfwURL, RasterWorldInfo worldInfo) {
		this.worldInfo = worldInfo;
		this.imageURL = imageURL;
		this.tfwURL = tfwURL;
	}

	public URL getImageURL() {
		return imageURL;
	}
	
	public URL getTfwURL() {
		return tfwURL;
	}

	public RasterWorldInfo getWorldInfo() {
		return worldInfo;
	}

	public RasterFileInfo cleanRoundoffs() {
		return new RasterFileInfo(imageURL, tfwURL, worldInfo.cleanRoundoffs());
	}

	public AffineTransform2D getTransform() {
		return worldInfo.tr;
	}

	public DimI getImageSize() {
		return worldInfo.getImageSize();
	}

	public int w() {
		return worldInfo.w;
	}

	public int h() {
		return worldInfo.h;
	}
}
