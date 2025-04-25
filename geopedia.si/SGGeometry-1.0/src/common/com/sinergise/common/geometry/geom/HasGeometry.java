package com.sinergise.common.geometry.geom;

import com.sinergise.common.util.lang.Function;

public interface HasGeometry {
	
	public static final Function<HasGeometry, Geometry> FUNC_GEOM_GETTER = new Function<HasGeometry, Geometry>() {
		@Override
		public Geometry execute(HasGeometry param) {
			return param.getGeometry();
		}
	};
	
	Geometry getGeometry();

}
