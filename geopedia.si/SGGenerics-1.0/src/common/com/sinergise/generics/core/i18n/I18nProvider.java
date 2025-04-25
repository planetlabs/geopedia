package com.sinergise.generics.core.i18n;

public interface I18nProvider {
	/**
	 * Returns translation of attributeValue if there's a translation or if attribute is translatable. Otherwise it returns original attributeValue.
	 * 
	 * @param name  - if processing named node this will contain node name. May be null.
	 * @param attributeName - node attribute name. May not be null.
	 * @param attributeValue - node attribute value. May be null or empty string.
	 * @return
	 */
	public String getAttributeTranslation(String attributeName, String attributeValue);
}
