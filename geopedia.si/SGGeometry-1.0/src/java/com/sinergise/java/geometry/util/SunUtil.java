/**
 * 
 */
package com.sinergise.java.geometry.util;

import java.util.Calendar;

/**
 * @author mabatic
 *
 * Utility class for calculations of sun position, its sunset/sunrise time etc
 * from http://williams.best.vwh.net/sunrise_sunset_algorithm.htm (retrieved 22.5.2013)
 * 
 * 
Source:
	Almanac for Computers, 1990
	published by Nautical Almanac Office
	United States Naval Observatory
	Washington, DC 20392

Inputs:
	day, month, year:      date of sunrise/sunset
	latitude, longitude:   location for sunrise/sunset
	zenith:                Sun's zenith for sunrise/sunset
	  offical      = 90 degrees 50'
	  civil        = 96 degrees
	  nautical     = 102 degrees
	  astronomical = 108 degrees

	NOTE: longitude is positive for East and negative for West
    NOTE: the algorithm assumes the use of a calculator with the
      	trig functions in "degree" (rather than "radian") mode. Most
        programming languages assume radian arguments, requiring back
        and forth convertions. The factor is 180/pi. So, for instance,
        the equation RA = atan(0.91764 * tan(L)) would be coded as RA
        = (180/pi)*atan(0.91764 * tan((pi/180)*L)) to give a degree
        answer with a degree input for L.


1. first calculate the day of the year
	N1 = floor(275 * month / 9)
	N2 = floor((month + 9) / 12)
	N3 = (1 + floor((year - 4 * floor(year / 4) + 2) / 3))
	N = N1 - (N2 * N3) + day - 30

2. convert the longitude to hour value and calculate an approximate time
	lngHour = longitude / 15
	if rising time is desired:
	  t = N + ((6 - lngHour) / 24)
	if setting time is desired:
	  t = N + ((18 - lngHour) / 24)

3. calculate the Sun's mean anomaly
	M = (0.9856 * t) - 3.289

4. calculate the Sun's true longitude
	L = M + (1.916 * sin(M)) + (0.020 * sin(2 * M)) + 282.634
	NOTE: L potentially needs to be adjusted into the range [0,360) by adding/subtracting 360

5a. calculate the Sun's right ascension
	RA = atan(0.91764 * tan(L))
	NOTE: RA potentially needs to be adjusted into the range [0,360) by adding/subtracting 360

5b. right ascension value needs to be in the same quadrant as L
	Lquadrant  = (floor( L/90)) * 90
	RAquadrant = (floor(RA/90)) * 90
	RA = RA + (Lquadrant - RAquadrant)

5c. right ascension value needs to be converted into hours
	RA = RA / 15

6. calculate the Sun's declination
	sinDec = 0.39782 * sin(L)
	cosDec = cos(asin(sinDec))

7a. calculate the Sun's local hour angle
	cosH = (cos(zenith) - (sinDec * sin(latitude))) / (cosDec * cos(latitude))
	if (cosH >  1) 
	  the sun never rises on this location (on the specified date)
	if (cosH < -1)
	  the sun never sets on this location (on the specified date)

7b. finish calculating H and convert into hours
	if if rising time is desired:
	  H = 360 - acos(cosH)
	if setting time is desired:
	  H = acos(cosH)
	H = H / 15

8. calculate local mean time of rising/setting
	T = H + RA - (0.06571 * t) - 6.622

9. adjust back to UTC
	UT = T - lngHour
	NOTE: UT potentially needs to be adjusted into the range [0,24) by adding/subtracting 24

10. convert UT value to local time zone of latitude/longitude
	localT = UT + localOffset

 */
public class SunUtil {

	public static Calendar getSunrise(double longitude, double latitude, double elevation, Calendar date){
		return getSunriseSunset(longitude, latitude, elevation, date, true, Zenith.OFFICIAL);
	}

	public static Calendar getSunrise(double longitude, double latitude, double elevation, Calendar date, Zenith sunZenith){
		return getSunriseSunset(longitude, latitude, elevation, date, true, sunZenith);
	}

	public static Calendar getSunset(double longitude, double latitude, double elevation, Calendar date){
		return getSunriseSunset(longitude, latitude, elevation, date, false, Zenith.OFFICIAL);
	}

	public static Calendar getSunset(double longitude, double latitude, double elevation, Calendar date, Zenith sunZenith){
		return getSunriseSunset(longitude, latitude, elevation, date, false, sunZenith);
	}

	public static Calendar getSunriseSunset(double longitude, double latitude, double elevation, Calendar date, boolean sunrise, Zenith sunZenith){
		double solarEventTimeH = 0.0;
		double cosH = getSunLocalHourAngle(longitude, latitude, elevation, date, sunrise, sunZenith);

		if(cosH>1 || cosH<-1){
			return null; //the sun never sets/rises on this location (on the specified date)
		}

		if(sunrise){
			solarEventTimeH = 360. - Math.toDegrees(Math.acos(cosH));
		} else {
			solarEventTimeH = Math.toDegrees(Math.acos(cosH));
		}
		solarEventTimeH /= 15.;

		double sunRightAscensionInHours = getSunRightAscensionInHours(longitude, date, sunrise);
		double approxTime = getApproxTime(longitude, date, sunrise);

		double solarLocalMeanTime =  solarEventTimeH + sunRightAscensionInHours - (0.06571 * approxTime) - 6.622;
		double solarUniversalTime = solarLocalMeanTime - getLongitudeHour(longitude);
		if(solarUniversalTime>24) solarUniversalTime -= 24.;

		return getDate(solarUniversalTime, date);
	}



	/**
	 * @param longitude
	 * @param latitude
	 * @param elevation
	 * @param date
	 * @param sunrise
	 * @param sunZenith
	 * @return Sun's local hour angle (cosH from equation), in radians
	 */
	private static double getSunLocalHourAngle(double longitude, double latitude, double elevation, Calendar date, boolean sunrise, Zenith sunZenith){
		double altitudeOfSolarDisc = sunZenith.degrees();
		if(elevation>0.0){
			altitudeOfSolarDisc -= 2.076*Math.sqrt(elevation)/60.;
		}
		double cosZenith = Math.cos(Math.toRadians(altitudeOfSolarDisc));
		double sunDeclination = getSunDeclination(longitude, date, sunrise);

//		6. calculate the Sun's declination
//		sinDec = 0.39782 * sin(L)
//		cosDec = cos(asin(sinDec))
//
//	7a. calculate the Sun's local hour angle
//		cosH = (cos(zenith) - (sinDec * sin(latitude))) / (cosDec * cos(latitude))
//		if (cosH >  1) 
//		  the sun never rises on this location (on the specified date)
//		if (cosH < -1)
//		  the sun never sets on this location (on the specified date)
		
		double cosH = cosZenith - Math.sin(Math.toRadians(sunDeclination)) * Math.sin(Math.toRadians(latitude));
		cosH /= (Math.cos(Math.toRadians(sunDeclination)) * Math.cos(Math.toRadians(latitude)));

		return cosH;

	}



	/**
	 * @param longitude
	 * @param date
	 * @param sunrise (false = sunset)
	 * @return Sun's declination (in degrees) at sunrise/sunset
	 */
	public static double getSunDeclination(double longitude, Calendar date, boolean sunrise){
		double sunTrueLongitude = getSunTrueLongitude(longitude, date, sunrise);
		return getSunDeclination(sunTrueLongitude);
	}

	private static double getSunDeclination(double sunLongitude){
		double sinDec = 0.39782 * Math.sin(Math.toRadians(sunLongitude));
		return Math.toDegrees(Math.asin(sinDec));
	}

	/**
	 * @param longitude
	 * @param date
	 * @param sunrise (false = sunset)
	 * @return Sun's right ascension (in degrees)
	 */
	public static double getSunRightAscension(double longitude, Calendar date, boolean sunrise){
		double sunTrueLongitude = getSunTrueLongitude(longitude, date, sunrise);
		return getSunRightAscension(sunTrueLongitude);
	}

	private static double getSunRightAscension(double sunLongitude){
		double sunRightAscension = Math.toDegrees(Math.atan(0.91764 * Math.tan(Math.toRadians(sunLongitude))));
		if (sunRightAscension > 360) {
			sunRightAscension -= 360;
		}
		return sunRightAscension;
	}

	/**
	 * @param longitude
	 * @param date
	 * @param sunrise (false = sunset)
	 * @return Sun's right ascension (in hours)
	 */
	public static double getSunRightAscensionInHours(double longitude, Calendar date, boolean sunrise){
		double sunTrueLongitude = getSunTrueLongitude(longitude, date, sunrise);
		double sunRightAscension = getSunRightAscension(longitude, date, sunrise);
		return getSunRightAscensionInHours(sunTrueLongitude, sunRightAscension);
	}


	private static double getSunRightAscensionInHours(double sunLongitude, double sunRightAscension){
		double Lquadrant  = (Math.floor(sunLongitude/90.) * 90);
		double RAquadrant = (Math.floor(sunRightAscension/90.) * 90);

		sunRightAscension += (Lquadrant - RAquadrant);
		return sunRightAscension/15.;
	}

	/**
	 * @param longitude
	 * @param date
	 * @param sunrise (false = sunset)
	 * @return true Sun Longitude (in degrees)
	 */
	public static double getSunTrueLongitude(double longitude, Calendar date, boolean sunrise){
		double sunMeanAnomaly = getSunMeanAnomaly(longitude, date, sunrise);
		return getSunTrueLongitude(sunMeanAnomaly);
	}

	private static double getSunTrueLongitude(double meanAnomaly){
		double sunLongitude = meanAnomaly;
		sunLongitude += (1.916 * Math.sin(   Math.toRadians(meanAnomaly) ));
		sunLongitude += (0.020 * Math.sin( 2*Math.toRadians(meanAnomaly) ));
		sunLongitude += 282.634;
		if (sunLongitude > 360) {
			sunLongitude -= 360;
		}
		return sunLongitude;
	}


	/**
	 * @param longitude
	 * @param date
	 * @param sunrise (false = sunset)
	 * @return Sun's mean anomaly (in degrees)
	 */
	public static double getSunMeanAnomaly(double longitude, Calendar date, boolean sunrise){
		double approxTime = getApproxTime(longitude, date, sunrise);
		return getSunMeanAnomaly(approxTime);
	}

	private static double getSunMeanAnomaly(double approxTime){
		return (0.9856 * approxTime) - 3.289;
	}


	/**
	 * @param longitude
	 * @param date
	 * @param sunrise
	 * @return Approximate time
	 */
	public static double getApproxTime(double longitude, Calendar date, boolean sunrise){
		double longitudeHour = getLongitudeHour(longitude);

		double approxTime=0.0;
		
		int dayOfTheYear = getDayOfTheYear(date);
		if(sunrise){
			approxTime = dayOfTheYear + ((  6. - longitudeHour) / 24.);
		} else {
			approxTime = dayOfTheYear + (( 18. - longitudeHour) / 24.);
		}

		return approxTime;
	}

	private static double getLongitudeHour(double longitude) {
		return longitude/15.;
	}

	private static int getDayOfTheYear(Calendar date){
		return date.get(Calendar.DAY_OF_YEAR);
	}

	private static Calendar getDate(double solarUniversalTime, Calendar date) {
		double utcOffSet = getUTCOffSet(date);
		double dstAdjustedTime = adjustForDST(solarUniversalTime + utcOffSet, date);
		return getLocalTimeAsCalendar(dstAdjustedTime,date);
	}
	
	private static Calendar getLocalTimeAsCalendar(double localTimeParam, Calendar date) {

        // Create a clone of the input calendar so we get locale/timezone information.
        Calendar resultTime = (Calendar) date.clone();

        double localTime = localTimeParam;
        if (localTime < -1) {
            localTime += 24.;
            // return sunset/sunrise of today
            // resultTime.add(Calendar.HOUR_OF_DAY, -24);
        }

        int hour = (int)localTime;
        int minutes = (int)((localTime - hour) * 60.);
        
        if (hour == 24) {
            hour = 0;
        }

        // Set the local time
        resultTime.set(Calendar.HOUR_OF_DAY, hour);
        resultTime.set(Calendar.MINUTE, minutes);
        resultTime.set(Calendar.SECOND, 0);
        resultTime.setTimeZone(date.getTimeZone());

        return resultTime;
    }

	private static double adjustForDST(double localMeanTime, Calendar date) {
		double localTime = localMeanTime;
		if( date.getTimeZone()!= null ){
			if (date.getTimeZone().inDaylightTime(date.getTime())) {
				localTime = localTime + 1.0;
			}
		}
		if (localTime > 24.0) {
			localTime -= 24.0;
		}
		return localTime;
	}


	private static double getUTCOffSet(Calendar date) {
		int offSetInMillis = date.get(Calendar.ZONE_OFFSET);
		return offSetInMillis / 3600000.;
	}

}
