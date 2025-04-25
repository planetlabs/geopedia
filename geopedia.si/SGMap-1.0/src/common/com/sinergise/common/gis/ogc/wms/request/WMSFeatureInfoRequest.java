/*
 *
 */
package com.sinergise.common.gis.ogc.wms.request;

import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.gis.ogc.OGCException;
import com.sinergise.common.gis.ogc.wms.WMSLayerElement;
import com.sinergise.common.gis.ogc.wms.WMSUtil;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.PointI;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.common.util.web.MimeType;

public class WMSFeatureInfoRequest extends WMSMapRequest implements IWMSFeatureInfoRequest {
	
	private static final long serialVersionUID = 1L;

	/**
	 * The mandatory QUERY_LAYERS parameter states the map layer(s) from which feature information is desired to be retrieved. Its value is a comma-separated list of one or more map layers. This parameter shall contain at least one layer name, but may contain fewer layers than the original GetMap request. If any layer in the QUERY_LAYERS parameter is not defined in the service metadata of the WMS, the server shall issue a service exception (code = LayerNotDefined).
	 */
	public static final String PARAM_QUERY_LAYERS = "QUERY_LAYERS";
	public WMSFeatureInfoRequest() {
		super(REQ_GET_FEATURE_INFO);
	}

	public void setQueryPoint(double x, double y, double worldPerPixel, double pxRadius, int imageW, int imageH) {
		if (imageW % 2 == 0) {
			imageW++;
		}
		if (imageH % 2 == 0) {
			imageH++;
		}
		double w = worldPerPixel * imageW;
		double h = worldPerPixel * imageH;
		setWindowSize(DimI.create(imageW, imageH));
		setBBox(Envelope.withCenter(x, y, w, h));
		setQueryPixel(imageW / 2, imageH / 2);
		setPxRadius(Double.valueOf(pxRadius));
	}

	@Override
	public void validate() throws OGCException {
		super.validate();
		validateNotNull(PARAM_QUERY_LAYERS);
		validateNotNull(PARAM_INFO_FORMAT);
		validateNotNull(PARAM_PIXEL_LEFT);
		validateNotNull(PARAM_PIXEL_TOP);
	}

	public MimeType getInfoFormat() {
		return MimeType.constructMimeType(get(PARAM_INFO_FORMAT));
	}

	public int getFeatureCount() {
		return getInt(PARAM_FEATURE_COUNT, 1);
	}

	public PointI getPixelPosition() {
		int i = getInt(PARAM_PIXEL_LEFT, getInt(PARAM_PIXEL_LEFT_OLD, -1));
		int j = getInt(PARAM_PIXEL_TOP, getInt(PARAM_PIXEL_TOP_OLD, -1));
		if (i < 0 || j < 0 || i >= getImageWidth() || j >= getImageHeight())
			throw new IllegalArgumentException("I and J should be non-negative integers in the displayed image, not (" + get(PARAM_PIXEL_LEFT) + "," + get(PARAM_PIXEL_TOP) + ")");
		return new PointI(i, j);
	}

	public Point getQueryPoint() {
		PointI px = getPixelPosition();
		Envelope bnds = getBBox();
		DimI size = new DimI(getImageWidth(), getImageHeight());
		double xSpan = bnds.getWidth();
		double ySpan = bnds.getHeight();
		double x = bnds.getMinX() + xSpan * (px.x + 0.5) / size.w();
		double y = bnds.getMaxY() - ySpan * (px.y + 0.5) / size.h();
		return new Point(x, y);
	}

	public void setQueryPixel(int left, int top) {
		set(PARAM_PIXEL_LEFT, String.valueOf(left));
		set(PARAM_PIXEL_TOP, String.valueOf(top));
		set(PARAM_PIXEL_LEFT_OLD, String.valueOf(left));
		set(PARAM_PIXEL_TOP_OLD, String.valueOf(top));
	}

	public String[] getQueryLayers() {
		return WMSUtil.decodeArray(get(PARAM_QUERY_LAYERS, ""));
	}

	public void setQueryLayers(WMSLayerElement[] layers) {
		setQueryLayerNames(WMSUtil.toWMSLayerNames(layers));
	}

	public void setQueryLayerNames(String[] names) {
		set(PARAM_QUERY_LAYERS, WMSUtil.encodeArray(names));
	}

	public void setInfoFormat(MimeType mimeType) {
		set(PARAM_INFO_FORMAT, mimeType.createContentTypeString());
	}

	/**
	 * @return radius to search, in px
	 */
	public Double getPxRadius() {
		return getDouble(PARAM_RADIUS, null);
	}

	/**
	 * @return radius to search, in px
	 */
	public Double getWorldRadius() {
		Double pxR = getPxRadius();
		if (pxR == null) {
			return null;
		}
		return Double.valueOf(getPixelSize() * pxR.doubleValue());
	}

	public double getPixelSize() {
		Envelope bnds = getBBox();
		DimI size = new DimI(getImageWidth(), getImageHeight());
		double xSpan = bnds.getWidth();
		double ySpan = bnds.getHeight();
		return Math.max(xSpan / size.w(), ySpan / size.h());
	}

	/**
	 * @param radius
	 *            to search, in px
	 */
	public void setPxRadius(Double pxRadius) {
		set(PARAM_RADIUS, StringUtil.toString(pxRadius, null));
	}

	public void setFeatureCount(int cnt) {
		set(PARAM_FEATURE_COUNT, String.valueOf(cnt));
	}
}
