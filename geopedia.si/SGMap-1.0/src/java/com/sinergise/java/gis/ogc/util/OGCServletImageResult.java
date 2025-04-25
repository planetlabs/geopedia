/**
 * 
 */
package com.sinergise.java.gis.ogc.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import com.sinergise.common.gis.ogc.wms.response.WMSImageResult;
import com.sinergise.common.util.web.MimeType;
import com.sinergise.java.util.io.ByteArrayOutputStream;
import com.sinergise.java.util.io.FileUtilJava;
import com.sinergise.java.web.ServletUtil;


public abstract class OGCServletImageResult implements WMSImageResult, OGCServerResponse {
	private final long expiration;
	private final MimeType mimeType;
	
	public static class WithData extends OGCServletImageResult {
		private final ServletRenderedImage src;
		public WithData(ServletRenderedImage src, long expiration) {
			super(expiration, src.mimeType);
			this.src = src;
		}
		
		public ServletRenderedImage getServletRenderedImage() {
			return src;
		}
		
		@Override
		public byte[] getDataBytes() {
			return src.data;
		}
		
		@Override
		public int getDataBytesLen() {
			return src.dataLen;
		}
	}
	
	public static class WithFile extends OGCServletImageResult {
		private static final long serialVersionUID = -5608126714698545703L;

		File f = null;
		transient ByteArrayOutputStream outStr = null;
		
		public WithFile(long expiration, MimeType mimeType, File f) {
			super(expiration, mimeType);
			this.f = f;
		}
		
		@Override
		public void writeData(OutputStream out) throws IOException {
			FileUtilJava.copyFile(f, out);
		}
		
		@Override
		public byte[] getDataBytes() throws IOException {
			if (outStr != null) return outStr.getInternalBuffer();
			outStr = new ByteArrayOutputStream((int)f.length());
			FileUtilJava.copyFile(f, outStr);
			return outStr.getInternalBuffer();
		}
		
		@Override
		public int getDataBytesLen() {
			if (outStr != null) return outStr.size();
			throw new IllegalStateException("Should call getDataBytes first");
		}
	}
	
	public OGCServletImageResult(long expiration, MimeType mimeType) {
		this.expiration = expiration;
		this.mimeType = mimeType;
	}
	
	@Override
	public MimeType getMimeType() {
		return mimeType;
	}
	
	@Override
	public void writeOutput(HttpServletResponse resp) throws IOException {
		if (expiration > 0) {
			ServletUtil.setExpiration(resp, expiration);
		}
		resp.setContentType(getMimeType().createContentTypeString());
		//TODO: Allow toggle open inline / download
//		if (src.fileName != null) ServletUtil.setAttachedFileName(resp, src.fileName);
		OutputStream os = resp.getOutputStream();
		try {
			writeData(os);
		} finally {
			os.close();
		}
	}
	
	public void writeData(OutputStream out) throws IOException {
		out.write(getDataBytes(), 0, getDataBytesLen());
	}
	
	abstract public byte[] getDataBytes() throws IOException;
	abstract public int getDataBytesLen() throws IOException;
}
