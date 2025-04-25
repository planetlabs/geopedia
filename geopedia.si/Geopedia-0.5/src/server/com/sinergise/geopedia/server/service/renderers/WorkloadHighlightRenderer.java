package com.sinergise.geopedia.server.service.renderers;

import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.ThemeTableLink;
import com.sinergise.geopedia.core.filter.AbstractFilter;
import com.sinergise.geopedia.core.filter.ListFilter;
import com.sinergise.geopedia.core.style.model.SymbolId;
import com.sinergise.geopedia.core.symbology.PaintingPass;
import com.sinergise.geopedia.core.symbology.Symbolizer;
import com.sinergise.geopedia.db.FilterBuilders;
import com.sinergise.geopedia.db.expressions.QueryBuilderNew;
import com.sinergise.geopedia.server.AbstractWorkload;
import com.sinergise.geopedia.server.HighlightWorkload;
import com.sinergise.geopedia.server.HighlightWorkload.HighlightWorkloadLayer;
import com.sinergise.geopedia.style.symbology.rhino.FillSymbolizerImpl;
import com.sinergise.geopedia.style.symbology.rhino.JavaSymbologyUtils;
import com.sinergise.geopedia.style.symbology.rhino.LineSymbolizerImpl;
import com.sinergise.geopedia.style.symbology.rhino.PointSymbolizerImpl;

public class WorkloadHighlightRenderer  extends WorkloadRenderer {
	
	private static String pointStyle;
	private static String lineStyle;
	private static String polygonStyle;
	
	static {
		JavaSymbologyUtils jsu = new JavaSymbologyUtils();
		// points
		PointSymbolizerImpl psi = (PointSymbolizerImpl) jsu.createPointSymbolizer();
		psi.setSymbolId(SymbolId.CIRCLE);
		psi.setSize(SymbolId.HIGHLIGHTED_POINT_SIZE);		
		psi.setFill(new Long(0xFFFF0000).doubleValue());
		pointStyle = jsu.toJavaScript(jsu.createSymbology(new PaintingPass[]{jsu.createPaintingPass(new Symbolizer[]{psi})}));
		// lines		
		LineSymbolizerImpl lsi = (LineSymbolizerImpl) jsu.createLineSymbolizer();
		lsi.setStroke(new Long(0xFFFF0000).doubleValue());
		lsi.setStrokeWidth(4);
		lsi.setLineType("solid");
		lineStyle = jsu.toJavaScript(jsu.createSymbology(new PaintingPass[]{jsu.createPaintingPass(new Symbolizer[]{lsi})}));
		// polygons
		FillSymbolizerImpl fsi = (FillSymbolizerImpl)jsu.createFillSymbolizer();
		fsi.setFill(new Long(0x40FF0000).doubleValue());
		fsi.setFillBackground(new Long(0x40FF0000).doubleValue());
		fsi.setFillType("solid");
		lsi.setStrokeWidth(3);
		polygonStyle = jsu.toJavaScript(jsu.createSymbology(new PaintingPass[]{
				jsu.createPaintingPass(new Symbolizer[]{fsi,lsi})}));
		
	}


	public WorkloadHighlightRenderer(AbstractWorkload workload, Session session, ServerInstance instance) {
		super(workload, session, instance);
	}
	
	// override default styles with highlight styles
	@Override
	protected String getTableStyle(ThemeTableLink ttl, Table table) {
		if (table.getGeometryType().isPoint()) {
			return pointStyle;
		} else if(table.getGeometryType().isLine()) {
			return lineStyle;
		} else if (table.getGeometryType().isPolygon()) {
			return polygonStyle;
		}
		return null;
	}
	
	
	 // Only draw selected features		 
	@Override
	protected String filtersToSQL(AbstractFilter[] filters, Table table, QueryBuilderNew qb) {
		HighlightWorkload hWorkload = (HighlightWorkload)workload;
		String ids = StringUtil.arrayToString(((HighlightWorkloadLayer)hWorkload.layers[pkgInProcessIdx]).featureIds,",");
		if (ids==null) return "";
		return " AND "+FilterBuilders.toSQL(new ListFilter(table.getId(), ids), qb);
	}
}