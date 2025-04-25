package com.sinergise.common.util.web;

import static com.sinergise.common.util.format.DateFormatUtil.RFC_1123_DATETIME_PATTERN_GMT;

import java.net.URI;
import java.util.Date;

import com.sinergise.common.util.collections.safe.StringTypedMapKey;
import com.sinergise.common.util.format.DateFormatUtil;
import com.sinergise.common.util.format.DateFormatter;
import com.sinergise.common.util.format.SGDateTimeConstants;
import com.sinergise.common.util.string.StringTransformUtil;
import com.sinergise.common.util.string.StringTransformer;

public class HttpHeaders {
	public static class HttpHeaderFieldSpec<T> extends StringTypedMapKey<T> {
		private static final long serialVersionUID = 1L;
		
		public static final <T> HttpHeaderFieldSpec<T> create(String name, Class<T> klass) {
			return new HttpHeaderFieldSpec<T>(name, klass);
		}
		
		private StringTransformer<T> tr = null;
		
		public HttpHeaderFieldSpec(String fieldName) {
			this(fieldName, null);
		}
		
		public HttpHeaderFieldSpec(String fieldName, Class<? extends T> klass) {
			super(fieldName);
			if (klass != null) {
				this.tr = StringTransformUtil.getTransformer(klass);
			}
		}

		@Override
		public String write(T value) {
			if (tr != null) {
				return tr.store(value);
			}
			return value == null ? null : String.valueOf(value);
		}
		
		@Override
		public T read(String valueString) {
			if (tr != null) {
				return tr.valueOf(valueString);
			}
			throw new UnsupportedOperationException();
		}
	}

	public static class HttpHeaderField<T> {
		HttpHeaderFieldSpec<T> field;
		T value;
	}

	public static class StringField extends HttpHeaderFieldSpec<String> {
		private static final long serialVersionUID = 1L;

		public StringField(String fieldName) {
			super(fieldName);
		}

		@Override
		public String read(String valueString) {
			return valueString;
		}
		
		@Override
		public String write(String value) {
			return value;
		}
	}
	
	private static DateFormatter rfc1123 = DateFormatUtil.create(RFC_1123_DATETIME_PATTERN_GMT, SGDateTimeConstants.DEFAULTS_EN);
	
	public static String formatDateForHttpHeader(Date value) {
		return rfc1123.formatDate(value, 0);
	}
	
	public static Date readDateFromHttpHeaderString(String value) {
		try {
			return rfc1123.parse(value, 0);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static class DateField extends HttpHeaderFieldSpec<Date> {
		private static final long serialVersionUID = 1L;

		public DateField(String fieldName) {
			super(fieldName);
		}

		@Override
		public String write(Date value) {
			return formatDateForHttpHeader(value);
		}

		@Override
		public Date read(String strVal) {
			return readDateFromHttpHeaderString(strVal);
		}
	}

	public static final StringField FIELD_USER_AGENT = new StringField("User-Agent");
	public static final StringField FIELD_VIA = new StringField("Via");
	public static final HttpHeaderFieldSpec<URI> FIELD_REFERER = HttpHeaderFieldSpec.create("Referer", URI.class);

	public static final DateField FIELD_DATE = new DateField("Date");
	public static final DateField FIELD_EXPIRES = new DateField("Expires");
	public static final StringField FIELD_AGE = new StringField("Age");
	public static final StringField FIELD_VARY = new StringField("Vary");
	public static final DateField FIELD_LAST_MODIFIED = new DateField("Last-Modified");
	public static final StringField FIELD_ETAG = new StringField("ETag");

	public static final StringField FIELD_CACHE_CONTROL = new StringField("Cache-Control");
	
	public static final HttpHeaderFieldSpec<MimeType> FIELD_CONTENT_TYPE = HttpHeaderFieldSpec.create("Content-Type", MimeType.class);
	
	public static final StringField FIELD_CONTENT_DISPOSITION = new StringField("Content-Disposition");
	public static final HttpHeaderFieldSpec<Long> FIELD_CONTENT_LENGTH = HttpHeaderFieldSpec.create("Content-Length", Long.class);
	
	public static final StringField FIELD_ACCEPT = new StringField("Accept");
	public static final StringField FIELD_ACCEPT_CHARSET = new StringField("Accept-Charset");
	public static final StringField FIELD_ACCEPT_ENCODING = new StringField("Accept-Encoding");
	public static final StringField FIELD_ACCEPT_LANGUAGE = new StringField("Accept-Language");

	public static final StringField FIELD_PRAGMA = new StringField("Pragma");
	
	public static final StringField FIELD_SET_COOKIE = new StringField("Set-Cookie");
}
