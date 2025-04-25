package com.sinergise.common.gis.map.print;

public interface ProvidesPrintTemplates {
	public static final String CAPABILITY_PROVIDES_PRINT_TEMPLATES = "ProvidesPrintTemplates";
	TemplateSpec[] getTemplates();
}
