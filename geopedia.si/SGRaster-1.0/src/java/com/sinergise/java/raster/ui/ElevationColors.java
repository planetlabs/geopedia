/*
 *
 */
package com.sinergise.java.raster.ui;

import com.sinergise.common.util.math.ColorUtil;
import com.sinergise.java.raster.ui.ColorInterpolator.ColorVal;

public class ElevationColors {
    public static ElevationColors singleColor(int clr, int nanClr) {
		return new ElevationColors(new ColorSpec[] {new ColorSpec(0,clr)}, nanClr);
	}

	public static final ElevationColors WHITE = singleColor(0xffffffff, 0);
    public static final ElevationColors TRANSPARENT = singleColor(0, 0);

	public static final ElevationColors SLO_SUMMER = new ElevationColors(new ColorSpec[] {
        new ColorSpec(-10,0xff05090b)
        ,new ColorSpec(0,0xff6db0b3)
        ,new ColorSpec(1,0xffd4ddbb)
        ,new ColorSpec(1400,0xff9bad83)
        ,new ColorSpec(1800,0xffbbbbbb)
        ,new ColorSpec(2900,0xffffffff)
	}, 0x00FFFFFF);
	
	public static final ElevationColors SLO_AUTUMN = new ElevationColors(new ColorSpec[] {
        new ColorSpec(-10,0xff000406) 
        ,new ColorSpec(0,0xff00797b) //morje
        ,new ColorSpec(1,0xffc3ce9b) //obala
        ,new ColorSpec(700,0xff7f9059) //gozd
        ,new ColorSpec(1400,0xff947b55) //hribi
        ,new ColorSpec(2900,0xffffffff) //sneg
	}, 0x00FFFFFF);
	
    public static final ElevationColors GISELLE_STANDARD=new ElevationColors(new ColorSpec[] {
            new ColorSpec(-1000,0xff000000),
            new ColorSpec(-5,0xff000080),
            new ColorSpec(0,0xff0000ff),
            new ColorSpec(1,0xff9ec4b7),
            new ColorSpec(300,0xff8cc897),
            new ColorSpec(600,0xffeec666),
            new ColorSpec(900,0xffeba069),
            new ColorSpec(1200,0xffcf858e),
            new ColorSpec(1500,0xffaaaaaa),
            new ColorSpec(1800,0xffffffff)
    }, 0x00FFFFFF);
    
    public static final ElevationColors MAURITIUS=new ElevationColors(new ColorSpec[] { //
      new ColorSpec(-5000,0xff2D5D79),//
      new ColorSpec(-250,0xff1795DD), //
      new ColorSpec(-10,0xff90FFD0),  // Deep Water
      new ColorSpec(0,0xFF88CCFF), //Water;
      new ColorSpec(1,0xffFFFFBB),  //Beach; tide max-min ~ 130 cm (http://ioc.unesco.org/glossafrica/Reports/mauritius.htm)
      new ColorSpec(50,0xffC8F098),  //Grass
      new ColorSpec(350,0xffA8E098),  //Forest
      new ColorSpec(550,0xffb4c490),  //Highlands
      new ColorSpec(850,0xff9e9562),  //Volcanic peaks
    }, 0xFF88CCFF);
    
    public final ColorSpec[] colors;
    public final int colorWhenEmpty;
    
    public ElevationColors(ColorSpec[] specs) {
    	this.colors = new ColorSpec[specs.length];
    	for (int i = 0; i < specs.length; i++) {
			this.colors[i] = new ColorSpec(specs[i].boundary, specs[i].color);
		}
        colorWhenEmpty = specs[0].color;
    }

    public ElevationColors(ColorSpec[] specs, int colorWhenEmpty) {
    	this.colors = new ColorSpec[specs.length];
    	for (int i = 0; i < specs.length; i++) {
			this.colors[i] = new ColorSpec(specs[i].boundary, specs[i].color);
		}
        this.colorWhenEmpty = colorWhenEmpty;
    }

	public ColorInterpolator createInterpolator() {
		if (colors.length == 1) {
			return new ColorInterpolator.Const(new ColorVal(colors[0].color));
		}
		double[] boundaries = new double[colors.length];
		ColorVal[] dCols = new ColorVal[colors.length];
		for (int i = 0; i < colors.length; i++) {
			boundaries[i] = colors[i].boundary;
			dCols[i] = new ColorVal(colors[i].color);
		}
		return new ColorInterpolator.Linear(boundaries, dCols);
	}
	
	public ColorInterpolator createPiecewiseInterpolator() {
		if (colors.length == 1) {
			return new ColorInterpolator.Const(new ColorVal(colors[0].color));
		}
		double[] boundaries = new double[colors.length];
		ColorVal[] dCols = new ColorVal[colors.length];
		for (int i = 0; i < colors.length; i++) {
			boundaries[i] = colors[i].boundary;
			dCols[i] = new ColorVal(colors[i].color);
		}
		return new ColorInterpolator.PiecewiseConst(boundaries, dCols);
	}

	public ElevationColors createForPlaneShade(double planeBlackAmount) {
		int newEmpty = (colorWhenEmpty & 0xFF000000) | (0xFFFFFF & ColorUtil.unblend(colorWhenEmpty & 0xFFFFFF, 0, planeBlackAmount));
		ColorSpec[] newSpec = new ColorSpec[colors.length];
		for (int i = 0; i < newSpec.length; i++) {
			int clr = colors[i].color & 0xFFFFFF;
			int alpha = colors[i].color & 0xFF000000;
			newSpec[i] = new ColorSpec(colors[i].boundary, alpha | (0xFFFFFF & ColorUtil.unblend(clr, 0, planeBlackAmount)));
		}
		return new ElevationColors(newSpec, newEmpty);
	}

	public void appendSignature(StringBuilder ret) {
		ret.append('C').append(Integer.toHexString(colorWhenEmpty));
		for (int i = 0; i < colors.length; i++) {
			ret.append(',').append(colors[i].boundary).append('-').append(Integer.toHexString(colors[i].color));
		}
	}

	public ElevationColors scaleTo(double max) {
		return scaleTo(0, max);
	}
	
	public ElevationColors scaleTo(double z0, double z1) {
		double fact = (z1 - z0) / colors[colors.length-1].boundary;
		ColorSpec[] ret = new ColorSpec[colors.length];
		for (int i = 0; i < ret.length; i++) {
			double val = fact * colors[i].boundary + z0;
			ret[i] = new ColorSpec(val, colors[i].color);
		}
		return new ElevationColors(ret, colorWhenEmpty);
	}


	public static class ColorSpec {
	    public final double boundary;
	    public final int color;
	    public ColorSpec(double boundary, int color) {
	        this.boundary=boundary;
	        this.color=color;
	    }
		public ColorSpec(double boundary, int r, int g, int b) {
			this(boundary, 0xff, r, g, b);
		}
		public ColorSpec(double boundary, int a, int r, int g, int b) {
			this(boundary, ColorUtil.fromIntArray(new int[] {a,r,g,b}));
		}
		@Override
		public String toString() {
			return boundary +" " +Integer.toHexString(color);
		}
	}

	public static class Mathematica {
	
		public static final ElevationColors MAT_GREEN_BROWN = new ElevationColors(new ColorSpec[] {
		        new ColorSpec(-1000,0xff000000),
		        new ColorSpec(-5,0xff4c85d9),
		        new ColorSpec(0,0xff8cc5d9),
		        new ColorSpec(1,0xff526758),
		        new ColorSpec(300,0xff7b8d67),
		        new ColorSpec(600,0xff9ca167),
		        new ColorSpec(900,0xffb1a661),
		        new ColorSpec(1200,0xffbda15c),
		        new ColorSpec(1500,0xffc19b5e),
		        new ColorSpec(1800,0xffc6a073),
		        new ColorSpec(2100,0xffd6bda4),
		        new ColorSpec(2400,0xffffffff)
		}, 0x00FFFFFF);
		public static final ElevationColors MAT_LIGHT_GRAY_BROWN = new ElevationColors(new ColorSpec[] {
		        new ColorSpec(-1000,0xff000000),
		        new ColorSpec(-5,0xff4c85d9),
		        new ColorSpec(0,0xff8cc5d9),
		        new ColorSpec(1,0xff8fa08f),
		        new ColorSpec(300,0xffa6aa83),
		        new ColorSpec(600,0xffb6b68a),
		        new ColorSpec(900,0xffc7c598),
		        new ColorSpec(1200,0xffd3d1a8),
		        new ColorSpec(1500,0xffdddcbc),
		        new ColorSpec(1800,0xffe3e2ce),
		        new ColorSpec(2100,0xffe5e5dc),
		        new ColorSpec(2400,0xffe6e6e6)
		}, 0x00FFFFFF);
		public static final ElevationColors MAT_LIGHT_GREEN_BROWN = new ElevationColors(new ColorSpec[] {
		        new ColorSpec(-1000,0xff000000),
		        new ColorSpec(-5,0xff4c85d9),
		        new ColorSpec(0,0xff8cc5d9),
		        new ColorSpec(1,0xff8c9a90),
		        new ColorSpec(300,0xffa7b399),
		        new ColorSpec(600,0xffbdc19a),
		        new ColorSpec(900,0xffcbc496),
		        new ColorSpec(1200,0xffd3c092),
		        new ColorSpec(1500,0xffd6bc94),
		        new ColorSpec(1800,0xffd9c0a1),
		        new ColorSpec(2100,0xffe3d3c2),
		        new ColorSpec(2400,0xffffffff)
		}, 0x00FFFFFF);
		
	}

	/**
	 * source: The Development and Rationale of Cross-blended Hypsometric Tints – Patterson, Jenny. Cartographic Perspectives, Number 69, 2011
	 */
	public static class NaturalEarth {
	    public static final ElevationColors WARM_HUMID=new ElevationColors(new ColorSpec[] {
	        new ColorSpec(-400, 106, 153, 135),
	        new ColorSpec(0, 114, 164, 141),
	        new ColorSpec(50, 120, 172, 149),
	        new ColorSpec(200, 134, 184, 159),
	        new ColorSpec(600, 169, 192, 166),
	        new ColorSpec(1000, 212, 201, 180),
	        new ColorSpec(2000, 212, 184, 163),
	        new ColorSpec(3000, 212, 193, 179),
	        new ColorSpec(4000, 212, 207, 204),
	        new ColorSpec(5000, 220, 220, 220),
	        new ColorSpec(6000, 235, 235, 237),
	        new ColorSpec(7000, 245, 245, 245)
	}, 0x00FFFFFF
	);
	public static final ElevationColors COLD_HUMID=new ElevationColors(new ColorSpec[] {
	    new ColorSpec(-400, 112, 147, 141),
	    new ColorSpec(0, 120, 159, 152),
	    new ColorSpec(50, 130, 165, 159),
	    new ColorSpec(200, 145, 177, 171),
	    new ColorSpec(600, 180, 192, 180),
	    new ColorSpec(1000, 212, 201, 180),
	    new ColorSpec(2000, 212, 184, 163),
	    new ColorSpec(3000, 212, 193, 179),
	    new ColorSpec(4000, 212, 207, 204),
	    new ColorSpec(5000, 220, 220, 220),
	    new ColorSpec(6000, 235, 235, 237),
	    new ColorSpec(7000, 245, 245, 245)
	}, 0x00FFFFFF);
	
	public static final ElevationColors ARID=new ElevationColors(new ColorSpec[] {
	    new ColorSpec(-400, 146, 136, 129),
	    new ColorSpec(0, 160, 152, 141),
	    new ColorSpec(50, 170, 160, 150),
	    new ColorSpec(200, 180, 170, 158),
	    new ColorSpec(600, 202, 190, 174),
	    new ColorSpec(1000, 212, 201, 180),
	    new ColorSpec(2000, 212, 184, 163),
	    new ColorSpec(3000, 212, 193, 179),
	    new ColorSpec(4000, 212, 207, 204),
	    new ColorSpec(5000, 220, 220, 220),
	    new ColorSpec(6000, 235, 235, 237),
	    new ColorSpec(7000, 245, 245, 245)
	}, 0x00FFFFFF);
	
	public static final ElevationColors POLAR=new ElevationColors(new ColorSpec[] {
	    new ColorSpec(0, 149, 169, 196),
	    new ColorSpec(50, 164, 180, 203),
	    new ColorSpec(200, 171, 192, 213),
	    new ColorSpec(600, 185, 201, 224),
	    new ColorSpec(1000, 201, 214, 231),
	    new ColorSpec(2000, 218, 226, 239),
	    new ColorSpec(3000, 239, 243, 252),
	    new ColorSpec(4000, 241, 245, 254)
	}, 0x00FFFFFF);    	
	}
	
	/**
	 * Adapted from:
	 * The Development and Rationale of Cross-blended Hypsometric Tints – Patterson, Jenny. Cartographic Perspectives, Number 69, 2011
	 * @author Miha
	 */
	public static class Historic {
		public static final ElevationColors IMHOF=new ElevationColors(new ColorSpec[] {
		    new ColorSpec(0, 183, 220, 177),
		    new ColorSpec(1200, 255, 245, 193),
		    new ColorSpec(3000, 255, 255, 255)
		}, 0x00FFFFFF);

		public static final ElevationColors JOHN_BARTHOLOMEW_JR = new ElevationColors(new ColorSpec[] {
		    new ColorSpec(0, 187, 203, 157),
		    new ColorSpec(250, 217, 228, 172),
		    new ColorSpec(750, 231, 193, 141),
		    new ColorSpec(1500, 208, 132, 120),
		    new ColorSpec(2200, 245, 245, 245),
		    new ColorSpec(3000, 255, 255, 255)
		}, 0x00FFFFFF);

		public static final ElevationColors INTL_MAP_WORLD = new ElevationColors(new ColorSpec[] {
		    new ColorSpec(0, 171, 215, 170),
		    new ColorSpec(750, 250, 247, 199),
		    new ColorSpec(1500, 250, 203, 183),
		    new ColorSpec(2000, 223, 197, 202),
		    new ColorSpec(3000, 255, 255, 255)
		}, 0x00FFFFFF);
	}

	//public static final ElevationColors DEFAULT = NaturalEarth.WARM_HUMID
	public static final ElevationColors DEFAULT = GISELLE_STANDARD; //Historic.IMHOF;
}
