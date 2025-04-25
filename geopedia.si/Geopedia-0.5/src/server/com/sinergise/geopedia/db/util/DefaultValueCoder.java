package com.sinergise.geopedia.db.util;

import java.util.GregorianCalendar;

import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.geopedia.core.entities.Field.FieldType;
import com.sinergise.geopedia.db.entities.MetaData;
import com.sinergise.geopedia.style.StyleVerifier;

public class DefaultValueCoder
{
	public static boolean isValidDefaultValue(FieldType fieldType, String defValueString, TiledCRS tiledCRS, MetaData meta)
	{
		if (defValueString == null)
			return true;
		
		switch(fieldType) {
		case BLOB:
			return false;
			
		case BOOLEAN:
			return "0".equals(defValueString) || "1".equals(defValueString);
			
		case DATE:
		case DATETIME:
			try {
				long time = Long.parseLong(defValueString);
				GregorianCalendar gc = new GregorianCalendar();
				gc.setTimeInMillis(time);
				int y = gc.get(GregorianCalendar.YEAR);
				return y >= 1600 && y <= 2100;
			} catch (NumberFormatException e) {
				return false;
			}
			
		case DECIMAL:
			try {
				Double.parseDouble(defValueString);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
			
		case FOREIGN_ID:
			return false;
			
		case INTEGER:
			try {
				Long.parseLong(defValueString);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
			
		case LONGPLAINTEXT:
			return defValueString.length() < 65536;
			
		case PLAINTEXT:
			return defValueString.length() < 256;
			
		case STYLE:
			return StyleVerifier.isValid(defValueString, tiledCRS, meta) == null;
			
		case WIKITEXT:
			return defValueString.length() < 65536; // TODO: check wiki syntax?
		
		default:
			throw new IllegalStateException("unknown field type");
		}
	}
}
