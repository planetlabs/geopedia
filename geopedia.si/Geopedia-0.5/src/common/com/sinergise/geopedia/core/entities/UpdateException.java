package com.sinergise.geopedia.core.entities;

public class UpdateException extends Exception
{

	private static final long serialVersionUID = 7936296986462973275L;

	/** One of the constants below with the meaning of exception */
	public int type;
	
	/** In the case of T_CONCURRENT_UPDATE, the newer version, if known (otherwise < 0) */
	public long newerVersion;

	/** For other/undefined exceptions */
	public static final int T_UNKNOWN = 0;

	/** If update fails (i.e. DB is not available, or some other SQLException) */
	public static final int T_UPDATE_FAILED = 1;

	/** If update succeeds, but waiting for local host to sync takes too long */
	public static final int T_SYNC_FAILED = 2;

	/**
	 * If the data sent is not valid for some reason (e.g., style spec is
	 * unparseable)
	 */
	public static final int T_INVALID_DATA = 3;

	/** If the expected timestamp doesn't match the one in DB */
	public static final int T_CONCURRENT_UPDATE = 4;

	/** If nothing changed */
	public static final int T_NOTHING_CHANGED = 5;

	/** If there is no session id and/or it is not valid */
	public static final int T_NO_SESSION = 6;

	/** If the user is not logged in */
	public static final int T_NOT_LOGGED_IN = 7;

	/** If the user doesn't have permission to update */
	public static final int T_NO_PERMISSION = 8;

	/** If some state is invalid (see method description for details) */
	public static final int T_INVALID_STATE = 9;
	
	public UpdateException()
	{
		type = T_UNKNOWN;
	}

	public UpdateException(int type, long newVersion, String msg)
	{
		super(msg);
		this.newerVersion = newVersion;
		this.type = type;
	}

	public UpdateException(int type, long newVersion)
	{
		this(type, newVersion, null);
	}
	
	public UpdateException(int type)
	{
		this(type, -1, null);
	}
	
	public UpdateException(int type, String msg)
	{
		this(type, -1, msg);
	}

	public String getMessage()
	{
        String supMes = super.getMessage();
        if (supMes == null)
        	return getTypeStr();
        else
        	return getTypeStr()+": "+supMes;
	}
    
	public String getTypeStr()
	{
		switch (type) {
		case T_UNKNOWN:
			return "Neznana napaka";
		case T_UPDATE_FAILED:
			return "Sprememba ni bila uspešna";
		case T_SYNC_FAILED:
			return "Napaka pri sinhronizaciji strežnikov";
		case T_INVALID_DATA:
			return "Napačni podatki";
		case T_CONCURRENT_UPDATE:
			return "Sočasno spreminjanje podatkov";
		case T_NOTHING_CHANGED:
			return "Ni sprememb";
		case T_NO_SESSION:
			return "Seja ni vzpostavljena";
		case T_NOT_LOGGED_IN:
			return "Uporabnik ni prijavljen";
		case T_NO_PERMISSION:
			return "Uporabnik nima pravic";
		case T_INVALID_STATE:
			return "Napačno stanje strežnika";
			
		default:
			break;
		}
		return "Illegal type";
	}
}
