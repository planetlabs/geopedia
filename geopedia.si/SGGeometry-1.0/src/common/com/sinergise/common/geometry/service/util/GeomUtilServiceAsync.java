package com.sinergise.common.geometry.service.util;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GeomUtilServiceAsync {

	void getGeomBuffer(GetGeomBufferRequest request, AsyncCallback<GeomOpResult> callback);
	
	void getGeomUnion(BinaryGeomOpRequest request, AsyncCallback<GeomOpResult> callback);
	
	void getGeomIntersection(BinaryGeomOpRequest request, AsyncCallback<GeomOpResult> callback);
	
	void getGeomDifference(BinaryGeomOpRequest request, AsyncCallback<GeomOpResult> callback);
	
}
