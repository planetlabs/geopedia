package com.sinergise.geopedia.core.constants;

public class Globals {


	/*------ both ------*/
	public static final String RAWHTMLHEADER = "***RAWHTML***";

	public static final int TILESIZE = 256;
	
	/*------ server ------*/
	/** Name of cookie which stores session ID */
	
	public static final String SESSION_COOKIE_WIDGET_PREFIX ="wgt";
	
	
	public static final String SESSION_HEADER = "X-GPD-Session";
	public static final String SESSION_PARAM_NAME="sid";
	
	public static final String BASELAYERS_SEPARATOR = "-";
	
	public static final int MAX_IMAGE_WIDTH = 3900;
	public static final int MAX_IMAGE_HEIGHT = 2500;
	
	/**
	 * <pre>
	 * This value increased when MAX_SYM_SIZE got bigger.
	 * This ensures that symbols of surrounding features are rendered on current tile 
	 * when they hover over more than one tile.
	 * Example: Feature with point geometry is located in some surrounding tile. 
	 * Its symbol size is set to 128. The symbol should be rendered in current tile 
	 * if distance from its centroid to MBR of current tile is less than 64.
	 * </pre>
	 */
	public static final int MAX_OFFSET = 64;
	
	
	/**
	 * minimum update interval for server session
	 */
	public static final long SESSION_UPDATE_MINIMUM=5L*60*1000; 
	/** Time after which cookie should expire (ms) */
	public static final long SESSION_TIMEOUT = 15*60*1000;

	
	
	public static final String REQ_PRINT="print";
	
	
	public static final String COPYRIGHT_DATE="2013";
	
	
	
	public enum PersonalGroup {
		PERSONAL(""), FAVOURITE("favorite"); // TODO: change db spelling to proper (british) english? 
		private String sqlValue;
		private PersonalGroup(String sqlValue) {
			this.sqlValue=sqlValue;
		}
		public String getSQLValue() {
			return sqlValue;
		}
		}
}
