package com.sinergise.geopedia.db;

import org.outerj.daisy.htmlcleaner.HtmlCleaner;

import com.sinergise.common.util.property.Property;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.Field.FieldFlags;
import com.sinergise.geopedia.core.entities.GeomType;
import com.sinergise.geopedia.core.entities.properties.HTMLProperty;
import com.sinergise.geopedia.core.entities.properties.PropertyUtils;
import com.sinergise.geopedia.core.exceptions.FeatureDataException;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.util.GeopediaServerUtility;

public class FeatureUtils {

	/**
	 * 
	 * @param feature
	 * @param isAdmin    @deprecated REMOVE when HTML is changed to BBCode
	 * @throws GeopediaException
	 */
	public static void verifyFeature(Feature feature, boolean isAdmin) throws GeopediaException {

		
		if (GeomType.isGeom(feature.geomType) && feature.featureGeometry==null) {
			throw FeatureDataException.create(FeatureDataException.Type.MISSING_GEOMETRY);
		}
			
		Field[] fields = feature.fields;
		Property<?>[] properties = feature.properties;
		if (fields != null)
			for (int a = 0; a < fields.length; a++) {
				Field ff = fields[a];
				Property<?> prop = properties[a];

				if (PropertyUtils.isNull(prop)) {
					if (ff.hasFlag(FieldFlags.MANDATORY))
						throw FeatureDataException.create(FeatureDataException.Type.MISSING_MANDATORY_FIELD_VALUE, ff);

					continue;
				}

				switch (ff.type) {
				case BLOB:
					// XXX check for valid ID!
				case BOOLEAN:
				case DATE:
				case INTEGER:
				case DATETIME:
				case DECIMAL:				
					break; // can't be not ok :)
				case FOREIGN_ID: // will check later
					break;

				case PLAINTEXT:
				case LONGPLAINTEXT:
					break;

				case WIKITEXT:
					
					
					String wiki = ((HTMLProperty) prop).getRawHtml();
					if (wiki == null)
						return;

					if (GeopediaServerUtility.isRawHTML(wiki)) {
						if (!isAdmin && ff.getVisibility().canEdit()){//if it cant edit than nothing was changed, therefore it shouldnt fail.
							throw FeatureDataException.create(FeatureDataException.Type.ILLEGAL_FIELD_VALUE, ff);
						}
					} else {
						try {
							wiki = wiki.trim();
							if (wiki.length() > 0)
								wiki = HtmlCleaner.newDefaultInstance().cleanToString(wiki, false);
						} catch (Exception e) {
							throw FeatureDataException.create(FeatureDataException.Type.ILLEGAL_FIELD_VALUE, ff);
						}
					}
					if (wiki.length() > 65535)
						FeatureDataException.create(FeatureDataException.Type.ILLEGAL_FIELD_VALUE, ff);
					((HTMLProperty) prop).setRawHtml(wiki); 
					break;

				case STYLE:					
					break;
				default:
					FeatureDataException.create(FeatureDataException.Type.UNKNOWN_FIELD_TYPE, ff);
				}
			}
	}
}
