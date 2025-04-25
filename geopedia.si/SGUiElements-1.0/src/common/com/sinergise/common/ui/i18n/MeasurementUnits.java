package com.sinergise.common.ui.i18n;

import com.google.gwt.i18n.client.ConstantsWithLookup;

/**
 * @author tcerovski
 *
 */
public interface MeasurementUnits extends ConstantsWithLookup {
	
	public static final MeasurementUnits INSTANCE = ResourceUtil.create(MeasurementUnits.class);

	@DefaultStringValue("metre")
	String METRE();
	@DefaultStringValue("inche")
	String INCH();
	@DefaultStringValue("foot")
	String FOOT();
	@DefaultStringValue("yard")
	String YARD();
	@DefaultStringValue("mile")
	String MILE();
	@DefaultStringValue("nautical mile")
	String NAUTICAL_MILE();
	@DefaultStringValue("pied du roi")
	String PIED_DU_ROI();
	@DefaultStringValue("fathom")
	String FATHOM();
	@DefaultStringValue("hvat")
	String HVAT();
	
	@DefaultStringValue("square metre")
	String SQ_METRE();
	@DefaultStringValue("square foot")
	String SQ_FOOT();
	@DefaultStringValue("are")
	String ARE();
	@DefaultStringValue("hectare")
	String HECTARE();
	@DefaultStringValue("square kilometre")
	String SQ_KM();
	@DefaultStringValue("acre")
	String ACRE();
	@DefaultStringValue("square pied")
	String PIED_CARRE();
	@DefaultStringValue("square toise")
	String TOISE_CARRE();
	@DefaultStringValue("square perche")
	String PERCHE_CARREE();
	@DefaultStringValue("square arpent")
	String ARPENT_CARRE();
	
	@DefaultStringValue("square hvat")
	String SQ_HVAT();
	@DefaultStringValue("katastrsko jutro")
	String KATASTRSKO_JUTRO();
	@DefaultStringValue("ral")
	String RAL();
	@DefaultStringValue("motika zemlje")
	String MOTIKA_ZEMLJE();
	@DefaultStringValue("dan oranja")
	String DAN_ORANJA();
	@DefaultStringValue("lanac")
	String LANAC();
	@DefaultStringValue("dulum")
	String DULUM();

}
