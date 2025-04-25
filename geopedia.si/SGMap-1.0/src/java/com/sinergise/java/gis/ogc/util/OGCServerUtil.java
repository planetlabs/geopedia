package com.sinergise.java.gis.ogc.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;

import com.sinergise.common.gis.ogc.OGCException;
import com.sinergise.common.gis.ogc.OGCRequest;
import com.sinergise.common.gis.ogc.OGCRequestContext;
import com.sinergise.common.gis.ogc.base.OGCImageRequest;
import com.sinergise.common.gis.ogc.wms.request.WMSRequest;
import com.sinergise.common.util.web.MimeType;


public class OGCServerUtil {
	private OGCServerUtil() {}

	public static String imageIOImageTypeFromMime(MimeType mimeType) {
	    if (MimeType.MIME_IMAGE_PNG.isEqualOrAlternative(mimeType)) {
	        return "PNG";
	    } else if (MimeType.MIME_IMAGE_TIF.isEqualOrAlternative(mimeType)) {
	    	return "TIFF";
	    } else {
	        return "JPEG";
	    }
	}

	public static MimeType mimeTypeFromImageIOImageType(String imageType) {
	    return MimeType.constructMimeType("image/"+imageType.toLowerCase());
	}

	public static void reportException(Exception e, OGCRequest req, HttpServletResponse resp, Logger logr) {
		OGCException oe=null;
		if (e instanceof OGCException) {
			oe=(OGCException)e;
		} else {
			oe=new OGCException(req, e.getMessage(), e.getCause());
		}
		String excType = oe.getRequest().get(WMSRequest.PARAM_EXCEPTIONS);
	    try {
	        logr.error("WMSServlet exception reporting as '" + excType + "'");
	        e.printStackTrace();
	        
	        if ((req instanceof OGCImageRequest) && WMSRequest.EXCEPTIONS_IN_IMAGE.equalsIgnoreCase(excType)) {
	        	OGCImageRequest oir=(OGCImageRequest)req;
	            reportExceptionInImage(oe, oir.getImageWidth(), oir.getImageHeight(), resp, logr);
	        } else if (WMSRequest.EXCEPTIONS_XML.equalsIgnoreCase(excType)) {
	            // TODO: Implement XML exceptions
	            resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "XML exceptions report not implemented. Plain message: " + e.getMessage());
	        } else {
	            resp.sendError(oe.getHttpCode(), e.getMessage());
	        }
	    } catch (Exception ex) {
	        logr.warn("Exception while reporting error", ex);
	    }
	}

	/**
	 * @param e
	 * @param resp
	 */
	public static void reportExceptionInImage(OGCException e, int imgW, int imgH, HttpServletResponse resp, Logger logr) {
	    byte[] ret = ErrorImageCreator.createErrorPNG(getOGCExceptionReport(e), imgW, imgH);
	    
	    try {
	        resp.setContentType(MimeType.MIME_IMAGE_PNG.createContentTypeString());
	        OutputStream os = resp.getOutputStream();
	        try {
	            os.write(ret);
	        } finally {
	            os.close();
	        }
	    } catch (Exception e2) {
	        if (checkForClientAbort(e2, logr)) return;
	        throw new RuntimeException("Exception while writing image for error",e2);
	    }
	}

	public static final String getOGCExceptionReport(OGCException e) {
		String msg=e.getMessage();
		StringWriter sw=new StringWriter(msg.length());
		PrintWriter pw=new PrintWriter(sw);
		try {
			pw.write(msg);
			pw.write("\n");
			if (e.getCause()!=null) {
				e.getCause().printStackTrace(pw);
			}
		} finally {
			try {
				sw.close();
				pw.close();
			} catch (IOException e1) {
			}
		}
		String ret=sw.toString();
		if (ret!=null && ret.length()>0) return ret;
		return "Error in "+e.getRequest().getRequestType()+" (" + e.getCause() + ") "+e.getRequest();
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> prepareParameterMap(HttpServletRequest req) {
		Map<String, String[]> reqMap = req.getParameterMap();
		HashMap<String, String> paramsMap = new HashMap<String, String>();
		for (Map.Entry<String, String[]> ent : reqMap.entrySet()) {
			paramsMap.put(ent.getKey().toUpperCase(), ent.getValue()[0]);
		}
		return paramsMap;
	}

	public static void sendResponse(HttpServletResponse resp, OGCRequest ogcReq, OGCServerResponse ogcResp, Logger wmsLogger) throws OGCException {
		try {
			ogcResp.writeOutput(resp);
		} catch (Exception e) {
		    if (checkForClientAbort(e, wmsLogger)) return;
		    throw new OGCException(ogcReq, "Exception when writing response to output", e);
		}
	}

	public static final void fillRequest(HttpServletRequest req, OGCRequest outRequest) {
		fillRequest(req, outRequest, true);
	}
	public static final void fillRequest(HttpServletRequest req, OGCRequest outRequest, boolean validate) {
		Map<String, String> paramsMap = prepareParameterMap(req);
		if (validate) {
			String myReq = paramsMap.get(OGCRequest.PARAM_REQUEST);
			String outRequestType = outRequest.getRequestType();
			if (!myReq.equals(outRequestType)) throw new IllegalArgumentException("Request type does not fit the passed request object");
		}
		for (Map.Entry<String, String> en : paramsMap.entrySet()) {
			outRequest.set(en.getKey(), en.getValue());
		}
	}
	
	public static void fillContext(HttpServletRequest req, OGCRequestContext contextData) {
		HttpSession ses=req.getSession(true);
		contextData.setLocalSession(ses);
	}

	public static <T extends OGCRequest> T fillRequestAndContext(HttpServletRequest req, T ogcReq) {
		return fillRequestAndContext(req, ogcReq, true);
	}
	public static <T extends OGCRequest> T fillRequestAndContext(HttpServletRequest req, T ogcReq, boolean validate) {
		fillRequest(req, ogcReq, validate);
		fillContext(req, ogcReq.getContextData());
		return ogcReq;
	}

	public static boolean checkForClientAbort(Exception e, Logger logger) {
	    String eMsg=e.toString();
	    if (eMsg.indexOf("connection abort")>=0 || eMsg.indexOf("Connection reset by peer")>=0 || eMsg.indexOf("ClientAbort")>=0) {
	        logger.info("Client aborted");
	        return true;
	    }
	    Throwable cause=e.getCause();
	    if (cause==null || cause==e) return false;
	    if (cause instanceof Exception) {
	        return checkForClientAbort((Exception)cause, logger);
	    }
	    return false;
	}
	
}
