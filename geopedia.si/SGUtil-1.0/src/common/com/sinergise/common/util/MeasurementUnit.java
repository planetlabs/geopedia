package com.sinergise.common.util;

/**
 * Sources: 
 * - http://en.wikipedia.org/wiki/French_units_of_measurement
 * - conversion table XLS from the Mauritius MHL Cartography Dept.
 * - http://www.pfos.hr/~dsego/ispitna_literatura/Prilozi/Mjere%20i%20mjerne%20jedinice.pdf
 * - http://en.wikipedia.org/wiki/Hvat
 */
public enum MeasurementUnit {
	
	METRE(MeasurementType.LENGTH, "METRE", "m", 1, null),
	INCH(MeasurementType.LENGTH, "INCH","\"", 0.0254, METRE),
	FOOT(MeasurementType.LENGTH, "FOOT", "ft", 0.3048, METRE), // 1 ft = 12"
	YARD(MeasurementType.LENGTH, "YARD", "yd", 0.9144, METRE), // 1 yd = 3 ft
	MILE(MeasurementType.LENGTH, "MILE", "m", 1609.344, METRE), // 1 mi = 1760 yd
	NAUTICAL_MILE(MeasurementType.LENGTH, "NAUTICAL MILE", "nmi",1852, METRE), // 1 nmi = 1852 m
	
	PIED_DU_ROI(MeasurementType.LENGTH, "PIED DU ROI", "pied du roi", 0.324845, METRE),
	
	FATHOM(MeasurementType.LENGTH, "FATHOM", "", 1.8288, METRE), //international hvat
	HVAT(MeasurementType.LENGTH, "HVAT", "hv",  1.896484, METRE), //hrvatski hvat
	
	SQ_METRE(MeasurementType.AREA, "SQ METRE", "m\u00b2"),
	SQ_FOOT(MeasurementType.AREA, "SQ FOOT", "ft\u00b2", 0.09290304, SQ_METRE),
	ARE(MeasurementType.AREA, "ARE", "a", 100, SQ_METRE),
	HECTARE(MeasurementType.AREA, "HECTARE", "ha", 10000, SQ_METRE),
	SQ_KM(MeasurementType.AREA, "SQ KM", "km\u00b2", 1000000, SQ_METRE),
	ACRE(MeasurementType.AREA, "ACRE", "acre", 4046.8564224, SQ_METRE),
	
	PIED_CARRE(MeasurementType.AREA, "SQ CARRÉ", "pied\u00b2", 0.105524274025, SQ_METRE), // Square of pied du roi
	TOISE_CARRE(MeasurementType.AREA, "SQ TOISE", "T", 3.7988738649, SQ_METRE), // Toise carree equals 36 pied carre
	PERCHE_CARREE(MeasurementType.AREA, "SQ PERCHE", "sq. perche",42.20970961, SQ_METRE), //  Perche (ordinaire) carree (local use). Square 20 pied on each side = 400 pied carre.  ~ (20*PIED_DU_ROI)^2
	ARPENT_CARREE(MeasurementType.AREA, "SQ ARPENT", "arp", 4220.970961, SQ_METRE), // Arpent carre. Square 10 perches on each side = 100 perches carrees.
	
	SQ_HVAT(MeasurementType.AREA, "SQ HVAT", "hv\u00b2",  3.59665, SQ_METRE), // hrvatski hvat
	KATASTRSKO_JUTRO(MeasurementType.AREA, "KAT. JUTRO", "kat. jutro", 5754.64, SQ_METRE), // 1600 square hvats or 1 ral
	RAL(MeasurementType.AREA, "RAL", "ral", 5754.64, SQ_METRE), // 1600 square hvats 
	MOTIKA_ZEMLJE(MeasurementType.AREA, "MOTIKA ZEMLJE", "motika zemlje", 800, SQ_METRE),
	DAN_ORANJA(MeasurementType.AREA, "DAN ORANJA", "dan oranja", 4000, SQ_METRE),
	LANAC(MeasurementType.AREA, "LANAC", "lanac", 793, SQ_METRE), // 2000 square hvats
	DULUM(MeasurementType.AREA, "DULUM", "dulum", 1000, SQ_METRE)
	;
	
	public enum MeasurementType {
		LENGTH, AREA, VOLUME, ANGLE;
	}
	
	public MeasurementType type;
	public String name;
	public String denotation;
	public double conversionFactor;
	private MeasurementUnit baseUnit;
	
	private MeasurementUnit(MeasurementType type, String name, String denotation) {
		this.type = type;
		this.name = name;
		this.denotation = denotation;
		this.conversionFactor = 1;
		this.baseUnit = this;
	}
	
	private MeasurementUnit(MeasurementType type, String name, String denotation, double conversionFactor, MeasurementUnit baseUnit) {
		this.type = type;
		this.name = name;
		this.denotation = denotation;
		this.conversionFactor = conversionFactor;
		this.baseUnit = baseUnit;
	}
	
	public double convertToBase(double amountInUnits) {
		return amountInUnits*conversionFactor;
	}
	
	public double getConversionFactor() {
		return conversionFactor;
	}
	
	public MeasurementUnit getBaseUnit() {
		if (baseUnit == null) {
			return this;
		}
		return baseUnit;
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
    
    public double convertTo(double value, MeasurementUnit toUnit) {
        if (!this.getBaseUnit().equals(toUnit.getBaseUnit())) {
            throw new IllegalArgumentException("Units "+name+" and "+toUnit.name+" are not compatible.");
        }
        return toUnit.convertFromBase(convertToBase(value));
    }
    
    public double convertFrom(double value, MeasurementUnit fromUnit) {
        if (!this.getBaseUnit().equals(fromUnit.getBaseUnit())) {
            throw new IllegalArgumentException("Units "+name+" and "+fromUnit.name+" are not compatible.");
        }
        return convertFromBase(fromUnit.convertToBase(value));
    }
    
}
