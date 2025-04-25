package com.sinergise.common.geometry.io;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.util.io.ObjectReader;

public interface GeometryReader extends ObjectReader<Geometry> {
	public static abstract class AbstractGeometryReaderImpl implements GeometryReader {
		protected boolean needsFetching = true;
		protected Geometry next = null;
		@Override
		public final boolean hasNext() throws com.sinergise.common.util.io.ObjectReader.ObjectReadException {
			if (needsFetching) {
				needsFetching = false;
				next = internalReadNext();
			}
			return next != null;
		}
		@Override
		public final Geometry readNext() throws ObjectReadException {
			if (!(hasNext())) {
				throw new ObjectReader.ObjectReadException("End of data reached!");
			}
			try {
				return next;
			} finally {
				next = null;
				needsFetching = true;
			}
		}
		
		/**
		 * Should be called when input changes
		 */
		protected void reset() {
			next = null;
			needsFetching = true;
		}

		/**
		 * @return null iff next geometry is not available
		 * @throws ObjectReadException
		 */
		protected abstract Geometry internalReadNext() throws ObjectReadException;
	}
}
