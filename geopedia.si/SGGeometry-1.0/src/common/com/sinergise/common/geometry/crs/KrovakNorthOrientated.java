package com.sinergise.common.geometry.crs;

import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;

public class KrovakNorthOrientated extends Krovak {
	@Deprecated
	protected KrovakNorthOrientated(){
	}

	public KrovakNorthOrientated(Ellipsoidal baseCRS, double latc_deg, double lon0_deg, double alpha_c, double latp_deg, double kp, CrsIdentifier id, Envelope bnds){
		super(baseCRS,latc_deg,lon0_deg,alpha_c,latp_deg,kp,id,bnds);
	}
	
	
	public static class GeographicToKrovakNorthOrientated extends GeographicToKrovak {

		public GeographicToKrovakNorthOrientated(KrovakNorthOrientated projection) {
			super(projection);
		}

		@Override
		public Point point(Point src, Point ret) {
			ret = super.point(src, ret);
			double x = -1. * ret.y;
			
			ret.y = -1. * ret.x;
			ret.x = x;
			
			updateCrsReference(ret);
			return ret;
		}

		@Override
		public Envelope envelope(Envelope src) {
			EnvelopeBuilder ret = new EnvelopeBuilder(target.getDefaultIdentifier());

			final Point srcPoint = new Point();
			final Point tgtPoint = new Point();

			double minX = src.getMinX();
			double minY = src.getMinY();
			double maxY = src.getMaxY();
			double maxX = src.getMaxX();

			srcPoint.setLocation(minX, minY);
			ret.expandToInclude(point(srcPoint, tgtPoint));

			srcPoint.setLocation(minX, maxY);
			ret.expandToInclude(point(srcPoint, tgtPoint));

			srcPoint.setLocation(maxX, maxY);
			ret.expandToInclude(point(srcPoint, tgtPoint));

			srcPoint.setLocation(maxX, minY);
			ret.expandToInclude(point(srcPoint, tgtPoint));

			return ret.getEnvelope();
		}

		@Override
		public KrovakNorthOrientatedToGeographic inverse() {
			return new KrovakNorthOrientatedToGeographic((KrovakNorthOrientated)this.target);
		}
		
		
		
	}
	
	
	public static class KrovakNorthOrientatedToGeographic extends KrovakToGeographic {

		public KrovakNorthOrientatedToGeographic(KrovakNorthOrientated projected) {
			super(projected);
		}

		@Override
		public Point point(Point src, Point ret) {
			double x = -1. * src.y;
			src.y = -1. * src.x;
			src.x = x;
			
			return super.point(src, ret);
		}

		@Override
		public GeographicToKrovakNorthOrientated inverse() {
			return new GeographicToKrovakNorthOrientated((KrovakNorthOrientated)source);
		}

		@Override
		public Envelope envelope(Envelope src) {
			EnvelopeBuilder builder = new EnvelopeBuilder(target.getDefaultIdentifier());
			double minX = -1. * src.getMinY();
			double minY = -1. * src.getMinX();
			double maxX = -1. * src.getMaxY();
			double maxY = -1. * src.getMaxX();
			builder.setMBR(minX, minY, maxX, maxY);
			
			return super.envelope(builder.getEnvelope());
		}
		
		
	}
}
