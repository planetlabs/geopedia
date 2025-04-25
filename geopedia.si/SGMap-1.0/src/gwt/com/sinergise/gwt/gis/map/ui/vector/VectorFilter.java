package com.sinergise.gwt.gis.map.ui.vector;

import java.io.Serializable;

public abstract class VectorFilter implements Serializable {
	
	public static class GlowFilter extends VectorFilter {
		public final double	width;
		public final String	color;
		public final double	opacity;

		public GlowFilter(String color, double width) {
			this.color = color;
			this.width = width;
			this.opacity = 1;
		}
		public GlowFilter(String color, double width, double opacity) {
			this.color = color;
			this.width = width;
			this.opacity = opacity;
		}
	}
	
}
