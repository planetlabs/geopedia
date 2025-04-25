package com.sinergise.common.gis.map.print;

import com.sinergise.common.util.web.MimeType;

public enum PrintOutputFormat {
	PDF {
		@Override
		public MimeType getMimeType() {
			return MimeType.MIME_DOCUMENT_PDF;
		}
		@Override
		public String getLabel() {
			return "PDF";
		}
	},
	JPEG {
		@Override
		public MimeType getMimeType() {
			return MimeType.MIME_IMAGE_JPG;
		}
		
		@Override
		public String getLabel() {
			return "JPEG";
		}
	};

	public abstract MimeType getMimeType();

	public abstract String getLabel();
	
	public static final PrintOutputFormat valueOf(MimeType format) {
		for (PrintOutputFormat f : values()) {
			if (f.getMimeType().equals(format)) {
				return f;
			}
		}
		throw new IllegalArgumentException("Unsupported format in PrintOutputFormat: "+format.createContentTypeString());
	}
}
