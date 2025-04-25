package com.sinergise.java.geometry.io.shp;

/**
 * @author dragan
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DBFField {
    public static final char TYPE_CHARACTER = 'C';
    public static final char TYPE_NUMERIC = 'N';
    public static final char TYPE_LOGICAL = 'L';
    public static final char TYPE_DATE = 'D';
    public static final char TYPE_DOUBLE = 'F';

	public char  type; // can be C, D, N, L, M
    
    /** Total length of the field in ASCII */
	public int len;
    
    /** Number of decimal places - used with TYPE_NUMERIC and TYPE_DOUBLE */
	public short decimals;
    
	int   offset;
    
    /** dbf field names are 10 chars long max. */
	public String name = "";
    
    public DBFField() {
        
    }
    
    public DBFField(String name, char type, int len, byte decimals) {
        this.name=name;
        this.type=type;
        this.len=len;
        this.decimals=decimals;
    }
    
    public void setName(String value) {
    	name = value.trim();
    	if (name.length()>10)
    		name = name.substring(0, 10);
    }
    
    @Override
    public String toString() {
    	return name;
    }
}
