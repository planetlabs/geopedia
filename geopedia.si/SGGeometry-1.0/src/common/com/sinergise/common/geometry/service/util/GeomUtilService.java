package com.sinergise.common.geometry.service.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.property.GeometryProperty;
import com.sinergise.common.util.ServiceException;
import com.sinergise.common.util.lang.AsyncFunction;
import com.sinergise.common.util.lang.SGAsyncCallback;

@RemoteServiceRelativePath("geom")
public interface GeomUtilService extends RemoteService {
	
	GeomOpResult getGeomBuffer(GetGeomBufferRequest request) throws ServiceException;
	
	GeomOpResult getGeomUnion(BinaryGeomOpRequest request) throws ServiceException;
	
	GeomOpResult getGeomIntersection(BinaryGeomOpRequest request) throws ServiceException;
	
	GeomOpResult getGeomDifference(BinaryGeomOpRequest request) throws ServiceException;
	
	public static class Proxy {
        private static synchronized GeomUtilServiceAsync createInstance() {
        	if (GWT.isClient()) {
        		return GWT.create(GeomUtilService.class);
            }
            return null;
        }
	}
	
	public static GeomUtilServiceAsync INSTANCE = Proxy.createInstance();
	
	public abstract static class UnaryGeomOpAsync implements AsyncFunction<Geometry, Geometry> {
		
		final double gridSize;
		
		UnaryGeomOpAsync() {
			this(0);
		}
		
		UnaryGeomOpAsync(double gridSize) {
			this.gridSize = gridSize;
		}
		
		AsyncCallback<GeomOpResult> wrapCallback(final SGAsyncCallback<? super Geometry> callback) {
			return new AsyncCallback<GeomOpResult>() {
				
				@Override
				public void onSuccess(GeomOpResult result) {
					callback.onSuccess(result.getResult());
				}
				
				@Override
				public void onFailure(Throwable e) {
					callback.onFailure(new Exception(e.getMessage(), e));
				}
			};
		}
	}
	
	public abstract static class BinaryGeomOpAsync extends UnaryGeomOpAsync {
		
		final Geometry firstGeom;
		
		BinaryGeomOpAsync(Geometry firstGeom) {
			this(firstGeom, 0);
		}
		
		BinaryGeomOpAsync(Geometry firstGeom, double gridSize) {
			super(gridSize);
			this.firstGeom = firstGeom;
		}
		
		BinaryGeomOpRequest createRequest(Geometry secondGeom) {
			return new BinaryGeomOpRequest(firstGeom, secondGeom, gridSize);
		}
		
	}
	
	public static class GeomIntersectionAsync extends BinaryGeomOpAsync {
		
		public GeomIntersectionAsync(Geometry firstGeom, double gridSize) {
			super(firstGeom, gridSize);
		}
		
		@Override
		public void executeAsync(Geometry secondGeom, final SGAsyncCallback<? super Geometry> callback) throws Exception {
			INSTANCE.getGeomIntersection(createRequest(secondGeom), wrapCallback(callback));
		}
	}
	
	public static class GeomUnionAsync extends BinaryGeomOpAsync {
		
		public GeomUnionAsync(Geometry firstGeom, double gridSize) {
			super(firstGeom, gridSize);
		}
		
		@Override
		public void executeAsync(Geometry secondGeom, final SGAsyncCallback<? super Geometry> callback) throws Exception {
			INSTANCE.getGeomUnion(createRequest(secondGeom), wrapCallback(callback));
		}
	}
	
	public static class GeomDifferenceAsync extends BinaryGeomOpAsync {
		
		private final Geometry secondGeom;
		
		public GeomDifferenceAsync(Geometry firstGeom, double gridSize) {
			super(firstGeom, gridSize);
			this.secondGeom = null;
		}
		
		public GeomDifferenceAsync(Geometry firstGeom, Geometry secondGeom, double gridSize) {
			super(firstGeom, gridSize);
			this.secondGeom = secondGeom;
		}
		
		@Override
		public void executeAsync(Geometry geomParam, final SGAsyncCallback<? super Geometry> callback) throws Exception {
			INSTANCE.getGeomDifference(createRequest(secondGeom != null ? secondGeom : geomParam), wrapCallback(callback));
		}
	}
	
	public static class GeomBufferAsync extends UnaryGeomOpAsync {
		
		private final double distance;
		
		public GeomBufferAsync(double distance) {
			this(distance, 0);
		}
		
		public GeomBufferAsync(double distance, double gridSize) {
			super(gridSize);
			this.distance = distance;
		}
		
		@Override
		public void executeAsync(Geometry geom, final SGAsyncCallback<? super Geometry> callback) throws Exception {
			INSTANCE.getGeomBuffer(new GetGeomBufferRequest(geom, distance, gridSize), wrapCallback(callback));
		}
	}
	
	public static class CheckEmptyGeomAsync implements AsyncFunction<Geometry, Geometry> {
		
		private static final String DEFAULT_MESSAGE = "Null or empty geometry";
		
		private final String message;
		
		public CheckEmptyGeomAsync() {
			this(DEFAULT_MESSAGE);
		}
		
		public CheckEmptyGeomAsync(String message) {
			this.message = message;
		}
		
		
		@Override
		public void executeAsync(Geometry geom, SGAsyncCallback<? super Geometry> callback) throws Exception {
			if (geom == null || geom.isEmpty()) {
				callback.onFailure(new Exception(message));
			} else {
				callback.onSuccess(geom);
			}
		}
	}
	
	public static class StoreGeometryAsync implements AsyncFunction<Geometry, Geometry> {
		
		private final GeometryProperty property;
		
		public StoreGeometryAsync(GeometryProperty property) {
			this.property = property;
		}
		
		public Geometry getGeometry() {
			return property.getValue();
		}
		
		@Override
		public void executeAsync(Geometry param, SGAsyncCallback<? super Geometry> callback) throws Exception {
			property.setValue(param);
			callback.onSuccess(param);
		}
	}
}
