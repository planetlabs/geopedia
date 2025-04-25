package com.sinergise.geopedia.core.exceptions;

import com.sinergise.geopedia.core.entities.Field;


public class TableDataException extends EnumeratedException{
	private static final long serialVersionUID = -1158385720931302658L;

	public enum Type {
	UNKNOWN,
	ILLEGAL_DESCRIPTION,
	STYLE_ERROR,
	REPTEXT_ERROR,
	FIELD_HAS_STYLE_REFERENCE,
	FIELD_HAS_REPTEXT_REFERENCE,
	TABLE_IS_REFERENCED
	};
	
	private String additionalError = null;
	private Integer referencingTableId = null;
	private Field field  = null;
	
	protected TableDataException() {
		super(Type.UNKNOWN);
	}
	private TableDataException(Type type) {
		super(type);
	}

	private TableDataException(Type type, String additionalError) {
		super(type);
		this.additionalError=additionalError;
	}
	public static final GeopediaException create(Type type) {
		return new GeopediaException(new TableDataException(type));
	}	
	
	public static final GeopediaException create(Type type, String additionalError) {
		return new GeopediaException(new TableDataException(type, additionalError));
	}
	
	public static final GeopediaException create(Type type, Field fld, int referencingTableId) {
		TableDataException tde = new TableDataException(type);
		tde.referencingTableId = referencingTableId;
		tde.field = fld;
		return new GeopediaException(tde);
	}	
	
	public static final GeopediaException createTableIsReferencedByTable(int referencingTableId) {
		TableDataException tde = new TableDataException(Type.TABLE_IS_REFERENCED);
		tde.referencingTableId = referencingTableId;
		return new GeopediaException(tde);
	}
	@Override
	public Type getType() {
		return (Type) type;
	}	

}