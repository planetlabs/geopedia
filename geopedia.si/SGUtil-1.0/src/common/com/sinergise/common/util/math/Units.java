package com.sinergise.common.util.math;

import java.io.Serializable;

import com.sinergise.common.util.ArrayUtil;


/**
 * Sources: 
 * - http://en.wikipedia.org/wiki/French_units_of_measurement
 * - conversion table XLS from the Mauritius MHL Cartography Dept.
 */
public class Units {
	public static final class Unit implements Serializable {
		
		private static final long serialVersionUID = 3486683443856903182L;
		
		public String name;
		public String denotation;
		public double conversionFactor;
		public Unit baseUnit;
		
		@Deprecated
		/** Serialization only! */
		protected Unit() {
			this(null);
		}
		
		private Unit(String name) {
			this(name, null);
		}
		
		Unit(String name, String denotation) {
			this.name=name;
			this.denotation = denotation;
			this.conversionFactor=1;
			this.baseUnit=this;
		}
		
		public Unit(String name, String denotation, double conversionFactor, Unit baseUnit) {
			this.name = name;
			this.denotation = denotation;
			this.conversionFactor = conversionFactor;
			this.baseUnit = baseUnit;
		}
		
		public boolean isBase() {
			return baseUnit==this;
		}
		
		public double convertToBase(double amountInUnits) {
			return amountInUnits*conversionFactor;
		}
		
		public double getConversionFactor() {
			return conversionFactor;
		}
		
		@Override
		public String toString() {
			if(denotation != null)
				return denotation;
			return name;
		}

        public double convertFromBase(double valueInBase) {
            return valueInBase / conversionFactor;
        }
        
        public double convertTo(double value, Unit toUnit) {
            if (!this.baseUnit.equals(toUnit.baseUnit)) {
                throw new IllegalArgumentException("Units "+name+" and "+toUnit.name+" are not compatible.");
            }
            return toUnit.convertFromBase(convertToBase(value));
        }
        
        public double convertFrom(double value, Unit fromUnit) {
            if (!this.baseUnit.equals(fromUnit.baseUnit)) {
                throw new IllegalArgumentException("Units "+name+" and "+fromUnit.name+" are not compatible.");
            }
            return convertFromBase(fromUnit.convertToBase(value));
        }
	}
	
	public static final Unit NONE = new Unit("", "");
	public static final Unit PERCENT = new Unit("Percent", "%", 0.01, NONE);
	public static final Unit CURRENCY_MUR = new Unit("Mauritian Rupee", "MUR");
	public static final Unit METRE = new Unit("METRE", "m");
	public static final Unit SQ_METRE = new Unit("SQ METRE", "m\u00b2");

	public static final Unit SECOND = new Unit("second", "s");
	public static final Unit MINUTE = new Unit("minute", "min", 60, SECOND);
	public static final Unit HOUR = new Unit("hour", "h", 60, MINUTE);
	public static final Unit DAY = new Unit("day", "d", 24, HOUR);
	/**
	 * Julian year, 356.25 days
	 */
	public static final Unit YEAR = new Unit("year", "a", 365.25, DAY);

	public static final Unit INCH = new Unit("INCH","\"",0.0254, METRE);
	/**
	 * 1 ft = 12"
	 */
	public static final Unit FOOT = new Unit("FOOT", "ft", 0.3048, METRE);
	/**
	 * 1 yd = 3 ft
	 */
    public static final Unit YARD = new Unit("YARD", "yd", 0.9144, METRE);
    
    /**
     * 1 mi = 1760 yd
     */
    public static final Unit MILE = new Unit("MILE","m",1609.344, METRE);
    
    /**
     * 1 nmi = 1852 m
     */
    public static final Unit NAUTICAL_MILE = new Unit("NAUTICAL MILE","nmi",1852, METRE);
    
	public static final Unit PIED_DU_ROI = new Unit("PIED DU ROI", "pied du roi", 0.324845, METRE);
	public static final Unit SQ_FOOT = new Unit("SQ FOOT", "ft\u00b2", 0.09290304, SQ_METRE);
	public static final Unit HECTARE = new Unit("HECTARE", "ha", 10000, SQ_METRE);
    public static final Unit SQ_KM = new Unit("SQ KM", "km\u00b2", 1000000, SQ_METRE);
	public static final Unit ACRE = new Unit("ACRE", "acre",4046.8564224, SQ_METRE);
	/**
	 * Square of pied du roi.
	 */
    public static final Unit PIED_CARRE = new Unit("PIED CARRÉ", "pied\u00b2", 0.105524274025, SQ_METRE);
    /**
     * Toise carree equals 36 pied carre. 
     * */
	public static final Unit TOISE = new Unit("SQ TOISE", "T", 3.7988738649, SQ_METRE);
    /**
     * Perche (ordinaire) carree (local use). Square 20 pied on each side = 400 pied carre. 
     * */
	public static final Unit PERCHE_CARREE = new Unit("SQ PERCHE", "sq.perche", 42.20970961, SQ_METRE); // ~ (20*PIED_DU_ROI)^2

	/**
     * Arpent carre. Square 10 perches on each side = 100 perches carrees. 
     * */
	public static final Unit ARPENT = new Unit("ARPENT", "arp", 4220.970961, SQ_METRE);
	
	public static final Unit[] AREA_UNITS = new Unit[]{SQ_METRE, SQ_FOOT, HECTARE, ACRE, TOISE, PERCHE_CARREE, ARPENT};
	public static final Unit[] LENGTH_UNITS = new Unit[]{METRE, FOOT, PIED_DU_ROI};
	public static final Unit[] ALL_UNITS = ArrayUtil.concat(LENGTH_UNITS, AREA_UNITS, new Unit[AREA_UNITS.length+LENGTH_UNITS.length]);
	
	public static Unit createRatio(Unit unitTop, Unit unitBot) {
		if (unitTop.isBase() && unitBot.isBase()) {
			return new Unit(unitTop.name+" per "+unitBot.name, unitTop.denotation +"/" + unitBot.denotation);
		}
		throw new IllegalArgumentException("Cannot create a ratio unit with non-base units");
		
	}
}
