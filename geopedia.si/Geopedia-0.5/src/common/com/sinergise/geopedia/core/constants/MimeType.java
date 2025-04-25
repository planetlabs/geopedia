/*
 *
 */
package com.sinergise.geopedia.core.constants;

import java.util.Iterator;

import com.sinergise.geopedia.core.common.util.Escaper;


/**
 * <a href="http://www.w3.org/Protocols/rfc1341/4_Content-Type.html">RFC1341</a>
 * @author <a href="mailto:miha.kadunc@cosylab.com">Miha Kadunc</a>
 */
public class MimeType {
    private static final char[] tspecials=new char[] {'(',')','<','>','@',',',';',':','/','[',']','?','.','='};
    private static final char quote='"';
    private static final char esc='\\';
    private static final Escaper MY_ESCAPER=new Escaper(quote,esc,tspecials);
    
    public static final String MIME_IMAGE_GIF = "image/gif";
    public static final String MIME_IMAGE_PNG = "image/png";
    public static final String MIME_IMAGE_JPG = "image/jpeg";
    public static final String MIME_IMAGE_TIF = "image/tiff";
    public static final String MIME_IMAGE_SVG = "image/svg+xml";
    /** WebCGM is a profile of ISO/IEC 8632 **/
    public static final String MIME_IMAGE_WEBCGM = "image/cgm;Version=4;ProfileId=WebCGM";
    
    public static final String MIME_XML = "text/xml";
    public static final String MIME_HTML = "text/html";

    public static final String MIME_CSL_OBJECT = "application/x-java-object";
    public static final String PARAM_CSL_OBJECT_TYP = "type";
    
    public static String deriveWithParameter(String baseMime, String paramName, String paramValue) {
        
        return baseMime+';'+MY_ESCAPER.escape(paramName)+'='+MY_ESCAPER.escape(paramValue);
    }
    
    public static String deriveObjectMime(Class objectClass) {
        String cName=String.valueOf(objectClass);
        if (cName.startsWith("class ")) {
            cName=cName.substring(6);
        } else if (cName.startsWith("interface ")) {
            cName=cName.substring(10);
        }
        //cName=cName.substring(0, cName.lastIndexOf('@'));
        cName=cName.replaceAll("\\.", "-");
        return deriveWithParameter(MIME_CSL_OBJECT, PARAM_CSL_OBJECT_TYP, cName);
    }
    public static void main(String[] args) {
        System.out.println(deriveObjectMime(String.class));
        System.out.println(deriveObjectMime(String[].class));
        System.out.println(deriveObjectMime(Iterator.class));
    }

    public static String imageIOType(String mime) {
        return mime.substring("image/".length());
    }
}
