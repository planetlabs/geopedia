<%@page import="com.sinergise.geopedia.core.entities.properties.ForeignReferenceProperty"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.sinergise.geopedia.core.query.FeaturesQueryResults"%>
<%@page import="com.sinergise.geopedia.core.query.filter.FilterFactory"%>
<%@page import="com.sinergise.geopedia.core.query.Query"%>
<%@page import="com.sinergise.geopedia.core.entities.properties.HTMLProperty"%>
<%@page import="com.sinergise.common.util.property.TextProperty"%>
<%@page import="com.sinergise.common.util.property.LongProperty"%>
<%@page import="com.sinergise.common.util.property.DoubleProperty"%>
<%@page import="com.sinergise.common.util.property.DateProperty"%>
<%@page import="com.sinergise.geopedia.core.entities.properties.PropertyUtils"%>
<%@page import="com.sinergise.common.util.property.BooleanProperty"%>
<%@page import="com.sinergise.common.util.property.Property"%>
<%@page import="com.sinergise.common.geometry.crs.CartesianCRS"%>
<%@page import="com.sinergise.common.geometry.display.ScaleBounds"%>
<%@page import="com.sinergise.common.geometry.display.DisplayBounds"%>
<%@page import="com.sinergise.common.geometry.tiles.WithBounds"%>
<%@page import="com.sinergise.geopedia.ServerInstance"%>
<%@page import="com.sinergise.geopedia.server.ServUtil"%>
<%@page import="com.sinergise.geopedia.core.entities.utils.EntityConsts.DataScope"%>
<%@page import="com.sinergise.geopedia.core.config.IsURLProvider"%>
<%@page import="com.sinergise.common.gis.geopedia.GeopediaTiledCRS"%>
<%@page import="com.sinergise.geopedia.core.config.DatasetHostConfig"%>
<%@page import="com.sinergise.geopedia.app.ConfigurationUtils"%>
<%@page import="com.sinergise.common.geometry.display.ScaleLevelsSpec.ZoomLevelsPix"%>
<%@page import="com.sinergise.common.geometry.tiles.TiledCRS"%>
<%@page import="com.sinergise.common.geometry.display.DisplayCoordinateAdapter"%>
<%@page import="com.sinergise.common.util.geom.Envelope"%>
<%@page import="com.sinergise.common.geometry.crs.CRS"%>
<%@page import="com.sinergise.geopedia.core.constants.Globals"%>
<%@page import="com.sinergise.geopedia.core.entities.ThemeTableLink"%>
<%@page import="com.sinergise.geopedia.core.common.TileUtil"%>
<%@page import="com.sinergise.common.util.geom.DimI"%>

<%@page import="com.sinergise.java.util.format.JavaFormatProvider"%>
<%@page import="com.sinergise.common.util.format.Format"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="java.text.NumberFormat"%>
<%@page import="com.sinergise.geopedia.core.util.LinkUtils"%>
<%@page import="java.util.Date"%>
<%@page import="com.sinergise.common.util.format.Format"%>
<%@page import="com.sinergise.geopedia.core.entities.Field"%>
<%@page import="com.sinergise.geopedia.core.entities.Feature"%>
<%@page import="com.sinergise.geopedia.core.service.FeatureService"%>
<%@page
	import="com.sinergise.geopedia.server.service.FeatureServiceImpl"%>
<%@page import="com.sinergise.geopedia.app.session.Session"%>
<%@page import="com.sinergise.geopedia.core.entities.Table"%>
<%@page import="com.sinergise.geopedia.core.entities.Theme"%>
<%@page import="com.sinergise.geopedia.server.service.MetaServiceImpl"%>
<%@page import="com.sinergise.geopedia.core.entities.utils.EntityConsts"%>
<%@page import="com.sinergise.geopedia.client.core.ParametersProcessor"%>
<%@page import="java.util.HashMap"%>

<%
	HashMap<String, String> paramMap = new HashMap<String, String>();
	ParametersProcessor.parseParameters(paramMap,
	request.getParameter("params"));
	ServerInstance instance = ServUtil.getInstance(request);
	String body = "";
	String title = "";
	String description = "";

	if (paramMap.size() > 0) {
		try {
	JavaFormatProvider.init();
	WithBounds mainCRS = instance.getCRSSettings().getMainCRS();
	DisplayCoordinateAdapter dca = new DisplayCoordinateAdapter(
	new DisplayBounds(mainCRS.getBounds().mbr,  new ScaleBounds.InDisp(mainCRS.zoomLevels.minWorldPerPix(),mainCRS.zoomLevels.maxWorldPerPix())), 
	(CartesianCRS)mainCRS.baseCRS);
	dca.setDisplaySize(300,300);
	ZoomLevelsPix zlp = mainCRS.zoomLevels;
	dca.setPreferredZoomLevels(zlp);
	
	Envelope mbr=dca.bounds.mbr;
	if (mbr==null) {
		mbr=dca.worldCRS.bounds2D;
	}

	dca.setWorldCenterAndScale(mbr.getCenterX(), mbr.getCenterY(),
		dca.getPreferredZoomLevels().scale(9,dca.pixSizeInMicrons));
	
	int themeId = -1;
	int featureId = -1;
	int tableId = -1;

	if (paramMap.containsKey(EntityConsts.PREFIX_THEME)) {
		try {
	themeId = Integer.parseInt(paramMap
			.get(EntityConsts.PREFIX_THEME));
		} catch (NumberFormatException ex) {
		}
	}
	if (paramMap.containsKey(EntityConsts.PREFIX_LAYER)) {
		try {
	tableId = Integer.parseInt(paramMap
			.get(EntityConsts.PREFIX_LAYER));
		} catch (NumberFormatException ex) {
		}
	}
	if (paramMap.containsKey(EntityConsts.PREFIX_FEATURE)) {
		try {
	featureId = Integer.parseInt(paramMap
			.get(EntityConsts.PREFIX_FEATURE));
		} catch (NumberFormatException ex) {
		}
	}

	MetaServiceImpl metaService = null;
	Theme theme = null;
	Table table = null;

	if (themeId >= -1 || tableId >= -1) {
		metaService = new MetaServiceImpl(
		Session.DEFAULT_SESSION);
	}
	/* load theme */
	if (themeId >= 0) {
		theme = metaService.getThemeById(themeId, 0,
		DataScope.ALL);
		if (theme != null) {
	title += " - " + theme.getName();
	description = theme.descDisplayableHtml
			.replaceAll("\\<.*?\\>", "")
			.replaceAll("\n", "").replaceAll("\t", "");
	if (tableId < 0 && featureId < 0) {
		body += theme.descDisplayableHtml;
	}
		}
	}

	if (tableId >= 0) {
		table = metaService.getTableById(tableId, 0,
		DataScope.ALL);
		if (table != null) {
	title += " - " + table.getName();
	if (featureId < 0) {
		body += table.descDisplayableHtml;
	}
		}
	}

	if (featureId >= 0 && tableId >= 0) {
		FeatureServiceImpl featService = new FeatureServiceImpl(Session.DEFAULT_SESSION);
	   	
    	Query query = new Query();
		query.startIdx=0;
		query.stopIdx=1;
		query.options.add(Query.Options.FLDMETA_ENVLENCEN);
		query.options.add(Query.Options.FLDMETA_BASE);
		query.options.add(Query.Options.FLDUSER_ALL);
		query.tableId = tableId;
		query.dataTimestamp = 0;
		query.filter = FilterFactory.createIdentifierDescriptor(tableId, featureId);
		FeaturesQueryResults fqr = featService.executeQuery(query,instance);
		ArrayList<Feature> fa = fqr.getCollection();
		if (fa != null && fa.size() > 0) {
	Feature feature = fa.get(0);
	StringBuffer fiBuf = new StringBuffer();
	fiBuf.append("<table>");
	for (int i = 0; i < feature.fields.length; i++) {
		if (!feature.fields[i].getVisibility().canView())
			continue;		
		Property<?> property = feature.properties[i];
		Field fld = feature.fields[i];
		fiBuf.append("<tr>");
		fiBuf.append("<th align=\"left\">")
				.append(fld.getName()).append(":</th>");
		fiBuf.append("<td>");
		if (!PropertyUtils.isNull(property)) {
			switch (fld.type) {
			case BLOB:
				break; //TODO: show blobs
			case BOOLEAN:
				fiBuf.append(((BooleanProperty) property).getValue() ? "Da"
						: "Ne");
				break;
			case DATE:
				fiBuf.append((Format.date(((DateProperty) property).getValue())));
				break;
			case DATETIME:
				fiBuf.append(Format.dateTime(((DateProperty) property).getValue()));
				break;
			case DECIMAL:
				fiBuf.append(formatDecimal(
						getFormatString(fld),
						((DoubleProperty) property).toString()));
				break;
			case FOREIGN_ID:
				ForeignReferenceProperty frp = (ForeignReferenceProperty)property;
				if (frp.getReptext()!=null)
					fiBuf.append(frp.getReptext());
				else
					fiBuf.append(frp.getValue());
				break;
			case INTEGER:
				fiBuf.append(String
						.valueOf(((LongProperty) property).getValue()));
				break;
			case LONGPLAINTEXT:
			case PLAINTEXT:
				fiBuf.append(LinkUtils
						.generateHyperlinks(((TextProperty) property).getValue())); // XXX
				break;
			case WIKITEXT:
				if (((HTMLProperty) property).getValue() != null)
					fiBuf.append(((HTMLProperty) property).getValue()
							.replaceAll(
									"\\*\\*RAWHTML\\*\\*",
									""));
				break;
			}
		}
		fiBuf.append("</td>");
		fiBuf.append("</tr>");
	}

	if (feature.envelope != null) {
		Envelope env = feature.envelope;
		dca.setDisplayedRect(env.minX, env.minY, env.maxX,
				env.maxY);
		if (env.isPoint()) {
			dca.setScale(dca.getPreferredZoomLevels().scale(16,dca.pixSizeInMicrons));
		}
	}
	fiBuf.append("</table>");

	body += fiBuf.toString();
		}

	}

	if (paramMap.containsKey(EntityConsts.PARAM_X)
	&& paramMap.containsKey(EntityConsts.PARAM_Y)) {
		try {
	dca.worldCenterX = Double.parseDouble(paramMap.get(
			EntityConsts.PARAM_X).toString());
	dca.worldCenterY = Double.parseDouble(paramMap.get(
			EntityConsts.PARAM_Y).toString());
		} catch (NumberFormatException nfe) {
	nfe.printStackTrace();
		}
	}
	if (paramMap.containsKey(EntityConsts.PARAM_SCALE)) {
		try {
	int zoomLevel = Integer.parseInt(paramMap.get(
			EntityConsts.PARAM_SCALE).toString());
	dca.setScale(dca.getPreferredZoomLevels().scale(zoomLevel,dca.pixSizeInMicrons));					
		} catch (NumberFormatException nfe) {
	nfe.printStackTrace();
		}
	}

	int[] baseLayers = ParametersProcessor
	.parseBaseLayers(paramMap
			.get(EntityConsts.PARAM_BASELAYERS));
	body += "<img src=\""
	+ createMapURL(theme, table, baseLayers, dca, instance)
	+ "\"/>";

		} catch (Throwable th) {
	th.printStackTrace();
		}
	}
	if (title.length() == 0) {
		title = "Geopedia - interaktivni spletni atlas in zemljevid Slovenije";
	} else {
		title = "Geopedia " + title;
	}

	if (description.length() == 0) {
		description = "Geopedia je interaktivni spletni atlas in zemljevid Slovenije, v katerem se nahajajo informacije o lokacijah sportnih ktivnosti, kulture in zabave, zdravstvenih in izobrazevalnih ustanovah";
	}
%>
<%!private static String getFormatString(Field f) {
		if (f == null || f.properties == null)
			return null;
		return f.properties.getString(Field.PROP_FORMAT_STRING, null);
	}

	private static String formatDecimal(String formatString, String text) {
		if (formatString == null)
			return text;
		try {
			NumberFormat nf = new DecimalFormat(formatString);
			double dec = Format.readDecimal(text);
			return nf.format(dec);
		} catch (Exception e) {
			return text;
		}
	}

	private static String createMapURL(Theme theme, Table table,
			int[] baseLayers, DisplayCoordinateAdapter dca, ServerInstance instance) {
		IsURLProvider prConf = instance.getCommonConfiguration().publicRenderers;

		StringBuffer buf = new StringBuffer();

		buf.append(prConf.getBaseURL());		
		buf.append(Globals.REQ_PRINT);
		buf.append('/');
		if (theme != null) {
			TileUtil.appendThemeLayers(theme, buf);
		}
		if (table != null) {
			TileUtil.appendLayerSpec(table, ThemeTableLink.ALL_ON, buf);
		}

		buf.append('?');

		buf.append("x=");
		buf.append(Math.round(dca.worldCenterX));
		buf.append("&y=");
		buf.append(Math.round(dca.worldCenterY));
		buf.append("&s=");
		
		int zoomLevel = dca.getPreferredZoomLevels().nearestZoomLevel(dca.getScale(), dca.pixSizeInMicrons);
		TiledCRS tiledCRS= new GeopediaTiledCRS("Geopedia tiles", 20); //TODO: make configurable		

		buf.append(TileUtil.tileLevelCharFromZoomLevel(tiledCRS,
				zoomLevel));
		buf.append("&w=300&h=300");
		buf.append("&type=image/jpeg");
		if (baseLayers != null) {
			buf.append("&b=");
			for (int i = 0; i < baseLayers.length; i++) {
				if (i > 0)
					buf.append(EntityConsts.BASELAYERS_SEPARATOR);
				buf.append(baseLayers[i]);
			}
		}

		return buf.toString();
	}%>