/*
 *
 */
package com.sinergise.common.gis.ogc.wms.request;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.gis.ogc.OGCException;
import com.sinergise.common.gis.ogc.wms.WMSLayerElement;
import com.sinergise.common.gis.ogc.wms.WMSUtil;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.url.URLUtil;

public class WMSMapRequest extends WMSRequest implements IWMSGetRequest {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Comma-separated list of one or more map layers.
	 * <p>
	 * The mandatory LAYERS parameter lists the map layer(s) to be returned by this GetMap request. The value of the LAYERS parameter is a
	 * comma-separated list of one or more valid layer names. Allowed layer names are the character data content of any <Layer><Name>
	 * element in the service metadata.
	 * </p>
	 * <p>
	 * A WMS shall render the requested layers by drawing the leftmost in the list bottommost, the next one over that, and so on.
	 * </p>
	 * <p>
	 * The optional <LayerLimit> element in the service metadata is a positive integer indicating the maximum number of layers a client is
	 * permitted to include in a single GetMap request. If this element is absent, the server imposes no limit.
	 * </p>
	 */
	public static final String PARAM_LAYERS = "LAYERS";
	/**
	 * Comma-separated list of one rendering style per requested layer.
	 * <p>
	 * The mandatory STYLES parameter lists the style in which each layer is to be rendered. The value of the STYLES parameter is a
	 * comma-separated list of one or more valid style names.
	 * </p>
	 * <p>
	 * There is a one-to-one correspondence between the values in the LAYERS parameter and the values in the STYLES parameter. Each map in
	 * the list of LAYERS is drawn using the corresponding style in the same position in the list of STYLES.
	 * </p>
	 * <p>
	 * Each style Name shall be one that was defined in a &lt;Style&gt;&lt;Name&gt; element that is either directly contained within, or
	 * inherited by, the associated &lt;Layer&gt; element in service metadata. (In other words, the client may not request a Layer in a
	 * Style that was only defined for a different Layer.) A server shall throw a service exception (code = StyleNotDefined) if an
	 * unadvertised Style is requested.
	 * </p>
	 * <p>
	 * A client may request the default Style using a null value (as in “STYLES=”). If several layers are requested with a mixture of named
	 * and default styles, the STYLES parameter shall include null values between commas (as in “STYLES=style1,,style2,,”). If all layers
	 * are to be shown using the default style, either the form “STYLES=” or “STYLES=,,,” is valid.
	 * <p>
	 * <p>
	 * If the server advertises several styles for a layer, and the client sends a request for the default style, the choice of which style
	 * to use as default is at the discretion of the server. The ordering of styles in the service metadata does not indicate which is the
	 * default.
	 * <p>
	 */
	public static final String PARAM_STYLES = "STYLES";
	/**
	 * Width in pixels of map picture.
	 */
	public static final String PARAM_WIDTH = "WIDTH";
	/**
	 * Height in pixels of map picture.
	 */
	public static final String PARAM_HEIGHT = "HEIGHT";

	/**
	 * BBOX="minx, miny, maxx, maxy" Bounding box corners (lower left, upper right) in CRS units.
	 */
	public static final String PARAM_BBOX = "BBOX";
	public static final String REQ_GET_MAP = "GetMap";

	/**
	 * CRS=namespace:identifier Coordinate reference system.
	 */
	public static final String PARAM_CRS = "CRS";
	/** 
	 * WMS version 1.1.1 and older: SRS=namespace:identifier Spatial reference system.
	 */
	public static final String PARAM_SRS = "SRS";
	
	public WMSMapRequest() {
		this(REQ_GET_MAP);
	}

	protected WMSMapRequest(String request) {
		super(request);
	}

	@Override
	public void setCRS(CRS crs) {
		WMSUtil.setCRS(this, crs);
	}

	@Override
	public void setTransparent(boolean trans) {
		WMSUtil.set(this, PARAM_TRANSPARENT, trans, false);
	}

	@Override
	public void setTransparentAdjustFormat(boolean trans) {
		WMSUtil.setTransparentAdjustFormat(this, trans);
	}

	@Override
	public boolean isTransparent() {
		return WMSUtil.isTransparent(this);
	}

	@Override
	public String prepareURLwithoutView(String baseURL) {
		String oldBBox = set(PARAM_BBOX, null);
		String oldW = set(PARAM_WIDTH, null);
		String oldH = set(PARAM_HEIGHT, null);
		try {
			return super.createRequestURL(baseURL);
		} finally {
			set(PARAM_BBOX, oldBBox);
			set(PARAM_WIDTH, oldW);
			set(PARAM_HEIGHT, oldH);
		}
	}

	public String createRequestURLWithPrepared(String preparedPrefix, Envelope bbox, DimI size) {
		StringBuffer buf = new StringBuffer(preparedPrefix);
		buf.append('&');
		buf.append(URLUtil.encodePart(PARAM_BBOX));
		buf.append('=');
		WMSUtil.appendWMSBBox(buf, bbox);
		buf.append('&');
		buf.append(URLUtil.encodePart(PARAM_WIDTH));
		buf.append('=');
		buf.append(size.w());
		buf.append('&');
		buf.append(URLUtil.encodePart(PARAM_HEIGHT));
		buf.append('=');
		buf.append(size.h());
		System.out.println(buf);
		return buf.toString();
	}

	public String createRequestURL(String baseURL, Envelope bbox, DimI size) {
		return createRequestURLWithPrepared(prepareURLwithoutView(baseURL), bbox, size);
	}

	@Override
	public void setLayerNames(String[] layerNames) {
		WMSUtil.setLayerNames(this, PARAM_LAYERS, layerNames);
	}

	@Override
	public void setStyleNames(String[] styleNames) {
		WMSUtil.setStyleNames(this, PARAM_STYLES, styleNames);
	}

	@Override
	public void setLayers(WMSLayerElement[] layers) {
		WMSUtil.setLayers(this, PARAM_LAYERS, PARAM_STYLES, layers);
	}

	@Override
	public String[] getLayerNames() {
		String[] layers = WMSUtil.getLayerNames(this, PARAM_LAYERS);
		if (layers == null)
			return new String[] {};
		return layers;
	}

	@Override
	public String[] getStyleNames() {
		return WMSUtil.getStyleNames(this, PARAM_STYLES);
	}

	@Override
	public int getImageWidth() {
		return getInt(PARAM_WIDTH, 256);
	}

	@Override
	public int getImageHeight() {
		return getInt(PARAM_HEIGHT, 256);
	}
	
	public DimI getWindowSize() {
		return DimI.create(getImageWidth(), getImageHeight());
	}

	public Envelope getBBox() {
		return WMSUtil.fromWMSBBox(get(PARAM_BBOX));
	}

	@Override
	public void validate() throws OGCException {
		validate(true);
	}

	public void validate(boolean doStyles) throws OGCException {
		super.validate();
		validateNotNull(PARAM_VERSION);
		if (doStyles) {
			validateNotNull(PARAM_LAYERS);
			validateNotNull(PARAM_STYLES);
			String[] lyr = getLayerNames();
			String[] sty = getStyleNames();
			if (sty != null && sty.length > 0 && sty.length != lyr.length) {
				throw new OGCException(this, PARAM_STYLES + " should be the same size as " + PARAM_LAYERS + ".");
			}
		}
		validateNotNull(PARAM_CRS);
		validateNotNull(PARAM_BBOX);
		validateNotNull(PARAM_WIDTH);
		validateNotNull(PARAM_HEIGHT);
		validateNotNull(PARAM_FORMAT);

		if (getImageWidth() < 1)
			throw new OGCException(this, "Illegal " + PARAM_WIDTH + " value:'" + get(PARAM_WIDTH) + "'");
		if (getImageHeight() < 1)
			throw new OGCException(this, "Illegal " + PARAM_HEIGHT + " value:'" + get(PARAM_HEIGHT) + "'");
		validateBBox(PARAM_BBOX, false, false, false);
	}

	public void setWindowSize(DimI size) {
		set(PARAM_WIDTH, String.valueOf(size.w()));
		set(PARAM_HEIGHT, String.valueOf(size.h()));
	}

	@Override
	public boolean hasLayers() {
		return WMSUtil.hasArray(this, PARAM_LAYERS);
	}

	public void setBBox(Envelope env) {
		set(PARAM_BBOX, WMSUtil.toWMSBBox(env));
	}
}
