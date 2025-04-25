/*
 *
 */
package com.sinergise.common.gis.ogc.wms.response;

import java.io.Serializable;

import com.sinergise.common.util.web.MimeType;

public interface WMSResponse extends Serializable {
    MimeType getMimeType();
}
