package com.sinergise.common.gis.map.shapes.snap;

import com.sinergise.common.util.event.selection.Selectable;
import com.sinergise.common.util.geom.HasCoordinate;

public interface SnapProvider {
	
	void snapPoint(HasCoordinate point, SnapProviderCallback callback);
	Selectable getEnabled();
	
	public interface SnapProviderCallback {
		void onPointSnapped(HasCoordinate point, HasCoordinate snapLocation);
		void onPointNotSnapped(HasCoordinate point);
	}
	
	public class SnapProviderCallbackAdapter implements SnapProviderCallback {
		@Override
		public void onPointSnapped(HasCoordinate point, HasCoordinate snapLocation) { }
		
		@Override
		public void onPointNotSnapped(HasCoordinate point) { }
	}

}
