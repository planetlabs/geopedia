package com.sinergise.geopedia.client.ui.feature;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.format.DateFormatUtil;
import com.sinergise.common.util.format.DateFormatter;
import com.sinergise.common.util.format.Format;
import com.sinergise.common.util.property.BooleanProperty;
import com.sinergise.common.util.property.DateProperty;
import com.sinergise.common.util.property.DoubleProperty;
import com.sinergise.common.util.property.LongProperty;
import com.sinergise.common.util.property.Property;
import com.sinergise.common.util.property.TextProperty;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.ui.feature.PictureDisplayer.PictureProvider;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.properties.BinaryFileProperty;
import com.sinergise.geopedia.core.entities.properties.ForeignReferenceProperty;
import com.sinergise.geopedia.core.entities.properties.HTMLProperty;
import com.sinergise.gwt.ui.maingui.StandardUIConstants;

public class FeatWidgetFactory {
	
//	public static DateFormatter FORMATTER_DATETIME = DateFormatUtil.create(DateFormatUtil.getDefaultDateTimePattern());

	
	public static Widget createView(Field f, Property<?> vh) {

		if (f.linkToWidget()) {
			return linkToWidget(f, vh);
		}

		return createWidget(f, vh);
	}

	private static Widget linkToWidget(Field f, Property<?> vh) {

		final Widget widget = createWidget(f, vh);
		return WidgetLinker.link(widget, f.getName());
	}

	private static Widget createWidget(Field f,final Property<?> vh) {

		if (vh == null)
			return new InlineLabel("");
		switch (f.type) {
		case BLOB:
			PictureDisplayer pictureDisplayer = new PictureDisplayer();
			if (vh!=null) {
			pictureDisplayer.setPictureProvider(new PictureProvider() {
				
				@Override
				public String getPictureUrl(int w, int h) {
					BinaryFileProperty vhBinary = ((BinaryFileProperty)vh);
					return ImageURL.createUrl(vhBinary.getValue(),w,h);
				}
			});
			}
			return pictureDisplayer;
		case BOOLEAN:
			return new InlineLabel(
					((BooleanProperty) vh).getValue() ? StandardUIConstants.STANDARD_CONSTANTS
							.buttonYes() : StandardUIConstants.STANDARD_CONSTANTS
							.buttonNo());// "Da" //"Ne"
		case DATE:
			Date dte = ((DateProperty) vh).getValue();
			return new InlineLabel(ClientGlobals.FORMAT_DATE.format(dte));
		case DATETIME:
			return new InlineLabel(ClientGlobals.FORMAT_DATETIME.format(((DateProperty) vh).getValue()));
		case DECIMAL:
			Double value = ((DoubleProperty)vh).getValue();
			return new InlineLabel(formatDecimal(getFormatString(f), String.valueOf(value)));
		case FOREIGN_ID:
			return new RepTextLabel(f.refdTableId,(ForeignReferenceProperty)vh);
		case INTEGER:
			return new InlineLabel(String.valueOf(((LongProperty) vh).getValue()));
		case LONGPLAINTEXT:
			return new InlineHTML(
					TranslatedLinkUtils
							.generateHyperlinks(((TextProperty) vh).getValue())); // XXX
		case PLAINTEXT:
			return new InlineHTML(
					TranslatedLinkUtils
							.generateHyperlinks(((TextProperty) vh).getValue()));
		case STYLE:
			return new Label(((TextProperty)vh).getValue());
		case WIKITEXT:			
			InlineHTML res = new InlineHTML(((HTMLProperty)vh).getValue());
			res.setStyleName("details");
			return res;
		default:
			throw new IllegalStateException();
		}
	}

	private static String formatDecimal(String formatString, String text) {
		if (formatString == null)
			return text;
		try {
			NumberFormat nf = NumberFormat.getFormat(formatString);
			double dec = Format.readDecimal(text);
			return nf.format(dec);
		} catch (Exception e) {
			return text;
		}
	}

	private static String getFormatString(Field f) {
		if (f == null || f.properties == null)
			return null;
		return f.properties.getString(Field.PROP_FORMAT_STRING, null);
	}

}
