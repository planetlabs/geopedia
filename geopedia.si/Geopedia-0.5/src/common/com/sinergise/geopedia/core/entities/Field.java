package com.sinergise.geopedia.core.entities;


import java.util.HashSet;

import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.geopedia.core.entities.utils.EntityConsts.DataScope;


public class Field extends AbstractEntityWithDescription
{
	private static final long serialVersionUID = -7951158855659633722L;

	public static final String PROP_FORMAT_STRING="formatString";    
    public static final String PROP_LINK_TO_WIDGET = "linkToWidget";

    
    public enum  FieldType {
    	INTEGER(2), DECIMAL(3), FOREIGN_ID(4), PLAINTEXT(6), LONGPLAINTEXT(7), WIKITEXT(8), BLOB(9), DATETIME(10), DATE(11), STYLE(13), BOOLEAN(16), GEOMETRY(17);
    	
    	
    	private int identifier;
    	private FieldType(int identifier) {
    		this.identifier=identifier;
    	}
        
    	public int getIdentifier() {
    		return identifier;
    	}
    	public static FieldType forId(int id) {
    		for (FieldType ft:FieldType.values()) {
    			if (ft.identifier==id)
    				return ft;
    		}
    		throw new IllegalArgumentException("No such FieldType id="+id);
    	}
    }

    public enum FieldVisibility {
    	ALL_VISIBLE(0), ALL_HIDDEN(4), VIEW_HIDDEN(2), EDIT_HIDDEN(64);    	
    	public int value;
    	private FieldVisibility(int value){
    		this.value=value;
    	}
    	
    	public  static FieldVisibility forInt(int i) {
    		for (FieldVisibility fv:values()) {
    			if (fv!=ALL_VISIBLE && (fv.value&i)==fv.value) {
    				return fv;
    			}
    		}
    		return ALL_VISIBLE;
    	}
    	
    	public boolean canView() {
    		if (this==ALL_HIDDEN || this==VIEW_HIDDEN)
    			return false;
    		return true;
    	}

		public boolean canEdit() {
			if (this == EDIT_HIDDEN || this == ALL_HIDDEN) {
				return false;
			}
			return true;
		}
    }
    
    public enum FieldFlags {
    	MANDATORY(1), DELETED(16), READONLY(32), FULLTEXTEXCLUDE(8);
    	public int value;
    	private FieldFlags(int value) {
    		this.value=value;
    	}
    	
    	public static HashSet<FieldFlags> forInt(int i) {
    		HashSet<FieldFlags> set = new HashSet<FieldFlags>();
    		for (FieldFlags ff:values()) {
    			if ((ff.value&i)==ff.value) {
    				set.add(ff);
    			}
    		}
    		return set;
    	}

		public static HashSet<FieldFlags> fullSet() {
			HashSet<FieldFlags> set = new HashSet<FieldFlags>();
    		for (FieldFlags ff:values()) {
    			set.add(ff);
    		}
    		return set;
		}
    }
    
    
    private FieldVisibility fieldVisibility = FieldVisibility.ALL_VISIBLE;
    private HashSet<FieldFlags> fieldFlags = new HashSet<FieldFlags>();
    public int tableId;
    public FieldType type = FieldType.PLAINTEXT;
    public int refdTableId;
    public int order;
    public String defaultValueString;
    public Table table;
    public Table refdTable;
    public long lastTableMeta;
    public StateGWT properties;
    
    /** Used for importing, ignore everywhere else */
    public transient String sysId;

    /**
     * Means the field is mandatory - the user must enter data
     */
    public static final int F_MANDATORY = 1;
    public static final int F_HIDDEN = 2;
    public static final int F_DELETED = 4;
    public static final int F_EXCLUDE_FROM_FULL_TEXT = 8;
    public static final int F_PERMENANTLY_DELETED = 16;
    public static final int F_VISIBLE_NON_EDITING = 32; // vidno, vendar se ne ureja
    public static final int F_INVISIBLE_WHILE_EDITING = 64; // nevidno pri urejanju
    
    /** = 2+4+8+16 = F_HIDDEN + F_DELETED + F_EXCLUDE_FROM_FULL_TEXT + F_PERMENANTLY_DELETED */
    public static final int FLAGS_PERMANENTLY_DELETE = 30;

    public static final Field[] emptyArray = new Field[0];

    
    @Override
	public Field clone(DataScope scope) {
    	Field cloned = new Field();
    	cloned.dataScope=scope;
    	cloned.id = id;
    	cloned.setName(getName());
    	
    	cloned.descDisplayableHtml=descDisplayableHtml;
    	cloned.descRawHtml = descRawHtml;
    	cloned.defaultValueString = defaultValueString;
    	cloned.lastTableMeta=lastTableMeta;
    	cloned.setType(type);
        cloned.setFlags(getFlags());
        cloned.order=order;
        cloned.tableId=tableId;
        cloned.refdTableId=refdTableId;
        if (refdTable!=null) {
        	cloned.refdTable = refdTable.clone(DataScope.BASIC);
        }
        if (properties!=null && !properties.isEmpty()) {
        	cloned.properties = new StateGWT();
        	cloned.properties.setFrom(properties, true);
        }
    	return cloned;
	}
    
    public Field()
    {
    //
    }
    
    
    public void setFlags(int flags) {
    	fieldVisibility = FieldVisibility.forInt(flags);
    	fieldFlags = FieldFlags.forInt(flags);
    }

	public int getFlags() {		
		int flags = fieldVisibility.value;
		for (FieldFlags ff:fieldFlags) {
			flags|=ff.value;
		}
		return flags;
	}

    

    public boolean isLongType()
    {
        return type == FieldType.INTEGER || type == FieldType.FOREIGN_ID
                || type == FieldType.DATETIME || type == FieldType.BOOLEAN
                || type == FieldType.BLOB;
    }

	
    
    public String toString() {
        StringBuffer bld=new StringBuffer();
        bld.append('\'');
        bld.append(getName());
        bld.append("' ");
        bld.append(type.name());
        if (type==FieldType.FOREIGN_ID) {
            bld.append('(');
            bld.append(refdTableId);
            if (refdTable != null) {
                bld.append(": '");
                bld.append(refdTable.getName());
                bld.append('\'');
            }
            bld.append(')');
        }
        return bld.toString();
    }
    
    
    /**
     * This property is used to minimize values that take a lot of space within a feature info,
     * for example a description that takes more than a page.
     * User can click a link to display original widget.
     * When can click a link again to hide original widget.
     * @return true if field_properties in database has PROP_LINK_TO_WIDGET set to true
     */
    public boolean linkToWidget() {
        return properties==null?false:properties.getBoolean(PROP_LINK_TO_WIDGET, false);
    }


	public void setDescription(String value) {
		this.descRawHtml = value;
	}


	public void setType(FieldType type) {
		this.type=type;
	}


	public void setOrder(int order) {
		this.order=order;
	}


	

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Field other = (Field) obj;
		if (getId() != other.getId())
			return false;
		return true;
	}


	


	


	public boolean isMandatory() {
		return hasFlag(FieldFlags.MANDATORY);
	}


	public boolean hasFlag(FieldFlags flag) {
		return fieldFlags.contains(flag);
	}



	@Override
	public void setDeleted(boolean on) {
		if (on) {
			fieldFlags.add(FieldFlags.DELETED);
		} else {
			fieldFlags.remove(FieldFlags.DELETED);
		}
		
	}

	@Override
	public boolean isDeleted() {
		return hasFlag(FieldFlags.DELETED);
	}


	public FieldVisibility getVisibility() {
		return fieldVisibility;
	}
	public void setVisibility(FieldVisibility visibility) {
		this.fieldVisibility = visibility;
	}


	public HashSet<FieldFlags> getFieldFlags() {
		return fieldFlags;
	}

	public void setFieldFlags(HashSet<FieldFlags> fieldFlags) {
		this.fieldFlags = fieldFlags;
	}
	
	
	public void setReferencedTable(Table table) {
		this.refdTable=table;
		if (table==null) {
			this.refdTableId=-1;
		} else {
			this.refdTableId=table.getId();
		}
	}


	public Table getReferencedTable() {
		return refdTable;
	}


	public Integer getTableId() {
		if (table==null)
			return tableId;
		return table.getId();
	}

	public FieldType getType() {
		return type;
	}

	public Integer getUIPosition() {
		return order;
	}
	
}
