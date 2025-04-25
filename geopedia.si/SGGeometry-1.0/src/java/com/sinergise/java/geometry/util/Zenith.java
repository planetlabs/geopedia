package com.sinergise.java.geometry.util;

/**
 * Defines the solar declination used in computing the sunrise/sunset.
 */
public class Zenith {
	
	private final double degrees;

    /** @param zenith in degrees */ 
    public Zenith(double degrees) {
        this.degrees = degrees;
    }

    public double degrees() {
        return degrees;
    }
    
    public double radians() {
    	return Math.toRadians(degrees);
    }
    

    // Various static instances for convenience
    
    /** Astronomical sunrise/set is when the sun is 18 degrees below the horizon. */
    public static final Zenith ASTRONOMICAL = new Zenith(108);

    /** Nautical sunrise/set is when the sun is 12 degrees below the horizon. */
    public static final Zenith NAUTICAL = new Zenith(102);

    /** Civil sunrise/set (dawn/dusk) is when the sun is 6 degrees below the horizon. */
    public static final Zenith CIVIL = new Zenith(96);

    /** Official sunrise/set is when the sun is 50' below the horizon. */
    public static final Zenith OFFICIAL = new Zenith(90. + 5./6.);

}