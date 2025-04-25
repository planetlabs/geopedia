package com.sinergise.geopedia.pro.client.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface ProMessages extends Messages {
	public static final ProMessages INSTANCE = (ProMessages) GWT.create(ProMessages.class);
	
	
	@DefaultMessage("Warning! The geometry contains large number  ({0}) of nodes. Editing may slow down your browser. Do you really wish to edit geometry?")
	String geometryIsBigWarning(int numNodes);
	@DefaultMessage("The value of selected field will be used as representation for this feature.")
	String fieldsHelp();
	//TODO: write actual meaning of codelist
	@DefaultMessage("<missing>")
	String codelistHelp();
	
	@DefaultMessage("To add point click on the map. To pan on the map, click and drag map. " +
			"To continue drawing existing line select final point and continue with drawing the line. " +
			"<a href=\"http://portal.geopedia.si/navodila/zapisi#helpGeometry\" target=\"_blank\">Read more ...</a>")
	String featureHelp();
	
	@DefaultMessage("Wrong geometry, only one point is allowed!")
	String geometryErrorOnlyPoint();
	@DefaultMessage("Wrong geometry, only one line is allowed!")
	String geometryErrorOneLine();
	@DefaultMessage("Wrong geometry, only lines are allowed!")
	String geometryErrorMultiLine();
	@DefaultMessage("Wrong geometry, only polygon is allowed!")
	String geometryErrorOnlyPolygon();
	@DefaultMessage("Wrong geometry, only polygons are allowed!")
	String geometryErrorMultiPolygon();
	@DefaultMessage("Wrong geometry, polygons are not allowed!")
	String geometryErrorPolygonsAreNotAllowedForThisLayer();
	@DefaultMessage("Wrong geometry, lines are not allowed!")
	String geometryErrorLinesAreNotAllowedForThisLayer();
	@DefaultMessage("Topology error!")
	String geometryErrorTopology();
	
	@DefaultMessage("Code is correct!")
	String codeEditorAllOk();
	
	@DefaultMessage("The contour lines may not be complete, because they are drawn in a fixed envelope. ")
	String contourInFixedEnvelope();
}
