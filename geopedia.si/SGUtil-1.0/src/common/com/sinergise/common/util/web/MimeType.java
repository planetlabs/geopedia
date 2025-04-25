/*
 *
 */
package com.sinergise.common.util.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.string.Escaper;

/**
 * <a href="http://www.w3.org/Protocols/rfc1341/4_Content-Type.html">RFC1341</a>
 * 
 * @author <a href="mailto:miha.kadunc@cosylab.com">Miha Kadunc</a>
 */
public class MimeType implements Serializable {
	private static final long serialVersionUID = 5087604708620259728L;
	
	// ---------------
	// ESCAPER
	// ---------------
	static final char         quote            = '"';
	static final char[]       tspecials        = new char[]{'(', ')', '<', '>', '@', ',', ';', ':', '/', '[', ']', '?', '.', '='};
	static {
		Arrays.sort(MimeType.tspecials);
	}
	static final char         esc              = '\\';
	static final Escaper      MY_ESCAPER       = new Escaper(MimeType.tspecials, new char[]{MimeType.quote}, MimeType.esc);
	
	public static class MimeParamValue implements Comparable<MimeParamValue>, Serializable {
		private static final long serialVersionUID = 7357490831882317539L;
		public String       name;
		public  String       value;
		// GWT serialization
		@Deprecated
		protected MimeParamValue() {
			
		}
		public MimeParamValue(final String paramNameVal) {
			final int eqIdx = MimeType.MY_ESCAPER.specialCharIndex(paramNameVal, '=');
			name = MimeType.MY_ESCAPER.unescapeComponent(paramNameVal.substring(0, eqIdx)).toLowerCase();
			if (MimeType.checkForSpecials(name)) {
				throw new IllegalArgumentException("Parameter name includes special characters " + name);
			}
			value = MimeType.MY_ESCAPER.unescapeComponent(paramNameVal.substring(eqIdx + 1));
		}
		
		public MimeParamValue(final String paramName, final String paramValue) {
			name = paramName.toLowerCase();
			if (MimeType.checkForSpecials(name)) {
				throw new IllegalArgumentException("Parameter name includes special characters " + paramName);
			}
			value = paramValue;
		}
		
		protected void appendTo(final StringBuffer buf) {
			buf.append(';');
			buf.append(name);
			buf.append('=');
			buf.append(MimeType.MY_ESCAPER.escapeComponent(value));
		}
		
		@Override
		public int compareTo(final MimeParamValue o) {
			final MimeParamValue mo = o;
			final int ret = name.compareTo(mo.name);
			if (ret != 0) {
				return ret;
			}
			return value.compareTo(mo.value);
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (!(obj instanceof MimeParamValue)) {
				return false;
			}
			return compareTo((MimeParamValue)obj) == 0;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}
	}
	
	// ---------------
	// MAIN TYPES
	// ---------------
	public static final String                     MAINTYPE_APPLICATION = "application";
	public static final String                     MAINTYPE_AUDIO       = "audio";
	public static final String                     MAINTYPE_IMAGE       = "image";
	public static final String                     MAINTYPE_MESSAGE     = "message";
	public static final String                     MAINTYPE_MULTIPART   = "multipart";
	public static final String                     MAINTYPE_TEXT        = "text";
	public static final String                     MAINTYPE_VIDEO       = "video";
	public static final String                     MAINTYPE_MODEL       = "model";
	
	// ---------------
	// SUB-TYPES
	// ---------------
	public static final String                     SUFFIX_SYNTAX_XML    = "+xml";
	
	// -------------------
	// MIME TYPES CACHE
	// -------------------
	private static final HashMap<String, MimeType> mappings             = new HashMap<String, MimeType>();
	
	// ------------------------
	// PRE-DEFINED MIME TYPES
	// ------------------------
	public static final MimeType                   MIME_DOCUMENT_PDF    = new MimeType(MimeType.MAINTYPE_APPLICATION, "pdf");
	public static final MimeType                   MIME_HTML            = new MimeType(MimeType.MAINTYPE_TEXT, "html").addExtension("htm");
	
	public static final MimeType                   MIME_IMAGE       = new MimeType(MimeType.MAINTYPE_IMAGE, null);

	public static final MimeType                   MIME_IMAGE_GIF       = new MimeType(MimeType.MAINTYPE_IMAGE, "gif");
	public static final MimeType                   MIME_IMAGE_JPG       = new MimeType(MimeType.MAINTYPE_IMAGE, "jpeg", "jpg").addExtension("jpeg");
	public static final MimeType                   MIME_IMAGE_PNG       = new MimeType(MimeType.MAINTYPE_IMAGE, "png");
	public static final MimeType                   MIME_IMAGE_SVG       = new MimeType(MimeType.MAINTYPE_IMAGE, "svg" + MimeType.SUFFIX_SYNTAX_XML, "svg");
	public static final MimeType                   MIME_IMAGE_TIF       = new MimeType(MimeType.MAINTYPE_IMAGE, "tiff", "tif").addExtension("tiff");
	/** WebCGM is a profile of ISO/IEC 8632 **/
	public static final MimeType                   MIME_IMAGE_WEBCGM    = new MimeType(MimeType.MAINTYPE_IMAGE, "cgm", new MimeParamValue[]{
	        new MimeParamValue("Version", "4"), new MimeParamValue("ProfileId", "WebCGM")}, "cgm");
	
	public static final MimeType                   MIME_JAVA_OBJECT     = new MimeType(MimeType.MAINTYPE_APPLICATION, "x-java-object");
	public static final MimeType                   MIME_PLAIN_TEXT      = new MimeType(MimeType.MAINTYPE_TEXT, "plain", "txt");
	public static final MimeType                   MIME_DIRECTORY      = new MimeType(MimeType.MAINTYPE_TEXT, "directory", "");
	public static final MimeType                   MIME_XML             = new MimeType(MimeType.MAINTYPE_APPLICATION, "xml");
	public static final MimeType				   MIME_GML 			= new MimeType(MimeType.MAINTYPE_APPLICATION, "vnd.ogc.gml").addExtension("gml");
	
	public static final MimeType				   MIME_VRML 			= new MimeType(MimeType.MAINTYPE_MODEL, "vrml").addExtension("wrl");
	
	public static final MimeType				   MIME_ZIP				= new MimeType(MimeType.MAINTYPE_APPLICATION, "zip", "zip"); 
	public static final MimeType                   MIME_DOCUMENT_DOC    = new MimeType(MimeType.MAINTYPE_APPLICATION, "msword", "doc");
	public static final MimeType                   MIME_DOCUMENT_DOCX   = 
		new MimeType(MimeType.MAINTYPE_APPLICATION,	"vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");
	
	public static final MimeType		MIME_CSV						= 
		new MimeType(MimeType.MAINTYPE_TEXT, "comma-separated-values", "csv")
	.setDescription("Comma-Separated Variables");
	
	
	public static final MimeType        MIME_DOCUMENT_XLS    = new MimeType(MimeType.MAINTYPE_APPLICATION, "vnd.ms-excel", "xls");
	public static final MimeType		MIME_OPENDOCUMENT_SPREADSHEET			= 
		new MimeType(MimeType.MAINTYPE_APPLICATION, "vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx")
	.setDescription("Microsoft Excel");
	
	public static final MimeType MIME_ZIPPED_ESRI_SHP = 
		new MimeType(MimeType.MAINTYPE_APPLICATION, "x-esri-shape", "zip")
	.setDescription("Zipped ESRI shapefile");
	
	public static final MimeType MIME_GPX = 
		new MimeType(MimeType.MAINTYPE_APPLICATION, "gpx+xml", "gpx")
	.setDescription("GPs eXange format");
	
	public static final MimeType MIME_DWG = new MimeType(MimeType.MAINTYPE_APPLICATION, "octet-stream", "dwg");
	public static final MimeType MIME_DFX = new MimeType(MimeType.MAINTYPE_APPLICATION, "octet-stream", "dxf");
	
	// --------------------
	// PRE-DEFINED PARAMS
	// --------------------
	public static final String                     PARAM_CSL_OBJECT_TYP = "type";
	
	static {
		MimeType.registerType("image/pjpeg", MimeType.MIME_IMAGE_JPG);
	}
	static {
		MimeType.registerType("image/x-png", MimeType.MIME_IMAGE_PNG);
	}
	static {
		MimeType.registerType("image/x-tiff", MimeType.MIME_IMAGE_JPG);
	}
	
	static {
		// text/xml is equivalent to application/xml;charset=us-ascii
		MimeType.registerType("text/xml", new MimeType(MimeType.MIME_XML, new MimeParamValue[]{new MimeParamValue("charset", "us-ascii")}));
	}
	
	// -----------------------
	// STATIC METHODS
	// -----------------------
	
	static final boolean checkForSpecials(final String str) {
		for (int i = 0; i < str.length(); i++) {
			final char c = str.charAt(i);
			if (Arrays.binarySearch(MimeType.tspecials, c) >= 0) {
				return true;
			}
		}
		return false;
	}
	
	public static final MimeType getForFileExtension(String fileSuffix) {
		if (!fileSuffix.startsWith(".")) {
			fileSuffix = fileSuffix.substring(fileSuffix.lastIndexOf('.')+1);
		}
		for (MimeType mime : mappings.values()) {
			if (mime.getDefaultFileExtension().equalsIgnoreCase(fileSuffix)) {
				return mime;
			}
		}
		for (MimeType mime : mappings.values()) {
			if (mime.isCandidateFileExtension(fileSuffix)) {
				return mime;
			}
		}
		return null;
	}
	
	private boolean isCandidateFileExtension(String fileSuffix) {
		if (defaultExtension != null && defaultExtension.equalsIgnoreCase(fileSuffix)) {
			return true;
		}
		return false;
	}

	public static final MimeType constructMimeType(final String mimeString) {
		if (mimeString == null) {
			return null;
		}
		final MimeType ret = MimeType.findReferenceMime(mimeString);
		if (ret != null) {
			return ret;
		}
		final String main = MimeType.extractMainType(mimeString);
		final String sub = MimeType.extractSubType(mimeString);
		final MimeParamValue[] params = MimeType.extractParams(mimeString);
		return new MimeType(main, sub, params, null);
	}
	
	static final MimeParamValue[] createParamsArray(final String[] parameterNames, final String[] parameterValues) {
		if (parameterNames == null) {
			throw new IllegalArgumentException("parameterNames should not be null");
		}
		final MimeParamValue[] ret = new MimeParamValue[parameterNames.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = new MimeParamValue(parameterNames[i], parameterValues[i]);
		}
		return ret;
	}
	
	public static final MimeType deriveWithParameter(final MimeType baseMime, final String paramName, final String paramValue) {
		
		if (baseMime.params == null) {
			return new MimeType(baseMime, new MimeParamValue[]{new MimeParamValue(paramName, paramValue)});
		}
		
		final MimeParamValue[] params = new MimeParamValue[baseMime.params.length + 1];
		for (int i = 0; i < params.length - 1; i++) {
			params[i] = baseMime.params[i];
		}
		params[params.length - 1] = new MimeParamValue(paramName, paramValue);
		return new MimeType(baseMime, params);
	}
	
	public static final String extractMainType(final String fullMime) {
		final int slashIdx = fullMime.indexOf('/');
		if (slashIdx < 0) {
			return fullMime;
		}
		return fullMime.substring(0, slashIdx);
	}
	
	public static final MimeParamValue[] extractParams(final String fullMime) {
		ArrayList<MimeParamValue> ret = null;
		int startIdx = fullMime.indexOf(';');
		while (true) {
			if ((startIdx < 0) || (startIdx >= fullMime.length())) {
				break;
			}
			int semicolIdx = MimeType.MY_ESCAPER.specialCharIndex(fullMime, ';', startIdx + 1);
			if (semicolIdx < 0) {
				semicolIdx = fullMime.length();
			}
			final String paramNameVal = fullMime.substring(startIdx + 1, semicolIdx);
			if (ret == null) {
				ret = new ArrayList<MimeParamValue>();
			}
			ret.add(new MimeParamValue(paramNameVal));
			startIdx = semicolIdx;
		}
		if (ret == null) {
			return null;
		}
		return ret.toArray(new MimeParamValue[ret.size()]);
	}
	
	public static final String extractSubType(final String fullMime) {
		final int slashIdx = fullMime.indexOf('/');
		if (slashIdx < 0) {
			return null;
		}
		int semicolIdx = fullMime.indexOf(';');
		if (semicolIdx < 0) {
			semicolIdx = fullMime.length();
		}
		return fullMime.substring(slashIdx + 1, semicolIdx);
	}
	
	public static final MimeType findReferenceMime(final String mimeString) {
		return MimeType.mappings.get(mimeString);
	}
	
	public static String getDefaultFileExtension(final String mimetype) {
		if (mimetype == null) {
			return null;
		}
		final MimeType mt = MimeType.constructMimeType(mimetype);
		if (mt == null) {
			return null;
		}
		return mt.getDefaultFileExtension();
	}
	
	public static final MimeType getObjectMime(final Class<?> objectClass) {
		String cName = String.valueOf(objectClass);
		if (cName.startsWith("class ")) {
			cName = cName.substring(6);
		} else if (cName.startsWith("interface ")) {
			cName = cName.substring(10);
		}
		// cName=cName.substring(0, cName.lastIndexOf('@'));
		cName = cName.replace('.', '-');
		// if (cName.charAt(cName.length()-1)==';')
		// cName=cName.substring(0,cName.length()-1);
		return MimeType.deriveWithParameter(MimeType.MIME_JAVA_OBJECT, MimeType.PARAM_CSL_OBJECT_TYP, cName);
	}
	
	public static String imageIOType(final String mime) {
		final int semic = mime.indexOf(';');
		return mime.substring("image/".length(), semic < 0 ? mime.length() : semic).toUpperCase();
	}
	
	public static final boolean isTransparentImage(final MimeType mime) {
		return mime.isEqualOrAlternative(MimeType.MIME_IMAGE_PNG) || mime.isEqualOrAlternative(MimeType.MIME_IMAGE_GIF)
		       || mime.isEqualOrAlternative(MimeType.MIME_IMAGE_TIF);
	}
	
	/**
	 * section 7 of <a href="http://tools.ietf.org/html/rfc3023#section-7">IETF RFC 3023</a>
	 * 
	 * @param mimeType
	 * @return
	 */
	public static final boolean isXML(final MimeType mime) {
		if (mime.subtype.endsWith(MimeType.SUFFIX_SYNTAX_XML)) {
			return true;
		}
		return MimeType.MIME_XML.isParentOrEqual(mime);
	}
	
	public static boolean isImage(MimeType mimetype) {
		return MIME_IMAGE_GIF.isParentOrEqual(mimetype);
	}
	
	public static final void registerType(final String mimeString, final MimeType referenceType) {
		MimeType.mappings.put(mimeString, referenceType);
	}
	
	// -----------------------
	// INSTANCE MEMBERS
	// -----------------------
	
	public String           defaultExtension;
	private HashSet<String> possibleExtensions = new HashSet<String>();
	
	public MimeParamValue[] params;
	
	public String           subtype;
	
	public String           type;
	
	public String			description;
	
	// for GWT serialization only
	@Deprecated
	protected MimeType() {
		
	}
	public MimeType(final MimeType mainType, final MimeParamValue[] parameters) {
		this(mainType.type, mainType.subtype, parameters, null);
		
	}
	
	public MimeType(final MimeType mainType, final String[] parameterNames, final String[] parameterValues) {
		this(mainType.type, mainType.subtype, MimeType.createParamsArray(parameterNames, parameterValues), null);
	}
	
	public MimeType(final String main, final String subtype) {
		this(main, subtype, null, null);
	}
	
	public MimeType(final String main, final String subtype, final MimeParamValue[] params, final String defaultExtension) {
		type = main;
		this.subtype = subtype;
		setDefaultExtension(defaultExtension);
		this.params = params;
		if (params != null) {
			Arrays.sort(params);
		}
		final String myString = createContentTypeString();
		if (MimeType.findReferenceMime(myString) == null) {
			MimeType.registerType(myString, this);
		}
	}
	
	public MimeType setDefaultExtension(String ext) {
		if (ext != null) {
			String lcExt = ext.toLowerCase();
			this.defaultExtension = lcExt;
			possibleExtensions.add(lcExt);
		}
		return this;
	}
	
	public MimeType addExtension(String ext) {
		possibleExtensions.add(ext.toLowerCase());
		return this;
	} 

	public MimeType(final String main, final String subtype, final String defaultExtension) {
		this(main, subtype, null, defaultExtension);
	}
	
	
	public MimeType setDescription(String description) {
		this.description=description;
		return this;
	}
	
	public String createContentTypeString() {
		final StringBuffer ret = new StringBuffer();
		ret.append(type);
		ret.append('/');
		ret.append(subtype);
		if (params != null) {
			for (final MimeParamValue param : params) {
				param.appendTo(ret);
			}
		}
		return ret.toString();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		final MimeType other = (MimeType)obj;
		if (!ArrayUtil.equals(params, other.params)) {
			return false;
		}
		if (subtype == null) {
			if (other.subtype != null) {
				return false;
			}
		} else if (!subtype.equals(other.subtype)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}
	
	/**
	 * @return lowercase default extension for the mime type
	 */
	public String getDefaultFileExtension() {
		final MimeType normal = MimeType.findReferenceMime(createContentTypeString());
		if ((normal != null) && (normal != this)) {
			return normal.getDefaultFileExtension();
		}
		if (defaultExtension != null) {
			return defaultExtension;
		}
		
		if (subtype == null)
			return "";
		
		String ret = subtype;
		// svg+xml --> svg
		if (ret.indexOf('+') > 0) {
			ret = ret.substring(0, ret.indexOf('+'));
		}
		// x-png --> png
		if (ret.startsWith("x-")) {
			ret = ret.substring(2);
		}
		return ret;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ArrayUtil.hashCode(params);
		result = prime * result + ((subtype == null) ? 0 : subtype.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}
	
	public boolean isEqualOrAlternative(final MimeType other) {
		if ((this == other) || equals(other)) {
			return true;
		}
		return false;
	}
	
	public boolean isEqualOrAlternative(final String mimetype) {
		return isEqualOrAlternative(MimeType.constructMimeType(mimetype));
	}
	
	public boolean isParentOrEqual(final MimeType t) {
		if ((t == this) || isEqualOrAlternative(t)) {
			return true;
		}
		if (t == null) {
			return false;
		}
		if ((params != null) && !ArrayUtil.equals(params, t.params)) {
			return false;
		}
		if ((subtype != null) && !subtype.equals(t.subtype)) {
			return false;
		}
		return type.equals(t.type);
	}
	
	@Override
	public String toString() {
		return createContentTypeString();
	}
	
	// ///////////
	// TEST
	// ///////////
	
	public static void main(final String[] args) {
		System.out.println(MIME_IMAGE_WEBCGM.createContentTypeString());
		System.out.println(new MimeType("image", "png"));
		System.out.println(MimeType.getObjectMime(String.class));
		System.out.println(MimeType.getObjectMime(String[].class));
		System.out.println(MimeType.getObjectMime(Iterator.class));
	}
}
