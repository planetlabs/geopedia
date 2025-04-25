/**
 * 
 */
package com.sinergise.java.gis.ogc.util;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;

import com.sinergise.common.util.web.MimeType;
import com.sinergise.java.util.io.ByteArrayOutputStream;


public class ServletRenderedImage implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 7642150933689425615L;
	
	public final byte[] data;
    public final MimeType mimeType;
    public final int dataLen;
    
	public ServletRenderedImage(MimeType mimeType, RenderedImage image) throws IOException {
		this.mimeType = mimeType;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, MimeType.imageIOType(mimeType.createContentTypeString()), baos);
		this.data = baos.getInternalBuffer();
		this.dataLen = baos.size();
	}
}