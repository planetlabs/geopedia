/*
 *
 */
package com.sinergise.common.raster.core;


public interface ImageFileFilter {
	ImageFileFilter ACCEPT_ALL = new ImageFileFilter() {
		@Override
		public boolean acceptImage(RasterWorldInfo imageCandidate) {
			return true;
		}
		@Override
		public boolean acceptFile(String fileName) {
			return true;
		}
		@Override
		public boolean acceptDirectory(String dirName) {
			return true;
		}
	};
	boolean acceptDirectory(String dirName);

	boolean acceptFile(String fileName);

	boolean acceptImage(RasterWorldInfo imageCandidate);
}
