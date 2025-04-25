package com.sinergise.common.geometry.crs.transform;

import com.sinergise.common.geometry.crs.DatumConversion;
import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;

/**
 *  Implementation of the Molodensky-Badekas transform from white paper http://www.epsg.org/guides/docs/G7-2.pdf, page 133.
 * @author mrepse
 */
public class MolodenskyBadekas extends DatumConversion {

	public final double dx; 	// = tX
	public final double dy; 	// = tY
	public final double dz; 	// = tZ
	public final double x0;		// = Xp
	public final double y0; 	// = Yp
	public final double z0; 	// = Zp
	protected final double Rx; 	// = rX
	protected final double Ry; 	// = rY
	protected final double Rz; 	// = rZ
	public final double M; 		// = M = 1 + dM
	public final double dM; 	// = dS/1e6
	public final boolean scale; // if M = 1 <=> dM = 0
	public final boolean rotate; // if rX = rY = rZ = 0
	
	/**
	 * @param elSrc - Source geodetic coordinate reference system
	 * @param elTgt - Target geodetic coordinate reference system
	 * @param dx - Translation vector [meters]
	 * @param Rx - Rotation amount [radians]
	 * @param dS - Scale factor [parts per million]
	 * @param x0 - Rotation origin [meters]
	 */
	public MolodenskyBadekas(Ellipsoidal elSrc, Ellipsoidal elTgt, 	double dx, double dy, double dz,
		double Rx, double Ry, double Rz, double dS, double x0, double y0, double z0) {
		
		super(elSrc, elTgt);
		if (Double.isNaN(dS)) dS = 0;
		if (Double.isNaN(Rx)) Rx = 0;
		if (Double.isNaN(Ry)) Ry = 0;
		if (Double.isNaN(Rz)) Rz = 0;
		if (Double.isNaN(x0)) x0 = 0;
		if (Double.isNaN(y0)) y0 = 0;
		if (Double.isNaN(z0)) z0 = 0;

		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
		this.Rx = Rx;
		this.Ry = Ry;
		this.Rz = Rz;
		dM = dS == 0 ? 0 : dS/1e6;
		M = dM == 0 ? 0 : 1.0 + dM;
		this.x0 = x0;
		this.y0 = y0;
		this.z0 = z0;

		scale = (dM != 0);
		rotate = (Rx != 0 && Ry != 0 && Rz !=0);
	}

	@Override
	public void convertXYZ(double[] xyz) {
		if(rotate){
			double x = xyz[0] - x0;
			double y = xyz[1] - y0;
			double z = xyz[2] - z0;
			
			if(scale){
				xyz[0] = M*(    x + Rz*y - Ry*z) + x0 + dx;
				xyz[1] = M*(-Rz*x +    y + Rx*z) + y0 + dy;
				xyz[2] = M*( Ry*x - Rx*y +    z) + z0 + dz;
			} else {
				xyz[0] +=  Rz*y - Ry*z + dx;
				xyz[1] += -Rz*x + Rx*z + dy;
				xyz[2] +=  Ry*x - Rx*y + dz;
			}
		} else {
			if(scale){
				xyz[0] = M*(xyz[0] - x0) + x0 + dx;
				xyz[1] = M*(xyz[1] - y0) + y0 + dy;
				xyz[2] = M*(xyz[2] - z0) + z0 + dz;
			} else {
				xyz[0] += dx;
				xyz[1] += dy;
				xyz[2] += dz;
			}
		}
	}

	@Override
	public DatumConversion inverse() {
		return new MolodenskyBadekasInverse(this);
	}
	
	/**
	 * The inverse transform of the Molodensky-Badekas.
	 * @author mrepse
	 */
	public static class MolodenskyBadekasInverse extends DatumConversion{
		public final double dx; 	// = tX
		public final double dy; 	// = tY
		public final double dz; 	// = tZ
		public final double x0; 	// = Xp
		public final double y0; 	// = Yp
		public final double z0; 	// = Zp
		protected final double Rx; 	// = rX
		protected final double Ry; 	// = rY
		protected final double Rz; 	// = rZ
		public final double M; 		// = 1/M_inverse
		public final double dM; 	// = 1/dM_inverse
		public final boolean scale; // if M = 1 <=> dM = 0
		public final boolean rotate; // if rX = rY = rZ = 0
		
		public final MolodenskyBadekas inverse;
		
		/**
		 * @param toInvert - The Molodensky-Badekas transform object you wish to invert
		 */
		public MolodenskyBadekasInverse(MolodenskyBadekas toInvert) {
			
			super(toInvert.getTarget(), toInvert.getSource());
			this.dx = toInvert.dx;
			this.dy = toInvert.dy;
			this.dz = toInvert.dz;
			this.Rx = toInvert.Rx;
			this.Ry = toInvert.Ry;
			this.Rz = toInvert.Rz;
			this.dM = 1/toInvert.dM;
			this.M = 1/toInvert.M;
			this.x0 = toInvert.x0;
			this.y0 = toInvert.y0;
			this.z0 = toInvert.z0;

			scale = toInvert.scale;
			rotate = toInvert.rotate;
			
			inverse = toInvert;
		}

		@Override
		public void convertXYZ(double[] xyz) {
			if(rotate){
				double x = xyz[0] - dx - x0;
				double y = xyz[1] - dy - y0;
				double z = xyz[2] - dz - z0;
				
				if(scale){
					xyz[0] = M*(    x - Rz*y + Ry*z) + x0;
					xyz[1] = M*( Rz*x +    y - Rx*z) + y0;
					xyz[2] = M*(-Ry*x + Rx*y +    z) + z0;
				} else {
					xyz[0] += -Rz*y + Ry*z - dx;
					xyz[1] +=  Rz*x - Rx*z - dy;
					xyz[2] += -Ry*x + Rx*y - dz;
				}
			} else {
				if(scale){
					xyz[0] = M*(xyz[0] - dx - x0) + x0;
					xyz[1] = M*(xyz[1] - dy - y0) + y0;
					xyz[2] = M*(xyz[2] - dz - z0) + z0;
				} else {
					xyz[0] -= dx;
					xyz[1] -= dy;
					xyz[2] -= dz;
				}
			}
		}
		@Override
		public DatumConversion inverse() {
			return inverse;
		}
	}
}
